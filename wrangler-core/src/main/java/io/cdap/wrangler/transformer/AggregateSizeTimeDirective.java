package io.cdap.wrangler.transformer;

import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveContext;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TransformationException;
import io.cdap.wrangler.api.TransformationService;
import io.cdap.wrangler.api.Usage;
import io.cdap.wrangler.api.annotations.Category;
import io.cdap.wrangler.api.annotations.Plugin;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.DirectiveName;
import io.cdap.wrangler.api.parser.TokenGroup;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.TokenizedLine;
import io.cdap.wrangler.api.parser.UsageDefinition;
import io.cdap.wrangler.api.parser.UsageDefinitionBuilder;
import io.cdap.wrangler.api.parser.UsageOutput;
import io.cdap.wrangler.api.parser.UsageType;
import io.cdap.wrangler.api.parser.TimeDuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A directive for aggregating byte sizes and time durations.
 * This directive accumulates total byte sizes and time durations from specified columns.
 */
@Plugin(type = Directive.TYPE, name = "aggregate-size-time", category = Category.AGGREGATE)
@Usage(
  directive = "aggregate-size-time",
  usage = "aggregate-size-time <source-size-column> <source-time-column> <target-size-column> <target-time-column> [size-unit] [time-unit] [aggregation-type]",
  description = "Aggregates byte sizes and time durations from specified columns."
)
public class AggregateSizeTimeDirective implements Directive {
  private String sourceSizeColumn;
  private String sourceTimeColumn;
  private String targetSizeColumn;
  private String targetTimeColumn;
  private String sizeUnit = "bytes";
  private String timeUnit = "nanoseconds";
  private String aggregationType = "total";
  private final AtomicLong totalBytes = new AtomicLong(0);
  private final AtomicLong totalTimeNanos = new AtomicLong(0);
  private long count = 0;

  @Override
  public UsageDefinition define() {
    UsageDefinitionBuilder builder = UsageDefinitionBuilder.builder("aggregate-size-time")
      .setDescription("Aggregates byte sizes and time durations from specified columns")
      .setCategory(Category.AGGREGATE)
      .setArguments(
        new UsageDefinition.Argument("source-size-column", UsageType.COLUMN_NAME, "Source column containing byte sizes"),
        new UsageDefinition.Argument("source-time-column", UsageType.COLUMN_NAME, "Source column containing time durations"),
        new UsageDefinition.Argument("target-size-column", UsageType.COLUMN_NAME, "Target column for total size"),
        new UsageDefinition.Argument("target-time-column", UsageType.COLUMN_NAME, "Target column for total/average time"),
        new UsageDefinition.Argument("size-unit", UsageType.TEXT, "Output unit for size (bytes, KB, MB, GB)", true),
        new UsageDefinition.Argument("time-unit", UsageType.TEXT, "Output unit for time (nanoseconds, milliseconds, seconds, minutes)", true),
        new UsageDefinition.Argument("aggregation-type", UsageType.TEXT, "Type of aggregation (total, average)", true)
      );
    return builder.build();
  }

  @Override
  public void initialize(TransformationService.Context context) throws DirectiveParseException {
    // Initialization is handled in parse method
  }

  @Override
  public void destroy() {
    // Cleanup if needed
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws TransformationException {
    if (rows == null || rows.isEmpty()) {
      return rows;
    }

    // Process each row and accumulate values
    for (Row row : rows) {
      try {
        // Get byte size value
        Object sizeValue = row.getValue(sourceSizeColumn);
        if (sizeValue != null) {
          ByteSize byteSize = new ByteSize(sizeValue.toString());
          totalBytes.addAndGet(byteSize.toBytes());
        }

        // Get time duration value
        Object timeValue = row.getValue(sourceTimeColumn);
        if (timeValue != null) {
          TimeDuration timeDuration = new TimeDuration(timeValue.toString());
          totalTimeNanos.addAndGet(timeDuration.toNanos());
        }

        count++;
      } catch (Exception e) {
        throw new TransformationException(
          String.format("Error processing row: %s", e.getMessage()),
          e
        );
      }
    }

    // If this is the last batch, create the result row
    if (context.isLastBatch()) {
      Row resultRow = new Row();
      
      // Convert and set total size
      long finalBytes = totalBytes.get();
      if ("average".equals(aggregationType)) {
        finalBytes = count > 0 ? finalBytes / count : 0;
      }
      ByteSize finalSize = new ByteSize(finalBytes, sizeUnit);
      resultRow.add(targetSizeColumn, finalSize.toString());

      // Convert and set total/average time
      long finalNanos = totalTimeNanos.get();
      if ("average".equals(aggregationType)) {
        finalNanos = count > 0 ? finalNanos / count : 0;
      }
      TimeDuration finalTime = new TimeDuration(finalNanos, timeUnit);
      resultRow.add(targetTimeColumn, finalTime.toString());

      List<Row> result = new ArrayList<>();
      result.add(resultRow);
      return result;
    }

    return rows;
  }

  @Override
  public void parse(TokenizedLine line) throws DirectiveParseException {
    TokenGroup group = line.group();
    if (group.size() < 4) {
      throw new DirectiveParseException(
        "aggregate-size-time requires at least 4 arguments: " +
        "<source-size-column> <source-time-column> <target-size-column> <target-time-column>"
      );
    }

    sourceSizeColumn = group.value(0, TokenType.COLUMN_NAME);
    sourceTimeColumn = group.value(1, TokenType.COLUMN_NAME);
    targetSizeColumn = group.value(2, TokenType.COLUMN_NAME);
    targetTimeColumn = group.value(3, TokenType.COLUMN_NAME);

    // Optional arguments
    if (group.size() > 4) {
      sizeUnit = group.value(4, TokenType.TEXT);
    }
    if (group.size() > 5) {
      timeUnit = group.value(5, TokenType.TEXT);
    }
    if (group.size() > 6) {
      aggregationType = group.value(6, TokenType.TEXT);
      if (!aggregationType.equals("total") && !aggregationType.equals("average")) {
        throw new DirectiveParseException("aggregation-type must be either 'total' or 'average'");
      }
    }
  }
} 
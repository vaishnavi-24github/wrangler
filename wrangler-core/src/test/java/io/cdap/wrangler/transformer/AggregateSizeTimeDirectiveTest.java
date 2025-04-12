package io.cdap.wrangler.transformer;

import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TransformationException;
import io.cdap.wrangler.api.TransformationService;
import io.cdap.wrangler.api.parser.TokenizedLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AggregateSizeTimeDirectiveTest {

  @Mock
  private ExecutorContext context;

  @Mock
  private TransformationService.Context serviceContext;

  private AggregateSizeTimeDirective directive;

  @Before
  public void setUp() {
    directive = new AggregateSizeTimeDirective();
  }

  @Test
  public void testBasicAggregation() throws Exception {
    // Setup test data
    List<Row> rows = new ArrayList<>();
    rows.add(createRow("size", "1KB", "time", "1s"));
    rows.add(createRow("size", "2KB", "time", "2s"));
    rows.add(createRow("size", "3KB", "time", "3s"));

    // Parse directive
    TokenizedLine line = TokenizedLine.parse("aggregate-size-time size time total_size total_time");
    directive.parse(line);

    // Execute with last batch
    when(context.isLastBatch()).thenReturn(true);
    List<Row> result = directive.execute(rows, context);

    // Verify results
    assertNotNull(result);
    assertEquals(1, result.size());
    Row resultRow = result.get(0);
    assertEquals("6KB", resultRow.getValue("total_size"));
    assertEquals("6s", resultRow.getValue("total_time"));
  }

  @Test
  public void testAverageAggregation() throws Exception {
    // Setup test data
    List<Row> rows = new ArrayList<>();
    rows.add(createRow("size", "1KB", "time", "1s"));
    rows.add(createRow("size", "2KB", "time", "2s"));
    rows.add(createRow("size", "3KB", "time", "3s"));

    // Parse directive with average aggregation
    TokenizedLine line = TokenizedLine.parse("aggregate-size-time size time total_size total_time bytes seconds average");
    directive.parse(line);

    // Execute with last batch
    when(context.isLastBatch()).thenReturn(true);
    List<Row> result = directive.execute(rows, context);

    // Verify results
    assertNotNull(result);
    assertEquals(1, result.size());
    Row resultRow = result.get(0);
    assertEquals("2KB", resultRow.getValue("total_size"));
    assertEquals("2s", resultRow.getValue("total_time"));
  }

  @Test
  public void testCustomUnits() throws Exception {
    // Setup test data
    List<Row> rows = new ArrayList<>();
    rows.add(createRow("size", "1024KB", "time", "3600s"));
    rows.add(createRow("size", "2048KB", "time", "7200s"));

    // Parse directive with custom units
    TokenizedLine line = TokenizedLine.parse("aggregate-size-time size time total_size total_time MB minutes");
    directive.parse(line);

    // Execute with last batch
    when(context.isLastBatch()).thenReturn(true);
    List<Row> result = directive.execute(rows, context);

    // Verify results
    assertNotNull(result);
    assertEquals(1, result.size());
    Row resultRow = result.get(0);
    assertEquals("3MB", resultRow.getValue("total_size"));
    assertEquals("180m", resultRow.getValue("total_time"));
  }

  @Test
  public void testEmptyRows() throws Exception {
    // Setup empty rows
    List<Row> rows = new ArrayList<>();

    // Parse directive
    TokenizedLine line = TokenizedLine.parse("aggregate-size-time size time total_size total_time");
    directive.parse(line);

    // Execute with last batch
    when(context.isLastBatch()).thenReturn(true);
    List<Row> result = directive.execute(rows, context);

    // Verify results
    assertNotNull(result);
    assertEquals(1, result.size());
    Row resultRow = result.get(0);
    assertEquals("0B", resultRow.getValue("total_size"));
    assertEquals("0ns", resultRow.getValue("total_time"));
  }

  @Test
  public void testNullValues() throws Exception {
    // Setup rows with null values
    List<Row> rows = new ArrayList<>();
    rows.add(createRow("size", null, "time", "1s"));
    rows.add(createRow("size", "2KB", "time", null));
    rows.add(createRow("size", "3KB", "time", "3s"));

    // Parse directive
    TokenizedLine line = TokenizedLine.parse("aggregate-size-time size time total_size total_time");
    directive.parse(line);

    // Execute with last batch
    when(context.isLastBatch()).thenReturn(true);
    List<Row> result = directive.execute(rows, context);

    // Verify results
    assertNotNull(result);
    assertEquals(1, result.size());
    Row resultRow = result.get(0);
    assertEquals("5KB", resultRow.getValue("total_size"));
    assertEquals("4s", resultRow.getValue("total_time"));
  }

  @Test
  public void testInvalidValues() throws Exception {
    // Setup rows with invalid values
    List<Row> rows = new ArrayList<>();
    rows.add(createRow("size", "invalid", "time", "1s"));
    rows.add(createRow("size", "2KB", "time", "invalid"));

    // Parse directive
    TokenizedLine line = TokenizedLine.parse("aggregate-size-time size time total_size total_time");
    directive.parse(line);

    // Execute with last batch
    when(context.isLastBatch()).thenReturn(true);
    List<Row> result = directive.execute(rows, context);

    // Verify results - invalid values should be skipped
    assertNotNull(result);
    assertEquals(1, result.size());
    Row resultRow = result.get(0);
    assertEquals("2KB", resultRow.getValue("total_size"));
    assertEquals("1s", resultRow.getValue("total_time"));
  }

  @Test(expected = DirectiveParseException.class)
  public void testInvalidArguments() throws Exception {
    // Parse directive with insufficient arguments
    TokenizedLine line = TokenizedLine.parse("aggregate-size-time size time");
    directive.parse(line);
  }

  @Test(expected = DirectiveParseException.class)
  public void testInvalidAggregationType() throws Exception {
    // Parse directive with invalid aggregation type
    TokenizedLine line = TokenizedLine.parse("aggregate-size-time size time total_size total_time bytes seconds invalid");
    directive.parse(line);
  }

  private Row createRow(String sizeCol, String sizeVal, String timeCol, String timeVal) {
    Row row = new Row();
    row.add(sizeCol, sizeVal);
    row.add(timeCol, timeVal);
    return row;
  }
} 
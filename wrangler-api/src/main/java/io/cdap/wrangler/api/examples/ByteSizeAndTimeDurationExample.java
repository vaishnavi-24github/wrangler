/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.examples;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Optional;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.List;

/**
 * Example directive that demonstrates how to use ByteSize and TimeDuration token types.
 * This directive is for demonstration purposes only and doesn't perform any actual data transformation.
 */
@Plugin(type = Directive.TYPE)
@Name("configure-resource")
@Categories(categories = {"example"})
@Description("Example directive that demonstrates how to use ByteSize and TimeDuration token types.")
public class ByteSizeAndTimeDurationExample implements Directive {
  public static final String NAME = "configure-resource";
  
  private ByteSize bufferSize;
  private TimeDuration timeout;
  private String column;

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
    builder.define("column", TokenType.COLUMN_NAME);
    builder.define("buffer", TokenType.BYTE_SIZE, Optional.TRUE);
    builder.define("timeout", TokenType.TIME_DURATION, Optional.TRUE);
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    this.column = ((ColumnName) args.value("column")).value();
    
    if (args.contains("buffer")) {
      this.bufferSize = (ByteSize) args.value("buffer");
    }
    
    if (args.contains("timeout")) {
      this.timeout = (TimeDuration) args.value("timeout");
    }
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) 
      throws DirectiveExecutionException, ErrorRowException {
    // This is just an example, so we don't actually modify the data
    // In a real directive, you would use the bufferSize and timeout values
    // to configure some resource or perform some operation
    
    for (Row row : rows) {
      // Just add the configuration information as a new column
      if (bufferSize != null) {
        row.add("buffer_size_bytes", bufferSize.toBytes());
        row.add("buffer_size_unit", bufferSize.getUnit());
      }
      
      if (timeout != null) {
        row.add("timeout_ms", timeout.toMilliseconds());
        row.add("timeout_unit", timeout.getUnit());
      }
    }
    
    return rows;
  }

  @Override
  public void destroy() {
    // No-op
  }
} 
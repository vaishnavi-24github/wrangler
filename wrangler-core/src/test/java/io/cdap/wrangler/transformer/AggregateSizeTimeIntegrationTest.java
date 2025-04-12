package io.cdap.wrangler.transformer;

import io.cdap.wrangler.api.RecipePipeline;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TransformationException;
import io.cdap.wrangler.api.TransformationService;
import io.cdap.wrangler.test.TestingRig;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AggregateSizeTimeIntegrationTest {

  private static final double TOLERANCE = 0.001;

  @Test
  public void testBasicAggregation() throws Exception {
    // Create sample log data with precise values
    String[] data = new String[] {
      "data_transfer_size,response_time",
      "1048576B,1000ms",    // 1MB, 1s
      "2097152B,2000ms",    // 2MB, 2s
      "3145728B,3000ms",    // 3MB, 3s
      "4194304B,4000ms",    // 4MB, 4s
      "5242880B,5000ms"     // 5MB, 5s
    };

    // Define the recipe
    String[] recipe = new String[] {
      "aggregate-size-time data_transfer_size response_time total_size_mb total_time_sec"
    };

    // Execute the recipe
    List<Row> results = TestingRig.execute(data, recipe);

    // Verify results with precise assertions
    assertNotNull(results);
    assertEquals(1, results.size());
    Row result = results.get(0);
    
    // Total size: 15MB (15 * 1024 * 1024 bytes)
    assertEquals(15.0, parseSizeValue(result.getValue("total_size_mb")), TOLERANCE);
    // Total time: 15 seconds (15000ms)
    assertEquals(15.0, parseTimeValue(result.getValue("total_time_sec")), TOLERANCE);
  }

  @Test
  public void testAverageAggregation() throws Exception {
    // Create sample log data with precise values
    String[] data = new String[] {
      "data_transfer_size,response_time",
      "1048576B,1000ms",    // 1MB, 1s
      "2097152B,2000ms",    // 2MB, 2s
      "3145728B,3000ms",    // 3MB, 3s
      "4194304B,4000ms",    // 4MB, 4s
      "5242880B,5000ms"     // 5MB, 5s
    };

    // Define the recipe with average aggregation
    String[] recipe = new String[] {
      "aggregate-size-time data_transfer_size response_time total_size_mb total_time_sec MB seconds average"
    };

    // Execute the recipe
    List<Row> results = TestingRig.execute(data, recipe);

    // Verify results with precise assertions
    assertNotNull(results);
    assertEquals(1, results.size());
    Row result = results.get(0);
    
    // Average size: 3MB (15MB / 5)
    assertEquals(3.0, parseSizeValue(result.getValue("total_size_mb")), TOLERANCE);
    // Average time: 3 seconds (15s / 5)
    assertEquals(3.0, parseTimeValue(result.getValue("total_time_sec")), TOLERANCE);
  }

  @Test
  public void testCustomUnits() throws Exception {
    // Create sample log data with precise values
    String[] data = new String[] {
      "data_transfer_size,response_time",
      "1073741824B,3600000ms",  // 1GB, 1h
      "2147483648B,7200000ms",  // 2GB, 2h
      "3221225472B,10800000ms"  // 3GB, 3h
    };

    // Define the recipe with custom units
    String[] recipe = new String[] {
      "aggregate-size-time data_transfer_size response_time total_size_gb total_time_hours GB hours"
    };

    // Execute the recipe
    List<Row> results = TestingRig.execute(data, recipe);

    // Verify results with precise assertions
    assertNotNull(results);
    assertEquals(1, results.size());
    Row result = results.get(0);
    
    // Total size: 6GB (6 * 1024 * 1024 * 1024 bytes)
    assertEquals(6.0, parseSizeValue(result.getValue("total_size_gb")), TOLERANCE);
    // Total time: 6 hours (21600000ms)
    assertEquals(6.0, parseTimeValue(result.getValue("total_time_hours")), TOLERANCE);
  }

  @Test
  public void testMixedUnits() throws Exception {
    // Create sample log data with mixed units
    String[] data = new String[] {
      "data_transfer_size,response_time",
      "1048576B,1000000000ns",    // 1MB, 1s
      "1024KB,1000ms",            // 1MB, 1s
      "0.001GB,1000000ns",        // 1MB, 1s
      "1048576B,1s"               // 1MB, 1s
    };

    // Define the recipe
    String[] recipe = new String[] {
      "aggregate-size-time data_transfer_size response_time total_size_mb total_time_sec"
    };

    // Execute the recipe
    List<Row> results = TestingRig.execute(data, recipe);

    // Verify results with precise assertions
    assertNotNull(results);
    assertEquals(1, results.size());
    Row result = results.get(0);
    
    // Total size: 4MB (all values are 1MB in different units)
    assertEquals(4.0, parseSizeValue(result.getValue("total_size_mb")), TOLERANCE);
    // Total time: 4 seconds (all values are 1s in different units)
    assertEquals(4.0, parseTimeValue(result.getValue("total_time_sec")), TOLERANCE);
  }

  @Test
  public void testEmptyAndNullValues() throws Exception {
    // Create sample log data with empty and null values
    String[] data = new String[] {
      "data_transfer_size,response_time",
      "1048576B,1000ms",    // 1MB, 1s
      ",2000ms",            // null, 2s
      "2097152B,",          // 2MB, null
      "null,3000ms",        // null, 3s
      "3145728B,null"       // 3MB, null
    };

    // Define the recipe
    String[] recipe = new String[] {
      "aggregate-size-time data_transfer_size response_time total_size_mb total_time_sec"
    };

    // Execute the recipe
    List<Row> results = TestingRig.execute(data, recipe);

    // Verify results with precise assertions
    assertNotNull(results);
    assertEquals(1, results.size());
    Row result = results.get(0);
    
    // Total size: 6MB (1MB + 2MB + 3MB)
    assertEquals(6.0, parseSizeValue(result.getValue("total_size_mb")), TOLERANCE);
    // Total time: 6 seconds (1s + 2s + 3s)
    assertEquals(6.0, parseTimeValue(result.getValue("total_time_sec")), TOLERANCE);
  }

  // Helper method to parse size values
  private double parseSizeValue(Object value) {
    if (value == null) return 0.0;
    String str = value.toString();
    // Remove unit and convert to double
    return Double.parseDouble(str.replaceAll("[^0-9.]", ""));
  }

  // Helper method to parse time values
  private double parseTimeValue(Object value) {
    if (value == null) return 0.0;
    String str = value.toString();
    // Remove unit and convert to double
    return Double.parseDouble(str.replaceAll("[^0-9.]", ""));
  }
} 
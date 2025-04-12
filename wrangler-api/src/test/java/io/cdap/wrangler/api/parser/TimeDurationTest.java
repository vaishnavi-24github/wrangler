package io.cdap.wrangler.api.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TimeDurationTest {

  @Test
  public void testBasicParsing() {
    // Test basic time durations
    TimeDuration ns = new TimeDuration("100ns");
    assertEquals(100L, ns.toNanos());
    assertEquals("100ns", ns.toString());

    TimeDuration ms = new TimeDuration("100ms");
    assertEquals(100 * 1000000L, ms.toNanos());
    assertEquals("100ms", ms.toString());

    TimeDuration sec = new TimeDuration("1s");
    assertEquals(1000000000L, sec.toNanos());
    assertEquals("1s", sec.toString());

    TimeDuration min = new TimeDuration("1m");
    assertEquals(60 * 1000000000L, min.toNanos());
    assertEquals("1m", min.toString());
  }

  @Test
  public void testDecimalParsing() {
    // Test decimal values
    TimeDuration halfSec = new TimeDuration("0.5s");
    assertEquals(500000000L, halfSec.toNanos());
    assertEquals("0.5s", halfSec.toString());

    TimeDuration onePointFiveMin = new TimeDuration("1.5m");
    assertEquals((long)(1.5 * 60 * 1000000000), onePointFiveMin.toNanos());
    assertEquals("1.5m", onePointFiveMin.toString());
  }

  @Test
  public void testCaseInsensitivity() {
    // Test case insensitivity
    TimeDuration ms1 = new TimeDuration("100MS");
    TimeDuration ms2 = new TimeDuration("100ms");
    assertEquals(ms1.toNanos(), ms2.toNanos());

    TimeDuration sec1 = new TimeDuration("1S");
    TimeDuration sec2 = new TimeDuration("1s");
    assertEquals(sec1.toNanos(), sec2.toNanos());
  }

  @Test
  public void testUnitConversion() {
    // Test unit conversion
    TimeDuration duration = new TimeDuration("1m", "s");
    assertEquals("60s", duration.toString());

    duration = new TimeDuration("1h", "m");
    assertEquals("60m", duration.toString());
  }

  @Test
  public void testComplexDurations() {
    // Test complex duration strings
    TimeDuration complex = new TimeDuration("1h30m");
    assertEquals((60 + 30) * 60 * 1000000000L, complex.toNanos());
    assertEquals("1h30m", complex.toString());

    complex = new TimeDuration("2h15m30s");
    assertEquals((2 * 3600 + 15 * 60 + 30) * 1000000000L, complex.toNanos());
    assertEquals("2h15m30s", complex.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    new TimeDuration("invalid");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    new TimeDuration("10XX");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeValue() {
    new TimeDuration("-1s");
  }
} 
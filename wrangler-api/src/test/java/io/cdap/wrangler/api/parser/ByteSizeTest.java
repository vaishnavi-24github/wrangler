package io.cdap.wrangler.api.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class ByteSizeTest {

  @Test
  public void testBasicParsing() {
    // Test basic byte sizes
    ByteSize bytes = new ByteSize("10B");
    assertEquals(10L, bytes.toBytes());
    assertEquals("10B", bytes.toString());

    ByteSize kb = new ByteSize("10KB");
    assertEquals(10 * 1024L, kb.toBytes());
    assertEquals("10KB", kb.toString());

    ByteSize mb = new ByteSize("1MB");
    assertEquals(1024 * 1024L, mb.toBytes());
    assertEquals("1MB", mb.toString());

    ByteSize gb = new ByteSize("1GB");
    assertEquals(1024 * 1024 * 1024L, gb.toBytes());
    assertEquals("1GB", gb.toString());
  }

  @Test
  public void testDecimalParsing() {
    // Test decimal values
    ByteSize halfKb = new ByteSize("0.5KB");
    assertEquals(512L, halfKb.toBytes());
    assertEquals("0.5KB", halfKb.toString());

    ByteSize onePointFiveMb = new ByteSize("1.5MB");
    assertEquals((long)(1.5 * 1024 * 1024), onePointFiveMb.toBytes());
    assertEquals("1.5MB", onePointFiveMb.toString());
  }

  @Test
  public void testCaseInsensitivity() {
    // Test case insensitivity
    ByteSize kb1 = new ByteSize("10kb");
    ByteSize kb2 = new ByteSize("10KB");
    assertEquals(kb1.toBytes(), kb2.toBytes());

    ByteSize mb1 = new ByteSize("1mb");
    ByteSize mb2 = new ByteSize("1MB");
    assertEquals(mb1.toBytes(), mb2.toBytes());
  }

  @Test
  public void testUnitConversion() {
    // Test unit conversion
    ByteSize size = new ByteSize("1MB", "KB");
    assertEquals("1024KB", size.toString());

    size = new ByteSize("1GB", "MB");
    assertEquals("1024MB", size.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    new ByteSize("invalid");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    new ByteSize("10XX");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeValue() {
    new ByteSize("-1MB");
  }
} 
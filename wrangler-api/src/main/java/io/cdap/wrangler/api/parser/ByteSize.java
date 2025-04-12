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

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.cdap.wrangler.api.annotations.PublicEvolving;

/**
 * The {@code ByteSize} class represents a byte size value with its unit.
 * This class parses a string value that represents a byte size (e.g., "10KB", "150MB", "2GB")
 * and provides methods to access the numeric value, unit, and type information.
 */
@PublicEvolving
public class ByteSize implements Token {
  private final String rawValue;
  private final double numericValue;
  private final String unit;

  public ByteSize(String value) {
    this.rawValue = value;
    
    // Parse the byte size string (e.g., "10KB", "150MB", "2GB")
    // Extract numeric part and unit
    int unitStartIndex = -1;
    for (int i = 0; i < value.length(); i++) {
      if (!Character.isDigit(value.charAt(i)) && value.charAt(i) != '.') {
        unitStartIndex = i;
        break;
      }
    }
    
    if (unitStartIndex == -1) {
      // No unit found, assume bytes
      this.numericValue = Double.parseDouble(value);
      this.unit = "B";
    } else {
      this.numericValue = Double.parseDouble(value.substring(0, unitStartIndex));
      this.unit = value.substring(unitStartIndex).toUpperCase();
    }
  }

  /**
   * Returns the raw string value of the byte size.
   *
   * @return the raw string value
   */
  @Override
  public String value() {
    return rawValue;
  }
  
  /**
   * Returns the numeric value of the byte size.
   *
   * @return the numeric value
   */
  public double getNumericValue() {
    return numericValue;
  }
  
  /**
   * Returns the unit of the byte size (B, KB, MB, GB, TB, PB).
   *
   * @return the unit
   */
  public String getUnit() {
    return unit;
  }
  
  /**
   * Converts the byte size to bytes.
   *
   * @return the byte size in bytes
   */
  public long toBytes() {
    switch (unit) {
      case "B":
        return (long) numericValue;
      case "KB":
        return (long) (numericValue * 1024);
      case "MB":
        return (long) (numericValue * 1024 * 1024);
      case "GB":
        return (long) (numericValue * 1024 * 1024 * 1024);
      case "TB":
        return (long) (numericValue * 1024 * 1024 * 1024 * 1024);
      case "PB":
        return (long) (numericValue * 1024 * 1024 * 1024 * 1024 * 1024);
      default:
        return (long) numericValue;
    }
  }

  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }

  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("type", TokenType.BYTE_SIZE.name());
    object.addProperty("value", rawValue);
    object.addProperty("numericValue", numericValue);
    object.addProperty("unit", unit);
    object.addProperty("bytes", toBytes());
    return object;
  }
} 
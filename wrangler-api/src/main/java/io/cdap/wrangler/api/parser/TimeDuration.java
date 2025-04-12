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
 * The {@code TimeDuration} class represents a time duration value with its unit.
 * This class parses a string value that represents a time duration (e.g., "300ms", "2s", "5m", "1h")
 * and provides methods to access the numeric value, unit, and type information.
 */
@PublicEvolving
public class TimeDuration implements Token {
  private final String rawValue;
  private final double numericValue;
  private final String unit;

  public TimeDuration(String value) {
    this.rawValue = value;
    
    // Parse the time duration string (e.g., "300ms", "2s", "5m", "1h")
    // Extract numeric part and unit
    int unitStartIndex = -1;
    for (int i = 0; i < value.length(); i++) {
      if (!Character.isDigit(value.charAt(i)) && value.charAt(i) != '.') {
        unitStartIndex = i;
        break;
      }
    }
    
    if (unitStartIndex == -1) {
      // No unit found, assume seconds
      this.numericValue = Double.parseDouble(value);
      this.unit = "s";
    } else {
      this.numericValue = Double.parseDouble(value.substring(0, unitStartIndex));
      this.unit = value.substring(unitStartIndex).toLowerCase();
    }
  }

  /**
   * Returns the raw string value of the time duration.
   *
   * @return the raw string value
   */
  @Override
  public String value() {
    return rawValue;
  }
  
  /**
   * Returns the numeric value of the time duration.
   *
   * @return the numeric value
   */
  public double getNumericValue() {
    return numericValue;
  }
  
  /**
   * Returns the unit of the time duration (ms, s, m, h, d).
   *
   * @return the unit
   */
  public String getUnit() {
    return unit;
  }
  
  /**
   * Converts the time duration to milliseconds.
   *
   * @return the time duration in milliseconds
   */
  public long toMilliseconds() {
    switch (unit) {
      case "ms":
        return (long) numericValue;
      case "s":
        return (long) (numericValue * 1000);
      case "m":
        return (long) (numericValue * 60 * 1000);
      case "h":
        return (long) (numericValue * 60 * 60 * 1000);
      case "d":
        return (long) (numericValue * 24 * 60 * 60 * 1000);
      default:
        return (long) numericValue;
    }
  }

  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }

  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("type", TokenType.TIME_DURATION.name());
    object.addProperty("value", rawValue);
    object.addProperty("numericValue", numericValue);
    object.addProperty("unit", unit);
    object.addProperty("milliseconds", toMilliseconds());
    return object;
  }
} 
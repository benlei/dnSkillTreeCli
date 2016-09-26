package com.github.ben.lei.dncli.converter;

import com.beust.jcommander.IStringConverter;

import java.util.Objects;

/**
 * Created by blei on 6/17/16.
 */
public class ByteCharacterConverter implements IStringConverter<Long> {
  @Override
  public Long convert(String value) {
    Objects.requireNonNull(value, "byte value is null");
    long min;

    char ch = value.toLowerCase().charAt(value.length() - 1);
    if (ch == 'b' || ch == 'k' || ch == 'm' || ch == 'g') {
      value = value.substring(0, value.length() - 1);
    } else if (ch < '0' || ch > '9') {
      throw new IllegalArgumentException(String.format("'%c' is not a valid file size", ch));
    }

    // throw exception if size is too big
    min = Integer.parseInt(value);
    if (ch == 'k') {
      min = Math.multiplyExact(min, 1 << 10);
    } else if (ch == 'm') {
      min = Math.multiplyExact(min, 1 << 20);
    } else if (ch == 'g') {
      min = Math.multiplyExact(min, 1 << 30);
    }

    return min;
  }
}

package com.github.ben.lei.dncli.converter;

import com.beust.jcommander.IStringConverter;
import com.github.ben.lei.dncli.enums.DdsOutputFormat;
import com.github.ben.lei.dncli.exception.InvalidDdsOutputFormatException;

/**
 * Created by blei on 6/16/16.
 */
public class DdsOutputFormatConverter implements IStringConverter<DdsOutputFormat> {
  @Override
  public DdsOutputFormat convert(String value) {
    if (value == null) {
      throw new InvalidDdsOutputFormatException("nothing was provided");
    }

    value = value.toLowerCase();
    if ("png".equals(value)) {
      return DdsOutputFormat.png;
    } else if ("jpg".equals(value)) {
      return DdsOutputFormat.jpg;
    }

    throw new InvalidDdsOutputFormatException("invalid output format: " + value);
  }
}

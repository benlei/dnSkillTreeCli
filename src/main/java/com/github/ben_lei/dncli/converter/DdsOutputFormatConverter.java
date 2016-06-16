package com.github.ben_lei.dncli.converter;

import com.beust.jcommander.IStringConverter;
import com.github.ben_lei.dncli.DdsOutputFormat;

/**
 * Created by blei on 6/16/16.
 */
public class DdsOutputFormatConverter implements IStringConverter<DdsOutputFormat> {
  @Override
  public DdsOutputFormat convert(String value) {
    if (value == null) {
      return DdsOutputFormat.INVALID;
    }

    value = value.toLowerCase();
    if ("png".equals(value)) {
      return DdsOutputFormat.PNG;
    } else if ("jpg".equals(value)) {
      return DdsOutputFormat.JPEG;
    }

    return DdsOutputFormat.INVALID;
  }
}

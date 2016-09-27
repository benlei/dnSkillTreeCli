package com.github.ben.lei.dncli.command;

import com.github.ben.lei.dncli.converter.DdsOutputFormatConverter;
import com.github.ben.lei.dncli.dds.DdsConverter;
import com.github.ben.lei.dncli.enums.DdsOutputFormat;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by blei on 6/15/16.
 */
@Parameters(commandNames = "dds",
    commandDescription = "Microsoft DDS to JPG/PNG converter. It is recommended to use "
        + "ImageMagick if it's already installed.")
public class CommandDds implements Runnable {
  private final Runnable runner = new DdsConverter(this);

  @Parameter(names = "-force",
      description = "Forces overwriting of converted file without confirmation")
  private boolean force;

  @Parameter(names = "-format",
      description = "The output file format. Valid options are png and jpg.",
      converter = DdsOutputFormatConverter.class,
      required = true)
  private DdsOutputFormat format;

  @Parameter(names = "-quiet", description = "Quiet output")
  private boolean quiet;

  @Parameter(description = "ddsFiles...", converter = FileConverter.class)
  private List<File> files = new ArrayList<>();

  public boolean isForce() {
    return force;
  }

  public DdsOutputFormat getFormat() {
    return format;
  }

  public List<File> getFiles() {
    return files;
  }

  public boolean isQuiet() {
    return quiet;
  }

  @Override
  public void run() {
    runner.run();
  }
}

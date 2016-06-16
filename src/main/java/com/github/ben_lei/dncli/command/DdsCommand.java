package com.github.ben_lei.dncli.command;

import com.beust.jcommander.Parameter;
import com.github.ben_lei.dncli.DdsOutputFormat;
import com.github.ben_lei.dncli.converter.DdsOutputFormatConverter;

/**
 * Created by blei on 6/15/16.
 */
public class DdsCommand {
  @Parameter(names = {"-f", "--force"}, description = "Forces overwriting of " +
      "destination file without confirmation")
  private boolean force;

  @Parameter(names = {"-q", "--quiet"}, description = "Quiet output")
  private boolean quiet;

  @Parameter(names = {"-m", "--format"}, description = "The output file format. " +
      "Valid options are png and jpg.", converter = DdsOutputFormatConverter.class,
  required = true)
  private DdsOutputFormat format;

  @Parameter(names = {"-h", "--help"}, description = "Displays this usage.", help = true)
  private boolean help;
}

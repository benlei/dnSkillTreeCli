package com.github.ben_lei.dncli.command;

import com.beust.jcommander.Parameter;

/**
 * Created by blei on 6/15/16.
 */
public class CliCommand {
  @Parameter(names = "dds", description = "Use DDS module.")
  private boolean dds;

  @Parameter(names = "dnt", description = "Use DNT module.")
  private boolean dnt;

  @Parameter(names = "pak", description = "Use Pak module.")
  private boolean pak;

  @Parameter(names = {"-h", "--help"}, description = "Displays this usage.", help = true)
  private boolean help;
}

package com.dnmaze.dncli.command;

import com.beust.jcommander.Parameter;

import lombok.Getter;

/**
 * Created by blei on 6/18/16.
 */
public class Command {
  @Getter
  private final CommandDds dds = new CommandDds();
  @Getter
  private final CommandDnt dnt = new CommandDnt();
  @Getter
  private final CommandPak pak = new CommandPak();

  @Getter
  @Parameter(names = "-help", description = "Displays this usage.", help = true)
  private boolean help;
}

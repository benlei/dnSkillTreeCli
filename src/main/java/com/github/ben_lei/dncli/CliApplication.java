package com.github.ben_lei.dncli;

import com.beust.jcommander.JCommander;
import com.github.ben_lei.dncli.command.CliCommand;

/**
 * Created by blei on 6/15/16.
 */
public class CliApplication {
  public static void main(String[] args) {
    String[] module = new String[] {args.length == 0 ? null : args[0]};

    CliCommand cliCommand = new CliCommand();
    JCommander jCommander = new JCommander(cliCommand, args);
    jCommander.setProgramName("dncli");
    jCommander.usage();

  }
}

package com.github.ben.lei.dncli.dnt;

import com.github.ben.lei.dncli.command.CommandDnt;

/**
 * Created by blei on 6/19/16.
 */
public class DntQuery implements Runnable {
  private final CommandDnt.Query args;

  public DntQuery(CommandDnt.Query args) {
    this.args = args;
  }

  @Override
  public void run() {

  }
}

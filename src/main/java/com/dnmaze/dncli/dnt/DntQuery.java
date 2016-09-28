package com.dnmaze.dncli.dnt;

import com.dnmaze.dncli.command.CommandDnt;
import com.dnmaze.dncli.util.JsUtil;

import java.io.File;
import java.util.List;

import javax.script.Invocable;

/**
 * Created by blei on 6/19/16.
 */
public class DntQuery implements Runnable {
  private final CommandDnt.Query args;
  private Dnt dnt;

  public DntQuery(CommandDnt.Query args) {
    this.args = args;
  }

  @Override
  public void run() {
    try {
      Invocable js = JsUtil.compileAndEval(args.getQueryFile());
      dnt = js.getInterface(Dnt.class);
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage());
    }

    File messageFile = args.getMessageFile();
    if (messageFile != null) {
      createMessageTable(messageFile);
    }

    createTables(args.getInputs());

    dnt.complete();
  }

  private void createMessageTable(File file) {

  }

  private void createTables(List<File> inputs) {

  }
}

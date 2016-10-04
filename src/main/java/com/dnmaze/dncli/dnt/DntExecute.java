package com.dnmaze.dncli.dnt;

import com.dnmaze.dncli.command.CommandDnt;
import com.dnmaze.dncli.util.JsUtil;

import javax.script.Invocable;

/**
 * Created by blei on 9/30/16.
 */
public class DntExecute implements Runnable {
  private final CommandDnt.Execute args;

  public DntExecute(CommandDnt.Execute args) {
    this.args = args;
  }

  @Override
  public void run() {
    try {
      Invocable js = JsUtil.compileAndEval(args.getInputs().get(0));
      Dnt dnt = js.getInterface(Dnt.class);
      dnt.process();
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage());
    }
  }
}

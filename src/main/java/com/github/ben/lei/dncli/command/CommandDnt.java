package com.github.ben.lei.dncli.command;

import com.github.ben.lei.dncli.dnt.DntQuery;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;
import lombok.Getter;

import java.io.File;
import java.util.List;

/**
 * Created by blei on 6/16/16.
 */
@Parameters(commandDescription = "DragonNest Table file reading and building.")
public class CommandDnt {
  @Getter
  private final Query query = new Query();

  @Parameters
  public static class Query implements Runnable {
    private final Runnable runner = new DntQuery(this);

    @Parameter(names = "-js",
        description = "The query JS file that should just have a query() function.",
        converter = FileConverter.class,
        required = true)
    private File queryFile;

    @Parameter(names = "-quiet", description = "Quiet output")
    private boolean quiet;

    @Parameter(description = "dntFiles...", required = true)
    private List<File> inputs;

    public File getQueryFile() {
      return queryFile;
    }

    public List<File> getInputs() {
      return inputs;
    }

    public boolean isQuiet() {
      return quiet;
    }

    @Override
    public void run() {
      runner.run();
    }
  }
}

package com.dnmaze.dncli.command;

import com.dnmaze.dncli.dnt.DntExecute;
import com.dnmaze.dncli.dnt.DntProcess;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;
import lombok.Getter;

import java.io.File;
import java.util.List;

/**
 * Created by blei on 6/16/16.
 */
@Parameters(commandDescription = "DragonNest Table file querying. Utilizes MySQL syntax.")
public class CommandDnt {
  @Getter
  private final Process process = new Process();

  @Getter
  private final Execute execute = new Execute();

  @Parameter(names = "-help", description = "Displays this usage.", help = true)
  @Getter
  private boolean help;

  @Parameters
  public static class Process implements Runnable {
    private final Runnable runner = new DntProcess(this);

    @Getter
    @Parameter(
        names = "-js",
        description = "The JS file that must have 4 functions defined: "
                      + "normalizeName(java.lang.String) for normalizing table names, "
                      + "getConnection() that should return a java.sql.Connection for this "
                      + "program to use to re-create tables and propagate data, "
                      + "close() for closing the JDBC connection, and "
                      + "process() for performing any tasks after all DNT data has been "
                      + "propagated.",
        converter = FileConverter.class,
        required = true
    )
    private File jsFile;

    @Getter
    @Parameter(
        names = "-uistring",
        description = "The uistring.xml file that can be loaded for the query() method.",
        converter = FileConverter.class
    )
    private File messageFile;

    @Getter
    @Parameter(
        names = "-fresh",
        description = "Deletes ALL found normalized tables before inserting."
    )
    private boolean fresh;

    @Getter
    @Parameter(description = "dntFiles...", required = true)
    private List<File> inputs;

    @Getter
    @Parameter(
        names = "-config",
        description = "A config file that will be passed as dncli.config system property.",
        converter = FileConverter.class
    )
    private File configFile;


    @Parameter(names = "-help", description = "Displays this usage.", help = true)
    @Getter
    private boolean help;

    @Override
    public void run() {
      runner.run();
    }
  }

  @Parameters
  public static class Execute implements Runnable {
    private final Runnable runner = new DntExecute(this);

    @Getter
    @Parameter(
        description = "The JS file should follow same requirements as the -js"
                      + "param for DNT processing.",
        required = true
    )
    private List<File> inputs;

    @Parameter(names = "-help", description = "Displays this usage.", help = true)
    @Getter
    private boolean help;


    @Getter
    @Parameter(
        names = "-config",
        description = "A config file that will be passed as dncli.config system property.",
        converter = FileConverter.class
    )
    private File configFile;

    @Override
    public void run() {
      runner.run();
    }
  }
}

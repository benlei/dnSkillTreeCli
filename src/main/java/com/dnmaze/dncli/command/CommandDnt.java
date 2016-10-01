package com.dnmaze.dncli.command;

import com.dnmaze.dncli.dnt.DntExecute;
import com.dnmaze.dncli.dnt.DntQuery;

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
  private final Query query = new Query();

  @Getter
  private final Execute execute = new Execute();

  @Parameters
  public static class Query implements Runnable {
    private final Runnable runner = new DntQuery(this);

    @Getter
    @Parameter(names = "-js",
        description = "The query JS file that must have 3 functions defined: "
                      + "normalizeName(java.lang.String) for normalizing table names, "
                      + "getConnection() that should return a java.sql.Connection for this "
                      + "program to use to re-create tables and propagate data, and "
                      + "complete() for performing any tasks after all DNT data has been "
                      + "propagated.",
        converter = FileConverter.class,
        required = true)
    private File jsFile;

    @Getter
    @Parameter(names = "-uistring",
        description = "The uistring.xml file that can be loaded for the query() method.",
        converter = FileConverter.class)
    private File messageFile;

    @Getter
    @Parameter(names = "-fresh",
        description = "Deletes ALL found normalized tables before inserting.")
    private boolean fresh;

    @Getter
    @Parameter(description = "dntFiles...", required = true)
    private List<File> inputs;

    @Override
    public void run() {
      runner.run();
    }
  }

  @Parameters
  public static class Execute implements Runnable {
    private final Runnable runner = new DntExecute(this);

    @Getter
    @Parameter(description = "The query JS file that must have 3 functions defined: "
                             + "normalizeName(java.lang.String) for normalizing table names, "
                             + "getConnection() that should return a java.sql.Connection for this "
                             + "program to use to re-create tables and propagate data, and "
                             + "complete() for performing any tasks after all DNT data has been "
                             + "propagated. The first parameter specified will be assumed to be "
                             + "the input.",
        required = true)
    private List<File> inputs;

    @Override
    public void run() {
      runner.run();
    }
  }
}

package com.github.ben_lei.dncli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;
import com.github.ben_lei.dncli.dnt.DntBuild;
import com.github.ben_lei.dncli.dnt.DntQuery;

import java.io.File;
import java.util.List;

/**
 * Created by blei on 6/16/16.
 */
@Parameters(commandDescription = "DragonNest Table file reading and building.")
public class CommandDnt {
    private final Query query = new Query();
    private final Build build = new Build();

    @Parameter(names = {"-q", "--quiet"}, description = "Quiet output")
    private boolean quiet;

    public Query getQuery() {
        return query;
    }

    public Build getBuild() {
        return build;
    }

    public boolean isQuiet() {
        return quiet;
    }

    @Parameters
    public class Query implements Runnable {
        private final Runnable runner = new DntQuery(this);

        @Parameter(names = {"-j", "--javascript"}, description = "The query JS file that should just have a query() function.",
            converter = FileConverter.class, required = true)
        private File queryFile;

        @Parameter(names = {"-q", "--quiet"}, description = "Quiet output")
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
            return CommandDnt.this.isQuiet() || quiet;
        }

        @Override
        public void run() {
            runner.run();
        }
    }

    @Parameters
    public class Build implements Runnable {
        private final Runnable runner = new DntBuild(this);

        @Parameter(names = {"-b", "--base"}, variableArity = true, description = "List of base files to setup  before building.")
        private List<File> baseFiles;

        @Parameter(names = {"-j", "--javascript"}, description = "The build JS file that should have a build() function.",
            converter = FileConverter.class, required = true)
        private File queryFile;

        @Parameter(names = {"-q", "--quiet"}, description = "Quiet output")
        private boolean quiet;

        public List<File> getBaseFiles() {
            return baseFiles;
        }

        public File getQueryFile() {
            return queryFile;
        }

        public boolean isQuiet() {
            return CommandDnt.this.isQuiet() || quiet;
        }

        @Override
        public void run() {
            runner.run();
        }
    }
}

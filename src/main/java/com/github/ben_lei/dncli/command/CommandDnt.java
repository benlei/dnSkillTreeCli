package com.github.ben_lei.dncli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

import java.io.File;
import java.util.List;

/**
 * Created by blei on 6/16/16.
 */
@Parameters(commandDescription = "DragonNest Table file reading and building.")
public class CommandDnt {
    @Parameters
    public static class Query {
        @Parameter(names = {"-j", "--javascript"}, description = "The query JS file that should just have a query() function.",
            converter = FileConverter.class, required = true)
        private File queryFile;

        @Parameter(description = "dntFiles...", required = true)
        private List<File> inputs;
    }

    @Parameters
    public static class Build {
        @Parameter(names = {"-b", "--base"}, variableArity = true, description = "List of base files to setup  before building.")
        private List<File> baseFiles;

        @Parameter(names = {"-j", "--javascript"}, description = "The build JS file that should have a build() function.",
            converter = FileConverter.class, required = true)
        private File queryFile;
    }
}

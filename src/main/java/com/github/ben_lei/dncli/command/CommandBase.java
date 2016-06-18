package com.github.ben_lei.dncli.command;

import com.beust.jcommander.Parameter;

/**
 * Created by blei on 6/18/16.
 */
public class CommandBase {
    @Parameter(names = {"-q", "--quiet"}, description = "Quiet output")
    private boolean quiet;

    @Parameter(names = {"-h", "--help"}, description = "Displays this usage.", help = true)
    private boolean help;
}

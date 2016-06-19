package com.github.ben_lei.dncli.command;

import com.beust.jcommander.Parameter;

/**
 * Created by blei on 6/18/16.
 */
public class Command {
    private final CommandDds dds = new CommandDds();
    private final CommandDnt dnt = new CommandDnt();
    private final CommandPak pak = new CommandPak();

    @Parameter(names = {"-h", "--help"}, description = "Displays this usage.", help = true)
    private boolean help;

    public boolean isHelp() {
        return help;
    }

    public CommandDds getDds() {
        return dds;
    }

    public CommandDnt getDnt() {
        return dnt;
    }

    public CommandPak getPak() {
        return pak;
    }
}

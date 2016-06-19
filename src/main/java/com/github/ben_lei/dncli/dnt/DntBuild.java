package com.github.ben_lei.dncli.dnt;

import com.github.ben_lei.dncli.command.CommandDnt;

/**
 * Created by blei on 5/29/16.
 */
public class DntBuild implements Runnable {
    private final CommandDnt.Build args;

    public DntBuild(CommandDnt.Build args) {
        this.args = args;
    }

    @Override
    public void run() {

    }
}

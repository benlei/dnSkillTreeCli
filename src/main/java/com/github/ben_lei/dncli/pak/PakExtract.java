package com.github.ben_lei.dncli.pak;

import com.github.ben_lei.dncli.command.CommandPak;

/**
 * Created by blei on 5/29/16.
 */
public class PakExtract implements Runnable {
    private final CommandPak.Extract args;

    public PakExtract(CommandPak.Extract args) {
        this.args = args;
    }

    @Override
    public void run() {

    }
}

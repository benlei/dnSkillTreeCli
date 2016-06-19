package com.github.ben_lei.dncli.pak;

import com.github.ben_lei.dncli.command.CommandPak;

/**
 * Created by blei on 6/19/16.
 */
public class PakCompress implements Runnable {
    private final CommandPak.Compress args;

    public PakCompress(CommandPak.Compress args) {
        this.args = args;
    }

    @Override
    public void run() {

    }
}

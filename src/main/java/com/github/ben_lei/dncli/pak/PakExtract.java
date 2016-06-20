package com.github.ben_lei.dncli.pak;

import com.github.ben_lei.dncli.command.CommandPak;
import com.github.ben_lei.dncli.pak.archive.PakFile;
import com.github.ben_lei.dncli.pak.archive.PakHeader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

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
        List<File> files = args.getFiles();

        files.parallelStream().forEach(file -> {
            try {
                PakHeader pakHeader = PakHeader.from(file);
                IntStream.range(0, pakHeader.getNumFiles()).parallel().forEach(frame -> {
                    try {
                        PakFile pakFile = PakFile.load(file, pakHeader.getStartPosition(), frame);
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                });
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });
    }
}

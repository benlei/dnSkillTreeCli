package com.github.ben_lei.dncli.pak;

import com.github.ben_lei.dncli.command.CommandPak;
import com.github.ben_lei.dncli.pak.archive.PakFile;
import com.github.ben_lei.dncli.pak.archive.PakHeader;
import com.github.ben_lei.dncli.util.JsUtil;
import jdk.nashorn.api.scripting.JSObject;

import javax.script.Invocable;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;
import java.util.zip.DataFormatException;

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
        File filterFile = args.getFilterFile();
        Invocable invocable = null;

        if (filterFile != null) {
            try {
                invocable = JsUtil.compileAndEval(filterFile);
            } catch (ScriptException | FileNotFoundException e) {
                System.err.println(e.getMessage());
            }
        }

        Invocable finalInvocable = invocable;
        files.parallelStream().forEach(file -> {
            try {
                PakHeader pakHeader = PakHeader.from(file);
                IntStream.range(0, pakHeader.getNumFiles()).parallel().forEach(frame -> {
                    try {
                        PakFile pakFile = PakFile.load(file, pakHeader.getStartPosition(), frame);
                        boolean extractable = false;

                        if (finalInvocable == null) {
                            if (pakFile.getSize() != 0) {
                                extractable = true;
                            }
                        } else {
                            JSObject jso = JsUtil.reflect(pakFile);
                            extractable = (Boolean) finalInvocable.invokeFunction("filter", jso);
                        }

                        if (extractable) {
                            pakFile.extractTo(args.getOutput());
                            if (!args.isQuiet()) {
                                System.out.println(pakFile.getPath());
                            }
                        }
                    } catch (IOException | DataFormatException | NoSuchMethodException | ScriptException e) {
                        System.err.println(e.getMessage());
                    }
                });
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });
    }
}

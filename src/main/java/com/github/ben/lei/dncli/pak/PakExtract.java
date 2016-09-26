package com.github.ben.lei.dncli.pak;

import com.github.ben.lei.dncli.command.CommandPak;
import com.github.ben.lei.dncli.pak.archive.PakFile;
import com.github.ben.lei.dncli.pak.archive.PakHeader;
import com.github.ben.lei.dncli.util.JsUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.zip.DataFormatException;

import javax.script.Invocable;
import javax.script.ScriptException;

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
    Function<PakFile, Boolean> filter = pakFile -> pakFile.getSize() != 0;

    if (filterFile != null) {
      try {
        Invocable invocable = JsUtil.compileAndEval(filterFile);
        filter = pakFile -> (Boolean) JsUtil.invoke(invocable, "filter", JsUtil.reflect(pakFile));
      } catch (ScriptException | FileNotFoundException ex) {
        System.err.println(ex.getMessage());
      }
    }

    extractFiles(files, filter);
  }

  /**
   * <p>Sets up a parallel stream to extract every pak in list.</p>
   *
   * @param files  the list of files
   * @param filter the filter function
   */
  private void extractFiles(List<File> files, Function<PakFile, Boolean> filter) {
    files.parallelStream().forEach(file -> {
      try {
        PakHeader pakHeader = PakHeader.from(file);
        extractPakFiles(file, pakHeader, filter);
      } catch (IOException ex) {
        System.err.println(ex.getMessage());
      }
    });
  }

  /**
   * <p>Extracts all files in a pak archive concurrently.</p>
   *
   * @param file   the pak archive
   * @param header the pak archive's header information
   * @param filter the filter function
   */
  private void extractPakFiles(File file, PakHeader header, Function<PakFile, Boolean> filter) {
    int numFiles = header.getNumFiles();
    long startPos = header.getStartPosition();

    IntStream.range(0, numFiles).parallel().forEach(frame -> {
      try {
        PakFile pakFile = PakFile.load(file, startPos, frame);
        Boolean extractable = filter.apply(pakFile);

        if (extractable != null && extractable) {
          pakFile.extractTo(args.getOutput());
          if (!args.isQuiet()) {
            System.out.println(pakFile.getPath());
          }
        }
      } catch (IOException | DataFormatException ex) {
        System.err.println(ex.getMessage());
      }
    });
  }
}

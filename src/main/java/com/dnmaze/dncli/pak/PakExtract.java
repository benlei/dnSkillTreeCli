package com.dnmaze.dncli.pak;

import com.dnmaze.dncli.command.CommandPak;
import com.dnmaze.dncli.pak.archive.PakFile;
import com.dnmaze.dncli.pak.archive.PakHeader;
import com.dnmaze.dncli.util.JsUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.zip.DataFormatException;
import javax.script.Invocable;
import javax.script.ScriptException;

/**
 * Created by blei on 5/29/16.
 */
public class PakExtract implements Runnable {
  private final CommandPak.Extract args;
  private File outputDir;

  public PakExtract(CommandPak.Extract args) {
    this.args = args;
  }

  @Override
  public void run() {
    outputDir = args.getOutput();
    Objects.requireNonNull(outputDir, "Output directory cannot be null");
    outputDir = outputDir.getAbsoluteFile();

    List<File> files = args.getFiles();
    File filterFile = args.getFilterFile();
    PakFilter filter = new PakFilterImpl();

    if (filterFile != null) {
      filterFile = filterFile.getAbsoluteFile();

      try {
        Invocable js = JsUtil.compileAndEval(filterFile);
        filter = js.getInterface(PakFilter.class);
      } catch (ScriptException | FileNotFoundException ex) {
        throw new RuntimeException(ex.getMessage());
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
  private void extractFiles(List<File> files, PakFilter filter) {
    files.parallelStream().forEach(file -> {
      file = file.getAbsoluteFile();

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
  private void extractPakFiles(File file, PakHeader header, PakFilter filter) {
    int numFiles = header.getNumFiles();
    long startPos = header.getStartPosition();

    IntStream stream = IntStream.range(0, numFiles);

    if (!args.isSync()) {
      stream = stream.parallel();
    }

    stream.forEach(frame -> {
      try {
        System.out.println(file.getAbsolutePath() + " being extracted");
        PakFile pakFile = PakFile.load(file, startPos, frame);
        boolean extractable = filter.filter(pakFile);

        if (extractable) {
          pakFile.extractTo(outputDir);

          if (!args.isQuiet()) {
            System.out.println(pakFile.getPath());
          }
        }
      } catch (IOException | DataFormatException ex) {
        System.err.println(ex.getMessage());
      }
    });
  }

  private static class PakFilterImpl implements PakFilter {
    @Override
    public boolean filter(PakFile pakFile) {
      return pakFile.getSize() != 0;
    }
  }
}

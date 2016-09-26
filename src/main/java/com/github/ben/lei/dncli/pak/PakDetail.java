package com.github.ben.lei.dncli.pak;

import com.github.ben.lei.dncli.command.CommandPak;
import com.github.ben.lei.dncli.pak.archive.PakFile;
import com.github.ben.lei.dncli.pak.archive.PakHeader;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by blei on 6/19/16.
 */
public class PakDetail implements Runnable {
  private final CommandPak.Detail args;

  public PakDetail(CommandPak.Detail args) {
    this.args = args;
  }

  /**
   * <p>Lists out the pak archive information.</p>
   */
  @Override
  public void run() {
    List<File> archives = args.getFiles();
    boolean isNotFirst = false;

    for (File file : archives) {
      try {
        if (isNotFirst) {
          System.out.println();
        }

        PakHeader header = PakHeader.from(file);
        System.out.println(String.format("%s contains %d files",
            file.getPath(),
            header.getNumFiles()));

        if (!args.isQuiet()) {
          listArchiveContents(file, header);
        }

        isNotFirst = true;
      } catch (IOException ex) {
        System.err.println(ex.getMessage());
      }
    }
  }

  /**
   * <p>Lists out all the files in archive with its actual size > compressed size.</p>
   *
   * @param file   the archive file
   * @param header the pak header
   */
  private void listArchiveContents(File file, PakHeader header) {
    for (int i = 0; i < header.getNumFiles(); i++) {
      try {
        PakFile pakFile = PakFile.load(file, header.getStartPosition(), i);
        String output = String.format("%s (%d > %d)", pakFile.getPath(),
            pakFile.getSize(),
            pakFile.getCompressedSize());

        System.out.println(output);
      } catch (IOException ex) {
        System.err.println(ex.getMessage());
      }
    }
  }
}

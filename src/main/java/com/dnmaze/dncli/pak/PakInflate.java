package com.dnmaze.dncli.pak;

import com.dnmaze.dncli.command.CommandPak;

import lombok.Cleanup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by blei on 9/30/16.
 */
public class PakInflate implements Runnable {
  private static final byte[] BUFFER = new byte[10240];
  private final CommandPak.Inflate args;

  public PakInflate(CommandPak.Inflate args) {
    this.args = args;
  }

  @Override
  public void run() {
    long desiredSize = args.getSize();
    for (File file : args.getFiles()) {
      file = file.getAbsoluteFile();
      long currSize = file.length();

      if (currSize < desiredSize) {
        try {
          @Cleanup FileOutputStream out = new FileOutputStream(file);

          while (currSize < desiredSize) {
            long currDiff = desiredSize - currSize;

            if (currDiff < BUFFER.length) {
              out.write(BUFFER, 0, (int) currDiff);
              currSize = desiredSize;
            } else {
              out.write(BUFFER);
              currSize += BUFFER.length;
            }
          }
        } catch (IOException ex) {
          throw new RuntimeException(ex.getMessage());
        }
      }
    }
  }
}

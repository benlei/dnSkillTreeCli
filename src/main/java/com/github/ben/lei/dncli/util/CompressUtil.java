package com.github.ben.lei.dncli.util;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Created by blei on 9/9/16.
 */
public final class CompressUtil {
  private static final int BUFSIZ = 4096;

  /**
   * <p>Compresses a file using ZLib.</p>
   *
   * @param file the file
   * @return the compressed bytes
   * @throws IOException if there was an issue compressing the file
   */
  public static byte[] compress(File file) throws IOException {
    Deflater deflater = new Deflater();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] deflated = new byte[BUFSIZ];
    int read;

    deflater.setInput(IOUtils.toByteArray(new FileInputStream(file)));
    deflater.finish();

    while ((read = deflater.deflate(deflated)) != 0) {
      baos.write(deflated, 0, read);
    }

    deflater.end();

    return baos.toByteArray();
  }

  /**
   * <p>Decompresses a the content of a file from {@code startPosition} of {@code compressedSize}
   * and return the bytes of it.</p>
   *
   * @param file the file
   * @param startPosition   the start position
   * @param compressedSize the compresse
   * @return the decompressed bytes
   * @throws IOException if there was any io issues
   * @throws DataFormatException if there was an issue decompressing content
   */
  public static byte[] decompress(File file, int startPosition, int compressedSize)
      throws IOException, DataFormatException {

    try (FileChannel fileChannel = FileChannel.open(file.toPath())) {
      ByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY,
          startPosition,
          compressedSize);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Inflater inflater = new Inflater();
      byte[] compressedBytes = new byte[compressedSize];
      byte[] inflated = new byte[BUFSIZ];
      int read;

      buffer.get(compressedBytes);
      inflater.setInput(compressedBytes);

      // unzip contents
      while ((read = inflater.inflate(inflated)) != 0) {
        baos.write(inflated, 0, read);
      }

      inflater.end();

      return baos.toByteArray();
    }
  }

  /**
   * <p>Decompresses a the content of a file from {@code startPosition} of {@code compressedSize}
   * and return the bytes of it. If a {@code compressedSize} can be provided if it is known
   * how large the decompressed content will be.</p>
   *
   * @param file the file
   * @param startPosition   the start position
   * @param compressedSize the compresse
   * @param decompressedSize the decompressed size
   * @return the decompressed bytes
   * @throws IOException if there was any io issues
   * @throws DataFormatException if there was an issue decompressing content
   */
  public static byte[] decompress(File file,
                                  int startPosition,
                                  int compressedSize,
                                  int decompressedSize)
      throws IOException, DataFormatException {

    if (decompressedSize == 0) {
      return decompress(file, startPosition, compressedSize);
    }

    try (FileChannel fileChannel = FileChannel.open(file.toPath())) {
      ByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY,
          startPosition,
          compressedSize);
      Inflater inflater = new Inflater();
      byte[] compressedBytes = new byte[compressedSize];
      byte[] bytes = new byte[decompressedSize];

      buffer.get(compressedBytes);
      inflater.setInput(compressedBytes, 0, compressedSize);
      inflater.inflate(bytes);
      inflater.end();

      return bytes;
    }
  }
}

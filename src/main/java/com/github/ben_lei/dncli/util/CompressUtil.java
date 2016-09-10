package com.github.ben_lei.dncli.util;

import org.apache.commons.io.IOUtils;

import java.io.*;
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

    public static byte[] decompress(File file) throws IOException, DataFormatException {
        return decompress(file, 0, (int)file.length());
    }

    public static byte[] decompress(File file, int startPosition, int compressedSize)
        throws IOException, DataFormatException {

        try(FileChannel fileChannel = FileChannel.open(file.toPath())) {
            ByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startPosition, compressedSize);
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

    public static byte[] decompress(File file, int startPosition, int compressedSize, int decompressedSize)
        throws IOException, DataFormatException {

        if (decompressedSize == 0) {
            return decompress(file, startPosition, compressedSize);
        }

        try(FileChannel fileChannel = FileChannel.open(file.toPath())) {
            ByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startPosition, compressedSize);
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

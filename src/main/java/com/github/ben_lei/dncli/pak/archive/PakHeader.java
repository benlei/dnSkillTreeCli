package com.github.ben_lei.dncli.pak.archive;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Created by blei on 6/19/16.
 */
public class PakHeader {
    public static final byte[] START = "EyedentityGames Packing File 0.1".getBytes();
    public static final int START_LEN = START.length;
    public static final int RC_START = 260;
    public static final int RC_BYTES = 8;

    private int numFiles;
    private int startPosition;

    /**
     * <p>Private constructor</p>
     */
    private PakHeader() {
    }

    /**
     * <p>Gets the header from a pak archive.</p>
     *
     * @param file the pak file
     * @return the pak's header information
     * @throws IOException if cannot open/read file
     */
    public static PakHeader from(File file) throws IOException {
        PakHeader thiz = new PakHeader();
        FileChannel fileChannel = null;

        try {
            fileChannel = FileChannel.open(file.toPath());
            byte[] headerBytes = new byte[START_LEN];
            ByteBuffer headerBuffer = ByteBuffer.wrap(headerBytes);
            ByteBuffer words = ByteBuffer.allocate(8);
            words.order(ByteOrder.LITTLE_ENDIAN);

            // check the header
            fileChannel.read(headerBuffer);
            if (!Arrays.equals(headerBytes, START)) {
                throw new IOException("Invalid Pak file header.");
            }

            // Sets where we start
            fileChannel.position(RC_START);
            fileChannel.read(words);

            // gets # of files and start offset (read it)
            words.flip();

            thiz.numFiles = words.getInt();
            thiz.startPosition = words.getInt();

            return thiz;
        } finally {
            IOUtils.closeQuietly(fileChannel);
        }
    }

    /**
     * <p>The number of files in the archive, accordng to
     * the header of the pak.</p>
     *
     * @return the number of files
     */
    public int getNumFiles() {
        return numFiles;
    }

    /**
     * <p>The start offset where the tape of files begin</p>
     *
     * @return the start offset
     */
    public int getStartPosition() {
        return startPosition;
    }
}

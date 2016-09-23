package com.github.ben_lei.dncli.pak;

import com.github.ben_lei.dncli.command.CommandPak;
import com.github.ben_lei.dncli.pak.archive.PakFile;
import com.github.ben_lei.dncli.pak.archive.PakHeader;
import com.github.ben_lei.dncli.util.CompressUtil;
import com.github.ben_lei.dncli.util.H2Util;
import com.github.ben_lei.dncli.util.ResourceUtil;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * Created by blei on 6/19/16.
 */
public class PakCompress implements Runnable {

    private final CommandPak.Compress args;
    private int largestBytes = PakFile.META_BYTES;
    private int files;
    private WritableByteChannel output;

    public PakCompress(CommandPak.Compress args) {
        this.args = args;
    }

    @Override
    public void run() {
        File input = args.getInput();
        long min = args.getMin();

        setWriter(args.getOutput());

        if (!input.isDirectory()) {
            System.err.println(String.format("%s is not a directory", input.getPath()));
            System.exit(1);
        }

        try {
            collect(input);
            compile();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            H2Util.closeQuietly();
        }
    }

    private void collect(File input) throws SQLException, IOException {
        Connection conn = H2Util.getConnection("/pakCompressInit.sql");
        String query = ResourceUtil.read("/pakCompressCollect.sql");
        Iterator<File> iterator = FileUtils.iterateFiles(input, null, true);
        int prefixLen = input.getPath().length();

        PreparedStatement stmt = conn.prepareStatement(query);

        while (iterator.hasNext()) {
            File file = iterator.next();
            String path = file.getPath().substring(prefixLen).replace('/', '\\');
            byte[] compressedBytes = CompressUtil.compress(file);
            int fileSize = (int) file.length();

            stmt.setString(1, path);
            stmt.setInt(2, fileSize);
            stmt.setBlob(3, new ByteArrayInputStream(compressedBytes));
            stmt.setInt(4, compressedBytes.length);

            // keep track of largest bytes for buffer
            if (largestBytes < compressedBytes.length) {
                largestBytes = compressedBytes.length;
            }

            stmt.execute();
            files++;
        }

        stmt.close();
    }

    private void compile() throws IOException {
        ByteBuffer buf = ByteBuffer.allocateDirect(largestBytes);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        // write header
        int metaStart = PakHeader.RC_START + 8; // row column start + bytes for row column ints.

        buf.put(PakHeader.START);
        buf.put(new byte[PakHeader.RC_START - PakHeader.START_LEN]); // pad bytes
        buf.putInt(files);
        buf.putInt(metaStart);
        buf.flip();
        output.write(buf);

        // start writing the meta
    }

    private void setWriter(File output) {
        if (output == null) {
            args.setWriter(System.out);
        } else {
            try {
                args.setWriter(new FileOutputStream(output));
            } catch (FileNotFoundException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }

        this.output = Channels.newChannel(args.getWriter());
    }
}

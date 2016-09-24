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
import java.sql.*;
import java.util.Iterator;

/**
 * Created by blei on 6/19/16.
 */
public class PakCompress implements Runnable {
    private static final int ROW_LIMIT = 10; // i think storing this many files 'in memory' is enough
    private final CommandPak.Compress args;
    private int largestBytes = PakFile.META_BYTES;
    private int files;
    private WritableByteChannel output;
    private Connection conn;

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
        Iterator<File> iterator = FileUtils.iterateFiles(input, null, true);
        int prefixLen = input.getPath().length();
        conn = H2Util.getConnection("/pakCompressInit.sql");
        String query = ResourceUtil.read("/pakCompressCollect.sql");
        PreparedStatement stmt = conn.prepareStatement(query);

        while (iterator.hasNext()) {
            if (files > 0 && files % ROW_LIMIT == 0) {
                conn.commit();
            }

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

        conn.commit(); // always have something to commit
        stmt.close();
    }

    private void compile() throws IOException, SQLException {
        ByteBuffer buf = ByteBuffer.allocate(largestBytes);
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
        compileMeta();

        // go back to beginning
        compileZData();
    }

    private void compileMeta() throws SQLException, IOException {
        ByteBuffer buf = ByteBuffer.allocateDirect(largestBytes);
        Statement stmt = conn.createStatement();
        int offset = 0;
        int dataPosition = Math.addExact(PakHeader.RC_START + PakHeader.RC_BYTES,
            Math.multiplyExact(PakFile.META_BYTES, files));

        buf.order(ByteOrder.LITTLE_ENDIAN);
        
        while (offset < files) {
            stmt.execute("SELECT * FROM file LIMIT " + ROW_LIMIT + " OFFSET " + offset);

            ResultSet rs = stmt.getResultSet();

            while (rs.next()) {
                String path = rs.getString("path");
                int fileSize = rs.getInt("file_size");
                int zFileSize = rs.getInt("zfile_size");

                buf.clear();
                buf.put(path.getBytes());
                buf.put(new byte[PakFile.PATH_BYTES - path.length()]); // fill rest with null terminating chars
                buf.putInt(fileSize);
                buf.putInt(zFileSize);
                buf.putInt(dataPosition);
                buf.put(new byte[44]); // 44 byte padding for some reason
                buf.flip();

                output.write(buf);

                dataPosition += zFileSize;
            }

            rs.close();

            offset += ROW_LIMIT;
        }

        stmt.close();
    }

    private void compileZData() throws SQLException, IOException {
        Statement stmt = conn.createStatement();
        int lastZFileSize = 0;
        int offset = 0;

        while (offset < files) {
            stmt.execute("SELECT * FROM file LIMIT " + ROW_LIMIT + " OFFSET " + offset);

            ResultSet rs = stmt.getResultSet();

            while (rs.next()) {
                Blob blob = rs.getBlob("zdata");
                int zFileSize = rs.getInt("zfile_size");
                ByteBuffer dataBuffer = ByteBuffer.wrap(blob.getBytes(0, zFileSize));
                blob.free(); // free the blob!

                output.write(dataBuffer);
                lastZFileSize = zFileSize;
            }

            rs.close();

            offset += ROW_LIMIT;
        }

        stmt.close();

        if (lastZFileSize < args.getMin()) {
            output.write(ByteBuffer.allocate((int) (args.getMin() - lastZFileSize)));
        }
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

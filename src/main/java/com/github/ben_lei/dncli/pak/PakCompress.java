package com.github.ben_lei.dncli.pak;

import com.github.ben_lei.dncli.command.CommandPak;
import com.github.ben_lei.dncli.util.CompressUtil;
import com.github.ben_lei.dncli.util.JdbcUtil;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * Created by blei on 6/19/16.
 */
public class PakCompress implements Runnable {
    private final CommandPak.Compress args;

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
            compress(input, min);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            JdbcUtil.closeQuietly();
        }
    }

    private void compress(File input, long min) throws SQLException, IOException {
        Iterator<File> iterator = FileUtils.iterateFiles(input, null, true);
        int prefixLen = input.getPath().length();
        Connection conn = JdbcUtil.getConnection("/pakCompressInit.sql");
        int numFiles = 0;

        PreparedStatement stmt = conn.prepareStatement("INSERT INTO file " +
            "(path, file_size, zdata, zfile_size) VALUES (?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE file_size = ?, zdata = ?, zfile_size = ?");

        while (iterator.hasNext()) {
            File file = iterator.next();
            String path = file.getPath().substring(prefixLen).replace('/', '\\');
            byte[] compressedBytes = CompressUtil.compress(file);
            int fileSize = (int)file.length();
            InputStream is = new ByteArrayInputStream(compressedBytes);

            stmt.setString(1, path);
            stmt.setInt(2, fileSize);
            stmt.setBlob(3, is);
            stmt.setInt(4, compressedBytes.length);
            stmt.setInt(5, fileSize);
            stmt.setBlob(6, is);
            stmt.setInt(7, compressedBytes.length);

            stmt.execute();
            numFiles++;
        }

        stmt.close();
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
    }

    private void write(byte[] bytes) {
        try {
            args.getWriter().write(bytes);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}

package com.github.ben_lei.dncli.pak;

import com.github.ben_lei.dncli.command.CommandPak;
import com.github.ben_lei.dncli.util.CompressUtil;
import com.github.ben_lei.dncli.util.JdbcUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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
        Connection conn = JdbcUtil.getConnection();
        int numFiles = 0;

        try {
            Statement stmt = conn.createStatement();
            stmt.execute(IOUtils.toString(getClass().getResourceAsStream("/pakCompressInit.sql"), "UTF-8"));
            stmt.close();
        } catch (Exception e) {
            // ignore
        }

        PreparedStatement pStmt = conn.prepareStatement("INSERT INTO file " +
            "(path, file_size, zdata, zfile_size) VALUES (?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE file_size = ?, zdata = ?, zfile_size = ?");

        while (iterator.hasNext()) {
            File file = iterator.next();
            String path = file.getPath().substring(prefixLen).replace('/', '\\');
            byte[] compressedBytes = CompressUtil.compress(file);
            int fileSize = (int)file.length();
            InputStream is = new ByteArrayInputStream(compressedBytes);

            pStmt.setString(1, path);
            pStmt.setInt(2, fileSize);
            pStmt.setBlob(3, is);
            pStmt.setInt(4, compressedBytes.length);
            pStmt.setInt(5, fileSize);
            pStmt.setBlob(6, is);
            pStmt.setInt(7, compressedBytes.length);

            pStmt.execute();
            numFiles++;
        }

        pStmt.close();
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

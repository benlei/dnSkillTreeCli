package com.github.ben_lei.dncli.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by blei on 8/2/16.
 */
public final class JdbcUtil {
    private static Connection conn;

    private JdbcUtil() {
        // util class
    }

    public static Connection getConnection() {
        if (conn != null) {
            return conn;
        }

        try {
            String path = System.getProperty("jdbc.h2.file", File.createTempFile("jdbc", "h2").getPath());
            String jdbcUrl = String.format("jdbc:h2:file:%s;MODE=MYSQL;IGNORECASE=TRUE", path);

            System.out.println(jdbcUrl);

            conn = DriverManager.getConnection(jdbcUrl, "sa", "sa");

            return conn;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeQuietly() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // do nothing
            }
        }
    }
}

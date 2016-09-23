package com.github.ben_lei.dncli.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by blei on 8/2/16.
 */
public final class H2Util {
    private static Connection conn;
    private static String path;

    private H2Util() {
        // util class
    }

    public static Connection getConnection(String initScript) {
        if (conn != null) {
            return conn;
        }

        try {
            path = System.getProperty("jdbc.h2.file", File.createTempFile("jdbc", "h2").getPath());
            String jdbcUrl = String.format("jdbc:h2:file:%s;MODE=MYSQL;IGNORECASE=TRUE", path);
            if (initScript != null) {
                jdbcUrl += String.format(";INIT=RUNSCRIPT FROM 'classpath:%s'", initScript);
            }

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

            FileUtils.deleteQuietly(new File(path));
        }
    }
}

package com.dnmaze.dncli.util;

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

  /**
   * <p>Gets a JDBC H2 connection and runs the {@code initScript}.</p>
   *
   * @param initScript the init script
   * @return the connection
   */
  public static Connection getConnection(String initScript) {
    if (conn != null) {
      return conn;
    }

    try {
      File tmp = File.createTempFile("dncli", "jdbc");
      FileUtils.deleteQuietly(tmp);

      path = tmp.getPath();
      String jdbcUrl = String.format("jdbc:h2:file:%s;"
              + "MODE=MYSQL;"
              + "IGNORECASE=TRUE;"
              + "MV_STORE=FALSE;"
              + "MVCC=FALSE",
          path);

      if (initScript != null) {
        jdbcUrl += String.format(";INIT=RUNSCRIPT FROM 'classpath:%s'", initScript);
      }

      conn = DriverManager.getConnection(jdbcUrl,
          System.getProperty("h2.username", "sa"),
          System.getProperty("h2.password", "sa"));
      return conn;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * <p>Quietly closes the H2 connection.</p>
   */
  public static void closeQuietly() {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException ex) {
        System.err.println(ex.getMessage());
      }

      FileUtils.deleteQuietly(new File(path + ".h2.db"));
    }
  }
}

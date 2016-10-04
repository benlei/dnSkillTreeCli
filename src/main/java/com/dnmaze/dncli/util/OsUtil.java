package com.dnmaze.dncli.util;

/**
 * Created by blei on 6/19/16.
 */

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Created by Benjamin Lei on 10/29/2015.
 */
public class OsUtil {
  private static final boolean WINDOWS = System.getProperty("os.name")
      .toLowerCase()
      .contains("win");

  private static Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.displayName());

  /**
   * <p>Checks if running OS is a Windows system.</p>
   *
   * @return true if OS is a Windows system, false otherwise
   */
  public static boolean isWindows() {
    return WINDOWS;
  }

  /**
   * <p>Confirms with the user if they intend to overwrite an existing file.</p>
   *
   * @param file the output file
   * @return true if the user allows it, false otherwise.
   */
  public static boolean confirmOverwrite(File file) {
    if (!file.exists()) {
      return true;
    }

    System.out.print("Enter [Y/n] if you want to overwrite " + file.getPath() + ": ");
    String confirm = scanner.next();
    return "Y".equalsIgnoreCase(confirm);
  }
}

package dncli.utils;

import java.io.File;
import java.util.Scanner;

/**
 * Created by Benjamin Lei on 10/29/2015.
 */
public class OS {
    public static final boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
    public static final boolean unix = ! windows;
    private static Scanner scanner = new Scanner(System.in);

    public static boolean isWindows() {
        return windows;
    }

    public static boolean isUnix() {
        return unix;
    }

    public static boolean confirmOverwrite(File file) {
        if (! file.exists()) {
            return true;
        }

        System.out.print("Enter [Y/n] if you want to overwrite " + file.getPath() + ": ");
        String confirm = scanner.next();
        return "Y".equalsIgnoreCase(confirm);
    }
}

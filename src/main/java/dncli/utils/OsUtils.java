package dncli.utils;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.io.File;
import java.util.Scanner;

/**
 * Created by Benjamin Lei on 10/29/2015.
 */
public class OsUtils {
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

    public static void usage(String command, String argsAppend, String description, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        if (argsAppend != null && !argsAppend.equals("")) {
            argsAppend = " " + argsAppend;
        }
        formatter.printHelp("dn " + command + " [options]" + argsAppend, description, options, null, false);
        System.exit(1);
    }


}

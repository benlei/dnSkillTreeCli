package dncli.utils;

/**
 * Created by Benjamin Lei on 10/29/2015.
 */
public class OS {
    public static final boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
    public static final boolean unix = ! windows;

    public static boolean isWindows() {
        return windows;
    }

    public static boolean isUnix() {
        return unix;
    }
}

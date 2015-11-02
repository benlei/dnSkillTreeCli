package dncli;

import dncli.dds.DDS;
import dncli.dnt.DNT;
import dncli.pak.Pak;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;

import static java.lang.System.err;

public class App {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            mainUsage();
        }

        String cmd = args[0];
        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, subArgs.length);

        CommandLineParser parser = new DefaultParser();
        CommandLine cli;

        if (cmd.equals("pak")) {
            cli = parser.parse(Pak.options, subArgs);
            Pak.use(cli);
        } else if (cmd.equals("dnt")) {
            cli = parser.parse(DNT.options, subArgs);
            DNT.use(cli);
        } else if (cmd.equals("dds")) {
            cli = parser.parse(DDS.options, subArgs);
            DDS.use(cli);
        } else {
            mainUsage();
        }
    }

    public static void mainUsage() {
        err.println("usage: dn <command> [options]");
        err.println("Extract/compacts data from/for DN related resources. Uses JavaScript to allow");
        err.println("for more control over data.\n");
        err.println("Available commands:");
        err.println(" pak            Creates/extracts pak files.");
        err.println(" dnt            Reads data from a .dnt file, and can also create .dnt files.");
        err.println(" dds            Converts .dds files to .png or .jpg image files.");
        System.exit(1);
    }
}

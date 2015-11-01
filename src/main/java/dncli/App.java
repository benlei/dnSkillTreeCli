package dncli;

import dncli.dds.DDS;
import dncli.dnt.DNT;
import dncli.pak.Pak;
import org.apache.commons.cli.*;

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
            boolean hasInfo = cli.hasOption("info");
            boolean hasExtract = cli.hasOption("extract");
            boolean hasCompress = cli.hasOption("compress");
            boolean hasList = cli.hasOption("list");
            boolean hasForce = cli.hasOption("force");
            boolean hasFilter = cli.hasOption("filter");
            int numArgs = cli.getArgList().size();
            if (! (hasInfo ^ hasExtract ^ hasCompress) ||
                    ! hasInfo & hasList ||
                    (numArgs == 0) | hasForce & hasInfo ||
                    hasFilter & ! hasExtract ||
                    hasCompress & (numArgs != 2) ||
                    hasExtract & (numArgs < 2) ||
                    cli.hasOption("help")) {
                usage("pak", "file [file]... [output]",
                        "Inspects/extracts/compresses a pak. For extracting you can specify a filter to evaluate " +
                                "what to extract.\n\nYou cannot specify info/extract/compress options together\n\n" +
                                "Available options:",
                        Pak.options);
            }

            Pak.perform(cli);
        } else if (cmd.equals("dnt")) {
            cli = parser.parse(DNT.options, subArgs);
            if (! (cli.hasOption("compile") ^ cli.hasOption("remodel") ||
                    cli.hasOption("help") ||
                    cli.getArgList().isEmpty())) {
                usage("dnt", "file [file]...",
                        "Parses through DNT file(s) for compiling data for yourself, or remodel a DNT to" +
                                "your own liking.\n\nYou cannot specify the compile and remodel options together\n\n" +
                                "Available options:",
                        DNT.options);
            }
        } else if (cmd.equals("dds")) {
            cli = parser.parse(DDS.options, subArgs);

            if (! (cli.hasOption("png") ^ cli.hasOption("jpg") ||
                    cli.hasOption("help"))) {
                usage("dds", "[file]...",
                        "Converts a DDS file to a PNG/JPG file. Cannot specify both PNG and PNG format.\n\n" +
                                "Available options:", DDS.options);
            }

            DDS.perform(cli);
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

    public static void usage(String command, String argsAppend, String description, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        if (argsAppend != null && !argsAppend.equals("")) {
            argsAppend = " " + argsAppend;
        }
        formatter.printHelp("dn " + command + " [options]" + argsAppend, description, options, null, false);
        System.exit(1);
    }
}

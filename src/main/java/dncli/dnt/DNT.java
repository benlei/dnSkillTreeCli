package dncli.dnt;

import dncli.utils.OS;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Created by Benjamin Lei on 10/29/2015.
 */
public class DNT {
    public final static Options options = new Options();

    static {
        options.addOption(Option.builder("c")
                .longOpt("compile")
                .hasArg()
                .desc("Must have a parsed(dnt) and compile() function for the given JavaScript arg.")
                .build());

        options.addOption(Option.builder("m")
                .longOpt("remodel")
                .hasArg()
                .desc("Must have a remodel(cols,entries) function that returns back a hash with the " +
                        "fields 'cols' that contains the modified cols and 'entries' with " +
                        "the modified entries for the given JavaScript arg.")
                .build());


        options.addOption(Option.builder("q")
                .longOpt("quiet")
                .desc("Quiet output.")
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Shows this usage message.")
                .build());
    }

    public static void checkUsage(CommandLine cli) throws Exception {
        if (!(cli.hasOption("compile") ^ cli.hasOption("remodel") ||
                cli.hasOption("help") ||
                cli.getArgList().isEmpty())) {
            OS.usage("dnt", "file [file]...",
                    "Parses through DNT file(s) for compiling data for yourself, or remodel a DNT to" +
                            "your own liking.\n\nYou cannot specify the compile and remodel options together\n\n" +
                            "Available options:",
                    options);
        }
    }

    public static void use(CommandLine cli) throws Exception {
        checkUsage(cli);
    }
}

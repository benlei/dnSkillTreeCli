package dncli.pak;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Created by Benjamin Lei on 10/29/2015.
 */
public class Pak {
    public final static Options options = new Options();
    static {
        options.addOption(Option.builder("q")
                .longOpt("info")
                .desc("Shows total files in pak, and of how many are marked as deleted.")
                .build());

        options.addOption(Option.builder("l")
                .longOpt("list")
                .desc("(info only) Lists all the files and their sizes in the pak.")
                .build());

        options.addOption(Option.builder("x")
                .longOpt("extract")
                .desc("Extracts pak file.")
                .build());

        options.addOption(Option.builder("c")
                .longOpt("compress")
                .desc("Compresses a directory into a pak file.")
                .build());

        options.addOption(Option.builder("f")
                .longOpt("force")
                .desc("(extract/compress with no filter only) Forces overwriting of output file(s) without prompting.")
                .build());

        options.addOption(Option.builder("e")
                .longOpt("filter")
                .hasArg()
                .desc("(extract only) Uses input JS file's filter function that must return a boolean to decide what to extract.")
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Shows this usage message.")
                .build());
    }

    public static void perform(CommandLine cli) throws Exception {

    }
}

package dncli.dnt;

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

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Shows this usage message.")
                .build());
    }

    public static void perform(CommandLine cli) throws Exception {

    }
}

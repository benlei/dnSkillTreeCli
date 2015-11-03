package dncli.dnt;

import dncli.utils.JSUtils;
import dncli.utils.OsUtils;
import jdk.nashorn.api.scripting.JSObject;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import javax.script.Invocable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin Lei on 10/29/2015.
 */
public class DNT {
    public final static Options options = new Options();

    static {
        options.addOption(Option.builder("c")
                .longOpt("compile")
                .hasArg()
                .desc("Must have an accumulate(file,list) and compile() function for the given JavaScript arg.")
                .build());

        options.addOption(Option.builder("m")
                .longOpt("model")
                .hasArg()
                .desc("Must have a model(cols,entries) function that returns back a hash with the " +
                        "fields 'cols' that contains the modified cols and 'entries' with " +
                        "the modified entries for the given JavaScript arg. If there is only one " +
                        "remaining arg, that will be used as the output file. If there are two, the " +
                        "first will be used as the DNT to parse + pass the cols and entries, and the " +
                        "second will be for the output file.")
                .build());


        options.addOption(Option.builder("f")
                .longOpt("force")
                .desc("(model only) Forces overwriting of output file without confirmation.")
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
        boolean isCompile = cli.hasOption("compile");
        boolean isModel = cli.hasOption("model");
        boolean isForce = cli.hasOption("force");
        int numArgs = cli.getArgList().size();
        if (! (isCompile ^ isModel) ||
                isModel & (numArgs > 2) ||
                ! isModel & isForce ||
                isCompile & (numArgs == 0) ||
                cli.hasOption("help")) {
            OsUtils.usage("dnt", "file [file]...",
                    "Parses through DNT file(s) for compiling data for yourself, or (re)model a DNT to" +
                            "your own liking.\n\nYou cannot specify the compile and remodel options together\n\n" +
                            "Available options:",
                    options);
        }
    }

    public static void use(CommandLine cli) throws Exception {
        checkUsage(cli);

        if (cli.hasOption("compile")) {
            compile(cli);
        } else {
            model(cli);
        }
    }


    private static void compile(CommandLine cli) throws Exception {
        List<String> dntPaths = cli.getArgList();
        List<File> dnts = new ArrayList<>();
        String scriptPath = cli.getOptionValue("compile");
        File script = new File(scriptPath);
        if (! script.exists()) {
            throw new FileNotFoundException(scriptPath);
        }

        Invocable invocable = (Invocable) JSUtils.compile(script);

        for (String path : dntPaths) {
            File file = new File(path);
            if (! file.exists()) {
                throw new FileNotFoundException(path);
            }
            dnts.add(file);
        }


        for (File file : dnts) {
            DNTParser parser = new DNTParser(file);
            JSObject map = parser.parse();
            invocable.invokeFunction("accumulate", file, map.getMember("entries"));
        }

        invocable.invokeFunction("compile");
    }

    private static void model(CommandLine cli) throws Exception {
        List<String> args = cli.getArgList();
        String scriptPath = cli.getOptionValue("model");
        File script = new File(scriptPath);
        File dntFile = null;
        File outputDnt;

        if (args.size() == 1) {
            outputDnt = new File(args.get(0));
        } else {
            dntFile = new File(args.get(0));
            if (! dntFile.exists()) {
                throw new FileNotFoundException(args.get(0));
            }

            outputDnt = new File(args.get(1));
        }

        if (outputDnt.exists() && ! cli.hasOption("force") && ! OsUtils.confirmOverwrite(outputDnt)) {
            return;
        }

        if (! script.exists()) {
            throw new FileNotFoundException(scriptPath);
        }

        Invocable invocable = (Invocable) JSUtils.compile(script);

        JSObject map;
        JSObject cols = JSUtils.newObject();
        JSObject entries = JSUtils.newArray();

        if (dntFile == null) { // no input dnt given
            map = (JSObject) invocable.invokeFunction("model", cols, entries);
        } else {
            DNTParser parser = new DNTParser(dntFile);
            map = parser.parse();
            map = (JSObject)invocable.invokeFunction("remodel", map.getMember("cols"), map.getMember(("entries")));
        }

        DNTWriter writer = new DNTWriter(outputDnt);
        cols = (JSObject)map.getMember("cols");
        entries = (JSObject) map.getMember("entries");
        writer.write(cols, entries);
    }
}

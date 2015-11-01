package dncli.pak;

import dncli.utils.OS;
import jdk.nashorn.api.scripting.JSObject;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by Benjamin Lei on 10/29/2015.
 */
public class Pak {
    public static final Options options = new Options();
    public static final String HEADER = "EyedentityGames Packing File 0.1";
    public static final int META_START = 260;
    public static final byte[] data = new byte[10485760];

    static {
        options.addOption(Option.builder("i")
                .longOpt("info")
                .desc("Shows total files in pak, and of how many are marked as deleted.")
                .build());

        options.addOption(Option.builder("l")
                .longOpt("list")
                .desc("(info only) Lists all the files and their sizes in the pak.")
                .build());

        options.addOption(Option.builder("x")
                .longOpt("extract")
                .desc("Extracts non-deleted files from one or more pak file.")
                .build());

        options.addOption(Option.builder("c")
                .longOpt("compress")
                .desc("Compresses a directory into a pak file. First argument must be the directory, second must be output file.")
                .build());

        options.addOption(Option.builder("f")
                .longOpt("force")
                .desc("(extract/compress with no filter only) Forces overwriting of output file(s) without prompting.")
                .build());

        options.addOption(Option.builder("e")
                .longOpt("filter")
                .hasArg()
                .desc("(extract only) Uses input JS file's filter(object) function that must return a boolean to decide what to extract.")
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Shows this usage message.")
                .build());
    }

    public static void perform(CommandLine cli) throws Exception {
        if (cli.hasOption("extract")) {
            extract(cli);
        } else if (cli.hasOption("compress")) {
            compress(cli);
        } else {
            info(cli);
        }
    }

    private static void info(CommandLine cli) throws Exception {
        List<String> paks = cli.getArgList();
        ArrayList<PakReader> pakList = new ArrayList<>();
        for (String pak : paks) {
            File file = new File(pak);
            if (! file.exists()) {
                throw new FileNotFoundException(pak + " does not exist.");
            }

            pakList.add(new PakReader(file, false));
        }

        boolean showList = cli.hasOption("list");
        for (int i = 0; i < paks.size(); i++) {
            PakReader reader = pakList.get(i);
            ArrayList<JSObject> jsObjects = new ArrayList<>();
            int deleted = 0;
            JSObject jsObject;
            while ((jsObject = reader.read()) != null) {
                if (showList) {
                    jsObjects.add(jsObject);
                }

                int size = (Integer)jsObject.getMember("size");
                if (size == 0) {
                    deleted++;
                }
            }

            System.out.println(String.format("%s contains %d objects, %d of which are marked as deleted.",
                    paks.get(i),
                    reader.getSize(),
                    deleted));

            if (showList) {
                for (JSObject js : jsObjects) {
                    int size = (Integer) js.getMember("size");
                    int zSize = (Integer) js.getMember("zsize");
                    String extraMessage;
                    if (size == 0) {
                        extraMessage = "*deleted*";
                    } else {
                        double percent = (1.0 - ((float) zSize / (float) size)) * 100.0;
                        extraMessage = String.format("(%.3f%% compressed)", percent);
                    }
                    System.out.println(String.format("%s %s",
                            js.getMember("path"),
                            extraMessage));
                }
            }
        }
    }

    private static void extract(CommandLine cli) throws Exception {
        List<String> paks = cli.getArgList();
        String output = paks.remove(paks.size() - 1);
        File outputFile = new File(output);
        boolean force = cli.hasOption("force");
        Invocable invocable = null;
        if (cli.hasOption("filter")) {
            String filter = cli.getOptionValue("filter");
            File filterFile = new File(filter);
            if (! filterFile.exists()) {
                throw new FileNotFoundException(filter + " does not exist.");
            }

            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
            ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("nashorn");
            scriptEngine.eval(new FileReader(filterFile));
            invocable = (Invocable)scriptEngine;
        }

        for (String pak : paks) {
            int extracted = 0;
            PakReader reader = new PakReader(new File(pak));
            JSObject jsObject;
            long start = System.currentTimeMillis();
            while ((jsObject = reader.read()) != null) {
                if (invocable != null) {
                    if ((Boolean)invocable.invokeFunction("filter", jsObject)) {
                        extractTo(outputFile, force, jsObject);
                        extracted++;
                    }
                } else if ((Integer)jsObject.getMember("size") != 0) {
                    extractTo(outputFile, force, jsObject);
                    extracted++;
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("Extracted " + extracted + " objects from " + pak + " in " + (end - start) + "ms");
        }
    }

    private static void extractTo(File parent, boolean force, JSObject jsObject) throws Exception {
        String child = jsObject.getMember("path").toString();
        if (OS.isUnix()) {
            child = child.replace('\\', '/');
        }

        File output = new File(parent, child);
        if (! force && ! OS.confirmOverwrite(output)) {
            return;
        }

        int read;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Inflater inflater = new Inflater();

        inflater.setInput((byte[])jsObject.getMember("data"));
        while ((read=inflater.inflate(data)) != 0) {
            baos.write(data, 0, read);
        }
        inflater.end();

        output.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(output);
        baos.writeTo(out);
        out.close();
        System.out.println(output.getPath());
    }

    private static void compress(CommandLine cli) throws Exception {
        List<String> args = cli.getArgList();
        String dir = args.get(0);
        String output = args.get(1);
        boolean force = cli.hasOption("force");
        File dirFile = new File(dir);
        File outputFile = new File(output);


        int dirLen = dirFile.getPath().length();
        Iterator<File> iterator = FileUtils.iterateFiles(dirFile, null, true);
        while (iterator.hasNext()) {
            File file = iterator.next();
            String path = file.getPath().substring(dirLen);
            if (OS.isUnix()) {
                path = path.replace('/', '\\');
            }
//            System.out.println(path);
        }

    }
}

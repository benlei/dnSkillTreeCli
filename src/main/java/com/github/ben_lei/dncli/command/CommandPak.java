package com.github.ben_lei.dncli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;
import com.github.ben_lei.dncli.converter.ByteCharacterConverter;
import com.github.ben_lei.dncli.pak.PakCompress;
import com.github.ben_lei.dncli.pak.PakDetail;
import com.github.ben_lei.dncli.pak.PakExtract;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by blei on 6/16/16.
 */
@Parameters(commandDescription = "DragonNest pak file extraction and compression.")
public class CommandPak {
    private final Compress compress = new Compress();
    private final Extract extract = new Extract();
    private final Detail detail = new Detail();
    private final Command command;

    CommandPak(Command command) {
        this.command = command;
    }

    public Compress getCompress() {
        return compress;
    }

    public Extract getExtract() {
        return extract;
    }

    public Detail getDetail() {
        return detail;
    }

    @Parameters(commandDescription = "Compresses a directory into a pak file.")
    public class Compress implements Runnable {
        private final Runnable runner = new PakCompress(this);

        @Parameter(names = {"-i", "--input"}, description = "Input root \"\\\" directory.", converter = FileConverter.class, required = true)
        private File input;

        @Parameter(names = {"-o", "--output"}, description = "Output contents to provided file.", converter = FileConverter.class)
        private File output;

        @Parameter(names = {"-m", "--min"}, converter = ByteCharacterConverter.class,
            description = "Sets the min. size a compressed pak can be.")
        private Long min = 0L;

        @Parameter(names = {"-f", "--force"}, description = "Force overwrite files")
        private boolean force;

        public File getInput() {
            return input;
        }

        public File getOutput() {
            return output;
        }

        public Long getMin() {
            return min;
        }

        public boolean isForce() {
            return force;
        }

        public boolean isQuiet() {
            return command.isQuiet();
        }

        @Override
        public void run() {
            runner.run();
        }
    }

    @Parameters(commandDescription = "Extracts DragonNest pak files to an output directory.")
    public class Extract implements Runnable {
        private final Runnable runner = new PakExtract(this);

        @Parameter(description = "pakFiles...", required = true)
        private List<File> files = new ArrayList<>();

        @Parameter(names = {"-o", "--output"}, description = "Output directory to extract the paks.", converter = FileConverter.class, required = true)
        private File output;

        @Parameter(names = {"-j", "--javascript"}, description = "The filter JS file that should have a pakCompressFilter() function.",
            converter = FileConverter.class)
        private File filterFile;

        @Parameter(names = {"-f", "--force"}, description = "Force overwrite files")
        private boolean force;

        public List<File> getFiles() {
            return files;
        }

        public File getOutput() {
            return output;
        }

        public File getFilterFile() {
            return filterFile;
        }

        public boolean isForce() {
            return force;
        }

        public boolean isQuiet() {
            return command.isQuiet();
        }

        @Override
        public void run() {
            runner.run();
        }
    }

    @Parameters(commandDescription = "lists all files in pak")
    public class Detail implements Runnable {
        private final Runnable runner = new PakDetail(this);

        @Parameter(description = "pakFiles...", required = true)
        private List<File> files = new ArrayList<>();

        public List<File> getFiles() {
            return files;
        }

        public boolean isQuiet() {
            return command.isQuiet();
        }

        @Override
        public void run() {
            runner.run();
        }
    }
}

package com.github.ben_lei.dncli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;
import com.github.ben_lei.dncli.converter.DdsOutputFormatConverter;
import com.github.ben_lei.dncli.dds.DdsConverter;
import com.github.ben_lei.dncli.enums.DdsOutputFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blei on 6/15/16.
 */
@Parameters(commandNames = "dds", commandDescription = "Microsoft DDS to JPG/PNG converter.")
public class CommandDds implements Runnable {
    private final Command command;

    private final Runnable runner = new DdsConverter(this);

    @Parameter(names = {"-f", "--force"}, description = "Forces overwriting of " +
        "converted file without confirmation")
    private boolean force;

    @Parameter(names = {"-m", "--format"}, description = "The output file format. " +
        "Valid options are png and jpg.", converter = DdsOutputFormatConverter.class,
        required = true)
    private DdsOutputFormat format;

    @Parameter(description = "ddsFiles...", converter = FileConverter.class)
    private List<String> files = new ArrayList<>();

    CommandDds(Command command) {
        this.command = command;
    }

    public boolean isForce() {
        return force;
    }

    public DdsOutputFormat getFormat() {
        return format;
    }

    public List<String> getFiles() {
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

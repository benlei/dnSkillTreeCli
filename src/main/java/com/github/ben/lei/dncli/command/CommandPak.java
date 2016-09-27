package com.github.ben.lei.dncli.command;

import com.github.ben.lei.dncli.converter.ByteCharacterConverter;
import com.github.ben.lei.dncli.pak.PakCompress;
import com.github.ben.lei.dncli.pak.PakDetail;
import com.github.ben.lei.dncli.pak.PakExtract;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by blei on 6/16/16.
 */
@Parameters(commandDescription = "DragonNest pak file extraction and compression.")
public class CommandPak {
  @Getter
  private final Compress compress = new Compress();

  @Getter
  private final Extract extract = new Extract();

  @Getter
  private final Detail detail = new Detail();

  @Parameters(commandDescription = "Compresses a directory into a pak file.")
  public static class Compress implements Runnable {
    private final Runnable runner = new PakCompress(this);

    @Getter
    @Parameter(names = "-input",
        description = "Input root \"\\\" directory.",
        converter = FileConverter.class,
        required = true)
    private File input;

    @Getter
    @Parameter(names = "-output",
        description = "Output contents to provided file.",
        converter = FileConverter.class)
    private File output;

    @Getter
    @Setter
    private OutputStream writer;

    @Getter
    @Parameter(names = "-min",
        converter = ByteCharacterConverter.class,
        description = "Sets the min. size a compressed pak can be.")
    private Long min = 0L;

    @Override
    public void run() {
      runner.run();
    }
  }

  @Parameters(commandDescription = "Extracts DragonNest pak files to an output directory.")
  public static class Extract implements Runnable {
    private final Runnable runner = new PakExtract(this);

    @Getter
    @Parameter(description = "pakFiles...")
    private List<File> files = new ArrayList<>();

    @Getter
    @Parameter(names = "-output",
        description = "Output directory to extract the paks.",
        converter = FileConverter.class,
        required = true)
    private File output;

    @Getter
    @Parameter(names = "-js",
        description = "The filter JS file that should have a pakCompressFilter() function.",
        converter = FileConverter.class)
    private File filterFile;

    @Getter
    @Parameter(names = "-force", description = "Force overwrite files")
    private boolean force;

    @Getter
    @Parameter(names = "-quiet", description = "Quiet output")
    private boolean quiet;

    @Override
    public void run() {
      runner.run();
    }
  }

  @Parameters(commandDescription = "lists all files in pak")
  public static class Detail implements Runnable {
    private final Runnable runner = new PakDetail(this);

    @Getter
    @Parameter(names = "-quiet", description = "Quiet output")
    private boolean quiet;

    @Getter
    @Parameter(description = "pakFiles...")
    private List<File> files = new ArrayList<>();

    @Override
    public void run() {
      runner.run();
    }
  }
}

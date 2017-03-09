package com.dnmaze.dncli.command;

import com.dnmaze.dncli.converter.ByteCharacterConverter;
import com.dnmaze.dncli.pak.PakCompress;
import com.dnmaze.dncli.pak.PakDetail;
import com.dnmaze.dncli.pak.PakExtract;
import com.dnmaze.dncli.pak.PakInflate;

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

  @Getter
  private final Inflate inflate = new Inflate();

  @Parameter(names = "-help", description = "Displays this usage.", help = true)
  @Getter
  private boolean help;

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
        converter = FileConverter.class,
        required = true)
    private File output;

    @Getter
    @Setter
    private OutputStream writer;

    @Getter
    @Parameter(names = "-min",
        converter = ByteCharacterConverter.class,
        description = "Sets the min. size a compressed pak can be.")
    private Long min = 0L;

    @Parameter(names = "-help", description = "Displays this usage.", help = true)
    @Getter
    private boolean help;

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

    @Parameter(names = "-help", description = "Displays this usage.", help = true)
    @Getter
    private boolean help;

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

    @Parameter(names = "-help", description = "Displays this usage.", help = true)
    @Getter
    private boolean help;

    @Override
    public void run() {
      runner.run();
    }
  }

  @Parameters(commandDescription = "Inflates all pak files to a certain size. Technically, "
                                   + "this is a general file inflater.")
  public static class Inflate implements Runnable {
    private final Runnable runner = new PakInflate(this);

    @Getter
    @Parameter(names = "-size",
        converter = ByteCharacterConverter.class,
        description = "Sets the min. size all paks should be.")
    private Long size = 0L;


    @Getter
    @Parameter(description = "pakFiles...", required = true)
    private List<File> files = new ArrayList<>();

    @Parameter(names = "-help", description = "Displays this usage.", help = true)
    @Getter
    private boolean help;

    @Override
    public void run() {
      runner.run();
    }
  }
}

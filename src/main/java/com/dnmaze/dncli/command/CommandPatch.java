package com.dnmaze.dncli.command;

import com.dnmaze.dncli.patch.Patcher;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;
import com.beust.jcommander.converters.URLConverter;
import lombok.Getter;

import java.io.File;
import java.net.URL;

/**
 * Created by blei on 6/15/16.
 */
@Parameters(commandNames = "patch",
    commandDescription = "DragonNest patch downloader, assuming you know the base URL of the patch"
                         + " server")
public class CommandPatch implements Runnable {
  private final Runnable runner = new Patcher(this);

  @Getter
  @Parameter(names = "-force",
      description = "Forces overwriting of existing files without confirmation, including the "
                    + "version file.")
  private boolean force;

  @Getter
  @Parameter(names = "-quiet",
      description = "Quiet (no) output.")
  private boolean quiet;

  @Getter
  @Parameter(names = "-version",
      description = "The current version; the patcher will attempt to download starting the "
                    + "next version.",
      required = true)
  private int baseVersion;

  @Getter
  @Parameter(names = "-endversion",
      description = "The version to stop downloading the patch.")
  private int endVersion = Integer.MAX_VALUE;

  @Getter
  @Parameter(names = "-output",
      description = "Output contents to provided file.",
      converter = FileConverter.class)
  private File output = new File("./"); // default is curr dir


  @Getter
  @Parameter(names = "-versionfile",
      description = "The file to output the end version to.",
      converter = FileConverter.class)
  private File versionFile;

  @Getter
  @Parameter(names = "-url",
      description = "The base URL of the patch server. It should be everything until "
                    + "something like /00000123/Patch00000123.pak",
      converter = URLConverter.class,
      required = true)
  private URL url;

  @Override
  public void run() {
    runner.run();
  }
}

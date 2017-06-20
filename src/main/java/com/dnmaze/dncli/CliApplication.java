package com.dnmaze.dncli;

import com.dnmaze.dncli.command.Command;
import com.dnmaze.dncli.command.CommandDds;
import com.dnmaze.dncli.command.CommandDnt;
import com.dnmaze.dncli.command.CommandPak;
import com.dnmaze.dncli.command.CommandPatch;
import com.dnmaze.dncli.exception.InvalidDdsOutputFormatException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Created by blei on 6/15/16.
 */
@SuppressFBWarnings("DM_EXIT")
@Slf4j
public class CliApplication {
  /**
   * <p>The main program.</p>
   *
   * @param args the args
   */
  @SneakyThrows
  public static void main(final String[] args) {
    // register H2
    Class.forName("org.h2.Driver");

    final CliApplication app = new CliApplication();
    app.run(args);
  }

  /**
   * runs the cli app.
   */
  @SneakyThrows
  public void run(final String[] args) {
    final Command command = new Command();
    final JCommander jc = new JCommander(command);
    jc.setProgramName("dncli");

    // pak command setup
    final CommandPak pak = command.getPak();
    final JCommander pakJc = addCommand(jc, "pak", pak);

    // pak subcommand setup
    pakJc.addCommand("compress", pak.getCompress());
    pakJc.addCommand("extract", pak.getExtract());
    pakJc.addCommand("inflate", pak.getInflate());
    pakJc.addCommand("list", pak.getDetail());

    // dnt command setup
    final CommandDnt dnt = command.getDnt();
    final JCommander dntJc = addCommand(jc, "dnt", dnt);

    //dnt subcommand setup
    dntJc.addCommand("process", dnt.getProcess());
    dntJc.addCommand("execute", dnt.getExecute());

    // dds command setup
    final CommandDds dds = command.getDds();
    jc.addCommand("dds", dds);

    // patch command setup
    final CommandPatch patch = command.getPatch();
    jc.addCommand("patch", patch);

    // parse args and set the params!
    try {
      jc.parse(args);
    } catch (ParameterException | InvalidDdsOutputFormatException ex) {
      log.error(ex.getMessage(), ex);
      System.exit(1);
    }

    final String parsedCommand = jc.getParsedCommand();

    // if no command or help was specified, show it!
    if (parsedCommand == null || command.isHelp()) {
      jc.usage();
      System.exit(0);
    }

    // find out what command is being used
    executeCommand(parsedCommand, jc, dds, patch, pak, dnt);
    System.exit(0);
  }

  private void executeCommand(final String parsedCommand,
                              final JCommander jc,
                              final CommandDds dds,
                              final CommandPatch patch,
                              final CommandPak pak,
                              final CommandDnt dnt) {
    final Map<String, JCommander> jcCommands = jc.getCommands();
    final JCommander pakJc = jcCommands.get("pak");
    final JCommander dntJc = jcCommands.get("dnt");

    switch (parsedCommand) {
      case "pak":
        final String pakCommand = pakJc.getParsedCommand();
        if (pakCommand == null || pak.isHelp()) {
          jc.usage("pak");
          System.exit(1);
        }

        pak.accept(pakJc, pakCommand);
        break;
      case "dnt":
        final String dntCommand = dntJc.getParsedCommand();
        if (dntCommand == null || dnt.isHelp()) {
          jc.usage("dnt");
          System.exit(1);
        }

        dnt.accept(dntJc, dntCommand);
        break;
      case "dds":
        if (dds.isHelp()) {
          jc.usage("dds");
          System.exit(1);
        }

        dds.run();
        break;
      case "patch":
        if (patch.isHelp()) {
          jc.usage("patch");
          System.exit(1);
        }

        patch.run();
        break;

      default:
        throw new UnsupportedOperationException("Unknown command: '" + parsedCommand + "'");

    }
  }

  private JCommander addCommand(final JCommander jcommander,
                                final String name,
                                final Object object) {
    jcommander.addCommand(name, object);

    final Map<String, JCommander> commands = jcommander.getCommands();
    return commands.get(name);
  }
}

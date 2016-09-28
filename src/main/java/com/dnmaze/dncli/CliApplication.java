package com.dnmaze.dncli;

import com.dnmaze.dncli.command.Command;
import com.dnmaze.dncli.command.CommandDds;
import com.dnmaze.dncli.command.CommandDnt;
import com.dnmaze.dncli.command.CommandPak;
import com.dnmaze.dncli.exception.InvalidDdsOutputFormatException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import java.util.Map;

/**
 * Created by blei on 6/15/16.
 */
public class CliApplication {
  static {
    try {
      Class.forName("org.h2.Driver");
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * <p>The main program.</p>
   *
   * @param args the args
   */
  public static void main(String[] args) {
    Command command = new Command();
    JCommander jc = new JCommander(command);
    jc.setProgramName("dncli");

    // pak command setup
    CommandPak pak = command.getPak();
    JCommander pakJc = addCommand(jc, "pak", pak);

    // pak subcommand setup
    pakJc.addCommand("-compress", pak.getCompress());
    pakJc.addCommand("-extract", pak.getExtract());
    pakJc.addCommand("-list", pak.getDetail());

    // dnt command setup
    CommandDnt dnt = command.getDnt();
    JCommander dntJc = addCommand(jc, "dnt", dnt);

    //dnt subcommand setup
    dntJc.addCommand("-query", dnt.getQuery());

    // dds command setup
    CommandDds dds = command.getDds();
    jc.addCommand("dds", dds);

    // parse args and set the params!
    try {
      jc.parse(args);
    } catch (ParameterException | InvalidDdsOutputFormatException ex) {
      System.out.println(ex.getMessage());
      System.exit(1);
    }

    String parsedCommand = jc.getParsedCommand();

    // if no command or help was specified, show it!
    if (parsedCommand == null || command.isHelp()) {
      jc.usage();
      System.exit(0);
    }

    // find out what command is being used
    try {
      switch (parsedCommand) {
        case "pak":
          switch (pakJc.getParsedCommand()) {
            case "-compress":
              pak.getCompress().run();
              break;
            case "-extract":
              pak.getExtract().run();
              break;
            case "-list":
              pak.getDetail().run();
              break;
            default:
              throw new UnsupportedOperationException("Unknown pak command: '"
                  + pakJc.getParsedCommand() + "'");
          }

          break;
        case "dnt":
          switch (dntJc.getParsedCommand()) {
            case "-query":
              dnt.getQuery().run();
              break;
            default:
              throw new UnsupportedOperationException("Unknown dnt command: '"
                  + dntJc.getParsedCommand() + "'");
          }

          break;
        case "dds":
          dds.run();
          break;

        default:
          throw new UnsupportedOperationException("Unknown command: '" + parsedCommand + "'");
      }

      System.exit(0);
    } catch (Throwable th) {
      System.err.println(th.getMessage());
      System.exit(1);
    }
  }

  private static JCommander addCommand(JCommander jcommander, String name, Object object) {
    jcommander.addCommand(name, object);

    Map<String, JCommander> commands = jcommander.getCommands();
    return commands.get(name);
  }
}

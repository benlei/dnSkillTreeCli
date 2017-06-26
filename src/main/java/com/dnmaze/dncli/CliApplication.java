package com.dnmaze.dncli;

import com.dnmaze.dncli.command.Command;
import com.dnmaze.dncli.command.CommandDds;
import com.dnmaze.dncli.command.CommandDnt;
import com.dnmaze.dncli.command.CommandDnt.Execute;
import com.dnmaze.dncli.command.CommandDnt.Process;
import com.dnmaze.dncli.command.CommandPak;
import com.dnmaze.dncli.command.CommandPak.Compress;
import com.dnmaze.dncli.command.CommandPak.Detail;
import com.dnmaze.dncli.command.CommandPak.Extract;
import com.dnmaze.dncli.command.CommandPak.Inflate;
import com.dnmaze.dncli.command.CommandPatch;
import com.dnmaze.dncli.exception.InvalidDdsOutputFormatException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import lombok.SneakyThrows;

import java.util.Map;

/**
 * Created by blei on 6/15/16.
 */
public class CliApplication {
  /**
   * <p>The main program.</p>
   *
   * @param args the args
   */
  @SneakyThrows
  public static void main(String[] args) {
    // register H2
    Class.forName("org.h2.Driver");

    Command command = new Command();
    JCommander jc = new JCommander(command);
    jc.setProgramName("dncli");

    // pak command setup
    CommandPak pak = command.getPak();
    JCommander pakJc = addCommand(jc, "pak", pak);

    // pak subcommand setup
    pakJc.addCommand("compress", pak.getCompress());
    pakJc.addCommand("extract", pak.getExtract());
    pakJc.addCommand("inflate", pak.getInflate());
    pakJc.addCommand("list", pak.getDetail());

    // dnt command setup
    CommandDnt dnt = command.getDnt();
    JCommander dntJc = addCommand(jc, "dnt", dnt);

    //dnt subcommand setup
    dntJc.addCommand("process", dnt.getProcess());
    dntJc.addCommand("execute", dnt.getExecute());

    // dds command setup
    CommandDds dds = command.getDds();
    jc.addCommand("dds", dds);

    // patch command setup
    CommandPatch patch = command.getPatch();
    jc.addCommand("patch", patch);

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
          String pakCommand = pakJc.getParsedCommand();
          if (pakCommand == null || pak.isHelp()) {
            jc.usage("pak");
            System.exit(1);
          }

          switch (pakCommand) {
            case "compress":
              Compress compress = pak.getCompress();

              if (compress.isHelp()) {
                pakJc.usage("compress");
                System.exit(1);
              }

              compress.run();
              break;
            case "extract":
              Extract extract = pak.getExtract();

              if (extract.isHelp()) {
                pakJc.usage("extract");
                System.exit(1);
              }

              extract.run();
              break;
            case "inflate":
              Inflate inflate = pak.getInflate();

              if (inflate.isHelp()) {
                pakJc.usage("inflate");
                System.exit(1);
              }

              inflate.run();
              break;
            case "list":
              Detail detail = pak.getDetail();

              if (detail.isHelp()) {
                pakJc.usage("list");
                System.exit(1);
              }

              detail.run();
              break;
            default:
              throw new UnsupportedOperationException("Unknown pak command: '"
                                                      + pakCommand + "'");
          }

          break;
        case "dnt":
          String dntCommand = dntJc.getParsedCommand();
          if (dntCommand == null || dnt.isHelp()) {
            jc.usage("dnt");
            System.exit(1);
          }

          switch (dntCommand) {
            case "process":
              Process process = dnt.getProcess();

              if (process.isHelp()) {
                dntJc.usage("process");
                System.exit(1);
              }

              dnt.getProcess().run();
              break;
            case "execute":
              Execute execute = dnt.getExecute();

              if (execute.isHelp()) {
                dntJc.usage("execute");
                System.exit(1);
              }

              execute.run();
              break;
            default:
              throw new UnsupportedOperationException("Unknown dnt command: '"
                                                      + dntCommand + "'");
          }

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

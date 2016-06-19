package com.github.ben_lei.dncli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.github.ben_lei.dncli.command.Command;
import com.github.ben_lei.dncli.command.CommandDds;
import com.github.ben_lei.dncli.command.CommandDnt;
import com.github.ben_lei.dncli.command.CommandPak;
import com.github.ben_lei.dncli.exception.InvalidDdsOutputFormatException;

import java.util.Map;

import static java.lang.System.exit;

/**
 * Created by blei on 6/15/16.
 */
public class CliApplication {
    public static void main(String[] args) {
        Command command = new Command();
        CommandPak pak = command.getPak();
        CommandDnt dnt = command.getDnt();
        CommandDds dds = command.getDds();

        JCommander jc = new JCommander(command);
        jc.setProgramName("dncli");

        // pak command setup
        JCommander pakJc = addCommand(jc, "pak", pak);

        // pak subcommand setup
        pakJc.addCommand("-c", pak.getCompress(), "--compress");
        pakJc.addCommand("-x", pak.getExtract(), "--extract");
        pakJc.addCommand("-l", pak.getDetail(), "--list");

        // dnt command setup
        JCommander dntJc = addCommand(jc, "dnt", dnt);

        //dnt subcommand setup
        dntJc.addCommand("-q", dnt.getQuery(), "--query");
        dntJc.addCommand("-b", dnt.getBuild(), "--build");

        // dds command setup
        jc.addCommand("dds", dds);

        // parse args and set the params!
        try {
            jc.parse(args);
        } catch (ParameterException | InvalidDdsOutputFormatException e) {
            System.out.println(e.getMessage());
            exit(1);
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
                        case "-c":
                        case "--compress":
                            pak.getCompress().run();
                            break;
                        case "-x":
                        case "--extract":
                            pak.getExtract().run();
                            break;
                        case "-l":
                        case "--list":
                            pak.getDetail().run();
                            break;
                        default:
                            throw new UnsupportedOperationException("Unknown pak command: '" + pakJc.getParsedCommand() + "'");
                    }

                    break;
                case "dnt":
                    switch (dntJc.getParsedCommand()) {
                        case "-q":
                        case "--query":
                            dnt.getQuery().run();
                            break;
                        case "-b":
                        case "--build":
                            dnt.getBuild().run();
                            break;
                        default:
                            throw new UnsupportedOperationException("Unknown dnt command: '" + dntJc.getParsedCommand() + "'");
                    }

                    break;
                case "dds":
                    dds.run();
                    break;

                default:
                    throw new UnsupportedOperationException("Unknown command: '" + parsedCommand + "'");
            }

            System.exit(0);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            System.out.println(111);
            System.exit(1);
        }
    }

    private static JCommander addCommand(JCommander jCommander, String name, Object object) {
        jCommander.addCommand(name, object);

        Map<String, JCommander> commands = jCommander.getCommands();
        return commands.get(name);
    }
}

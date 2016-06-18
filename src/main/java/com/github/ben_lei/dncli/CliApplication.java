package com.github.ben_lei.dncli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.github.ben_lei.dncli.command.CommandBase;
import com.github.ben_lei.dncli.command.CommandDds;
import com.github.ben_lei.dncli.command.CommandDnt;
import com.github.ben_lei.dncli.command.CommandPak;
import com.github.ben_lei.dncli.exception.InvalidDdsOutputFormatException;

import java.util.Map;

/**
 * Created by blei on 6/15/16.
 */
public class CliApplication {
    public static void main(String[] args) {
        CommandBase commandBase = new CommandBase();
        JCommander mainCommand = new JCommander(commandBase);
        mainCommand.setProgramName("dncli");

        // dds command setup
        CommandDds commandDds = new CommandDds();
        mainCommand.addCommand("dds", commandDds);

        // pak command setup
        CommandPak commandPak = new CommandPak();
        JCommander pakCommand = addCommand(mainCommand, "pak", commandPak);

        // pak subcommand setup
        CommandPak.Compress commandPakCompress = new CommandPak.Compress();
        CommandPak.Extract commandPakExtract = new CommandPak.Extract();
        CommandPak.Detail commandPakList = new CommandPak.Detail();
        pakCommand.addCommand("-c", commandPakCompress, "--compress");
        pakCommand.addCommand("-x", commandPakExtract, "--extract");
        pakCommand.addCommand("-l", commandPakList, "--list");

        // dnt command setup
        CommandDnt commandDnt = new CommandDnt();
        JCommander dntCommand = addCommand(mainCommand, "dnt", commandDnt);

        //dnt subcommand setup
        CommandDnt.Query commandDntQuery = new CommandDnt.Query();
        CommandDnt.Build commandDntBuild = new CommandDnt.Build();
        dntCommand.addCommand("-q", commandDntQuery, "--query");
        dntCommand.addCommand("-b", commandDntBuild, "--build");

        try {
            mainCommand.parse(args);
        } catch (ParameterException | InvalidDdsOutputFormatException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        mainCommand.usage();
    }

    private static JCommander addCommand(JCommander jCommander, String name, Object object) {
        jCommander.addCommand(name, object);

        Map<String, JCommander> commands = jCommander.getCommands();
        return commands.get(name);
    }
}

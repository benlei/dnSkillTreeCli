/*
 * App.java - This file is part of DNCLI
 *
 * Copyright (C) 2015 Benjamin Lei. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * FILE DESCRIPTION:
 * Class that takes in arguments and redirects it to DDS.java, Pak.java, or
 * DNT.java, all of which are part of DNCLI.
 */
package dncli;

import dncli.dds.DDS;
import dncli.dnt.DNT;
import dncli.pak.Pak;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;

import static java.lang.System.err;

public class App {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            mainUsage();
        }

        String cmd = args[0];
        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, subArgs.length);

        CommandLineParser parser = new DefaultParser();
        CommandLine cli;

        if (cmd.equals("pak")) {
            cli = parser.parse(Pak.options, subArgs);
            Pak.use(cli);
        } else if (cmd.equals("dnt")) {
            cli = parser.parse(DNT.options, subArgs);
            DNT.use(cli);
        } else if (cmd.equals("dds")) {
            cli = parser.parse(DDS.options, subArgs);
            DDS.use(cli);
        } else {
            mainUsage();
        }
    }

    public static void mainUsage() {
        err.println("usage: dn <command> [options]");
        err.println("Extract/compacts data from/for DN related resources. Uses JavaScript to allow");
        err.println("for more control over data.\n");
        err.println("Available commands:");
        err.println(" pak            Creates/extracts pak files.");
        err.println(" dnt            Reads data from a .dnt file, and can also create .dnt files.");
        err.println(" dds            Converts .dds files to .png or .jpg image files.");
        System.exit(1);
    }
}

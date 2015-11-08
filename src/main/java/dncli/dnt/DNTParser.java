/*
 * DNTParser.java - This file is part of DNCLI
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
 * Parses through a DNT file and returns a JSObject that contains the columns
 * and its data type, and also every entry in the DNT.
 */
package dncli.dnt;

import dncli.utils.JSUtils;
import jdk.nashorn.api.scripting.JSObject;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Benjamin Lei on 11/1/2015.
 */
public class DNTParser {
    private final File file;

    public DNTParser(File file) {
        this.file = file;
    }

    public JSObject parse() throws Exception {
        JSObject ret = JSUtils.newObject();
        JSObject cols = JSUtils.newObject();
        JSObject entries = JSUtils.newArray();

        ret.setMember("cols", cols);
        ret.setMember("entries", entries);

        if (file.length() < 10) {
            return ret;
        }

        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        FileChannel fileChannel = randomAccessFile.getChannel();
        ByteBuffer buf = fileChannel.map(FileChannel.MapMode.READ_ONLY, 4, file.length() - 4); // it's already flipped
        buf.order(ByteOrder.LITTLE_ENDIAN);

        int numCols = buf.getShort();
        int numRows = buf.getInt();

        // get cols and their type (in order)
        Map<String, String> colsOrdered = new LinkedHashMap<>();
        colsOrdered.put("PrimaryID", "int");
        for (int i = 0; i < numCols; i++) {
            byte[] fieldNameBytes = new byte[buf.getShort()];
            buf.get(fieldNameBytes);
            String fieldName = new String(fieldNameBytes);
            byte type = buf.get();
            fieldName = fieldName.substring(1);
            switch (type) {
                case 1: colsOrdered.put(fieldName, "string"); break;
                case 2: colsOrdered.put(fieldName, "bool"); break;
                case 3: colsOrdered.put(fieldName, "int"); break;
                case 4: colsOrdered.put(fieldName, "float"); break;
                case 5: colsOrdered.put(fieldName, "double"); break; // (really a float...)
                default:
                    throw new RuntimeException("Cannot resolve type id " + type);
            }
        }

        // store cols into the cols map (unordered) for JS
        for (Map.Entry<String, String> col : colsOrdered.entrySet()) {
            cols.setMember(col.getKey(), col.getValue());
        }

        // read each row and put it into entries
        for (int i = 0; i < numRows; i++) {
            JSObject entry = JSUtils.newObject();
            for (Map.Entry<String, String> col : colsOrdered.entrySet()) {
                switch (col.getValue()) {
                    case "string":
                        byte[] bytes = new byte[buf.getShort()];
                        buf.get(bytes);
                        entry.setMember(col.getKey(), new String(bytes));
                        break;
                    case "bool":
                        entry.setMember(col.getKey(), buf.getInt() != 0);
                        break;
                    case "int":
                        entry.setMember(col.getKey(), buf.getInt());
                        break;
                    case "float":
                        entry.setMember(col.getKey(), buf.getFloat());
                        break;
                    case "double":
                        entry.setMember(col.getKey(), (double)buf.getFloat());
                        break;
                }
            }

            JSUtils.push(entries, entry);
        }

        fileChannel.close();
        randomAccessFile.close();
        return ret;
    }
}

/*
 * DNTWriter.java - This file is part of DNCLI
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
 * Creates a DNT file from a given JSObject cols and JSObject entries.
 */
package dncli.dnt;

import dncli.utils.JSUtils;
import jdk.nashorn.api.scripting.JSObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin Lei on 11/2/2015.
 */
public class DNTWriter {
    private final File file;

    public DNTWriter(File file) {
        this.file = file;
    }

    public void write(JSObject cols, JSObject entries) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();
        ByteBuffer buf = ByteBuffer.allocateDirect(Short.MAX_VALUE + 2 + 1);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        cols.removeMember("PrimaryID");

        // padding
        buf.putInt(0);

        // colsxrows
        buf.putShort((short) JSUtils.sizeOf(cols));
        buf.putInt(JSUtils.sizeOf(entries));

        buf.flip();
        fileChannel.write(buf);

        List<String> colsOrdered = new ArrayList<>();

        // ID must be first element
        colsOrdered.add("PrimaryID");

        for (String col : cols.keySet()) {
            buf.clear();

            // make sure length isn't too long
            int colSize = col.length() + 1;
            if (colSize > Short.MAX_VALUE) {
                throw new IllegalArgumentException("_" + col + " exceeds " + Short.MAX_VALUE + " characters");
            }

            colsOrdered.add(col);

            // make sure all col names start with '_'
            buf.putShort((short)colSize);
            buf.put(("_" + col).getBytes());
            switch (cols.getMember(col).toString()) {
                case "string": buf.put((byte) 1); break;
                case "bool":   buf.put((byte) 2); break;
                case "int":    buf.put((byte) 3); break;
                case "float":  buf.put((byte) 4); break;
                case "double": buf.put((byte) 5); break;
                default: // must be float/double
                    throw new IllegalArgumentException(String.format("Column %s contain an invalid type %s (only accept string, bool, int, float, and double as types)",
                            col,
                            cols.getMember(col).toString()));
            }

            buf.flip();
            fileChannel.write(buf);
        }

        int length = JSUtils.sizeOf(entries);
        cols.setMember("PrimaryID", "int"); // add it back
        for (int i = 0; i < length; i++) {
            JSObject entry = (JSObject)entries.getSlot(i);

            for (String col : colsOrdered) {
                buf.clear();
                switch (cols.getMember(col).toString()) {
                    case "string":
                        String str = entry.getMember(col).toString();
                        int len = str.length();
                        if (len > Short.MAX_VALUE) {
                            throw new IllegalArgumentException(str + " exceeds " + Short.MAX_VALUE + " characters");
                        }

                        buf.putShort((short) len);
                        buf.put(str.getBytes());
                        break;
                    case "bool":
                        boolean bool = (Boolean)entry.getMember(col);
                        buf.putInt(bool ? 1 : 0);
                        break;
                    case "int":
                        int val = JSUtils.numberToInt(entry.getMember(col));
                        buf.putInt(val);
                        break;
                    case "float":
                        float f = (Float)entry.getMember(col);
                        buf.putFloat(f);
                        break;
                    case "double":
                        double d = (Double)entry.getMember(col);
                        buf.putFloat((float)d);
                        break;
                }

                buf.flip();
                fileChannel.write(buf);
            }
        }

        fileChannel.close();
        randomAccessFile.close();
    }
}

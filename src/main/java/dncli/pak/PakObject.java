/*
 * PakObject.java - This file is part of DNCLI
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
 * Class used by the PakWriter to create Pak archive.
 */
package dncli.pak;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.Deflater;

/**
 * Created by Benjamin Lei on 11/1/2015.
 */
public class PakObject {
    private final File file;
    private final String path;
    private int position;
    private int compressedSize;
    private int size;

    public PakObject(File file, String path) throws IOException {
        this.file = file;
        if (path.endsWith("*")) {
            this.path = path.substring(0, path.length() - 1);
            size = 0;
        } else {
            this.path = path;
            size = (int)file.length();
        }
    }

    public int getSize() throws IOException {
        return size;
    }

    public String getPath() {
        return path;
    }

    public int getCompressedSize() {
        return compressedSize;
    }

    public void setCompressedSize(int compressedSize) {
        this.compressedSize = compressedSize;
    }

    public byte[] getZData() throws IOException {
        byte[] output = new byte[8192];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);

        deflater.setInput(Files.readAllBytes(file.toPath()));
        deflater.finish();

        int compressed;
        while ((compressed = deflater.deflate(output)) != 0) {
            baos.write(output, 0, compressed);
        }
        deflater.end();

        return baos.toByteArray();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}

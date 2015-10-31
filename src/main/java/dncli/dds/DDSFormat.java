/*
 * DDSPixelFormat.java - This file is part of Java DDS ImageIO Plugin
 *
 * Copyright (C) 2011 Niklas Kyster Rasmussen
 *
 * COPYRIGHT NOTICE:
 * Java DDS ImageIO Plugin is based on code from the DDS GIMP plugin.
 * Copyright (C) 2004-2010 Shawn Kirst <skirst@insightbb.com>,
 * Copyright (C) 2003 Arne Reuter <homepage@arnereuter.de>
 *
 * Java DDS ImageIO Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * Java DDS ImageIO Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Java DDS ImageIO Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * FILE DESCRIPTION:
 * TODO Write File Description for DDSPixelFormat.java
 *
 * CHANGES:
 * - Renamed package.
 * - Extracted Format to DDSFormat
 *
 * ORIGINAL: https://code.google.com/p/java-dds/
 */

package dncli.dds;

public enum DDSFormat {
    NOT_DDS("NOT DDS FORMAT"),
    UNCOMPRESSED("UNCOMPRESSED"),
    DXT1("DXT1"),
    DXT2("DXT2"),
    DXT3("DXT3"),
    DXT4("DXT4"),
    DXT5("DXT5"),
    BC4U("BC4U"),
    BC4S("BC4S"),
    ATI1("ATI1"),
    ATI2("ATI2"),
    BC5S("BC5S"),
    RGBG("RGBG"),
    GRGB("GRGB"),
    UYVY("UYVY"),
    YUY2("YUY2"),
    DX10("DX10"),;

    private String name;
    private final int fourCC;

    DDSFormat(String name) {
        this.name = name;
        this.fourCC = fourCC(name);
    }

    public int getFourCC() {
        return fourCC;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private int fourCC(String cc) {
        int result = 0;
        for (int i = cc.length() - 1; i >= 0; i--) {
            result = (result << 8) + (int) cc.charAt(i);
        }
        return result;
    }
}

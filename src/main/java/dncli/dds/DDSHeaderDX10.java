/*
 * DDSImageReaderTest.java - This file is part of Java DDS ImageIO Plugin
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
 * TODO Write File Description for DDSImageReaderTest.java
 *
 * CHANGES:
 * - Renamed Package
 * - Extracted D3d10ResourceDimension to own file
 * - Extracted DxgiFormat to own file
 *
 * ORIGINAL: https://code.google.com/p/java-dds/
 */
package dncli.dds;

public class DDSHeaderDX10 {
    private final long dxgiFormat;
    private final long resourceDimension;
    private final long miscFlag;
    private final long arraySize;
    private final long miscFlags2;
    private final DDSFormat format;

    public DDSHeaderDX10(long dxgiFormat, long resourceDimension, long miscFlag, long arraySize, long miscFlags2) {
        this.dxgiFormat = dxgiFormat;
        this.resourceDimension = resourceDimension;
        this.miscFlag = miscFlag;
        this.arraySize = arraySize;
        this.miscFlags2 = miscFlags2;
        this.format = calcFormat();
    }

    public DDSFormat getFormat() {
        return format;
    }

    public void print() {
        System.out.println("dxgiFormat: " + dxgiFormat + " (" + DxgiFormat.values()[(int) dxgiFormat].name() + ")");
        System.out.println("resourceDimension: " + resourceDimension + " (" + D3d10ResourceDimension.values()[(int) resourceDimension].name() + ")");
        System.out.println("miscFlag: " + miscFlag);
        System.out.println("arraySize: " + arraySize);
        System.out.println("reserved: " + miscFlags2);
    }

    public long getArraySize() {
        return arraySize;
    }

    private DDSFormat calcFormat() {
        if (dxgiFormat < DxgiFormat.values().length) {
            DDSFormat format = DxgiFormat.values()[(int) dxgiFormat].getFormat();
            if (format == DDSFormat.UNCOMPRESSED) {
                //format.setName(null);
            }
            return format;
        } else {
            return DDSFormat.NOT_DDS;
        }
    }
}
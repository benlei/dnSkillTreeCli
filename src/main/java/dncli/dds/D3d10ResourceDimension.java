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
 *
 * ORIGINAL: https://code.google.com/p/java-dds/
 */
package dncli.dds;

public enum D3d10ResourceDimension {
    D3D10_RESOURCE_DIMENSION_UNKNOWN(0),
    D3D10_RESOURCE_DIMENSION_BUFFER(1),
    D3D10_RESOURCE_DIMENSION_TEXTURE1D(2),
    D3D10_RESOURCE_DIMENSION_TEXTURE2D(3),
    D3D10_RESOURCE_DIMENSION_TEXTURE3D(4);

    private final int value;

    D3d10ResourceDimension(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

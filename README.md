# dncli
CLI for gathering data from Dragon Nest related files.

For Linux: `./gradlew build`, or Windows: `./gradlew.bat build`, and then create an alias that runs the jar file in your `./build/libs`.

Alternatively download the jar file from the release page.

Alternatively use the Docker container instead.

## Prerequisites
Java 8

## Features

DragonNest Pak:
- View information about Pak file(s), including listing all the files in it.
- Extracting Pak file(s), with the ability to provide a JS filter for better control over data.
- Creating a Pak from a directory.
- Inflating a Pak file.

DragonNest DNT:
- Mapping DragonNestTables to MySQL tables.
- Nashorn JavaScript usage to programmatically query these tables for data.

DDS:
- Extracting images from DDS files into compressed PNG/JPG files.

## Usage

Note: The  below assumes you have correctly created an alias/shell script/etc. named "dn" that calls the Java Jar file.

```
$ dn -help
```

The following should parse through all DNT files, pass it into the accumulate function defined in dnt.js, and then finally run a compile() function (also must be defined in dnt.js).

```
$ dn dnt -process -fresh -js ./example/maze.js /path/to/resource/ext/*
```

or on a Windows PowerShell:
```
> dn dnt -process -fresh -js ".\example\maze.js" "D:\path\to\resources\ext\*"
```

Please look up more information about Nashorn in order to better write JavaScript for Java: <http://www.oracle.com/technetwork/articles/java/jf14-nashorn-2126515.html>

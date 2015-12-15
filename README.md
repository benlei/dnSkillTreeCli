# dncli
CLI for gathering data from Dragon Nest related files.

You need do a `mvn package`, and then create an alias that runs the jar file. Or alternatively download the jar file from the release page.

## Prerequisites
Java 8

## Features

- View information about Pak file(s), including listing all the files in it.
- Extracting Pak file(s), with the ability to provide a JS filter for better control over data.
- Creating a Pak from a directory.
- Accumulating DNT files, and then compiling data -- must provide own JavaScript (check the dnss.js example).
- Modification/creation of DNT files.
- Extracting images from DDS files into compressed PNG/JPG files.

## Usage

Note: The  below assumes you have correctly created an alias/shell script/etc. named "dn" that calls the Java Jar.

```
$ dn
usage: dn <command> [options]
Extract/compacts data from/for DN related resources. Uses JavaScript to allow
for more control over data.

Available commands:
 pak            Creates/extracts pak files.
 dnt            Reads data from a .dnt file, and can also create .dnt files.
 dds            Converts .dds files to .png or .jpg image files.
```

The following should parse through all DNT files, pass it into the accumulate function defined in maze.js, and then finally run a compile() function (also must be defined in maze.js).

```
$ dn dnt -c maze.js /path/to/resources/ext/*
```

or on a Windows PowerShell:
```
> dn dnt -c maze.js "D:\dragonnest\resources\ext\*"
```

Please look up more information about Nashorn in order to better write JavaScript for Java: <http://www.oracle.com/technetwork/articles/java/jf14-nashorn-2126515.html>

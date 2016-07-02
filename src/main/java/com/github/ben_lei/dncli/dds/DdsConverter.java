package com.github.ben_lei.dncli.dds;

import com.github.ben_lei.dncli.command.CommandDds;
import com.github.ben_lei.dncli.util.OsUtil;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by blei on 5/29/16.
 */
public class DdsConverter implements Runnable {
    private final CommandDds args;

    public DdsConverter(CommandDds args) {
        this.args = args;
    }

    /**
     * <p>Runs DDS conversion based on the CLI parameters.</p>
     */
    @Override
    public void run() {
        List<File> files = args.getFiles();
        Stream<File> stream = args.isForce() ? files.parallelStream() : files.stream();

        stream.forEach(file -> {
            try {
                ImageReader imageReader = ImageIO.getImageReadersBySuffix("dds").next();
                FileImageInputStream imageInputStream = new FileImageInputStream(file);
                String newFormat = args.getFormat().name();

                BufferedImage image;
                File output;
                int maxImages;

                imageReader.setInput(imageInputStream);
                maxImages = imageReader.getNumImages(true);

                for (int i = 0; i < maxImages; i++) {
                    String prefix = maxImages > 0 ? i + "." : "";

                    output = changeExt(file, prefix + newFormat);
                    image = imageReader.read(i);

                    writeImage(image, output);
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });
    }

    /**
     * <p>Changes the extension of a file</p>
     *
     * @param file   the file
     * @param newExt the new extension to give the file
     * @return the new file with a new extension
     */
    private File changeExt(File file, String newExt) {
        String path = file.getPath();
        int extIndex = path.lastIndexOf('.');
        if (extIndex == -1) {
            path = path + "." + newExt;
        } else {
            path = path.substring(0, extIndex) + "." + newExt;
        }

        return new File(path);
    }

    /**
     * <p>Writes a {@code BufferedImage} to an <strong>output</strong></p>
     *
     * @param image
     * @param output
     * @throws IOException if writing to output fails
     */
    private void writeImage(BufferedImage image, File output) throws IOException {
        if (!args.isForce() && !OsUtil.confirmOverwrite(output)) {
            return;
        }

        ImageIO.write(image, args.getFormat().name(), output);

        if (!args.isQuiet()) {
            System.out.println("Created " + output.getPath());
        }
    }
}

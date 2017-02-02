package com.dnmaze.dncli.dds;

import com.dnmaze.dncli.command.CommandDds;
import com.dnmaze.dncli.util.OsUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;

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
    File outputDir = args.getOutput();

    Objects.requireNonNull(outputDir, "Output directory cannot be null");

    if (!outputDir.exists() && !outputDir.getAbsoluteFile().mkdirs()) {
      throw new RuntimeException("Could not create " + outputDir.getPath());
    }

    stream.forEach(file -> {
      ImageReader imageReader = ImageIO.getImageReadersBySuffix("dds").next();
      file = file.getAbsoluteFile();

      try {
        FileImageInputStream imageInputStream = new FileImageInputStream(file);
        String newFormat = args.getFormat().name();

        BufferedImage image;
        File output;
        int maxImages;

        imageReader.setInput(imageInputStream);
        maxImages = imageReader.getNumImages(true);

        for (int i = 0; i < maxImages; i++) {
          String prefix = maxImages > 1 ? i + "." : "";

          output = changeExt(file, prefix + newFormat);
          image = imageReader.read(i);

          if (args.isFavorDark()) {
            favorDarkColors(image);
          }

          writeImage(image, output);
        }
      } catch (IOException ex) {
        System.err.println(ex.getMessage());
      }
    });
  }

  /**
   * <p>Changes the extension of a file.</p>
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

    File outputDir = args.getOutput();
    File output = new File(path);
    if (outputDir == null) {
      return output;
    } else {
      return new File(outputDir, output.getName());
    }
  }

  /**
   * <p>Writes a {@code BufferedImage} to an <strong>output</strong>.</p>
   *
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

  private void favorDarkColors(BufferedImage image) {
    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        int pixel = image.getRGB(x, y);
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = pixel & 0xff;

        if (red < 200 && green < 200 && blue < 200) {
          red = 0;
          blue = 0;
          green = 0;
        } else {
          red = 255;
          green = 255;
          blue = 255;
        }

        if (alpha < 220) {
          alpha = 0;
        } else {
          alpha = 192;
        }

        pixel = (alpha << 24) | (red << 16) | (green << 8) | blue;

        image.setRGB(x, y, pixel);
      }
    }
  }
}

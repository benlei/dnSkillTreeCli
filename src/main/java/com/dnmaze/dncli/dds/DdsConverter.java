package com.dnmaze.dncli.dds;

import com.dnmaze.dncli.command.CommandDds;
import com.dnmaze.dncli.util.OsUtil;

import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
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

          if (args.isBorder()) {
            image = addDifferentBorderColors(image);
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

  private BufferedImage addDifferentBorderColors(BufferedImage image) {
    int spriteSize = 52;
    int type = image.getType();

    BufferedImage newImage = new BufferedImage(spriteSize, spriteSize * 3, type);
    Graphics newGraphics = newImage.getGraphics();

    // ORIGINAL IMAGE
    newGraphics.drawImage(image,
        0, 0, spriteSize, spriteSize,
        spriteSize * 3, 0, spriteSize * 4, spriteSize,
        null);

    // GRAYSCALE IMAGE
    BufferedImage grayImage = grayscaleImage(image);

    newGraphics.drawImage(grayImage,
        0, spriteSize, spriteSize, spriteSize * 2,
        spriteSize * 3, 0, spriteSize * 4, spriteSize,
        null);


    // CRESTED IMAGE
    BufferedImage crestedImage = tintImage(image, 255, 207, 140);

    newGraphics.drawImage(crestedImage,
        0, spriteSize * 2, spriteSize, spriteSize * 3,
        spriteSize * 3, 0, spriteSize * 4, spriteSize,
        null);

    newGraphics.dispose();

    return newImage;
  }

  private BufferedImage grayscaleImage(BufferedImage image) {
    BufferedImage grayImage = new BufferedImage(
        image.getWidth(),
        image.getHeight(),
        image.getType()
    );

    ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    op.filter(image, grayImage);

    return grayImage;
  }

  private BufferedImage tintImage(BufferedImage image, int red, int green, int blue) {
    BufferedImage tintedSprite = new BufferedImage(
        image.getWidth(),
        image.getHeight(),
        image.getType()
    );

    Graphics graphics = tintedSprite.getGraphics();
    graphics.drawImage(image, 0, 0, null);
    graphics.dispose();

    int width = image.getWidth();
    int height = image.getHeight();

    float redPercent = red / 255.f;
    float greenPercent = green / 255.f;
    float bluePercent = blue / 255.f;

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        ColorModel colorModel = tintedSprite.getColorModel();
        WritableRaster raster = tintedSprite.getRaster();

        int newRed = colorModel.getRed(raster.getDataElements(x, y, null));
        int newGreen = colorModel.getGreen(raster.getDataElements(x, y, null));
        int newBlue = colorModel.getBlue(raster.getDataElements(x, y, null));

        newRed *= redPercent;
        newGreen *= greenPercent;
        newBlue *= bluePercent;

        int alpha = colorModel.getAlpha(raster.getDataElements(x, y, null));

        tintedSprite.setRGB(x, y, (alpha << 24) | (newRed << 16) | (newGreen << 8) | newBlue);
      }
    }
    return tintedSprite;
  }
}

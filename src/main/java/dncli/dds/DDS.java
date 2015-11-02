package dncli.dds;

import dncli.utils.OS;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Created by Benjamin Lei on 10/29/2015.
 */
public class DDS {
    public final static Options options = new Options();
    static {
        options.addOption(Option.builder()
                .longOpt("png")
                .desc("Converts DDS file to a PNG file.")
                .build());

        options.addOption(Option.builder()
                .longOpt("jpg")
                .desc("Converts DDS file to a JPG file.")
                .build());

        options.addOption(Option.builder("p")
                .longOpt("inplace")
                .desc("Converts DDS files in place, converting image.dds to image.png or image.jpg")
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Shows this usage message.")
                .build());

        options.addOption(Option.builder("q")
                .longOpt("quiet")
                .desc("Quiet output")
                .build());

        options.addOption(Option.builder("f")
                .longOpt("force")
                .desc("Forces overwriting of destination file without confirmation.")
                .build());
    }

    public static void checkUsage(CommandLine cli) throws Exception {
        int numArgs = cli.getArgList().size();
        boolean isPng = cli.hasOption("png");
        boolean isJpg = cli.hasOption("jpg");
        if (cli.hasOption("help") ||
                ! (isPng ^ isJpg) ||
                numArgs == 0) {
            OS.usage("dds", "[file]...",
                    "Converts a DDS file to a PNG/JPG file. Cannot specify both PNG and PNG format.\n\n" +
                            "Available options:",
                    options);
        }
    }

    private static File changeExt(File file, String newExt) {
        String path = file.getPath();
        int extIndex = path.lastIndexOf('.');
        if (extIndex == -1) {
            path = path + "." + newExt;
        } else {
            path = path.substring(0, extIndex) + "." + newExt;
        }

        return new File(path);
    }

    private static ImageReader getDDSImageReader(File file) throws Exception {
        ImageReader imageReader = ImageIO.getImageReadersBySuffix("dds").next();
        FileImageInputStream imageInputStream = new FileImageInputStream(file);
        imageReader.setInput(imageInputStream);
        return imageReader;
    }

    private static void writeImage(BufferedImage image, String ext, File output, boolean force) throws Exception{
        if (! force && ! OS.confirmOverwrite(output)) {
            return;
        }

        ImageIO.write(image, ext, output);
    }

    public static void use(CommandLine cli) throws Exception{
        checkUsage(cli);

        // gets remaining arguments that could not be parsed
        List<String> args = cli.getArgList();

        boolean force = cli.hasOption("force");
        boolean inplace = cli.hasOption("inplace");
        boolean quiet = cli.hasOption("quiet");

        String ext = "png";
        if (cli.hasOption("jpg")) {
            ext = "jpg";
        }


        if (inplace) {
            for (String filePath : args) {
                File ddsFile = new File(filePath);
                ImageReader imageReader = getDDSImageReader(ddsFile);

                // make the jpg/png file
                File output = changeExt(ddsFile, ext);

                // warn if dds has multiple images
                int maxImages = imageReader.getNumImages(true);
                if (maxImages > 1 && ! quiet) {
                    System.out.println(filePath + " has " + maxImages + ", but only first will be extracted.");
                }

                // output first image in dds
                BufferedImage image = imageReader.read(0);
                writeImage(image, ext, output, force);
                if (! quiet) {
                    System.out.println("Converted " + ddsFile.getPath() + " to " + output.getPath());
                }
            }
        } else {
            File ddsFile = new File(args.remove(0));
            ImageReader imageReader = getDDSImageReader(ddsFile);

            // make sure there are enough args for files to output
            int maxImages = imageReader.getNumImages(true);
            int totalOutputs = args.size();
            if (maxImages == 1 && totalOutputs == 0) {
                args.add(changeExt(ddsFile, ext).getPath());
                totalOutputs = 1;
            }

            if (totalOutputs != maxImages) {
                throw new MissingArgumentException(String.format("ERROR: DDS contains %d images, but expecting %d outputs!", maxImages, totalOutputs));
            }

            // output all images in dds
            for (int i = 0; i < maxImages; i++) {
                BufferedImage image = imageReader.read(i);
                File outputFile = new File(args.get(i));
                writeImage(image, ext, outputFile, force);
                if (! quiet) {
                    System.out.println("Converted " + ddsFile.getPath() + " to " + outputFile.getPath());
                }
            }
        }
    }
}

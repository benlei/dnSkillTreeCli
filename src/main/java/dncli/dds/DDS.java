package dncli.dds;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Scanner;

import static java.lang.System.err;
import static java.lang.System.out;

/**
 * Created by Benjamin Lei on 10/29/2015.
 */
public class DDS {
    public final static Options options = new Options();
    static {
        options.addOption(Option.builder()
                .longOpt("png")
                .hasArg()
                .desc("Converts DDS file to a PNG file.")
                .build());

        options.addOption(Option.builder()
                .longOpt("jpg")
                .hasArg()
                .desc("Converts DDS file to a JPG file.")
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Shows this usage message.")
                .build());

        options.addOption(Option.builder("f")
                .longOpt("force")
                .desc("Forces overwriting of destination file without confirmation.")
                .build());
    }

    public static void perform(CommandLine cli) throws Exception{
        // gets remaining arguments that could not be parsed
        List<String> outputs = cli.getArgList();
        Scanner scanner = null;
        if (cli.hasOption("f")) {
            scanner = new Scanner(System.in);
        }

        String ext = "png";
        if (cli.hasOption("jpg")) {
            ext = "jpg";
        }

        String file = cli.getOptionValue("png");
        if (file == null) {
            file = cli.getOptionValue("jpg");
        }

        File ddsFile = new File(file);
        ImageReader imageReader = ImageIO.getImageReadersBySuffix("dds").next();
        FileImageInputStream imageInputStream = new FileImageInputStream(ddsFile);
        imageReader.setInput(imageInputStream);
        int maxImages = imageReader.getNumImages(true);

        int totalOutputs = outputs.size();
        if (maxImages == 1 && totalOutputs == 0) {
            String outputFile;
            outputFile = ddsFile.getPath();
            int extIndex = outputFile.lastIndexOf('.');
            if (extIndex == -1) {
                outputFile = outputFile + "." + ext;
            } else {
                outputFile = outputFile.substring(0, extIndex) + "." + ext;
            }
            outputs.add(outputFile);
            totalOutputs = 1;
        }

        if (totalOutputs != maxImages) {
            err.println(String.format("ERROR: DDS contains %d images, but expecting %d outputs!", maxImages, totalOutputs));
            System.exit(1);
        }

        for (int i = 0; i < maxImages; i++) {
            BufferedImage image = imageReader.read(i);
            File outputFile = new File(outputs.get(i));
            if (outputFile.exists() && scanner != null) {
                out.print("Enter [Y/n] if you want to overwrite " + outputFile.getPath() + ": ");
                String confirm = scanner.next();
                if (confirm != null && ! confirm.equals("Y")) {
                    continue;
                }
            }

            ImageIO.write(image, ext, outputFile);
        }
    }
}

package dncli.pak;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.Deflater;

/**
 * Created by Benjamin Lei on 11/1/2015.
 */
public class PakObject {
    private final File file;
    private final String path;
    private int position;
    private int compressedSize;
    private int size;

    public PakObject(File file, String path) throws IOException {
        this.file = file;
        if (path.endsWith("*")) {
            this.path = path.substring(0, path.length() - 1);
            size = 0;
        } else {
            this.path = path;
            size = (int)file.length();
        }
    }

    public int getSize() throws IOException {
        return size;
    }

    public String getPath() {
        return path;
    }

    public int getCompressedSize() {
        return compressedSize;
    }

    public void setCompressedSize(int compressedSize) {
        this.compressedSize = compressedSize;
    }

    public byte[] getZData() throws IOException {
        byte[] output = new byte[8192];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);

        deflater.setInput(Files.readAllBytes(file.toPath()));
        deflater.finish();

        int compressed;
        while ((compressed = deflater.deflate(output)) != 0) {
            baos.write(output, 0, compressed);
        }
        deflater.end();

        return baos.toByteArray();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}

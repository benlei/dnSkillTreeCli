package com.github.ben_lei.dncli.pak.archive;

import com.github.ben_lei.dncli.util.CompressUtil;
import com.github.ben_lei.dncli.util.OsUtil;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.zip.DataFormatException;

/**
 * Created by blei on 6/19/16.
 */
public class PakFile {
    private static final int PATH_BYTES = 256 + 4; // not sure what the +4 is for...
    private static final int SIZE_BYTES = 4;
    private static final int COMPRESSED_SIZE_BYTES = 4;
    private static final int DATA_POSITION_BYTES = 4;
    private static final int PADDING_BYTES = 44;
    private static final int META_BYTES = PATH_BYTES + SIZE_BYTES + COMPRESSED_SIZE_BYTES + DATA_POSITION_BYTES + PADDING_BYTES;

    private final File pakFile;

    private String path;
    private int size;
    private int compressedSize;
    private long dataPosition;

    /**
     * <p>The originating pak archive this pak file is from</p>
     *
     * @param pakFile the pak archive
     */
    public PakFile(File pakFile) {
        this.pakFile = pakFile;
    }

    /**
     * <p>Loads up a file meta information in a pak archive, given a
     * start position, and a frame number</p>
     *
     * @param file          the file
     * @param startPosition the start position
     * @param frame         the frame number
     * @return the pak file
     * @throws IOException if cannot open/read file
     */
    public static PakFile load(File file, long startPosition, int frame) throws IOException {
        PakFile thiz = new PakFile(file);
        ByteBuffer buffer = ByteBuffer.allocate(META_BYTES);
        FileChannel fileChannel = null;
        byte[] pathBytes = new byte[PATH_BYTES];

        buffer.order(ByteOrder.LITTLE_ENDIAN);
        try {
            fileChannel = FileChannel.open(file.toPath());
            fileChannel.position(startPosition + META_BYTES * frame);
            fileChannel.read(buffer);

            // flip the buffer to read from it
            buffer.flip();
            buffer.get(pathBytes);
            thiz.path = new String(pathBytes);

            // path ends with '\0'
            thiz.path = thiz.path.substring(0, thiz.path.indexOf('\0')).trim();
            thiz.size = buffer.getInt();
            thiz.compressedSize = buffer.getInt();
            thiz.dataPosition = buffer.getInt() & 0x00000000FFFFFFFL;

            return thiz;
        } finally {
            IOUtils.closeQuietly(fileChannel);
        }
    }

    /**
     * <p>Gets the originating pak archive.</p>
     *
     * @return the pak archive
     */
    public File getPakFile() {
        return pakFile;
    }

    /**
     * <p>Gets the path of this pak file</p>
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * <p>Gets the size of this pak file</p>
     *
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * <p>Gets the compressed size of this pak file</p>
     *
     * @return the compressed size
     */
    public int getCompressedSize() {
        return compressedSize;
    }

    /**
     * <p>The data position of the pak file</p>
     *
     * @return the data position
     */
    public long getDataPosition() {
        return dataPosition;
    }

    public void extractTo(File outputDir) throws IOException, DataFormatException {
        String absolutePath = OsUtil.isWindows() ? path : path.replace('\\', '/');
        File absoluteFile = new File(outputDir, absolutePath);
        File absoluteDir = absoluteFile.getParentFile();

        // create all directories
        absoluteDir.mkdirs();

        try (FileOutputStream out = new FileOutputStream(absoluteFile)) {
            out.write(CompressUtil.decompress(pakFile, (int)dataPosition, compressedSize, size));
        }
    }

    /**
     * <p>Returns a string representation of the pak file.</p>
     *
     * @return the string representation of the pak file
     */
    @Override
    public String toString() {
        return "PakFile{" +
            "path='" + path + '\'' +
            ", size=" + size +
            ", compressedSize=" + compressedSize +
            ", dataPosition=" + dataPosition +
            ", pakFile=" + pakFile +
            '}';
    }
}

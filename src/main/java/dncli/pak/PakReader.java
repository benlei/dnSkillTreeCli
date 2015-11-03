package dncli.pak;

import dncli.utils.JSUtils;
import jdk.nashorn.api.scripting.JSObject;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * Created by Benjamin Lei on 10/29/2015.
 */
public class PakReader {
    private final boolean readData;

    private final RandomAccessFile randomAccessFile;
    private final ByteBuffer buf = ByteBuffer.allocateDirect(Pak.META_SIZE * 64); // 19.75 KB
    private final FileChannel fileChannel;

    private int size = 0;
    private int index = 0;

    public PakReader(File file) throws IOException {
        this(file, true);
    }

    public PakReader(File file, boolean readData) throws IOException {
        this.readData = readData;

        randomAccessFile = new RandomAccessFile(file, "r");
        fileChannel = randomAccessFile.getChannel();

        buf.order(LITTLE_ENDIAN);
        init();
    }

    private void init() throws IOException {
        byte[] headerBytes = new byte[Pak.HEADER.length()];
        ByteBuffer headerBuffer = ByteBuffer.wrap(headerBytes);
        ByteBuffer words = ByteBuffer.allocate(8);
        words.order(LITTLE_ENDIAN);

        // check the header
        fileChannel.read(headerBuffer);
        if (! Arrays.equals(headerBytes, Pak.HEADER.getBytes())) {
            throw new IOException("Invalid Pak file header: " + new String(headerBytes));
        }

        // Sets where we start
        fileChannel.position(Pak.META_START);
        fileChannel.read(words);

        // gets # of files and start offset
        words.flip(); // read it
        size = words.getInt();
        fileChannel.position(words.getInt());

        buf.position(buf.limit());
    }

    public int getSize() {
        return size;
    }

    public JSObject read() throws Exception {
        if (index >= size) {
            IOUtils.closeQuietly(fileChannel);
            IOUtils.closeQuietly(randomAccessFile);
            return null;
        }

        String path;
        byte[] pathBytes = new byte[Pak.PATH_SIZE];
        JSObject map = JSUtils.newObject();
        int compressedSize;
        int position;

        // fill with data
        if (! buf.hasRemaining()) {
            buf.clear();
            fileChannel.read(buf);
            buf.flip(); // make it readable
        }

        try {
            buf.get(pathBytes);
            buf.position(buf.position() + 4); // skip 4 bytes
            map.setMember("size", buf.getInt());
            compressedSize = buf.getInt();
            position = buf.getInt();
            buf.position(buf.position() + 44); // 44 padding bytes

            // fix the path
            path = new String(pathBytes);
            path = path.substring(0, path.indexOf('\0')).trim();
            map.setMember("path", path);
            map.setMember("zsize", compressedSize);
            map.setMember("position", position);
            map.setMember("index", index);

            // read compressed contents from pak
            if (readData) {
                long currentPosition = fileChannel.position();
                byte[] data = new byte[compressedSize];
                ByteBuffer dataBuffer = ByteBuffer.wrap(data);

                fileChannel.position(position);
                fileChannel.read(dataBuffer);
                fileChannel.position(currentPosition);
                map.setMember("data", data);
            }

            ++index;
            if (index == size) {
                read(); // do to close IO
            }
            return map;
        } catch (IOException e) {
            index = size; // this should stop working now
            return read(); // will close the relevant IO
        }
    }
}

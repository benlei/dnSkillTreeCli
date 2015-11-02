package dncli.pak;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Created by Benjamin Lei on 10/29/2015.
 */
public class PakWriter {
    private final int STREAM_OFFSET_START = Pak.META_START + 8;

    private final File file;
    private final int min;
    private final ArrayList<PakObject> prepObjects = new ArrayList<>();

    public PakWriter(File file, int min) {
        this.file = file;
        this.min = min;
    }

    public void write(File file, String path) throws IOException {
        if (! file.exists()) {
            throw new FileNotFoundException(file.getPath() + " does not exist!");
        }

        PakObject object = new PakObject(file, path);
        prepObjects.add(object);
    }

    public void flush() throws Exception {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();
        ArrayList<PakObject> objects = (ArrayList<PakObject>)prepObjects.clone();
        ByteBuffer buf;
        int largestSize = Pak.META_SIZE;

        // set the location of when the data of the objects starts.
        int dataOffset = Math.addExact(STREAM_OFFSET_START, Math.multiplyExact(Pak.META_SIZE, objects.size()));

        // record the compressed size, and of the max bytes necessary for buffer
        for (PakObject object : objects) {
            byte[] zData = object.getZData();
            object.setPosition(dataOffset);
            dataOffset = Math.addExact(dataOffset, zData.length);
            object.setCompressedSize(zData.length);

            if (largestSize < zData.length) {
                largestSize = zData.length;
            }
        }

        // create buffer + set to LE
        buf = ByteBuffer.allocateDirect(largestSize);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        // write pak header (auto flipped)
        buf.put(Pak.HEADER.getBytes()); // header
        buf.put(new byte[Pak.META_START - Pak.HEADER.length()]); // padding
        buf.flip();
        fileChannel.write(buf);

        // write total files + start of meta
        buf.clear();
        buf.putInt(objects.size());
        buf.putInt(STREAM_OFFSET_START);
        buf.flip();
        fileChannel.write(buf);

        // write meta
        for (PakObject object : objects) {
            String path = object.getPath();
            buf.clear();
            buf.put(path.getBytes());
            buf.put(new byte[Pak.PATH_SIZE - path.length()]); // fill rest with null terminating chars
            buf.putInt(0); // useless
            buf.putInt(object.getSize());
            buf.putInt(object.getCompressedSize());
            buf.putInt(object.getPosition());
            buf.put(new byte[44]); // 44 byte padding for some reason
            buf.flip();
            fileChannel.write(buf);
        }

        // write data
        for (PakObject object : objects) {
            buf.clear();
            buf.put(object.getZData());
            buf.flip();
            fileChannel.write(buf);
        }

        // add padding
        if (dataOffset < min) {
            fileChannel.write(ByteBuffer.allocate(min - dataOffset));
        }

        fileChannel.close();
        randomAccessFile.close();
    }
}

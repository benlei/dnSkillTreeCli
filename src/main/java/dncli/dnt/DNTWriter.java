package dncli.dnt;

import dncli.utils.JSUtils;
import jdk.nashorn.api.scripting.JSObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin Lei on 11/2/2015.
 */
public class DNTWriter {
    private final File file;

    public DNTWriter(File file) {
        this.file = file;
    }

    public void write(JSObject cols, JSObject entries) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();
        ByteBuffer buf = ByteBuffer.allocateDirect(Short.MAX_VALUE + 2 + 1);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        cols.removeMember("ID");

        // padding
        buf.putInt(0);

        // colsxrows
        buf.putShort((short) JSUtils.sizeOf(cols));
        buf.putInt(JSUtils.sizeOf(entries));

        buf.flip();
        fileChannel.write(buf);

        List<String> colsOrdered = new ArrayList<>();

        // ID must be first element
        colsOrdered.add("ID");

        for (String col : cols.keySet()) {
            buf.clear();

            // make sure length isn't too long
            int colSize = col.length() + 1;
            if (colSize > Short.MAX_VALUE) {
                throw new IllegalArgumentException("_" + col + " exceeds " + Short.MAX_VALUE + " characters");
            }

            colsOrdered.add(col);

            // make sure all col names start with '_'
            buf.putShort((short)colSize);
            buf.put(("_" + col).getBytes());
            switch (cols.getMember(col).toString()) {
                case "string": buf.put((byte) 1); break;
                case "bool":   buf.put((byte) 2); break;
                case "int":    buf.put((byte) 3); break;
                case "float":  buf.put((byte) 4); break;
                case "double": buf.put((byte) 5); break;
                default: // must be float/double
                    throw new IllegalArgumentException(String.format("Column %s contain an invalid type %s",
                            col,
                            cols.getMember(col).toString()));
            }

            buf.flip();
            fileChannel.write(buf);
        }

        int length = JSUtils.sizeOf(entries);
        for (int i = 0; i < length; i++) {
            JSObject entry = (JSObject)entries.getSlot(i);
            for (String col : colsOrdered) {
                buf.clear();
                switch (cols.getMember(col).toString()) {
                    case "string":
                        String str = entry.getMember(col).toString();
                        int len = str.length();
                        if (len > Short.MAX_VALUE) {
                            throw new IllegalArgumentException(str + " exceeds " + Short.MAX_VALUE + " characters");
                        }
                        buf.putShort((short) len);
                        buf.put(str.getBytes());
                        break;
                    case "bool":
                        boolean bool = (Boolean)entry.getMember(col);
                        buf.putInt(bool ? 1 : 0);
                        break;
                    case "int":
                        int val = (Integer)entry.getMember(col);
                        buf.putInt(val);
                        break;
                    case "float":
                        float f = (Float)entry.getMember(col);
                        buf.putFloat(f);
                        break;
                    case "double":
                        double d = (Double)entry.getMember(col);
                        buf.putFloat((float)d);
                        break;
                }

                buf.flip();
                fileChannel.write(buf);
            }
        }

        fileChannel.close();
        randomAccessFile.close();
    }
}

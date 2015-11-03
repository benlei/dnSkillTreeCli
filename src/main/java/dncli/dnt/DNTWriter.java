package dncli.dnt;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Benjamin Lei on 11/2/2015.
 */
public class DNTWriter {
    private final File file;

    public DNTWriter(File file) {
        this.file = file;
    }

    public void write(Map<String, String> cols, List<Map<String, Object>> entries) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();
        ByteBuffer buf = ByteBuffer.allocateDirect(Short.MAX_VALUE + 2 + 1);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        cols.remove("ID");

        // padding
        buf.putInt(0);

        // colsxrows
        buf.putShort((short)cols.size());
        buf.putInt(entries.size());

        buf.flip();
        fileChannel.write(buf);

        List<String> colsOrdered = new ArrayList<>();
        // ID must be first element
        colsOrdered.add("ID");
        for (Map.Entry<String, String> col : cols.entrySet()) {
            buf.clear();
            String colName = col.getKey();

            // make sure length isn't too long
            int colSize = colName.length() + 1;
            if (colSize > Short.MAX_VALUE) {
                throw new IllegalArgumentException("_" + colName + " exceeds " + Short.MAX_VALUE + " characters");
            }

            colsOrdered.add(colName);

            // make sure all col names start with '_'
            buf.putShort((short)colSize);
            buf.put(("_" + colName).getBytes());
            switch (col.getValue()) {
                case "string": buf.put((byte) 1); break;
                case "bool":   buf.put((byte) 2); break;
                case "int":    buf.put((byte) 3); break;
                case "float":  buf.put((byte) 4); break;
                case "double": buf.put((byte) 5); break;
                default: // must be float/double
                    throw new IllegalArgumentException(String.format("Column %s contain an invalid type %s",
                            col.getKey(),
                            col.getValue()));
            }

            buf.flip();
            fileChannel.write(buf);
        }

        for (Map<String, Object> entry : entries) {
            for (String col : colsOrdered) {
                buf.clear();
                switch (cols.get(col)) {
                    case "string":
                        String str = entry.get(col).toString();
                        int len = str.length();
                        if (len > Short.MAX_VALUE) {
                            throw new IllegalArgumentException(str + " exceeds " + Short.MAX_VALUE + " characters");
                        }
                        buf.putShort((short) len);
                        buf.put(str.getBytes());
                        break;
                    case "bool":
                        boolean bool = (Boolean)entry.get(col);
                        buf.putInt(bool ? 1 : 0);
                        break;
                    case "int":
                        int val = (Integer)entry.get(col);
                        buf.putInt(val);
                        break;
                    case "float":
                        float f = (Float)entry.get(col);
                        buf.putFloat(f);
                        break;
                    case "double":
                        double d = (Double)entry.get(col);
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

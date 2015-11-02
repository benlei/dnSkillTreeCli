package dncli.dnt;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Benjamin Lei on 11/1/2015.
 */
public class DNTParser {
    private final File file;

    public DNTParser(File file) {
        this.file = file;
    }

    public Map<String, Object> parse() throws IOException {
        HashMap<String, Object> ret = new HashMap<>();
        HashMap<String, String> cols = new HashMap<>();
        ArrayList<HashMap<String, Object>> entries = new ArrayList<>();
        ret.put("cols", cols);
        ret.put("entries", entries);

        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        FileChannel fileChannel = randomAccessFile.getChannel();
        ByteBuffer buf = fileChannel.map(FileChannel.MapMode.READ_ONLY, 4, file.length() - 4); // it's already flipped
        buf.order(ByteOrder.LITTLE_ENDIAN);

        int numCols = buf.getShort();
        int numRows = buf.getInt();

        // get cols and their type (in order)
        Map<String, String> colsOrdered = new LinkedHashMap<>();
        colsOrdered.put("ID", "int");
        for (int i = 0; i < numCols; i++) {
            byte[] fieldNameBytes = new byte[buf.getShort()];
            buf.get(fieldNameBytes);
            String fieldName = new String(fieldNameBytes);
            byte type = buf.get();

            fieldName = fieldName.substring(1);
            switch (type) {
                case 1: colsOrdered.put(fieldName, "string"); break;
                case 2: colsOrdered.put(fieldName, "bool"); break;
                case 3: colsOrdered.put(fieldName, "int"); break;
                case 4: colsOrdered.put(fieldName, "float"); break;
                case 5: colsOrdered.put(fieldName, "double"); break; // (really a float...)
                default:
                    throw new RuntimeException("Cannot resolve type id " + type);
            }
        }

        // store cols into the cols map (unordered) for JS
        for (Map.Entry<String, String> col : colsOrdered.entrySet()) {
            cols.put(col.getKey(), col.getValue());
        }

        // read each row and put it into entries
        for (int i = 0; i < numRows; i++) {
            HashMap<String, Object> entry = new HashMap<>();
            for (Map.Entry<String, String> col : colsOrdered.entrySet()) {
                switch (col.getValue()) {
                    case "string":
                        byte[] bytes = new byte[buf.getShort()];
                        buf.get(bytes);
                        entry.put(col.getKey(), new String(bytes));
                        break;
                    case "bool":
                        entry.put(col.getKey(), buf.getInt() != 0);
                        break;
                    case "int":
                        entry.put(col.getKey(), buf.getInt());
                        break;
                    default: // must be float/double
                        entry.put(col.getKey(), buf.getFloat());
                        break;
                }
            }

            entries.add(entry);
        }

        fileChannel.close();
        randomAccessFile.close();
        return ret;
    }
}

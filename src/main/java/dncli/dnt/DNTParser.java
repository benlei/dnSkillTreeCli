package dncli.dnt;

import dncli.utils.JSUtils;
import jdk.nashorn.api.scripting.JSObject;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
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

    public JSObject parse() throws Exception {
        JSObject ret = JSUtils.newObject();
        JSObject cols = JSUtils.newObject();
        JSObject entries = JSUtils.newArray();

        ret.setMember("cols", cols);
        ret.setMember("entries", entries);

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
            cols.setMember(col.getKey(), col.getValue());
        }

        // read each row and put it into entries
        for (int i = 0; i < numRows; i++) {
            JSObject entry = JSUtils.newObject();
            for (Map.Entry<String, String> col : colsOrdered.entrySet()) {
                switch (col.getValue()) {
                    case "string":
                        byte[] bytes = new byte[buf.getShort()];
                        buf.get(bytes);
                        entry.setMember(col.getKey(), new String(bytes));
                        break;
                    case "bool":
                        entry.setMember(col.getKey(), buf.getInt() != 0);
                        break;
                    case "int":
                        entry.setMember(col.getKey(), buf.getInt());
                        break;
                    case "float":
                        entry.setMember(col.getKey(), buf.getFloat());
                        break;
                    case "double":
                        entry.setMember(col.getKey(), (double)buf.getFloat());
                        break;
                }
            }

            JSUtils.push(entries, entry);
        }

        fileChannel.close();
        randomAccessFile.close();
        return ret;
    }
}

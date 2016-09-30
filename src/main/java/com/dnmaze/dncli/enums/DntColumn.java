package com.dnmaze.dncli.enums;

import lombok.Getter;
import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

/**
 * Created by blei on 9/28/16.
 */
public enum DntColumn {
  STRING("VARCHAR(255)", 1),
  BOOLEAN("BOOLEAN", 2),
  INTEGER("INTEGER", 3),
  FLOAT("FLOAT", 4, 5);

  @Getter
  private final String definition;
  private final List<Integer> definitionValues;

  DntColumn(String definition, Integer... definitionValues) {
    this.definition = definition;
    this.definitionValues = Arrays.asList(definitionValues);
  }

  /** Creates a DntColumn from a byte value. */
  public static DntColumn from(byte definitionValue) {
    int value = Byte.toUnsignedInt(definitionValue);

    for (DntColumn column : values()) {
      if (column.definitionValues.contains(value)) {
        return column;
      }
    }

    return null;
  }

  /** Helper method to set value in a prepared statement. */
  @SneakyThrows
  public void setValue(PreparedStatement stmt, int parameterIndex, Object obj) {
    switch (this) {
      case STRING:
        stmt.setString(parameterIndex, (String) obj);
        break;
      case BOOLEAN:
        stmt.setBoolean(parameterIndex, (Boolean) obj);
        break;
      case INTEGER:
        stmt.setInt(parameterIndex, (Integer) obj);
        break;
      case FLOAT:
        stmt.setFloat(parameterIndex, (Float) obj);
        break;
      default:
        throw new RuntimeException("Invalid DntColumn: " + this);
    }
  }

  /** Reads a value from the buffer based on this type. */
  public Object readBuffer(ByteBuffer buf) {
    switch (this) {
      case STRING:
        byte[] bytes = new byte[buf.getShort()];
        buf.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
      case BOOLEAN:
        return buf.getInt() != 0;
      case INTEGER:
        return buf.getInt();
      case FLOAT:
        return buf.getFloat();
      default:
        throw new RuntimeException("Invalid DntColumn: " + this);
    }
  }
}

package com.dnmaze.dncli.enums;

import lombok.Getter;
import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;

/**
 * Created by blei on 9/28/16.
 */
public enum DntColumn {
  TEXT("TEXT"),
  BOOLEAN("BOOLEAN"),
  INTEGER("INTEGER"),
  FLOAT("FLOAT");

  @Getter
  private final String definition;

  DntColumn(String definition) {
    this.definition = definition;
  }

  /** Helper method to set value in a prepared statement. */
  @SneakyThrows
  public void setValue(PreparedStatement stmt, int parameterIndex, Object obj) {
    switch (this) {
      case TEXT:
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
      case TEXT:
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

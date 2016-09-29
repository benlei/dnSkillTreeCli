package com.dnmaze.dncli.enums;

import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by blei on 9/28/16.
 */
public enum DntColumn {
  STRING("VARCHAR(255)", "setString", 1),
  BOOLEAN("BOOLEAN", "setBoolean", 2),
  INTEGER("INTEGER", "setInt", 3),
  FLOAT("FLOAT", "setFloat", 4, 5);

  @Getter
  private final String definition;

  private final List<Integer> definitionValues;

  private Method method;

  DntColumn(String definition, String methodName, Integer... definitionValues) {
    this.definition = definition;
    this.definitionValues = Arrays.asList(definitionValues);

    for (Method method : PreparedStatement.class.getMethods()) {
      if (method.getName().equals(methodName)) {
        this.method = method;
      }
    }

    // technically should never happen, but just in case...
    Objects.requireNonNull(this.method);
  }

  public static DntColumn from(byte definitionValue) {
    int value = Byte.toUnsignedInt(definitionValue);

    for (DntColumn column : values()) {
      if (column.definitionValues.contains(value)) {
        return column;
      }
    }

    return null;
  }

  @SneakyThrows
  public void setValue(PreparedStatement stmt, int parameterIndex, Object x) {
    method.invoke(stmt, parameterIndex, x);
  }
}

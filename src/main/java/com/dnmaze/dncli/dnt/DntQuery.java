package com.dnmaze.dncli.dnt;

import static com.dnmaze.dncli.enums.DntColumn.BOOLEAN;
import static com.dnmaze.dncli.enums.DntColumn.FLOAT;
import static com.dnmaze.dncli.enums.DntColumn.INTEGER;
import static com.dnmaze.dncli.enums.DntColumn.STRING;

import com.dnmaze.dncli.command.CommandDnt;
import com.dnmaze.dncli.enums.DntColumn;
import com.dnmaze.dncli.util.JsUtil;
import com.dnmaze.dncli.util.ResourceUtil;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.script.Invocable;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by blei on 6/19/16.
 */
public class DntQuery implements Runnable {
  private static final int COMMIT_COUNT = 1000;
  private final CommandDnt.Query args;
  private Dnt dnt;
  private Set<String> deleted = new HashSet<>();
  private int commitCount;

  public DntQuery(CommandDnt.Query args) {
    this.args = args;
  }

  @Override
  public void run() {
    try {
      Invocable js = JsUtil.compileAndEval(args.getJsFile());
      dnt = js.getInterface(Dnt.class);
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage());
    }

    File messageFile = args.getMessageFile();
    if (messageFile != null) {
      createMessageTable(messageFile);
    }

    args.getInputs().forEach(this::createTables);

    dnt.complete();
  }

  private Pair<String, String> getNameIdPair(File file) {
    if (file.length() < 10) {
      throw new RuntimeException(file.getPath() + " is not a valid DNT file.");
    }

    String tableName = StringUtils.capitalize(dnt.normalizeName(stripExtension(file)));

    if (tableName == null) {
      String stripped = StringUtils.capitalize(stripExtension(file));
      System.err.println(file.getPath() + " has no normalized name, using '" + stripped + "'");
      tableName = stripped;
    }

    if (!StringUtils.isAlphanumeric(tableName)) {
      throw new RuntimeException(tableName + " is not alphanumeric!");
    }

    return Pair.of(tableName, "_" + tableName + "ID");
  }

  @SuppressFBWarnings
      ({
          "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE",
          "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING"
      })
  @SneakyThrows
  private void createMessageTable(File file) {
    Pair<String, String> nameIdPair = getNameIdPair(file);

    String createTableQuery = String.format(ResourceUtil.read("/dntMessageInit.sql"),
        nameIdPair.getKey(), nameIdPair.getValue());

    @Cleanup Connection connection = dnt.getConnection();
    connection.setAutoCommit(false);

    if (args.isFresh()) {
      deleteTableOnce(connection, nameIdPair.getKey());
    }

    Statement stmt = connection.createStatement();
    stmt.execute(createTableQuery);

    PreparedStatement pstmt =
        connection.prepareStatement(String.format("INSERT INTO %s (%s, message) VALUES (?, ?)",
            nameIdPair.getKey(), nameIdPair.getValue()));

    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
    document.getDocumentElement().normalize();
    NodeList nodes = document.getElementsByTagName("message");
    int nodesLength = nodes.getLength();

    for (int i = 0; i < nodesLength; i++) {
      Element element = (Element)nodes.item(i);
      int mid = Integer.parseInt(element.getAttribute("mid"));
      String data = ((CharacterData)element.getFirstChild()).getData();

      pstmt.setInt(1, mid);
      pstmt.setString(2, data);

      pstmt.execute();

      maybeCommit(connection);
    }

    finalCommit(connection);

    pstmt.close();
    stmt.close();
  }

  @SuppressFBWarnings
      ({
          "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING",
          "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE"
      })
  @SneakyThrows
  private void createTables(File file) {
    Pair<String, String> nameIdPair = getNameIdPair(file);

    FileChannel fileChannel = FileChannel.open(file.toPath());
    ByteBuffer buf = fileChannel.map(FileChannel.MapMode.READ_ONLY, 4, file.length() - 4);

    buf.order(ByteOrder.LITTLE_ENDIAN);

    final int numCols = buf.getShort();
    final int numRows = buf.getInt();

    // get cols and their type (in order)
    List<String> columnNames = new ArrayList<>();
    List<DntColumn> dntColumns = new ArrayList<>();

    for (int i = 0; i < numCols; i++) {
      byte[] fieldNameBytes = new byte[buf.getShort()];
      buf.get(fieldNameBytes);
      String fieldName = new String(fieldNameBytes, StandardCharsets.UTF_8);
      byte type = buf.get();

      columnNames.add(fieldName);

      switch (type) {
        case 1:
          dntColumns.add(STRING);
          break;
        case 2:
          dntColumns.add(BOOLEAN);
          break;
        case 3:
          dntColumns.add(INTEGER);
          break;
        case 4:
        case 5:
          dntColumns.add(FLOAT);
          break;
        default:
          throw new RuntimeException("Cannot resolve type id " + type);
      }
    }

    String createQuery = createCreateQuery(nameIdPair, columnNames, dntColumns);
    String insertQuery = createInsertQuery(nameIdPair, columnNames);
    @Cleanup Connection connection = dnt.getConnection();
    connection.setAutoCommit(false);

    if (args.isFresh()) {
      deleteTableOnce(connection, nameIdPair.getKey());
    }

    Statement stmt = connection.createStatement();
    PreparedStatement pstmt = connection.prepareStatement(insertQuery);

    stmt.execute(createQuery);

    // read each row and put it into entries
    for (int i = 0; i < numRows; i++) {
      pstmt.setInt(1, buf.getInt());

      int parameterIndex = 2;
      for (DntColumn dntColumn : dntColumns) {
        Object obj = dntColumn.readBuffer(buf);
        dntColumn.setValue(pstmt, parameterIndex, obj);
        parameterIndex++;
      }

      pstmt.execute();
      maybeCommit(connection);
    }

    finalCommit(connection);

    pstmt.close();
    stmt.close();
  }

  @SneakyThrows
  private void deleteTableOnce(Connection connection, String tableName) {
    if (deleted.contains(tableName)) {
      return;
    }

    deleted.add(tableName);

    Statement stmt = connection.createStatement();
    stmt.execute("DROP TABLE IF EXISTS " + tableName);
    stmt.close();
  }

  private String createCreateQuery(Pair<String, String> nameIdPair,
                                   List<String> columnNames,
                                   List<DntColumn> dntColumns) {
    int size = columnNames.size();
    StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
    builder
        .append(nameIdPair.getKey())
        .append(" (")
        .append(nameIdPair.getValue())
        .append(" INTEGER PRIMARY KEY");

    for (int i = 0; i < size; i++) {
      String columnName = columnNames.get(i);
      DntColumn dntColumn = dntColumns.get(i);

      builder
          .append(',')
          .append(columnName)
          .append(' ')
          .append(dntColumn.getDefinition());
    }

    return builder.append(')').toString();
  }

  private String createInsertQuery(Pair<String, String> nameIdPair,
                                   List<String> columnNames) {
    StringBuilder frontBuilder = new StringBuilder("INSERT INTO "
                                                   + nameIdPair.getKey()
                                                   + " ("
                                                   + nameIdPair.getValue());

    StringBuilder backBuilder = new StringBuilder("(?");

    for (String columnName : columnNames) {
      frontBuilder.append(',').append(columnName);
      backBuilder.append(",?");
    }

    return frontBuilder
        .append(')')
        .append(" VALUES ")
        .append(backBuilder)
        .append(')')
        .toString();
  }

  @SneakyThrows
  private void maybeCommit(Connection connection) {
    commitCount++;

    if (commitCount == COMMIT_COUNT) {
      commitCount = 0;
      connection.commit();
    }
  }

  @SneakyThrows
  private void finalCommit(Connection connection) {
    if (commitCount > 0) {
      commitCount = 0;
      connection.commit();
    }
  }

  private String stripExtension(File file) {
    String name = file.getName();

    // "foo.bar.baz" = "foo"; can't have a table named "foo.bar".
    return name.substring(0, name.indexOf('.'));
  }
}

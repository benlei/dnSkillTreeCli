package com.dnmaze.dncli.dnt;

import static com.dnmaze.dncli.enums.DntColumn.BOOLEAN;
import static com.dnmaze.dncli.enums.DntColumn.FLOAT;
import static com.dnmaze.dncli.enums.DntColumn.INTEGER;
import static com.dnmaze.dncli.enums.DntColumn.TEXT;

import com.dnmaze.dncli.command.CommandDnt;
import com.dnmaze.dncli.enums.DntColumn;
import com.dnmaze.dncli.util.JsUtil;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.sql.SQLException;
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
public class DntProcess implements Runnable {
  private static final int COMMIT_COUNT = Integer.parseInt(System.getProperty("dnt.commit.count",
      "5000"));

  private static final List<String> MESSAGE_COLUMN_NAMES = new ArrayList<>();
  private static final List<DntColumn> MESSAGE_COLUMN_DEFN = new ArrayList<>();

  static {
    MESSAGE_COLUMN_NAMES.add("_Message");
    MESSAGE_COLUMN_DEFN.add(DntColumn.TEXT);
  }

  private final CommandDnt.Process args;
  private Dnt dnt;
  private Set<String> deleted = new HashSet<>();
  private int commitCount;
  private Connection connection;

  public DntProcess(CommandDnt.Process args) {
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

    connection = dnt.getConnection();

    try {
      connection.setAutoCommit(false);
    } catch (SQLException ex) {
      System.err.println(ex.getMessage());
      System.err.println("Attempted to set autocommit to false, failed. Continuing...");
    }

    File messageFile = args.getMessageFile();
    if (messageFile != null) {
      try {
        System.out.println("Processing uistring file " + messageFile.getPath());
        createMessageTable(messageFile);
      } catch (Exception ex) {
        System.err.println(ex.getMessage());
      }

      System.out.println();
    }

    for (File file : args.getInputs()) {
      try {
        System.out.println("Processing DNT file " + file.getPath());
        createTables(file);
      } catch (Exception ex) {
        System.err.println(ex.getMessage());
      }

      System.out.println();
    }

    System.out.println("Running process() function...");
    dnt.process();

    IOUtils.closeQuietly(dnt);
  }

  private String getTableName(File file) {
    if (file.length() < 10) {
      throw new RuntimeException(file.getPath() + " is not a valid DNT file.");
    }

    String tableName = dnt.normalizeName(stripExtension(file));

    if (tableName == null) {
      String stripped = stripExtension(file);
      System.err.println(file.getPath() + " has no normalized name, using '" + stripped + "'");
      tableName = stripped;
    }

    if (!StringUtils.isAlphanumeric(tableName)) {
      throw new RuntimeException(tableName + " is not alphanumeric!");
    }

    return tableName;
  }

  @SuppressFBWarnings
      ({
          "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE",
          "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING"
      })
  @SneakyThrows
  private void createMessageTable(File file) {
    String tableName = getTableName(file);
    connection.setAutoCommit(false);

    if (args.isFresh()) {
      deleteTableOnce(connection, tableName);
    }

    // Create table
    Statement stmt = connection.createStatement();
    String createTableQuery = createCreateQuery(connection, tableName,
        MESSAGE_COLUMN_NAMES,
        MESSAGE_COLUMN_DEFN);

    stmt.execute(createTableQuery);

    // prepared insert query
    String insertQuery = createInsertQuery(tableName, MESSAGE_COLUMN_NAMES);
    PreparedStatement pstmt = connection.prepareStatement(insertQuery);

    // parse the XML
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

      maybeCommit(connection, i);
    }

    finalCommit(connection, nodesLength);

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
    String tableName = getTableName(file);

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
          dntColumns.add(TEXT);
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

    if (args.isFresh()) {
      deleteTableOnce(connection, tableName);
    }

    // Create the table
    Statement stmt = connection.createStatement();
    String createQuery = createCreateQuery(connection, tableName, columnNames, dntColumns);

    stmt.execute(createQuery);

    // Create the prepared insert query
    String insertQuery = createInsertQuery(tableName, columnNames);
    PreparedStatement pstmt = connection.prepareStatement(insertQuery);

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
      maybeCommit(connection, i);
    }

    finalCommit(connection, numRows);

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

  @SneakyThrows
  private String createCreateQuery(Connection connection,
                                   String tableName,
                                   List<String> columnNames,
                                   List<DntColumn> dntColumns) {
    int size = columnNames.size();
    StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
    builder
        .append(tableName)
        .append(" (ID INTEGER PRIMARY KEY");

    for (int i = 0; i < size; i++) {
      String columnName = columnNames.get(i);
      DntColumn dntColumn = dntColumns.get(i);

      builder
          .append(',')
          .append(columnName)
          .append(' ')
          .append(dntColumn.getDefinition());
    }

    builder.append(')');

    if (connection.getMetaData()
        .getDatabaseProductName()
        .equals("H2")) {
      return builder.toString();
    }

    return builder.append(" CHARACTER SET utf8 COLLATE utf8_unicode_ci").toString();
  }

  private String createInsertQuery(String tableName,
                                   List<String> columnNames) {
    StringBuilder frontBuilder = new StringBuilder("INSERT INTO "
                                                   + tableName
                                                   + " (ID");

    StringBuilder backBuilder = new StringBuilder("?");

    for (String columnName : columnNames) {
      frontBuilder.append(',').append(columnName);
      backBuilder.append(",?");
    }

    return frontBuilder
        .append(") VALUES (")
        .append(backBuilder)
        .append(") ON DUPLICATE KEY UPDATE ID = ID")
        .toString();
  }

  @SneakyThrows
  private void maybeCommit(Connection connection, int curr) {
    commitCount++;

    if (commitCount == COMMIT_COUNT) {
      commitCount = 0;
      connection.commit();
      System.out.println("Processed " + (curr + 1) + " entries...");
    }
  }

  @SneakyThrows
  private void finalCommit(Connection connection, int entries) {
    if (commitCount > 0) {
      commitCount = 0;
      connection.commit();
      System.out.println("Completed processing " + entries + " entries.");
    }
  }

  private String stripExtension(File file) {
    String name = file.getName();

    // "foo.bar.baz" = "foo"; can't have a table named "foo.bar".
    return name.substring(0, name.indexOf('.'));
  }
}

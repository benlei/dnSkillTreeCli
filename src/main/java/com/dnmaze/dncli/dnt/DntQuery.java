package com.dnmaze.dncli.dnt;

import com.dnmaze.dncli.command.CommandDnt;
import com.dnmaze.dncli.util.JsUtil;
import com.dnmaze.dncli.util.ResourceUtil;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import javax.script.Invocable;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by blei on 6/19/16.
 */
public class DntQuery implements Runnable {
  private final CommandDnt.Query args;
  private Dnt dnt;

  public DntQuery(CommandDnt.Query args) {
    this.args = args;
  }

  @Override
  public void run() {
    try {
      Invocable js = JsUtil.compileAndEval(args.getQueryFile());
      dnt = js.getInterface(Dnt.class);
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage());
    }

    File messageFile = args.getMessageFile();
    if (messageFile != null) {
      createMessageTable(messageFile);
    }

    createTables(args.getInputs());

    dnt.complete();
  }

  @SneakyThrows
  private void createMessageTable(File file) {
    String messageName = dnt.normalizeName(stripExtension(file));

    if (messageName == null) {
      throw new RuntimeException(file.getPath() + " does not have a normalized name.");
    }

    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
    document.getDocumentElement().normalize();
    NodeList nodes = document.getElementsByTagName("message");
    int nodesLength = nodes.getLength();

    String createTableQuery = String.format(ResourceUtil.read("/dntMessageInit.sql"), messageName);
    @Cleanup Connection connection = dnt.getConnection();

    @Cleanup Statement stmt = connection.createStatement();
    stmt.execute(createTableQuery);

    PreparedStatement pstmt =
        connection.prepareStatement(String.format("INSERT INTO %1$s (%1$s_id, message) "
                                                  + "VALUES (?, ?)", messageName));

    for (int i = 0; i < nodesLength; i++) {
      Element element = (Element)nodes.item(i);
      int mid = Integer.valueOf(element.getAttribute("mid"));

      String data = ((CharacterData)element.getFirstChild()).getData();
    }
  }

  private void createTables(List<File> inputs) {

  }

  private String stripExtension(File file) {
    String name = file.getName();

    // "foo.bar.baz" = "foo"; can't have a table named "foo.bar".
    return name.substring(0, name.indexOf('.'));
  }
}

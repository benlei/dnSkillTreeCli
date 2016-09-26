package com.github.ben.lei.dncli.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by blei on 9/22/16.
 */
public final class ResourceUtil {
  private ResourceUtil() {
    // do nothing
  }

  /**
   * <p>Reads a resource file as a string.</p>
   *
   * @param path the path
   * @return the contents of the file
   * @throws IOException if there was an issue opening resource
   */
  public static String read(String path) throws IOException {
    InputStream is = ResourceUtil.class.getResourceAsStream(path);
    String data = IOUtils.toString(is, Charset.defaultCharset());
    IOUtils.closeQuietly(is);
    return data;
  }
}

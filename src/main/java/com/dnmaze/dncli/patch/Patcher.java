package com.dnmaze.dncli.patch;

import com.dnmaze.dncli.command.CommandPatch;
import com.dnmaze.dncli.util.OsUtil;

import lombok.Cleanup;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by blei on 11/2/16.
 */
public class Patcher implements Runnable {
  private static final int BUFSIZE = 8192;
  private static final int MAX_REDIRECTS =
      System.getenv("MAX_REDIRECTS") == null ? 10 :
          Integer.parseInt(System.getenv("MAX_REDIRECTS"));

  private final CommandPatch args;

  public Patcher(CommandPatch args) {
    this.args = args;
  }

  @SneakyThrows
  @Override
  public void run() {
    int baseVersion = args.getBaseVersion();
    int endVersion = args.getEndVersion();
    int version = baseVersion;

    if (endVersion < baseVersion) {
      throw new RuntimeException("the end version must be at least equal to the end version.");
    }

    HttpURLConnection.setFollowRedirects(false);

    while (version < endVersion) {
      int nextVersion = version + 1;
      String strVersion = getPatchString(nextVersion);
      URL patchUrl = new URL(args.getUrl(), strVersion + "/Patch" + strVersion + ".pak");
      String url = patchUrl.toString();
      File output = new File(args.getOutput(), "Patch" + strVersion + ".pak");

      if (download(patchUrl, output, 0)) {
        log(url + " -> " + output.getPath());
      } else {
        break;
      }

      version = nextVersion;
    }

    int diff = version - baseVersion;
    if (diff != 0) {
      log("");
    }

    log("Downloaded " + diff + " patch(es).");
    log("Ended at version " + version);

    File versionFile = args.getVersionFile();

    if (versionFile != null) {
      log("");
      createVersionFile(versionFile, version);
    }
  }

  private boolean download(URL url, File destination, int redirectCount) throws IOException {
    if (redirectCount >= MAX_REDIRECTS) {
      throw new IOException("Too many redirects (" + MAX_REDIRECTS + ")");
    }

    @Cleanup("disconnect") HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    conn.setInstanceFollowRedirects(false);
    conn.connect();

    if (conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM
        || conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
      String redirectUrl = conn.getHeaderField("Location"); // follow redirect

      return redirectUrl.endsWith(destination.getName())
             && download(new URL(redirectUrl), destination, redirectCount + 1);
    }

    if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
      return false;
    }

    // if not force overwrite, check with user if can overwrite.
    if (!args.isForce() && !OsUtil.confirmOverwrite(destination)) {
      return true;
    }

    File parentDir = destination.getParentFile();
    if (!parentDir.exists() && !parentDir.mkdirs()) {
      throw new IOException("Could not create " + parentDir.getPath());
    }

    try (InputStream input = conn.getInputStream()) {
      try (OutputStream output = new FileOutputStream(destination)) {
        byte[] buffer = new byte[BUFSIZE];
        int read;

        while ((read = input.read(buffer)) != -1) {
          output.write(buffer, 0, read);
        }
      }
    }

    return true;
  }

  private void createVersionFile(File file, int version) throws IOException {
    File parentDir = file.getParentFile();
    if (!parentDir.exists() && !parentDir.mkdirs()) {
      throw new IOException("Could not create directory " + parentDir.getPath());
    }

    // if not force overwrite, check with user if can overwrite.
    if (!args.isForce() && !OsUtil.confirmOverwrite(file)) {
      return;
    }

    try (FileOutputStream output = new FileOutputStream(file)) {
      output.write(Integer.toString(version).getBytes(StandardCharsets.UTF_8));
      log("Created version file " + file.getPath());
    }
  }

  private String getPatchString(int version) {
    return ("" + (100000000 + version)).substring(1);
  }

  private void log(String message) {
    if (!args.isQuiet()) {
      System.out.println(message);
    }
  }
}

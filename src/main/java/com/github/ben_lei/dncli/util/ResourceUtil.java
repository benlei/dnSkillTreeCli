package com.github.ben_lei.dncli.util;

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

    public static String read(String path) throws IOException {
        InputStream is = ResourceUtil.class.getResourceAsStream(path);
        String data = IOUtils.toString(is, Charset.defaultCharset());
        IOUtils.closeQuietly(is);
        return data;
    }
}

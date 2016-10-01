package com.dnmaze.dncli.pak;

import com.dnmaze.dncli.pak.archive.PakFile;

/**
 * Created by blei on 9/30/16.
 */
public interface PakFilter {
  boolean filter(PakFile pakFile);
}

package com.orientechnologies.orient.core.record.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.orientechnologies.orient.core.record.ORecord;

/**
 * Created by tglman on 05/01/16.
 */
public interface OBlob extends ORecord {
  byte RECORD_TYPE = 'b';

  int fromInputStream(final InputStream in) throws IOException;

  int fromInputStream(final InputStream in, final int maxSize) throws IOException;

  void toOutputStream(final OutputStream out) throws IOException;
}

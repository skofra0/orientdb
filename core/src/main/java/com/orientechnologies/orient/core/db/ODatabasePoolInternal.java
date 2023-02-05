package com.orientechnologies.orient.core.db;

/**
 * Created by tglman on 27/06/16.
 */
public interface ODatabasePoolInternal extends AutoCloseable {

  ODatabaseSession acquire();

  void close();

  void release(ODatabaseDocumentInternal database);

  OrientDBConfig getConfig();
}

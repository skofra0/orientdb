package com.orientechnologies.orient.core.storage.impl.local.paginated.wal.cas;

import java.nio.ByteBuffer;
import com.orientechnologies.orient.core.storage.impl.local.paginated.wal.OWALRecord;

public interface OWriteableWALRecord extends OWALRecord {
  void setBinaryContent(ByteBuffer buffer, long pointer);

  ByteBuffer getBinaryContent();

  void freeBinaryContent();

  int getBinaryContentLen();

  int toStream(byte[] content, int offset);

  void toStream(ByteBuffer buffer);

  int fromStream(byte[] content, int offset);

  int serializedSize();

  boolean isUpdateMasterRecord();

  void written();

  boolean isWritten();

  byte getId();
}


package com.orientechnologies.orient.enterprise.channel.binary;

import java.io.IOException;
import java.io.OutputStream;
import com.orientechnologies.orient.core.id.ORID;

/**
 * Created by luigidellaquila on 12/12/16.
 */
public interface OChannelDataOutput {

  OChannelDataOutput writeByte(final byte iContent) throws IOException;

  OChannelDataOutput writeBoolean(final boolean iContent) throws IOException;

  OChannelDataOutput writeInt(final int iContent) throws IOException ;

  OChannelDataOutput writeLong(final long iContent) throws IOException;

  OChannelDataOutput writeShort(final short iContent) throws IOException;

  OChannelDataOutput writeString(final String iContent) throws IOException;

  OChannelDataOutput writeBytes(final byte[] iContent) throws IOException;

  OChannelDataOutput writeBytes(final byte[] iContent, final int iLength) throws IOException;

  void writeRID(final ORID iRID) throws IOException;

  void writeVersion(final int version) throws IOException;

  OutputStream getDataOutput();

}



package com.orientechnologies.orient.client.remote;

import java.io.IOException;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializer;
import com.orientechnologies.orient.enterprise.channel.binary.OChannelDataInput;
import com.orientechnologies.orient.enterprise.channel.binary.OChannelDataOutput;

/**
 * Created by tglman on 07/06/16.
 */
public interface OBinaryResponse {

  void write(OChannelDataOutput channel, int protocolVersion, ORecordSerializer serializer) throws IOException;

  void read(final OChannelDataInput network, OStorageRemoteSession session) throws IOException;
}

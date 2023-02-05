package com.orientechnologies.orient.client.remote.message;

import java.io.IOException;
import com.orientechnologies.orient.enterprise.channel.binary.OChannelDataInput;
import com.orientechnologies.orient.enterprise.channel.binary.OChannelDataOutput;

/**
 * Created by tglman on 04/05/17.
 */
public interface OBinaryPushResponse {

  void write(final OChannelDataOutput network) throws IOException;

  void read(OChannelDataInput channel) throws IOException;

}

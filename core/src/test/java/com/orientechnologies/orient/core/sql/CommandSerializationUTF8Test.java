package com.orientechnologies.orient.core.sql;

import org.junit.Assert;
import org.junit.Test;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializerFactory;
import com.orientechnologies.orient.core.sql.query.OSQLQuery;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class CommandSerializationUTF8Test {

  @Test
  public void testRightSerializationEncoding() {

    OSQLQuery<?> query = new OSQLSynchQuery<>("select from Profile where name='😢😂 '");

    Assert.assertEquals(query.toStream().length, 66);

    OSQLQuery<?> query1 = new OSQLSynchQuery<>();
    query1.fromStream(query.toStream(), ORecordSerializerFactory.instance().getDefaultRecordSerializer());

    Assert.assertEquals(query1.getText(), "select from Profile where name='😢😂 '");

  }

}

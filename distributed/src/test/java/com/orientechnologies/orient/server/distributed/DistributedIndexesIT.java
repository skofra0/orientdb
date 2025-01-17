package com.orientechnologies.orient.server.distributed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.junit.Test;

/*
 *
 *  *  Copyright 2015 OrientDB LTD (info(-at-)orientdb.com)
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *
 *  * For more information: http://orientdb.com
 *
 */

/** @author Luigi Dell'Aquila (l.dellaquila-(at)-orientdb.com) */
public class DistributedIndexesIT extends AbstractServerClusterTest {
  private static final int SERVERS = 2;

  public String getDatabaseName() {
    return "DistributedIndexesTest";
  }

  @Test
  public void test() throws Exception {
    init(SERVERS);
    prepare(false);
    execute();
  }

  @Override
  protected void executeTest() throws Exception {
    ODatabaseDocument db =
        serverInstance
            .get(1)
            .getServerInstance()
            .getContext()
            .open(getDatabaseName(), "admin", "admin");
    try {
      testIndexUsage(db);
      testIndexAcceptsNulls(db);
    } finally {
      db.close();
    }
  }

  @Override
  protected void onAfterDatabaseCreation(ODatabaseDocument db) {
    db.command("CREATE CLASS Person extends V").close();
    db.command("CREATE PROPERTY Person.name STRING").close();
    db.command("CREATE INDEX Person.name NOTUNIQUE METADATA { ignoreNullValues: false }").close();
  }

  private void testIndexAcceptsNulls(ODatabaseDocument db) {
    db.command("CREATE VERTEX Person SET name = 'Tobie'").close();
    db.command("CREATE VERTEX Person SET temp = true").close();
  }

  private void testIndexUsage(ODatabaseDocument db) {
    db.command("create class DistributedIndexTest").close();
    db.command("create property DistributedIndexTest.unique STRING").close();
    db.command("create property DistributedIndexTest.notunique STRING").close();
    db.command("create property DistributedIndexTest.dictionary STRING").close();
    db.command("create property DistributedIndexTest.unique_hash STRING").close();
    db.command("create property DistributedIndexTest.notunique_hash STRING").close();
    try {
      db.command("CREATE INDEX index_unique         ON DistributedIndexTest (unique) UNIQUE")
          .close();
      db.command("CREATE INDEX index_notunique      ON DistributedIndexTest (notunique) NOTUNIQUE")
          .close();
      db.command(
              "CREATE INDEX index_dictionary     ON DistributedIndexTest (dictionary) DICTIONARY")
          .close();
      db.command(
              "CREATE INDEX index_unique_hash    ON DistributedIndexTest (unique_hash) UNIQUE_HASH_INDEX")
          .close();
      db.command(
              "CREATE INDEX index_notunique_hash ON DistributedIndexTest (notunique_hash) NOTUNIQUE_HASH_INDEX")
          .close();

      final ODocument test1 = new ODocument("DistributedIndexTest");
      test1.field("unique", "test1");
      test1.field("notunique", "test1");
      test1.field("dictionary", "test1");
      test1.field("unique_hash", "test1");
      test1.field("notunique_hash", "test1");
      test1.save();

      final ODocument test2 = new ODocument("DistributedIndexTest");
      test2.field("unique", "test2");
      test2.field("notunique", "test2");
      test2.field("dictionary", "test2");
      test2.field("unique_hash", "test2");
      test2.field("notunique_hash", "test2");
      test2.save();

      final ODocument test3 = new ODocument("DistributedIndexTest");
      test3.field("unique", "test2");
      test3.field("notunique", "test3");
      test3.field("dictionary", "test3");
      test3.field("unique_hash", "test3");
      test3.field("notunique_hash", "test3");
      try {
        test3.save();
        fail();
      } catch (Exception e) {
        // CHECK DB COHERENCY
        try (OResultSet result = db.query("select count(*) as count from DistributedIndexTest")) {
          assertEquals((long) result.next().getProperty("count"), 2l);
        }
      }

      final ODocument test4 = new ODocument("DistributedIndexTest");
      test4.field("unique", "test4");
      test4.field("notunique", "test4");
      test4.field("dictionary", "test4");
      test4.field("unique_hash", "test2");
      test4.field("notunique_hash", "test4");
      try {
        test4.save();
        fail();
      } catch (Exception e) {
        // CHECK DB COHERENCY
        try (OResultSet result = db.command("select count(*) as count from DistributedIndexTest")) {
          assertEquals((long) result.next().getProperty("count"), 2l);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.toString());
    }
  }
}

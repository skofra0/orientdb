/*
 * Copyright 2015 OrientDB LTD (info--at--orientdb.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orientechnologies.orient.server.distributed.scenariotest;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.server.distributed.ServerRun;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * It checks the consistency in the cluster with the following scenario:
 * - 3 server down (quorum=2) with DBs distributed as below:
 *    - server1: db A, db B
 *    - server2: db B, db C
 * - populating the databases
 * - servers startup
 * - each server deploys its dbs in the cluster of nodes
 * - check consistency on all servers:
 *      - all the servers have  db A, db B, db C.
 *      - db A, db B and db C are consistent on each server
 *
 * @author Gabriele Ponzi
 * @email  <gabriele.ponzi--at--gmail.com>
 */
public class MultipleDBAlignmentOnNodesJoining extends AbstractScenarioTest {

  private String dbA = "db-A";
  private String dbB = "db-B";
  private String dbC = "db-C";

  @Test
  public void test() throws Exception {

    writerCount = 1;
    maxRetries = 10;
    init(SERVERS);
    prepare(true, true);
    execute();
  }


  public void executeTest() throws Exception {    //  TO-CHANGE

    // wait for db deploy completion
    Thread.sleep(5000L);

    try {

      // check consistency on all the server:
      //  - all the servers have  db A, db B, db C.
      //  - db A, db B and db C are consistent on each server
      compareDBOnServer(serverInstance, dbA);
      compareDBOnServer(serverInstance, dbB);
      compareDBOnServer(serverInstance, dbC);

    } catch(Exception e) {
      e.printStackTrace();
      fail();
    }
  }


  /**
   * Creates the databases as follows:
   * - server1: db A, db B
   * - server2: db B, db C
   *
   * @throws IOException
   */
  @Override
  protected void prepare(final boolean iCopyDatabaseToNodes, final boolean iCreateDatabase) throws IOException {

    serverInstance.remove(2);

    // creating databases on server1
    ServerRun master = serverInstance.get(0);

    if (iCreateDatabase) {
      final ODatabaseDocument graph1 = master.createDatabase(dbA);
      final ODatabaseDocument graph2 = master.createDatabase(dbB);
      try {
        onAfterDatabaseCreation(graph1);
        onAfterDatabaseCreation(graph2);
      } finally {
        if(!graph1.isClosed()) {
          graph1.close();
        }
        if(!graph1.isClosed()) {
          graph2.close();
        }
      }
    }

    // copying db-B on server2
    if (iCopyDatabaseToNodes)
      master.copyDatabase(dbB, serverInstance.get(1).getDatabasePath(dbB));

    // creating db-C on server2
    master = serverInstance.get(1);

    if (iCreateDatabase) {
      ODatabaseDocument graph1 = master.createDatabase(dbC);
      try {
        onAfterDatabaseCreation(graph1);
      } finally {
        if(!graph1.isClosed()) {
          graph1.close();
        }
      }
    }
  }

  /**
   * Event called right after the database has been created. It builds the schema and populates the db.
   *
   * @param db Current database
   */
  protected void onAfterDatabaseCreation(final ODatabaseDocument db) {

    String databaseName = db.getName();
    System.out.println("Creating database schema for " + databaseName + "...");

    db.activateOnCurrentThread();

    // building basic schema
    OClass personClass = db.getMetadata().getSchema().createClass("Person");
    personClass.createProperty("id", OType.STRING);
    personClass.createProperty("name", OType.STRING);
    personClass.createProperty("birthday", OType.DATE);
    personClass.createProperty("children", OType.STRING);

    final OSchema schema = db.getMetadata().getSchema();
    OClass person = schema.getClass("Person");
    idx = person.createIndex("Person.name", OClass.INDEX_TYPE.UNIQUE, "name");

    //    ODatabaseRecordThreadLocal.INSTANCE.set(null);

    // populating db
    try {
      ExecutorService executor = Executors.newSingleThreadExecutor();
      Callable writer = createWriter(db);
      Future f = executor.submit(writer);
      f.get();
      assertTrue(f.isDone());
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  // compares a database consistency on multiple servers
  protected void compareDBOnServer(List<ServerRun> checkConsistencyOnServers, String databaseName) {

    /*
     * Preliminar checks
     */

    // database must be present on all the servers

    String checkOnServer = "";
    List<ODatabaseDocument> dbs = new LinkedList<ODatabaseDocument>();
    for (ServerRun server : checkConsistencyOnServers) {
      try {
        dbs.add(getDatabase(server));
        checkOnServer += server.getServerInstance().getDistributedManager().getLocalNodeName() + ",";
      } catch(Exception e) {
        fail(databaseName + " is not present on server" + server.getServerId());
      }
    }
    checkOnServer = checkOnServer.substring(0, checkOnServer.length() - 1);
    super.banner("Checking " + databaseName + " consistency among servers...\nChecking on servers {" + checkOnServer + "}.");

    // class person is Present in each database
    for(ODatabaseDocument db: dbs) {
      assertTrue(db.getMetadata().getSchema().existsClass("Person"));
    }

    // each database on each server has the same number of records in class Person
    int j = 0;
    while (j <= dbs.size() - 2) {
      long count1 = dbs.get(j).getMetadata().getSchema().getClass("Person").count();
      long count2 = dbs.get(j+1).getMetadata().getSchema().getClass("Person").count();
      assertEquals(count1, count2);
      j++;
    }

    /*
     * Checking record by record
     */

    List<ODocument> docsToCompare = new LinkedList<ODocument>();

    super.banner("Checking " + databaseName + " consistency among servers...\nChecking on servers {" + checkOnServer
        + "}.");

    try {

      for (int i = 0; i < count; i++) {

        // load records to compare
        for (ODatabaseDocument db : dbs) {
          docsToCompare.add(loadRecord(db, i + baseCount));
        }

        // checking that record is present on each server db
        for (ODocument doc : docsToCompare) {
          assertTrue(doc != null);
        }

        // checking that all the records have the same version and values (each record is equal to the next one)
        int k = 0;
        while (k <= docsToCompare.size() - 2) {
          assertEquals(
              "Inconsistency detected. Record: " + docsToCompare.get(k).toString() + " ; Servers: " + (k + 1) + "," + (k + 2),
              docsToCompare.get(k).field("@version"), docsToCompare.get(k + 1).field("@version"));
          assertEquals(
              "Inconsistency detected. Record: " + docsToCompare.get(k).toString() + " ; Servers: " + (k + 1) + "," + (k + 2),
              docsToCompare.get(k).field("name"), docsToCompare.get(k + 1).field("name"));
          assertEquals(
              "Inconsistency detected. Record: " + docsToCompare.get(k).toString() + " ; Servers: " + (k + 1) + "," + (k + 2),
              docsToCompare.get(k).field("surname"), docsToCompare.get(k + 1).field("surname"));
          assertEquals(
              "Inconsistency detected. Record: " + docsToCompare.get(k).toString() + " ; Servers: " + (k + 1) + "," + (k + 2),
              docsToCompare.get(k).field("birthday"), docsToCompare.get(k + 1).field("birthday"));
          assertEquals(
              "Inconsistency detected. Record: " + docsToCompare.get(k).toString() + " ; Servers: " + (k + 1) + "," + (k + 2),
              docsToCompare.get(k).field("children"), docsToCompare.get(k + 1).field("children"));
          k++;
        }
        docsToCompare.clear();
      }

      System.out.println("The database " + databaseName + " is consistent in the cluster.");

    } catch (Exception e) {
      e.printStackTrace();
    } finally {

      for (ODatabaseDocument db : dbs) {
        db.close();
      }
    }

  }

  protected ODocument loadRecord(ODatabaseDocument database, int i) {
    final String uniqueId = database.getName() + "-" + i;
    database.activateOnCurrentThread();
    List<ODocument> result = database
        .query(new OSQLSynchQuery<ODocument>("select from Person where name = 'Billy" + uniqueId + "'"));
    if (result.size() == 0)
      assertTrue("No record found with name = 'Billy" + uniqueId + "'!", false);
    else if (result.size() > 1)
      assertTrue(result.size() + " records found with name = 'Billy" + uniqueId + "'!", false);
    return result.get(0);
  }

  protected Callable<Void> createWriter(ODatabaseDocument database) {
    return new DBStartupWriter(database);
  }


  class DBStartupWriter implements Callable<Void> {
    private final ODatabaseDocument db;

    public DBStartupWriter(final ODatabaseDocument db) {
      this.db = db;
    }

    @Override
    public Void call() throws Exception {

      db.activateOnCurrentThread();
      
      for (int i = 0; i < count; i++) {
        try {
          if ((i + 1) % 100 == 0)
            System.out.println("\nDBStartupWriter '" + db.getName() + "' (" + db.getURL() + ") managed " + (i + 1) + "/" + count + " records so far");

          final ODocument person = createRecord(db, i);
          updateRecord(db, i);
          checkRecord(db, i);
          checkIndex(db, (String) person.field("name"), person.getIdentity());

          if (delayWriter > 0)
            Thread.sleep(delayWriter);

        } catch (InterruptedException e) {
          System.out.println("DBStartupWriter received interrupt (db=" + db.getURL());
          Thread.currentThread().interrupt();
          break;
        } catch (Exception e) {
          System.out.println("DBStartupWriter received exception (db=" + db.getURL());
          e.printStackTrace();
          break;
        }
      }

      System.out.println("\nDBStartupWriter '" + db.getName() + "' END");
      return null;
    }

    private ODocument createRecord(ODatabaseDocument database, int i) {
      final String uniqueId = database.getName() + "-" + i;

      ODocument person = new ODocument("Person").fields("id", UUID.randomUUID().toString(), "name", "Billy" + uniqueId, "birthday", new Date(), "children", uniqueId);
      database.save(person);

      assertTrue(person.getIdentity().isPersistent());

      return person;
    }

    private void updateRecord(ODatabaseDocument database, int i) {
      ODocument doc = loadRecord(database, i);
      doc.field("updated", true);
      doc.save();
    }

    private void checkRecord(ODatabaseDocument database, int i) {
      ODocument doc = loadRecord(database, i);
      assertEquals(doc.field("updated"), Boolean.TRUE);
    }

    private void checkIndex(ODatabaseDocument database, final String key, final ORID rid) {
      final List<OIdentifiable> result = database.command(new OCommandSQL("select from index:Person.name where key = ?"))
          .execute(key);
      assertNotNull(result);
      assertEquals(result.size(), 1);
      assertNotNull(result.get(0).getRecord());
      assertEquals(((ODocument) result.get(0)).field("rid"), rid);
    }

    private ODocument loadRecord(ODatabaseDocument database, int i) {
      final String uniqueId = database.getName() + "-" + i;

      List<ODocument> result = database
          .query(new OSQLSynchQuery<ODocument>("select from Person where name = 'Billy" + uniqueId + "'"));
      if (result.size() == 0)
        assertTrue("No record found with name = 'Billy" + uniqueId + "'!", false);
      else if (result.size() > 1)
        assertTrue(result.size() + " records found with name = 'Billy" + uniqueId + "'!", false);

      return result.get(0);
    }
  }
}

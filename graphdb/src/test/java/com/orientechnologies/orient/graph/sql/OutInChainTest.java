package com.orientechnologies.orient.graph.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import org.junit.BeforeClass;
import org.junit.Test;

public class OutInChainTest {

  @BeforeClass
  public static void before() {
    // generate schema
    OrientGraphNoTx graph =
        new OrientGraphNoTx("memory:" + OutInChainTest.class.getSimpleName(), "admin", "admin");
    graph.sqlCommand("create class User extends V").close();
    graph.sqlCommand("create class Car extends V").close();
    graph.sqlCommand("create class Owns extends E").close();

    initTestMultipleLabels(graph);
    graph.shutdown();
  }

  private static void initTestMultipleLabels(OrientGraphNoTx graph) {
    graph.sqlCommand("create class V1 extends V").close();
    graph.sqlCommand("create class E1 extends E").close();
    graph.sqlCommand("create class E2 extends E").close();
  }

  @Test
  public void t() {
    OrientGraph graph =
        new OrientGraph("memory:" + OutInChainTest.class.getSimpleName(), "admin", "admin");

    Vertex vUser = graph.addVertex("class:User");
    Vertex vCar = graph.addVertex("class:Car");
    graph.addEdge("class:Owns", vUser, vCar, null);

    graph.commit();

    Iterable<Vertex> res =
        graph.command(new OCommandSQL("select expand( out('Owns') ) from User")).execute();
    assertTrue(res.iterator().hasNext());
    assertEquals("Car", res.iterator().next().getProperty("@class").toString());

    Iterable<Vertex> resEdge =
        graph.command(new OCommandSQL("select expand( inE('Owns') ) from Car")).execute();
    assertTrue(resEdge.iterator().hasNext());

    // when out('Owns') is executed we have Car vertex (see above)
    // after that inE('Owns') should return Owns edge (see above)
    // but test fails
    resEdge =
        graph
            .command(new OCommandSQL("select expand( out('Owns').inE('Owns') ) from User"))
            .execute();
    assertTrue(resEdge.iterator().hasNext()); // assertion error here
    graph.shutdown();
  }

  @Test
  public void testMultipleLabels() {
    // issue #5359

    OrientGraph graph =
        new OrientGraph("memory:" + OutInChainTest.class.getSimpleName(), "admin", "admin");

    graph.sqlCommand("create vertex V1 set name = '1'").close();
    graph.sqlCommand("create vertex V1 set name = '2'").close();
    graph.sqlCommand("create vertex V1 set name = '3'").close();

    graph
        .sqlCommand(
            "create edge E1 from (select from V1 where name = '1') to (select from V1 where name = '2')")
        .close();
    graph
        .sqlCommand(
            "create edge E2 from (select from V1 where name = '1') to (select from V1 where name = '3')")
        .close();

    Iterable result =
        graph
            .command(
                new OSQLSynchQuery<OrientEdge>(
                    "select expand(outE('E1').out.outE('E1', 'E2')) from V1 where name = '1'"))
            .execute();
    int count = 0;
    for (Object e : result) {
      count++;
    }

    assertEquals(2, count);
    //    result = graph.command(new OSQLSynchQuery<OrientEdge>("select outE('E1').out.outE('E1',
    // 'E2') from V1 where name = '1'"))
    //        .execute();
    //    iterator = result.iterator();
    //
    //    assertTrue(iterator.hasNext());
    //    iterator.next();
    //    assertTrue(iterator.hasNext());
  }
}

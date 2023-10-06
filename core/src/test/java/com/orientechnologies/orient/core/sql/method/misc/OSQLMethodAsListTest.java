package com.orientechnologies.orient.core.sql.method.misc;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Tests the "asList()" method implemented by the OSQLMethodAsList class.  Note
 * that the only input to the execute() method from the OSQLMethod interface
 * that is used is the ioResult argument (the 4th argument).
 *
 * @author Michael MacFadden
 */
public class OSQLMethodAsListTest {

  private OSQLMethodAsList function;

  @Before
  public void setup() {
    function = new OSQLMethodAsList();
  }

  @Test
  public void testList() {
    // The expected behavior is to return the list itself.
    ArrayList<Object> aList = new ArrayList<>();
    aList.add(1);
    aList.add("2");
    Object result = function.execute(null, null, null, aList, null);
    assertEquals(result, aList);
  }

  @Test
  public void testNull() {
    // The expected behavior is to return an empty list.
    Object result = function.execute(null, null, null, null, null);
    assertEquals(result, new ArrayList<>());
  }

  @Test
  public void testCollection() {
    // The expected behavior is to return a list with all of the elements
    // of the collection in it.
    Set<Object> aCollection = new LinkedHashSet<>();
    aCollection.add(1);
    aCollection.add("2");
    Object result = function.execute(null, null, null, aCollection, null);

    ArrayList<Object> expected = new ArrayList<>();
    expected.add(1);
    expected.add("2");
    assertEquals(result, expected);
  }

  public void testIterable() {
    // The expected behavior is to return a list with all of the elements
    // of the iterable in it, in order of the collecitons iterator.
    ArrayList<Object> expected = new ArrayList<>();
    expected.add(1);
    expected.add("2");

    TestIterable<Object> anIterable = new TestIterable<>(expected);
    Object result = function.execute(null, null, null, anIterable, null);

    assertEquals(result, expected);
  }

  public void testIterator() {
    // The expected behavior is to return a list with all of the elements
    // of the iterator in it, in order of the iterator.
    ArrayList<Object> expected = new ArrayList<>();
    expected.add(1);
    expected.add("2");

    TestIterable<Object> anIterable = new TestIterable<>(expected);
    Object result = function.execute(null, null, null, anIterable.iterator(), null);

    assertEquals(result, expected);
  }

  public void testODocument() {
    // The expected behavior is to return a list with only the single
    // ODocument in it.
    ODocument doc = new ODocument();
    doc.field("f1", 1);
    doc.field("f2", 2);

    Object result = function.execute(null, null, null, doc, null);

    ArrayList<Object> expected = new ArrayList<>();
    expected.add(doc);

    assertEquals(result, expected);
  }

  public void testOtherSingleValue() {
    // The expected behavior is to return a list with only the single
    // element in it.

    Object result = function.execute(null, null, null, Integer.valueOf(4), null);
    ArrayList<Object> expected = new ArrayList<>();
    expected.add(Integer.valueOf(4));
    assertEquals(result, expected);
  }
}

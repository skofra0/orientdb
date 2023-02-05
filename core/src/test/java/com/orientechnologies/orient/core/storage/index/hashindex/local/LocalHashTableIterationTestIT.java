package com.orientechnologies.orient.core.storage.index.hashindex.local;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import com.orientechnologies.common.serialization.types.OIntegerSerializer;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.serialization.serializer.binary.OBinarySerializerFactory;
import com.orientechnologies.orient.core.storage.impl.local.OAbstractPaginatedStorage;
import com.orientechnologies.orient.core.storage.impl.local.paginated.atomicoperations.OAtomicOperationsManager;

/**
 * @author Andrey Lomakin (a.lomakin-at-orientdb.com)
 * @since 13.03.13
 */
public class LocalHashTableIterationTestIT {
  private static final int KEYS_COUNT = 500000;

  private ODatabaseDocumentTx databaseDocumentTx;

  private OLocalHashTable<Integer, String> localHashTable;
  private OAtomicOperationsManager         atomicOperationsManager;

  @Before
  public void beforeClass() throws Exception {
    String buildDirectory = System.getProperty("buildDirectory");
    if (buildDirectory == null)
      buildDirectory = ".";

    databaseDocumentTx = new ODatabaseDocumentTx("plocal:" + buildDirectory + "/localHashTableIterationTest");
    if (databaseDocumentTx.exists()) {
      databaseDocumentTx.open("admin", "admin");
      databaseDocumentTx.drop();
    }

    databaseDocumentTx.create();

    OHashFunction<Integer> hashFunction = new OHashFunction<>() {
      @Override
      public long hashCode(Integer value) {
        return Long.MAX_VALUE / 2 + value;
      }
    };

    final OAbstractPaginatedStorage storage = (OAbstractPaginatedStorage) databaseDocumentTx.getStorage();
    atomicOperationsManager = storage.getAtomicOperationsManager();
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      localHashTable = new OLocalHashTable<>("localHashTableIterationTest", ".imc", ".tsc", ".obf", ".nbh", storage);

      localHashTable.create(atomicOperation, OIntegerSerializer.INSTANCE,
          OBinarySerializerFactory.getInstance().<String>getObjectSerializer(OType.STRING), null, null, hashFunction, true);
    });
  }

  @After
  public void afterClass() throws Exception {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      localHashTable.clear(atomicOperation);
      localHashTable.delete(atomicOperation);
    });
    databaseDocumentTx.drop();
  }

  @After
  public void afterMethod() throws Exception {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      localHashTable.clear(atomicOperation);
    });
  }

  @Test
  public void testNextHaveRightOrder() throws Exception {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      SortedSet<Integer> keys = new TreeSet<>();
      keys.clear();
      final Random random = new Random();

      while (keys.size() < KEYS_COUNT) {
        int key = random.nextInt();

        if (localHashTable.get(key) == null) {
          localHashTable.put(atomicOperation, key, key + "");
          keys.add(key);
          Assert.assertEquals(localHashTable.get(key), "" + key);
        }
      }

      OHashIndexBucket.Entry<Integer, String>[] entries = localHashTable.ceilingEntries(Integer.MIN_VALUE);
      int curPos = 0;
      for (int key : keys) {
        int sKey = entries[curPos].key;

        Assert.assertEquals(key, sKey);
        curPos++;
        if (curPos >= entries.length) {
          entries = localHashTable.higherEntries(entries[entries.length - 1].key);
          curPos = 0;
        }
      }
    });
  }

  public void testNextSkipsRecordValid() throws Exception {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      List<Integer> keys = new ArrayList<>();

      final Random random = new Random();
      while (keys.size() < KEYS_COUNT) {
        int key = random.nextInt();

        if (localHashTable.get(key) == null) {
          localHashTable.put(atomicOperation, key, key + "");
          keys.add(key);
          Assert.assertEquals(localHashTable.get(key), "" + key);
        }
      }

      Collections.sort(keys);

      OHashIndexBucket.Entry<Integer, String>[] entries = localHashTable.ceilingEntries(keys.get(10));
      int curPos = 0;
      for (int key : keys) {
        if (key < keys.get(10)) {
          continue;
        }
        int sKey = entries[curPos].key;
        Assert.assertEquals(key, sKey);

        curPos++;
        if (curPos >= entries.length) {
          entries = localHashTable.higherEntries(entries[entries.length - 1].key);
          curPos = 0;
        }
      }
    });
  }

  @Test
  @Ignore
  public void testNextHaveRightOrderUsingNextMethod() throws Exception {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      List<Integer> keys = new ArrayList<>();
      keys.clear();
      Random random = new Random();

      while (keys.size() < KEYS_COUNT) {
        int key = random.nextInt();

        if (localHashTable.get(key) == null) {
          localHashTable.put(atomicOperation, key, key + "");
          keys.add(key);
          Assert.assertEquals(localHashTable.get(key), key + "");
        }
      }

      Collections.sort(keys);

      for (int key : keys) {
        OHashIndexBucket.Entry<Integer, String>[] entries = localHashTable.ceilingEntries(key);
        Assert.assertTrue(key == entries[0].key);
      }

      for (int j = 0, keysSize = keys.size() - 1; j < keysSize; j++) {
        int key = keys.get(j);
        int sKey = localHashTable.higherEntries(key)[0].key;
        Assert.assertTrue(sKey == keys.get(j + 1));
      }
    });
  }
}

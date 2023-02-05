package com.orientechnologies.orient.core.storage.cluster;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import com.orientechnologies.common.exception.OException;
import com.orientechnologies.common.types.OModifiableInteger;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.storage.OPhysicalPosition;
import com.orientechnologies.orient.core.storage.ORawBuffer;
import com.orientechnologies.orient.core.storage.cluster.v0.OPaginatedClusterV0;
import com.orientechnologies.orient.core.storage.impl.local.OAbstractPaginatedStorage;
import com.orientechnologies.orient.core.storage.impl.local.paginated.atomicoperations.OAtomicOperationsManager;

public abstract class LocalPaginatedClusterAbstract {
  protected static String                    buildDirectory;
  protected static OPaginatedCluster         paginatedCluster;
  public static    ODatabaseDocumentTx       databaseDocumentTx;
  protected static OAbstractPaginatedStorage storage;
  protected static OAtomicOperationsManager  atomicOperationsManager;

  @AfterClass
  public static void afterClass() throws IOException {
    storage.getAtomicOperationsManager()
        .executeInsideAtomicOperation((atomicOperation) -> paginatedCluster.delete(atomicOperation));

    databaseDocumentTx.drop();
  }

  @Before
  public void beforeMethod() throws IOException {
    atomicOperationsManager = storage.getAtomicOperationsManager();
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> paginatedCluster.truncate(atomicOperation));
  }

  @Test
  public void testDeleteRecordAndAddNewOnItsPlace() throws IOException {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      byte[] smallRecord = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
      int recordVersion = 0;
      recordVersion++;
      recordVersion++;

      OPhysicalPosition physicalPosition = paginatedCluster
          .createRecord(atomicOperation, smallRecord, recordVersion, (byte) 1, null);
      Assert.assertEquals(physicalPosition.clusterPosition, 0);
      paginatedCluster.deleteRecord(atomicOperation, physicalPosition.clusterPosition);

      recordVersion = 0;
      Assert.assertEquals(recordVersion, 0);
      physicalPosition = paginatedCluster.createRecord(atomicOperation, smallRecord, recordVersion, (byte) 1, null);
      Assert.assertEquals(physicalPosition.clusterPosition, 1);

      Assert.assertEquals(physicalPosition.recordVersion, recordVersion);
    });
  }

  @Test
  public void testAddOneSmallRecord() throws IOException {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {

      byte[] smallRecord = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
      int recordVersion = 0;
      recordVersion++;
      recordVersion++;

      OPhysicalPosition physicalPosition = paginatedCluster
          .createRecord(atomicOperation, smallRecord, recordVersion, (byte) 1, null);
      Assert.assertEquals(physicalPosition.clusterPosition, 0);

      ORawBuffer rawBuffer = paginatedCluster.readRecord(physicalPosition.clusterPosition, false);
      Assert.assertNotNull(rawBuffer);

      Assert.assertEquals(rawBuffer.version, recordVersion);
      Assertions.assertThat(rawBuffer.buffer).isEqualTo(smallRecord);
      Assert.assertEquals(rawBuffer.recordType, 1);
    });
  }

  @Test
  public void testAddOneBigRecord() throws IOException {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      byte[] bigRecord = new byte[2 * 65536 + 100];
      Random mersenneTwisterFast = new Random();
      mersenneTwisterFast.nextBytes(bigRecord);

      int recordVersion = 0;
      recordVersion++;
      recordVersion++;

      OPhysicalPosition physicalPosition = paginatedCluster.createRecord(atomicOperation, bigRecord, recordVersion, (byte) 1, null);
      Assert.assertEquals(physicalPosition.clusterPosition, 0);

      ORawBuffer rawBuffer = paginatedCluster.readRecord(physicalPosition.clusterPosition, false);
      Assert.assertNotNull(rawBuffer);

      Assert.assertEquals(rawBuffer.version, recordVersion);
      Assertions.assertThat(rawBuffer.buffer).isEqualTo(bigRecord);
      Assert.assertEquals(rawBuffer.recordType, 1);
    });
  }

  @Test
  public void testAddManySmallRecords() throws IOException {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      final int records = 10000;

      long seed = System.currentTimeMillis();
      Random mersenneTwisterFast = new Random(seed);
      System.out.println("testAddManySmallRecords seed : " + seed);

      Map<Long, byte[]> positionRecordMap = new HashMap<>();

      int recordVersion = 0;
      recordVersion++;
      recordVersion++;

      for (int i = 0; i < records; i++) {
        int recordSize = mersenneTwisterFast.nextInt(OClusterPage.MAX_RECORD_SIZE - 1) + 1;
        byte[] smallRecord = new byte[recordSize];
        mersenneTwisterFast.nextBytes(smallRecord);

        final OPhysicalPosition physicalPosition = paginatedCluster
            .createRecord(atomicOperation, smallRecord, recordVersion, (byte) 2, null);

        positionRecordMap.put(physicalPosition.clusterPosition, smallRecord);
      }

      for (Map.Entry<Long, byte[]> entry : positionRecordMap.entrySet()) {
        ORawBuffer rawBuffer = paginatedCluster.readRecord(entry.getKey(), false);
        Assert.assertNotNull(rawBuffer);

        Assert.assertEquals(rawBuffer.version, recordVersion);
        //      Assert.assertEquals(rawBuffer.buffer, entry.getValue());

        Assertions.assertThat(rawBuffer.buffer).isEqualTo(entry.getValue());
        Assert.assertEquals(rawBuffer.recordType, 2);
      }
    });
  }

  @Test
  public void testAddManyBigRecords() throws IOException {
    final int records = 5000;

    long seed = System.currentTimeMillis();
    Random mersenneTwisterFast = new Random(seed);

    System.out.println("testAddManyBigRecords seed : " + seed);

    Map<Long, byte[]> positionRecordMap = new HashMap<>();

    final int recordVersion = 2;

    for (int i = 0; i < records; i++) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        int recordSize = mersenneTwisterFast.nextInt(2 * OClusterPage.MAX_RECORD_SIZE) + OClusterPage.MAX_RECORD_SIZE + 1;
        byte[] bigRecord = new byte[recordSize];
        mersenneTwisterFast.nextBytes(bigRecord);

        final OPhysicalPosition physicalPosition = paginatedCluster
            .createRecord(atomicOperation, bigRecord, recordVersion, (byte) 2, null);
        positionRecordMap.put(physicalPosition.clusterPosition, bigRecord);
      });
    }

    for (Map.Entry<Long, byte[]> entry : positionRecordMap.entrySet()) {
      ORawBuffer rawBuffer = paginatedCluster.readRecord(entry.getKey(), false);
      Assert.assertNotNull(rawBuffer);

      Assert.assertEquals(rawBuffer.version, recordVersion);
      Assertions.assertThat(rawBuffer.buffer).isEqualTo(entry.getValue());
      Assert.assertEquals(rawBuffer.recordType, 2);
    }
  }

  @Test
  public void testAddManyRecords() throws IOException {
    final int records = 10000;
    long seed = System.currentTimeMillis();
    Random mersenneTwisterFast = new Random(seed);

    System.out.println("testAddManyRecords seed : " + seed);

    Map<Long, byte[]> positionRecordMap = new HashMap<>();

    final int recordVersion = 2;

    for (int i = 0; i < records; i++) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        int recordSize = mersenneTwisterFast.nextInt(2 * OClusterPage.MAX_RECORD_SIZE) + 1;
        byte[] smallRecord = new byte[recordSize];
        mersenneTwisterFast.nextBytes(smallRecord);

        final OPhysicalPosition physicalPosition = paginatedCluster
            .createRecord(atomicOperation, smallRecord, recordVersion, (byte) 2, null);

        positionRecordMap.put(physicalPosition.clusterPosition, smallRecord);
      });
    }

    for (Map.Entry<Long, byte[]> entry : positionRecordMap.entrySet()) {
      ORawBuffer rawBuffer = paginatedCluster.readRecord(entry.getKey(), false);
      Assert.assertNotNull(rawBuffer);

      Assert.assertEquals(rawBuffer.version, recordVersion);
      Assertions.assertThat(rawBuffer.buffer).isEqualTo(entry.getValue());
      Assert.assertEquals(rawBuffer.recordType, 2);
    }
  }

  @Test
  public void testAllocatePositionMap() throws IOException {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      OPhysicalPosition position = paginatedCluster.allocatePosition(atomicOperation, (byte) 'd');
      Assert.assertTrue(position.clusterPosition >= 0);
      ORawBuffer rec = paginatedCluster.readRecord(position.clusterPosition, false);
      Assert.assertNull(rec);
      paginatedCluster.createRecord(atomicOperation, new byte[20], 1, (byte) 'd', position);
      rec = paginatedCluster.readRecord(position.clusterPosition, false);
      Assert.assertNotNull(rec);
    });
  }

  @Test
  public void testManyAllocatePositionMap() throws IOException {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      final int records = 10000;

      List<OPhysicalPosition> positions = new ArrayList<>();
      for (int i = 0; i < records; i++) {
        OPhysicalPosition position = paginatedCluster.allocatePosition(atomicOperation, (byte) 'd');
        Assert.assertTrue(position.clusterPosition >= 0);
        ORawBuffer rec = paginatedCluster.readRecord(position.clusterPosition, false);
        Assert.assertNull(rec);
        positions.add(position);
      }

      for (int i = 0; i < records; i++) {
        OPhysicalPosition position = positions.get(i);
        paginatedCluster.createRecord(atomicOperation, new byte[20], 1, (byte) 'd', position);
        ORawBuffer rec = paginatedCluster.readRecord(position.clusterPosition, false);
        Assert.assertNotNull(rec);
      }
    });
  }

  @Test
  public void testRemoveHalfSmallRecords() throws IOException {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      final int records = 10000;
      long seed = System.currentTimeMillis();
      Random mersenneTwisterFast = new Random(seed);

      System.out.println("testRemoveHalfSmallRecords seed : " + seed);

      Map<Long, byte[]> positionRecordMap = new HashMap<>();

      int recordVersion = 0;
      recordVersion++;
      recordVersion++;

      for (int i = 0; i < records; i++) {
        int recordSize = mersenneTwisterFast.nextInt(OClusterPage.MAX_RECORD_SIZE - 1) + 1;
        byte[] smallRecord = new byte[recordSize];
        mersenneTwisterFast.nextBytes(smallRecord);

        final OPhysicalPosition physicalPosition = paginatedCluster
            .createRecord(atomicOperation, smallRecord, recordVersion, (byte) 2, null);

        positionRecordMap.put(physicalPosition.clusterPosition, smallRecord);
      }

      int deletedRecords = 0;
      Assert.assertEquals(records, paginatedCluster.getEntries());
      Set<Long> deletedPositions = new HashSet<>();
      Iterator<Long> positionIterator = positionRecordMap.keySet().iterator();
      while (positionIterator.hasNext()) {
        long clusterPosition = positionIterator.next();
        if (mersenneTwisterFast.nextBoolean()) {
          deletedPositions.add(clusterPosition);
          Assert.assertTrue(paginatedCluster.deleteRecord(atomicOperation, clusterPosition));
          deletedRecords++;

          Assert.assertEquals(records - deletedRecords, paginatedCluster.getEntries());

          positionIterator.remove();
        }
      }

      Assert.assertEquals(paginatedCluster.getEntries(), records - deletedRecords);
      for (long deletedPosition : deletedPositions) {
        Assert.assertNull(paginatedCluster.readRecord(deletedPosition, false));
        Assert.assertFalse(paginatedCluster.deleteRecord(atomicOperation, deletedPosition));
      }

      for (Map.Entry<Long, byte[]> entry : positionRecordMap.entrySet()) {
        ORawBuffer rawBuffer = paginatedCluster.readRecord(entry.getKey(), false);
        Assert.assertNotNull(rawBuffer);

        Assert.assertEquals(rawBuffer.version, recordVersion);
        //      Assert.assertEquals(rawBuffer.buffer, entry.getValue());
        Assertions.assertThat(rawBuffer.buffer).isEqualTo(entry.getValue());
        Assert.assertEquals(rawBuffer.recordType, 2);
      }
    });
  }

  @Test
  public void testHideHalfSmallRecords() throws IOException {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      final int records = 10000;
      long seed = System.currentTimeMillis();
      Random mersenneTwisterFast = new Random(seed);

      System.out.println("testHideHalfSmallRecords seed : " + seed);

      Map<Long, byte[]> positionRecordMap = new HashMap<>();

      int recordVersion = 0;
      recordVersion++;
      recordVersion++;

      for (int i = 0; i < records; i++) {
        int recordSize = mersenneTwisterFast.nextInt(OClusterPage.MAX_RECORD_SIZE - 1) + 1;
        byte[] smallRecord = new byte[recordSize];
        mersenneTwisterFast.nextBytes(smallRecord);

        final OPhysicalPosition physicalPosition = paginatedCluster
            .createRecord(atomicOperation, smallRecord, recordVersion, (byte) 2, null);

        positionRecordMap.put(physicalPosition.clusterPosition, smallRecord);
      }

      int hiddenRecords = 0;
      Assert.assertEquals(records, paginatedCluster.getEntries());
      Set<Long> hiddenPositions = new HashSet<>();
      Iterator<Long> positionIterator = positionRecordMap.keySet().iterator();
      while (positionIterator.hasNext()) {
        long clusterPosition = positionIterator.next();
        if (mersenneTwisterFast.nextBoolean()) {
          hiddenPositions.add(clusterPosition);
          Assert.assertTrue(paginatedCluster.hideRecord(atomicOperation, clusterPosition));
          hiddenRecords++;

          Assert.assertEquals(records - hiddenRecords, paginatedCluster.getEntries());

          positionIterator.remove();
        }
      }

      Assert.assertEquals(paginatedCluster.getEntries(), records - hiddenRecords);
      for (long deletedPosition : hiddenPositions) {
        Assert.assertNull(paginatedCluster.readRecord(deletedPosition, false));
        Assert.assertFalse(paginatedCluster.hideRecord(atomicOperation, deletedPosition));
      }

      for (Map.Entry<Long, byte[]> entry : positionRecordMap.entrySet()) {
        ORawBuffer rawBuffer = paginatedCluster.readRecord(entry.getKey(), false);
        Assert.assertNotNull(rawBuffer);

        Assert.assertEquals(rawBuffer.version, recordVersion);
        //      Assert.assertEquals(rawBuffer.buffer, entry.getValue());

        Assertions.assertThat(rawBuffer.buffer).isEqualTo(entry.getValue());
        Assert.assertEquals(rawBuffer.recordType, 2);
      }
    });
  }

  @Test
  public void testRemoveHalfBigRecords() throws IOException {
    final int records = 5000;
    long seed = System.currentTimeMillis();
    Random mersenneTwisterFast = new Random(seed);

    System.out.println("testRemoveHalfBigRecords seed : " + seed);

    Map<Long, byte[]> positionRecordMap = new HashMap<>();

    final int recordVersion = 2;

    for (int i = 0; i < records; i++) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        int recordSize = mersenneTwisterFast.nextInt(2 * OClusterPage.MAX_RECORD_SIZE) + OClusterPage.MAX_RECORD_SIZE + 1;

        byte[] bigRecord = new byte[recordSize];
        mersenneTwisterFast.nextBytes(bigRecord);

        final OPhysicalPosition physicalPosition = paginatedCluster
            .createRecord(atomicOperation, bigRecord, recordVersion, (byte) 2, null);

        positionRecordMap.put(physicalPosition.clusterPosition, bigRecord);
      });
    }

    OModifiableInteger deletedRecords = new OModifiableInteger();
    Assert.assertEquals(records, paginatedCluster.getEntries());
    Set<Long> deletedPositions = new HashSet<>();
    Iterator<Long> positionIterator = positionRecordMap.keySet().iterator();
    while (positionIterator.hasNext()) {
      long clusterPosition = positionIterator.next();
      if (mersenneTwisterFast.nextBoolean()) {
        atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
          deletedPositions.add(clusterPosition);
          Assert.assertTrue(paginatedCluster.deleteRecord(atomicOperation, clusterPosition));
          deletedRecords.increment();

          Assert.assertEquals(records - deletedRecords.value, paginatedCluster.getEntries());

          positionIterator.remove();
        });
      }
    }

    Assert.assertEquals(paginatedCluster.getEntries(), records - deletedRecords.value);
    for (long deletedPosition : deletedPositions) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        Assert.assertNull(paginatedCluster.readRecord(deletedPosition, false));
        Assert.assertFalse(paginatedCluster.deleteRecord(atomicOperation, deletedPosition));
      });
    }

    for (Map.Entry<Long, byte[]> entry : positionRecordMap.entrySet()) {
      ORawBuffer rawBuffer = paginatedCluster.readRecord(entry.getKey(), false);
      Assert.assertNotNull(rawBuffer);

      Assert.assertEquals(rawBuffer.version, recordVersion);
      Assertions.assertThat(rawBuffer.buffer).isEqualTo(entry.getValue());
      Assert.assertEquals(rawBuffer.recordType, 2);
    }
  }

  @Test
  public void testHideHalfBigRecords() throws IOException {
    final int records = 5000;
    long seed = System.currentTimeMillis();
    Random mersenneTwisterFast = new Random(seed);

    System.out.println("testHideHalfBigRecords seed : " + seed);

    Map<Long, byte[]> positionRecordMap = new HashMap<>();

    for (int i = 0; i < records; i++) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        int recordSize = mersenneTwisterFast.nextInt(2 * OClusterPage.MAX_RECORD_SIZE) + OClusterPage.MAX_RECORD_SIZE + 1;

        byte[] bigRecord = new byte[recordSize];
        mersenneTwisterFast.nextBytes(bigRecord);

        final OPhysicalPosition physicalPosition = paginatedCluster.createRecord(atomicOperation, bigRecord, 2, (byte) 2, null);

        positionRecordMap.put(physicalPosition.clusterPosition, bigRecord);
      });
    }

    final OModifiableInteger hiddenRecords = new OModifiableInteger();
    Assert.assertEquals(records, paginatedCluster.getEntries());
    Set<Long> hiddenPositions = new HashSet<>();

    Iterator<Long> positionIterator = positionRecordMap.keySet().iterator();
    while (positionIterator.hasNext()) {
      long clusterPosition = positionIterator.next();
      if (mersenneTwisterFast.nextBoolean()) {
        atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
          hiddenPositions.add(clusterPosition);
          Assert.assertTrue(paginatedCluster.hideRecord(atomicOperation, clusterPosition));
          hiddenRecords.increment();

          Assert.assertEquals(records - hiddenRecords.getValue(), paginatedCluster.getEntries());

          positionIterator.remove();
        });
      }
    }

    Assert.assertEquals(paginatedCluster.getEntries(), records - hiddenRecords.getValue());
    for (long hiddenPosition : hiddenPositions) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        Assert.assertNull(paginatedCluster.readRecord(hiddenPosition, false));
        Assert.assertFalse(paginatedCluster.hideRecord(atomicOperation, hiddenPosition));
      });
    }

    for (Map.Entry<Long, byte[]> entry : positionRecordMap.entrySet()) {
      ORawBuffer rawBuffer = paginatedCluster.readRecord(entry.getKey(), false);
      Assert.assertNotNull(rawBuffer);

      Assert.assertEquals(rawBuffer.version, 2);
      //      Assert.assertEquals(rawBuffer.buffer, entry.getValue());
      Assertions.assertThat(rawBuffer.buffer).isEqualTo(entry.getValue());
      Assert.assertEquals(rawBuffer.recordType, 2);
    }
  }

  @Test
  public void testRemoveHalfRecords() throws IOException {
    final int records = 10000;
    long seed = System.currentTimeMillis();
    Random mersenneTwisterFast = new Random(seed);

    System.out.println("testRemoveHalfRecords seed : " + seed);

    Map<Long, byte[]> positionRecordMap = new HashMap<>();

    for (int i = 0; i < records; i++) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        int recordSize = mersenneTwisterFast.nextInt(3 * OClusterPage.MAX_RECORD_SIZE) + 1;

        byte[] bigRecord = new byte[recordSize];
        mersenneTwisterFast.nextBytes(bigRecord);

        final OPhysicalPosition physicalPosition = paginatedCluster.createRecord(atomicOperation, bigRecord, 2, (byte) 2, null);

        positionRecordMap.put(physicalPosition.clusterPosition, bigRecord);
      });
    }

    OModifiableInteger deletedRecords = new OModifiableInteger();
    Assert.assertEquals(records, paginatedCluster.getEntries());
    Set<Long> deletedPositions = new HashSet<>();
    Iterator<Long> positionIterator = positionRecordMap.keySet().iterator();
    while (positionIterator.hasNext()) {
      long clusterPosition = positionIterator.next();
      if (mersenneTwisterFast.nextBoolean()) {
        atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
          deletedPositions.add(clusterPosition);
          Assert.assertTrue(paginatedCluster.deleteRecord(atomicOperation, clusterPosition));
          deletedRecords.increment();

          Assert.assertEquals(records - deletedRecords.getValue(), paginatedCluster.getEntries());

          positionIterator.remove();
        });
      }
    }

    Assert.assertEquals(paginatedCluster.getEntries(), records - deletedRecords.getValue());
    for (long deletedPosition : deletedPositions) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        Assert.assertNull(paginatedCluster.readRecord(deletedPosition, false));
        Assert.assertFalse(paginatedCluster.deleteRecord(atomicOperation, deletedPosition));
      });
    }

    for (Map.Entry<Long, byte[]> entry : positionRecordMap.entrySet()) {
      ORawBuffer rawBuffer = paginatedCluster.readRecord(entry.getKey(), false);
      Assert.assertNotNull(rawBuffer);

      Assert.assertEquals(rawBuffer.version, 2);
      Assertions.assertThat(rawBuffer.buffer).isEqualTo(entry.getValue());
      Assert.assertEquals(rawBuffer.recordType, 2);
    }
  }

  @Test
  public void testHideHalfRecords() throws IOException {
    final int records = 10000;
    long seed = System.currentTimeMillis();
    Random mersenneTwisterFast = new Random(seed);

    System.out.println("testHideHalfRecords seed : " + seed);

    Map<Long, byte[]> positionRecordMap = new HashMap<>();

    for (int i = 0; i < records; i++) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        int recordSize = mersenneTwisterFast.nextInt(3 * OClusterPage.MAX_RECORD_SIZE) + 1;

        byte[] bigRecord = new byte[recordSize];
        mersenneTwisterFast.nextBytes(bigRecord);

        final OPhysicalPosition physicalPosition = paginatedCluster.createRecord(atomicOperation, bigRecord, 2, (byte) 2, null);

        positionRecordMap.put(physicalPosition.clusterPosition, bigRecord);
      });
    }

    OModifiableInteger hiddenRecords = new OModifiableInteger();
    Assert.assertEquals(records, paginatedCluster.getEntries());
    Set<Long> hiddenPositions = new HashSet<>();
    Iterator<Long> positionIterator = positionRecordMap.keySet().iterator();
    while (positionIterator.hasNext()) {
      long clusterPosition = positionIterator.next();
      if (mersenneTwisterFast.nextBoolean()) {
        atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
          hiddenPositions.add(clusterPosition);
          Assert.assertTrue(paginatedCluster.hideRecord(atomicOperation, clusterPosition));
          hiddenRecords.increment();

          Assert.assertEquals(records - hiddenRecords.getValue(), paginatedCluster.getEntries());

          positionIterator.remove();
        });
      }
    }

    Assert.assertEquals(paginatedCluster.getEntries(), records - hiddenRecords.getValue());
    for (long deletedPosition : hiddenPositions) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        Assert.assertNull(paginatedCluster.readRecord(deletedPosition, false));
        Assert.assertFalse(paginatedCluster.hideRecord(atomicOperation, deletedPosition));
      });
    }

    for (Map.Entry<Long, byte[]> entry : positionRecordMap.entrySet()) {
      ORawBuffer rawBuffer = paginatedCluster.readRecord(entry.getKey(), false);
      Assert.assertNotNull(rawBuffer);

      Assert.assertEquals(rawBuffer.version, 2);
      //      Assert.assertEquals(rawBuffer.buffer, entry.getValue());

      Assertions.assertThat(rawBuffer.buffer).isEqualTo(entry.getValue());
      Assert.assertEquals(rawBuffer.recordType, 2);
    }
  }

  @Test
  public void testRemoveHalfRecordsAndAddAnotherHalfAgain() throws IOException {

    final int records = 10000;
    long seed = System.currentTimeMillis();
    Random mersenneTwisterFast = new Random(seed);

    System.out.println("testRemoveHalfRecordsAndAddAnotherHalfAgain seed : " + seed);

    Map<Long, byte[]> positionRecordMap = new HashMap<>();

    for (int i = 0; i < records; i++) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        int recordSize = mersenneTwisterFast.nextInt(3 * OClusterPage.MAX_RECORD_SIZE) + 1;

        byte[] bigRecord = new byte[recordSize];
        mersenneTwisterFast.nextBytes(bigRecord);

        final OPhysicalPosition physicalPosition = paginatedCluster.createRecord(atomicOperation, bigRecord, 2, (byte) 2, null);

        positionRecordMap.put(physicalPosition.clusterPosition, bigRecord);
      });
    }

    OModifiableInteger deletedRecords = new OModifiableInteger();
    Assert.assertEquals(records, paginatedCluster.getEntries());

    Iterator<Long> positionIterator = positionRecordMap.keySet().iterator();
    while (positionIterator.hasNext()) {
      long clusterPosition = positionIterator.next();
      if (mersenneTwisterFast.nextBoolean()) {
        atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
          Assert.assertTrue(paginatedCluster.deleteRecord(atomicOperation, clusterPosition));
          deletedRecords.increment();

          Assert.assertEquals(paginatedCluster.getEntries(), records - deletedRecords.getValue());

          positionIterator.remove();
        });
      }
    }

    Assert.assertEquals(paginatedCluster.getEntries(), records - deletedRecords.getValue());

    for (int i = 0; i < records / 2; i++) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        int recordSize = mersenneTwisterFast.nextInt(3 * OClusterPage.MAX_RECORD_SIZE) + 1;

        byte[] bigRecord = new byte[recordSize];
        mersenneTwisterFast.nextBytes(bigRecord);

        final OPhysicalPosition physicalPosition = paginatedCluster.createRecord(atomicOperation, bigRecord, 2, (byte) 2, null);

        positionRecordMap.put(physicalPosition.clusterPosition, bigRecord);
      });
    }

    Assert.assertEquals(paginatedCluster.getEntries(), (long) (1.5 * records - deletedRecords.getValue()));
  }

  @Test
  public void testHideHalfRecordsAndAddAnotherHalfAgain() throws IOException {
    final int records = 10000;
    long seed = System.currentTimeMillis();
    Random mersenneTwisterFast = new Random(seed);

    System.out.println("testHideHalfRecordsAndAddAnotherHalfAgain seed : " + seed);

    Map<Long, byte[]> positionRecordMap = new HashMap<>();

    for (int i = 0; i < records; i++) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        int recordSize = mersenneTwisterFast.nextInt(3 * OClusterPage.MAX_RECORD_SIZE) + 1;

        byte[] bigRecord = new byte[recordSize];
        mersenneTwisterFast.nextBytes(bigRecord);

        final OPhysicalPosition physicalPosition = paginatedCluster.createRecord(atomicOperation, bigRecord, 2, (byte) 2, null);

        positionRecordMap.put(physicalPosition.clusterPosition, bigRecord);
      });
    }

    OModifiableInteger hiddenRecords = new OModifiableInteger();
    Assert.assertEquals(records, paginatedCluster.getEntries());

    Iterator<Long> positionIterator = positionRecordMap.keySet().iterator();
    while (positionIterator.hasNext()) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        long clusterPosition = positionIterator.next();
        if (mersenneTwisterFast.nextBoolean()) {
          Assert.assertTrue(paginatedCluster.hideRecord(atomicOperation, clusterPosition));
          hiddenRecords.increment();

          Assert.assertEquals(paginatedCluster.getEntries(), records - hiddenRecords.getValue());

          positionIterator.remove();
        }
      });
    }

    Assert.assertEquals(paginatedCluster.getEntries(), records - hiddenRecords.getValue());

    for (int i = 0; i < records / 2; i++) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        int recordSize = mersenneTwisterFast.nextInt(3 * OClusterPage.MAX_RECORD_SIZE) + 1;

        byte[] bigRecord = new byte[recordSize];
        mersenneTwisterFast.nextBytes(bigRecord);

        final OPhysicalPosition physicalPosition = paginatedCluster.createRecord(atomicOperation, bigRecord, 2, (byte) 2, null);

        positionRecordMap.put(physicalPosition.clusterPosition, bigRecord);
      });
    }

    Assert.assertEquals(paginatedCluster.getEntries(), (long) (1.5 * records - hiddenRecords.getValue()));
  }

  @Test
  public void testUpdateOneSmallRecord() throws IOException {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      byte[] smallRecord = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
      int recordVersion = 0;
      recordVersion++;
      recordVersion++;

      OPhysicalPosition physicalPosition = paginatedCluster
          .createRecord(atomicOperation, smallRecord, recordVersion, (byte) 1, null);
      Assert.assertEquals(physicalPosition.clusterPosition, 0);

      recordVersion++;
      smallRecord = new byte[] { 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3 };
      paginatedCluster.updateRecord(atomicOperation, physicalPosition.clusterPosition, smallRecord, recordVersion, (byte) 2);

      ORawBuffer rawBuffer = paginatedCluster.readRecord(physicalPosition.clusterPosition, false);
      Assert.assertNotNull(rawBuffer);

      Assert.assertEquals(rawBuffer.version, recordVersion);
      //    Assert.assertEquals(rawBuffer.buffer, smallRecord);

      Assertions.assertThat(rawBuffer.buffer).isEqualTo(smallRecord);
      Assert.assertEquals(rawBuffer.recordType, 2);
    });
  }

  @Test
  public void testUpdateOneSmallRecordVersionIsLowerCurrentOne() throws IOException {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      byte[] smallRecord = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
      int recordVersion = 0;
      recordVersion++;
      recordVersion++;

      OPhysicalPosition physicalPosition = paginatedCluster
          .createRecord(atomicOperation, smallRecord, recordVersion, (byte) 1, null);
      Assert.assertEquals(physicalPosition.clusterPosition, 0);

      int updateRecordVersion = 0;
      updateRecordVersion++;

      smallRecord = new byte[] { 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3 };
      paginatedCluster.updateRecord(atomicOperation, physicalPosition.clusterPosition, smallRecord, updateRecordVersion, (byte) 2);

      ORawBuffer rawBuffer = paginatedCluster.readRecord(physicalPosition.clusterPosition, false);
      Assert.assertNotNull(rawBuffer);

      Assert.assertEquals(rawBuffer.version, updateRecordVersion);

      //    Assert.assertEquals(rawBuffer.buffer, smallRecord);

      Assertions.assertThat(rawBuffer.buffer).isEqualTo(smallRecord);
      Assert.assertEquals(rawBuffer.recordType, 2);
    });
  }

  @Test
  public void testUpdateOneSmallRecordVersionIsMinusTwo() throws IOException {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      byte[] smallRecord = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
      int recordVersion = 0;
      recordVersion++;
      recordVersion++;

      OPhysicalPosition physicalPosition = paginatedCluster
          .createRecord(atomicOperation, smallRecord, recordVersion, (byte) 1, null);
      Assert.assertEquals(physicalPosition.clusterPosition, 0);

      int updateRecordVersion;
      updateRecordVersion = -2;

      smallRecord = new byte[] { 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3 };
      paginatedCluster.updateRecord(atomicOperation, physicalPosition.clusterPosition, smallRecord, updateRecordVersion, (byte) 2);

      ORawBuffer rawBuffer = paginatedCluster.readRecord(physicalPosition.clusterPosition, false);
      Assert.assertNotNull(rawBuffer);

      Assert.assertEquals(rawBuffer.version, updateRecordVersion);
      //    Assert.assertEquals(rawBuffer.buffer, smallRecord);

      Assertions.assertThat(rawBuffer.buffer).isEqualTo(smallRecord);

      Assert.assertEquals(rawBuffer.recordType, 2);
    });
  }

  @Test
  public void testUpdateOneBigRecord() throws IOException {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      byte[] bigRecord = new byte[2 * 65536 + 100];
      Random mersenneTwisterFast = new Random();
      mersenneTwisterFast.nextBytes(bigRecord);

      int recordVersion = 0;
      recordVersion++;
      recordVersion++;

      OPhysicalPosition physicalPosition = paginatedCluster.createRecord(atomicOperation, bigRecord, recordVersion, (byte) 1, null);
      Assert.assertEquals(physicalPosition.clusterPosition, 0);

      recordVersion++;
      bigRecord = new byte[2 * 65536 + 20];
      mersenneTwisterFast.nextBytes(bigRecord);

      paginatedCluster.updateRecord(atomicOperation, physicalPosition.clusterPosition, bigRecord, recordVersion, (byte) 2);

      ORawBuffer rawBuffer = paginatedCluster.readRecord(physicalPosition.clusterPosition, false);
      Assert.assertNotNull(rawBuffer);

      Assert.assertEquals(rawBuffer.version, recordVersion);
      //    Assert.assertEquals(rawBuffer.buffer, bigRecord);

      Assertions.assertThat(rawBuffer.buffer).isEqualTo(bigRecord);
      Assert.assertEquals(rawBuffer.recordType, 2);
    });
  }

  @Test
  public void testUpdateManySmallRecords() throws IOException {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      final int records = 10000;

      long seed = System.currentTimeMillis();
      Random mersenneTwisterFast = new Random(seed);
      System.out.println("testUpdateManySmallRecords seed : " + seed);

      Map<Long, byte[]> positionRecordMap = new HashMap<>();
      Set<Long> updatedPositions = new HashSet<>();

      int recordVersion = 0;
      recordVersion++;
      recordVersion++;

      for (int i = 0; i < records; i++) {
        int recordSize = mersenneTwisterFast.nextInt(OClusterPage.MAX_RECORD_SIZE - 1) + 1;
        byte[] smallRecord = new byte[recordSize];
        mersenneTwisterFast.nextBytes(smallRecord);

        final OPhysicalPosition physicalPosition = paginatedCluster
            .createRecord(atomicOperation, smallRecord, recordVersion, (byte) 2, null);

        positionRecordMap.put(physicalPosition.clusterPosition, smallRecord);
      }

      int newRecordVersion;
      newRecordVersion = recordVersion;
      newRecordVersion++;

      for (long clusterPosition : positionRecordMap.keySet()) {
        if (mersenneTwisterFast.nextBoolean()) {
          int recordSize = mersenneTwisterFast.nextInt(OClusterPage.MAX_RECORD_SIZE - 1) + 1;
          byte[] smallRecord = new byte[recordSize];
          mersenneTwisterFast.nextBytes(smallRecord);

          paginatedCluster.updateRecord(atomicOperation, clusterPosition, smallRecord, newRecordVersion, (byte) 3);

          positionRecordMap.put(clusterPosition, smallRecord);
          updatedPositions.add(clusterPosition);
        }
      }

      for (Map.Entry<Long, byte[]> entry : positionRecordMap.entrySet()) {
        ORawBuffer rawBuffer = paginatedCluster.readRecord(entry.getKey(), false);
        Assert.assertNotNull(rawBuffer);

        //      Assert.assertEquals(rawBuffer.buffer, entry.getValue());

        Assertions.assertThat(rawBuffer.buffer).isEqualTo(entry.getValue());

        if (updatedPositions.contains(entry.getKey())) {
          Assert.assertEquals(rawBuffer.version, newRecordVersion);
          Assert.assertEquals(rawBuffer.recordType, 3);
        } else {
          Assert.assertEquals(rawBuffer.version, recordVersion);
          Assert.assertEquals(rawBuffer.recordType, 2);
        }
      }
    });
  }

  @Test
  public void testUpdateManyBigRecords() throws IOException {
    final int records = 5000;

    long seed = System.currentTimeMillis();
    Random mersenneTwisterFast = new Random(seed);
    System.out.println("testUpdateManyBigRecords seed : " + seed);

    Map<Long, byte[]> positionRecordMap = new HashMap<>();
    Set<Long> updatedPositions = new HashSet<>();

    final int recordVersion = 2;

    for (int i = 0; i < records; i++) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        int recordSize = mersenneTwisterFast.nextInt(2 * OClusterPage.MAX_RECORD_SIZE) + OClusterPage.MAX_RECORD_SIZE + 1;
        byte[] bigRecord = new byte[recordSize];
        mersenneTwisterFast.nextBytes(bigRecord);

        final OPhysicalPosition physicalPosition = paginatedCluster
            .createRecord(atomicOperation, bigRecord, recordVersion, (byte) 2, null);
        positionRecordMap.put(physicalPosition.clusterPosition, bigRecord);
      });
    }

    final int newRecordVersion = recordVersion + 1;

    for (long clusterPosition : positionRecordMap.keySet()) {
      if (mersenneTwisterFast.nextBoolean()) {
        atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {

          int recordSize = mersenneTwisterFast.nextInt(2 * OClusterPage.MAX_RECORD_SIZE) + OClusterPage.MAX_RECORD_SIZE + 1;
          byte[] bigRecord = new byte[recordSize];
          mersenneTwisterFast.nextBytes(bigRecord);

          paginatedCluster.updateRecord(atomicOperation, clusterPosition, bigRecord, newRecordVersion, (byte) 3);

          positionRecordMap.put(clusterPosition, bigRecord);
          updatedPositions.add(clusterPosition);
        });
      }
    }

    for (Map.Entry<Long, byte[]> entry : positionRecordMap.entrySet()) {
      ORawBuffer rawBuffer = paginatedCluster.readRecord(entry.getKey(), false);
      Assert.assertNotNull(rawBuffer);
      Assertions.assertThat(rawBuffer.buffer).isEqualTo(entry.getValue());

      if (updatedPositions.contains(entry.getKey())) {
        Assert.assertEquals(rawBuffer.version, newRecordVersion);

        Assert.assertEquals(rawBuffer.recordType, 3);
      } else {
        Assert.assertEquals(rawBuffer.version, recordVersion);
        Assert.assertEquals(rawBuffer.recordType, 2);
      }
    }
  }

  @Test
  public void testUpdateManyRecords() throws IOException {
    final int records = 10000;

    long seed = System.currentTimeMillis();
    Random mersenneTwisterFast = new Random(seed);
    System.out.println("testUpdateManyRecords seed : " + seed);

    Map<Long, byte[]> positionRecordMap = new HashMap<>();
    Set<Long> updatedPositions = new HashSet<>();

    final int recordVersion = 2;

    for (int i = 0; i < records; i++) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        int recordSize = mersenneTwisterFast.nextInt(2 * OClusterPage.MAX_RECORD_SIZE) + 1;
        byte[] record = new byte[recordSize];
        mersenneTwisterFast.nextBytes(record);

        final OPhysicalPosition physicalPosition = paginatedCluster
            .createRecord(atomicOperation, record, recordVersion, (byte) 2, null);
        positionRecordMap.put(physicalPosition.clusterPosition, record);
      });
    }

    final int newRecordVersion = recordVersion + 1;

    for (long clusterPosition : positionRecordMap.keySet()) {
      if (mersenneTwisterFast.nextBoolean()) {
        atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
          int recordSize = mersenneTwisterFast.nextInt(2 * OClusterPage.MAX_RECORD_SIZE) + 1;
          byte[] record = new byte[recordSize];
          mersenneTwisterFast.nextBytes(record);

          paginatedCluster.updateRecord(atomicOperation, clusterPosition, record, newRecordVersion, (byte) 3);

          positionRecordMap.put(clusterPosition, record);
          updatedPositions.add(clusterPosition);
        });
      }
    }

    for (Map.Entry<Long, byte[]> entry : positionRecordMap.entrySet()) {
      ORawBuffer rawBuffer = paginatedCluster.readRecord(entry.getKey(), false);
      Assert.assertNotNull(rawBuffer);

      Assertions.assertThat(rawBuffer.buffer).isEqualTo(entry.getValue());

      if (updatedPositions.contains(entry.getKey())) {
        Assert.assertEquals(rawBuffer.version, newRecordVersion);
        Assert.assertEquals(rawBuffer.recordType, 3);
      } else {
        Assert.assertEquals(rawBuffer.version, recordVersion);
        Assert.assertEquals(rawBuffer.recordType, 2);
      }
    }
  }

  @Test
  public void testForwardIteration() throws IOException {
    final int records = 10000;

    long seed = System.currentTimeMillis();
    Random mersenneTwisterFast = new Random(seed);
    System.out.println("testForwardIteration seed : " + seed);

    NavigableMap<Long, byte[]> positionRecordMap = new TreeMap<>();

    final int recordVersion = 2;

    for (int i = 0; i < records; i++) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        int recordSize = mersenneTwisterFast.nextInt(2 * OClusterPage.MAX_RECORD_SIZE) + 1;
        byte[] record = new byte[recordSize];
        mersenneTwisterFast.nextBytes(record);

        final OPhysicalPosition physicalPosition = paginatedCluster
            .createRecord(atomicOperation, record, recordVersion, (byte) 2, null);
        positionRecordMap.put(physicalPosition.clusterPosition, record);
      });
    }

    Iterator<Long> positionIterator = positionRecordMap.keySet().iterator();
    while (positionIterator.hasNext()) {
      long clusterPosition = positionIterator.next();
      if (mersenneTwisterFast.nextBoolean()) {
        atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
          Assert.assertTrue(paginatedCluster.deleteRecord(atomicOperation, clusterPosition));
          positionIterator.remove();
        });
      }
    }

    OPhysicalPosition physicalPosition = new OPhysicalPosition();
    physicalPosition.clusterPosition = 0;

    OPhysicalPosition[] positions = paginatedCluster.ceilingPositions(physicalPosition);
    Assert.assertTrue(positions.length > 0);

    int counter = 0;
    for (long testedPosition : positionRecordMap.keySet()) {
      Assert.assertTrue(positions.length > 0);
      Assert.assertEquals(positions[0].clusterPosition, testedPosition);

      OPhysicalPosition positionToFind = positions[0];
      positions = paginatedCluster.higherPositions(positionToFind);

      counter++;
    }

    Assert.assertEquals(paginatedCluster.getEntries(), counter);

    Assert.assertEquals(paginatedCluster.getFirstPosition(), (long) positionRecordMap.firstKey());
    Assert.assertEquals(paginatedCluster.getLastPosition(), (long) positionRecordMap.lastKey());
  }

  @Test
  public void testBackwardIteration() throws IOException {
    final int records = 10000;

    long seed = System.currentTimeMillis();
    Random mersenneTwisterFast = new Random(seed);
    System.out.println("testBackwardIteration seed : " + seed);

    NavigableMap<Long, byte[]> positionRecordMap = new TreeMap<>();

    final int recordVersion = 2;

    for (int i = 0; i < records; i++) {
      atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
        int recordSize = mersenneTwisterFast.nextInt(2 * OClusterPage.MAX_RECORD_SIZE) + 1;
        byte[] record = new byte[recordSize];
        mersenneTwisterFast.nextBytes(record);

        final OPhysicalPosition physicalPosition = paginatedCluster
            .createRecord(atomicOperation, record, recordVersion, (byte) 2, null);
        positionRecordMap.put(physicalPosition.clusterPosition, record);
      });
    }

    final Iterator<Long> positionIterator = positionRecordMap.keySet().iterator();
    while (positionIterator.hasNext()) {
      long clusterPosition = positionIterator.next();
      if (mersenneTwisterFast.nextBoolean()) {
        atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
          Assert.assertTrue(paginatedCluster.deleteRecord(atomicOperation, clusterPosition));
          positionIterator.remove();
        });
      }
    }

    OPhysicalPosition physicalPosition = new OPhysicalPosition();
    physicalPosition.clusterPosition = Long.MAX_VALUE;

    OPhysicalPosition[] positions = paginatedCluster.floorPositions(physicalPosition);
    Assert.assertTrue(positions.length > 0);

    final Iterator<Long> descendingPositionIterator = positionRecordMap.descendingKeySet().iterator();
    int counter = 0;
    while (descendingPositionIterator.hasNext()) {
      Assert.assertTrue(positions.length > 0);

      long testedPosition = descendingPositionIterator.next();
      Assert.assertEquals(positions[positions.length - 1].clusterPosition, testedPosition);

      OPhysicalPosition positionToFind = positions[positions.length - 1];
      positions = paginatedCluster.lowerPositions(positionToFind);

      counter++;
    }

    Assert.assertEquals(paginatedCluster.getEntries(), counter);

    Assert.assertEquals(paginatedCluster.getFirstPosition(), (long) positionRecordMap.firstKey());
    Assert.assertEquals(paginatedCluster.getLastPosition(), (long) positionRecordMap.lastKey());
  }

  @Test
  public void testGetPhysicalPosition() throws IOException {
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> {
      final int records = 10000;

      long seed = System.currentTimeMillis();
      Random mersenneTwisterFast = new Random(seed);
      System.out.println("testGetPhysicalPosition seed : " + seed);

      Set<OPhysicalPosition> positions = new HashSet<>();

      int recordVersion = 0;
      recordVersion++;
      recordVersion++;

      for (int i = 0; i < records; i++) {
        int recordSize = mersenneTwisterFast.nextInt(2 * OClusterPage.MAX_RECORD_SIZE) + 1;
        byte[] record = new byte[recordSize];
        mersenneTwisterFast.nextBytes(record);
        recordVersion++;

        final OPhysicalPosition physicalPosition = paginatedCluster
            .createRecord(atomicOperation, record, recordVersion, (byte) i, null);
        positions.add(physicalPosition);
      }

      Set<OPhysicalPosition> removedPositions = new HashSet<>();
      for (OPhysicalPosition position : positions) {
        OPhysicalPosition physicalPosition = new OPhysicalPosition();
        physicalPosition.clusterPosition = position.clusterPosition;

        physicalPosition = paginatedCluster.getPhysicalPosition(physicalPosition);

        Assert.assertEquals(physicalPosition.clusterPosition, position.clusterPosition);
        Assert.assertEquals(physicalPosition.recordType, position.recordType);

        Assert.assertEquals(physicalPosition.recordSize, position.recordSize);
        if (mersenneTwisterFast.nextBoolean()) {
          paginatedCluster.deleteRecord(atomicOperation, position.clusterPosition);
          removedPositions.add(position);
        }
      }

      for (OPhysicalPosition position : positions) {
        OPhysicalPosition physicalPosition = new OPhysicalPosition();
        physicalPosition.clusterPosition = position.clusterPosition;

        physicalPosition = paginatedCluster.getPhysicalPosition(physicalPosition);

        if (removedPositions.contains(position))
          Assert.assertNull(physicalPosition);
        else {
          Assert.assertEquals(physicalPosition.clusterPosition, position.clusterPosition);
          Assert.assertEquals(physicalPosition.recordType, position.recordType);

          Assert.assertEquals(physicalPosition.recordSize, position.recordSize);
        }
      }
    });
  }

  @Test
  public void testResurrectRecord() throws IOException {
    OPhysicalPosition physicalPosition = atomicOperationsManager.calculateInsideAtomicOperation((atomicOperation) -> {
      final byte[] smallRecord = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
      return paginatedCluster.createRecord(atomicOperation, smallRecord, 2, (byte) 1, null);
    });
    Assert.assertEquals(physicalPosition.clusterPosition, 0);

    Assert.assertEquals(paginatedCluster.getRecordStatus(physicalPosition.clusterPosition),
        OPaginatedClusterV0.RECORD_STATUS.PRESENT);

    final OModifiableInteger recordVersion = new OModifiableInteger(2);
    for (int i = 0; i < 1000; ++i) {
      atomicOperationsManager.executeInsideAtomicOperation(atomicOperation -> {
        recordVersion.increment();
        final byte[] smallRecord = new byte[] { 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3 };
        try {
          paginatedCluster
              .recycleRecord(atomicOperation, physicalPosition.clusterPosition, smallRecord, recordVersion.getValue(), (byte) 2);
          Assert.fail("it must be not possible to resurrect a non deleted record");
        } catch (OException e) {
          // OK
        }

        Assert.assertEquals(paginatedCluster.getRecordStatus(physicalPosition.clusterPosition),
            OPaginatedCluster.RECORD_STATUS.PRESENT);

        paginatedCluster.deleteRecord(atomicOperation, physicalPosition.clusterPosition);

        Assert.assertEquals(paginatedCluster.getRecordStatus(physicalPosition.clusterPosition),
            OPaginatedCluster.RECORD_STATUS.REMOVED);

        ORawBuffer rawBuffer = paginatedCluster.readRecord(physicalPosition.clusterPosition, false);
        Assert.assertNull(rawBuffer);

        paginatedCluster
            .recycleRecord(atomicOperation, physicalPosition.clusterPosition, smallRecord, recordVersion.getValue(), (byte) 2);

        rawBuffer = paginatedCluster.readRecord(physicalPosition.clusterPosition, false);
        Assert.assertNotNull(rawBuffer);
        Assert.assertEquals(rawBuffer.version, recordVersion.value);
        //      Assert.assertEquals(rawBuffer.buffer, smallRecord);
        Assertions.assertThat(rawBuffer.buffer).isEqualTo(smallRecord);

        Assert.assertEquals(rawBuffer.recordType, 2);

        // UPDATE 10 TIMES WITH A GROWING CONTENT TO STIMULATE DEFRAG AND CHANGE OF PAGES
        for (int k = 0; k < 10; ++k) {
          final byte[] updatedRecord = new byte[10 * k];
          for (int j = 0; j < updatedRecord.length; ++j) {
            updatedRecord[j] = (byte) j;
          }
          paginatedCluster
              .updateRecord(atomicOperation, physicalPosition.clusterPosition, updatedRecord, recordVersion.getValue(), (byte) 4);

        }
      });
    }
  }
}

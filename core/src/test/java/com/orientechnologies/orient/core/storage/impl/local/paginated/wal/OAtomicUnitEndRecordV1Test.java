package com.orientechnologies.orient.core.storage.impl.local.paginated.wal;

import org.junit.Assert;
import org.junit.Test;
import java.util.LinkedHashMap;
import java.util.Map;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.storage.impl.local.paginated.ORecordOperationMetadata;
import com.orientechnologies.orient.core.storage.impl.local.paginated.atomicoperations.OAtomicOperationMetadata;

public class OAtomicUnitEndRecordV1Test {
  @Test
  public void recordMetadataSerializationTest() {
    ORecordOperationMetadata recordOperationMetadata = new ORecordOperationMetadata();
    recordOperationMetadata.addRid(new ORecordId(10, 42));
    recordOperationMetadata.addRid(new ORecordId(42, 10));

    Map<String, OAtomicOperationMetadata<?>> metadata = new LinkedHashMap<>();
    metadata.put(recordOperationMetadata.getKey(), recordOperationMetadata);

    OAtomicUnitEndRecord atomicUnitEndRecord = new OAtomicUnitEndRecordV1(OOperationUnitId.generateId(), false, metadata);
    int arraySize = atomicUnitEndRecord.serializedSize() + 1;
    byte[] content = new byte[arraySize];

    final int endOffset = atomicUnitEndRecord.toStream(content, 1);
    Assert.assertEquals(endOffset, content.length);

    OAtomicUnitEndRecord atomicUnitEndRecordD = new OAtomicUnitEndRecordV1();
    final int dEndOffset = atomicUnitEndRecordD.fromStream(content, 1);
    Assert.assertEquals(dEndOffset, content.length);

    Assert.assertEquals(atomicUnitEndRecordD.getOperationUnitId(), atomicUnitEndRecord.getOperationUnitId());
    ORecordOperationMetadata recordOperationMetadataD = (ORecordOperationMetadata) atomicUnitEndRecordD.getAtomicOperationMetadata()
        .get(ORecordOperationMetadata.RID_METADATA_KEY);

    Assert.assertEquals(recordOperationMetadataD.getValue(), recordOperationMetadata.getValue());
  }

  @Test
  public void recordNoMetadataSerializationTest() {
    OAtomicUnitEndRecord atomicUnitEndRecord = new OAtomicUnitEndRecordV1(OOperationUnitId.generateId(), false, null);
    int arraySize = atomicUnitEndRecord.serializedSize() + 1;
    byte[] content = new byte[arraySize];

    final int endOffset = atomicUnitEndRecord.toStream(content, 1);
    Assert.assertEquals(endOffset, content.length);

    OAtomicUnitEndRecord atomicUnitEndRecordD = new OAtomicUnitEndRecordV1();
    final int dEndOffset = atomicUnitEndRecordD.fromStream(content, 1);
    Assert.assertEquals(dEndOffset, content.length);
  }
}

package com.orientechnologies.orient.core.storage.impl.local.paginated.wal;

import java.util.Map;
import com.orientechnologies.orient.core.storage.impl.local.paginated.atomicoperations.OAtomicOperationMetadata;

public final class OAtomicUnitEndRecordV1 extends OAtomicUnitEndRecord<OOperationUnitId> implements OperationUnitOperationId {
  public OAtomicUnitEndRecordV1() {
  }

  public OAtomicUnitEndRecordV1(OOperationUnitId operationUnitId, boolean rollback,
      Map<String, OAtomicOperationMetadata<?>> atomicOperationMetadataMap) {
    super(operationUnitId, rollback, atomicOperationMetadataMap);
  }

  @Override
  public byte getId() {
    return WALRecordTypes.ATOMIC_UNIT_END_RECORD;
  }
}

package com.orientechnologies.orient.core.storage.index.sbtree.local;

import java.io.File;
import com.orientechnologies.common.io.OFileUtils;
import com.orientechnologies.common.serialization.types.OIntegerSerializer;
import com.orientechnologies.orient.core.db.ODatabaseInternal;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.encryption.OEncryption;
import com.orientechnologies.orient.core.encryption.OEncryptionFactory;
import com.orientechnologies.orient.core.serialization.serializer.binary.impl.OLinkSerializer;
import com.orientechnologies.orient.core.storage.impl.local.OAbstractPaginatedStorage;
import com.orientechnologies.orient.core.storage.impl.local.paginated.atomicoperations.OAtomicOperationsManager;

public class SBTreeTestEncryptionTestIT extends SBTreeTestIT {
  @Override
  public void before() throws Exception {
    buildDirectory = System.getProperty("buildDirectory", ".");

    dbName = "localSBTreeEncryptedTest";
    final File dbDirectory = new File(buildDirectory, dbName);
    OFileUtils.deleteRecursively(dbDirectory);

    orientDB = new OrientDB("plocal:" + buildDirectory, OrientDBConfig.defaultConfig());

    orientDB.create(dbName, ODatabaseType.PLOCAL);
    databaseDocumentTx = orientDB.open(dbName, "admin", "admin");

    final OAbstractPaginatedStorage storage = (OAbstractPaginatedStorage) ((ODatabaseInternal) databaseDocumentTx).getStorage();
    sbTree = new OSBTree<>("sbTreeEncrypted", ".sbt", ".nbt", storage);

    final OEncryption encryption = OEncryptionFactory.INSTANCE.getEncryption("aes/gcm", "T1JJRU5UREJfSVNfQ09PTA==");
    OAtomicOperationsManager atomicOperationsManager = storage.getAtomicOperationsManager();
    atomicOperationsManager.executeInsideAtomicOperation((atomicOperation) -> sbTree
        .create(atomicOperation, OIntegerSerializer.INSTANCE, OLinkSerializer.INSTANCE, null, 1, false, encryption));
  }
}

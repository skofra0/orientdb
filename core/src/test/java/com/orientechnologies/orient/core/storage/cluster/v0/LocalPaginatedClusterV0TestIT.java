package com.orientechnologies.orient.core.storage.cluster.v0;

import org.junit.BeforeClass;
import java.io.File;
import java.io.IOException;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.storage.cluster.LocalPaginatedClusterAbstract;
import com.orientechnologies.orient.core.storage.impl.local.OAbstractPaginatedStorage;

/**
 * @author Andrey Lomakin (a.lomakin-at-orientdb.com)
 * @since 26.03.13
 */

public class LocalPaginatedClusterV0TestIT extends LocalPaginatedClusterAbstract {
  @BeforeClass
  public static void beforeClass() throws IOException {
    buildDirectory = System.getProperty("buildDirectory");
    if (buildDirectory == null || buildDirectory.isEmpty())
      buildDirectory = ".";

    buildDirectory += "/localPaginatedClusterTest";

    databaseDocumentTx = new ODatabaseDocumentTx(
        "plocal:" + buildDirectory + File.separator + LocalPaginatedClusterV0TestIT.class.getSimpleName());
    if (databaseDocumentTx.exists()) {
      databaseDocumentTx.open("admin", "admin");
      databaseDocumentTx.drop();
    }

    databaseDocumentTx.create();

    storage = (OAbstractPaginatedStorage) databaseDocumentTx.getStorage();
    atomicOperationsManager = storage.getAtomicOperationsManager();

    storage.getAtomicOperationsManager().executeInsideAtomicOperation((atomicOperation) -> {
      paginatedCluster = new OPaginatedClusterV0("paginatedClusterTest", storage);
      paginatedCluster.configure(storage, 42, "paginatedClusterTest", buildDirectory, -1);
      paginatedCluster.create(atomicOperation, -1);
    });

  }

}

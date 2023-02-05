package com.orientechnologies.orient.core.storage.index.hashindex.local.cache;

import org.junit.Before;
import com.orientechnologies.orient.core.storage.cache.local.twoq.ConcurrentLRUList;

public class ConcurrentLRUListTest extends AbstractLRUListTestTemplate {
  @Before
  public void setUp() throws Exception {
    lruList = new ConcurrentLRUList();
  }
}

package com.orientechnologies.orient.core.storage.index.sbtree.multivalue.v2;

import java.util.Map;
import com.orientechnologies.orient.core.id.ORID;

final class MapEntry<K> implements Map.Entry<K, ORID> {
  private final K    key;
  private final ORID rid;

  MapEntry(K key, ORID rid) {
    this.key = key;
    this.rid = rid;
  }

  @Override
  public K getKey() {
    return key;
  }

  @Override
  public ORID getValue() {
    return rid;
  }

  @Override
  public ORID setValue(ORID value) {
    throw new UnsupportedOperationException();
  }
}

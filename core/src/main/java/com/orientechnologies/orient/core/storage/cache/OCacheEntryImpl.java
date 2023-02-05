package com.orientechnologies.orient.core.storage.cache;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import com.orientechnologies.orient.core.storage.cache.chm.LRUList;
import com.orientechnologies.orient.core.storage.impl.local.paginated.wal.OLogSequenceNumber;
import com.orientechnologies.orient.core.storage.impl.local.paginated.wal.OWALChanges;

/**
 * Created by tglman on 23/06/16.
 */
public class OCacheEntryImpl implements OCacheEntry {

  private static final AtomicIntegerFieldUpdater<OCacheEntryImpl> USAGES_COUNT_UPDATER;
  private static final AtomicIntegerFieldUpdater<OCacheEntryImpl> STATE_UPDATER;

  static {
    USAGES_COUNT_UPDATER =
        AtomicIntegerFieldUpdater.newUpdater(OCacheEntryImpl.class, "usagesCount");
    STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(OCacheEntryImpl.class, "state");
  }

  private static final int FROZEN = -1;
  private static final int DEAD = -2;

  private OCachePointer dataPointer;
  private final long fileId;
  private final long pageIndex;

  private volatile int usagesCount = 0;
  private volatile int state = 0;

  private OCacheEntry next;
  private OCacheEntry prev;

  private LRUList container;

  private final boolean insideCache;

  public OCacheEntryImpl(final long fileId, final long pageIndex, final OCachePointer dataPointer,
      boolean insideCache) {
    this.fileId = fileId;
    this.pageIndex = pageIndex;

    this.dataPointer = dataPointer;
    this.insideCache = insideCache;
  }

  @Override
  public OCachePointer getCachePointer() {
    return dataPointer;
  }

  @Override
  public void clearCachePointer() {
    dataPointer = null;
  }

  @Override
  public void setCachePointer(final OCachePointer cachePointer) {
    this.dataPointer = cachePointer;
  }

  @Override
  public long getFileId() {
    return fileId;
  }

  @Override
  public long getPageIndex() {
    return pageIndex;
  }

  @Override
  public void acquireExclusiveLock() {
    dataPointer.acquireExclusiveLock();
  }

  @Override
  public void releaseExclusiveLock() {
    dataPointer.releaseExclusiveLock();
  }

  @Override
  public void acquireSharedLock() {
    dataPointer.acquireSharedLock();
  }

  @Override
  public void releaseSharedLock() {
    dataPointer.releaseSharedLock();
  }

  @Override
  public int getUsagesCount() {
    return USAGES_COUNT_UPDATER.get(this);
  }

  @Override
  public void incrementUsages() {
    USAGES_COUNT_UPDATER.incrementAndGet(this);
  }

  /**
   * DEBUG only !!
   *
   * @return Whether lock acquired on current entry
   */
  @Override
  public boolean isLockAcquiredByCurrentThread() {
    return dataPointer.isLockAcquiredByCurrentThread();
  }

  @Override
  public void decrementUsages() {
    USAGES_COUNT_UPDATER.decrementAndGet(this);
  }

  @Override
  public OWALChanges getChanges() {
    return null;
  }

  @Override
  public OLogSequenceNumber getEndLSN() {
    return dataPointer.getEndLSN();
  }

  @Override
  public void setEndLSN(final OLogSequenceNumber endLSN) {
    dataPointer.setEndLSN(endLSN);
  }

  @Override
  public boolean insideCache() {
    return insideCache;
  }

  @Override
  public boolean acquireEntry() {
    int state = STATE_UPDATER.get(this);

    while (state >= 0) {
      if (STATE_UPDATER.compareAndSet(this, state, state + 1)) {
        return true;
      }

      state = STATE_UPDATER.get(this);
    }

    return false;
  }

  @Override
  public void releaseEntry() {
    int state = STATE_UPDATER.get(this);

    while (true) {
      if (state <= 0) {
        throw new IllegalStateException("Cache entry " + fileId + ":" + pageIndex + " has invalid state " + state);
      }

      if (STATE_UPDATER.compareAndSet(this, state, state - 1)) {
        return;
      }

      state = STATE_UPDATER.get(this);
    }
  }

  @Override
  public boolean isReleased() {
    return STATE_UPDATER.get(this) == 0;
  }

  @Override
  public boolean isAlive() {
    return STATE_UPDATER.get(this) >= 0;
  }

  @Override
  public boolean freeze() {
    int state = STATE_UPDATER.get(this);
    while (state == 0) {
      if (STATE_UPDATER.compareAndSet(this, state, FROZEN)) {
        return true;
      }

      state = STATE_UPDATER.get(this);
    }

    return false;
  }

  @Override
  public boolean isFrozen() {
    return STATE_UPDATER.get(this) == FROZEN;
  }

  @Override
  public void makeDead() {
    int state = STATE_UPDATER.get(this);

    while (state == FROZEN) {
      if (STATE_UPDATER.compareAndSet(this, state, DEAD)) {
        return;
      }

      state = STATE_UPDATER.get(this);
    }

    throw new IllegalStateException("Cache entry " + fileId + ":" + pageIndex + " has invalid state " + state);
  }

  @Override
  public boolean isDead() {
    return STATE_UPDATER.get(this) == DEAD;
  }

  @Override
  public OCacheEntry getNext() {
    return next;
  }

  @Override
  public OCacheEntry getPrev() {
    return prev;
  }

  @Override
  public void setPrev(final OCacheEntry prev) {
    this.prev = prev;
  }

  @Override
  public void setNext(final OCacheEntry next) {
    this.next = next;
  }

  @Override
  public void setContainer(final LRUList lruList) {
    this.container = lruList;
  }

  @Override
  public LRUList getContainer() {
    return container;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final OCacheEntryImpl that = (OCacheEntryImpl) o;

    if (fileId != that.fileId)
      return false;
    return pageIndex == that.pageIndex;
  }

  @Override
  public int hashCode() {
    int result = (int) (fileId ^ (fileId >>> 32));
    result = 31 * result + (int) (pageIndex ^ (pageIndex >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "OCacheEntryImpl{" + "dataPointer=" + dataPointer + ", fileId=" + fileId + ", pageIndex=" + pageIndex + ", usagesCount="
        + usagesCount + '}';
  }
}

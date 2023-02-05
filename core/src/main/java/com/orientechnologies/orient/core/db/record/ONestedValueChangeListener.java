package com.orientechnologies.orient.core.db.record;

import java.lang.ref.WeakReference;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.record.impl.ONestedMultiValueChangeEvent;

/**
 * Created by tglman on 11/03/16.
 */
public class ONestedValueChangeListener<K, V> implements OMultiValueChangeListener<K, V> {

  private WeakReference<ODocument>           ownerDoc;
  private OTrackedMultiValue                 ownerCollection;
  private OTrackedMultiValue                 currentCollecion;
  private ONestedMultiValueChangeEvent<K, V> nestedEvent;

  public ONestedValueChangeListener(ODocument ownerDoc, OTrackedMultiValue ownerCollection, OTrackedMultiValue currentCollecion) {
    this.ownerDoc = new WeakReference<>(ownerDoc);
    this.ownerCollection = ownerCollection;
    this.currentCollecion = currentCollecion;
  }

  @Override
  public void onAfterRecordChanged(OMultiValueChangeEvent<K, V> event) {
    if (ownerDoc.get() == null)
      return;

    if (nestedEvent == null) {
      nestedEvent = new ONestedMultiValueChangeEvent(currentCollecion, currentCollecion);
      ownerCollection.fireCollectionChangedEvent(nestedEvent);

    }
    OMultiValueChangeTimeLine timeline = nestedEvent.getTimeLine();
    if (timeline == null) {
      timeline = new OMultiValueChangeTimeLine();
      nestedEvent.setTimeLine(timeline);
    }
    timeline.addCollectionChangeEvent(event);

  }



}

package com.orientechnologies.orient.core.exception;

/**
 * @author Andrey Lomakin (a.lomakin-at-orientdb.com) <lomakin.andrey@gmail.com>.
 * @since 9/28/2015
 */
public class OReadCacheException extends OCoreException {

  public OReadCacheException(OReadCacheException exception) {
    super(exception);
  }

  public OReadCacheException(String message) {
    super(message);
  }
}

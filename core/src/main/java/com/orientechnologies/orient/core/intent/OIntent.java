/*
 *
 *  *  Copyright 2010-2016 OrientDB LTD (http://orientdb.com)
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *
 *  * For more information: http://orientdb.com
 *
 */
package com.orientechnologies.orient.core.intent;

import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;

/**
 * Intents aim to define common use case in order to optimize the execution.
 *
 * @author Luca Garulli (l.garulli--(at)--orientdb.com)
 */
@Deprecated
public interface OIntent {
  /**
   * Activate the intent.
   *
   * @param iDatabase Database where to activate it
   */
  public void begin(ODatabaseDocumentInternal iDatabase);

  /**
   * Activate the intent.
   *
   * @param iDatabase Database where to activate it
   */
  public void end(ODatabaseDocumentInternal iDatabase);

  public OIntent copy();
}

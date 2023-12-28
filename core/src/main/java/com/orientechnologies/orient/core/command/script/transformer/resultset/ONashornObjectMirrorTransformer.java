package com.orientechnologies.orient.core.command.script.transformer.resultset;

import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import java.util.LinkedHashMap;
import java.util.Map;
import com.orientechnologies.orient.core.command.script.OScriptResultSets;
import com.orientechnologies.orient.core.command.script.transformer.OScriptTransformer;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

/**
 * Created by Enrico Risa on 27/01/17.
 */
public class ONashornObjectMirrorTransformer implements OResultSetTransformer<ScriptObjectMirror> {

  OScriptTransformer transformer;

  public ONashornObjectMirrorTransformer(OScriptTransformer transformer) {
    this.transformer = transformer;
  }

  @Override
  public OResultSet transform(ScriptObjectMirror value) {
    if (value.isArray()) {
      return OScriptResultSets.singleton(value.values(), transformer);
    } else {
      Map<String, Object> object = new LinkedHashMap<>();
      value.forEach(object::put);
      OScriptResultSets.singleton(object, transformer);
    }
    return OScriptResultSets.empty();
  }
}

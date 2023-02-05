package com.orientechnologies.orient.core.command.script;

import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import com.orientechnologies.orient.core.command.script.transformer.OScriptTransformer;
import com.orientechnologies.orient.core.command.script.transformer.resultset.ONashornObjectMirrorTransformer;

/**
 * Created by tglman on 25/01/17.
 */
public class OJavascriptScriptExecutor extends OJsr223ScriptExecutor {

  public OJavascriptScriptExecutor(String language, OScriptTransformer scriptTransformer) {
    super(language, scriptTransformer);
    scriptTransformer
        .registerResultSetTransformer(ScriptObjectMirror.class, new ONashornObjectMirrorTransformer(scriptTransformer));

  }
}

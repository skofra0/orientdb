package com.orientechnologies.orient.core.command.script.js;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import com.orientechnologies.orient.core.command.script.OSecuredScriptFactory;

public class ORhinoScriptEngineFactory extends OSecuredScriptFactory<ScriptEngineFactory> {

  public ORhinoScriptEngineFactory(ScriptEngineFactory engineFactory) {
    super( engineFactory);
  }

  private static final Set<String> CLASSES = ConcurrentHashMap.newKeySet();

  @Override
  public ScriptEngine getScriptEngine() {

    var engine = engineFactory.getScriptEngine();
    /*
    var s = org.mozilla.javascript.Context.enter().getClassShutterSetter();
    if (s != null) {
      s.setClassShutter(new OClassShutter());
    }
    */
    CLASSES.clear();
    CLASSES.addAll(getPackages());
    return engine;
  }

  /*
  public static class OClassShutter implements org.mozilla.javascript.ClassShutter {

    @Override
    public boolean visibleToScripts(String fullClassName) {
      var classes = CLASSES;
      if (classes.isEmpty()) {
        return false;
      }
      var test = classes.stream().anyMatch(e -> fullClassName.matches(e));
      return test;
    }
  }
  */
}

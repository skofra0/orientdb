package com.orientechnologies.orient.core.command.script.js;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.script.ScriptEngineFactory;

public class OJSScriptEngineFactory {

  private static final List<String> WRAPPED_LANGUAGES = Arrays.asList("javascript", "ecmascript", "rhino");

  public static ScriptEngineFactory maybeWrap(ScriptEngineFactory engineFactory) {
    if (WRAPPED_LANGUAGES.contains(engineFactory.getLanguageName().toLowerCase(Locale.ENGLISH)) && engineFactory.getClass().getName().equalsIgnoreCase("org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory")) {
      return new ONashornScriptEngineFactory(engineFactory);
    }
    if (WRAPPED_LANGUAGES.contains(engineFactory.getLanguageName().toLowerCase(Locale.ENGLISH)) && engineFactory.getClass().getName().equalsIgnoreCase("org.mozilla.javascript.engine.RhinoScriptEngineFactory")) {
      return new ORhinoScriptEngineFactory(engineFactory);
    }
    return engineFactory;
  }
}

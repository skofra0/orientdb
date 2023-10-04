package com.orientechnologies.orient.core.command.script.js;

import org.openjdk.nashorn.api.scripting.ClassFilter;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import com.orientechnologies.orient.core.command.script.OSecuredScriptFactory;

public class ONashornScriptEngineFactory extends OSecuredScriptFactory<NashornScriptEngineFactory> {

  public ONashornScriptEngineFactory(ScriptEngineFactory engineFactory) {
    super((NashornScriptEngineFactory)engineFactory);
  }

  @Override
  public ScriptEngine getScriptEngine() {
    return engineFactory.getScriptEngine(new OClassFilter(this));
  }

  public static class OClassFilter implements ClassFilter {

    private ONashornScriptEngineFactory factory;

    public OClassFilter(ONashornScriptEngineFactory factory) {
      this.factory = factory;
    }

    @Override
    public boolean exposeToScripts(String s) {
      return factory.getPackages().stream().map(e -> s.matches(e)).filter(f -> f).findFirst().isPresent();
    }
  }

}

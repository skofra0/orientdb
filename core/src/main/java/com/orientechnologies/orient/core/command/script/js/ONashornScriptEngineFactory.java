package com.orientechnologies.orient.core.command.script.js;

import com.orientechnologies.orient.core.command.script.OSecuredScriptFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import java.util.List;

public class ONashornScriptEngineFactory extends OSecuredScriptFactory {

  private ScriptEngineFactory engineFactory;

  public ONashornScriptEngineFactory(ScriptEngineFactory engineFactory) {
    this.engineFactory = engineFactory;
  }

  @Override
  public String getEngineName() {
    return engineFactory.getEngineName();
  }

  @Override
  public String getEngineVersion() {
    return engineFactory.getEngineVersion();
  }

  @Override
  public List<String> getExtensions() {
    return engineFactory.getExtensions();
  }

  @Override
  public List<String> getMimeTypes() {
    return engineFactory.getMimeTypes();
  }

  @Override
  public List<String> getNames() {
    return engineFactory.getNames();
  }

  @Override
  public String getLanguageName() {
    return engineFactory.getLanguageName();
  }

  @Override
  public String getLanguageVersion() {
    return engineFactory.getLanguageVersion();
  }

  @Override
  public Object getParameter(String key) {
    return engineFactory.getParameter(key);
  }

  @Override
  public String getMethodCallSyntax(String obj, String m, String... args) {
    return engineFactory.getMethodCallSyntax(obj, m, args);
  }

  @Override
  public String getOutputStatement(String toDisplay) {
    return engineFactory.getOutputStatement(toDisplay);
  }

  @Override
  public String getProgram(String... statements) {
    return engineFactory.getProgram(statements);
  }

  @Override
  public ScriptEngine getScriptEngine() {
    // NEXUS_MOD remove NashornScript
    return ((NashornScriptEngineFactory) engineFactory)
        .getScriptEngine(s -> this.getPackages().stream().map(e -> s.matches(e)).filter(f -> f).findFirst().isPresent());
    // return engineFactory.getScriptEngine();
  }

}

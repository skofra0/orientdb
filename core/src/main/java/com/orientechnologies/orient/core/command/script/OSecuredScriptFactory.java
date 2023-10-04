package com.orientechnologies.orient.core.command.script;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.script.ScriptEngineFactory;

public abstract class OSecuredScriptFactory<T extends ScriptEngineFactory> implements ScriptEngineFactory {
  
  protected T engineFactory;

  protected Set<String> packages = new HashSet<>();

  protected OSecuredScriptFactory(T engineFactory) {
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

  public void addAllowedPackages(Set<String> packages) {
    this.packages.addAll(packages);
  }

  public Set<String> getPackages() {
    return packages;
  }

  public void removeAllowedPackages(Set<String> packages) {
    this.packages.removeAll(packages);
  }
}

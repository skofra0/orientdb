package com.orientechnologies.orient.core.command.script.js;

import org.openjdk.nashorn.api.scripting.ClassFilter;
import com.orientechnologies.orient.core.command.script.OSecuredScriptFactory;

public class ONashornClassFilter implements ClassFilter {

  private OSecuredScriptFactory factory;

  public ONashornClassFilter(OSecuredScriptFactory factory) {
    this.factory = factory;
  }

  @Override
  public boolean exposeToScripts(String s) {
    return factory.getPackages().stream().map(e -> s.matches(e)).filter(f -> f).findFirst().isPresent();
  }
}

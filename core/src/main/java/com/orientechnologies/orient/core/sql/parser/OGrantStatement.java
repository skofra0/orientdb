/* Generated By:JJTree: Do not edit this line. OGrantStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.orientechnologies.orient.core.sql.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.sql.executor.OInternalResultSet;
import com.orientechnologies.orient.core.sql.executor.OResultInternal;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

public class OGrantStatement extends OSimpleExecStatement {
  protected OPermission permission;
  protected List<OResourcePathItem> resourceChain = new ArrayList<>();
  protected OIdentifier actor;

  public OGrantStatement(int id) {
    super(id);
  }

  public OGrantStatement(OrientSql p, int id) {
    super(p, id);
  }

  @Override public OResultSet executeSimple(OCommandContext ctx) {
    ORole role = getDatabase().getMetadata().getSecurity().getRole(actor.getStringValue());
    if (role == null)
      throw new OCommandExecutionException("Invalid role: " + actor.getStringValue());

    String resourcePath = toResourcePath(resourceChain, ctx);
    role.grant(resourcePath, toPrivilege(permission.permission));
    role.save();

    OInternalResultSet rs = new OInternalResultSet();
    OResultInternal result = new OResultInternal();
    result.setProperty("operation", "grant");
    result.setProperty("role", actor.getStringValue());
    result.setProperty("permission", permission.toString());
    result.setProperty("resource", resourcePath);
    rs.add(result);
    return rs;
  }

  private String toResourcePath(List<OResourcePathItem> resourceChain, OCommandContext ctx) {
    Map<Object, Object> params = ctx.getInputParameters();
    StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (OResourcePathItem res : resourceChain) {
      if (!first) {
        builder.append(".");
      }
      res.toString(params, builder);
      first = false;
    }
    return builder.toString();
  }

  protected int toPrivilege(String privilegeName) {
    int privilege;
    if ("CREATE".equals(privilegeName))
      privilege = ORole.PERMISSION_CREATE;
    else if ("READ".equals(privilegeName))
      privilege = ORole.PERMISSION_READ;
    else if ("UPDATE".equals(privilegeName))
      privilege = ORole.PERMISSION_UPDATE;
    else if ("DELETE".equals(privilegeName))
      privilege = ORole.PERMISSION_DELETE;
    else if ("EXECUTE".equals(privilegeName))
      privilege = ORole.PERMISSION_EXECUTE;
    else if ("ALL".equals(privilegeName))
      privilege = ORole.PERMISSION_ALL;
    else if ("NONE".equals(privilegeName))
      privilege = ORole.PERMISSION_NONE;
    else
      throw new OCommandExecutionException("Unrecognized privilege '" + privilegeName + "'");
    return privilege;
  }

  @Override public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("GRANT ");
    permission.toString(params, builder);
    builder.append(" ON ");
    boolean first = true;
    for (OResourcePathItem res : resourceChain) {
      if (!first) {
        builder.append(".");
      }
      res.toString(params, builder);
      first = false;
    }
    builder.append(" TO ");
    actor.toString(params, builder);
  }

  @Override public OGrantStatement copy() {
    OGrantStatement result = new OGrantStatement(-1);
    result.permission = permission == null ? null : permission.copy();
    this.resourceChain = resourceChain == null ? null : resourceChain.stream().map(x -> x.copy()).collect(Collectors.toList());
    this.actor = actor == null ? null : actor.copy();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    OGrantStatement that = (OGrantStatement) o;

    if (permission != null ? !permission.equals(that.permission) : that.permission != null)
      return false;
    if (resourceChain != null ? !resourceChain.equals(that.resourceChain) : that.resourceChain != null)
      return false;
    if (actor != null ? !actor.equals(that.actor) : that.actor != null)
      return false;

    return true;
  }

  @Override public int hashCode() {
    int result = permission != null ? permission.hashCode() : 0;
    result = 31 * result + (resourceChain != null ? resourceChain.hashCode() : 0);
    result = 31 * result + (actor != null ? actor.hashCode() : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=c5f7b91e57070a95c6ea490373d16f7f (do not edit this line) */

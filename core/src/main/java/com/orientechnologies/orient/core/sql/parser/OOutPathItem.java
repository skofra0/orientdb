/* Generated By:JJTree: Do not edit this line. OOutPathItem.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.orientechnologies.orient.core.sql.parser;

import java.util.Map;
import com.orientechnologies.orient.core.sql.executor.OResult;

public
class OOutPathItem extends OMatchPathItem {
  public OOutPathItem(int id) {
    super(id);
  }

  public OOutPathItem(OrientSql p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(OrientSqlVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("-");
    boolean first = true;
    if (this.method.params != null) {
      for (OExpression exp : this.method.params) {
        if (!first) {
          builder.append(", ");
        }
        builder.append(exp.execute((OResult) null, null));
        first = false;
      }
    }
    builder.append("->");
    if (filter != null) {
      filter.toString(params, builder);
    }
  }
}
/* JavaCC - OriginalChecksum=b9cd4c40325a129d9166b281866b7a34 (do not edit this line) */

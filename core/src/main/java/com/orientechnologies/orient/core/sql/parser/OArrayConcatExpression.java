/* Generated By:JJTree: Do not edit this line. OArrayConcatExpression.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.orientechnologies.orient.core.sql.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.orientechnologies.common.collection.OMultiValue;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.sql.executor.AggregationContext;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultInternal;

public class OArrayConcatExpression extends SimpleNode {

  List<OArrayConcatExpressionElement> childExpressions = new ArrayList<>();

  public OArrayConcatExpression(int id) {
    super(id);
  }

  public OArrayConcatExpression(OrientSql p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(OrientSqlVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public List<OArrayConcatExpressionElement> getChildExpressions() {
    return childExpressions;
  }

  public void setChildExpressions(List<OArrayConcatExpressionElement> childExpressions) {
    this.childExpressions = childExpressions;
  }

  public Object apply(Object left, Object right) {

    if (left == null && right == null) {
      return null;
    }

    if (right == null) {
      if (OMultiValue.isMultiValue(left)) {
        return left;
      } else {
        return Collections.singletonList(left);
      }
    }

    if (left == null) {
      if (OMultiValue.isMultiValue(right)) {
        return right;
      } else {
        return Collections.singletonList(right);
      }
    }

    List<Object> result = new ArrayList<>();
    if (OMultiValue.isMultiValue(left)) {
      Iterator<Object> leftIter = OMultiValue.getMultiValueIterator(left);
      while (leftIter.hasNext()) {
        result.add(leftIter.next());
      }
    } else {
      result.add(left);
    }

    if (OMultiValue.isMultiValue(right)) {
      Iterator<Object> rigthIter = OMultiValue.getMultiValueIterator(right);
      while (rigthIter.hasNext()) {
        result.add(rigthIter.next());
      }
    } else {
      result.add(right);
    }

    return result;
  }

  public Object execute(OIdentifiable iCurrentRecord, OCommandContext ctx) {
    Object result = childExpressions.get(0).execute(iCurrentRecord, ctx);
    for (int i = 1; i < childExpressions.size(); i++) {
      result = apply(result, childExpressions.get(i).execute(iCurrentRecord, ctx));
    }
    return result;
  }

  public Object execute(OResult iCurrentRecord, OCommandContext ctx) {
    Object result = childExpressions.get(0).execute(iCurrentRecord, ctx);
    for (int i = 1; i < childExpressions.size(); i++) {
      result = apply(result, childExpressions.get(i).execute(iCurrentRecord, ctx));
    }
    return result;
  }

  public boolean isEarlyCalculated(OCommandContext ctx) {
    for (OArrayConcatExpressionElement element : childExpressions) {
      if (!element.isEarlyCalculated(ctx)) {
        return false;
      }
    }
    return true;
  }

  protected boolean supportsBasicCalculation() {
    for (OArrayConcatExpressionElement expr : this.childExpressions) {
      if (!expr.supportsBasicCalculation()) {
        return false;
      }
    }
    return true;
  }

  public boolean needsAliases(Set<String> aliases) {
    for (OArrayConcatExpressionElement expr : childExpressions) {
      if (expr.needsAliases(aliases)) {
        return true;
      }
    }
    return false;
  }

  public boolean isAggregate() {
    for (OArrayConcatExpressionElement expr : this.childExpressions) {
      if (expr.isAggregate()) {
        return true;
      }
    }
    return false;
  }

  public SimpleNode splitForAggregation(AggregateProjectionSplit aggregateProj) {
    if (isAggregate()) {
      throw new OCommandExecutionException("Cannot use aggregate functions in array concatenation");
    } else {
      return this;
    }
  }

  public AggregationContext getAggregationContext(OCommandContext ctx) {
    throw new UnsupportedOperationException("array concatenation expressions do not allow plain aggregation");
  }

  public OArrayConcatExpression copy() {
    OArrayConcatExpression result = new OArrayConcatExpression(-1);
    this.childExpressions.forEach(x -> result.childExpressions.add(x.copy()));
    return result;
  }

  public void extractSubQueries(SubQueryCollector collector) {
    for (OArrayConcatExpressionElement expr : this.childExpressions) {
      expr.extractSubQueries(collector);
    }
  }

  public boolean refersToParent() {
    for (OArrayConcatExpressionElement expr : this.childExpressions) {
      if (expr.refersToParent()) {
        return true;
      }
    }
    return false;
  }

  public List<String> getMatchPatternInvolvedAliases() {
    List<String> result = new ArrayList<>();
    for (OArrayConcatExpressionElement exp : childExpressions) {
      List<String> x = exp.getMatchPatternInvolvedAliases();
      if (x != null) {
        result.addAll(x);
      }
    }
    if (result.size() == 0) {
      return null;
    }
    return result;
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    for (int i = 0; i < childExpressions.size(); i++) {
      if (i > 0) {
        builder.append(" || ");
      }
      childExpressions.get(i).toString(params, builder);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    OArrayConcatExpression that = (OArrayConcatExpression) o;

    return childExpressions != null ? childExpressions.equals(that.childExpressions) : that.childExpressions == null;
  }

  @Override
  public int hashCode() {
    return childExpressions != null ? childExpressions.hashCode() : 0;
  }

  public OResult serialize() {
    OResultInternal result = new OResultInternal();
    if (childExpressions != null) {
      result.setProperty("childExpressions", childExpressions.stream().map(x -> x.serialize()).collect(Collectors.toList()));
    }
    return result;
  }

  public void deserialize(OResult fromResult) {

    if (fromResult.getProperty("childExpressions") != null) {
      List<OResult> ser = fromResult.getProperty("childExpressions");
      childExpressions = new ArrayList<>();
      for (OResult r : ser) {
        OArrayConcatExpressionElement exp = new OArrayConcatExpressionElement(-1);
        exp.deserialize(r);
        childExpressions.add(exp);
      }
    }
  }

  public boolean isCacheable() {
    for (OArrayConcatExpressionElement exp : childExpressions) {
      if (!exp.isCacheable()) {
        return false;
      }
    }
    return true;
  }
}
/* JavaCC - OriginalChecksum=8d976a02f84460bf21c4304009135345 (do not edit this line) */

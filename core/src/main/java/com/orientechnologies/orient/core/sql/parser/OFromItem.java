/* Generated By:JJTree: Do not edit this line. OFromItem.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.orientechnologies.orient.core.sql.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultInternal;

public class OFromItem extends SimpleNode {

  protected List<ORid>            rids;
  protected List<OInputParameter> inputParams;
  protected OCluster              cluster;
  protected OClusterList          clusterList;
  protected OIndexIdentifier      index;
  protected OMetadataIdentifier   metadata;
  protected OStatement            statement;
  protected OInputParameter       inputParam;
  protected OIdentifier           identifier;
  protected OFunctionCall         functionCall;
  protected OModifier             modifier;

  public OFromItem(int id) {
    super(id);
  }

  public OFromItem(OrientSql p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(OrientSqlVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    if (rids != null && rids.size() > 0) {
      if (rids.size() == 1) {
        rids.get(0).toString(params, builder);
        return;
      } else {
        builder.append("[");
        boolean first = true;
        for (ORid rid : rids) {
          if (!first) {
            builder.append(", ");
          }
          rid.toString(params, builder);
          first = false;
        }
        builder.append("]");
        return;
      }
    } else if (inputParams != null && inputParams.size() > 0) {
      if (inputParams.size() == 1) {
        inputParams.get(0).toString(params, builder);
        return;
      } else {
        builder.append("[");
        boolean first = true;
        for (OInputParameter rid : inputParams) {
          if (!first) {
            builder.append(", ");
          }
          rid.toString(params, builder);
          first = false;
        }
        builder.append("]");
        return;
      }
    } else if (cluster != null) {
      cluster.toString(params, builder);
      return;
      // } else if (className != null) {
      // return className.getValue();
    } else if (clusterList != null) {
      clusterList.toString(params, builder);
      return;
    } else if (metadata != null) {
      metadata.toString(params, builder);
      return;
    } else if (statement != null) {
      builder.append("(");
      statement.toString(params, builder);
      builder.append(")");
      return;
    } else if (index != null) {
      index.toString(params, builder);
      return;
    } else if (inputParam != null) {
      inputParam.toString(params, builder);
    } else if (functionCall != null) {
      functionCall.toString(params, builder);
    } else if (identifier != null) {
      identifier.toString(params, builder);
    }
    if (modifier != null) {
      modifier.toString(params, builder);
    }
  }

  public OIdentifier getIdentifier() {
    return identifier;
  }

  public List<ORid> getRids() {
    return rids;
  }

  public OCluster getCluster() {
    return cluster;
  }

  public OClusterList getClusterList() {
    return clusterList;
  }

  public OIndexIdentifier getIndex() {
    return index;
  }

  public OMetadataIdentifier getMetadata() {
    return metadata;
  }

  public OStatement getStatement() {
    return statement;
  }

  public OInputParameter getInputParam() {
    return inputParam;
  }

  public List<OInputParameter> getInputParams() {
    return inputParams;
  }

  public OFunctionCall getFunctionCall() {
    return functionCall;
  }

  public OModifier getModifier() {
    return modifier;
  }

  public OFromItem copy() {
    OFromItem result = new OFromItem(-1);
    if (rids != null) {
      result.rids = rids.stream().map(r -> r.copy()).collect(Collectors.toList());
    }
    if (inputParams != null) {
      result.inputParams = inputParams.stream().map(r -> r.copy()).collect(Collectors.toList());
    }
    result.cluster = cluster == null ? null : cluster.copy();
    result.clusterList = clusterList == null ? null : clusterList.copy();
    result.index = index == null ? null : index.copy();
    result.metadata = metadata == null ? null : metadata.copy();
    result.statement = statement == null ? null : statement.copy();
    result.inputParam = inputParam == null ? null : inputParam.copy();
    result.identifier = identifier == null ? null : identifier.copy();
    result.functionCall = functionCall == null ? null : functionCall.copy();
    result.modifier = modifier == null ? null : modifier.copy();

    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    OFromItem oFromItem = (OFromItem) o;

    if (rids != null ? !rids.equals(oFromItem.rids) : oFromItem.rids != null)
      return false;
    if (inputParams != null ? !inputParams.equals(oFromItem.inputParams) : oFromItem.inputParams != null)
      return false;
    if (cluster != null ? !cluster.equals(oFromItem.cluster) : oFromItem.cluster != null)
      return false;
    if (clusterList != null ? !clusterList.equals(oFromItem.clusterList) : oFromItem.clusterList != null)
      return false;
    if (index != null ? !index.equals(oFromItem.index) : oFromItem.index != null)
      return false;
    if (metadata != null ? !metadata.equals(oFromItem.metadata) : oFromItem.metadata != null)
      return false;
    if (statement != null ? !statement.equals(oFromItem.statement) : oFromItem.statement != null)
      return false;
    if (inputParam != null ? !inputParam.equals(oFromItem.inputParam) : oFromItem.inputParam != null)
      return false;
    if (identifier != null ? !identifier.equals(oFromItem.identifier) : oFromItem.identifier != null)
      return false;
    if (functionCall != null ? !functionCall.equals(oFromItem.functionCall) : oFromItem.functionCall != null)
      return false;
    if (modifier != null ? !modifier.equals(oFromItem.modifier) : oFromItem.modifier != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = rids != null ? rids.hashCode() : 0;
    result = 31 * result + (inputParams != null ? inputParams.hashCode() : 0);
    result = 31 * result + (cluster != null ? cluster.hashCode() : 0);
    result = 31 * result + (clusterList != null ? clusterList.hashCode() : 0);
    result = 31 * result + (index != null ? index.hashCode() : 0);
    result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
    result = 31 * result + (statement != null ? statement.hashCode() : 0);
    result = 31 * result + (inputParam != null ? inputParam.hashCode() : 0);
    result = 31 * result + (identifier != null ? identifier.hashCode() : 0);
    result = 31 * result + (functionCall != null ? functionCall.hashCode() : 0);
    result = 31 * result + (modifier != null ? modifier.hashCode() : 0);
    return result;
  }

  public void setRids(List<ORid> rids) {
    this.rids = rids;
  }

  public void setCluster(OCluster cluster) {
    this.cluster = cluster;
  }

  public void setClusterList(OClusterList clusterList) {
    this.clusterList = clusterList;
  }

  public void setIndex(OIndexIdentifier index) {
    this.index = index;
  }

  public void setMetadata(OMetadataIdentifier metadata) {
    this.metadata = metadata;
  }

  public void setStatement(OStatement statement) {
    this.statement = statement;
  }

  public void setInputParam(OInputParameter inputParam) {
    this.inputParam = inputParam;
  }

  public void setIdentifier(OIdentifier identifier) {
    this.identifier = identifier;
  }

  public void setFunctionCall(OFunctionCall functionCall) {
    this.functionCall = functionCall;
  }

  public void setModifier(OModifier modifier) {
    this.modifier = modifier;
  }

  public void setInputParams(List<OInputParameter> inputParams) {
    this.inputParams = inputParams;
  }

  public OResult serialize() {
    OResultInternal result = new OResultInternal();
    if (rids != null) {
      result.setProperty("rids", rids.stream().map(x -> x.serialize()).collect(Collectors.toList()));
    }
    if (inputParams != null) {
      result.setProperty("inputParams", rids.stream().map(x -> x.serialize()).collect(Collectors.toList()));
    }
    if (cluster != null) {
      result.setProperty("cluster", cluster.serialize());
    }
    if (clusterList != null) {
      result.setProperty("clusterList", clusterList.serialize());
    }
    if (index != null) {
      result.setProperty("index", index.serialize());
    }
    if (metadata != null) {
      result.setProperty("metadata", metadata.serialize());
    }
    if (statement != null) {
      result.setProperty("statement", statement.serialize());
    }
    if (inputParam != null) {
      result.setProperty("inputParam", inputParam.serialize());
    }
    if (identifier != null) {
      result.setProperty("identifier", identifier.serialize());
    }
    if (functionCall != null) {
      result.setProperty("functionCall", functionCall.serialize());
    }
    if (modifier != null) {
      result.setProperty("modifier", modifier.serialize());
    }

    return result;
  }

  public void deserialize(OResult fromResult) {
    if (fromResult.getProperty("rids") != null) {
      List<OResult> serRids = fromResult.getProperty("rids");
      rids = new ArrayList<>();
      for (OResult res : serRids) {
        ORid rid = new ORid(-1);
        rid.deserialize(res);
        rids.add(rid);
      }
    }

    if (fromResult.getProperty("inputParams") != null) {
      List<OResult> ser = fromResult.getProperty("inputParams");
      inputParams = new ArrayList<>();
      for (OResult res : ser) {
        inputParams.add(OInputParameter.deserializeFromOResult(res));
      }
    }

    if (fromResult.getProperty("cluster") != null) {
      cluster = new OCluster(-1);
      cluster.deserialize(fromResult.getProperty("cluster"));
    }
    if (fromResult.getProperty("clusterList") != null) {
      clusterList = new OClusterList(-1);
      clusterList.deserialize(fromResult.getProperty("clusterList"));
    }

    if (fromResult.getProperty("index") != null) {
      index = new OIndexIdentifier(-1);
      index.deserialize(fromResult.getProperty("index"));
    }
    if (fromResult.getProperty("metadata") != null) {
      metadata = new OMetadataIdentifier(-1);
      metadata.deserialize(fromResult.getProperty("metadata"));
    }
    if (fromResult.getProperty("statement") != null) {
      statement = OStatement.deserializeFromOResult(fromResult.getProperty("statement"));
    }
    if (fromResult.getProperty("inputParam") != null) {
      inputParam = OInputParameter.deserializeFromOResult(fromResult.getProperty("inputParam"));
    }
    if (fromResult.getProperty("identifier") != null) {
      identifier = OIdentifier.deserialize(fromResult.getProperty("identifier"));
    }
    if (fromResult.getProperty("functionCall") != null) {
      functionCall = new OFunctionCall(-1);
      functionCall.deserialize(fromResult.getProperty("functionCall"));
    }
    if (fromResult.getProperty("modifier") != null) {
      modifier = new OModifier(-1);
      modifier.deserialize(fromResult.getProperty("modifier"));
    }
  }

  public boolean isCacheable() {
    if (modifier != null) {
      return false;
    }
    if (inputParam != null) {
      return false;
    }
    if (inputParams != null && !inputParams.isEmpty()) {
      return false;
    }
    if (statement != null) {
      return statement.executinPlanCanBeCached();
    }
    if (functionCall != null) {
      return functionCall.isCacheable();
    }

    return true;
  }

  public boolean refersToParent() {
    if (modifier != null && modifier.refersToParent()) {
      return true;
    }
    if (statement != null && statement.refersToParent()) {
      return true;
    }
    if (functionCall != null && functionCall.refersToParent()) {
      return true;
    }
    return false;
  }
}
/* JavaCC - OriginalChecksum=f64e3b4d2a2627a1b5d04a7dcb95fa94 (do not edit this line) */

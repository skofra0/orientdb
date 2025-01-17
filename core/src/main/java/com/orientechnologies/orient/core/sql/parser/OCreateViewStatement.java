/* Generated By:JJTree: Do not edit this line. OCreateViewStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.orientechnologies.orient.core.sql.parser;

import com.orientechnologies.orient.core.command.OBasicCommandContext;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OViewConfig;
import com.orientechnologies.orient.core.sql.OCommandSQLParsingException;
import com.orientechnologies.orient.core.sql.executor.OResultInternal;
import com.orientechnologies.orient.core.sql.executor.resultset.OExecutionStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OCreateViewStatement extends ODDLStatement {

  protected OIdentifier name;
  protected OStatement statement;
  protected boolean ifNotExists = false;
  protected OJson metadata;

  public OCreateViewStatement(int id) {
    super(id);
  }

  public OCreateViewStatement(OrientSql p, int id) {
    super(p, id);
  }

  @Override
  public OExecutionStream executeDDL(OCommandContext ctx) {
    OSchema schema = ctx.getDatabase().getMetadata().getSchema();
    if (schema.existsView(name.getStringValue())) {
      if (ifNotExists) {
        return OExecutionStream.empty();
      } else {
        throw new OCommandExecutionException("View " + name + " already exists");
      }
    }

    if (schema.existsClass(name.getStringValue())) {
      throw new OCommandExecutionException(
          "Cannot create view " + name + ", a class with the same name already exists");
    }

    OResultInternal result = new OResultInternal();
    result.setProperty("operation", "create view");
    result.setProperty("viewName", name.getStringValue());

    schema.createView(
        (ODatabaseDocumentInternal) ctx.getDatabase(),
        name.getStringValue(),
        statement.toString(),
        metadata == null ? new HashMap<>() : metadata.toMap(new OResultInternal(), ctx));

    return OExecutionStream.singleton(result);
  }

  public void checkMetadataSyntax() throws OCommandSQLParsingException {
    Set<String> validAttributes = new HashSet<>();
    validAttributes.add("updatable");
    validAttributes.add("updateIntervalSeconds");
    validAttributes.add("updateStrategy");
    validAttributes.add("watchClasses");
    validAttributes.add("originRidField");
    validAttributes.add("nodes");
    validAttributes.add("indexes");
    if (metadata == null) {
      return;
    }
    Map<String, Object> metadataMap =
        metadata.toMap(new OResultInternal(), new OBasicCommandContext());
    for (Map.Entry<String, Object> s : metadataMap.entrySet()) {

      String key = s.getKey();
      Object value = s.getValue();
      switch (key) {
        case "updatable":
          if (!(value instanceof Boolean)) {
            throw new OCommandSQLParsingException(
                "Invalid value for view metadata: updatable should be true or false, it is "
                    + value);
          }
          if (Boolean.TRUE.equals(value)) {
            if (!metadataMap.containsKey("originRidField"))
              throw new OCommandSQLParsingException(
                  "Updatable view needs a originRidField defined");
          }
          break;
        case "updateIntervalSeconds":
          if (!(value instanceof Number)) {
            throw new OCommandSQLParsingException(
                "Invalid value for view metadata: updateIntervalSeconds should be a number, it is "
                    + value);
          }
          break;
        case "updateStrategy":
          if (!(OViewConfig.UPDATE_STRATEGY_BATCH.equals(value)
              || OViewConfig.UPDATE_STRATEGY_LIVE.equals(value))) {
            throw new OCommandSQLParsingException(
                "Invalid value for view metadata: updateStrategy should be "
                    + OViewConfig.UPDATE_STRATEGY_LIVE
                    + " or "
                    + OViewConfig.UPDATE_STRATEGY_BATCH
                    + ", it is "
                    + value);
          }
          break;
        case "watchClasses":
          if (!(value instanceof Collection)) {
            throw new OCommandSQLParsingException(
                "Invalid value for view metadata: watchClasses should be a list of class names as strings, it is "
                    + value);
          }
          if (((Collection) value).stream().anyMatch(x -> !(x instanceof String))) {
            throw new OCommandSQLParsingException(
                "Invalid value for view metadata: watchClasses should be a list of class names as strings, one value is null");
          }
          break;
        case "originRidField":
          if (!(value instanceof String)) {
            throw new OCommandSQLParsingException(
                "Invalid value for view metadata: updateStrategy should be a string, it is "
                    + value);
          }
          break;
        case "nodes":
          if (!(value instanceof Collection)) {
            throw new OCommandSQLParsingException(
                "Invalid value for view metadata: nodes should be a list of class names as strings, it is "
                    + value);
          }
          if (((Collection) value).stream().anyMatch(x -> !(x instanceof String))) {
            throw new OCommandSQLParsingException(
                "Invalid value for view metadata: nodes should be a list of class names as strings, one value is null");
          }
          break;
        case "indexes":
          if (!(value instanceof Collection)) {
            throw new OCommandSQLParsingException(
                "Invalid value for view metadata: indexes should be a list of class names as strings, it is "
                    + value);
          }
          for (Object o : (Collection) value) {
            if (!(o instanceof Map)) {
              throw new OCommandSQLParsingException(
                  "Invalid value for view metadata: index configuration should be as follows: {type:'<index_type>', engine:'<engine_name>', properties:{propName1:'<type>', propNameN:'<type'>}}. Engine is optional");
            }
            Map<String, Object> valueMap = (Map<String, Object>) o;
            for (String idxKey : valueMap.keySet()) {
              switch (idxKey) {
                case "type":
                case "engine":
                case "properties":
                  break;

                default:
                  throw new OCommandSQLParsingException(
                      "Invalid key for view index metadata: "
                          + idxKey
                          + ". Valid keys are 'type', 'engine', 'properties'");
              }
            }
          }
          break;

        default:
          throw new OCommandSQLParsingException("Invalid metadata for view: " + key);
      }
    }
  }

  @Override
  public OStatement copy() {
    OCreateViewStatement result = new OCreateViewStatement(-1);
    result.name = this.name.copy();
    result.statement = this.statement.copy();
    result.ifNotExists = this.ifNotExists;
    result.metadata = metadata == null ? null : metadata.copy();
    return result;
  }

  @Override
  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("CREATE VIEW ");
    name.toString(params, builder);
    if (ifNotExists) {
      builder.append(" IF NOT EXISTS");
    }
    builder.append(" FROM (");
    statement.toString(params, builder);
    builder.append(")");
    if (metadata != null) {
      builder.append(" METADATA ");
      metadata.toString(params, builder);
    }
  }

  @Override
  public void toGenericStatement(StringBuilder builder) {
    builder.append("CREATE VIEW ");
    name.toGenericStatement(builder);
    if (ifNotExists) {
      builder.append(" IF NOT EXISTS");
    }
    builder.append(" FROM (");
    statement.toGenericStatement(builder);
    builder.append(")");
    if (metadata != null) {
      builder.append(" METADATA ");
      metadata.toGenericStatement(builder);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    OCreateViewStatement that = (OCreateViewStatement) o;

    if (ifNotExists != that.ifNotExists) return false;
    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    if (statement != null ? !statement.equals(that.statement) : that.statement != null)
      return false;
    return metadata != null ? metadata.equals(that.metadata) : that.metadata == null;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (statement != null ? statement.hashCode() : 0);
    result = 31 * result + (ifNotExists ? 1 : 0);
    result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=a89bf0075fd907075ece2ef20ed1494a (do not edit this line) */

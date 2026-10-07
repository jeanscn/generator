package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.core.db.query.DatabaseType;
import org.apache.commons.lang3.ObjectUtils;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;

import java.util.List;

/**
 * @author cen_c
 */
public class InsertByTableElementGenerator extends AbstractXmlElementGenerator {
    private enum ManagedRelationReadGroup {
        /** Row locking reads that bypass old transaction snapshots. */
        FOR_UPDATE,
        /** SQL Server update/range locks, including snapshot-enabled installations. */
        SQL_SERVER_LOCK_HINT,
        /** SQLite's preceding insert establishes a serialized writer transaction. */
        SQLITE_WRITER
    }

    private ManagedRelationReadGroup managedReadGroup(DatabaseType database) {
        return switch (database) {
            case MYSQL, MARIADB, ORACLE, POSTGRE_SQL, DM, HIGHGO, KINGBASE_ES, GAUSS_DB, UXDB, H2, HSQL ->
                    ManagedRelationReadGroup.FOR_UPDATE;
            case SQL_SERVER, SQL_SERVER2005 -> ManagedRelationReadGroup.SQL_SERVER_LOCK_HINT;
            case SQLITE -> ManagedRelationReadGroup.SQLITE_WRITER;
            default -> throw new IllegalArgumentException("Managed relation current-read/savepoint semantics are not verified for database: "
                    + database.getCode());
        };
    }
    private enum InsertByTableDialectGroup {
        /** MySQL family: INSERT IGNORE INTO ... */
        MYSQL_INSERT_IGNORE,
        /** PostgreSQL family: INSERT INTO ... ON CONFLICT DO NOTHING */
        POSTGRES_ON_CONFLICT,
        /** Oracle-like fallback: INSERT ... SELECT ... FROM dual WHERE NOT EXISTS (...) */
        INSERT_SELECT_NOT_EXISTS_FROM_DUAL,
        /** DB2 fallback: INSERT ... SELECT ... FROM SYSIBM.SYSDUMMY1 WHERE NOT EXISTS (...) */
        INSERT_SELECT_NOT_EXISTS_FROM_DUMMY1,
        /** Generic fallback: INSERT ... SELECT ... WHERE NOT EXISTS (...) */
        INSERT_SELECT_NOT_EXISTS
    }

    public InsertByTableElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        final List<SelectByTableGeneratorConfiguration> configurations = introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration();
        if (configurations.stream().noneMatch(SelectByTableGeneratorConfiguration::isEnableUnion)) {
            return;
        }
        if (configurations.stream().anyMatch(configuration -> configuration.isEnableUnion() && configuration.isManagedRelation())) {
            introspectedTable.getTableConfiguration().getAdditionalDbTypes().forEach(this::managedReadGroup);
        }
        for (SelectByTableGeneratorConfiguration configuration : configurations) {
            if (!configuration.isEnableUnion()) {
                continue;
            }
            if (ObjectUtils.anyNull(configuration.getTableName(), configuration.getPrimaryKeyColumn(), configuration.getOtherPrimaryKeyColumn())) {
                continue;
            }
            for (DatabaseType dbType : introspectedTable.getTableConfiguration().getAdditionalDbTypes()) {
                if (configuration.isManagedRelation()) {
                    XmlElement row = new XmlElement("insert");
                    row.addAttribute(new Attribute("id", configuration.getRowInsertMethodName()));
                    row.addAttribute(new Attribute("parameterType", "java.util.Map"));
                    row.addAttribute(new Attribute("databaseId", dbType.getCode()));
                    context.getCommentGenerator().addComment(row);
                    row.addElement(new TextElement("insert into " + configuration.getTableName() + " ("
                            + DefaultColumnNameEnum.ID.columnName() + ", " + configuration.getPrimaryKeyColumn()
                            + ", " + configuration.getOtherPrimaryKeyColumn() + ")"));
                    row.addElement(new TextElement("values (#{relationId,jdbcType=VARCHAR}, "
                            + MyBatis3FormattingUtilities.getParameterClause(configuration.getThisColumn()) + ", "
                            + MyBatis3FormattingUtilities.getParameterClause(configuration.getOtherColumn()) + ")"));
                    parentElement.addElement(row);
                    XmlElement exists = new XmlElement("select");
                    exists.addAttribute(new Attribute("id", configuration.getExistsMethodName()));
                    exists.addAttribute(new Attribute("databaseId", dbType.getCode()));
                    exists.addAttribute(new Attribute("resultType", "java.lang.String"));
                    exists.addAttribute(new Attribute("useCache", "false"));
                    exists.addAttribute(new Attribute("flushCache", "true"));
                    context.getCommentGenerator().addComment(exists);
                    ManagedRelationReadGroup readGroup = managedReadGroup(dbType);
                    String lockHint = readGroup == ManagedRelationReadGroup.SQL_SERVER_LOCK_HINT
                            ? " with (updlock, holdlock)" : "";
                    exists.addElement(new TextElement("select " + DefaultColumnNameEnum.ID.columnName()
                            + " from " + configuration.getTableName() + lockHint
                            + " where " + configuration.getPrimaryKeyColumn() + " = "
                            + MyBatis3FormattingUtilities.getParameterClause(configuration.getThisColumn())
                            + " and " + configuration.getOtherPrimaryKeyColumn() + " = "
                            + MyBatis3FormattingUtilities.getParameterClause(configuration.getOtherColumn())));
                    switch (readGroup) {
                        case FOR_UPDATE -> exists.addElement(new TextElement("for update"));
                        case SQL_SERVER_LOCK_HINT, SQLITE_WRITER -> {
                            // The table hint or preceding SQLite write supplies current-read semantics.
                        }
                        default -> throw new IllegalStateException("Unknown managed relation read group: " + readGroup);
                    }
                    parentElement.addElement(exists);
                    continue;
                }
                XmlElement answer = buildInitialInsert(configuration.getUnionMethodName(), FullyQualifiedJavaType.getStringInstance(), dbType);
                InsertByTableDialectGroup dialectGroup = resolveDialectGroup(dbType);
                switch (dialectGroup) {
                    case POSTGRES_ON_CONFLICT:
                        answer.addElement(new TextElement("insert into " + configuration.getTableName()));
                        break;
                    case MYSQL_INSERT_IGNORE:
                        answer.addElement(new TextElement("insert ignore into " + configuration.getTableName()));
                        break;
                    case INSERT_SELECT_NOT_EXISTS_FROM_DUAL:
                        addInsertSelectNotExists(answer, configuration, "from dual");
                        parentElement.addElement(answer);
                        continue;
                    case INSERT_SELECT_NOT_EXISTS_FROM_DUMMY1:
                        addInsertSelectNotExists(answer, configuration, "from SYSIBM.SYSDUMMY1");
                        parentElement.addElement(answer);
                        continue;
                    case INSERT_SELECT_NOT_EXISTS:
                        addInsertSelectNotExists(answer, configuration, "");
                        parentElement.addElement(answer);
                        continue;
                    default:
                        throw new IllegalStateException("Unexpected insertByTable dialect group: " + dialectGroup.name());
                }

                StringBuilder sb = new StringBuilder("(");
                sb.append(DefaultColumnNameEnum.ID.columnName())
                        .append(",")
                        .append(configuration.getPrimaryKeyColumn())
                        .append(",")
                        .append(configuration.getOtherPrimaryKeyColumn());
                sb.append(")");
                answer.addElement(new TextElement(sb.toString()));
                answer.addElement(new TextElement("values"));

                XmlElement foreachListField = new XmlElement("foreach");
                foreachListField.addAttribute(new Attribute("collection", configuration.getOtherColumn().getJavaProperty() + "s"));
                foreachListField.addAttribute(new Attribute("item", configuration.getOtherColumn().getJavaProperty()));
                foreachListField.addAttribute(new Attribute("index", "index"));
                foreachListField.addAttribute(new Attribute("separator", ","));
                foreachListField.addAttribute(new Attribute("open", ""));
                foreachListField.addAttribute(new Attribute("close", ""));
                sb.setLength(0);
                sb.append("(md5( concat(");
                sb.append(MyBatis3FormattingUtilities.getParameterClause(configuration.getThisColumn()));
                sb.append(",");
                sb.append(MyBatis3FormattingUtilities.getParameterClause(configuration.getOtherColumn()));
                sb.append(")),");
                sb.append(MyBatis3FormattingUtilities.getParameterClause(configuration.getThisColumn()));
                sb.append(",");
                sb.append(MyBatis3FormattingUtilities.getParameterClause(configuration.getOtherColumn()));
                sb.append(")");
                foreachListField.addElement(new TextElement(sb.toString()));
                answer.addElement(foreachListField);

                if (dialectGroup == InsertByTableDialectGroup.POSTGRES_ON_CONFLICT) {
                    answer.addElement(new TextElement("on conflict do nothing"));
                }
                parentElement.addElement(answer);
            }
        }
    }

    private void addInsertSelectNotExists(XmlElement answer, SelectByTableGeneratorConfiguration configuration, String rowSelectFromClause) {
        answer.addElement(new TextElement("insert into " + configuration.getTableName()));

        StringBuilder sb = new StringBuilder("(");
        sb.append(DefaultColumnNameEnum.ID.columnName())
                .append(",")
                .append(configuration.getPrimaryKeyColumn())
                .append(",")
                .append(configuration.getOtherPrimaryKeyColumn())
                .append(")");
        answer.addElement(new TextElement(sb.toString()));

        answer.addElement(new TextElement("select distinct src.union_id, src.this_key, src.other_key"));
        answer.addElement(new TextElement("from ("));

        XmlElement foreachListField = new XmlElement("foreach");
        foreachListField.addAttribute(new Attribute("collection", configuration.getOtherColumn().getJavaProperty() + "s"));
        foreachListField.addAttribute(new Attribute("item", configuration.getOtherColumn().getJavaProperty()));
        foreachListField.addAttribute(new Attribute("separator", " union all "));
        foreachListField.addAttribute(new Attribute("open", ""));
        foreachListField.addAttribute(new Attribute("close", ""));

        sb.setLength(0);
        sb.append("select md5( concat(");
        sb.append(MyBatis3FormattingUtilities.getParameterClause(configuration.getThisColumn()));
        sb.append(",");
        sb.append(MyBatis3FormattingUtilities.getParameterClause(configuration.getOtherColumn()));
        sb.append(")) as union_id, ");
        sb.append(MyBatis3FormattingUtilities.getParameterClause(configuration.getThisColumn()));
        sb.append(" as this_key, ");
        sb.append(MyBatis3FormattingUtilities.getParameterClause(configuration.getOtherColumn()));
        sb.append(" as other_key");
        if (!rowSelectFromClause.isEmpty()) {
            sb.append(" ");
            sb.append(rowSelectFromClause);
        }
        foreachListField.addElement(new TextElement(sb.toString()));
        answer.addElement(foreachListField);

        answer.addElement(new TextElement(") src"));
        answer.addElement(new TextElement("where not exists ("));
        answer.addElement(new TextElement("select 1 from " + configuration.getTableName() + " t"));
        answer.addElement(new TextElement("where t." + configuration.getPrimaryKeyColumn() + " = src.this_key"));
        answer.addElement(new TextElement("and t." + configuration.getOtherPrimaryKeyColumn() + " = src.other_key"));
        answer.addElement(new TextElement(")"));
    }

    private InsertByTableDialectGroup resolveDialectGroup(DatabaseType dbType) {
        return switch (dbType) {
            case POSTGRE_SQL, KINGBASE_ES, HIGHGO, SQLITE -> InsertByTableDialectGroup.POSTGRES_ON_CONFLICT;
            case MYSQL, MARIADB -> InsertByTableDialectGroup.MYSQL_INSERT_IGNORE;
            case ORACLE, DM, XU_GU, CACHEDB -> InsertByTableDialectGroup.INSERT_SELECT_NOT_EXISTS_FROM_DUAL;
            case DB2 -> InsertByTableDialectGroup.INSERT_SELECT_NOT_EXISTS_FROM_DUMMY1;
            default -> InsertByTableDialectGroup.INSERT_SELECT_NOT_EXISTS;
        };
    }
}

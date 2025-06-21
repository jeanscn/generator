package org.mybatis.generator.api;

import com.vgosoft.core.constant.enums.db.DDLDefaultValueEnum;
import com.vgosoft.core.db.enums.JDBCTypeTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.util.StringUtility;

import java.sql.JDBCType;
import java.sql.Types;
import java.util.Properties;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * This class holds information about an introspected column.
 *
 * @author Jeff Butler
 */
@Getter
@Setter
public class IntrospectedColumn {

    protected String actualColumnName;

    protected int jdbcType;

    /**
     * The platform specific data type name as reported from DatabaseMetadata.getColumns()
     */
    protected String actualTypeName;

    protected String jdbcTypeName;

    protected boolean nullable;

    protected int length;

    protected int scale;

    protected int minLength;

    protected boolean identity;

    protected boolean isSequenceColumn;

    protected boolean isForeignKey;

    protected String javaProperty;

    protected FullyQualifiedJavaType fullyQualifiedJavaType;

    protected String tableAlias;

    protected String typeHandler;

    protected Context context;

    protected boolean isColumnNameDelimited;

    protected IntrospectedTable introspectedTable;

    protected final Properties properties;

    // any database comment associated with this column. May be null
    protected String remarks;

    protected String subRemarks;

    protected String defaultValue;

    /**
     * true if the JDBC driver reports that this column is auto-increment.
     */
    protected boolean isAutoIncrement;

    /**
     * true if the JDBC driver reports that this column is generated.
     */
    protected boolean isGeneratedColumn;

    /**
     * True if there is a column override that defines this column as GENERATED ALWAYS.
     */
    protected boolean isGeneratedAlways;

    /*
     * Constructs a Column definition. This object holds all the information
     * about a column that is required to generate Java objects and SQL maps;
     */

    /**
     * 额外增加：位置标识
     * 列在表格中的位置，在创建或编辑列时使用
     */
    protected String position;

    protected int order = 10;

    protected boolean beValidated = false;

    protected boolean isPrimaryKey = false;

    public IntrospectedColumn() {
        super();
        properties = new Properties();
    }

    /*
     * This method is primarily used for debugging, so we don't externalize the
     * strings
     */
    @Override
    public String toString() {
        return "Actual Column Name: " //$NON-NLS-1$
                + actualColumnName
                + ", JDBC Type: " //$NON-NLS-1$
                + jdbcType
                + ", Nullable: " //$NON-NLS-1$
                + nullable
                + ", Length: " //$NON-NLS-1$
                + length
                + ", Scale: " //$NON-NLS-1$
                + scale
                + ", Identity: " //$NON-NLS-1$
                + identity;
    }

    public void setActualColumnName(String actualColumnName) {
        this.actualColumnName = actualColumnName;
        isColumnNameDelimited = StringUtility
                .stringContainsSpace(actualColumnName);
    }

    public boolean isBLOBColumn() {
        String typeName = getJdbcTypeName();

        return "BINARY".equals(typeName) || "BLOB".equals(typeName) || "CLOB".equals(typeName)
                || "LONGVARBINARY".equals(typeName) || "NCLOB".equals(typeName) || "VARBINARY".equals(typeName);
    }

    public boolean isLongVarchar() {
        //-1:LONGVARCHAR, -16:LONGNVARCHAR
        return this.getJdbcType() == -1 || this.getJdbcType() == -16;
    }

    public boolean isStringColumn() {
        return fullyQualifiedJavaType.equals(FullyQualifiedJavaType
                .getStringInstance());
    }

    public boolean isJdbcCharacterColumn() {
        return jdbcType == Types.CHAR || jdbcType == Types.CLOB
                || jdbcType == Types.LONGVARCHAR || jdbcType == Types.VARCHAR
                || jdbcType == Types.LONGNVARCHAR || jdbcType == Types.NCHAR
                || jdbcType == Types.NCLOB || jdbcType == Types.NVARCHAR;
    }

    public boolean isDateColumn() {
        return jdbcType == Types.DATE;
    }

    public boolean isTimeColumn() {
        return jdbcType == Types.TIME;
    }

    public boolean isTimestampColumn() {
        return jdbcType == Types.TIMESTAMP;
    }

    public boolean isBigDecimalColumn() {
        return jdbcType == Types.DECIMAL || jdbcType == Types.NUMERIC;
    }

    public boolean isBytesColumn() {
        return jdbcType == Types.BINARY || jdbcType == Types.VARBINARY
                || jdbcType == Types.LONGVARBINARY;
    }

    public boolean isNumericColumn() {
        return jdbcType == Types.BIGINT || jdbcType == Types.DECIMAL
                || jdbcType == Types.DOUBLE || jdbcType == Types.FLOAT
                || jdbcType == Types.INTEGER || jdbcType == Types.NUMERIC
                || jdbcType == Types.REAL || jdbcType == Types.SMALLINT
                || jdbcType == Types.TINYINT;
    }

    public boolean isBooleanColumn() {
        return jdbcType == Types.BIT || jdbcType == Types.BOOLEAN;
    }

    public String getJavaProperty() {
        return getJavaProperty(null);
    }

    public String getJavaProperty(String prefix) {
        if (prefix == null) {
            return javaProperty;
        }

        return prefix + javaProperty;
    }

    public boolean isJDBCDateColumn() {
        return fullyQualifiedJavaType.equals(FullyQualifiedJavaType.getDateInstance())
                && "DATE".equalsIgnoreCase(jdbcTypeName);
    }

    public boolean isJDBCTimeColumn() {
        return fullyQualifiedJavaType.equals(FullyQualifiedJavaType.getDateInstance())
                && "TIME".equalsIgnoreCase(jdbcTypeName); //$NON-NLS-1$
    }

    public boolean isJDBCTimeStampColumn() {
        return fullyQualifiedJavaType.equals(FullyQualifiedJavaType.getDateInstance())
                && "TIMESTAMP".equalsIgnoreCase(jdbcTypeName); //$NON-NLS-1$
    }

    public boolean isJavaLocalDateColumn() {
        return fullyQualifiedJavaType.equals(new FullyQualifiedJavaType("java.time.LocalDate"));
    }

    public boolean isJavaLocalDateTimeColumn() {
        return fullyQualifiedJavaType.equals(new FullyQualifiedJavaType("java.time.LocalDateTime"));
    }

    public boolean isJavaLocalTimeColumn() {
        return fullyQualifiedJavaType.equals(new FullyQualifiedJavaType("java.time.LocalTime"));
    }

    public boolean isJava8TimeColumn() {
        return fullyQualifiedJavaType.equals(new FullyQualifiedJavaType("java.time.LocalDate"))
                || fullyQualifiedJavaType.equals(new FullyQualifiedJavaType("java.time.LocalDateTime"))
                || fullyQualifiedJavaType.equals(new FullyQualifiedJavaType("java.time.LocalTime"));
    }

    public void setColumnNameDelimited(boolean isColumnNameDelimited) {
        this.isColumnNameDelimited = isColumnNameDelimited;
    }

    public boolean isColumnNameDelimited() {
        return isColumnNameDelimited;
    }

    public String getJdbcTypeName() {
        if (jdbcTypeName == null) {
            return "OTHER"; //$NON-NLS-1$
        }

        return jdbcTypeName;
    }

    public void setProperties(Properties properties) {
        this.properties.putAll(properties);
    }


    public void addProperty(String key, String value) {
        if (stringHasValue(value)) {
            this.properties.setProperty(key, value);
        } else {
            this.properties.remove(key);
        }
    }

    /**
     * 获得列注释
     *
     * @param simple 是否格式化为短标签。
     *               false-获得完整注释
     *               true-格式为短标签。
     */
    public String getRemarks(boolean simple) {
        return simple ? StringUtility.remarkLeft(remarks) : remarks;
    }

    public boolean isSequenceColumn() {
        return isSequenceColumn;
    }

    public void setSequenceColumn(boolean isSequenceColumn) {
        this.isSequenceColumn = isSequenceColumn;
    }

    public boolean isAutoIncrement() {
        return isAutoIncrement;
    }

    public void setAutoIncrement(boolean isAutoIncrement) {
        this.isAutoIncrement = isAutoIncrement;
    }

    public boolean isGeneratedColumn() {
        return isGeneratedColumn;
    }

    public void setGeneratedColumn(boolean isGeneratedColumn) {
        this.isGeneratedColumn = isGeneratedColumn;
    }

    public boolean isGeneratedAlways() {
        return isGeneratedAlways;
    }

    public void setGeneratedAlways(boolean isGeneratedAlways) {
        this.isGeneratedAlways = isGeneratedAlways;
    }

    /**
     * The platform specific type name as reported by the JDBC driver. This value is determined
     * from the DatabaseMetadata.getColumns() call - specifically ResultSet.getString("TYPE_NAME").
     * This value is platform dependent.
     *
     * @return the platform specific type name as reported by the JDBC driver
     */

    public String getDatePattern() {
        switch (this.fullyQualifiedJavaType.getShortName()) {
            case "LocalDate":
                return "yyyy-MM-dd";
            case "LocalDateTime":
                return "yyyy-MM-dd HH:mm:ss";
            case "Instant":
                return "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
            case "ZonedDateTime":
                return "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
            case "LocalTime":
                return "HH:mm:ss";
            case "Date":
                return "yyyy-MM-dd HH:mm:ss.SSS";
            default:
                return "";
        }
    }

    public String getSqlFragmentLength() {
        if (this.getActualTypeName().toUpperCase().contains("UNSIGNED")) {
            return "";
        }
        StringBuilder sb_length = new StringBuilder();
        if (!(this.isJDBCDateColumn() || this.isJDBCTimeColumn() || this.isJDBCTimeStampColumn() || this.isBLOBColumn() || this.isJava8TimeColumn() || this.isLongVarchar())) {
            sb_length.append("(");
            sb_length.append(this.length);
            if (this.scale > 0) {
                sb_length.append(",").append(this.scale);
            }
            sb_length.append(") ");
        } else {
            sb_length.append(" ");
        }
        return sb_length.toString();
    }

    public String getSqlFragmentNotNull() {
        StringBuilder not_null = new StringBuilder();
        if (!this.isNullable()) {
            not_null.append("NOT NULL ");
        } else {
            not_null.append("NULL ");
        }
        JDBCTypeTypeEnum jdbcTypeType = JDBCTypeTypeEnum.getJDBCTypeType(JDBCType.valueOf(this.jdbcType));
        not_null.append(getDefaultValueString(jdbcTypeType, this.defaultValue));
        return not_null.toString();
    }

    private String getDefaultValueString(JDBCTypeTypeEnum jdbcTypeType, String defaultValue) {
        if (VStringUtil.stringHasValue(defaultValue)) {
            if (jdbcTypeType.equals(JDBCTypeTypeEnum.CHARACTER)) {
                if (defaultValue.startsWith("'") && defaultValue.endsWith("'")) {
                    return " DEFAULT " + defaultValue + " ";
                } else {
                    return " DEFAULT '" + defaultValue + "' ";
                }
            } else if (jdbcTypeType.equals(JDBCTypeTypeEnum.DATETIME)) {
                if (defaultValue.startsWith("CURRENT_")) {
                    return " DEFAULT (" + defaultValue + ") ";
                } else if (defaultValue.startsWith("'now")) {
                    return " DEFAULT "+ DDLDefaultValueEnum.CURRENT_DATETIME_EXPR.code() +" ";
                } else if (defaultValue.startsWith("'curdate")) {
                    return " DEFAULT "+ DDLDefaultValueEnum.CURRENT_DATE_EXPR.code() +" ";
                } else if (defaultValue.startsWith("'curtime")) {
                    return " DEFAULT "+ DDLDefaultValueEnum.CURRENT_TIME_EXPR.code() +" ";
                } else {
                    return " DEFAULT '" + defaultValue + "' ";
                }
            } else {
                return " DEFAULT " + defaultValue + " ";
            }
        } else {
            if (jdbcTypeType.equals(JDBCTypeTypeEnum.CHARACTER)) {
                return " DEFAULT '' ";
            } else if (jdbcTypeType.equals(JDBCTypeTypeEnum.DATETIME)) {
                return " DEFAULT " + DDLDefaultValueEnum.CURRENT_DATETIME_EXPR.code() + " ";
            } else if (jdbcTypeType.equals(JDBCTypeTypeEnum.NUMERIC)) {
                return " DEFAULT 0 ";
            } else {
                return " ";
            }
        }
    }


    public boolean isForeignKey() {
        return isForeignKey;
    }

    public void setForeignKey(boolean foreignKey) {
        isForeignKey = foreignKey;
    }

    public boolean isRequired() {
        return !this.isNullable() && !this.isAutoIncrement() && !this.isGeneratedColumn() && !this.isSequenceColumn();
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }
}

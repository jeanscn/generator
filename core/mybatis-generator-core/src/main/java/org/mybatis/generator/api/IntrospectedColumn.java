package org.mybatis.generator.api;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.Context;
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

    protected AbstractIntrospectedTable introspectedTable;

    protected final Properties properties;

    /**
     * any database comment associated with this column. May be null
     */
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

    /**
     * This method is primarily used for debugging, so we don't externalize the
     * strings
     */
    @Override
    public String toString() {
        return "Actual Column Name: "
                + actualColumnName
                + ", JDBC Type: "
                + jdbcType
                + ", Nullable: "
                + nullable
                + ", Length: "
                + length
                + ", Scale: "
                + scale
                + ", Identity: "
                + identity;
    }

    public void setActualColumnName(String actualColumnName) {
        this.actualColumnName = actualColumnName;
        isColumnNameDelimited = StringUtility
                .stringContainsSpace(actualColumnName);
    }

    public boolean isBlobColumn() {
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

    public boolean isJdbcDateColumn() {
        return fullyQualifiedJavaType.equals(FullyQualifiedJavaType.getDateInstance())
                && JDBCType.DATE.getName().equalsIgnoreCase(jdbcTypeName);
    }

    public boolean isJdbcTimeColumn() {
        return fullyQualifiedJavaType.equals(FullyQualifiedJavaType.getDateInstance())
                && "TIME".equalsIgnoreCase(jdbcTypeName);
    }

    public boolean isJdbcTimeStampColumn() {
        return fullyQualifiedJavaType.equals(FullyQualifiedJavaType.getDateInstance())
                && "TIMESTAMP".equalsIgnoreCase(jdbcTypeName);
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

    public boolean isIntegerColumn() {
        return fullyQualifiedJavaType.equals(new FullyQualifiedJavaType("java.lang.Integer"));
    }

    public String getJdbcTypeName() {
        if (jdbcTypeName == null) {
            return "OTHER";
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
        String remarks = simple ? StringUtility.remarkLeft(this.remarks) : this.remarks;
        return VStringUtil.stringHasValue(remarks) ? remarks : "--";
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
        StringBuilder sbLength = new StringBuilder();
        if (!(this.isJdbcDateColumn() || this.isJdbcTimeColumn() || this.isJdbcTimeStampColumn() || this.isBlobColumn()
                || this.isJava8TimeColumn() || this.isLongVarchar() || this.isIntegerColumn())) {
            sbLength.append("(");
            sbLength.append(this.length);
            if (this.scale > 0) {
                sbLength.append(",").append(this.scale);
            }
            sbLength.append(") ");
        } else {
            sbLength.append(" ");
        }
        return sbLength.toString();
    }

    public boolean isRequired() {
        return !this.isNullable() && !this.isAutoIncrement() && !this.isGeneratedColumn() && !this.isSequenceColumn();
    }

}

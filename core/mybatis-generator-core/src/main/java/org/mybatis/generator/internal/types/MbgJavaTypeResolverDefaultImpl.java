package org.mybatis.generator.internal.types;

import com.vgosoft.core.db.query.DatabaseType;
                            import com.vgosoft.core.db.types.JdbcTypeInformation;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.JavaTypeResolver;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.StringUtility;

import java.sql.JDBCType;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @author cen_c
 */
public class MbgJavaTypeResolverDefaultImpl implements JavaTypeResolver {

    protected List<String> warnings;
    protected final Properties properties;
    protected Context context;
    protected boolean forceBigDecimals;
    protected boolean useJSR310Types;

    protected final com.vgosoft.core.db.types.JavaTypeResolverDefaultImpl coreTypeResolver;

    public MbgJavaTypeResolverDefaultImpl() {
        super();
        properties = new Properties();
        coreTypeResolver = new com.vgosoft.core.db.types.JavaTypeResolverDefaultImpl(DatabaseType.OTHER);
    }

    @Override
    public void addConfigurationProperties(Properties properties) {
        this.properties.putAll(properties);
        forceBigDecimals = StringUtility
                .isTrue(properties.getProperty(PropertyRegistry.TYPE_RESOLVER_FORCE_BIG_DECIMALS));
        useJSR310Types = StringUtility
                .isTrue(properties.getProperty(PropertyRegistry.TYPE_RESOLVER_USE_JSR310_TYPES));
    }

    @Override
    public FullyQualifiedJavaType calculateJavaType(IntrospectedColumn introspectedColumn) {
        FullyQualifiedJavaType answer = null;
        JdbcTypeInformation jdbcTypeInformation = getJdbcTypeInformation(introspectedColumn.getJdbcType());

        if (jdbcTypeInformation != null) {
            answer = toFullyQualifiedJavaType(jdbcTypeInformation.getDefaultJavaType());
            answer = overrideDefaultType(introspectedColumn, answer);
        }

        return answer;
    }

    protected FullyQualifiedJavaType overrideDefaultType(IntrospectedColumn column,
                                                         FullyQualifiedJavaType defaultType) {
        FullyQualifiedJavaType answer = defaultType;

        switch (column.getJdbcType()) {
            case Types.BIT:
                answer = calculateBitReplacement(column, defaultType);
                break;
            case Types.DATE:
                answer = calculateDateType(column, defaultType);
                break;
            case Types.DECIMAL:
            case Types.NUMERIC:
                answer = calculateBigDecimalReplacement(column, defaultType);
                break;
            case Types.TIME:
                answer = calculateTimeType(column, defaultType);
                break;
            case Types.TIMESTAMP:
                answer = calculateTimestampType(column, defaultType);
                break;
            default:
                break;
        }

        return answer;
    }

    protected FullyQualifiedJavaType calculateDateType(IntrospectedColumn column, FullyQualifiedJavaType defaultType) {
        FullyQualifiedJavaType answer;

        if (useJSR310Types) {
            answer = new FullyQualifiedJavaType("java.time.LocalDate");
        } else {
            answer = new FullyQualifiedJavaType(Date.class.getName());
        }

        return answer;
    }

    protected FullyQualifiedJavaType calculateTimeType(IntrospectedColumn column, FullyQualifiedJavaType defaultType) {
        FullyQualifiedJavaType answer;

        if (useJSR310Types) {
            answer = new FullyQualifiedJavaType("java.time.LocalTime");
        } else {
            answer = new FullyQualifiedJavaType(Date.class.getName());
        }

        return answer;
    }

    protected FullyQualifiedJavaType calculateTimestampType(IntrospectedColumn column,
                                                            FullyQualifiedJavaType defaultType) {
        FullyQualifiedJavaType answer;

        if (useJSR310Types) {
            answer = new FullyQualifiedJavaType("java.time.LocalDateTime");
        } else {
            answer = new FullyQualifiedJavaType(Date.class.getName());
        }

        return answer;
    }

    protected FullyQualifiedJavaType calculateBitReplacement(IntrospectedColumn column,
                                                             FullyQualifiedJavaType defaultType) {
        FullyQualifiedJavaType answer;

        if (column.getLength() > 1) {
            answer = new FullyQualifiedJavaType("byte[]");
        } else {
            answer = defaultType;
        }

        return answer;
    }

    protected FullyQualifiedJavaType calculateBigDecimalReplacement(IntrospectedColumn column,
                                                                    FullyQualifiedJavaType defaultType) {
        FullyQualifiedJavaType answer;

        if (column.getScale() > 0 || column.getLength() > 18 || forceBigDecimals) {
            answer = defaultType;
        } else if (column.getLength() > 9) {
            answer = new FullyQualifiedJavaType(Long.class.getName());
        } else if (column.getLength() > 4) {
            answer = new FullyQualifiedJavaType(Integer.class.getName());
        } else {
            answer = new FullyQualifiedJavaType(Short.class.getName());
        }

        return answer;
    }

    @Override
    public String calculateJdbcTypeName(IntrospectedColumn introspectedColumn) {
        JDBCType jdbcType = resolveJdbcType(introspectedColumn.getJdbcType());
        return jdbcType == null ? null : jdbcType.getName();
    }

    @Override
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    protected JdbcTypeInformation getJdbcTypeInformation(int jdbcTypeCode) {
        JDBCType jdbcType = resolveJdbcType(jdbcTypeCode);
        if (jdbcType == null) {
            return null;
        }
        return coreTypeResolver.getJdbcTypeInformation(jdbcType);
    }

    protected JDBCType resolveJdbcType(int jdbcTypeCode) {
        try {
            return JDBCType.valueOf(jdbcTypeCode);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    protected FullyQualifiedJavaType toFullyQualifiedJavaType(Class<?> javaType) {
        String typeName = javaType.getCanonicalName();
        if (typeName == null) {
            typeName = javaType.getName();
        }
        return new FullyQualifiedJavaType(typeName);
    }
}

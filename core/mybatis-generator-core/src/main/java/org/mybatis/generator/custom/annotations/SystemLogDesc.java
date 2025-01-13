package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.SystemLog;
import com.vgosoft.core.constant.enums.log.LogTargetTableEnum;
import com.vgosoft.core.constant.enums.log.LogTypesEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedTable;

import static org.mybatis.generator.custom.ConstantsUtil.LOG_TARGET_TABLE_ENUM;
import static org.mybatis.generator.custom.ConstantsUtil.LOG_TYPES_ENUM;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-03 16:12
 * @version 3.0
 */
public class SystemLogDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@"+ SystemLog.class.getSimpleName();

    private final String value;
    private LogTypesEnum logType;
    private LogTargetTableEnum targetTable;
    private final IntrospectedTable introspectedTable;

    public SystemLogDesc(String value, IntrospectedTable introspectedTable) {
        super();
        this.value = value;
        this.introspectedTable = introspectedTable;
        this.addImports(SystemLog.class.getCanonicalName());
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(value)) {
            items.add(VStringUtil.format("value = \"{0}:{1}\"", introspectedTable.getRemarks(true),value));
        }
        if (logType != null) {
            items.add(VStringUtil.format("logType = LogTypesEnum.{0}", logType.name()));
        }
        if (targetTable != null) {
            items.add(VStringUtil.format("logType = LogTargetTableEnum.{0}", targetTable.name()));
        }
        return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
    }

    public String getValue() {
        return value;
    }

    public LogTypesEnum getLogType() {
        return logType;
    }

    public void setLogType(LogTypesEnum logType) {
        this.logType = logType;
        this.addImports(LOG_TYPES_ENUM);
    }

    public LogTargetTableEnum getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(LogTargetTableEnum targetTable) {
        this.targetTable = targetTable;
        this.addImports(LOG_TARGET_TABLE_ENUM);
    }
}

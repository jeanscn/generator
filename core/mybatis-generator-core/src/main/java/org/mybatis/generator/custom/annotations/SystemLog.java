package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.constant.enums.LogTargetTableEnum;
import com.vgosoft.core.constant.enums.LogTypesEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedTable;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-03 16:12
 * @version 3.0
 */
public class SystemLog extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@SystemLog";

    private final String value;
    private LogTypesEnum logType;
    private LogTargetTableEnum targetTable;
    private final IntrospectedTable introspectedTable;

    public SystemLog(String value,IntrospectedTable introspectedTable) {
        super();
        this.value = value;
        this.introspectedTable = introspectedTable;
        this.addImports("com.vgosoft.core.annotation.SystemLog");
    }

    @Override
    public String toAnnotation() {
        StringBuilder sb = new StringBuilder();
        if (VStringUtil.isNotBlank(value)) {
            sb.append("value=\"").append(introspectedTable.getRemarks(true)).append(":").append(value).append("\"");
            if (logType != null) {
                sb.append(",logType=LogTypesEnum.").append(logType.name());
            }
            if (targetTable != null) {
                sb.append(",logType=LogTargetTableEnum.").append(targetTable.name());
            }
        }
        return ANNOTATION_NAME+"("+ sb +")";
    }

    public String getValue() {
        return value;
    }

    public LogTypesEnum getLogType() {
        return logType;
    }

    public void setLogType(LogTypesEnum logType) {
        this.logType = logType;
        this.addImports("com.vgosoft.core.constant.enums.LogTypesEnum");
    }

    public LogTargetTableEnum getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(LogTargetTableEnum targetTable) {
        this.targetTable = targetTable;
        this.addImports("com.vgosoft.core.constant.enums.LogTargetTableEnum");
    }
}

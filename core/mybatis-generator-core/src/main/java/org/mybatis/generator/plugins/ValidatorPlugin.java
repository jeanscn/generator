package org.mybatis.generator.plugins;

import com.vgosoft.core.db.enums.JDBCTypeTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.StringUtility;

import javax.validation.constraints.NotNull;
import java.sql.JDBCType;
import java.util.List;
import java.util.Optional;

/**
 * 添加日期属性的JsonFormat
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-04-19 14:51
 * @version 3.0
 */
public class ValidatorPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean voModelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        addNotNullValidate(method,topLevelClass,introspectedColumn);
        return true;
    }

    private void addNotNullValidate(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn) {
        if (!introspectedColumn.isNullable()) {
            String message = introspectedColumn.getRemarks() + "不能为空";
            if (JDBCTypeTypeEnum.getJDBCTypeType(JDBCType.valueOf(introspectedColumn.getJdbcType()))
                    .equals(JDBCTypeTypeEnum.CHARACTER)) {
                method.addAnnotation("@NotEmpty(groups = {ValidateInsert.class},message = \""+message+"\")");
                topLevelClass.addImportedType("javax.validation.constraints.NotEmpty");
            } else {
                method.addAnnotation("@NotNull(groups = {ValidateInsert.class},message = \""+message+"\")");
                topLevelClass.addImportedType("javax.validation.constraints.NotNull");
            }
            topLevelClass.addImportedType("com.vgosoft.web.valid.ValidateInsert");
        }
    }

}

package org.mybatis.generator.plugins;

import com.vgosoft.core.db.enums.JDBCTypeTypeEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.JavaElement;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.StringUtility;

import java.sql.JDBCType;
import java.util.List;

/**
 * 添加日期属性的JsonFormat
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-04-19 14:51
 * @version 3.0
 */
public class ValidatorPlugin extends PluginAdapter {

    public static final String VALIDATE_INSERT = "com.vgosoft.core.valid.ValidateInsert";
    public static final String VALIDATE_UPDATE = "com.vgosoft.core.valid.ValidateUpdate";

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean voModelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        addNotNullValidate(method, topLevelClass, introspectedColumn,"ValidateInsert.class,ValidateUpdate.class");
        topLevelClass.addImportedType(VALIDATE_INSERT);
        topLevelClass.addImportedType(VALIDATE_UPDATE);
        return true;
    }

    @Override
    public boolean voCreateGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        addNotNullValidate(method, topLevelClass, introspectedColumn,"ValidateInsert.class");
        topLevelClass.addImportedType(VALIDATE_INSERT);
        return true;
    }

    @Override
    public boolean voUpdateGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        addNotNullValidate(method, topLevelClass, introspectedColumn,"ValidateUpdate.class");
        topLevelClass.addImportedType(VALIDATE_UPDATE);
        return true;
    }

    @Override
    public boolean voUpdateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (!introspectedColumn.isNullable()) {
            addNotNullValidate(field, topLevelClass, introspectedColumn,"ValidateUpdate.class");
            topLevelClass.addImportedType(VALIDATE_UPDATE);
        }
        return true;
    }

    private void addNotNullValidate(JavaElement element, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,String validateGroups) {
        if (!introspectedColumn.isNullable()) {
            String message = introspectedColumn.getRemarks(true) + "不能为空";
            if (JDBCTypeTypeEnum.getJDBCTypeType(JDBCType.valueOf(introspectedColumn.getJdbcType()))
                    .equals(JDBCTypeTypeEnum.CHARACTER)) {
                element.addAnnotation("@NotEmpty(groups = {"+validateGroups+"},message = \"" + message + "\")");
                topLevelClass.addImportedType("javax.validation.constraints.NotEmpty");
            } else {
                element.addAnnotation("@NotNull(groups = {"+validateGroups+"},message = \"" + message + "\")");
                topLevelClass.addImportedType("javax.validation.constraints.NotNull");
            }
        }
    }



}

package org.mybatis.generator.plugins;

import com.vgosoft.core.db.enums.JDBCTypeTypeEnum;
import jdk.nashorn.internal.ir.IfNode;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.JavaElement;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.VOCreateGeneratorConfiguration;
import org.mybatis.generator.config.VOGeneratorConfiguration;
import org.mybatis.generator.config.VOUpdateGeneratorConfiguration;

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
        VOGeneratorConfiguration voCfg = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration();
        boolean ngc = voCfg.getVoCreateConfiguration() == null || !voCfg.getVoCreateConfiguration().isGenerate();
        boolean ngu = voCfg.getVoUpdateConfiguration() == null || !voCfg.getVoUpdateConfiguration().isGenerate();
        if (!(introspectedColumn.isNullable() || introspectedTable.getTableConfiguration().getValidateIgnoreColumns().contains(introspectedColumn.getActualColumnName()))) {
            if (ngc && ngu) {
                addNotNullValidate(method, topLevelClass, introspectedColumn, "ValidateInsert.class,ValidateUpdate.class", introspectedTable);
            }else if(ngc){
                addNotNullValidate(method, topLevelClass, introspectedColumn, "ValidateUpdate.class", introspectedTable);
            }else if(ngu){
                addNotNullValidate(method, topLevelClass, introspectedColumn, "ValidateInsert.class", introspectedTable);
            }
            if(ngc){
                topLevelClass.addImportedType(VALIDATE_INSERT);
            }
            if(ngu){
                topLevelClass.addImportedType(VALIDATE_UPDATE);
            }
        }
        return true;
    }

    // 添加非空校验
    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (introspectedColumn == null) {
            return true;
        }
        VOGeneratorConfiguration voCfg = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration();
        boolean ngc = voCfg.getVoCreateConfiguration() == null || !voCfg.getVoCreateConfiguration().isGenerate();
        boolean ngu = voCfg.getVoUpdateConfiguration() == null || !voCfg.getVoUpdateConfiguration().isGenerate();
        if (!(introspectedColumn.isNullable() || introspectedTable.getTableConfiguration().getValidateIgnoreColumns().contains(introspectedColumn.getActualColumnName()))) {
            if (ngc && ngu) {
                if (PropertyRegistry.DEFAULT_PRIMARY_KEY.equalsIgnoreCase(introspectedColumn.getActualColumnName())) {
                    addNotNullValidate(field, topLevelClass, introspectedColumn, "ValidateUpdate.class", introspectedTable);
                }else{
                    addNotNullValidate(field, topLevelClass, introspectedColumn, "ValidateInsert.class,ValidateUpdate.class", introspectedTable);
                }
            }else if(ngc){
                addNotNullValidate(field, topLevelClass, introspectedColumn,"ValidateInsert.class",introspectedTable);
            }else if(ngu){
                addNotNullValidate(field, topLevelClass, introspectedColumn,"ValidateUpdate.class",introspectedTable);
            }
            if(ngc){
                topLevelClass.addImportedType(VALIDATE_INSERT);
            }
            if(ngu){
                topLevelClass.addImportedType(VALIDATE_UPDATE);
            }
        }
        return true;
    }

    @Override
    public boolean voCreateGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        VOCreateGeneratorConfiguration voCreateConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoCreateConfiguration();
        if (!(introspectedColumn.isNullable() || voCreateConfiguration.getValidateIgnoreColumns().contains(introspectedColumn.getActualColumnName()))) {
            addNotNullValidate(method, topLevelClass, introspectedColumn, "ValidateInsert.class", introspectedTable);
            topLevelClass.addImportedType(VALIDATE_INSERT);
        }
        return true;
    }

    @Override
    public boolean voUpdateGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        VOUpdateGeneratorConfiguration voUpdateConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoUpdateConfiguration();
        if (!(introspectedColumn.isNullable() || voUpdateConfiguration.getValidateIgnoreColumns().contains(introspectedColumn.getActualColumnName()))) {
            addNotNullValidate(method, topLevelClass, introspectedColumn, "ValidateUpdate.class", introspectedTable);
            topLevelClass.addImportedType(VALIDATE_UPDATE);
        }
        return true;
    }

    @Override
    public boolean voUpdateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (introspectedColumn == null) {
            return true;
        }
        VOUpdateGeneratorConfiguration voUpdateConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoUpdateConfiguration();
        if (!(introspectedColumn.isNullable() || voUpdateConfiguration.getValidateIgnoreColumns().contains(introspectedColumn.getActualColumnName()))) {
            addNotNullValidate(field, topLevelClass, introspectedColumn,"ValidateUpdate.class",introspectedTable);
            topLevelClass.addImportedType(VALIDATE_UPDATE);
        }
        return true;
    }

    @Override
    public boolean voCreateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (introspectedColumn == null) {
            return true;
        }
        VOCreateGeneratorConfiguration voCreateConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoCreateConfiguration();
        if (!introspectedColumn.isNullable() && !voCreateConfiguration.getValidateIgnoreColumns().contains(introspectedColumn.getActualColumnName())) {
            addNotNullValidate(field, topLevelClass, introspectedColumn,"ValidateInsert.class",introspectedTable);
            topLevelClass.addImportedType(VALIDATE_INSERT);
        }
        return true;
    }

    private void addNotNullValidate(JavaElement element, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, String validateGroups, IntrospectedTable introspectedTable) {
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

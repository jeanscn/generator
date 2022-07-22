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
    public void setContext(Context context) {
        super.setContext(context);
    }

    @Override
    public boolean voModelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }


    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        addNotNullValidate(field, topLevelClass, introspectedColumn);
        return true;
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        addNotNullValidate(field, topLevelClass, introspectedColumn);
        return true;
    }

    private void addNotNullValidate(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn) {
        if (!introspectedColumn.isNullable()) {
            String message = introspectedColumn.getRemarks() + "不能为空";
            if (JDBCTypeTypeEnum.getJDBCTypeType(JDBCType.valueOf(introspectedColumn.getJdbcType()))
                    .equals(JDBCTypeTypeEnum.CHARACTER)) {
                field.addAnnotation("@NotEmpty(message = \"" + message + "\")");
                topLevelClass.addImportedType("javax.validation.constraints.NotEmpty");
            } else {
                field.addAnnotation("@NotNull(message = \"" + message + "\")");
                topLevelClass.addImportedType("javax.validation.constraints.NotNull");
            }
        }
    }

    /**
     * controller及方法注解@Api、@ApiOperation
     */
    @Override
    public boolean ControllerGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

}

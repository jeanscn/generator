package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.mybatis.generator.codegen.mybatis3.vo.AbstractVoGenerator.SUB_PACKAGE_POJO;
import static org.mybatis.generator.internal.util.StringUtility.packageToDir;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 18:39
 * @version 3.0
 */
public class VoGeneratorUtil {

    /** MapStruct 映射注解类型，声明忽略字段时由映射接口统一导入。 */
    public static final String MAPSTRUCT_MAPPING_TYPE = "org.mapstruct.Mapping";

    /**
     * 返回客户端不得经 VO→实体映射写入的服务端强制字段。
     *
     * <p>以既有 {@link GlobalConstant#MAPSTRUCT_IGNORE_DEFAULT_FIELD} 为基础，补齐方案 §4.1 中
     * 同属 A 类的版本号与租户标识；按字段名排序，保证生成输出稳定。</p>
     *
     * @return 有序的审计字段名集合
     */
    public static Set<String> auditIgnoreFields() {
        Set<String> fields = new TreeSet<>(GlobalConstant.MAPSTRUCT_IGNORE_DEFAULT_FIELD);
        fields.add(DefaultColumnNameEnum.VERSION.fieldName());
        fields.add(DefaultColumnNameEnum.TENANT_ID.fieldName());
        return fields;
    }

    /**
     * 返回 UpdateVo→实体映射的忽略字段，版本号保留供 C05 旧链乐观锁使用。
     *
     * @return 不含版本号的审计字段名集合
     */
    public static Set<String> auditIgnoreFieldsForUpdate() {
        Set<String> fields = auditIgnoreFields();
        fields.remove(DefaultColumnNameEnum.VERSION.fieldName());
        return fields;
    }

    public static Method addMappingMethod(FullyQualifiedJavaType fromType, FullyQualifiedJavaType toType, boolean isList, AbstractIntrospectedTable abstractIntrospectedTable) {
        return addMappingMethod(fromType, toType, isList, abstractIntrospectedTable, List.of());
    }

    /**
     * 生成映射方法，并为当前表实际存在的审计字段追加忽略声明。
     *
     * <p>只对当前表内省列输出 {@code @Mapping(target = "...", ignore = true)}，
     * 不因实体父类声明了同名字段就为缺失列生成注解；传入空集合时输出与原重载完全一致。
     * 本重载只用于 VO→实体方向，实体→VO 方向不应传入忽略字段。</p>
     *
     * @param fromType                   源类型
     * @param toType                     目标类型
     * @param isList                     是否生成集合映射方法
     * @param abstractIntrospectedTable  内省表，用于判定目标实体实际拥有的属性
     * @param ignoreFields               需要忽略的目标实体属性名
     * @return 映射方法
     */
    public static Method addMappingMethod(FullyQualifiedJavaType fromType, FullyQualifiedJavaType toType, boolean isList,
                                          AbstractIntrospectedTable abstractIntrospectedTable, Collection<String> ignoreFields) {
        String methodName;
        Method method;
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(abstractIntrospectedTable.getBaseRecordType());
        if (entityType.getFullyQualifiedName().equalsIgnoreCase(toType.getFullyQualifiedName())) {
            methodName = "from" + fromType.getShortNameWithoutTypeArguments();
        } else if (entityType.getFullyQualifiedName().equalsIgnoreCase(fromType.getFullyQualifiedName())) {
            methodName = "to" + toType.getShortNameWithoutTypeArguments();
        } else {
            methodName = "from" + fromType.getShortNameWithoutTypeArguments() + "To" + toType.getShortNameWithoutTypeArguments();
        }
        if (isList) {
            methodName = methodName + "s";
            method = new Method(methodName);
            FullyQualifiedJavaType listInstanceFrom = FullyQualifiedJavaType.getNewListInstance();
            listInstanceFrom.addTypeArgument(fromType);
            method.addParameter(new Parameter(listInstanceFrom, JavaBeansUtil.getFirstCharacterLowercase(fromType.getShortNameWithoutTypeArguments()) + "s"));
            FullyQualifiedJavaType listInstanceTo = FullyQualifiedJavaType.getNewListInstance();
            listInstanceTo.addTypeArgument(toType);
            method.setReturnType(listInstanceTo);
        } else {
            method = new Method(methodName);
            method.addParameter(new Parameter(fromType, JavaBeansUtil.getFirstCharacterLowercase(fromType.getShortNameWithoutTypeArguments())));
            method.setReturnType(toType);
        }
        method.setAbstract(true);
        abstractIntrospectedTable.getContext().getCommentGenerator().addMappingMethodJavaDocLine(method, fromType, toType);
        if (ignoreFields != null && !ignoreFields.isEmpty()) {
            Set<String> targetProperties = tableColumnProperties(abstractIntrospectedTable);
            for (String ignoreField : ignoreFields) {
                if (targetProperties.contains(ignoreField)) {
                    method.addAnnotation(String.format("@Mapping(target = \"%s\", ignore = true)", ignoreField));
                }
            }
        }
        return method;
    }

    /**
     * 汇总当前表实际内省出的 Java 属性名。
     *
     * @param table 内省表
     * @return 表列属性名集合
     */
    private static Set<String> tableColumnProperties(AbstractIntrospectedTable table) {
        Set<String> properties = new LinkedHashSet<>();
        for (IntrospectedColumn column : table.getAllColumns()) {
            properties.add(column.getJavaProperty());
        }
        return properties;
    }

    public static boolean fileNotExist(String subPackage, String fileName, String targetProject, Context context) {
        File project = new File(targetProject);
        if (!project.isDirectory()) {
            return true;
        }
        String baseTargetPackage = context.getJavaModelGeneratorConfiguration().getBaseTargetPackage() + "."+ SUB_PACKAGE_POJO;
        File directory = new File(project, packageToDir(String.join(".", baseTargetPackage, subPackage)));
        if (!directory.isDirectory()) {
            return true;
        }
        File file = new File(directory, fileName + ".java");
        return !file.exists();
    }
}

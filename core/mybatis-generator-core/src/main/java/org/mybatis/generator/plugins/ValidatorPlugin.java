package org.mybatis.generator.plugins;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.VOGeneratorConfiguration;
import org.mybatis.generator.config.VOUpdateGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.validate.NotEmpty;
import org.mybatis.generator.custom.annotations.validate.NotNull;
import org.mybatis.generator.custom.annotations.validate.ValidateDigits;
import org.mybatis.generator.custom.annotations.validate.ValidateSize;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public static final String[] insertValidate = new String[]{"ValidateInsert.class"};
    public static final String[] updateValidate = new String[]{"ValidateUpdate.class"};
    public static final String[] insertAndUpdateValidate = new String[]{"ValidateInsert.class", "ValidateUpdate.class"};

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> ignoreColumns = tc.getValidateIgnoreColumns();
        if (introspectedColumn == null) {
            return true;
        }
        if (!introspectedTable.getRules().isGenerateVoModel()) {
            if (introspectedColumn.isIdentity()) {
                addNotNullValidate(field, topLevelClass, introspectedColumn, updateValidate, introspectedTable, ignoreColumns);
            } else {
                addNotNullValidate(field, topLevelClass, introspectedColumn, insertAndUpdateValidate, introspectedTable, ignoreColumns);
            }
            addLengthValidate(field, topLevelClass, introspectedColumn, insertAndUpdateValidate, introspectedTable, ignoreColumns);
        }
        return true;
    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> ignoreColumns = tc.getValidateIgnoreColumns();
        ignoreColumns.addAll(tc.getVoGeneratorConfiguration().getValidateIgnoreColumns());
        if (introspectedColumn.isIdentity()) {
            addNotNullValidate(field, topLevelClass, introspectedColumn, updateValidate, introspectedTable, ignoreColumns);
        } else {
            addNotNullValidate(field, topLevelClass, introspectedColumn, insertAndUpdateValidate, introspectedTable, ignoreColumns);
        }
        addLengthValidate(field, topLevelClass, introspectedColumn, insertAndUpdateValidate, introspectedTable, ignoreColumns);
        return true;
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> ignoreColumns = tc.getValidateIgnoreColumns();
        VOGeneratorConfiguration voGeneratorConfiguration = tc.getVoGeneratorConfiguration();
        ignoreColumns.addAll(voGeneratorConfiguration.getValidateIgnoreColumns());
        return addVoModelValidate(field, topLevelClass, introspectedColumn, introspectedTable, ignoreColumns);
    }

    @Override
    public boolean voModelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> ignoreColumns = tc.getValidateIgnoreColumns();
        VOGeneratorConfiguration voGeneratorConfiguration = tc.getVoGeneratorConfiguration();
        ignoreColumns.addAll(voGeneratorConfiguration.getValidateIgnoreColumns());
        return addVoModelValidate(method, topLevelClass, introspectedColumn, introspectedTable, ignoreColumns);
    }

    // 添加非空校验
    @Override
    public boolean voCreateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> ignoreColumns = tc.getValidateIgnoreColumns();
        VOGeneratorConfiguration voGeneratorConfiguration = tc.getVoGeneratorConfiguration();
        ignoreColumns.addAll(voGeneratorConfiguration.getValidateIgnoreColumns());
        Set<String> validateIgnoreColumns = tc.getVoGeneratorConfiguration().getVoCreateConfiguration().getValidateIgnoreColumns();
        ignoreColumns.addAll(validateIgnoreColumns);
        return addCreateValidate(field, topLevelClass, introspectedColumn, introspectedTable, ignoreColumns);
    }

    @Override
    public boolean voCreateGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> validateIgnoreColumns = tc.getVoGeneratorConfiguration().getVoCreateConfiguration().getValidateIgnoreColumns();
        return addCreateValidate(method, topLevelClass, introspectedColumn, introspectedTable, validateIgnoreColumns);
    }

    @Override
    public boolean voUpdateGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> validateIgnoreColumns = tc.getVoGeneratorConfiguration().getVoUpdateConfiguration().getValidateIgnoreColumns();
        return addUpdateValidate(method, topLevelClass, introspectedColumn, introspectedTable, validateIgnoreColumns);
    }

    @Override
    public boolean voUpdateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> validateIgnoreColumns = tc.getVoGeneratorConfiguration().getVoUpdateConfiguration().getValidateIgnoreColumns();
        return addUpdateValidate(field, topLevelClass, introspectedColumn, introspectedTable, validateIgnoreColumns);
    }

    private boolean addVoModelValidate(JavaElement javaElement,
                                       TopLevelClass topLevelClass,
                                       IntrospectedColumn introspectedColumn,
                                       IntrospectedTable introspectedTable,
                                       Set<String> ignoreColumns) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        ignoreColumns.addAll(tc.getVoGeneratorConfiguration().getVoModelConfiguration().getValidateIgnoreColumns());
        if (introspectedColumn == null || ignoreColumns.contains(introspectedColumn.getActualColumnName())) {
            return true;
        }
        boolean generateCreateVO = introspectedTable.getRules().isGenerateCreateVO();
        boolean generateUpdateVO = introspectedTable.getRules().isGenerateUpdateVO();
        if (!(generateCreateVO || generateUpdateVO)) {
            if (introspectedColumn.isIdentity()) {
                addNotNullValidate(javaElement, topLevelClass, introspectedColumn, updateValidate, introspectedTable, ignoreColumns);
            } else {
                addNotNullValidate(javaElement, topLevelClass, introspectedColumn, insertAndUpdateValidate, introspectedTable, ignoreColumns);
            }
            addLengthValidate(javaElement, topLevelClass, introspectedColumn, insertAndUpdateValidate, introspectedTable, ignoreColumns);
        } else if (!generateCreateVO) {
            addCreateValidate(javaElement, topLevelClass, introspectedColumn, introspectedTable, ignoreColumns);
        } else if (!generateUpdateVO) {
            addUpdateValidate(javaElement, topLevelClass, introspectedColumn, introspectedTable, ignoreColumns);
        }
        return true;
    }

    private boolean addCreateValidate(JavaElement javaElement,
                                      TopLevelClass topLevelClass,
                                      IntrospectedColumn introspectedColumn,
                                      IntrospectedTable introspectedTable,
                                      Set<String> ignoreColumns) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> validateIgnoreColumns = tc.getVoGeneratorConfiguration().getVoCreateConfiguration().getValidateIgnoreColumns();
        ignoreColumns.addAll(validateIgnoreColumns);
        if (introspectedColumn == null || ignoreColumns.contains(introspectedColumn.getActualColumnName())) {
            return true;
        }
        addNotNullValidate(javaElement, topLevelClass, introspectedColumn, insertValidate, introspectedTable, ignoreColumns);
        addLengthValidate(javaElement, topLevelClass, introspectedColumn, insertValidate, introspectedTable, ignoreColumns);
        return true;
    }

    private boolean addUpdateValidate(JavaElement javaElement,
                                      TopLevelClass topLevelClass,
                                      IntrospectedColumn introspectedColumn,
                                      IntrospectedTable introspectedTable,
                                      Set<String> ignoreColumns) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        VOUpdateGeneratorConfiguration voUpdateConfiguration = tc.getVoGeneratorConfiguration().getVoUpdateConfiguration();
        ignoreColumns.addAll(voUpdateConfiguration.getValidateIgnoreColumns());
        if (introspectedColumn == null || ignoreColumns.contains(introspectedColumn.getActualColumnName())) {
            return true;
        }
        addNotNullValidate(javaElement, topLevelClass, introspectedColumn, updateValidate, introspectedTable, ignoreColumns);
        addLengthValidate(javaElement, topLevelClass, introspectedColumn, updateValidate, introspectedTable, ignoreColumns);
        return true;
    }

    /**
     * 添加长度校验
     *
     * @param element            生成的方法或者属性
     * @param topLevelClass      生成的类
     * @param introspectedColumn 表字段
     * @param validateGroups     校验组
     */
    private void addLengthValidate(JavaElement element,
                                   TopLevelClass topLevelClass,
                                   IntrospectedColumn introspectedColumn,
                                   String[] validateGroups,
                                   IntrospectedTable introspectedTable, Set<String> validateIgnoreColumns) {
        //校验字符串长度
        if (introspectedColumn.getLength() == 0
                || validateIgnoreColumns.contains(introspectedColumn.getActualColumnName())) {
            return;
        }
        if (introspectedColumn.isStringColumn()) {
            ValidateSize validateSize = new ValidateSize();
            validateSize.setMax(introspectedColumn.getLength() > 0 ? String.valueOf(introspectedColumn.getLength()) : "");
            if (VStringUtil.stringHasValue(validateSize.getMax())) {
                validateSize.setMin(String.valueOf(introspectedColumn.getMinLength()));
                if (introspectedColumn.getMinLength() > 0) {
                    validateSize.setMessage(VStringUtil.format("{0}的长度应该在{1}-{2}之间"
                            , introspectedColumn.getRemarks(true)
                            , introspectedColumn.getMinLength()
                            , introspectedColumn.getLength()));
                } else {
                    validateSize.setMessage(VStringUtil.format("{0}的长度不能超过{1}"
                            , introspectedColumn.getRemarks(true)
                            , introspectedColumn.getLength()));
                }
                validateSize.setGroups(validateGroups);
                element.addAnnotation(validateSize.toAnnotation());
                topLevelClass.addImportedTypes(validateSize.getImportedTypes());
                topLevelClass.addImportedTypes(getImportedTypes(validateGroups));
            }
        }else if(introspectedColumn.isNumericColumn()){
            ValidateDigits validateDigits = new ValidateDigits();
            validateDigits.setInteger(String.valueOf(introspectedColumn.getLength()));
            validateDigits.setFraction(String.valueOf(introspectedColumn.getScale()));
            if (introspectedColumn.getScale()>0) {
                validateDigits.setMessage(VStringUtil.format("{0}的整数位数不能超过{1}，小数位数不能超过{2}"
                        , introspectedColumn.getRemarks(true)
                        , introspectedColumn.getLength()
                        , introspectedColumn.getScale()));
            }else{
                validateDigits.setMessage(VStringUtil.format("{0}的整数位数不能超过{1}"
                        , introspectedColumn.getRemarks(true)
                        , introspectedColumn.getLength()));
            }
            validateDigits.setGroups(validateGroups);
            element.addAnnotation(validateDigits.toAnnotation());
            topLevelClass.addImportedTypes(validateDigits.getImportedTypes());
            topLevelClass.addImportedTypes(getImportedTypes(validateGroups));
        }
    }

    /**
     * 添加非空校验
     *
     * @param element            生成的方法或者属性
     * @param topLevelClass      生成的类
     * @param introspectedColumn 表字段
     * @param validateGroups     校验组
     */
    private void addNotNullValidate(JavaElement element,
                                    TopLevelClass topLevelClass,
                                    IntrospectedColumn introspectedColumn,
                                    String[] validateGroups,
                                    IntrospectedTable introspectedTable, Set<String> validateIgnoreColumns) {
        if (validateIgnoreColumns.contains(introspectedColumn.getActualColumnName()) || introspectedColumn.isNullable()) {
            return;
        }
        String message = introspectedColumn.getRemarks(true) + "不能为空";
        if (introspectedColumn.getFullyQualifiedJavaType().getShortName().equalsIgnoreCase("String")) {
            NotEmpty notEmpty = NotEmpty.create(message, validateGroups);
            element.addAnnotation(notEmpty.toAnnotation());
            topLevelClass.addImportedTypes(notEmpty.getImportedTypes());
        } else {
            NotNull notNull = NotNull.create(message, validateGroups);
            element.addAnnotation(notNull.toAnnotation());
            topLevelClass.addImportedTypes(notNull.getImportedTypes());
        }
        topLevelClass.addImportedTypes(getImportedTypes(validateGroups));
    }

    private Set<FullyQualifiedJavaType> getImportedTypes(String[] validateGroups) {
        Set<FullyQualifiedJavaType> importedTypes = new HashSet<>();
        for (String validateGroup : validateGroups) {
            if ("ValidateInsert.class".equals(validateGroup)) {
                importedTypes.add(new FullyQualifiedJavaType(VALIDATE_INSERT));
            } else if ("ValidateUpdate.class".equals(validateGroup)) {
                importedTypes.add(new FullyQualifiedJavaType(VALIDATE_UPDATE));
            }
        }
        return importedTypes;
    }
}

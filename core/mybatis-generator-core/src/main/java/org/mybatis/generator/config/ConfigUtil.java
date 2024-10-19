package org.mybatis.generator.config;

import com.vgosoft.tool.core.VCollectionUtil;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import com.vgosoft.core.constant.enums.view.HtmlElementDataSourceEnum;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import static com.vgosoft.tool.core.VStringUtil.*;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-14 10:29
 * @version 3.0
 */
public class ConfigUtil {

    public static boolean javaPropertyExist(final String propertyName, final IntrospectedTable introspectedTable) {
        if (introspectedTable.getAllColumns().stream().anyMatch(c -> c.getJavaProperty().equals(propertyName))) {
            return true;
        }
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        //检查VoModel，Vo配置中是否存在对应的转换属性
        if (tc.getVoGeneratorConfiguration().getVoModelConfiguration() != null && !tc.getVoGeneratorConfiguration().getVoModelConfiguration().getOverridePropertyConfigurations().isEmpty()) {
            return tc.getVoGeneratorConfiguration().getVoModelConfiguration().getOverridePropertyConfigurations().stream()
                    .anyMatch(c -> c.getTargetPropertyName().equals(propertyName))
                    || tc.getVoGeneratorConfiguration().getVoModelConfiguration().getAdditionalPropertyConfigurations().stream()
                    .anyMatch(c -> c.getName().equals(propertyName));
        } else {
            return tc.getJavaModelGeneratorConfiguration().getOverridePropertyConfigurations().stream()
                    .anyMatch(c -> c.getTargetPropertyName().equals(propertyName))
                    || tc.getJavaModelGeneratorConfiguration().getAdditionalPropertyConfigurations().stream()
                    .anyMatch(c -> c.getName().equals(propertyName));
        }
    }

    public static String getOverrideJavaProperty(final String propertyName) {
        if (propertyName.length() > 2 && propertyName.endsWith("Id")) {
            return propertyName.substring(0, propertyName.length() - 2) + "Text";
        } else {
            return propertyName + "Text";
        }
    }

    // 通过HtmlElementDescriptor配置，创建转换属性配置
    public static OverridePropertyValueGeneratorConfiguration createOverridePropertyConfiguration(HtmlElementDescriptor elementDescriptor, final IntrospectedTable introspectedTable) {
        OverridePropertyValueGeneratorConfiguration overrideConfiguration = new OverridePropertyValueGeneratorConfiguration(introspectedTable.getContext(), introspectedTable.getTableConfiguration(), elementDescriptor.getName());

        HtmlElementDataSourceEnum anEnum = HtmlElementDataSourceEnum.getEnum(elementDescriptor.getDataSource());
        if (anEnum != null && stringHasValue(anEnum.getBeanName())) {
            overrideConfiguration.setBeanName(anEnum.getBeanName());
        } else if (stringHasValue(elementDescriptor.getBeanName())) {
            overrideConfiguration.setBeanName(elementDescriptor.getBeanName());
        }
        if (stringHasValue(elementDescriptor.getEnumClassName())) {
            overrideConfiguration.setEnumClassName(elementDescriptor.getEnumClassName());
        }
        if (stringHasValue(elementDescriptor.getDictCode())) {
            overrideConfiguration.setTypeValue(elementDescriptor.getDictCode());
        }
        if (anEnum != null) {
            overrideConfiguration.setAnnotationType(anEnum.getAnnotationName());
        }
        overrideConfiguration.setTargetPropertyName(elementDescriptor.getOtherFieldName());
        overrideConfiguration.setTargetPropertyType(FullyQualifiedJavaType.getStringInstance().getFullyQualifiedName());
        if (elementDescriptor.getApplyProperty() != null) {
            overrideConfiguration.setApplyProperty(elementDescriptor.getApplyProperty());
        }
        introspectedTable.getColumn(elementDescriptor.getName()).ifPresent(c -> overrideConfiguration.setRemark(c.getRemarks(true)));
        return overrideConfiguration;

    }

    public static OverridePropertyValueGeneratorConfiguration createOverridePropertyConfiguration(
            Context context, TableConfiguration tc, String columnName, String beanName, String annotationType, String targetPropertyName, String targetPropertyType, String remark) {
        OverridePropertyValueGeneratorConfiguration overrideConfiguration = new OverridePropertyValueGeneratorConfiguration(context, tc, columnName);
        overrideConfiguration.setBeanName(beanName);
        overrideConfiguration.setAnnotationType(annotationType);
        overrideConfiguration.setTargetPropertyName(targetPropertyName);
        overrideConfiguration.setTargetPropertyType(targetPropertyType);
        overrideConfiguration.setRemark(remark);
        return overrideConfiguration;
    }

    /**
     * 在配置对象中，追加转换属性配置。所有htmlElementDescriptor配置，都会转换为转换属性配置
     * 1、如果生成了vo对象，则追加到voModel、voView、voExcel中
     * 2、如果没有生成vo对象，则追加到JavaModelGeneratorConfiguration中
     */

    public static void addOverridePropertyConfiguration(OverridePropertyValueGeneratorConfiguration overrideConfiguration, final TableConfiguration tc) {
        if (tc.getVoGeneratorConfiguration() != null && tc.getVoGeneratorConfiguration().isGenerate()) {
            if (tc.getVoGeneratorConfiguration().getVoModelConfiguration() != null && tc.getVoGeneratorConfiguration().getVoModelConfiguration().isGenerate()) {
                VCollectionUtil.addIfNotContains(tc.getVoGeneratorConfiguration().getVoModelConfiguration().getOverridePropertyConfigurations(), overrideConfiguration);
            }
            if (tc.getVoGeneratorConfiguration().getVoViewConfiguration() != null && tc.getVoGeneratorConfiguration().getVoViewConfiguration().isGenerate()) {
                VCollectionUtil.addIfNotContains(tc.getVoGeneratorConfiguration().getVoViewConfiguration().getOverridePropertyConfigurations(), overrideConfiguration);
            }
            if (tc.getVoGeneratorConfiguration().getVoExcelConfiguration() != null && tc.getVoGeneratorConfiguration().getVoExcelConfiguration().isGenerate()) {
                VCollectionUtil.addIfNotContains(tc.getVoGeneratorConfiguration().getVoExcelConfiguration().getOverridePropertyConfigurations(), overrideConfiguration);
            }
        } else {
            VCollectionUtil.addIfNotContains(tc.getJavaModelGeneratorConfiguration().getOverridePropertyConfigurations(), overrideConfiguration);
        }
    }

    public static String getIntrospectedTableBeanName(TableConfiguration tc) {
        if (tc.getDomainObjectName() != null) {
            return JavaBeansUtil.getFirstCharacterLowercase(tc.getDomainObjectName()) + "Impl";
        } else {
            return JavaBeansUtil.getCamelCaseString(tc.getTableName(), false) + "Impl";
        }
    }

}

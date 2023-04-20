package org.mybatis.generator.config;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.custom.DictTypeEnum;
import org.mybatis.generator.custom.HtmlElementTagTypeEnum;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-14 10:29
 * @version 3.0
 */
public class ConfigUtil {

    public static boolean javaPropertyExist(final String propertyName, final IntrospectedTable introspectedTable) {
        if (introspectedTable.getAllColumns().stream().anyMatch(c->c.getJavaProperty().equals(propertyName))) {
            return true;
        }
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        //检查VoModel，Vo配置中是否存在对应的转换属性
        if (tc.getVoGeneratorConfiguration().getVoModelConfiguration()!=null && tc.getVoGeneratorConfiguration().getVoModelConfiguration().getOverridePropertyConfigurations().size()>0) {
            return tc.getVoGeneratorConfiguration().getVoModelConfiguration().getOverridePropertyConfigurations().stream()
                    .anyMatch(c -> c.getTargetPropertyName().equals(propertyName))
                    || tc.getVoGeneratorConfiguration().getVoModelConfiguration().getAdditionalPropertyConfigurations().stream()
                    .anyMatch(c -> c.getName().equals(propertyName));
        }else if(tc.getVoGeneratorConfiguration().getVoModelConfiguration()!=null){
            return tc.getVoGeneratorConfiguration().getOverridePropertyConfigurations().stream()
                    .anyMatch(c -> c.getTargetPropertyName().equals(propertyName))
                    || tc.getVoGeneratorConfiguration().getAdditionalPropertyConfigurations().stream()
                    .anyMatch(c -> c.getName().equals(propertyName));
        }else{
            return tc.getJavaModelGeneratorConfiguration().getOverridePropertyConfigurations().stream()
                    .anyMatch(c -> c.getTargetPropertyName().equals(propertyName))
                    || tc.getJavaModelGeneratorConfiguration().getAdditionalPropertyConfigurations().stream()
                    .anyMatch(c -> c.getName().equals(propertyName));
        }
    }

    public static String getOverrideJavaProperty(final String propertyName) {
        if (propertyName.endsWith("Id")) {
            return propertyName.substring(0, propertyName.length() - 2)+"Text";
        }else{
            return propertyName+"Text";
        }
    }

    public static OverridePropertyValueGeneratorConfiguration createOverridePropertyConfiguration(HtmlElementDescriptor elementDescriptor, final IntrospectedTable introspectedTable) {
        if (!elementDescriptor.getTagType().equals(HtmlElementTagTypeEnum.SELECT.getCode())) {
            return null;
        }
        OverridePropertyValueGeneratorConfiguration overrideConfiguration = new OverridePropertyValueGeneratorConfiguration(introspectedTable.getContext(), introspectedTable.getTableConfiguration());
        overrideConfiguration.setSourceColumnName(elementDescriptor.getName());
        overrideConfiguration.setAnnotationType(DictTypeEnum.DICT.getCode());
        overrideConfiguration.setTargetPropertyName(elementDescriptor.getOtherFieldName());
        overrideConfiguration.setTargetPropertyType(FullyQualifiedJavaType.getStringInstance().getFullyQualifiedName());
        switch (elementDescriptor.getDataFormat()) {
            case "department":
                overrideConfiguration.setBeanName("orgDepartmentImpl");
                break;
            case "user":
                overrideConfiguration.setBeanName("orgUserImpl");
                break;
            case "role":
                overrideConfiguration.setBeanName("orgRoleImpl");
                break;
            case "organ":
                overrideConfiguration.setBeanName("orgOrganizationImpl");
                break;
        }
        return overrideConfiguration;
    }
}

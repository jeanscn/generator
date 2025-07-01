package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.internal.util.JavaBeansUtil;

@Setter
@Getter
public class SelectBySqlMethodGeneratorConfiguration extends PropertyHolder {

    private String sqlMethod;

    private String parentIdColumnName;

    private String primaryKeyColumnName;

    private IntrospectedColumn parentIdColumn;

    private IntrospectedColumn primaryKeyColumn;

    public SelectBySqlMethodGeneratorConfiguration() {
        super();
    }

    public String getMethodName() {
        return "selectBySqlMethod"+ JavaBeansUtil.getFirstCharacterUppercase(this.sqlMethod);
    }
}

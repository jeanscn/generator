package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class SelectByTableGeneratorConfiguration extends PropertyHolder {

    private String tableName;

    private String primaryKeyColumn;

    private String otherPrimaryKeyColumn;

    private String methodSuffix;

    private String parameterName;

    private String orderByClause;

    private String additionCondition;

    private String returnTypeParam;

    private boolean enableSplit;

    private boolean enableUnion;

    private IntrospectedColumn thisColumn;

    private IntrospectedColumn otherColumn;

    private String parameterType = "single";

    private final List<EnableCacheConfiguration> cacheConfigurationList = new ArrayList<>();

    public SelectByTableGeneratorConfiguration() {
        super();
        returnTypeParam = "model";
        enableSplit = true;
        enableUnion = true;
    }

    public boolean isReturnPrimaryKey() {
        return this.getReturnTypeParam().equals("primaryKey")
                ||this.getReturnTypeParam().equals("list_primaryKey");
    }

    public String getMethodName() {
        return "selectByTable"+JavaBeansUtil.getFirstCharacterUppercase(this.getMethodSuffix());
    }

    public String getSplitMethodName() {
        return "deleteByTable"+JavaBeansUtil.getFirstCharacterUppercase(this.getMethodSuffix());
    }

    public String getUnionMethodName() {
        return "insertByTable"+JavaBeansUtil.getFirstCharacterUppercase(this.getMethodSuffix());
    }

    public String getSelectByTableMethodName() {
        return "selectByTable"+JavaBeansUtil.getFirstCharacterUppercase(this.getMethodSuffix());
    }

}

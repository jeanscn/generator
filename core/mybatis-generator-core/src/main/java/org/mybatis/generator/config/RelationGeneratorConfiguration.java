package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.enums.RelationTypeEnum;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
public class RelationGeneratorConfiguration extends PropertyHolder {

    /**
     *  关系类型：association、collection
    * */
    private RelationTypeEnum type;

    /**
     * 新增的属性名称
     */
    private String propertyName;

    /**
     * 用于查询的列名
     */
    private String column;

    /**
     * 执行子查询调用的mapper方法
     */
    private String select;

    /**
     * 属性类型的泛型（PO类中使用）
     */
    private String modelTye;

    /**
     * VO属性类型的泛型（VO类中使用）
     */
    private String voModelTye;

    /**
     * mapper xml中collection元算的javaType。
     * 默认值为ArrayList
     */
    private String javaType;

    /**
     *
     */
    private String columnRemark;

    /**
     * 是否支持属性级联insert操作
     */
    private boolean enableInsert;

    /**
     * 是否支持属性级联Update操作
     */
    private boolean enableUpdate;


    private boolean enableInsertOrUpdate;

    /**
     * 是否支持属性级联Delete操作
     */
    private boolean enableDelete;

    /**
     * 用于支持新增和更新时，新增更新方法的bean类全名。
     * 仅在支持新增、更新、删除时使用
     */
    private String beanClassFullName;

    /**
     * 用于支持新增和更新时，检查关联属性值。构造getter、setter
     * 仅在支持新增、更新、删除时使用
     */
    private String relationProperty;

    /**
     * 用于支持新增和更新时，检查关联属性值。构造getter
     * 仅在支持新增、更新、删除时使用
     */
    private boolean relationPropertyIsBoolean;

    private String remark;

    private String initializationString;

    private final Set<String> importTypes = new HashSet<>();

    public RelationGeneratorConfiguration() {
        super();
        this.enableInsert = false;
        this.enableUpdate = false;
        this.enableDelete = false;
        this.enableInsertOrUpdate = false;
        this.relationPropertyIsBoolean = false;
    }

    ;

    public boolean isSubSelected(){
        return StringUtility.stringHasValue(select) && StringUtility.stringHasValue(column);
    }

    public String getVoModelTye() {
        return voModelTye==null?this.modelTye:voModelTye;
    }

    public Optional<String> getInitializationString() {
        return Optional.ofNullable(initializationString);
    }

    public void addImportTypes(String importType) {
        this.importTypes.add(importType);
    }
}

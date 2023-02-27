package org.mybatis.generator.custom.annotations.mybatisplus;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;

/**
 * mybatisplus的TableField注解
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-02-27 02:12
 * @version 3.0
 */
public class TableField extends AbstractAnnotation {
public static final String ANNOTATION_NAME = "@TableField";
    public static final String importClass = "com.baomidou.mybatisplus.annotation.TableField";
    private  String value;
    private boolean exist = true;
    private String condition;
    private String update;
    private String select;
    private String whereStrategy;
    private boolean jdbcType = false;
    private String typeHandler;
    private String fill;
    private String updateStrategy;
    private String insertStrategy;
    private String updateIgnoreStrategy;
    private String insertIgnoreStrategy;

    public static TableField create(String value) {
        return new TableField(value);
    }

    public TableField() {
        super();
        this.addImports(importClass);
    }

    public TableField(String value) {
        super();
        this.value = value;
        this.addImports(importClass);
    }

    @Override
    public String toAnnotation() {
        if (value != null) {
            this.items.add(VStringUtil.format("value = \"{0}\"", this.value));
        }
        this.items.add(VStringUtil.format("exist = {0}", this.exist));

        if (condition != null) {
            this.items.add(VStringUtil.format("condition = \"{0}\"", this.condition));
        }
        if (update != null) {
            this.items.add(VStringUtil.format("update = \"{0}\"", this.update));
        }
        if (select != null) {
            this.items.add(VStringUtil.format("select = \"{0}\"", this.select));
        }
        if (whereStrategy != null) {
            this.items.add(VStringUtil.format("whereStrategy = \"{0}\"", this.whereStrategy));
        }
        return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    public String getWhereStrategy() {
        return whereStrategy;
    }

    public void setWhereStrategy(String whereStrategy) {
        this.whereStrategy = whereStrategy;
    }

    public boolean isJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(boolean jdbcType) {
        this.jdbcType = jdbcType;
    }

    public String getTypeHandler() {
        return typeHandler;
    }

    public void setTypeHandler(String typeHandler) {
        this.typeHandler = typeHandler;
    }

    public String getFill() {
        return fill;
    }

    public void setFill(String fill) {
        this.fill = fill;
    }

    public String getUpdateStrategy() {
        return updateStrategy;
    }

    public void setUpdateStrategy(String updateStrategy) {
        this.updateStrategy = updateStrategy;
    }

    public String getInsertStrategy() {
        return insertStrategy;
    }

    public void setInsertStrategy(String insertStrategy) {
        this.insertStrategy = insertStrategy;
    }

    public String getUpdateIgnoreStrategy() {
        return updateIgnoreStrategy;
    }

    public void setUpdateIgnoreStrategy(String updateIgnoreStrategy) {
        this.updateIgnoreStrategy = updateIgnoreStrategy;
    }

    public String getInsertIgnoreStrategy() {
        return insertIgnoreStrategy;
    }

    public void setInsertIgnoreStrategy(String insertIgnoreStrategy) {
        this.insertIgnoreStrategy = insertIgnoreStrategy;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

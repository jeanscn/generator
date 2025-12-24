package org.mybatis.generator.custom.annotations.mybatisplus;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;

/**
 * mybatisplus的TableField注解
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-02-27 02:12
 * @version 3.0
 */
@Setter
@Getter
public class TableField extends AbstractAnnotation {
public static final String ANNOTATION_NAME = "@TableField";
    public static final String TABLE_FIELD = "com.baomidou.mybatisplus.annotation.TableField";
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
        this.addImports(TABLE_FIELD);
    }

    public TableField(String value) {
        super();
        this.value = value;
        this.addImports(TABLE_FIELD);
    }

    @Override
    public String toAnnotation() {
        if (value != null) {
            this.items.add(VStringUtil.format("value = \"{0}\"", this.value));
        }
        if (!this.exist) {
            this.items.add(VStringUtil.format("exist = {0}", "false"));
        }

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

}

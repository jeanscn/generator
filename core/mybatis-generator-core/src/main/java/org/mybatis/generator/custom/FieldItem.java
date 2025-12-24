package org.mybatis.generator.custom;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.dom.java.Field;

import java.util.Objects;

/**
 * @author cen_c
 */
@Setter
@Getter
public class FieldItem {
    public String name;
    public String type;
    public String remarks;
    public boolean required = false;

    public FieldItem(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public FieldItem(Field field) {
        this.name = field.getName();
        this.type = field.getType().getShortName();
        this.remarks = field.getRemark();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FieldItem fieldItem = (FieldItem) o;
        return Objects.equals(name, fieldItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

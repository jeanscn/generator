package org.mybatis.generator.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-07-17 04:28
 * @version 4.0
 */
@Setter
@Getter
@NoArgsConstructor
public class ListColumnConfiguration extends PropertyHolder{

    private String field;

    private String width;

    private Integer minWidth;

    private String fixed;

    private String templet;

    private String style;

    private String label;

    private String align;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) { return false; }
        ListColumnConfiguration that = (ListColumnConfiguration) o;
        return Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(field);
    }
}

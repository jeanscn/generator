package org.mybatis.generator.api.dom.html;

import java.util.Objects;

public class Attribute {

    private String name;

    private String value;

    public Attribute(String name, String value) {
        this.name = Objects.requireNonNull(name);
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value){
        this.value = value;
    }
}

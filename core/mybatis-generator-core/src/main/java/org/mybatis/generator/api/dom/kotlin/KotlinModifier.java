package org.mybatis.generator.api.dom.kotlin;

public enum KotlinModifier {

    PUBLIC("public"),
    PRIVATE("private"),
    DATA("data"),
    LATE_INIT("lateinit");

    private final String value;

    KotlinModifier(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

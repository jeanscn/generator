package org.mybatis.generator.api.dom.kotlin;

public enum KotlinModifier {

    PUBLIC("public"), //$NON-NLS-1$
    PRIVATE("private"), //$NON-NLS-1$
    DATA("data"), //$NON-NLS-1$
    LATE_INIT("lateinit"); //$NON-NLS-1$

    private final String value;

    KotlinModifier(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

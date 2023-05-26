package org.mybatis.generator.api.dom.java;

import java.util.EnumSet;

/**
 * Typesafe enum of possible Java visibility settings.
 *
 * @author Jeff Butler
 */
public enum JavaVisibility {
    PUBLIC("public "), //$NON-NLS-1$
    PRIVATE("private "), //$NON-NLS-1$
    PROTECTED("protected "), //$NON-NLS-1$
    DEFAULT(""); //$NON-NLS-1$

    private final String value;

    JavaVisibility(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static JavaVisibility ofCode(final String value){
        return EnumSet.allOf(JavaVisibility.class).stream()
                .filter(e -> e.value.equals(value))
                .findFirst().orElse(JavaVisibility.DEFAULT);
    }
}

package org.mybatis.generator.api.dom.kotlin;

import lombok.Getter;

/**
 * @author cen_c
 */

@Getter
public enum KotlinModifier {

    /**
     * Kotlin modifiers
     */
    PUBLIC("public"),
    PRIVATE("private"),
    DATA("data"),
    LATE_INIT("lateinit");

    private final String value;

    KotlinModifier(String value) {
        this.value = value;
    }

}

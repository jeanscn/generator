package org.mybatis.generator.config;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * Typesafe enum of different model types.
 *
 * @author Jeff Butler
 */
public enum ModelType {
    HIERARCHICAL("hierarchical"), //$NON-NLS-1$
    FLAT("flat"), //$NON-NLS-1$
    CONDITIONAL("conditional"); //$NON-NLS-1$

    private final String type;

    ModelType(String type) {
        this.type = type;
    }

    public static ModelType getModelType(String type) {
        if (HIERARCHICAL.type.equalsIgnoreCase(type)) {
            return HIERARCHICAL;
        } else if (FLAT.type.equalsIgnoreCase(type)) {
            return FLAT;
        } else if (CONDITIONAL.type.equalsIgnoreCase(type)) {
            return CONDITIONAL;
        } else {
            throw new RuntimeException(getString(
                    "RuntimeError.13", type)); //$NON-NLS-1$
        }
    }
}

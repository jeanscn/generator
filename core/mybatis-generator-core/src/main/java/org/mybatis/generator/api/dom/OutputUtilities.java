package org.mybatis.generator.api.dom;

public class OutputUtilities {

    /**
     * Utility class - no instances allowed.
     */
    private OutputUtilities() {
        super();
    }

    /**
     * Utility method that indents the buffer by the default amount for Java
     * (four spaces per indent level).
     *
     * @param sb
     *            a StringBuilder to append to
     * @param indentLevel
     *            the required indent level
     */
    public static void javaIndent(StringBuilder sb, int indentLevel) {
        for (int i = 0; i < indentLevel; i++) {
            sb.append("    "); //$NON-NLS-1$
        }
    }

    /**
     * Utility method that indents the buffer by the default amount for Kotlin
     * (four spaces per indent level).
     *
     * @param sb
     *            a StringBuilder to append to
     * @param indentLevel
     *            the required indent level
     */
    public static void kotlinIndent(StringBuilder sb, int indentLevel) {
        javaIndent(sb, indentLevel);
    }

    /**
     * Utility method that indents the buffer by the default amount for XML (two
     * spaces per indent level).
     *
     * @param sb
     *            a StringBuilder to append to
     * @param indentLevel
     *            the required indent level
     */
    public static void xmlIndent(StringBuilder sb, int indentLevel) {
        for (int i = 0; i < indentLevel; i++) {
            sb.append("  "); //$NON-NLS-1$
        }
    }
}

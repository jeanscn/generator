package org.mybatis.generator.internal.util;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author cen_c
 */
public final class JavaLiteralUtil {

    private static final Pattern INTEGER_PATTERN = Pattern.compile("[-+]?\\d+");
    private static final Pattern INTEGER_WITH_SUFFIX_PATTERN = Pattern.compile("[-+]?\\d+[lL]");
    private static final Pattern FLOATING_PATTERN = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?(?:[eE][-+]?\\d+)?");
    private static final Pattern FLOATING_WITH_SUFFIX_PATTERN = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?(?:[eE][-+]?\\d+)?[fFdD]");
    private static final Pattern NUMBER_WITH_SUFFIX_PATTERN = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?(?:[eE][-+]?\\d+)?[fFdDlL]");

    private JavaLiteralUtil() {
    }

    public static String normalizeLiteralForJavaType(FullyQualifiedJavaType javaType, String defaultValue) {
        if (defaultValue == null || javaType == null) {
            return defaultValue;
        }
        String javaTypeName = javaType.getFullyQualifiedNameWithoutTypeParameters();
        String literal = unwrapSqlDefaultLiteral(defaultValue);
        return switch (javaTypeName) {
            case "long", "java.lang.Long" -> normalizeLongLiteral(literal, defaultValue);
            case "float", "java.lang.Float" -> normalizeFloatingLiteral(literal, defaultValue, 'F');
            case "double", "java.lang.Double" -> normalizeFloatingLiteral(literal, defaultValue, 'D');
            case "java.math.BigDecimal" -> normalizeBigDecimalLiteral(literal, defaultValue);
            default -> defaultValue;
        };
    }

    private static String normalizeLongLiteral(String literal, String fallback) {
        if (INTEGER_WITH_SUFFIX_PATTERN.matcher(literal).matches()) {
            return literal.substring(0, literal.length() - 1) + "L";
        }
        if (INTEGER_PATTERN.matcher(literal).matches()) {
            return literal + "L";
        }
        return fallback;
    }

    private static String normalizeFloatingLiteral(String literal, String fallback, char suffix) {
        if (FLOATING_WITH_SUFFIX_PATTERN.matcher(literal).matches()) {
            return literal.substring(0, literal.length() - 1) + suffix;
        }
        if (FLOATING_PATTERN.matcher(literal).matches()) {
            return literal + suffix;
        }
        return fallback;
    }

    private static String normalizeBigDecimalLiteral(String literal, String fallback) {
        if (fallback.contains("BigDecimal.valueOf(") || fallback.contains("new BigDecimal(")) {
            return fallback;
        }
        String numericLiteral = literal;
        if (NUMBER_WITH_SUFFIX_PATTERN.matcher(numericLiteral).matches()) {
            numericLiteral = numericLiteral.substring(0, numericLiteral.length() - 1);
        }
        if (FLOATING_PATTERN.matcher(numericLiteral).matches()) {
            return "BigDecimal.valueOf(" + numericLiteral + ")";
        }
        return fallback;
    }

    static String unwrapSqlDefaultLiteral(String defaultValue) {
        String literal = defaultValue == null ? null : defaultValue.trim();
        if (literal == null) {
            return null;
        }
        literal = stripWrappers(literal);
        if (literal.toLowerCase(Locale.ROOT).startsWith("cast(") && literal.endsWith(")")) {
            String inner = literal.substring("cast(".length(), literal.length() - 1).trim();
            int asIndex = inner.toLowerCase(Locale.ROOT).indexOf(" as ");
            if (asIndex > 0) {
                return stripWrappers(inner.substring(0, asIndex).trim());
            }
        }
        int castIndex = literal.indexOf("::");
        if (castIndex > 0) {
            literal = literal.substring(0, castIndex).trim();
        }
        return stripWrappers(literal);
    }

    private static String stripWrappers(String literal) {
        String value = literal;
        boolean changed = true;
        while (changed) {
            changed = false;
            if (value.startsWith("(") && value.endsWith(")")) {
                value = value.substring(1, value.length() - 1).trim();
                changed = true;
            }
            if ((value.startsWith("'") && value.endsWith("'"))
                    || (value.startsWith("\"") && value.endsWith("\""))) {
                value = value.substring(1, value.length() - 1).trim();
                changed = true;
            }
        }
        return value;
    }
}

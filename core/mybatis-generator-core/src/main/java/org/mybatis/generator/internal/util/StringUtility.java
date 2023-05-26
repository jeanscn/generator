package org.mybatis.generator.internal.util;

import java.io.File;
import java.util.*;

public class StringUtility {

    /**
     * Utility class. No instances allowed
     */
    private StringUtility() {
        super();
    }

    public static boolean stringHasValue(String s) {
        return s != null && s.length() > 0;
    }

    public static String composeFullyQualifiedTableName(String catalog,
            String schema, String tableName, char separator) {
        StringBuilder sb = new StringBuilder();

        if (stringHasValue(catalog)) {
            sb.append(catalog);
            sb.append(separator);
        }

        if (stringHasValue(schema)) {
            sb.append(schema);
            sb.append(separator);
        } else {
            if (sb.length() > 0) {
                sb.append(separator);
            }
        }

        sb.append(tableName);

        return sb.toString();
    }

    public static boolean stringContainsSpace(String s) {
        return s != null && s.indexOf(' ') != -1;
    }

    public static String escapeStringForJava(String s) {
        StringTokenizer st = new StringTokenizer(s, "\"", true); //$NON-NLS-1$
        StringBuilder sb = new StringBuilder();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if ("\"".equals(token)) { //$NON-NLS-1$
                sb.append("\\\""); //$NON-NLS-1$
            } else {
                sb.append(token);
            }
        }

        return sb.toString();
    }

    public static String escapeStringForKotlin(String s) {
        StringTokenizer st = new StringTokenizer(s, "\"$", true); //$NON-NLS-1$
        StringBuilder sb = new StringBuilder();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if ("\"".equals(token)) { //$NON-NLS-1$
                sb.append("\\\""); //$NON-NLS-1$
            } else if ("$".equals(token)) { //$NON-NLS-1$
                sb.append("\\$"); //$NON-NLS-1$
            } else {
                sb.append(token);
            }
        }

        return sb.toString();
    }

    public static boolean isTrue(String s) {
        return "true".equalsIgnoreCase(s); //$NON-NLS-1$
    }

    public static boolean stringContainsSQLWildcard(String s) {
        if (s == null) {
            return false;
        }

        return s.indexOf('%') != -1 || s.indexOf('_') != -1;
    }

    public static String substringAfter(String str, String separator) {
        if (isEmpty(str)) {
            return str;
        } else if (separator == null) {
            return "";
        } else {
            int pos = str.indexOf(separator);
            return pos == -1 ? "" : str.substring(pos + separator.length());
        }
    }

    public static String substringAfterLast(String str, String separator) {
        if (isEmpty(str)) {
            return str;
        } else if (isEmpty(separator)) {
            return "";
        } else {
            int pos = str.lastIndexOf(separator);
            return pos != -1 && pos != str.length() - separator.length() ? str.substring(pos + separator.length()) : "";
        }
    }

    public static String substringBefore(String str, String separator) {
        if (!isEmpty(str) && separator != null) {
            if (separator.isEmpty()) {
                return "";
            } else {
                int pos = str.indexOf(separator);
                return pos == -1 ? str : str.substring(0, pos);
            }
        } else {
            return str;
        }
    }

    public static String substringBeforeLast(String str, String separator) {
        if (!isEmpty(str) && !isEmpty(separator)) {
            int pos = str.lastIndexOf(separator);
            return pos == -1 ? str : str.substring(0, pos);
        } else {
            return str;
        }
    }

    /**
     * 截取字符串中括号前的内容
     * 主要用于截取表注释、列注释，用于生成标题
     * @param remark 注释信息
     * @return 字符串
     */
    public static String remarkLeft(String remark) {
        return remark.split("[(（]")[0];
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen = length(cs);
        if (strLen == 0) {
            return true;
        } else {
            for(int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
    public static int length(CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }
    public static String lowerCase(String str) {
        return str == null ? null : str.toLowerCase();
    }

    public static String lowerCase(String str, Locale locale) {
        return str == null ? null : str.toLowerCase(locale);
    }

    public static String[][] parsePropertyValue(String propertyValue){
        final String cs1 = ",";
        final String cs2 = "\\|";
        String[] items = propertyValue.split(cs1);
        String[][] ret = new String[items.length][];
        for (int i = 0; i < items.length; i++) {
           String[] split = items[i].split(cs2);
            ret[i] = new String[split.length];
            System.arraycopy(split, 0, ret[i], 0, split.length);
        }
        return ret;
    }

    public static boolean propertyValueValid(String value){
        return StringUtility.stringHasValue(value) && !"_".equals(value);
    }

    public static String packageToDir(String targetPackage){
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(targetPackage, ".");
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
            sb.append(File.separatorChar);
        }
        return sb.toString();
    }

    public static String getTargetProject(String targetProject){
        if (targetProject.toUpperCase().contains("$PROJECT_DIR$")) {
            targetProject =  targetProject.replace("$PROJECT_DIR$", StringUtility.substringBeforeLast(System.getProperty("user.dir"),"\\"));
        }
        return targetProject;
    }

    public static  List<String> spiltToList(String str) {
        List<String> ret = new ArrayList<>();
        if (stringHasValue(str)) {
            String[] split = removeSpaces(str).split("[|,;，；、]");
            Collections.addAll(ret, split);
        }
        return ret;
    }

    public static  Set<String> spiltToSet(String str){
        Set<String> ret = new HashSet<>();
        if (stringHasValue(str)) {
            String[] split = removeSpaces(str).split("[|,;，；、]");
            Collections.addAll(ret, split);
        }
        return ret;
    }

    public static String removeSpaces(String str) {
        if (str == null) {
            return null;
        }
        // 使用正则表达式替换所有空格
        return str.replaceAll("\\s", "");
    }
}

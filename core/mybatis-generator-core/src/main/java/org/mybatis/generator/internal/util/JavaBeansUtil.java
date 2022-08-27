/*
 *    Copyright 2006-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.internal.util;

import com.sun.jna.platform.win32.WinNT;
import com.vgosoft.core.db.util.JDBCUtil;
import org.apache.commons.lang3.ClassUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.ObjectFactory;

import java.io.File;
import java.sql.JDBCType;
import java.util.Locale;
import java.util.Properties;

import static org.mybatis.generator.internal.util.StringUtility.isTrue;
import static org.mybatis.generator.internal.util.StringUtility.packageToDir;

public class JavaBeansUtil {

    private JavaBeansUtil() {
        super();
    }

    /**
     * Computes a getter method name.  Warning - does not check to see that the property is a valid
     * property.  Call getValidPropertyName first.
     *
     * @param property               the property
     * @param fullyQualifiedJavaType the fully qualified java type
     * @return the getter method name
     */
    public static String getGetterMethodName(String property, FullyQualifiedJavaType fullyQualifiedJavaType) {
        StringBuilder sb = new StringBuilder(getMethodName(property));
        if (fullyQualifiedJavaType.equals(FullyQualifiedJavaType.getBooleanPrimitiveInstance())) {
            sb.insert(0, "is"); //$NON-NLS-1$
        } else {
            sb.insert(0, "get"); //$NON-NLS-1$
        }
        return sb.toString();
    }

    private static String getMethodName(String property) {
        StringBuilder sb = new StringBuilder();
        sb.append(property);
        if (Character.isLowerCase(sb.charAt(0)) && (sb.length() == 1 || !Character.isUpperCase(sb.charAt(1)))) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }
        return sb.toString();
    }

    /**
     * Computes a setter method name.  Warning - does not check to see that the property is a valid
     * property.  Call getValidPropertyName first.
     *
     * @param property the property
     * @return the setter method name
     */
    public static String getSetterMethodName(String property) {
        StringBuilder sb = new StringBuilder(getMethodName(property));
        sb.insert(0, "set"); //$NON-NLS-1$
        return sb.toString();
    }

    public static String getFirstCharacterUppercase(String inputString) {
        if (inputString != null) {
            StringBuilder sb = new StringBuilder(inputString);
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
            return sb.toString();
        } else {
            return null;
        }

    }

    public static String getFirstCharacterLowercase(String inputString) {
        if (inputString != null) {
            StringBuilder sb = new StringBuilder(inputString);
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
            return sb.toString();
        } else {
            return null;
        }
    }

    public static String getCamelCaseString(String inputString,
                                            boolean firstCharacterUppercase) {
        StringBuilder sb = new StringBuilder();

        boolean nextUpperCase = false;
        for (int i = 0; i < inputString.length(); i++) {
            char c = inputString.charAt(i);

            switch (c) {
                case '_':
                case '-':
                case '@':
                case '$':
                case '#':
                case ' ':
                case '/':
                case '&':
                    if (sb.length() > 0) {
                        nextUpperCase = true;
                    }
                    break;

                default:
                    if (nextUpperCase) {
                        sb.append(Character.toUpperCase(c));
                        nextUpperCase = false;
                    } else {
                        sb.append(Character.toLowerCase(c));
                    }
                    break;
            }
        }

        if (firstCharacterUppercase) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }

        return sb.toString();
    }

    /**
     * This method ensures that the specified input string is a valid Java property name.
     *
     * <p>The rules are as follows:
     *
     * <ol>
     *   <li>If the first character is lower case, then OK</li>
     *   <li>If the first two characters are upper case, then OK</li>
     *   <li>If the first character is upper case, and the second character is lower case, then the first character
     *       should be made lower case</li>
     * </ol>
     *
     * <p>For example:
     *
     * <ul>
     *   <li>eMail &gt; eMail</li>
     *   <li>firstName &gt; firstName</li>
     *   <li>URL &gt; URL</li>
     *   <li>XAxis &gt; XAxis</li>
     *   <li>a &gt; a</li>
     *   <li>B &gt; b</li>
     *   <li>Yaxis &gt; yaxis</li>
     * </ul>
     *
     * @param inputString the input string
     * @return the valid property name
     */
    public static String getValidPropertyName(String inputString) {
        String answer;

        if (inputString == null) {
            answer = null;
        } else if (inputString.length() < 2) {
            answer = inputString.toLowerCase(Locale.US);
        } else {
            if (Character.isUpperCase(inputString.charAt(0))
                    && !Character.isUpperCase(inputString.charAt(1))) {
                answer = inputString.substring(0, 1).toLowerCase(Locale.US)
                        + inputString.substring(1);
            } else {
                answer = inputString;
            }
        }

        return answer;
    }

    public static Method getJavaBeansGetter(IntrospectedColumn introspectedColumn,
                                            Context context,
                                            IntrospectedTable introspectedTable) {
        Method method = getBasicJavaBeansGetter(introspectedColumn);
        addGeneratedGetterJavaDoc(method, introspectedColumn, context, introspectedTable);
        return method;
    }

    public static Method getJavaBeansGetterWithGeneratedAnnotation(IntrospectedColumn introspectedColumn,
                                                                   Context context, IntrospectedTable introspectedTable, CompilationUnit compilationUnit) {
        Method method = getBasicJavaBeansGetter(introspectedColumn);
        addGeneratedGetterAnnotation(method, introspectedColumn, context, introspectedTable, compilationUnit);
        return method;
    }

    public static Method getBasicJavaBeansGetter(String columnJavaProperty, FullyQualifiedJavaType parameter) {
        Method method = new Method(getGetterMethodName(columnJavaProperty, parameter));
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(parameter);

        String sb = "return " +
                columnJavaProperty +
                ';';
        method.addBodyLine(sb);

        return method;
    }

    private static Method getBasicJavaBeansGetter(IntrospectedColumn introspectedColumn) {
        FullyQualifiedJavaType fqjt = introspectedColumn
                .getFullyQualifiedJavaType();
        String property = introspectedColumn.getJavaProperty();

        Method method = new Method(getGetterMethodName(property, fqjt));
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(fqjt);

        String s = "return " + property + ';'; //$NON-NLS-1$
        method.addBodyLine(s);

        return method;
    }

    private static void addGeneratedGetterJavaDoc(Method method, IntrospectedColumn introspectedColumn,
                                                  Context context, IntrospectedTable introspectedTable) {
        context.getCommentGenerator().addGetterComment(method,
                introspectedTable, introspectedColumn);
    }

    private static void addGeneratedGetterAnnotation(Method method, IntrospectedColumn introspectedColumn,
                                                     Context context,
                                                     IntrospectedTable introspectedTable, CompilationUnit compilationUnit) {
        context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable, introspectedColumn,
                compilationUnit.getImportedTypes());
    }

    public static Field getJavaBeansField(IntrospectedColumn introspectedColumn,
                                          Context context,
                                          IntrospectedTable introspectedTable) {
        Field field = getBasicJavaBeansField(introspectedColumn);
        addGeneratedJavaDoc(field, context, introspectedColumn, introspectedTable);
        return field;
    }

    public static Field getJavaBeansFieldWithGeneratedAnnotation(IntrospectedColumn introspectedColumn,
                                                                 Context context,
                                                                 IntrospectedTable introspectedTable,
                                                                 CompilationUnit compilationUnit) {
        Field field = getBasicJavaBeansField(introspectedColumn);
        addGeneratedAnnotation(field, context, introspectedColumn, introspectedTable, compilationUnit);
        return field;
    }

    private static Field getBasicJavaBeansField(IntrospectedColumn introspectedColumn) {
        FullyQualifiedJavaType fqjt = introspectedColumn
                .getFullyQualifiedJavaType();
        String property = introspectedColumn.getJavaProperty();

        Field field = new Field(property, fqjt);
        field.setVisibility(JavaVisibility.PRIVATE);

        return field;
    }

    private static void addGeneratedJavaDoc(Field field, Context context, IntrospectedColumn introspectedColumn,
                                            IntrospectedTable introspectedTable) {
        context.getCommentGenerator().addFieldComment(field,
                introspectedTable, introspectedColumn);
    }

    private static void addGeneratedAnnotation(Field field, Context context, IntrospectedColumn introspectedColumn,
                                               IntrospectedTable introspectedTable, CompilationUnit compilationUnit) {
        context.getCommentGenerator().addFieldAnnotation(field, introspectedTable, introspectedColumn,
                compilationUnit.getImportedTypes());
    }

    public static Method getJavaBeansSetter(IntrospectedColumn introspectedColumn,
                                            Context context,
                                            IntrospectedTable introspectedTable) {
        Method method = getBasicJavaBeansSetter(introspectedColumn);
        addGeneratedSetterJavaDoc(method, introspectedColumn, context, introspectedTable);
        return method;
    }

    public static Method getJavaBeansSetterWithGeneratedAnnotation(IntrospectedColumn introspectedColumn,
                                                                   Context context,
                                                                   IntrospectedTable introspectedTable, CompilationUnit compilationUnit) {
        Method method = getBasicJavaBeansSetter(introspectedColumn);
        addGeneratedSetterAnnotation(method, introspectedColumn, context, introspectedTable, compilationUnit);
        return method;
    }

    public static Method getBasicJavaBeanSetter(String columnJavaProperty, boolean isStringReturn, FullyQualifiedJavaType parameter) {
        Method method = new Method(getSetterMethodName(columnJavaProperty));
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(parameter, columnJavaProperty));

        StringBuilder sb = new StringBuilder();
        if (isStringReturn) {
            sb.append("this."); //$NON-NLS-1$
            sb.append(columnJavaProperty);
            sb.append(" = "); //$NON-NLS-1$
            sb.append(columnJavaProperty);
            sb.append(" == null ? null : "); //$NON-NLS-1$
            sb.append(columnJavaProperty);
            sb.append(".trim();"); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
        } else {
            sb.append("this."); //$NON-NLS-1$
            sb.append(columnJavaProperty);
            sb.append(" = "); //$NON-NLS-1$
            sb.append(columnJavaProperty);
            sb.append(';');
            method.addBodyLine(sb.toString());
        }
        return method;
    }

    private static Method getBasicJavaBeansSetter(IntrospectedColumn introspectedColumn) {
        FullyQualifiedJavaType fqjt = introspectedColumn
                .getFullyQualifiedJavaType();
        String property = introspectedColumn.getJavaProperty();

        Method method = new Method(getSetterMethodName(property));
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(fqjt, property));

        StringBuilder sb = new StringBuilder();
        if (introspectedColumn.isStringColumn() && isTrimStringsEnabled(introspectedColumn)) {
            sb.append("this."); //$NON-NLS-1$
            sb.append(property);
            sb.append(" = "); //$NON-NLS-1$
            sb.append(property);
            sb.append(" == null ? null : "); //$NON-NLS-1$
            sb.append(property);
            sb.append(".trim();"); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
        } else {
            sb.append("this."); //$NON-NLS-1$
            sb.append(property);
            sb.append(" = "); //$NON-NLS-1$
            sb.append(property);
            sb.append(';');
            method.addBodyLine(sb.toString());
        }

        return method;
    }

    private static void addGeneratedSetterJavaDoc(Method method, IntrospectedColumn introspectedColumn, Context context,
                                                  IntrospectedTable introspectedTable) {
        context.getCommentGenerator().addSetterComment(method,
                introspectedTable, introspectedColumn);
    }

    private static void addGeneratedSetterAnnotation(Method method, IntrospectedColumn introspectedColumn,
                                                     Context context,
                                                     IntrospectedTable introspectedTable, CompilationUnit compilationUnit) {
        context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable, introspectedColumn,
                compilationUnit.getImportedTypes());
    }

    private static boolean isTrimStringsEnabled(Context context) {
        Properties properties = context
                .getJavaModelGeneratorConfiguration().getProperties();
        return isTrue(properties
                .getProperty(PropertyRegistry.MODEL_GENERATOR_TRIM_STRINGS));
    }

    private static boolean isTrimStringsEnabled(IntrospectedTable table) {
        TableConfiguration tableConfiguration = table.getTableConfiguration();
        String trimSpaces = tableConfiguration.getProperties().getProperty(
                PropertyRegistry.MODEL_GENERATOR_TRIM_STRINGS);
        if (trimSpaces != null) {
            return isTrue(trimSpaces);
        }
        return isTrimStringsEnabled(table.getContext());
    }

    private static boolean isTrimStringsEnabled(IntrospectedColumn column) {
        String trimSpaces = column.getProperties().getProperty(PropertyRegistry.MODEL_GENERATOR_TRIM_STRINGS);
        if (trimSpaces != null) {
            return isTrue(trimSpaces);
        }
        return isTrimStringsEnabled(column.getIntrospectedTable());
    }

    //是否某个类的子类
    public static boolean isAssignable(String parentClassName, String childClassName, IntrospectedTable introspectedTable) {
        try {
            //处理生成key类未加载的问题
            FullyQualifiedJavaType childClassNameType = new FullyQualifiedJavaType(childClassName);
            //FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
            Class<?> cClazz = getClass(childClassName);
            //if (childClassNameType.getShortName().equals(entityType.getShortName() + "Key")) {
            if (cClazz == null) {
                childClassName = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ANY_ROOT_CLASS);
                if (childClassName == null) {
                    Properties properties = introspectedTable.getContext().getJavaModelGeneratorConfiguration().getProperties();
                    childClassName = properties.getProperty(PropertyRegistry.ANY_ROOT_CLASS);
                }
                cClazz = Class.forName(childClassName);
            }
            //}
            Class<?> pClazz = Class.forName(parentClassName);
            return ClassUtils.isAssignable(cClazz, pClazz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Class<?> getClass(String className)  {
        try {
            return ObjectFactory.internalClassForName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static boolean isAssignableCurrent(String parentClassName, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (topLevelClass.getSuperClass().isPresent()) {
            FullyQualifiedJavaType fullyQualifiedJavaType = topLevelClass.getSuperClass().get();
            String fullyQualifiedName = fullyQualifiedJavaType.getFullyQualifiedNameWithoutTypeParameters();
            boolean assignable = JavaBeansUtil.isAssignable(parentClassName, fullyQualifiedName, introspectedTable);
            if (assignable) {
                return true;
            }
        }
        for (FullyQualifiedJavaType superInterfaceType : topLevelClass.getSuperInterfaceTypes()) {
            boolean assignableCurrent = JavaBeansUtil.isAssignable(parentClassName, superInterfaceType.getFullyQualifiedName(), introspectedTable);
            if (assignableCurrent) {
                return true;
            }
        }
        return false;
    }

    public static String byColumnMethodName(IntrospectedColumn column) {
        String javaProperty = column.getJavaProperty();
        return "selectByColumn" + getFirstCharacterUppercase(javaProperty);
    }

    public static void addAnnotation(AbstractJavaType javaType, String annotation) {
        long count = javaType.getAnnotations().stream().filter(t -> t.equals(annotation)).count();
        if (count == 0) {
            javaType.addAnnotation(annotation);
        }
    }

    public static boolean isSelectBaseByPrimaryKeyMethod(String methodName) {
        return "selectBaseByPrimaryKey".equalsIgnoreCase(methodName);
    }

    public static String getColumnExampleValue(IntrospectedColumn introspectedColumn) {
        String defaultValue = JDBCUtil.getJDBCTypeExample(JDBCType.valueOf(introspectedColumn.getJdbcType()));
        String javaTypeName = introspectedColumn.getFullyQualifiedJavaType().getShortName();
        if (introspectedColumn.getJavaProperty().equals("active")) {
            return "1";
        } else if (javaTypeName.equals("String")) {
            return "\"" + introspectedColumn.getJavaProperty() + "\"";
        } else {
            return defaultValue;
        }
    }

    public static boolean javaFileExist(String targetProject,String targetPackage, String fileName) {
        File project = new File(targetProject);
        if (!project.isDirectory()) {
            return false;
        }
        File directory = new File(project, packageToDir(targetPackage));
        if (!directory.isDirectory()) {
            return false;
        }
        File file = new File(directory, fileName + ".java");
        return file.exists();
    }

    public static boolean javaFileNotExist(String targetProject,String targetPackage, String fileName){
        return !javaFileExist(targetProject,targetPackage,fileName);
    }

    public static String getRootClass(IntrospectedTable introspectedTable) {
        String rootClass = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ANY_ROOT_CLASS);
        if (rootClass == null) {
            Properties properties = introspectedTable.getContext().getJavaModelGeneratorConfiguration().getProperties();
            rootClass = properties.getProperty(PropertyRegistry.ANY_ROOT_CLASS);
        }
        return rootClass;
    }
}

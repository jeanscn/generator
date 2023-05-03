package org.mybatis.generator.codegen;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.ObjectFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * Holds information about a class (uses the JavaBeans Introspector to find properties).
 * @author Jeff Butler
 *
 */
public class RootClassInfo {

    private static final Map<String, RootClassInfo> rootClassInfoMap;

    static {
        rootClassInfoMap = Collections.synchronizedMap(new HashMap<>());
    }

    public static RootClassInfo getInstance(String className, List<String> warnings) {
        return rootClassInfoMap.computeIfAbsent(className, k -> new RootClassInfo(k, warnings));
    }

    /**
     * Clears the internal map containing root class info.  This method should be called at the beginning of
     * a generation run to clear the cached root class info in case there has been a change.
     * For example, when using the eclipse launcher, the cache would be kept until eclipse
     * was restarted.
     *
     */
    public static void reset() {
        rootClassInfoMap.clear();
    }

    private PropertyDescriptor[] propertyDescriptors;
    private final String className;
    private final List<String> warnings;
    private boolean genericMode = false;

    private RootClassInfo(String className, List<String> warnings) {
        super();
        this.className = className;
        this.warnings = warnings;

        if (className == null) {
            return;
        }

        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(className);
        String nameWithoutGenerics = fqjt.getFullyQualifiedNameWithoutTypeParameters();
        if (!nameWithoutGenerics.equals(className)) {
            genericMode = true;
        }

        try {
            Class<?> clazz = ObjectFactory.externalClassForName(nameWithoutGenerics);
            BeanInfo bi = Introspector.getBeanInfo(clazz);
            propertyDescriptors = bi.getPropertyDescriptors();
        } catch (Exception e) {
            propertyDescriptors = null;
            warnings.add(getString("Warning.20", className)); //$NON-NLS-1$
        }
    }

    public boolean containsProperty(IntrospectedColumn introspectedColumn) {
        if (propertyDescriptors == null) {
            return false;
        }

        boolean found = false;
        String propertyName = introspectedColumn.getJavaProperty();
        String propertyType = introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName();

        // get method names from class and check against this column definition.
        // better yet, have a map of method Names. check against it.
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
            if (hasProperty(propertyName, propertyType, propertyDescriptor)) {
                found = true;
                break;
            }
        }

        return found;
    }

    private boolean hasProperty(String propertyName, String propertyType, PropertyDescriptor propertyDescriptor) {
        return hasCorrectName(propertyName, propertyDescriptor)
                && isProperType(propertyName, propertyType, propertyDescriptor)
                && hasGetter(propertyName, propertyDescriptor)
                && hasSetter(propertyName, propertyDescriptor);
    }

    private boolean hasCorrectName(String propertyName, PropertyDescriptor propertyDescriptor) {
        return propertyDescriptor.getName().equals(propertyName);
    }

    private boolean isProperType(String propertyName, String propertyType, PropertyDescriptor propertyDescriptor) {
        String introspectedPropertyType = propertyDescriptor.getPropertyType().getTypeName();
        if (genericMode && introspectedPropertyType.equals("java.lang.Object")) { //$NON-NLS-1$
            // OK - but add a warning
            warnings.add(getString("Warning.28", propertyName, className)); //$NON-NLS-1$
        } else if (!introspectedPropertyType.equals(propertyType)) {
            warnings.add(getString("Warning.21", propertyName, className, propertyType)); //$NON-NLS-1$
            return false;
        }

        return true;
    }

    private boolean hasGetter(String propertyName, PropertyDescriptor propertyDescriptor) {
        if (propertyDescriptor.getReadMethod() == null) {
            warnings.add(getString("Warning.22", propertyName, className)); //$NON-NLS-1$
            return false;
        }

        return true;
    }

    private boolean hasSetter(String propertyName, PropertyDescriptor propertyDescriptor) {
        if (propertyDescriptor.getWriteMethod() == null) {
            warnings.add(getString("Warning.23", propertyName, className)); //$NON-NLS-1$
            return false;
        }

        return true;
    }
}

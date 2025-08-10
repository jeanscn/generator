package org.mybatis.generator.api.dom.java;

import lombok.Getter;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author cen_c
 */
public class FullyQualifiedJavaType implements
        Comparable<FullyQualifiedJavaType> {

    private static final String JAVA_LANG = "java.lang";

    private static FullyQualifiedJavaType intInstance = null;

    private static FullyQualifiedJavaType stringInstance = null;

    private static FullyQualifiedJavaType booleanPrimitiveInstance = null;

    private static FullyQualifiedJavaType objectInstance = null;

    private static FullyQualifiedJavaType dateInstance = null;

    private static FullyQualifiedJavaType criteriaInstance = null;

    private static FullyQualifiedJavaType generatedCriteriaInstance = null;

    /** The short name without any generic arguments. */
    private String baseShortName;

    /** The fully qualified name without any generic arguments. */
    private String baseQualifiedName;

    @Getter
    private boolean explicitlyImported;

    @Getter
    private String packageName;

    @Getter
    private boolean primitive;

    private boolean isArray;

    @Getter
    private PrimitiveTypeWrapper primitiveTypeWrapper;

    @Getter
    private final List<FullyQualifiedJavaType> typeArguments;

    /**
     * the following three values are used for dealing with wildcard types
     */
    private boolean wildcardType;

    private boolean boundedWildcard;

    private boolean extendsBoundedWildcard;

    /**
     * Use this constructor to construct a generic type with the specified type parameters.
     *
     * @param fullTypeSpecification
     *            the full type specification
     */
    public FullyQualifiedJavaType(String fullTypeSpecification) {
        super();
        typeArguments = new ArrayList<>();
        parse(fullTypeSpecification);
    }

    /**
     * Returns the fully qualified name - including any generic type parameters.
     *
     * @return Returns the fullyQualifiedName.
     */
    public String getFullyQualifiedName() {
        StringBuilder sb = new StringBuilder();
        if (wildcardType) {
            sb.append('?');
            if (boundedWildcard) {
                if (extendsBoundedWildcard) {
                    sb.append(" extends ");
                } else {
                    sb.append(" super ");
                }

                sb.append(baseQualifiedName);
            }
        } else {
            sb.append(baseQualifiedName);
        }

        if (!typeArguments.isEmpty()) {
            boolean first = true;
            sb.append('<');
            for (FullyQualifiedJavaType fqjt : typeArguments) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(fqjt.getFullyQualifiedName());

            }
            sb.append('>');
        }

        return sb.toString();
    }

    public String getFullyQualifiedNameWithoutTypeParameters() {
        return baseQualifiedName;
    }

    /**
     * Returns a list of Strings that are the fully qualified names of this type, and any generic type argument
     * associated with this type.
     *
     * @return the import list
     */
    public List<String> getImportList() {
        List<String> answer = new ArrayList<>();
        if (isExplicitlyImported()) {
            int index = baseShortName.indexOf('.');
            if (index == -1) {
                answer.add(calculateActualImport(baseQualifiedName));
            } else {
                // an inner class is specified, only import the top
                // level class
                String sb = packageName + '.' + calculateActualImport(baseShortName.substring(0, index));
                answer.add(sb);
            }
        }

        for (FullyQualifiedJavaType fqjt : typeArguments) {
            answer.addAll(fqjt.getImportList());
        }

        return answer;
    }

    private String calculateActualImport(String name) {
        String answer = name;
        if (this.isArray()) {
            int index = name.indexOf('[');
            if (index != -1) {
                answer = name.substring(0, index);
            }
        }
        return answer;
    }

    public String getShortName() {
        StringBuilder sb = new StringBuilder();
        if (wildcardType) {
            sb.append('?');
            if (boundedWildcard) {
                if (extendsBoundedWildcard) {
                    sb.append(" extends ");
                } else {
                    sb.append(" super ");
                }

                sb.append(baseShortName);
            }
        } else {
            sb.append(baseShortName);
        }

        if (!typeArguments.isEmpty()) {
            boolean first = true;
            sb.append('<');
            for (FullyQualifiedJavaType fqjt : typeArguments) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(fqjt.getShortName());

            }
            sb.append('>');
        }

        return sb.toString();
    }

    public String getShortNameFirstLowCase() {
        return JavaBeansUtil.getFirstCharacterLowercase(getShortName());
    }

    public String getShortNameWithoutTypeArguments() {
        return baseShortName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof FullyQualifiedJavaType)) {
            return false;
        }

        FullyQualifiedJavaType other = (FullyQualifiedJavaType) obj;

        return getFullyQualifiedName().equals(other.getFullyQualifiedName());
    }

    @Override
    public int hashCode() {
        return getFullyQualifiedName().hashCode();
    }

    @Override
    public String toString() {
        return getFullyQualifiedName();
    }

    public static FullyQualifiedJavaType getIntInstance() {
        if (intInstance == null) {
            intInstance = new FullyQualifiedJavaType("int");
        }

        return intInstance;
    }

    public static FullyQualifiedJavaType getIntegerInstance() {
        return new FullyQualifiedJavaType("java.lang.Integer");
    }

    public static FullyQualifiedJavaType getNewListInstance() {
        // always return a new instance because the type may be parameterized
        return new FullyQualifiedJavaType("java.util.List");
    }

    public static FullyQualifiedJavaType getNewHashMapInstance() {
        // always return a new instance because the type may be parameterized
        return new FullyQualifiedJavaType("java.util.HashMap");
    }

    public static FullyQualifiedJavaType getNewArrayListInstance() {
        // always return a new instance because the type may be parameterized
        return new FullyQualifiedJavaType("java.util.ArrayList");
    }

    public static FullyQualifiedJavaType getNewIteratorInstance() {
        // always return a new instance because the type may be parameterized
        return new FullyQualifiedJavaType("java.util.Iterator");
    }

    public static FullyQualifiedJavaType getStringInstance() {
        if (stringInstance == null) {
            stringInstance = new FullyQualifiedJavaType("java.lang.String");
        }

        return stringInstance;
    }

    public static FullyQualifiedJavaType getBooleanPrimitiveInstance() {
        if (booleanPrimitiveInstance == null) {
            booleanPrimitiveInstance = new FullyQualifiedJavaType("boolean");
        }

        return booleanPrimitiveInstance;
    }

    public static FullyQualifiedJavaType getObjectInstance() {
        if (objectInstance == null) {
            objectInstance = new FullyQualifiedJavaType("java.lang.Object");
        }

        return objectInstance;
    }

    public static FullyQualifiedJavaType getDateInstance() {
        if (dateInstance == null) {
            dateInstance = new FullyQualifiedJavaType("java.util.Date");
        }

        return dateInstance;
    }

    public static FullyQualifiedJavaType getOptionalFullyQualifiedJavaType() {
        return new FullyQualifiedJavaType("java.util.Optional");
    }

    public static FullyQualifiedJavaType getOptionalFullyQualifiedJavaType(FullyQualifiedJavaType typeArgument) {
        FullyQualifiedJavaType optionalFullyQualifiedJavaType = getOptionalFullyQualifiedJavaType();
        optionalFullyQualifiedJavaType.addTypeArgument(typeArgument);
        return optionalFullyQualifiedJavaType;
    }

    public static FullyQualifiedJavaType getCriteriaInstance() {
        if (criteriaInstance == null) {
            criteriaInstance = new FullyQualifiedJavaType("Criteria");
        }

        return criteriaInstance;
    }

    public static FullyQualifiedJavaType getGeneratedCriteriaInstance() {
        if (generatedCriteriaInstance == null) {
            generatedCriteriaInstance = new FullyQualifiedJavaType(
                    "GeneratedCriteria");
        }

        return generatedCriteriaInstance;
    }

    @Override
    public int compareTo(FullyQualifiedJavaType other) {
        return getFullyQualifiedName().compareTo(other.getFullyQualifiedName());
    }

    public void addTypeArgument(FullyQualifiedJavaType type) {
        typeArguments.add(type);
    }

    private void parse(String fullTypeSpecification) {
        String spec = fullTypeSpecification.trim();

        if (spec.startsWith("?")) {
            wildcardType = true;
            spec = spec.substring(1).trim();
            if (spec.startsWith("extends ")) {
                boundedWildcard = true;
                extendsBoundedWildcard = true;
                spec = spec.substring(8);  // "extends ".length()
            } else if (spec.startsWith("super ")) {
                boundedWildcard = true;
                extendsBoundedWildcard = false;
                spec = spec.substring(6);  // "super ".length()
            } else {
                boundedWildcard = false;
            }
            parse(spec);
        } else {
            int index = fullTypeSpecification.indexOf('<');
            if (index == -1) {
                simpleParse(fullTypeSpecification);
            } else {
                simpleParse(fullTypeSpecification.substring(0, index));
                int endIndex = fullTypeSpecification.lastIndexOf('>');
                if (endIndex == -1) {
                    throw new RuntimeException(getString(
                            "RuntimeError.22", fullTypeSpecification));
                }
                genericParse(fullTypeSpecification.substring(index, endIndex + 1));
            }

            // this is far from a perfect test for detecting arrays, but is close
            // enough for most cases.  It will not detect an improperly specified
            // array type like byte], but it will detect byte[] and byte[   ]
            // which are both valid
            isArray = fullTypeSpecification.endsWith("]");
        }
    }

    private void simpleParse(String typeSpecification) {
        baseQualifiedName = typeSpecification.trim();
        if (baseQualifiedName.contains(".")) {
            packageName = getPackage(baseQualifiedName);
            baseShortName = baseQualifiedName
                    .substring(packageName.length() + 1);
            int index = baseShortName.lastIndexOf('.');
            if (index != -1) {
                baseShortName = baseShortName.substring(index + 1);
            }


            explicitlyImported = !JAVA_LANG.equals(packageName);
        } else {
            baseShortName = baseQualifiedName;
            explicitlyImported = false;
            packageName = "";

            switch (baseQualifiedName) {
            case "byte":
                primitive = true;
                primitiveTypeWrapper = PrimitiveTypeWrapper.getByteInstance();
                break;
            case "short":
                primitive = true;
                primitiveTypeWrapper = PrimitiveTypeWrapper.getShortInstance();
                break;
            case "int":
                primitive = true;
                primitiveTypeWrapper = PrimitiveTypeWrapper.getIntegerInstance();
                break;
            case "long":
                primitive = true;
                primitiveTypeWrapper = PrimitiveTypeWrapper.getLongInstance();
                break;
            case "char":
                primitive = true;
                primitiveTypeWrapper = PrimitiveTypeWrapper.getCharacterInstance();
                break;
            case "float":
                primitive = true;
                primitiveTypeWrapper = PrimitiveTypeWrapper.getFloatInstance();
                break;
            case "double":
                primitive = true;
                primitiveTypeWrapper = PrimitiveTypeWrapper.getDoubleInstance();
                break;
            case "boolean":
                primitive = true;
                primitiveTypeWrapper = PrimitiveTypeWrapper.getBooleanInstance();
                break;
            default:
                primitive = false;
                primitiveTypeWrapper = null;
                break;
            }
        }
    }

    private void genericParse(String genericSpecification) {
        int lastIndex = genericSpecification.lastIndexOf('>');
        if (lastIndex == -1) {
            // shouldn't happen - should be caught already, but just in case...
            throw new RuntimeException(getString(
                    "RuntimeError.22", genericSpecification));
        }
        String argumentString = genericSpecification.substring(1, lastIndex);
        // need to find "," outside of a <> bounds
        StringTokenizer st = new StringTokenizer(argumentString, ",<>", true);
        int openCount = 0;
        StringBuilder sb = new StringBuilder();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if ("<".equals(token)) {
                sb.append(token);
                openCount++;
            } else if (">".equals(token)) {
                sb.append(token);
                openCount--;
            } else if (",".equals(token)) {
                if (openCount == 0) {
                    typeArguments
                            .add(new FullyQualifiedJavaType(sb.toString()));
                    sb.setLength(0);
                } else {
                    sb.append(token);
                }
            } else {
                sb.append(token);
            }
        }

        if (openCount != 0) {
            throw new RuntimeException(getString(
                    "RuntimeError.22", genericSpecification));
        }

        String finalType = sb.toString();
        if (stringHasValue(finalType)) {
            typeArguments.add(new FullyQualifiedJavaType(finalType));
        }
    }

    /**
     * Returns the package name of a fully qualified type.
     *
     * <p>This method calculates the package as the part of the fully qualified name up to, but not including, the last
     * element. Therefore, it does not support fully qualified inner classes. Not totally fool proof, but correct in
     * most instances.
     *
     * @param baseQualifiedName
     *            the base qualified name
     * @return the package
     */
    private static String getPackage(String baseQualifiedName) {
        int index = baseQualifiedName.lastIndexOf('.');
        return baseQualifiedName.substring(0, index);
    }

    public boolean isArray() {
        return isArray;
    }
}

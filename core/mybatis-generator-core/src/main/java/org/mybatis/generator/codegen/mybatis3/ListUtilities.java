package org.mybatis.generator.codegen.mybatis3;

import java.util.List;
import java.util.stream.Collectors;

import org.mybatis.generator.api.IntrospectedColumn;

/**
 * Couple of little utility methods to make dealing with generated always
 * columns easier.  If a column is GENERATED ALWAYS, it should not
 * be referenced on an insert or update method.
 *
 * <p>If a column is identity, it should not be referenced on an insert method.
 *
 * @author Jeff Butler
 *
 */
public class ListUtilities {

    private ListUtilities() {}

    public static List<IntrospectedColumn> removeGeneratedAlwaysColumns(List<IntrospectedColumn> columns) {
        return columns.stream()
                .filter(ic -> !ic.isGeneratedAlways())
                .collect(Collectors.toList());
    }

    public static List<IntrospectedColumn> removeIdentityAndGeneratedAlwaysColumns(List<IntrospectedColumn> columns) {
        return columns.stream()
                .filter(ic -> !ic.isGeneratedAlways() && !ic.isIdentity())
                .collect(Collectors.toList());
    }
}

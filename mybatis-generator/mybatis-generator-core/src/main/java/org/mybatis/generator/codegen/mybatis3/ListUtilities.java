package org.mybatis.generator.codegen.mybatis3;

import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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

    /**
     * 移除创建审计字段、租户字段和受控主键，保留删除标记、修改审计字段及版本字段供专门更新规则处理。
     *
     * @param columns 原始更新字段
     * @return 可参与普通更新的字段及需专门处理的修改/版本字段
     */
    public static List<IntrospectedColumn> removeImmutableAuditColumns(List<IntrospectedColumn> columns) {
        return columns.stream()
                .filter(ic -> !isImmutableAuditColumn(ic))
                .collect(Collectors.toList());
    }

    /**
     * 判断字段是否为服务端强制维护的修改审计字段。
     *
     * @param column 数据库列
     * @return 属于修改时间或修改人字段时返回 true
     */
    public static boolean isModifiedAuditColumn(IntrospectedColumn column) {
        return findDefaultColumnName(column)
                .map(columnName -> columnName == DefaultColumnNameEnum.UPDATE_TIME
                        || columnName == DefaultColumnNameEnum.UPDATE_BY)
                .orElse(false);
    }

    /**
     * 判断字段是否为服务端强制维护的版本字段。
     *
     * @param column 数据库列
     * @return 属于版本字段时返回 true
     */
    public static boolean isVersionColumn(IntrospectedColumn column) {
        return findDefaultColumnName(column)
                .map(columnName -> columnName == DefaultColumnNameEnum.VERSION)
                .orElse(false);
    }

    private static boolean isImmutableAuditColumn(IntrospectedColumn column) {
        DefaultColumnNameEnum columnName = findDefaultColumnName(column).orElse(null);
        if (columnName == null
                || columnName == DefaultColumnNameEnum.DELETE_FLAG
                || columnName == DefaultColumnNameEnum.VERSION
                || columnName == DefaultColumnNameEnum.UPDATE_TIME
                || columnName == DefaultColumnNameEnum.UPDATE_BY) {
            return false;
        }
        return columnName.auditRole() == DefaultColumnNameEnum.AuditRole.A
                || columnName.auditRole() == DefaultColumnNameEnum.AuditRole.A_PRIME;
    }

    private static Optional<DefaultColumnNameEnum> findDefaultColumnName(IntrospectedColumn column) {
        String actualColumnName = column.getActualColumnName();
        if (actualColumnName == null) {
            return Optional.empty();
        }
        return DefaultColumnNameEnum.ofColumnName(actualColumnName.toLowerCase(Locale.ROOT));
    }
}

package org.mybatis.generator.codegen.mybatis3;

import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.config.VoCacheGeneratorConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一解析字典查询、缓存模型和预取注解使用的有效排序列。
 *
 * @author vgosoft-agent
 */
public final class DictCacheOrderResolver {

    private DictCacheOrderResolver() {
    }

    /** 校验字典键及显式物理列，按原顺序补齐全部主键；无主键不阻止生成。 */
    public static List<IntrospectedColumn> resolve(AbstractIntrospectedTable table) {
        VoCacheGeneratorConfiguration config = table.getTableConfiguration().getVoCacheGeneratorConfiguration();
        requireColumn(table, config.getKeyColumn(), "keyColumn");
        List<IntrospectedColumn> result = new ArrayList<>();
        if (config.getDictCacheOrderColumns().isEmpty()) {
            for (String name : List.of(DefaultColumnNameEnum.SORT.columnName(),
                    DefaultColumnNameEnum.CREATE_TIME.columnName())) {
                table.getColumn(name).ifPresent(result::add);
            }
        } else {
            for (String name : config.getDictCacheOrderColumns()) {
                IntrospectedColumn column = requireColumn(table, name, "dictCacheOrderColumns");
                if (result.contains(column)) {
                    throw new IllegalArgumentException("dictCacheOrderColumns contains duplicate physical column: "
                            + column.getActualColumnName());
                }
                result.add(column);
            }
        }
        for (IntrospectedColumn column : table.getPrimaryKeyColumns()) {
            if (!result.contains(column)) {
                result.add(column);
            }
        }
        return List.copyOf(result);
    }

    /** 使用内省表的统一物理列匹配规则解析必需列。 */
    public static IntrospectedColumn requireColumn(AbstractIntrospectedTable table, String name, String option) {
        return table.getColumn(name).orElseThrow(() ->
                new IllegalArgumentException(option + " references missing column: " + name));
    }
}

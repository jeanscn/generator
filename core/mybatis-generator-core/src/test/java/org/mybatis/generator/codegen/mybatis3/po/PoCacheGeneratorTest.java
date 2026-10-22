package org.mybatis.generator.codegen.mybatis3.po;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证缓存模型排序元数据不会触发业务比较方法生成。
 *
 * @author vgosoft-agent
 */
class PoCacheGeneratorTest {

    @Test
    void doesNotGenerateCompareMethodWhenSortIsNotIncluded() {
        IntrospectedColumn name = column("name_", "name");
        TopLevelClass cachePo = new TopLevelClass("sample.pojo.po.ArticleCachePo");

        PoCacheGenerator.addCompareMethod(cachePo, List.of(name));

        assertThat(cachePo.getMethods()).isEmpty();
    }

    @Test
    void keepsCompareMethodWhenSortIsAnIncludedBusinessColumn() {
        TopLevelClass cachePo = new TopLevelClass("sample.pojo.po.ArticleCachePo");

        PoCacheGenerator.addCompareMethod(cachePo, List.of(column("sort_", "sort")));

        assertThat(cachePo.getMethods()).extracting("name").containsExactly("compareTo");
    }

    private IntrospectedColumn column(String actualName, String property) {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setActualColumnName(actualName);
        column.setJavaProperty(property);
        column.setFullyQualifiedJavaType(FullyQualifiedJavaType.getIntegerInstance());
        return column;
    }
}

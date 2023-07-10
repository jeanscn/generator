package org.mybatis.generator.config.factory;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.HtmlElementDescriptor;

/**
 * 默认html元素描述器工厂
 *
 */
public interface DefaultHtmlElementDescriptorFactory {

    /**
     * 获取默认html元素描述器
     * @param introspectedColumn 列
     *
     * @return html元素描述器
     */
    HtmlElementDescriptor getDefaultHtmlElementDescriptor(IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable);
}

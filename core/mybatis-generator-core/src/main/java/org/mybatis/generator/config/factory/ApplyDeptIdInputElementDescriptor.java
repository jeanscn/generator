package org.mybatis.generator.config.factory;

import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.HtmlElementDescriptor;
import com.vgosoft.core.constant.enums.view.HtmlElementDataSourceEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;

/**
 * priorityElementDescriptor 生成缓急程度的元素描述
 */
public class ApplyDeptIdInputElementDescriptor implements DefaultHtmlElementDescriptorFactory{

    @Override
    public HtmlElementDescriptor getDefaultHtmlElementDescriptor(IntrospectedColumn column, IntrospectedTable introspectedTable) {
        HtmlElementDescriptor elementDescriptor = new HtmlElementDescriptor();
        elementDescriptor.setName(column.getActualColumnName());
        elementDescriptor.setColumn(column);
        elementDescriptor.setOtherFieldName(DefaultColumnNameEnum.APPLY_DEPT_ID.otherFieldName());
        elementDescriptor.setDataSource(HtmlElementDataSourceEnum.DEPARTMENT.getCode());
        elementDescriptor.setTagType(HtmlElementTagTypeEnum.INPUT.codeName());
        return elementDescriptor;
    }
}

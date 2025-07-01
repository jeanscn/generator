package org.mybatis.generator.config.factory;

import com.vgosoft.core.constant.enums.core.UrgencyEnum;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementDataSourceEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.HtmlElementDescriptor;

/**
 * priorityElementDescriptor 生成缓急程度的元素描述
 */
public class PriorityElementDescriptor implements DefaultHtmlElementDescriptorFactory{

    @Override
    public HtmlElementDescriptor getDefaultHtmlElementDescriptor(IntrospectedColumn column, IntrospectedTable introspectedTable) {
        HtmlElementDescriptor elementDescriptor = new HtmlElementDescriptor();
        elementDescriptor.setName(column.getActualColumnName());
        elementDescriptor.setDataSource(HtmlElementDataSourceEnum.DICT_ENUM.getCode());
        elementDescriptor.setEnumClassName(UrgencyEnum.class.getCanonicalName());
        elementDescriptor.setTagType(HtmlElementTagTypeEnum.RADIO.codeName());
        elementDescriptor.setColumn(column);
        elementDescriptor.setOtherFieldName(DefaultColumnNameEnum.PRIORITY.otherFieldName());
        return elementDescriptor;
    }
}

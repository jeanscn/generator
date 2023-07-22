package org.mybatis.generator.config.factory;

import com.vgosoft.core.constant.enums.core.CommonStatusEnum;
import com.vgosoft.core.constant.enums.core.UrgencyEnum;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.custom.HtmlElementDataSourceEnum;
import org.mybatis.generator.custom.HtmlElementTagTypeEnum;

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
        elementDescriptor.setTagType(HtmlElementTagTypeEnum.RADIO.getCode());
        elementDescriptor.setColumn(column);
        elementDescriptor.setOtherFieldName(DefaultColumnNameEnum.PRIORITY.otherFieldName());
        return elementDescriptor;
    }
}

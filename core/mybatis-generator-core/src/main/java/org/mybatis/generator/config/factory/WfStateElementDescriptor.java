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
public class WfStateElementDescriptor implements DefaultHtmlElementDescriptorFactory{

    @Override
    public HtmlElementDescriptor getDefaultHtmlElementDescriptor(IntrospectedColumn column, IntrospectedTable introspectedTable) {
        HtmlElementDescriptor elementDescriptor = new HtmlElementDescriptor();
        elementDescriptor.setName(column.getActualColumnName());
        elementDescriptor.setDataSource(HtmlElementDataSourceEnum.DICT_ENUM.getCode());
        elementDescriptor.setEnumClassName("com.vgosoft.workflow.enums.WorkFlowStatusEnum");
        elementDescriptor.setTagType(HtmlElementTagTypeEnum.SELECT.codeName());
        elementDescriptor.setColumn(column);
        elementDescriptor.setOtherFieldName(DefaultColumnNameEnum.WF_STATE.otherFieldName());
        return elementDescriptor;
    }
}

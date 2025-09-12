package org.mybatis.generator.config.factory;

import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementDataSourceEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.config.HtmlElementDescriptor;

/**
 * priorityElementDescriptor 生成申请人的元素描述
 * @author cen_c
 */
public class ApplyUserIdInputElementDescriptor implements DefaultHtmlElementDescriptorFactory{

    @Override
    public HtmlElementDescriptor getDefaultHtmlElementDescriptor(IntrospectedColumn column, AbstractIntrospectedTable abstractIntrospectedTable) {
        HtmlElementDescriptor elementDescriptor = new HtmlElementDescriptor();
        elementDescriptor.setName(column.getActualColumnName());
        elementDescriptor.setColumn(column);
        elementDescriptor.setOtherFieldName(DefaultColumnNameEnum.APPLY_USER_ID.otherFieldName());
        elementDescriptor.setDataSource(HtmlElementDataSourceEnum.USER.getCode());
        elementDescriptor.setTagType(HtmlElementTagTypeEnum.INPUT.codeName());
        return elementDescriptor;
    }
}

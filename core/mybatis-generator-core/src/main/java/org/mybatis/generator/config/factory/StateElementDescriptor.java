package org.mybatis.generator.config.factory;

import com.vgosoft.core.constant.enums.core.CommonStatusEnum;
import com.vgosoft.core.constant.enums.db.DefultColumnNameEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.HtmlElementDescriptor;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-07-10 05:13
 * @version 4.0
 */
public class StateElementDescriptor implements DefaultHtmlElementDescriptorFactory{

    @Override
    public HtmlElementDescriptor getDefaultHtmlElementDescriptor(IntrospectedColumn column, IntrospectedTable introspectedTable) {
        HtmlElementDescriptor elementDescriptor = new HtmlElementDescriptor();
        elementDescriptor.setName(column.getActualColumnName());
        elementDescriptor.setDataSource("DictEnum");
        elementDescriptor.setEnumClassName(CommonStatusEnum.class.getCanonicalName());
        elementDescriptor.setSwitchText(CommonStatusEnum.switchText());
        elementDescriptor.setTagType("switch");
        elementDescriptor.setColumn(column);
        elementDescriptor.setOtherFieldName(DefultColumnNameEnum.STATE.otherFieldName());
        return elementDescriptor;
    }
}

package org.mybatis.generator.config.factory;

import com.vgosoft.core.constant.enums.core.YesNoEnum;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.ConfigUtil;
import org.mybatis.generator.config.HtmlElementDescriptor;
import com.vgosoft.core.constant.enums.view.HtmlElementDataSourceEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-07-10 05:13
 * @version 4.0
 */
public class YesNoElementDescriptor implements DefaultHtmlElementDescriptorFactory{

    @Override
    public HtmlElementDescriptor getDefaultHtmlElementDescriptor(IntrospectedColumn column, IntrospectedTable introspectedTable) {
        HtmlElementDescriptor elementDescriptor = new HtmlElementDescriptor();
        elementDescriptor.setName(column.getActualColumnName());
        elementDescriptor.setDataSource(HtmlElementDataSourceEnum.DICT_ENUM.getCode());
        elementDescriptor.setEnumClassName(YesNoEnum.class.getCanonicalName());
        elementDescriptor.setSwitchText(YesNoEnum.switchText());
        elementDescriptor.setTagType(HtmlElementTagTypeEnum.SWITCH.codeName());
        elementDescriptor.setColumn(column);
        DefaultColumnNameEnum columnNameEnum = DefaultColumnNameEnum.ofColumnName(column.getActualColumnName()).orElse(null);
        if (columnNameEnum != null) {
            elementDescriptor.setOtherFieldName(columnNameEnum.otherFieldName());
        } else {
            elementDescriptor.setOtherFieldName(ConfigUtil.getOverrideJavaProperty(column.getJavaProperty()));
        }
        return elementDescriptor;
    }
}

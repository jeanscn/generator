package org.mybatis.generator.config.factory;

import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.ConfigUtil;
import org.mybatis.generator.config.HtmlElementDescriptor;
import com.vgosoft.core.constant.enums.view.HtmlElementDataSourceEnum;
import org.mybatis.generator.internal.util.Mb3GenUtil;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-07-10 05:13
 * @version 4.0
 */
public class ParentIdElementDescriptor implements DefaultHtmlElementDescriptorFactory{

    @Override
    public HtmlElementDescriptor getDefaultHtmlElementDescriptor(IntrospectedColumn column, IntrospectedTable introspectedTable) {
        HtmlElementDescriptor elementDescriptor = new HtmlElementDescriptor();
        elementDescriptor.setName(column.getActualColumnName());
        elementDescriptor.setTagType("select");
        elementDescriptor.setDataSource(HtmlElementDataSourceEnum.DICT.getCode());
        elementDescriptor.setBeanName(ConfigUtil.getIntrospectedTableBeanName(introspectedTable.getTableConfiguration()));
        elementDescriptor.setApplyProperty(DefaultColumnNameEnum.NAME.fieldName());
        elementDescriptor.setDataUrl(Mb3GenUtil.getControllerBaseMappingPath(introspectedTable) + "/tree");
        elementDescriptor.setColumn(column);
        elementDescriptor.setOtherFieldName(DefaultColumnNameEnum.PARENT_ID.otherFieldName());
        return elementDescriptor;
    }
}

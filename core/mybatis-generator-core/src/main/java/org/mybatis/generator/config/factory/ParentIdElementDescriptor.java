package org.mybatis.generator.config.factory;

import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementDataSourceEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.ConfigUtil;
import org.mybatis.generator.config.HtmlElementDescriptor;
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
        if (VStringUtil.stringHasValue(introspectedTable.getContext().getVueEndProjectPath())) {
            elementDescriptor.setTagType(HtmlElementTagTypeEnum.CASCADER.codeName());
            elementDescriptor.setRemoteToTree(true);
            elementDescriptor.setRemoteApiParse(true);
            elementDescriptor.setRemoteAsync(true);
            elementDescriptor.setMultiple(false);
            elementDescriptor.setDataUrl(Mb3GenUtil.getControllerBaseMappingPath(introspectedTable) + "/async-tree");
            elementDescriptor.setExcludeSelf(true);
        }else{
            elementDescriptor.setTagType(HtmlElementTagTypeEnum.SELECT.codeName());
            elementDescriptor.setDataUrl(Mb3GenUtil.getControllerBaseMappingPath(introspectedTable) + "/tree");
        }
        elementDescriptor.setDataSource(HtmlElementDataSourceEnum.DICT.getCode());
        elementDescriptor.setBeanName(ConfigUtil.getIntrospectedTableBeanName(introspectedTable.getTableConfiguration()));
        elementDescriptor.setApplyProperty(DefaultColumnNameEnum.NAME.fieldName());

        elementDescriptor.setColumn(column);
        elementDescriptor.setOtherFieldName(DefaultColumnNameEnum.PARENT_ID.otherFieldName());
        return elementDescriptor;
    }
}

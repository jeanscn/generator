package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.internal.util.StringUtility;

import static org.mybatis.generator.custom.ConstantsUtil.COM_SEL_SQL_PARAMETER;

public class SelectBySqlConditionElementGenerator extends AbstractXmlElementGenerator{


    private final boolean isSub;

    public SelectBySqlConditionElementGenerator(boolean isSub) {
        super();
        this.isSub = isSub;
    }

    @Override
    public void addElements(XmlElement parentElement) {
        StringBuilder sb = new StringBuilder();
        //追加selectBySqlCondition or selectBySqlConditionSub方法
        FullyQualifiedJavaType sqlParam = new FullyQualifiedJavaType(COM_SEL_SQL_PARAMETER);
        XmlElement selectBySqlCondition = new XmlElement("select");
        if (isSub) {
            selectBySqlCondition.addAttribute(new Attribute("id", "selectBySqlConditionSub"));
        } else {
            selectBySqlCondition.addAttribute(new Attribute("id", "selectBySqlCondition"));
        }
        selectBySqlCondition.addAttribute(new Attribute("parameterType", sqlParam.getFullyQualifiedName()));
        selectBySqlCondition.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        context.getCommentGenerator().addComment(selectBySqlCondition);
        //select
        selectBySqlCondition.addElement(new TextElement("select")); //$NON-NLS-1$
        //distinct
        XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "distinct != null")); //$NON-NLS-1$ //$NON-NLS-2$
        ifElement.addElement(new TextElement("distinct")); //$NON-NLS-1$
        selectBySqlCondition.addElement(ifElement);
        //include
        XmlElement xmlElement = new XmlElement("include");
        xmlElement.addAttribute(new Attribute("refid", introspectedTable.getBaseColumnListId()));
        selectBySqlCondition.addElement(xmlElement);
        //from
        sb.setLength(0);
        sb.append("from "); //$NON-NLS-1$
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        selectBySqlCondition.addElement(new TextElement(sb.toString()));
        //where
        if (!isSub) {
            xmlElement = new XmlElement("if");
            xmlElement.addAttribute(new Attribute("test", "condition != null and  condition !=''"));
            xmlElement.addElement(new TextElement("where ${condition}"));
            selectBySqlCondition.addElement(xmlElement);
        } else {
            //<where>
            XmlElement whereElement = new XmlElement("where");
            //<if test="condition != null and  condition !=''">
            //    ${condition}
            //</if>
            xmlElement = new XmlElement("if");
            xmlElement.addAttribute(new Attribute("test", "condition != null and  condition !=''"));
            xmlElement.addElement(new TextElement("${condition}"));
            whereElement.addElement(xmlElement);

            xmlElement = new XmlElement("if");
            xmlElement.addAttribute(new Attribute("test", "principals != null and principals.size &gt;0"));
            sb.setLength(0);

            if (introspectedTable.getPrimaryKeyColumns().size() > 0) {
                IntrospectedColumn introspectedColumn = introspectedTable.getPrimaryKeyColumns().get(0);
                String actualColumnName = introspectedColumn.getActualColumnName();
                if (!StringUtility.isEmpty(actualColumnName)) {
                    sb.append("and ").append(actualColumnName).append(" in(");
                } else {
                    sb.append("and ").append(DefaultColumnNameEnum.ID.columnName()).append(" in(");
                }
            } else {
                sb.append("and id_ in(");
            }


            sb.append("select distinct business_key from sys_ru_authority ");
            sb.append("where authority_name in");
            xmlElement.addElement(new TextElement(sb.toString()));
            //<foreach close=")" collection="principals" index="index" item="principal" open="(" separator=",">
            //  #{principal}
            //</foreach>
            XmlElement forEachElement = new XmlElement("foreach");
            forEachElement.addAttribute(new Attribute("close", ")"));
            forEachElement.addAttribute(new Attribute("collection", "principals"));
            forEachElement.addAttribute(new Attribute("index", "index"));
            forEachElement.addAttribute(new Attribute("item", "principal"));
            forEachElement.addAttribute(new Attribute("open", "("));
            forEachElement.addAttribute(new Attribute("separator", ","));
            forEachElement.addElement(new TextElement("#{principal}"));
            xmlElement.addElement(forEachElement);
            xmlElement.addElement(new TextElement(")"));
            whereElement.addElement(xmlElement);
            selectBySqlCondition.addElement(whereElement);
        }
        //orderby
        xmlElement = new XmlElement("if");
        xmlElement.addAttribute(new Attribute("test", "orderby != null and orderby !=''"));
        xmlElement.addElement(new TextElement("order by ${orderby}"));
        selectBySqlCondition.addElement(xmlElement);
        parentElement.addElement(selectBySqlCondition);
    }
}

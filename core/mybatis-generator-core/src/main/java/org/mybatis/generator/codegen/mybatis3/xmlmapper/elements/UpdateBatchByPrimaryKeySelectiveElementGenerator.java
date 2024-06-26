package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

public class UpdateBatchByPrimaryKeySelectiveElementGenerator extends AbstractXmlElementGenerator {

    public UpdateBatchByPrimaryKeySelectiveElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("update"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("id", introspectedTable.getUpdateBatchStatementId())); //$NON-NLS-1$

        String parameterType;
        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            parameterType = introspectedTable.getRecordWithBLOBsType();
        } else {
            parameterType = introspectedTable.getBaseRecordType();
        }
        answer.addAttribute(new Attribute("parameterType", parameterType)); //$NON-NLS-1$
        context.getCommentGenerator().addComment(answer);

        StringBuilder sb = new StringBuilder();
        sb.append("update "); //$NON-NLS-1$
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        XmlElement trimSetElement = new XmlElement("trim");
        trimSetElement.addAttribute(new Attribute("prefix", "set"));
        trimSetElement.addAttribute(new Attribute("suffixOverrides", ","));
        answer.addElement(trimSetElement);

        for (IntrospectedColumn introspectedColumn :
                ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns())) {

            XmlElement fieldSetTrimElement = new XmlElement("trim");
            fieldSetTrimElement.addAttribute(new Attribute("prefix", introspectedColumn.getActualColumnName()+" = case id_"));
            fieldSetTrimElement.addAttribute(new Attribute("suffix", "end,"));
            trimSetElement.addElement(fieldSetTrimElement);

            XmlElement foreachListField = new XmlElement("foreach");
            foreachListField.addAttribute(new Attribute("collection", "list"));
            foreachListField.addAttribute(new Attribute("item", "item"));
            foreachListField.addAttribute(new Attribute("index", "index"));
            foreachListField.addAttribute(new Attribute("separator", " "));
            foreachListField.addAttribute(new Attribute("open", ""));
            foreachListField.addAttribute(new Attribute("close", ""));
            fieldSetTrimElement.addElement(foreachListField);

            XmlElement testItemIf = new XmlElement("if");
            testItemIf.addAttribute(new Attribute("test", "item != null and item."+introspectedColumn.getJavaProperty()+" != null"));
            foreachListField.addElement(testItemIf);

            sb.setLength(0);
            sb.append("when #{item.id} then ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn,"item."));
            testItemIf.addElement(new TextElement(sb.toString()));
        }

        answer.addElement(new TextElement("where id_ in"));
        XmlElement foreachListId = new XmlElement("foreach");
        foreachListId.addAttribute(new Attribute("collection", "list"));
        foreachListId.addAttribute(new Attribute("item", "item"));
        foreachListId.addAttribute(new Attribute("index", "index"));
        foreachListId.addAttribute(new Attribute("separator", ","));
        foreachListId.addAttribute(new Attribute("open", "("));
        foreachListId.addAttribute(new Attribute("close", ")"));
        foreachListId.addElement(new TextElement("#{item.id}"));
        answer.addElement(foreachListId);

        if (context.getPlugins().sqlMapUpdateBatchElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}

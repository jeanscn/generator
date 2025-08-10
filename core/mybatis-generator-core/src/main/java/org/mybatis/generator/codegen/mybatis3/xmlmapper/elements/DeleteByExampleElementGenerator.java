package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * @author cen_c
 */
public class DeleteByExampleElementGenerator extends AbstractXmlElementGenerator {

    public DeleteByExampleElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("delete");

        answer.addAttribute(new Attribute("id", introspectedTable.getDeleteByExampleStatementId()));
        answer.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));

        context.getCommentGenerator().addComment(answer);

        String s = "delete "+introspectedTable.getTableConfiguration().getAlias()+" from " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        answer.addElement(new TextElement(s));
        answer.addElement(getExampleIncludeElement());
        // 增加if判断，防止传入的参数为null或空
        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "_parameter == null or _parameter.oredCriteria == null or _parameter.oredCriteria.size() == 0"));
        ifElement.addElement(new TextElement("-- 防止无条件删除，返回影响0行"));
        XmlElement where = new XmlElement("where");
        where.addElement(new TextElement("1 = 0"));
        ifElement.addElement(where);
        answer.addElement(ifElement);
        if (context.getPlugins().sqlMapDeleteByExampleElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}

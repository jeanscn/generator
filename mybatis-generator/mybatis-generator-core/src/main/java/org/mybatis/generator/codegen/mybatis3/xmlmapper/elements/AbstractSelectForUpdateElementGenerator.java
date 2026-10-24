package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import com.vgosoft.core.db.query.DatabaseType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.dbstrategy.SelectForUpdateDialect;
import org.mybatis.generator.codegen.mybatis3.javamapper.SelectForUpdateGuardGenerator;

/**
 * 共享锁定查询的 XML 语句结构、模型参数、缓存属性与门禁绑定。
 *
 * @author vgosoft
 */
public abstract class AbstractSelectForUpdateElementGenerator extends AbstractXmlElementGenerator {
    /**
     * 创建指定方言的锁定语句，不负责判断生成规则或添加到文档。
     *
     * @param database 目标方言
     * @param example 是否按条件查询
     * @return 锁定查询 XML 元素
     */
    protected XmlElement select(DatabaseType database, boolean example) {
        XmlElement answer = new XmlElement("select");
        answer.addAttribute(new Attribute("id", example ? "selectByExampleForUpdate" : "selectByPrimaryKeyForUpdate"));
        answer.addAttribute(new Attribute("databaseId", database.getCode()));
        answer.addAttribute(new Attribute("useCache", "false"));
        answer.addAttribute(new Attribute("flushCache", "true"));
        answer.addAttribute(new Attribute("resultMap", introspectedTable.getRules().generateResultMapWithBlobs()
                ? introspectedTable.getResultMapWithBlobsId() : introspectedTable.getBaseResultMapId()));
        String parameterType = example ? introspectedTable.getExampleType()
                : introspectedTable.getRules().generatePrimaryKeyClass() ? introspectedTable.getPrimaryKeyType()
                : introspectedTable.getPrimaryKeyColumns().size() > 1 ? "map"
                : introspectedTable.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType().getFullyQualifiedName();
        answer.addAttribute(new Attribute("parameterType", parameterType));
        context.getCommentGenerator().addComment(answer);
        XmlElement guard = new XmlElement("bind");
        guard.addAttribute(new Attribute("name", "_selectForUpdateGuard"));
        guard.addAttribute(new Attribute("value", "@" + SelectForUpdateGuardGenerator.guardType(introspectedTable)
                + "@" + (example ? "requireSelectForUpdateExample(_parameter)" : "requireSelectForUpdateTransaction()")));
        answer.addElement(guard);
        answer.addElement(new TextElement("select"));
        answer.addElement(getBaseColumnListElement());
        if (introspectedTable.hasBlobColumns()) {
            answer.addElement(new TextElement(","));
            answer.addElement(getBlobColumnListElement());
        }
        boolean hint = SelectForUpdateDialect.usesTableHint(database);
        answer.addElement(new TextElement("from " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()
                + (hint ? " with (updlock, holdlock)" : "")));
        if (example) {
            XmlElement include = new XmlElement("include");
            include.addAttribute(new Attribute("refid", introspectedTable.getExampleWhereClauseId()));
            answer.addElement(include);
        } else {
            buildPrimaryKeyWhereClause().forEach(answer::addElement);
        }
        if (!hint) {
            answer.addElement(new TextElement("for update"));
        }
        return answer;
    }
}

package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import io.swagger.models.Xml;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;
import org.mybatis.generator.config.VOCacheGeneratorConfiguration;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

public class SelectByKeysDictElementGenerator extends AbstractXmlElementGenerator {

    public SelectByKeysDictElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("select");
        answer.addAttribute(new Attribute("id", introspectedTable.getSelectByKeysDictStatementId()));
        answer.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));

        answer.addAttribute(new Attribute("parameterType", "com.vgosoft.core.pojo.parameter.SelDictByKeysParam")); //$NON-NLS-1$
        context.getCommentGenerator().addComment(answer);

        answer.addElement(new TextElement("select "));
        answer.addElement(getBaseColumnListElement());
        String sb = "from " +
                introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        answer.addElement(new TextElement(sb));

        VOCacheGeneratorConfiguration config = introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration();
        XmlElement where = new XmlElement("where");
        answer.addElement(where);
        XmlElement trim = createBracketTrim();
        where.addElement(trim);
        //choose条件
        XmlElement choose = new XmlElement("choose");
        trim.addElement(choose);
        if (stringHasValue(config.getKeyColumn()) && introspectedTable.getColumn(config.getKeyColumn()).isPresent()) {
            introspectedTable.getColumn(config.getKeyColumn()).ifPresent(introspectedColumn -> {
                choose.addElement(getWhenMultiKeyInElement(introspectedColumn, "keys"));
            });
        }else{
            introspectedTable.getPrimaryKeyColumns().forEach(introspectedColumn -> {
                choose.addElement(getWhenMultiKeyInElement(introspectedColumn, "keys"));
            });
        }
        introspectedTable.getColumn(config.getTypeColumn()).ifPresent(introspectedColumn -> {
            XmlElement choose1 = new XmlElement("choose");
            trim.addElement(choose1);
            choose1.addElement(getWhenMultiKeyInElement(introspectedColumn,"types"));
        });
        //增加默认排序
        TextElement defaultOrderBy = buildOrderByDefault();
        if (defaultOrderBy != null) {
            answer.addElement(defaultOrderBy);
        }
        if (context.getPlugins().sqlMapSelectByKeysDictElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}

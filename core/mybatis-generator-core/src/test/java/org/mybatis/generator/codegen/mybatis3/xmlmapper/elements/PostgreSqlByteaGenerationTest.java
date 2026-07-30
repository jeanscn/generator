package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.DefaultXmlFormatter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.XmlConstants;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class PostgreSqlByteaGenerationTest {

    private static final String BYTE_ARRAY_TYPE_HANDLER = "org.apache.ibatis.type.ByteArrayTypeHandler";

    @Test
    void shouldGenerateBlobJdbcTypeAndByteArrayTypeHandlerForByteaColumns() {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setActualColumnName("payload");
        column.setJavaProperty("payload");
        column.setJdbcTypeName("BLOB");
        column.setTypeHandler(BYTE_ARRAY_TYPE_HANDLER);
        column.setFullyQualifiedJavaType(new FullyQualifiedJavaType("byte[]"));

        String parameterClause = MyBatis3FormattingUtilities.getParameterClause(column);
        assertThat(parameterClause)
                .isEqualTo("#{payload,jdbcType=BLOB,typeHandler=" + BYTE_ARRAY_TYPE_HANDLER + "}");

        XmlElement root = new XmlElement("mapper");

        XmlElement resultMap = new XmlElement("resultMap");
        resultMap.addAttribute(new Attribute("id", "BaseResultMap"));
        resultMap.addAttribute(new Attribute("type", "example.Row"));
        new TestXmlElementGenerator()
                .buildResultMapItems(AbstractXmlElementGenerator.ResultElementType.RESULT, Collections.singletonList(column))
                .forEach(resultMap::addElement);
        root.addElement(resultMap);

        XmlElement insert = new XmlElement("insert");
        insert.addAttribute(new Attribute("id", "insertSelective"));
        insert.addElement(new TextElement("values (" + parameterClause + ")"));
        root.addElement(insert);

        Document document = new Document(
                XmlConstants.MYBATIS3_MAPPER_PUBLIC_ID,
                XmlConstants.MYBATIS3_MAPPER_SYSTEM_ID);
        document.setRootElement(root);

        String xml = new DefaultXmlFormatter().getFormattedContent(document);
        assertThat(xml).contains("jdbcType=\"BLOB\"");
        assertThat(xml).contains("typeHandler=\"" + BYTE_ARRAY_TYPE_HANDLER + "\"");
        assertThat(xml).contains("jdbcType=BLOB");
        assertThat(xml).contains("typeHandler=" + BYTE_ARRAY_TYPE_HANDLER);
    }

    private static class TestXmlElementGenerator extends AbstractXmlElementGenerator {
        @Override
        public void addElements(XmlElement parentElement) {
        }
    }
}

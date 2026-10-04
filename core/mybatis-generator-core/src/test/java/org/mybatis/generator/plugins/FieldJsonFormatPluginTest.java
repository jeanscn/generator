package org.mybatis.generator.plugins;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieldJsonFormatPluginTest {

    @Test
    void shouldGenerateJackson3SerializerAnnotationForAbstractVoJsonColumn() {
        Field field = new Field("jsonRecord", new FullyQualifiedJavaType("java.lang.String"));
        TopLevelClass type = new TopLevelClass("example.RecordVo");
        type.addField(field);
        IntrospectedColumn column = column("JSON", "java.lang.String");

        new FieldJsonFormatPlugin().voAbstractFieldGenerated(field, type, column, null);

        String source = new DefaultJavaFormatter().getFormattedContent(type);
        assertThat(source)
                .contains("import tools.jackson.databind.annotation.JsonSerialize;")
                .contains("import com.vgosoft.system.parser.JsonRecordSerializer;")
                .contains("@JsonSerialize(using = JsonRecordSerializer.class)")
                .doesNotContain("com.fasterxml.jackson.databind");
    }

    @Test
    void shouldGenerateJackson3SerializerAnnotationForModelVoJsonColumn() {
        Field field = new Field("jsonRecord", new FullyQualifiedJavaType("java.lang.String"));
        TopLevelClass type = new TopLevelClass("example.RecordVo");
        type.addField(field);

        new FieldJsonFormatPlugin().voModelFieldGenerated(field, type, column("json", "java.lang.String"), null);

        assertThat(new DefaultJavaFormatter().getFormattedContent(type))
                .contains("import tools.jackson.databind.annotation.JsonSerialize;")
                .contains("@JsonSerialize(using = JsonRecordSerializer.class)");
    }

    @Test
    void shouldRetainOfficialJsonFormatNamespaceForDates() {
        Field field = new Field("created", new FullyQualifiedJavaType("java.time.LocalDateTime"));
        TopLevelClass type = new TopLevelClass("example.RecordVo");
        type.addField(field);

        new FieldJsonFormatPlugin().voAbstractFieldGenerated(field, type,
                column("TIMESTAMP", "java.time.LocalDateTime"), null);

        assertThat(new DefaultJavaFormatter().getFormattedContent(type))
                .contains("import com.fasterxml.jackson.annotation.JsonFormat;")
                .contains("import org.springframework.format.annotation.DateTimeFormat;")
                .doesNotContain("JsonSerialize")
                .doesNotContain("tools.jackson.annotation");
    }

    private IntrospectedColumn column(String actualType, String javaType) {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setActualTypeName(actualType);
        column.setFullyQualifiedJavaType(new FullyQualifiedJavaType(javaType));
        return column;
    }

    @Test
    void shouldSerializeLogicalJsonWithoutChangingPhysicalTextMetadata() {
        IntrospectedColumn column = column("TEXT", "java.lang.String");
        column.getProperties().setProperty(FieldJsonFormatPlugin.LOGICAL_TYPE_PROPERTY, "JSON");
        Field field = new Field("jsonRecord", FullyQualifiedJavaType.getStringInstance());
        TopLevelClass type = new TopLevelClass("example.RecordVo");
        type.addField(field);

        new FieldJsonFormatPlugin().voAbstractFieldGenerated(field, type, column, null);

        assertThat(new DefaultJavaFormatter().getFormattedContent(type))
                .contains("tools.jackson.databind.annotation.JsonSerialize");
        assertThat(column.getActualTypeName()).isEqualTo("TEXT");
    }

    @Test
    void shouldNotSerializeOrdinaryText() {
        Field field = new Field("notes", FullyQualifiedJavaType.getStringInstance());
        TopLevelClass type = new TopLevelClass("example.RecordVo");
        type.addField(field);

        new FieldJsonFormatPlugin().voAbstractFieldGenerated(field, type,
                column("TEXT", "java.lang.String"), null);

        assertThat(new DefaultJavaFormatter().getFormattedContent(type)).doesNotContain("JsonSerialize");
    }

    @Test
    void shouldRejectUnknownLogicalTypeAndNonStringJson() {
        IntrospectedColumn unknown = column("TEXT", "java.lang.String");
        unknown.getProperties().setProperty(FieldJsonFormatPlugin.LOGICAL_TYPE_PROPERTY, "JSNO");
        IntrospectedColumn nonString = column("INTEGER", "java.lang.Integer");
        nonString.getProperties().setProperty(FieldJsonFormatPlugin.LOGICAL_TYPE_PROPERTY, "JSON");
        FieldJsonFormatPlugin plugin = new FieldJsonFormatPlugin();
        Field field = new Field("value", FullyQualifiedJavaType.getStringInstance());
        TopLevelClass type = new TopLevelClass("example.RecordVo");

        assertThatThrownBy(() -> plugin.voAbstractFieldGenerated(field, type, unknown, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unsupported logicalType");
        assertThatThrownBy(() -> plugin.voAbstractFieldGenerated(field, type, nonString, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("requires a String");
    }

    @Test
    void shouldParseLogicalJsonUsingExistingColumnOverridePropertyContract() throws Exception {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE generatorConfiguration PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
                  "mybatis-generator-config_1_7_0.dtd">
                <generatorConfiguration>
                  <context id="offline" targetRuntime="MyBatis3" appKeyword="offline"
                    moduleKeyword="offline" moduleName="offline" jdkVersion="21">
                    <jdbcConnection driverClass="unused" connectionURL="jdbc:invalid:never-connected"/>
                    <javaModelGenerator targetPackage="example" targetProject="target"/>
                    <table tableName="records">
                      <columnOverride column="json_record">
                        <property name="logicalType" value="JSON"/>
                      </columnOverride>
                    </table>
                  </context>
                </generatorConfiguration>
                """;
        Configuration configuration = new ConfigurationParser(new ArrayList<>())
                .parseConfiguration(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        assertThat(configuration.getContexts().get(0).getTableConfigurations().get(0)
                .getColumnOverride("json_record").getProperty(FieldJsonFormatPlugin.LOGICAL_TYPE_PROPERTY))
                .isEqualTo("JSON");
    }
}

package org.mybatis.generator.config.xml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.exception.XMLParserException;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 验证外部 Properties/YAML 配置的格式识别与路径解析。
 *
 * @author vgosoft-agent
 */
class ExternalPropertiesConfigurationTest {

    @TempDir
    Path temporaryDirectory;

    /** 相对路径按配置文件所在目录解析，三种支持的扩展名均能得到点分属性。 */
    @Test
    void loadsPropertiesAndYamlFromRelativePaths() throws Exception {
        assertPasswordFromRelativeFile(".properties", "spring.datasource.password=properties-value");
        assertPasswordFromRelativeFile(".yml", "spring:\n  datasource:\n    password: yaml-value\n");
        assertPasswordFromRelativeFile(".yaml", "spring:\n  datasource:\n    password: yaml-value\n");
    }

    /** 绝对 URL 保持 Properties 文件的既有读取方式。 */
    @Test
    void loadsPropertiesFromAbsoluteUrl() throws Exception {
        Path propertiesFile = temporaryDirectory.resolve("external.properties");
        Files.writeString(propertiesFile, "spring.datasource.password=url-value\n", StandardCharsets.ISO_8859_1);
        Path configurationFile = writeConfiguration(
                "<properties url=\"" + propertiesFile.toUri().toURL() + "\"/>");

        assertThat(readPassword(configurationFile)).isEqualTo("url-value");
    }

    /** classpath resource 仍按 Java Properties 格式加载。 */
    @Test
    void loadsPropertiesFromClasspathResource() throws Exception {
        Path configurationFile = writeConfiguration(
                "<properties resource=\"external-properties-test.properties\"/>");

        assertThat(readPassword(configurationFile)).isEqualTo("resource-value");
    }

    /** 不支持的扩展名必须显式失败。 */
    @Test
    void rejectsUnsupportedExtension() throws Exception {
        Path propertyFile = temporaryDirectory.resolve("external.conf");
        Files.writeString(propertyFile, "spring.datasource.password=value\n", StandardCharsets.UTF_8);
        Path configurationFile = writeConfiguration("<properties path=\"external.conf\"/>");

        assertThatThrownBy(() -> readPassword(configurationFile))
                .isInstanceOf(XMLParserException.class)
                .hasMessageContaining(".properties, .yml, or .yaml");
    }

    /** 从 Reader 解析时没有配置文件目录，不能静默把相对路径解释为工作目录。 */
    @Test
    void rejectsRelativePathWithoutConfigurationFileBase() {
        String xml = configurationXml("<properties path=\"external.properties\"/>");

        assertThatThrownBy(() -> new ConfigurationParser(new ArrayList<>())
                .parseConfiguration(new StringReader(xml)))
                .isInstanceOf(XMLParserException.class)
                .hasMessageContaining("file-based generator configuration");
    }

    /** YAML 语法错误不应把输入行或密码值写进异常。 */
    @Test
    void doesNotExposeYamlContentInParseErrors() throws Exception {
        String secretMarker = "do-not-print-this-value";
        Files.writeString(temporaryDirectory.resolve("application.yml"),
                "spring:\n  datasource:\n    password: " + secretMarker + "\n  invalid: [\n",
                StandardCharsets.UTF_8);
        Path configurationFile = writeConfiguration("<properties path=\"application.yml\"/>");

        assertThatThrownBy(() -> readPassword(configurationFile))
                .isInstanceOf(XMLParserException.class)
                .hasMessageContaining("Invalid YAML properties file")
                .hasMessageNotContaining(secretMarker);
    }

    /** 循环 YAML alias 必须显式拒绝，避免递归展开耗尽调用栈。 */
    @Test
    void rejectsRecursiveYamlAliases() throws Exception {
        Files.writeString(temporaryDirectory.resolve("application.yml"),
                "spring: &spring\n  datasource: *spring\n",
                StandardCharsets.UTF_8);
        Path configurationFile = writeConfiguration("<properties path=\"application.yml\"/>");

        assertThatThrownBy(() -> readPassword(configurationFile))
                .isInstanceOf(XMLParserException.class)
                .hasMessageContaining("Recursive YAML aliases are not supported");
    }

    private void assertPasswordFromRelativeFile(String extension, String contents) throws Exception {
        Path propertiesFile = temporaryDirectory.resolve("external" + extension);
        Files.writeString(propertiesFile, contents, StandardCharsets.UTF_8);
        Path configurationFile = writeConfiguration(
                "<properties path=\"external" + extension + "\"/>");

        assertThat(readPassword(configurationFile))
                .isEqualTo(extension.equals(".properties") ? "properties-value" : "yaml-value");
    }

    private Path writeConfiguration(String propertiesElement) throws Exception {
        Path configurationFile = temporaryDirectory.resolve("generatorConfig.xml");
        Files.writeString(configurationFile, configurationXml(propertiesElement), StandardCharsets.UTF_8);
        return configurationFile;
    }

    private String configurationXml(String propertiesElement) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE generatorConfiguration
                        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
                        "mybatis-generator-config_1_8_0.dtd">
                <generatorConfiguration>
                    %s
                    <context id="test" appKeyword="test" moduleKeyword="test" moduleName="test" jdkVersion="17">
                        <jdbcConnection driverClass="org.example.Driver" connectionURL="jdbc:example:test"
                                        userId="test" password="${spring.datasource.password}"/>
                        <table tableName="example"/>
                    </context>
                </generatorConfiguration>
                """.formatted(propertiesElement);
    }

    private String readPassword(Path configurationFile) throws Exception {
        Configuration configuration = new ConfigurationParser(new ArrayList<>())
                .parseConfiguration(configurationFile.toFile());
        return configuration.getContexts().get(0).getJdbcConnectionConfiguration().getPassword();
    }
}

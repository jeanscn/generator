package org.mybatis.generator.config.xml;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PropertyParserTest {

    @DisplayName("Testing MyBatisGeneratorConfigurationParser.parsePropertyTokens(String token)")
    @ParameterizedTest(name = "{index} => properties:''{0}'', token:''{1}'', expected:''{2}''")
    @MethodSource("parsePropertyTokensTestSource")
    void parsePropertyTokensTest(Properties prop, String token, String expected){
        MyBatisGeneratorConfigurationParser parser = new MyBatisGeneratorConfigurationParser(prop);
        String result = parser.parsePropertyTokens(token);
        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> parsePropertyTokensTestSource() {
        Properties properties = new Properties();
        properties.setProperty("gen.code.package", "value1");
        properties.setProperty("gen.code.mapper", "value2");
        properties.setProperty("gen.code.subpackage", "value3");

        Properties extendedProperties = new Properties();
        extendedProperties.setProperty("domain", "foo");
        extendedProperties.setProperty("project.foo", "pfoo");
        extendedProperties.setProperty("addr", "localhost");
        extendedProperties.setProperty("env.localhost", "dev");
        extendedProperties.setProperty("jdbc.pfoo.dev.url", "mysql");

        return Stream.of(
            arguments(null, "${gen.code.package}.${gen.code.mapper}.${gen.code.subpackage}",
                        "${gen.code.package}.${gen.code.mapper}.${gen.code.subpackage}"),
            arguments(null, "someValue", "someValue"),
            arguments(null, "${someValue", "${someValue"),
            arguments(properties, "${gen.code.package}", "value1"),
            arguments(properties, "${gen.code.mapper}", "value2"),
            arguments(properties, "${gen.code.subpackage}", "value3"),
            arguments(properties, "${gen.code.package}.pg", "value1.pg"),
            arguments(properties, "${gen.code.package}.${gen.code.mapper}", "value1.value2"),
            arguments(properties, "${gen.code.package}${gen.code.mapper}", "value1value2"),
            arguments(properties, "${gen.code.package}.${gen.code.mapper}.pg", "value1.value2.pg"),
            arguments(properties, "${gen.code.package}.${gen.code.mapper}.", "value1.value2."),
            arguments(properties, "${gen.code.package}.${gen.code.mapper}.${gen.code.subpackage}",
                    "value1.value2.value3"),
            arguments(extendedProperties, "${jdbc.${project.${domain}}.${env.${addr}}.url}", "mysql")
        );
    }
}

/*
 *    Copyright 2006-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.config.xml;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.mybatis.generator.codegen.XmlConstants;
import org.mybatis.generator.config.*;
import org.mybatis.generator.exception.XMLParserException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class ConfigurationParser {

    private final List<String> warnings;
    private final List<String> parseErrors;
    private final Properties extraProperties;

    public ConfigurationParser(List<String> warnings) {
        this(null, warnings);
    }

    /**
     * This constructor accepts a properties object which may be used to specify
     * an additional property set.  Typically this property set will be Ant or Maven properties
     * specified in the build.xml file or the POM.
     *
     * <p>If there are name collisions between the different property sets, they will be
     * resolved in this order:
     *
     * <ol>
     *   <li>System properties take highest precedence</li>
     *   <li>Properties specified in the &lt;properties&gt; configuration
     *       element are next</li>
     *   <li>Properties specified in this "extra" property set are
     *       lowest precedence.</li>
     * </ol>
     *
     * @param extraProperties an (optional) set of properties used to resolve property
     *     references in the configuration file
     * @param warnings any warnings are added to this array
     */
    public ConfigurationParser(Properties extraProperties, List<String> warnings) {
        super();
        this.extraProperties = extraProperties;

        if (warnings == null) {
            this.warnings = new ArrayList<>();
        } else {
            this.warnings = warnings;
        }

        parseErrors = new ArrayList<>();
    }

    public Configuration parseConfiguration(File inputFile) throws IOException,
            XMLParserException {

        FileReader fr = new FileReader(inputFile);

        return parseConfiguration(fr);
    }

    public Configuration parseConfiguration(Reader reader) throws IOException,
            XMLParserException {

        InputSource is = new InputSource(reader);

        return parseConfiguration(is);
    }

    public Configuration parseConfiguration(InputStream inputStream)
            throws IOException, XMLParserException {

        InputSource is = new InputSource(inputStream);

        return parseConfiguration(is);
    }

    private Configuration parseConfiguration(InputSource inputSource)
            throws IOException, XMLParserException {
        parseErrors.clear();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setValidating(true);

        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new ParserEntityResolver());

            ParserErrorHandler handler = new ParserErrorHandler(warnings,
                    parseErrors);
            builder.setErrorHandler(handler);

            Document document = null;
            try {
                document = builder.parse(inputSource);
            } catch (SAXParseException e) {
                throw new XMLParserException(parseErrors);
            } catch (SAXException e) {
                if (e.getException() == null) {
                    parseErrors.add(e.getMessage());
                } else {
                    parseErrors.add(e.getException().getMessage());
                }
            }

            if (document == null || !parseErrors.isEmpty()) {
                throw new XMLParserException(parseErrors);
            }

            Configuration config;
            Element rootNode = document.getDocumentElement();
            DocumentType docType = document.getDoctype();
            if (rootNode.getNodeType() == Node.ELEMENT_NODE
                    && docType.getPublicId().equals(
                            XmlConstants.MYBATIS_GENERATOR_CONFIG_PUBLIC_ID)) {
                config = parseMyBatisGeneratorConfiguration(rootNode);
            } else {
                throw new XMLParserException(getString("RuntimeError.5")); //$NON-NLS-1$
            }

            if (!parseErrors.isEmpty()) {
                throw new XMLParserException(parseErrors);
            }

            return config;
        } catch (ParserConfigurationException e) {
            parseErrors.add(e.getMessage());
            throw new XMLParserException(parseErrors);
        }
    }

    private Configuration parseMyBatisGeneratorConfiguration(Element rootNode)
            throws XMLParserException {
        MyBatisGeneratorConfigurationParser parser = new MyBatisGeneratorConfigurationParser(
                extraProperties);
        return parser.parseConfiguration(rootNode);
    }

    public void customConfig(Configuration config){
        List<Context> contexts = config.getContexts();
        for (Context context : contexts) {
            context.addProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING, "UTF-8");
            //添加generator plugin
            PluginConfiguration pluginConfiguration = new PluginConfiguration();
            pluginConfiguration.setConfigurationType("org.mybatis.generator.custom.JavaClientGeneratePlugins");
            context.addPluginConfiguration(pluginConfiguration);

            //添加commentGenerator
            CommentGeneratorConfiguration commentGeneratorConfiguration = Optional.ofNullable(context.getCommentGeneratorConfiguration())
                    .orElseGet(CommentGeneratorConfiguration::new);
            commentGeneratorConfiguration.setConfigurationType("org.mybatis.generator.custom.VgoCommentGenerator");
            commentGeneratorConfiguration.addProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING, "UTF-8");
            commentGeneratorConfiguration.addProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_ALL_COMMENTS, "false");
            commentGeneratorConfiguration.addProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_DATE, "false");
            commentGeneratorConfiguration.addProperty(PropertyRegistry.COMMENT_GENERATOR_DATE_FORMAT, "yyyy-MM-dd HH:mm");
            commentGeneratorConfiguration.addProperty(PropertyRegistry.COMMENT_GENERATOR_ADD_REMARK_COMMENTS, "true");
            context.setCommentGeneratorConfiguration(commentGeneratorConfiguration);

            //生成mybatis的类型
            context.setTargetRuntime("Mybatis3");
            context.addProperty(PropertyRegistry.CONTEXT_AUTO_DELIMIT_KEYWORDS, "true");
            context.addProperty(PropertyRegistry.CONTEXT_BEGINNING_DELIMITER, "'");
            context.addProperty(PropertyRegistry.CONTEXT_ENDING_DELIMITER, "'");
            for (TableConfiguration tableConfiguration : context.getTableConfigurations()) {
                tableConfiguration.setConfiguredModelType("flat");
            }
            context.setDefaultModelType(ModelType.FLAT);

            //类型转换
            JavaTypeResolverConfiguration javaTypeResolverConfiguration = Optional.ofNullable(context.getJavaTypeResolverConfiguration())
                    .orElseGet(JavaTypeResolverConfiguration::new);
            javaTypeResolverConfiguration.addProperty(PropertyRegistry.TYPE_RESOLVER_FORCE_BIG_DECIMALS, "true");
            context.setJavaTypeResolverConfiguration(javaTypeResolverConfiguration);

            //generator Model 配置
            JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = Optional.ofNullable(context.getJavaModelGeneratorConfiguration())
                    .orElseGet(JavaModelGeneratorConfiguration::new);
            javaModelGeneratorConfiguration.addProperty(PropertyRegistry.MODEL_GENERATOR_TRIM_STRINGS, "true");
            javaModelGeneratorConfiguration.addProperty(PropertyRegistry.ANY_CONSTRUCTOR_BASED, "false");
            String targetPackage = context.getJavaModelGeneratorConfiguration().getTargetPackage();
            javaModelGeneratorConfiguration.addProperty(PropertyRegistry.MODEL_GENERATOR_EXAMPLE_PACKAGE, targetPackage + ".example");
            context.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);
/*
            //生成html配置
            HtmlMapGeneratorConfiguration htmlMapGeneratorConfiguration =context.getHtmlMapGeneratorConfiguration()==null?new HtmlMapGeneratorConfiguration(context):context.getHtmlMapGeneratorConfiguration();
            htmlMapGeneratorConfiguration.setTargetProject(Optional.ofNullable(context.getProperty(PropertyRegistry.CONTEXT_HTML_TARGET_PROJECT))
                    .orElse("src/main/resources/templates"));
            String modelPackage = javaModelGeneratorConfiguration.getTargetPackage();
            String p = StringUtils.substringAfterLast(StringUtils.substringBeforeLast(modelPackage, "."), ".");
            htmlMapGeneratorConfiguration.setTargetPackage(Optional.ofNullable(context.getProperty(PropertyRegistry.CONTEXT_HTML_TARGET_PACKAGE))
                    .orElse(p));
            context.setHtmlMapGeneratorConfiguration(htmlMapGeneratorConfiguration);*/
        }
    }
}

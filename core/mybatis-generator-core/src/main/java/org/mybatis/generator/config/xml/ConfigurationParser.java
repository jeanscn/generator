package org.mybatis.generator.config.xml;

import org.mybatis.generator.codegen.XmlConstants;
import org.mybatis.generator.config.*;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.util.StringUtility;
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
        MyBatisGeneratorConfigurationParser parser = new MyBatisGeneratorConfigurationParser(extraProperties);
        return parser.parseConfiguration(rootNode);
    }

    public void customConfig(Configuration config){
        List<Context> contexts = config.getContexts();
        for (Context context : contexts) {
            context.addProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING, "UTF-8");
            String mybatisPlus = context.getProperty(PropertyRegistry.CONTEXT_INTEGRATE_MYBATIS_PLUS);
            if (!StringUtility.stringHasValue(mybatisPlus)) {
                context.addProperty(PropertyRegistry.CONTEXT_INTEGRATE_MYBATIS_PLUS, "true");
            }

            //添加generator plugin
            //JavaClientGeneratePlugins do nothing
            PluginConfiguration javaClientGeneratePlugins = new PluginConfiguration();
            javaClientGeneratePlugins.setConfigurationType("org.mybatis.generator.plugins.JavaClientGeneratePlugins");
            context.addPluginConfiguration(javaClientGeneratePlugins);

            //属性 - serialVersionUID
            PluginConfiguration serializablePlugin = new PluginConfiguration();
            serializablePlugin.setConfigurationType("org.mybatis.generator.plugins.SerializablePlugin");
            serializablePlugin.addProperty("suppressJavaInterface", "false");
            context.addPluginConfiguration(serializablePlugin);
            //重写方法 - EqualsHashCode方法
            PluginConfiguration equalsAndHashCode = new PluginConfiguration();
            equalsAndHashCode.setConfigurationType("org.mybatis.generator.plugins.EqualsHashCodePlugin");
            context.addPluginConfiguration(equalsAndHashCode);
            //属性 - 附加的
            PluginConfiguration generateAdditionalFilesPlugin  = new PluginConfiguration();
            generateAdditionalFilesPlugin.setConfigurationType("org.mybatis.generator.plugins.GenerateAdditionalFilesPlugin");
            context.addPluginConfiguration(generateAdditionalFilesPlugin);
            //属性 - 工作流相关
            PluginConfiguration workflowPropertyPlugin = new PluginConfiguration();
            workflowPropertyPlugin.setConfigurationType("org.mybatis.generator.plugins.WorkflowPropertyPlugin");
            context.addPluginConfiguration(workflowPropertyPlugin);
            //属性 - web相关
            PluginConfiguration modelWebPropertiesPlugin  = new PluginConfiguration();
            modelWebPropertiesPlugin.setConfigurationType("org.mybatis.generator.plugins.ModelWebPropertiesPlugin");
            context.addPluginConfiguration(modelWebPropertiesPlugin);

            //注解 - TableMeta、ColumnMeta
            PluginConfiguration tableMetaAnnotationPlugin = new PluginConfiguration();
            tableMetaAnnotationPlugin.setConfigurationType("org.mybatis.generator.plugins.TableMetaAnnotationPlugin");
            context.addPluginConfiguration(tableMetaAnnotationPlugin);
            //注解 - MybatisPlus
            PluginConfiguration mybatisPlusAnnotationPlugin = new PluginConfiguration();
            mybatisPlusAnnotationPlugin.setConfigurationType("org.mybatis.generator.plugins.MybatisPlusAnnotationPlugin");
            context.addPluginConfiguration(mybatisPlusAnnotationPlugin);
            //注解 - hibernate validator
            PluginConfiguration validatorPlugin  = new PluginConfiguration();
            validatorPlugin.setConfigurationType("org.mybatis.generator.plugins.ValidatorPlugin");
            context.addPluginConfiguration(validatorPlugin);
            //注解 - JsonFormat
            PluginConfiguration fieldJsonFormatPlugin = new PluginConfiguration();
            fieldJsonFormatPlugin.setConfigurationType("org.mybatis.generator.plugins.FieldJsonFormatPlugin");
            context.addPluginConfiguration(fieldJsonFormatPlugin);
            //注解 - ViewMeta
            PluginConfiguration viewMetaAnnotationPlugin  = new PluginConfiguration();
            viewMetaAnnotationPlugin.setConfigurationType("org.mybatis.generator.plugins.ViewMetaAnnotationPlugin");
            context.addPluginConfiguration(viewMetaAnnotationPlugin);
            //注解 - LayuiTableMeta、LayuiTableColumnMeta
            PluginConfiguration layuiTableMetaAnnotationPlugin  = new PluginConfiguration();
            layuiTableMetaAnnotationPlugin.setConfigurationType("org.mybatis.generator.plugins.LayuiTableMetaAnnotationPlugin");
            context.addPluginConfiguration(layuiTableMetaAnnotationPlugin);
            //注解 - EasyExcel
            PluginConfiguration easyExcelAnnotationPlugin  = new PluginConfiguration();
            easyExcelAnnotationPlugin.setConfigurationType("org.mybatis.generator.plugins.EasyExcelAnnotationPlugin");
            context.addPluginConfiguration(easyExcelAnnotationPlugin);
            //注解 - Swagger
            PluginConfiguration swaggerApiPlugin = new PluginConfiguration();
            swaggerApiPlugin.setConfigurationType("org.mybatis.generator.plugins.SwaggerApiPlugin");
            context.addPluginConfiguration(swaggerApiPlugin);

            //工具 - 生成js、css文件
            PluginConfiguration jqueryPlugin = new PluginConfiguration();
            jqueryPlugin.setConfigurationType("org.mybatis.generator.plugins.jquery.JQueryPlugin");
            context.addPluginConfiguration(jqueryPlugin);

            //生成html编辑器模板的片段文件
            PluginConfiguration htmlEditorPlugin = new PluginConfiguration();
            htmlEditorPlugin.setConfigurationType("org.mybatis.generator.plugins.html.HtmlFragmentsPlugin");
            context.addPluginConfiguration(htmlEditorPlugin);

            //生成vue相关注解
            PluginConfiguration vueEditorPlugin = new PluginConfiguration();
            vueEditorPlugin.setConfigurationType("org.mybatis.generator.plugins.VueHtmMetaAnnotationPlugin");
            context.addPluginConfiguration(vueEditorPlugin);

            //添加commentGenerator
            CommentGeneratorConfiguration commentGeneratorConfiguration = Optional.ofNullable(context.getCommentGeneratorConfiguration())
                    .orElseGet(CommentGeneratorConfiguration::new);
            commentGeneratorConfiguration.setConfigurationType("org.mybatis.generator.custom.VgoCommentGenerator");
            Properties commentProperties = commentGeneratorConfiguration.getProperties();
            commentGeneratorConfiguration.addProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING, "UTF-8");
            commentGeneratorConfiguration.addProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_ALL_COMMENTS, commentProperties.getProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_ALL_COMMENTS, "false"));
            commentGeneratorConfiguration.addProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_DATE, commentProperties.getProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_DATE, "true"));
            commentGeneratorConfiguration.addProperty(PropertyRegistry.COMMENT_GENERATOR_DATE_FORMAT, commentProperties.getProperty(PropertyRegistry.COMMENT_GENERATOR_DATE_FORMAT, "yyyy-MM-dd HH:mm:ss"));
            commentGeneratorConfiguration.addProperty(PropertyRegistry.COMMENT_GENERATOR_ADD_REMARK_COMMENTS, commentProperties.getProperty(PropertyRegistry.COMMENT_GENERATOR_ADD_REMARK_COMMENTS, "true"));
            context.setCommentGeneratorConfiguration(commentGeneratorConfiguration);

            //生成mybatis的类型
            context.setTargetRuntime("Mybatis3");
            context.addProperty(PropertyRegistry.CONTEXT_AUTO_DELIMIT_KEYWORDS, "true");
            context.addProperty(PropertyRegistry.CONTEXT_BEGINNING_DELIMITER, "'");
            context.addProperty(PropertyRegistry.CONTEXT_ENDING_DELIMITER, "'");
            for (TableConfiguration tableConfiguration : context.getTableConfigurations()) {
                tableConfiguration.setConfiguredModelType("flat");
            }

            //类型转换
            JavaTypeResolverConfiguration javaTypeResolverConfiguration = Optional.ofNullable(context.getJavaTypeResolverConfiguration())
                    .orElseGet(JavaTypeResolverConfiguration::new);
            javaTypeResolverConfiguration.addProperty(PropertyRegistry.TYPE_RESOLVER_FORCE_BIG_DECIMALS, "true");
            javaTypeResolverConfiguration.addProperty(PropertyRegistry.TYPE_RESOLVER_USE_JSR310_TYPES, "true");
            context.setJavaTypeResolverConfiguration(javaTypeResolverConfiguration);

            //generator Model 配置
            JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = Optional.ofNullable(context.getJavaModelGeneratorConfiguration())
                    .orElseGet(JavaModelGeneratorConfiguration::new);
            Properties properties = javaModelGeneratorConfiguration.getProperties();
            javaModelGeneratorConfiguration.addProperty(PropertyRegistry.MODEL_GENERATOR_TRIM_STRINGS, "true");
            if (!properties.containsKey(PropertyRegistry.MODEL_GENERATOR_TRIM_STRINGS)) {
                properties.put(PropertyRegistry.MODEL_GENERATOR_TRIM_STRINGS, "true");
            }
            if (!properties.containsKey(PropertyRegistry.ANY_CONSTRUCTOR_BASED)) {
                properties.put(PropertyRegistry.ANY_CONSTRUCTOR_BASED, "false");
            }
            String targetPackage = context.getJavaModelGeneratorConfiguration().getTargetPackage();
            if (!properties.containsKey(PropertyRegistry.MODEL_GENERATOR_EXAMPLE_PACKAGE)) {
                properties.put(PropertyRegistry.MODEL_GENERATOR_EXAMPLE_PACKAGE, targetPackage + ".example");
            }else{
                properties.put(PropertyRegistry.MODEL_GENERATOR_EXAMPLE_PACKAGE, targetPackage + "."+properties.get(PropertyRegistry.MODEL_GENERATOR_EXAMPLE_PACKAGE));
            }
            context.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);
        }
    }
}

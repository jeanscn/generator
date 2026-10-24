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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.mybatis.generator.custom.ConstantsUtil.DEFAULT_CHARSET;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * @author XXXX
 */
public class ConfigurationParser {

    private final List<String> warnings;
    private final List<String> parseErrors;
    private final Properties extraProperties;

    public ConfigurationParser(List<String> warnings) {
        this(null, warnings);
        // 设置控制台编码
        setupConsoleEncoding();
    }
    /**
      此构造函数接受一个 properties 对象，该对象可用于指定其他属性集。
      通常，此属性集将在 build.xml 文件或 POM 中指定的 Ant 或 Maven 属性。<p>如果不同属性集之间存在名称冲突，则按以下顺序解决它们：
      <ol>
          <li>系统属性具有最高优先级<li>
          <li>&lt;properties&gt;配置元素中指定的属性是下一个<li>
          <li>此“extra”属性集中指定的属性是最低优先级。<li>
      <ol>
      @param extraProperties 的一组（可选）属性，用于解析配置文件中的属性引用
     * @param warnings，任何警告都会添加到此数组中
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

        // 设置控制台编码
        setupConsoleEncoding();
    }

    public Configuration parseConfiguration(File inputFile) throws IOException,XMLParserException {
        //FileReader fr = new FileReader(inputFile);
        Reader fr = new InputStreamReader(Files.newInputStream(inputFile.toPath()), DEFAULT_CHARSET);
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
        inputSource = checkCasAttributeLiterals(inputSource);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setValidating(true);

        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new ParserEntityResolver());

            ParserErrorHandler handler = new ParserErrorHandler(warnings,parseErrors);
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
                throw new XMLParserException(getString("RuntimeError.5"));
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

    /**
     * 在枚举 DTD 将属性空白折叠之前，按统一表配置规则检查原始 CAS 属性值。
     *
     * @param source 原始输入
     * @return 内容不变、可供正常验证解析的输入
     * @throws IOException 读取配置失败
     * @throws XMLParserException 原始配置不是合法 XML
     */
    private InputSource checkCasAttributeLiterals(InputSource source) throws IOException, XMLParserException {
        InputSource probe;
        InputSource original;
        if (source.getCharacterStream() != null) {
            var writer = new java.io.StringWriter();
            source.getCharacterStream().transferTo(writer);
            String text = writer.toString();
            original = new InputSource(new java.io.StringReader(text));
            if (!text.contains(PropertyRegistry.TABLE_ENABLE_CAS_WRITE_CONTRACT)) { return original; }
            probe = new InputSource(new java.io.StringReader(text));
        } else {
            byte[] bytes = source.getByteStream().readAllBytes();
            original = new InputSource(new java.io.ByteArrayInputStream(bytes));
            probe = new InputSource(new java.io.ByteArrayInputStream(bytes));
        }
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            var tables = factory.newDocumentBuilder().parse(probe).getElementsByTagName("table");
            for (int i = 0; i < tables.getLength(); i++) {
                var table = (Element) tables.item(i);
                if (table.hasAttribute(PropertyRegistry.TABLE_ENABLE_CAS_WRITE_CONTRACT)) {
                    var configuration = new TableConfiguration(new Context(null), warnings);
                    configuration.setTableName(table.getAttribute("tableName"));
                    configuration.parseEnableCasWriteContract(table.getAttribute(PropertyRegistry.TABLE_ENABLE_CAS_WRITE_CONTRACT));
                }
            }
            return original;
        } catch (ParserConfigurationException | SAXException exception) {
            throw new XMLParserException(java.util.List.of(exception.getMessage()));
        }
    }

    private Configuration parseMyBatisGeneratorConfiguration(Element rootNode)
            throws XMLParserException {
        MyBatisGeneratorConfigurationParser parser = new MyBatisGeneratorConfigurationParser(extraProperties,warnings);
        return parser.parseConfiguration(rootNode);
    }

    public void customConfig(Configuration config) {
        List<Context> contexts = config.getContexts();
        for (Context context : contexts) {
            ensureUtf8Encoding(context);

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
            PluginConfiguration generateAdditionalFilesPlugin = new PluginConfiguration();
            generateAdditionalFilesPlugin.setConfigurationType("org.mybatis.generator.plugins.GenerateAdditionalFilesPlugin");
            context.addPluginConfiguration(generateAdditionalFilesPlugin);
            //属性 - 工作流相关
            PluginConfiguration workflowPropertyPlugin = new PluginConfiguration();
            workflowPropertyPlugin.setConfigurationType("org.mybatis.generator.plugins.WorkflowPropertyPlugin");
            context.addPluginConfiguration(workflowPropertyPlugin);
            //属性 - web相关
            PluginConfiguration modelWebPropertiesPlugin = new PluginConfiguration();
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
            PluginConfiguration validatorPlugin = new PluginConfiguration();
            validatorPlugin.setConfigurationType("org.mybatis.generator.plugins.ValidatorPlugin");
            context.addPluginConfiguration(validatorPlugin);
            //注解 - JsonFormat
            PluginConfiguration fieldJsonFormatPlugin = new PluginConfiguration();
            fieldJsonFormatPlugin.setConfigurationType("org.mybatis.generator.plugins.FieldJsonFormatPlugin");
            context.addPluginConfiguration(fieldJsonFormatPlugin);
            //注解 - ViewMeta
            PluginConfiguration viewMetaAnnotationPlugin = new PluginConfiguration();
            viewMetaAnnotationPlugin.setConfigurationType("org.mybatis.generator.plugins.ViewMetaAnnotationPlugin");
            context.addPluginConfiguration(viewMetaAnnotationPlugin);
            //注解 - LayuiTableMeta、LayuiTableColumnMeta
            PluginConfiguration layuiTableMetaAnnotationPlugin = new PluginConfiguration();
            layuiTableMetaAnnotationPlugin.setConfigurationType("org.mybatis.generator.plugins.LayuiTableMetaAnnotationPlugin");
            context.addPluginConfiguration(layuiTableMetaAnnotationPlugin);
            //注解 - EasyExcel
            PluginConfiguration easyExcelAnnotationPlugin = new PluginConfiguration();
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

            //工具 - 生成TypeScript类型文件
            PluginConfiguration typeScriptTypePlugin = new PluginConfiguration();
            typeScriptTypePlugin.setConfigurationType("org.mybatis.generator.plugins.FieldTypescriptTypePlugin");
            context.addPluginConfiguration(typeScriptTypePlugin);

            //工具 - 生成vue文件
            PluginConfiguration vue3FormFilesPlugin = new PluginConfiguration();
            vue3FormFilesPlugin.setConfigurationType("org.mybatis.generator.plugins.vue3.Vue3FormFilesPlugin");
            context.addPluginConfiguration(vue3FormFilesPlugin);
            PluginConfiguration vue3OtherFilesPlugin = new PluginConfiguration();
            vue3OtherFilesPlugin.setConfigurationType("org.mybatis.generator.plugins.vue3.Vue3OtherFilesPlugin");
            context.addPluginConfiguration(vue3OtherFilesPlugin);

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
            } else {
                properties.put(PropertyRegistry.MODEL_GENERATOR_EXAMPLE_PACKAGE, targetPackage + "." + properties.get(PropertyRegistry.MODEL_GENERATOR_EXAMPLE_PACKAGE));
            }
            context.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);
        }
    }

    /**
     * 设置控制台输出编码，解决中文乱码问题
     */
    private void setupConsoleEncoding() {
        try {
            // 设置系统编码为UTF-8
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("console.encoding", "UTF-8");

            // 重新设置输出流编码
            if (System.out != null) {
                System.setOut(new PrintStream(System.out, true, "UTF-8"));
            }
            if (System.err != null) {
                System.setErr(new PrintStream(System.err, true, "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            // 如果UTF-8不支持，使用默认编码
            warnings.add("无法设置UTF-8编码，使用系统默认编码");
        }
    }

    /**
     * 在 customConfig 方法开头添加
     * @param context 上下文
     */
    private void ensureUtf8Encoding(Context context) {
        // 确保Java文件编码为UTF-8
        context.addProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING, "UTF-8");

        // 确保注释生成器使用UTF-8
        CommentGeneratorConfiguration commentConfig = context.getCommentGeneratorConfiguration();
        if (commentConfig != null) {
            commentConfig.addProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING, "UTF-8");
        }
    }
}

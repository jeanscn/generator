package org.mybatis.generator.codegen.mybatis3;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.dom.DefaultXmlFormatter;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.CasWriteElementGenerator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import static org.mybatis.generator.codegen.mybatis3.CasWriteGenerationValidator.failure;

/**
 * 在最终插件回调之后校验必需输出，并聚合当前 context 的唯一版本化契约资源。
 * 不负责其他层渲染或任何运行时自动装配。
 *
 * @author vgosoft
 */
public final class CasWriteContractGeneratedFile extends AbstractGeneratedFile {
    private final String content;

    public CasWriteContractGeneratedFile(List<AbstractIntrospectedTable> tables,
                                         List<GeneratedJavaFile> javaFiles, List<GeneratedXmlFile> xmlFiles) {
        super(tables.get(0).getTableConfiguration().getSqlMapGeneratorConfiguration().getTargetProject(),
                "META-INF.vgosoft.write-contracts", module(tables.get(0)) + ".xml", tables.get(0));
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<write-contract format-version=\"managed-cas-1\" module-id=\"")
                .append(module(tables.get(0))).append("\" generator-build=\"1.8.0-SNAPSHOT\">\n")
                .append("<requires capability=\"managed-cas-1\"/>\n");
        for (var table : tables) {
            CasWriteGenerationValidator.validate(table);
            LogicalDeleteGenerationValidator.validate(table);
            checkJava(table, javaFiles);
            appendMapper(xml, table, xmlFiles);
        }
        content = xml.append("</write-contract>\n").toString();
    }

    private static String module(AbstractIntrospectedTable table) {
        String name = table.getContext().getModuleKeyword();
        if (name == null || !name.matches("[a-z][a-z0-9-]*")) {
            throw failure(table, "moduleKeyword 必须为稳定的小写模块标识");
        }
        return name;
    }

    @Override
    public String getFormattedContent() { return content; }

    private static void checkJava(AbstractIntrospectedTable table, List<GeneratedJavaFile> files) {
        String model = table.getTableConfiguration().getDomainObjectName();
        String mapper = required(table, files, "Gen" + model + "Mapper.java");
        String service = required(table, files, "IGen" + model + ".java");
        String impl = required(table, files, "Abstract" + model + "Impl.java");
        String entity = required(table, files, model + ".java");
        var implementation = (org.mybatis.generator.api.dom.java.TopLevelClass) files.stream()
                .filter(f -> f.getFileName().equals("Abstract" + model + "Impl.java"))
                .findFirst().orElseThrow().getCompilationUnit();
        for (var method : implementation.getMethods()) {
            String name = method.getName();
            if (Set.of("updateByPrimaryKeyCas", "updateByPrimaryKeySelectiveCas",
                    "insert", "insertSelective", "insertBatch").contains(name)) {
                if (!method.getAnnotations().contains("@org.springframework.transaction.annotation.Transactional("
                        + "propagation = org.springframework.transaction.annotation.Propagation.MANDATORY, rollbackFor = Exception.class)")) {
                    throw failure(table, "插件后服务方法缺少受管事务：" + name);
                }
                if (name.endsWith("Cas") && !method.getBodyLines().equals(List.of(
                        "int affected = mapper." + name + "(record, expectedVersion);",
                        "return ServiceResult.success(record, affected);"))) {
                    throw failure(table, "插件后服务 CAS 实现不得捕获冲突或改变调用：" + name);
                }
            } else if (CasWriteGenerationValidator.operations(table).getOrDefault(name, "").equals("LOGICAL_DELETE")) {
                var config = LogicalDeleteGenerationValidator.configurations(table).stream()
                        .filter(c -> c.getDeleteMethodName().equals(name)).findFirst().orElseThrow();
                if (!method.getAnnotations().contains("@org.springframework.transaction.annotation.Transactional("
                        + "propagation = org.springframework.transaction.annotation.Propagation.MANDATORY, rollbackFor = Exception.class)")
                        || !method.getBodyLines().equals(org.mybatis.generator.codegen.mybatis3.service.elements
                        .DeleteByColumnElement.logicalBody(table, config))) {
                    throw failure(table, "插件后逻辑删除实现、事务或冲突传播被改变：" + name);
                }
                String signature = config.getColumns().stream().map(c -> c.getFullyQualifiedJavaType().getShortName()
                        + " " + c.getJavaProperty()).collect(java.util.stream.Collectors.joining(", "));
                if (!service.contains(name + "(" + signature + ")")) {
                    throw failure(table, "插件后逻辑删除服务签名缺失：" + name);
                }
                String mapperSignature = config.getColumns().stream().map(c -> "@Param(\"" + c.getJavaProperty()
                        + "\") " + c.getFullyQualifiedJavaType().getShortName() + " " + c.getJavaProperty())
                        .collect(java.util.stream.Collectors.joining(", "));
                if (!mapper.contains(name + "(" + mapperSignature + ")")) {
                    throw failure(table, "插件后逻辑删除 Mapper 签名缺失：" + name);
                }
            } else if (name.startsWith("update") || name.startsWith("delete") || name.startsWith("insertOrUpdate")
                    || name.startsWith("cleanup") || name.contains("InsertUpdateBatch")) {
                if (!method.getBodyLines().equals(List.of("throw new IllegalStateException(\"ENTRY_DISABLED\");"))) {
                    throw failure(table, "插件后旧危险服务入口必须保持显式拒绝：" + name);
                }
            }
        }
        for (String method : List.of("updateByPrimaryKeyCas", "updateByPrimaryKeySelectiveCas")) {
            if (!mapper.contains(method + "(@Param(\"record\") " + model + " record, @Param(\"expectedVersion\") Integer expectedVersion)")
                    || !service.contains(method + "(" + model + " record, Integer expectedVersion)")
                    || !impl.contains("mapper." + method + "(record, expectedVersion)")
                    || !impl.contains("Propagation.MANDATORY")) {
                throw failure(table, "插件后 CAS Mapper/Service 签名或事务实现缺失：" + method);
            }
            for (var config : LogicalDeleteGenerationValidator.configurations(table)) {
                if (implementation.getMethods().stream().noneMatch(m -> m.getName().equals(config.getDeleteMethodName()))) {
                    throw failure(table, "必需逻辑删除服务实现缺失：" + config.getDeleteMethodName());
                }
            }
        }
        if (entity.matches("(?s).*\\b(?:private|protected|public)\\s+Integer\\s+version\\s*[;=].*")
                || !entity.contains("this.version = 1")) {
            throw failure(table, "模型必须继承版本且保留初始值 1，禁止隐藏同名字段");
        }
        var vo = table.getTableConfiguration().getVoGeneratorConfiguration();
        if (vo != null && vo.isGenerate()) {
            String abstractVo = required(table, files, "Abstract" + model + "Vo.java");
            String updateVo = required(table, files, model + "UpdateVo.java");
            String createVo = required(table, files, model + "CreateVo.java");
            required(table, files, model + "Mappings.java");
            if (!abstractVo.contains("@NotNull(groups = ValidateUpdate.class)")
                    || !abstractVo.contains("Integer version;") || updateVo.contains("this.version")
                    || !createVo.contains("this.version = 1")) {
                throw failure(table, "插件后 VO 版本校验、空值传播或新增默认值输出不完整");
            }
        }
    }

    private static String required(AbstractIntrospectedTable table, List<GeneratedJavaFile> files, String name) {
        var found = files.stream().filter(f -> f.getFileName().equals(name)).toList();
        if (found.size() != 1) { throw failure(table, "必需输出缺失或重复：" + name); }
        return found.get(0).getFormattedContent().replaceAll("\\s+", " ");
    }

    private static void appendMapper(StringBuilder xml, AbstractIntrospectedTable table, List<GeneratedXmlFile> files) {
        String namespace = table.getMyBatis3SqlMapNamespace();
        List<Element> roots = files.stream().map(f -> {
                    try { return parse(f.getFormattedContent()); }
                    catch (RuntimeException exception) {
                        var failure = failure(table, "插件后 XML 输出缺失或不可渲染：" + f.getFileName());
                        failure.initCause(exception);
                        throw failure;
                    }
                })
                .filter(e -> namespace.equals(e.getAttribute("namespace"))).toList();
        if (roots.size() != 1) { throw failure(table, "必需 XML 输出缺失或重复：" + namespace); }
        Element root = roots.get(0);
        xml.append("<mapper interface=\"").append(namespace).append("\" table=\"")
                .append(table.getFullyQualifiedTableNameAtRuntime()).append("\">\n<columns>\n");
        for (var c : table.getAllColumns()) {
            xml.append("<column name=\"").append(c.getActualColumnName()).append("\" property=\"")
                    .append(c.getJavaProperty()).append("\" java-type=\"")
                    .append(c.getFullyQualifiedJavaType().getFullyQualifiedName()).append("\" jdbc-type=\"")
                    .append(c.getJdbcTypeName()).append("\" role=\"").append(CasWriteGenerationValidator.role(c)).append("\"/>\n");
        }
        xml.append("</columns>\n");
        Set<String> seen = new HashSet<>();
        for (Node n = root.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (!(n instanceof Element element) || !Set.of("insert", "update", "delete").contains(n.getNodeName())) { continue; }
            String id = element.getAttribute("id");
            String db = element.getAttribute("databaseId");
            String key = id + "@" + db;
            if (!seen.add(key) || table.getTableConfiguration().getAdditionalDbTypes().stream()
                    .noneMatch(d -> d.getCode().equals(db))) { throw failure(table, "重复或未知方言写输出：" + key); }
            String operation = CasWriteGenerationValidator.operations(table).get(id);
            var expected = CasWriteElementGenerator.statement(table, id, db,
                    operation == null ? element.getTagName() : operation.startsWith("INSERT") ? "insert" : "update", operation);
            Document expectedDocument = new Document();
            expectedDocument.setRootElement(expected);
            Element expectedRoot = parse(new DefaultXmlFormatter().getFormattedContent(expectedDocument));
            if (!shape(element).equals(shape(expectedRoot))) {
                throw failure(table, "插件后写 SQL 骨架、参数、审计或版本语义被改变：" + key);
            }
            xml.append("<statement id=\"").append(namespace).append('.').append(id)
                    .append("\" database-id=\"").append(db).append("\" command=\"")
                    .append(element.getTagName().toUpperCase(java.util.Locale.ROOT)).append("\" entry=\"")
                    .append(operation == null ? "reject\" reason=\"ENTRY_DISABLED" : "allow\" operation=\"" + operation)
                    .append("\">\n");
            if (operation != null) {
                for (var c : CasWriteGenerationValidator.writes(table, operation)) {
                    xml.append("<write column=\"").append(c.getActualColumnName()).append("\"/>\n");
                }
            }
            xml.append("</statement>\n");
        }
        for (var db : table.getTableConfiguration().getAdditionalDbTypes()) {
            for (String id : CasWriteGenerationValidator.operations(table).keySet()) {
                if (!seen.contains(id + "@" + db.getCode())) { throw failure(table, "必需写输出缺失：" + id + "@" + db.getCode()); }
            }
        }
        xml.append("</mapper>\n");
    }

    private static Element parse(String xml) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return factory.newDocumentBuilder().parse(new org.xml.sax.InputSource(new StringReader(xml))).getDocumentElement();
        } catch (Exception e) { throw new IllegalArgumentException("受管输出 XML 无法解析", e); }
    }

    private static List<String> shape(Node node) {
        List<String> result = new ArrayList<>();
        result.add("<" + node.getNodeName());
        TreeMap<String, String> attributes = new TreeMap<>();
        for (int i = 0; i < node.getAttributes().getLength(); i++) {
            Node attribute = node.getAttributes().item(i);
            attributes.put(attribute.getNodeName(), attribute.getNodeValue());
        }
        attributes.forEach((name, value) -> result.add(name + "=" + value));
        StringBuilder text = new StringBuilder();
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element) {
                result.add(text.toString().replaceAll("\\s+", "")); text.setLength(0);
                result.addAll(shape(child));
            } else if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
                text.append(child.getTextContent());
            }
        }
        result.add(text.toString().replaceAll("\\s+", ""));
        result.add(">");
        return result;
    }
}

package org.mybatis.generator.offline;

import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.codegen.mybatis3.freemaker.vue3.GeneratedVueFile;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.TypeScriptTypeDescriptor;
import org.mybatis.generator.plugins.vue3.Vue3TypeImports;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用生产导入解析器和模板局部再生成四个获准类型文件的导入区，不内省或写入数据库。
 * 既有字段和类型声明逐字保留；配置、继承签名或覆盖标记不一致时拒绝写盘。
 *
 * @author vgosoft
 */
public final class Vue3TypeImportRegenerator {
    private static final List<String> DOMAINS =
            List.of("OrgDepartment", "OrgGroup", "OrgRole", "OrgTeam");
    private static final Pattern DECLARATION = Pattern.compile(
            "export\\s+interface\\s+(\\w+)\\s*(?:extends\\s+([^\\{]+))?\\{");

    private Vue3TypeImportRegenerator() {
    }

    /**
     * 读取组织模块配置，仅在显式传入 --write 时写入固定四个目标文件。
     *
     * @param args 配置文件、前端根目录以及可选的 --write
     * @throws Exception 配置或产物不符合局部再生成前提时抛出
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 3
                || (args.length == 3 && !"--write".equals(args[2]))) {
            throw new IllegalArgumentException("generatorConfig.xml uiRoot [--write]");
        }
        Map<Path, String> outputs = regenerate(Path.of(args[0]), Path.of(args[1]));
        for (Map.Entry<Path, String> output : outputs.entrySet()) {
            if (args.length == 3) {
                Files.writeString(output.getKey(), output.getValue(), StandardCharsets.UTF_8);
            }
            System.out.println((args.length == 3 ? "WROTE " : "CHECKED ") + output.getKey());
        }
    }

    /**
     * 在内存中校验全部目标并生成导入区，调用方决定是否写盘。
     *
     * @param configuration 真实消费配置文件
     * @param uiRoot 真实前端工程根目录
     * @return 四个路径与再生成文本
     * @throws Exception 配置、文件或模板不满足约束时抛出
     */
    public static Map<Path, String> regenerate(Path configuration, Path uiRoot) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Element xml = factory.newDocumentBuilder().parse(configuration.toFile()).getDocumentElement();
        Context context = new Context(ModelType.FLAT);
        NodeList tables = xml.getElementsByTagName("table");
        Map<String, Element> configured = new LinkedHashMap<>();
        for (int i = 0; i < tables.getLength(); i++) {
            Element table = (Element) tables.item(i);
            String name = table.getAttribute("domainObjectName");
            TableConfiguration target = new TableConfiguration(context, new ArrayList<>());
            target.setTableName(table.getAttribute("tableName"));
            target.setDomainObjectName(name);
            context.addTableConfiguration(target);
            configured.put(name, table);
        }
        Map<Path, String> outputs = new LinkedHashMap<>();
        for (String domain : DOMAINS) {
            Element table = configured.get(domain);
            if (table == null || !"organization".equals(
                    ((Element) table.getParentNode()).getAttribute("moduleKeyword"))) {
                throw new IllegalArgumentException("缺少组织模块配置：" + domain);
            }
            Path target = uiRoot.resolve("src\\modules\\organization")
                    .resolve(domain.toLowerCase(Locale.ROOT)).resolve("types\\T" + domain + ".ts");
            String original = Files.readString(target, StandardCharsets.UTF_8);
            if (!original.contains("@VCF.overridable")) {
                throw new IllegalArgumentException("产物不允许覆盖：" + target);
            }
            List<TypeScriptTypeDescriptor> descriptors = new ArrayList<>();
            Matcher declarations = DECLARATION.matcher(original);
            String entitySuperclass = null;
            while (declarations.find()) {
                TypeScriptTypeDescriptor descriptor =
                        new TypeScriptTypeDescriptor(declarations.group(1), "interface", "");
                descriptor.setExtendsName(declarations.group(2));
                descriptors.add(descriptor);
                if (domain.equals(descriptor.getName())) {
                    entitySuperclass = descriptor.getExtendsName();
                }
            }
            String superclass = property(table, "rootClass").replaceAll(".*\\.", "");
            String arguments = property(table, "rootClassTypeArguments");
            List<String> argumentNames = new ArrayList<>();
            for (String argument : arguments.split(",")) {
                argumentNames.add(argument.trim().replaceAll(".*\\.", ""));
            }
            String expected = superclass + "<" + String.join(",", argumentNames) + ">";
            if (entitySuperclass == null || !expected.equals(entitySuperclass.replaceAll("\\s+", ""))) {
                throw new IllegalArgumentException("配置与既有实体继承声明不一致：" + target);
            }
            IntrospectedTableMyBatis3Impl metadata = new IntrospectedTableMyBatis3Impl();
            metadata.setContext(context);
            String imports = new GeneratedVueFile(target.getFileName().toString(), ".", "", metadata,
                    "vue_module_type_imports.ts.ftl",
                    Map.of("typeImports", Vue3TypeImports.resolve(context, descriptors))).getFormattedContent();
            if (imports.isBlank()) {
                throw new IllegalArgumentException("没有可再生成的类型导入：" + target);
            }
            int headerEnd = original.indexOf("*/");
            if (headerEnd < 0) {
                throw new IllegalArgumentException("产物缺少文件头：" + target);
            }
            headerEnd += 2;
            String newline = original.contains("\r\n") ? "\r\n" : "\n";
            if (!original.startsWith(newline, headerEnd)) {
                throw new IllegalArgumentException("产物文件头格式不匹配：" + target);
            }
            headerEnd += newline.length();
            imports = imports.replace("\r\n", "\n").replace("\n", newline);
            String body = original.substring(headerEnd);
            if (body.startsWith(imports)) {
                body = body.substring(imports.length());
            }
            outputs.put(target, original.substring(0, headerEnd) + imports + body);
        }
        return outputs;
    }

    private static String property(Element table, String name) {
        NodeList properties = table.getElementsByTagName("property");
        for (int i = 0; i < properties.getLength(); i++) {
            Element property = (Element) properties.item(i);
            if (name.equals(property.getAttribute("name"))) {
                return property.getAttribute("value");
            }
        }
        throw new IllegalArgumentException("缺少配置：" + name);
    }
}

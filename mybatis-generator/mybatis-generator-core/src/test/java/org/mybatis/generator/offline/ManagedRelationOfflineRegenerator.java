package org.mybatis.generator.offline;

import com.vgosoft.core.db.query.DatabaseType;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.mybatis.generator.codegen.mybatis3.service.ManagedRelationGeneration;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.java.render.FieldRenderer;
import org.mybatis.generator.api.dom.java.render.MethodRenderer;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.api.dom.xml.render.ElementRenderer;
import org.mybatis.generator.codegen.AbstractGenerator;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.codegen.mybatis3.controller.elements.DeleteByTableGenerator;
import org.mybatis.generator.codegen.mybatis3.controller.elements.InsertByTableGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.InsertByTableMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.codegen.mybatis3.service.elements.DeleteByTableElement;
import org.mybatis.generator.codegen.mybatis3.service.elements.InsertByTableElement;
import org.mybatis.generator.codegen.mybatis3.service.elements.SelectByTableElement;
import org.mybatis.generator.codegen.mybatis3.sqlschema.SqlSchemaScriptGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.InsertByTableElementGenerator;
import org.mybatis.generator.config.*;
import org.mybatis.generator.internal.rules.ConditionalModelRules;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Types;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Offline, selected-block regeneration. Fixtures describe only metadata already present in
 * the checked-in outputs; neither introspection nor the normal generator entry point is used.
 */
public final class ManagedRelationOfflineRegenerator {
    private ManagedRelationOfflineRegenerator() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3 && args.length != 4) {
            throw new IllegalArgumentException("basicRoot organizationRoot stageDirectory [--write]");
        }
        regenerate(Path.of(args[0]), Path.of(args[1]), Path.of(args[2]),
                args.length == 4 && "--write".equals(args[3]));
    }

    public static void regenerate(Path basic, Path organization, Path stage, boolean write) throws Exception {
        Map<Path, String> outputs = new LinkedHashMap<>();
        regenerateEntity(basic.resolve("vgosoft-system"), basic.resolve("vgosoft-web2"), "system",
                "SysMenu", "sys_menu", "ForRole", outputs);
        regenerateEntity(organization, organization, "organization", "OrgRole", "org_role", "ForMenu", outputs);
        Element relationTable = tableElement(organization.resolve("src\\main\\resources\\generatorConfig.xml"), "org_role_menu");
        IntrospectedTableMyBatis3Impl relation = fixture("organization", "RoleMenu", "org_role_menu", null);
        relation.getTableConfiguration().setRelationKeyColumns(split(relationTable.getAttribute("relationKeyColumns")));
        relation.setRemarks("角色和菜单关联表");
        relation.getBaseColumns().add(column("role_id", "roleId", "角色ID", true));
        relation.getBaseColumns().add(column("menu_id", "menuId", "菜单ID", true));
        for (DatabaseType type : List.of(DatabaseType.MYSQL, DatabaseType.POSTGRE_SQL, DatabaseType.DM)) {
            Path destination = organization.resolve("src\\main\\resources\\sql\\" + type.getCode() + "\\schema_org_role_menu.sql");
            if (!Files.isRegularFile(destination)) {
                throw new IllegalArgumentException("Missing existing DDL: " + destination);
            }
            outputs.put(destination, new SqlSchemaScriptGenerator(relation, type).getSqlScript());
        }
        // Stage every file first; callers can review without --write. No business DB is accessed.
        Files.createDirectories(stage);
        int number = 0;
        for (Map.Entry<Path, String> output : outputs.entrySet()) {
            Path staged = stage.resolve(String.format("%02d-%s", ++number, output.getKey().getFileName()));
            Files.writeString(staged, output.getValue(), StandardCharsets.UTF_8);
            System.out.println(staged + " -> " + output.getKey());
        }
        if (write) {
            for (Map.Entry<Path, String> output : outputs.entrySet()) {
                Files.writeString(output.getKey(), output.getValue(), StandardCharsets.UTF_8);
            }
        }
    }

    private static void regenerateEntity(Path module, Path webModule, String namespace, String domain,
                                         String tableName, String suffix, Map<Path, String> outputs) throws Exception {
        Element table = tableElement(module.resolve("src\\main\\resources\\generatorConfig.xml"), tableName);
        Element configured = null;
        NodeList relations = table.getElementsByTagName("selectByTable");
        for (int i = 0; i < relations.getLength(); i++) {
            Element candidate = (Element) relations.item(i);
            if (suffix.equals(candidate.getAttribute("methodSuffix"))) {
                configured = candidate;
            }
        }
        if (configured == null || configured.getAttribute("relationIdKey").isBlank()) {
            throw new IllegalArgumentException("Missing explicit managed relation: " + tableName);
        }
        SelectByTableGeneratorConfiguration query = relationConfiguration(configured);
        IntrospectedTableMyBatis3Impl fixture = fixture(namespace, domain, tableName, query);
        fixture.setRemarks("SysMenu".equals(domain) ? "菜单管理" : "角色管理");
        Path javaRoot = module.resolve("src\\main\\java\\com\\vgosoft\\" + namespace + "\\codegen");
        Path servicePath = javaRoot.resolve("service\\impl\\Abstract" + domain + "Impl.java");
        TopLevelClass service = new TopLevelClass("com.vgosoft." + namespace + ".codegen.service.impl.Abstract" + domain + "Impl");
        for (AbstractServiceElementGenerator generator : List.of(new SelectByTableElement(), new DeleteByTableElement(), new InsertByTableElement())) {
            configure(generator, fixture);
            java.lang.reflect.Method initialize = AbstractServiceElementGenerator.class.getDeclaredMethod("initGenerator");
            initialize.setAccessible(true);
            initialize.invoke(generator);
            generator.addElements(service);
        }
        String serviceText = readGeneratedJava(servicePath);
        serviceText = regenerateEntityMutationAnnotations(serviceText, fixture, service);
        for (Method method : service.getMethods()) {
            if (!"vgosoftCacheKeyJson".equals(method.getName())) {
                serviceText = spliceMethod(serviceText, method.getName(), renderMethod(method, service, false), true);
            } else if (!hasMethod(serviceText, method.getName())) {
                int closing = serviceText.lastIndexOf('}');
                serviceText = serviceText.substring(0, closing) + newline(serviceText)
                        + withNewlines(renderMethod(method, service, false), serviceText)
                        + newline(serviceText) + serviceText.substring(closing);
            }
        }
        for (Field generatedField : service.getFields()) {
            if (Pattern.compile("\\b" + Pattern.quote(generatedField.getName()) + "\\s*[=;]").matcher(serviceText).find()) {
                continue;
            }
            String field = new FieldRenderer().render(generatedField, service).stream()
                    .map(line -> "    " + line).collect(Collectors.joining("\n"));
            int closing = serviceText.lastIndexOf('}');
            serviceText = serviceText.substring(0, closing) + newline(serviceText)
                    + withNewlines(field, serviceText) + newline(serviceText) + serviceText.substring(closing);
        }
        if (!serviceText.contains("@RelationRead")) {
            serviceText = serviceText.replaceAll("(?m)^import com\\.vgosoft\\.mybatis\\.relation\\.RelationRead;\\r?\\n", "");
        }
        outputs.put(servicePath, addImports(serviceText, service.getImportedTypes()));

        Path mapperPath = javaRoot.resolve("dao\\Gen" + domain + "Mapper.java");
        Interface mapper = new Interface("com.vgosoft." + namespace + ".codegen.dao.Gen" + domain + "Mapper");
        InsertByTableMethodGenerator mapperGenerator = new InsertByTableMethodGenerator(false, query);
        configure(mapperGenerator, fixture);
        mapperGenerator.addInterfaceElements(mapper);
        String mapperText = readGeneratedJava(mapperPath);
        String rowMethods = mapper.getMethods().stream().map(method -> renderMethod(method, mapper, true))
                .collect(Collectors.joining("\n\n"));
        if (hasMethod(mapperText, query.getUnionMethodName())) {
            mapperText = spliceMethod(mapperText, query.getUnionMethodName(), rowMethods, true);
        } else {
            for (Method method : mapper.getMethods()) {
                String oldName = "existsByTable" + suffix;
                String existingName = method.getName().equals(query.getExistsMethodName()) && hasMethod(mapperText, oldName)
                        ? oldName : method.getName();
                mapperText = spliceMethod(mapperText, existingName, renderMethod(method, mapper, true), true);
            }
        }
        outputs.put(mapperPath, addImports(mapperText, mapper.getImportedTypes()));

        Path xmlPath = module.resolve("src\\main\\resources\\mappers\\" + domain + "Mapper.xml");
        String xml = Files.readString(xmlPath);
        XmlElement statements = new XmlElement("mapper");
        InsertByTableElementGenerator xmlGenerator = new InsertByTableElementGenerator();
        configure(xmlGenerator, fixture);
        xmlGenerator.addElements(statements);
        String newStatements = statements.getElements().stream().flatMap(element -> element.accept(new ElementRenderer()))
                .map(line -> "  " + line).collect(Collectors.joining("\n"));
        Set<String> statementIds = Set.of(query.getUnionMethodName(), query.getRowInsertMethodName(),
                query.getExistsMethodName(), "existsByTable" + suffix);
        Pattern statement = Pattern.compile("(?ms)^ {2}<(insert|select)\\b[^>]*\\bid=\"(" +
                statementIds.stream().map(Pattern::quote).collect(Collectors.joining("|")) + ")\"[^>]*>.*?^ {2}</\\1>\\r?\\n?");
        Matcher matches = statement.matcher(xml);
        int count = 0;
        StringBuilder retained = new StringBuilder();
        while (matches.find()) {
            if (!matches.group().contains("@mbg.generated")) {
                throw new IllegalArgumentException("Refusing to replace an unprotected/manual statement in " + xmlPath);
            }
            matches.appendReplacement(retained, Matcher.quoteReplacement(count++ == 0
                    ? withNewlines(newStatements, xml) + newline(xml) : ""));
        }
        matches.appendTail(retained);
        if (count == 0) {
            throw new IllegalArgumentException("Missing generated relation statements in " + xmlPath);
        }
        outputs.put(xmlPath, retained.toString());

        Path controllerPath = webModule.resolve("src\\main\\java\\com\\vgosoft\\" + namespace
                + "\\codegen\\controller\\Abstract" + domain + "Controller.java");
        TopLevelClass controller = new TopLevelClass("com.vgosoft." + namespace + ".codegen.controller.Abstract" + domain + "Controller");
        for (AbstractControllerElementGenerator generator : List.of(new DeleteByTableGenerator(), new InsertByTableGenerator())) {
            configure(generator, fixture);
            generator.setCommentGenerator(fixture.getContext().getCommentGenerator());
            generator.setServiceBeanName(Character.toLowerCase(domain.charAt(0)) + domain.substring(1) + "Impl");
            generator.addElements(controller);
        }
        String controllerText = readGeneratedJava(controllerPath);
        for (Method method : controller.getMethods()) {
            controllerText = spliceMethod(controllerText, method.getName(), renderMethod(method, controller, false), true);
        }
        outputs.put(controllerPath, addImports(controllerText, controller.getImportedTypes()));
    }

    public static IntrospectedTableMyBatis3Impl fixture(String namespace, String domain, String tableName,
                                                       SelectByTableGeneratorConfiguration relation) {
        Context context = new Context(ModelType.CONDITIONAL);
        context.setModuleKeyword(namespace);
        context.setModuleName(namespace);
        context.setIntegrateSpringSecurity(true);
        JavaModelGeneratorConfiguration model = new JavaModelGeneratorConfiguration();
        model.setTargetPackage("com.vgosoft." + namespace + ".entity");
        context.setJavaModelGeneratorConfiguration(model);
        CommentGeneratorConfiguration comment = new CommentGeneratorConfiguration();
        comment.setConfigurationType("org.mybatis.generator.custom.VgoCommentGenerator");
        context.setCommentGeneratorConfiguration(comment);
        TableConfiguration configuration = new TableConfiguration(context, new ArrayList<>());
        configuration.setTableName(tableName);
        configuration.setDomainObjectName(domain);
        configuration.setJavaModelGeneratorConfiguration(model);
        configuration.setJavaServiceImplGeneratorConfiguration(new JavaServiceImplGeneratorConfiguration(context));
        configuration.getAdditionalDbTypes().addAll(List.of(DatabaseType.MYSQL, DatabaseType.POSTGRE_SQL, DatabaseType.DM));
        context.addTableConfiguration(configuration);
        IntrospectedTableMyBatis3Impl table = new IntrospectedTableMyBatis3Impl();
        table.setContext(context);
        table.setTableConfiguration(configuration);
        table.setBaseRecordType(model.getTargetPackage() + "." + domain);
        table.setExampleType(model.getTargetPackage() + ".example." + domain + "Example");
        table.getPrimaryKeyColumns().add(column("id_", "id", "数据标识", false));
        table.setRules(new ConditionalModelRules(table));
        if (relation != null) {
            configuration.getSelectByTableGeneratorConfiguration().add(relation);
        }
        return table;
    }

    public static SelectByTableGeneratorConfiguration relationConfiguration(Element element) {
        SelectByTableGeneratorConfiguration query = new SelectByTableGeneratorConfiguration();
        query.setTableName(element.getAttribute("table"));
        query.setPrimaryKeyColumn(element.getAttribute("thisColumn"));
        query.setOtherPrimaryKeyColumn(element.getAttribute("otherColumn"));
        query.setMethodSuffix(element.getAttribute("methodSuffix"));
        query.setRelationIdKey(element.getAttribute("relationIdKey"));
        query.setOrderByClause(element.getAttribute("orderByClause"));
        if (element.hasAttribute("returnType")) {
            query.setReturnTypeParam(element.getAttribute("returnType"));
        }
        query.setEnableSplit(!"false".equals(element.getAttribute("enableSplit")));
        query.setEnableUnion(!"false".equals(element.getAttribute("enableUnion")));
        query.setThisColumn(column(query.getPrimaryKeyColumn(), camelCase(query.getPrimaryKeyColumn()), "当前对象主键标识", false));
        query.setOtherColumn(column(query.getOtherPrimaryKeyColumn(), camelCase(query.getOtherPrimaryKeyColumn()), "关联数据表的主键标识", false));
        query.setParameterName(query.getOtherColumn().getJavaProperty());
        NodeList cacheNodes = element.getElementsByTagName("enableCache");
        for (int i = 0; i < cacheNodes.getLength(); i++) {
            Element cacheElement = (Element) cacheNodes.item(i);
            EnableCacheConfiguration cache = new EnableCacheConfiguration();
            cache.setCacheNames(split(cacheElement.getAttribute("cacheNames")));
            cache.setEnableCache(!"false".equals(cacheElement.getAttribute("enable")));
            query.getCacheConfigurationList().add(cache);
        }
        return query;
    }

    private static String camelCase(String column) {
        String[] parts = column.split("_");
        return parts[0] + Arrays.stream(parts).skip(1).map(p -> Character.toUpperCase(p.charAt(0)) + p.substring(1))
                .collect(Collectors.joining());
    }

    public static IntrospectedColumn column(String name, String property, String remark, boolean nullable) {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setActualColumnName(name);
        column.setJavaProperty(property);
        column.setFullyQualifiedJavaType(FullyQualifiedJavaType.getStringInstance());
        column.setJdbcType(Types.VARCHAR);
        column.setJdbcTypeName("VARCHAR");
        column.setActualTypeName("VARCHAR");
        column.setLength(36);
        column.setRemarks(remark);
        column.setNullable(nullable);
        return column;
    }

    public static void configure(AbstractGenerator generator, IntrospectedTableMyBatis3Impl table) {
        generator.setContext(table.getContext());
        generator.setIntrospectedTable(table);
        generator.setWarnings(new ArrayList<>());
    }

    private static Element tableElement(Path configuration, String tableName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        NodeList tables = factory.newDocumentBuilder().parse(configuration.toFile()).getElementsByTagName("table");
        for (int i = 0; i < tables.getLength(); i++) {
            Element table = (Element) tables.item(i);
            if (tableName.equals(table.getAttribute("tableName"))) {
                return table;
            }
        }
        throw new IllegalArgumentException("Missing table: " + tableName);
    }

    private static List<String> split(String value) {
        return value.isBlank() ? List.of() : Arrays.stream(value.split(",")).map(String::trim).toList();
    }

    private static String readGeneratedJava(Path path) throws Exception {
        if (!path.toString().contains("\\codegen\\")) {
            throw new IllegalArgumentException("Only selected codegen Java files may be regenerated: " + path);
        }
        String text = Files.readString(path);
        if (!text.contains("The content below is generated by vgosoft's code factory.")) {
            throw new IllegalArgumentException("Missing generated origin marker: " + path);
        }
        return text;
    }

    public static String renderMethod(Method method, CompilationUnit parent, boolean inInterface) {
        return new MethodRenderer().render(method, inInterface, parent).stream()
                .map(line -> "    " + line).collect(Collectors.joining("\n"));
    }

    private static boolean hasMethod(String text, String name) {
        return methodSignature(name).matcher(text).find();
    }

    private static Pattern methodSignature(String name) {
        return Pattern.compile("(?m)^ {4}(?:(?:public|protected|private|abstract|default|static|final)\\s+)*"
                + "[\\w.$]+(?:<[^\\n]+>)?(?:\\[])?\\s+" + Pattern.quote(name) + "\\(");
    }

    public static String spliceMethod(String text, String name, String replacement, boolean required) {
        Matcher signature = methodSignature(name).matcher(text);
        if (!signature.find()) {
            if (required) {
                throw new IllegalArgumentException("Missing generated method: " + name);
            }
            return text;
        }
        Matcher separator = Pattern.compile("\\r?\\n[ \\t]*\\r?\\n").matcher(text);
        int start = -1;
        while (separator.find() && separator.end() <= signature.start()) {
            start = separator.end();
        }
        if (start < 0) {
            throw new IllegalArgumentException("No generated method block boundary: " + name);
        }
        int lineEnd = text.indexOf('\n', signature.start());
        if (lineEnd == -1) {
            lineEnd = text.length();
        }
        int bodyStart = text.indexOf('{', signature.start());
        int end;
        if (text.substring(signature.start(), lineEnd).stripTrailing().endsWith(";")) {
            end = lineEnd > 0 && text.charAt(lineEnd - 1) == '\r' ? lineEnd - 1 : lineEnd;
        } else {
            int depth = 0;
            boolean quoted = false;
            boolean escaped = false;
            end = bodyStart;
            for (; end < text.length(); end++) {
                char c = text.charAt(end);
                if (quoted) {
                    if (escaped) {
                        escaped = false;
                    } else if (c == '\\') {
                        escaped = true;
                    } else if (c == '"') {
                        quoted = false;
                    }
                } else if (c == '"') {
                    quoted = true;
                } else if (c == '{') {
                    depth++;
                } else if (c == '}' && --depth == 0) {
                    end++;
                    break;
                }
            }
        }
        return text.substring(0, start) + withNewlines(replacement, text) + text.substring(end);
    }

    private static String addImports(String text, Set<FullyQualifiedJavaType> imports) {
        String additional = imports.stream().map(type -> "import " + type.getFullyQualifiedNameWithoutTypeParameters() + ";")
                .filter(statement -> !text.contains(statement)).sorted().collect(Collectors.joining("\n"));
        if (additional.isEmpty()) {
            return text;
        }
        int position = text.indexOf(';', text.indexOf("package ")) + 1;
        return text.substring(0, position) + newline(text) + newline(text)
                + withNewlines(additional, text) + text.substring(position);
    }

    private static String newline(String text) {
        return text.contains("\r\n") ? "\r\n" : "\n";
    }

    public static String regenerateEntityMutationAnnotations(String text, IntrospectedTableMyBatis3Impl table,
                                                             TopLevelClass parent) {
        List<MethodDeclaration> methods = StaticJavaParser.parse(text).findAll(MethodDeclaration.class);
        methods.sort(Comparator.comparingInt((MethodDeclaration method) -> method.getBegin().orElseThrow().line).reversed());
        for (MethodDeclaration existing : methods) {
            boolean otherRelationWriter = (existing.getNameAsString().startsWith("insertByTable")
                    || existing.getNameAsString().startsWith("deleteByTable"))
                    && table.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream()
                    .filter(SelectByTableGeneratorConfiguration::isManagedRelation)
                    .noneMatch(relation -> existing.getNameAsString().equals(relation.getUnionMethodName())
                            || existing.getNameAsString().equals(relation.getSplitMethodName()));
            if (otherRelationWriter && existing.getAnnotationByName("RelationMutation").isPresent()) {
                var annotation = existing.getAnnotationByName("RelationMutation").orElseThrow();
                if (annotation.toString().contains("\"" + table.getTableConfiguration()
                        .getSelectByTableGeneratorConfiguration().stream()
                        .filter(SelectByTableGeneratorConfiguration::isManagedRelation).findFirst().orElseThrow()
                        .getRelationIdKey() + "\"")) {
                    int start = lineStart(text, annotation.getBegin().orElseThrow().line);
                    int end = text.indexOf('\n', lineStart(text, annotation.getEnd().orElseThrow().line)) + 1;
                    text = text.substring(0, start) + text.substring(end);
                }
                continue;
            }
            if (existing.getAnnotationByName("CacheEvict").isEmpty()) {
                continue;
            }
            TopLevelClass snapshot = new TopLevelClass(parent.getType());
            Method method = new Method(existing.getNameAsString());
            existing.getAnnotations().forEach(annotation -> {
                var retained = annotation.clone();
                // Only this offline migration removes the retired role-menu regions.
                if (retained.getNameAsString().equals("CacheEvict")
                        && table.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream()
                        .anyMatch(relation -> relation.isManagedRelation()
                                && "org_role_menu".equals(relation.getRelationIdKey())
                                && "org_role_menu".equals(relation.getTableName()))) {
                    retained.asNormalAnnotationExpr().getPairs().stream()
                            .filter(pair -> pair.getNameAsString().equals("cacheNames"))
                            .forEach(pair -> pair.getValue().asArrayInitializerExpr().getValues()
                                    .removeIf(value -> value.isStringLiteralExpr()
                                            && Set.of("roleIdMenuIdsCache", "menuIdRoleIdsCache",
                                                    "roleIdMenuIdsCache#72h", "menuIdRoleIdsCache#72h")
                                            .contains(value.asStringLiteralExpr().asString())));
                }
                method.addAnnotation(retained.toString());
            });
            snapshot.addMethod(method);
            ManagedRelationGeneration.addEntityMutationAnnotations(snapshot, table);
            Optional<String> generated = method.getAnnotations().stream()
                    .filter(annotation -> annotation.startsWith("@RelationMutation")).findFirst();
            if (generated.isEmpty()) {
                continue;
            }
            String generatedMutation = generated.orElseThrow();
            var existingMutation = existing.getAnnotationByName("RelationMutations")
                    .or(() -> existing.getAnnotationByName("RelationMutation"));
            if (existingMutation.isPresent()) {
                var mutation = existingMutation.orElseThrow();
                int start = lineStart(text, mutation.getBegin().orElseThrow().line) + mutation.getBegin().orElseThrow().column - 1;
                int end = lineStart(text, mutation.getEnd().orElseThrow().line) + mutation.getEnd().orElseThrow().column;
                text = text.substring(0, start) + generatedMutation + text.substring(end);
            }
            var cache = existing.getAnnotationByName("CacheEvict").orElseThrow();
            int start = lineStart(text, cache.getBegin().orElseThrow().line) + cache.getBegin().orElseThrow().column - 1;
            int end = lineStart(text, cache.getEnd().orElseThrow().line) + cache.getEnd().orElseThrow().column;
            String replacement = method.getAnnotations().stream()
                    .filter(annotation -> annotation.startsWith("@CacheEvict")).findFirst().orElseThrow();
            if (existingMutation.isEmpty()) {
                replacement += newline(text) + "    " + generatedMutation;
            }
            text = text.substring(0, start) + replacement + text.substring(end);
            snapshot.getImportedTypes().forEach(parent::addImportedType);
        }
        return text;
    }

    private static int lineStart(String text, int lineNumber) {
        int position = 0;
        for (int line = 1; line < lineNumber; line++) {
            position = text.indexOf('\n', position) + 1;
        }
        return position;
    }

    private static String withNewlines(String generated, String existing) {
        return generated.replace("\r\n", "\n").replace("\n", newline(existing));
    }
}

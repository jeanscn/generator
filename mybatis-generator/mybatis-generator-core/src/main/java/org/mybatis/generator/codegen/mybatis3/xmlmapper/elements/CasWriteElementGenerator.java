package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.CasWriteGenerationValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 在现有 XML 装配层生成有限 CAS、固定版本新增与显式入口拒绝。
 * MySQL/H2 的这些 SQL 语义相同，按声明方言分别输出，不扩展其他方言。
 *
 * @author vgosoft
 */
public final class CasWriteElementGenerator extends AbstractXmlElementGenerator {
    @Override
    public void addElements(XmlElement parent) {
        if (!introspectedTable.getTableConfiguration().isCasWriteContractEnabled()) { return; }
        List<XmlElement> old = parent.getElements().stream().filter(e -> e instanceof XmlElement)
                .map(e -> (XmlElement) e).filter(e -> Set.of("insert", "update", "delete").contains(e.getName())).toList();
        parent.getElements().removeAll(old);
        var operations = CasWriteGenerationValidator.operations(introspectedTable);
        for (var db : introspectedTable.getTableConfiguration().getAdditionalDbTypes()) {
            Set<String> ids = new java.util.HashSet<>();
            for (XmlElement element : old) {
                String id = attribute(element, "id");
                if (!ids.add(id) || operations.containsKey(id)) { continue; }
                parent.addElement(statement(introspectedTable, id, db.getCode(), element.getName(), null));
            }
            operations.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey())
                    .forEach(e -> parent.addElement(statement(introspectedTable, e.getKey(), db.getCode(),
                            e.getValue().startsWith("INSERT") ? "insert" : "update", e.getValue())));
        }
    }

    public static String attribute(XmlElement element, String name) {
        return element.getAttributes().stream().filter(a -> a.getName().equals(name))
                .map(Attribute::getValue).findFirst().orElseThrow();
    }

    /**
     * 创建规范语句节点，供最终输出校验复用同一语义定义。
     *
     * @param table 已预检表
     * @param id 局部方法名
     * @param database 方言代码
     * @param command SQL 命令
     * @param operation 允许操作，null 表示拒绝
     * @return 带受管语言驱动的语句
     */
    public static XmlElement statement(AbstractIntrospectedTable table, String id, String database,
                                       String command, String operation) {
        XmlElement result = new XmlElement(command);
        result.addAttribute(new Attribute("id", id));
        result.addAttribute(new Attribute("databaseId", database));
        result.addAttribute(new Attribute("lang", CasWriteGenerationValidator.LANGUAGE_DRIVER));
        result.addElement(new TextElement("<!-- @mbg.generated 受管写入口 -->"));
        if (operation == null) {
            XmlElement bind = node(result, "bind", "name", "_writeRejected", "value", "'ENTRY_DISABLED'");
            return result;
        }
        String name = table.getFullyQualifiedTableNameAtRuntime();
        var columns = CasWriteGenerationValidator.writes(table, operation);
        if (operation.equals("LOGICAL_DELETE")) {
            text(result, "update " + name + " set " + columns.stream().map(c ->
                    c.getActualColumnName() + " = " + (c.getActualColumnName().equals("delete_flag") ? "1"
                            : role(c).equals("version") ? c.getActualColumnName() + " + 1" : parameter(c, "")))
                    .collect(Collectors.joining(",")));
            var key = table.getPrimaryKeyColumns().get(0);
            var version = table.getColumn("version_").orElseThrow();
            text(result, "where " + key.getActualColumnName() + " = " + parameter(key, "") + " and "
                    + version.getActualColumnName() + " = " + parameter(version, "") + " and delete_flag = 0");
        } else if (operation.equals("INSERT_BATCH")) {
            text(result, "insert into " + name + " (" + names(columns) + ") values");
            XmlElement loop = node(result, "foreach", "collection", "list", "item", "item", "index", "index", "separator", ",");
            text(loop, "(" + columns.stream().map(c -> role(c).equals("version") ? "1"
                    : parameter(c, audit(c) ? "" : "item.")).collect(Collectors.joining(",")) + ")");
        } else if (operation.equals("INSERT_SELECTIVE")) {
            text(result, "insert into " + name);
            var names = node(result, "trim", "prefix", "(", "suffix", ")", "suffixOverrides", ",");
            var values = node(result, "trim", "prefix", "values (", "suffix", ")", "suffixOverrides", ",");
            for (var c : columns) {
                fragment(names, c, c.getActualColumnName() + ",");
                fragment(values, c, (role(c).equals("version") ? "1" : parameter(c, "")) + ",");
            }
        } else if (operation.equals("INSERT_FULL")) {
            text(result, "insert into " + name + " (" + names(columns) + ") values ("
                    + columns.stream().map(c -> role(c).equals("version") ? "1" : parameter(c, ""))
                    .collect(Collectors.joining(",")) + ")");
        } else {
            text(result, "update " + name);
            if (operation.equals("UPDATE_SELECTIVE")) {
                var set = node(result, "set");
                for (var c : columns) { fragment(set, c, assignment(c) + ","); }
            } else {
                text(result, "set " + columns.stream().map(CasWriteElementGenerator::assignment)
                        .collect(Collectors.joining(",")));
            }
            var key = table.getPrimaryKeyColumns().get(0);
            var version = table.getColumn("version_").orElseThrow();
            text(result, "where " + key.getActualColumnName() + " = " + parameter(key, "")
                    + " and " + version.getActualColumnName() + " = #{expectedVersion,jdbcType="
                    + version.getJdbcTypeName() + ",javaType=" + version.getFullyQualifiedJavaType().getFullyQualifiedName() + "}");
        }
        return result;
    }

    private static String names(List<IntrospectedColumn> columns) {
        return columns.stream().map(IntrospectedColumn::getActualColumnName).collect(Collectors.joining(","));
    }
    private static String role(IntrospectedColumn c) { return CasWriteGenerationValidator.role(c); }
    private static boolean audit(IntrospectedColumn c) {
        return Set.of("created", "created-id", "modified", "modified-id").contains(role(c));
    }
    private static String parameter(IntrospectedColumn c, String prefix) {
        return "#{" + prefix + (audit(c) ? "_writeAudit." : "") + c.getJavaProperty()
                + ",jdbcType=" + c.getJdbcTypeName() + ",javaType=" + c.getFullyQualifiedJavaType().getFullyQualifiedName() + "}";
    }
    private static String assignment(IntrospectedColumn c) {
        return c.getActualColumnName() + " = " + (role(c).equals("version")
                ? c.getActualColumnName() + " + 1" : parameter(c, ""));
    }
    private static void fragment(XmlElement parent, IntrospectedColumn c, String value) {
        text(role(c).equals("business") ? node(parent, "if", "test", c.getJavaProperty() + " != null") : parent, value);
    }
    private static XmlElement node(XmlElement parent, String name, String... attributes) {
        XmlElement node = new XmlElement(name);
        for (int i = 0; i < attributes.length; i += 2) { node.addAttribute(new Attribute(attributes[i], attributes[i + 1])); }
        parent.addElement(node);
        return node;
    }
    private static void text(XmlElement parent, String value) { parent.addElement(new TextElement(value)); }
}

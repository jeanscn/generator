package org.mybatis.generator.codegen.mybatis3.javamapper;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * 生成 Mapper 锁定查询的运行时门禁类及其稳定命名。
 *
 * @author vgosoft
 */
public final class SelectForUpdateGuardGenerator {
    private SelectForUpdateGuardGenerator() {
    }

    /**
     * 返回生成 Mapper 的全限定名。
     *
     * @param table 表信息
     * @return 生成 Mapper 的全限定名
     */
    public static String mapperType(AbstractIntrospectedTable table) {
        return table.getTableConfiguration().getJavaClientGeneratorConfiguration().getTargetPackageGen()
                + ".Gen" + table.getTableConfiguration().getDomainObjectName() + "Mapper";
    }

    /**
     * 返回生成门禁类全限定名，避免 OGNL 对接口静态方法的版本差异。
     *
     * @param table 表信息
     * @return 生成门禁类的全限定名
     */
    public static String guardType(AbstractIntrospectedTable table) {
        return mapperType(table) + "LockQueryGuard";
    }

    /**
     * 生成 XML/Service 共用的门禁类，不依赖 OGNL 的接口静态方法支持。
     *
     * @param table 表信息
     * @return 生成的门禁类
     */
    public static TopLevelClass createGuard(AbstractIntrospectedTable table) {
        TopLevelClass unit = new TopLevelClass(guardType(table));
        unit.setVisibility(JavaVisibility.PUBLIC);
        unit.setFinal(true);
        table.getContext().getCommentGenerator().addJavaFileComment(unit, false);
        unit.addJavaDocLine("/**");
        unit.addJavaDocLine(" * 锁定查询的事务、分页和条件门禁，仅用于生成的独立锁定查询。");
        unit.addJavaDocLine(" * @author vgosoft");
        unit.addJavaDocLine(" */");
        Method guard = new Method("requireSelectForUpdateTransaction");
        guard.setVisibility(JavaVisibility.PUBLIC);
        guard.setStatic(true);
        guard.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        guard.addJavaDocLine("/**");
        guard.addJavaDocLine(" * 验证非只读既有事务及无分页上下文，禁止绕过 Service 的直接 Mapper 调用。");
        guard.addJavaDocLine(" * @return 门禁验证通过");
        guard.addJavaDocLine(" * @throws IllegalStateException 无事务、只读事务或存在分页上下文");
        guard.addJavaDocLine(" */");
        guard.addBodyLine("if (!org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()");
        guard.addBodyLine("        || org.springframework.transaction.support.TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {");
        guard.addBodyLine("throw new IllegalStateException(\"锁定查询必须在非只读既有事务中执行\");");
        guard.addBodyLine("}");
        guard.addBodyLine("if (com.github.pagehelper.PageHelper.getLocalPage() != null) {");
        guard.addBodyLine("throw new IllegalStateException(\"锁定查询禁止 PageHelper 分页或排序上下文\");");
        guard.addBodyLine("}");
        guard.addBodyLine("return true;");
        unit.addMethod(guard);
        if (table.getRules().generateSelectByExampleForUpdate()) {
            addExampleGuard(unit, table);
        }
        return unit;
    }

    private static void addExampleGuard(TopLevelClass unit, AbstractIntrospectedTable table) {
        var type = new FullyQualifiedJavaType(table.getExampleType());
        unit.addImportedType(type);
        Method guard = new Method("requireSelectForUpdateExample");
        guard.setVisibility(JavaVisibility.PUBLIC);
        guard.setStatic(true);
        guard.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        guard.addParameter(new Parameter(type, "example"));
        guard.addJavaDocLine("/**");
        guard.addJavaDocLine(" * 校验非空条件及内省列操作符白名单，拒绝原始 SQL、去重和任意排序。");
        guard.addJavaDocLine(" * @param example 非空标准查询条件");
        guard.addJavaDocLine(" * @return 门禁验证通过");
        guard.addJavaDocLine(" * @throws IllegalStateException 无事务、只读事务或存在分页上下文");
        guard.addJavaDocLine(" * @throws IllegalArgumentException 查询条件不符合锁定查询约束");
        guard.addJavaDocLine(" */");
        guard.addBodyLine("requireSelectForUpdateTransaction();");
        guard.addBodyLine("if (example == null || example.isDistinct() || example.getOrderByClause() != null");
        guard.addBodyLine("        || example.getOredCriteria() == null || example.getOredCriteria().isEmpty()) {");
        guard.addBodyLine("throw new IllegalArgumentException(\"锁定查询要求非空条件，禁止 DISTINCT 和动态排序\");");
        guard.addBodyLine("}");
        List<String> conditions = new ArrayList<>();
        for (IntrospectedColumn column : table.getNonBlobColumns()) {
            String name = MyBatis3FormattingUtilities.getAliasedActualColumnName(column);
            for (String operator : List.of("is null", "is not null", "=", "<>", ">", ">=", "<", "<=",
                    "like", "not like", "in", "not in", "between", "not between")) {
                conditions.add("\"" + (name + " " + operator).replace("\\", "\\\\").replace("\"", "\\\"") + "\"");
            }
        }
        guard.addBodyLine("java.util.Set<String> allowed = new java.util.HashSet<>(java.util.Arrays.asList("
                + String.join(", ", conditions) + "));");
        guard.addBodyLine("boolean valid = false;");
        guard.addBodyLine("for (" + type.getShortName() + ".Criteria criteria : example.getOredCriteria()) {");
        guard.addBodyLine("if (criteria == null) { throw new IllegalArgumentException(\"锁定查询条件组不能为空\"); }");
        guard.addBodyLine("if (!criteria.isValid()) { continue; }");
        guard.addBodyLine("for (" + type.getShortName() + ".Criterion criterion : criteria.getAllCriteria()) {");
        guard.addBodyLine("if (criterion == null || !allowed.contains(criterion.getCondition())) {");
        guard.addBodyLine("throw new IllegalArgumentException(\"锁定查询只允许内省列的标准条件，禁止原始 SQL\");");
        guard.addBodyLine("}");
        guard.addBodyLine("if (criterion.isListValue() && (!(criterion.getValue() instanceof java.util.List)");
        guard.addBodyLine("        || ((java.util.List<?>) criterion.getValue()).isEmpty())) {");
        guard.addBodyLine("throw new IllegalArgumentException(\"锁定查询的集合条件不能为空\");");
        guard.addBodyLine("}");
        guard.addBodyLine("valid = true;");
        guard.addBodyLine("}");
        guard.addBodyLine("}");
        guard.addBodyLine("if (!valid) { throw new IllegalArgumentException(\"锁定查询禁止无条件整表查询\"); }");
        guard.addBodyLine("return true;");
        unit.addMethod(guard);
    }
}

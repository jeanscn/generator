package org.mybatis.generator.codegen.mybatis3.service.elements;

import com.vgosoft.core.constant.enums.core.EntityEventEnum;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.config.SelectByColumnGeneratorConfiguration;

import java.util.stream.Collectors;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class DeleteByColumnElement extends AbstractServiceElementGenerator {

    private final SelectByColumnGeneratorConfiguration configuration;

    public DeleteByColumnElement(SelectByColumnGeneratorConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        if (configuration.isLogicalDelete()) {
            Method method = serviceMethods.getDeleteByColumnMethod(parentElement, configuration, false);
            method.addAnnotation("@Override");
            method.addAnnotation("@org.springframework.transaction.annotation.Transactional("
                    + "propagation = org.springframework.transaction.annotation.Propagation."
                    + (tc.isCasWriteContractEnabled() ? "MANDATORY" : "REQUIRED")
                    + ", rollbackFor = Exception.class)");
            method.getBodyLines().addAll(logicalBody(introspectedTable, configuration));
            addMutationCacheEviction(method, parentElement);
            parentElement.addMethod(method);
            return;
        }

        boolean containPreDeleteEvent = this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_DELETE.name());
        boolean containDeletedEvent = this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.DELETED.name());

        String params = configuration.getColumns().stream()
                .map(column -> column.getJavaProperty() + (configuration.getParameterList() ? "s" : ""))
                .collect(Collectors.joining(","));
        Method methodByColumn = serviceMethods.getDeleteByColumnMethod(parentElement, configuration, false);
        methodByColumn.addAnnotation("@Override");
        if (containPreDeleteEvent || containDeletedEvent) {
            methodByColumn.addBodyLine("int ret = 0;");
            methodByColumn.addBodyLine(" List<{0}> items = mapper.{1}({2});",entityType.getShortName(),configuration.getMethodName(),params);
            methodByColumn.addBodyLine("if (!items.isEmpty()) {");
            if (containPreDeleteEvent) {
                methodByColumn.addBodyLine("if (publisher != null) {");
                methodByColumn.addBodyLine("publisher.publishEvent(items,EntityEventEnum.{0});", EntityEventEnum.PRE_DELETE.name());
                methodByColumn.addBodyLine("}");
            }
            methodByColumn.addBodyLine("ret = mapper.{0}({1});",configuration.getDeleteMethodName(),params);
            if (containDeletedEvent) {
                methodByColumn.addBodyLine("if (publisher != null) {");
                methodByColumn.addBodyLine("publisher.publishEvent(items,EntityEventEnum.{0});",EntityEventEnum.DELETED.name());
                methodByColumn.addBodyLine("}");
            }
            methodByColumn.addBodyLine("}");
            methodByColumn.addBodyLine("return ret;");
            parentElement.addImportedType(new FullyQualifiedJavaType(EntityEventEnum.class.getCanonicalName()));
        }else{
            String sb = "return mapper." + configuration.getDeleteMethodName() +
                    "(" + params +  ");";
            methodByColumn.addBodyLine(sb);
        }

        addMutationCacheEviction(methodByColumn, parentElement);

        parentElement.addMethod(methodByColumn);
    }

    /**
     * 提供逻辑删除的规范方法体，最终插件校验复用同一契约。
     *
     * @param table 已预检表
     * @param configuration 按列删除配置
     * @return 不捕获冲突的方法体
     */
    public static java.util.List<String> logicalBody(org.mybatis.generator.api.AbstractIntrospectedTable table,
                                                     SelectByColumnGeneratorConfiguration configuration) {
        Method method = new Method(configuration.getDeleteMethodName());
        configuration.getColumns().forEach(column -> {
            String argument = column.getJavaProperty() + (configuration.getParameterList() ? "s" : "");
            method.addBodyLine("org.springframework.util.Assert.notNull(" + argument + ", \"删除条件不能为空：" + argument + "\");");
            if (configuration.getParameterList()) {
                method.addBodyLine("org.springframework.util.Assert.isTrue(!" + argument
                        + ".isEmpty() && " + argument + ".stream().noneMatch(java.util.Objects::isNull), \"删除条件列表不能为空或包含空值\");");
            }
        });
        table.getColumn("version_").ifPresent(column -> {
            String argument = column.getJavaProperty();
            String max = "BIGINT".equals(column.getJdbcTypeName()) ? "Long.MAX_VALUE" : "Integer.MAX_VALUE";
            method.addBodyLine("org.springframework.util.Assert.isTrue(" + argument + " > 0 && "
                    + argument + " < " + max + ", \"旧版本必须为可递增的正数\");");
        });
        method.addBodyLine("int affected = mapper." + configuration.getDeleteMethodName() + "("
                + configuration.getColumns().stream().map(c -> c.getJavaProperty()
                + (configuration.getParameterList() ? "s" : "")).collect(Collectors.joining(", ")) + ");");
        method.addBodyLine("if (affected " + (table.getColumn("version_").isPresent() ? "!= 1" : "< 1") + ") {");
        method.addBodyLine("throw new IllegalStateException(\"LOGICAL_DELETE_CONFLICT\");");
        method.addBodyLine("}");
        method.addBodyLine("return affected;");
        return method.getBodyLines();
    }
}

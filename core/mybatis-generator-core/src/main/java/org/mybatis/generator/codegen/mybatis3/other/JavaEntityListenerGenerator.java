package org.mybatis.generator.codegen.mybatis3.other;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * 生成实体监听器类
 */
public class JavaEntityListenerGenerator extends AbstractJavaGenerator {

    private static final String DEFAULT_ENTITY_LISTENER_CLASS = "com.vgosoft.core.event.entity.AbstractEntityEventListener";

    public JavaEntityListenerGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        CommentGenerator commentGenerator = context.getCommentGenerator();

        List<CompilationUnit> answer = new ArrayList<>();
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        String targetPackage = context.getJavaModelGeneratorConfiguration().getBaseTargetPackage() + ".listener";
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        Set<String> entityEvent = tc.getJavaServiceImplGeneratorConfiguration().getEntityEvent();
        if (entityEvent == null || entityEvent.isEmpty()) {
           return answer;
        }
        String clazzName = entityType.getShortName() + "EventListener";
        boolean fileNotExist = JavaBeansUtil.javaFileNotExist(project, targetPackage, clazzName);
        if (!fileNotExist) {
            return answer;
        }
        progressCallback.startTask(VStringUtil.format("生成实体监听器类: {0}", introspectedTable.getFullyQualifiedTable().toString()));
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(targetPackage + "." + clazzName);
        TopLevelClass topClazz = new TopLevelClass(type);
        topClazz.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(topClazz);
        FullyQualifiedJavaType supClazzType = new FullyQualifiedJavaType(DEFAULT_ENTITY_LISTENER_CLASS);
        supClazzType.addTypeArgument(entityType);
        topClazz.setSuperClass(supClazzType);
        topClazz.addJavaDocLine(VStringUtil.format( "/**\n" +
                " * {0}实体生命周期阶段事件监听\n" +
                " * 事件类型包括：CREATED,PRE_UPDATE,UPDATED,PRE_DELETE,DELETED。\n" +
                " * 见枚举类：'{'@link com.vgosoft.core.constant.enums.core.EntityEventEnum'}'\n" +
                " * 事件监听器的实现类必须继承自'{'@link AbstractEntityEventListener'}'\n" +
                " * 并且使用'{'@link Component'}'注解进行标注。\n" +
                " */",entityType.getShortName()));
        topClazz.addAnnotation("@Component");

        // 添加无参构造方法
        Method method = new Method(clazzName);
        method.setConstructor(true);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addBodyLine("super({0}.class.getSimpleName());",entityType.getShortName());
        topClazz.addMethod(method);
        for (String eventName : entityEvent) {
            addOverrideMethod(topClazz,entityType,eventName);
        }
        topClazz.addImportedType(entityType);
        topClazz.addImportedType(supClazzType);
        topClazz.addImportedType("org.springframework.stereotype.Component");
        topClazz.addImportedType("com.vgosoft.core.event.entity.AbstractEntityEventListener");
        topClazz.addImportedType("com.vgosoft.core.constant.enums.core.EntityEventEnum");
        answer.add(topClazz);
        return answer;
    }

    private void addOverrideMethod(TopLevelClass topLevelClass, FullyQualifiedJavaType entityType,String entityEvent) {
        Method method;
        switch (entityEvent) {
            case "CREATED":
                method = new Method("entityCreatedEvent");
                break;
            case "PRE_UPDATE":
                method = new Method("entityPreUpdateEvent");
                break;
            case "UPDATED":
                method = new Method("entityUpdatedEvent");
                break;
            case "PRE_DELETE":
                method = new Method("entityPreDeleteEvent");
                break;
            case "DELETED":
                method = new Method("entityDeletedEvent");
                break;
            default:
                throw new RuntimeException(getString("RuntimeError.12"));
        }
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(new FullyQualifiedJavaType("java.util.List<" + entityType.getShortName() + ">"), "entityList"));
        method.addAnnotation("@Override");
        method.addBodyLine("super.{0}(entityList);",method.getName());
        topLevelClass.addMethod(method);
        topLevelClass.addImportedType("java.util.List");
    }
}

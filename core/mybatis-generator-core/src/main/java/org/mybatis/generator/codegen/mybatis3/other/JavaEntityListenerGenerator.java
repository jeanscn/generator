package org.mybatis.generator.codegen.mybatis3.other;

import com.vgosoft.core.constant.enums.core.EntityEventEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
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

    private static final String DEFAULT_ENTITY_LISTENER_CLASS_1 = "com.vgosoft.core.event.entity.AbstractEntityEventListener";

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

        // 设置父类
        String supClazzName = "com.vgosoft.core.event.entity.AbstractEntityEventListener";
        if (GenerateUtils.isWorkflowInstance(introspectedTable)) {
            supClazzName = "com.vgosoft.workflow.event.AbstractWorkflowEventListener";
        }
        FullyQualifiedJavaType supClazzType = new FullyQualifiedJavaType(supClazzName);
        supClazzType.addTypeArgument(entityType);
        topClazz.setSuperClass(supClazzType);

        topClazz.addJavaDocLine(VStringUtil.format("/**\n" +
                " * {0}实体生命周期阶段事件监听\n" +
                " * 事件类型包括：CREATED,PRE_INSERT,INSERTED,PRE_UPDATE,UPDATED,RECYCLED、RESTORED,PRE_DELETE,DELETED。\n" +
                " * 见枚举类：'{'@link com.vgosoft.core.constant.enums.core.EntityEventEnum'}'\n" +
                " * 事件监听器的实现类必须继承自'{'@link " + supClazzType.getShortName() + "'}'\n" +
                " * 并且使用'{'@link Component'}'注解进行标注。\n" +
                " */", entityType.getShortName()));
        topClazz.addAnnotation("@Component");

        // 添加无参构造方法
        Method method = new Method(clazzName);
        method.setConstructor(true);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addBodyLine("super({0}.class.getSimpleName());", entityType.getShortName());
        topClazz.addMethod(method);
        for (String eventName : entityEvent) {
            addOverrideMethod(topClazz, entityType, eventName);
        }
        if (introspectedTable.getRules().isGenerateRecycleBin() && !entityEvent.contains(EntityEventEnum.RECYCLED.name())) {
            addRecyclePersonListMethod(topClazz, entityType);
        }
        topClazz.addImportedType(entityType);
        topClazz.addImportedType(supClazzType);
        topClazz.addImportedType("org.springframework.stereotype.Component");
        topClazz.addImportedType(supClazzName);
        answer.add(topClazz);
        return answer;
    }

    private void addOverrideMethod(TopLevelClass topLevelClass, FullyQualifiedJavaType entityType, String entityEvent) {
        switch (entityEvent) {
            case "CREATED":
                addDefaultMethod(topLevelClass, entityType, "entityCreatedEvent");
                break;
            case "PRE_INSERT":
                addDefaultMethod(topLevelClass, entityType, "entityPreInsertEvent");
                break;
            case "INSERTED":
                addDefaultMethod(topLevelClass, entityType, "entityInsertedEvent");
                break;
            case "PRE_UPDATE":
                addDefaultMethod(topLevelClass, entityType, "entityPreUpdateEvent");
                break;
            case "UPDATED":
                addDefaultMethod(topLevelClass, entityType, "entityUpdatedEvent");
                break;
            case "RECYCLED":
                if (GenerateUtils.isWorkflowInstance(introspectedTable)) {
                    addRecyclePersonListMethod(topLevelClass, entityType);
                } else {
                    addDefaultMethod(topLevelClass, entityType, "entityRecycledEvent");
                }
                break;
            case "RESTORED":
                addDefaultMethod(topLevelClass, entityType, "entityRestoredEvent");
                break;
            case "PRE_DELETE":
                addDefaultMethod(topLevelClass, entityType, "entityPreDeleteEvent");
                break;
            case "DELETED":
                addDefaultMethod(topLevelClass, entityType, "entityDeletedEvent");
                break;
            default:
                throw new RuntimeException(getString("创建监听类出现错误,位置的事件类型：{0}-{1}",introspectedTable.getTableConfiguration().getTableName(), entityEvent));
        }
        topLevelClass.addImportedType("java.util.List");
    }

    private void addDefaultMethod(TopLevelClass topLevelClass, FullyQualifiedJavaType entityType, String entityEvent) {
        Method method = new Method(entityEvent);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(new FullyQualifiedJavaType("java.util.List<" + entityType.getShortName() + ">"), "entityList"));
        method.addAnnotation("@Override");
        method.addBodyLine("super.{0}(entityList);", method.getName());
        topLevelClass.addMethod(method);
    }

    private void addRecyclePersonListMethod(TopLevelClass topLevelClass, FullyQualifiedJavaType entityType) {
        Method method = new Method("entityRecycledEvent");
        method.addAnnotation("@Override");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(new FullyQualifiedJavaType("java.util.List<" + entityType.getShortName() + ">"), "entityList"));
        method.addBodyLine("try {");
        if (context.getJdkVersion() > 8) {
            method.addBodyLine("List<String> recordIds = entityList.stream().map({0}::getId).toList();", entityType.getShortName());
        } else {
            method.addBodyLine("List<String> recordIds = entityList.stream().map({0}::getId).collect(Collectors.toList());", entityType.getShortName());
        }
        method.addBodyLine("//回收个人事宜");
        method.addBodyLine("super.recyclePersonList(recordIds);");
        method.addBodyLine("} catch (Exception e) {");
        method.addBodyLine("throw new RuntimeException(e);");
        method.addBodyLine("}");
        topLevelClass.addMethod(method);
    }
}

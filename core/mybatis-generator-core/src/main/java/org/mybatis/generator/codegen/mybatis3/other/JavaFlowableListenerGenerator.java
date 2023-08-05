package org.mybatis.generator.codegen.mybatis3.other;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * 生成实体监听器类
 */
public class JavaFlowableListenerGenerator extends AbstractJavaGenerator {

    private static final String DEFAULT_FLOWABLE_LISTENER_CLASS = "com.vgosoft.workflow.event.AbstractAsyncFlowableEventListener";

    public JavaFlowableListenerGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        CommentGenerator commentGenerator = context.getCommentGenerator();

        List<CompilationUnit> answer = new ArrayList<>();
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        String targetPackage = context.getJavaModelGeneratorConfiguration().getBaseTargetPackage() + ".listener.flowable";
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());

        FullyQualifiedJavaType rootClass = new FullyQualifiedJavaType(JavaBeansUtil.getRootClass(introspectedTable));
        EntityAbstractParentEnum entityAbstractParentEnum = EntityAbstractParentEnum.ofCode(rootClass.getShortName());
        if (entityAbstractParentEnum == null || entityAbstractParentEnum.scope() != 1) {
            return answer;
        }

        String clazzName = entityType.getShortName() + "FlowableEventListener";
        boolean fileNotExist = JavaBeansUtil.javaFileNotExist(project, targetPackage, clazzName);
        if (!fileNotExist) {
            return answer;
        }
        progressCallback.startTask(VStringUtil.format("生成工作流监听器类: {0}", introspectedTable.getFullyQualifiedTable().toString()));
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(targetPackage + "." + clazzName);
        TopLevelClass topClazz = new TopLevelClass(type);
        topClazz.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(topClazz);
        FullyQualifiedJavaType supClazzType = new FullyQualifiedJavaType(DEFAULT_FLOWABLE_LISTENER_CLASS);
        topClazz.setSuperClass(supClazzType);
        topClazz.addJavaDocLine(VStringUtil.format( "/**\n" +
                " * {0}对应的工作流生命周期阶段事件监听\n" +
                " * 目前有效事件类型包括：PROCESS_STARTED、PROCESS_COMPLETED。\n" +
                " * 见枚举类：'{'@link org.flowable.common.engine.api.delegate.event.FlowableEngineEventType'}'\n" +
                " * 事件监听器的实现类必须继承自'{'@link AbstractAsyncFlowableEventListener'}'\n" +
                " * 并且使用'{'@link Component'}'注解进行标注。\n" +
                " */",entityType.getShortName()));
        topClazz.addAnnotation("@Component");
        topClazz.addImportedType("org.springframework.stereotype.Component");

        // 添加无参构造方法
        Method method = new Method(clazzName);
        method.setConstructor(true);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addBodyLine("super({0}.class.getSimpleName());",entityType.getShortName());
        topClazz.addMethod(method);
        addOverrideMethod(topClazz,"PROCESS_STARTED");
        addOverrideMethod(topClazz,"PROCESS_COMPLETED");
        topClazz.addImportedType(entityType);
        topClazz.addImportedType(supClazzType);
        answer.add(topClazz);
        return answer;
    }

    private void addOverrideMethod(TopLevelClass topLevelClass, String entityEvent) {
        Method method;
        switch (entityEvent) {
            case "PROCESS_STARTED":
                method = new Method("processStartEvent");
                break;
            case "PROCESS_COMPLETED":
                method = new Method("processCompleteEvent");
                break;
            default:
                throw new RuntimeException(getString("RuntimeError.12"));
        }
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(new FullyQualifiedJavaType("Object"), "entity"));
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "processDefinitionKey"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType("Map<String, Object>"), "variables"));
        method.addAnnotation("@Override");
        method.addBodyLine("super.{0}(entity, processDefinitionKey, variables);",method.getName());
        topLevelClass.addMethod(method);
        topLevelClass.addImportedType("java.util.Map");
    }
}

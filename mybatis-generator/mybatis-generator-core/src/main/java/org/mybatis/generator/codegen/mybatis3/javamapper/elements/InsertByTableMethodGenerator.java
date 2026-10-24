package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;

/**
 * @author cen_c
 */
public class InsertByTableMethodGenerator extends AbstractJavaMapperMethodGenerator {

    private final boolean isSimple;

    private final SelectByTableGeneratorConfiguration configuration;

    public InsertByTableMethodGenerator(boolean isSimple, SelectByTableGeneratorConfiguration configuration) {
        super();
        this.isSimple = isSimple;
        this.configuration = configuration;
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        if (isSimple) {
            return;
        }
        ServiceMethods serviceMethods = new ServiceMethods(context, introspectedTable);
        if (configuration.isManagedRelation()) {
            interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
            interfaze.addMethod(managedMethod(configuration.getRowInsertMethodName(), true));
            interfaze.addMethod(managedMethod(configuration.getExistsMethodName(), false));
            return;
        }
        Method method = serviceMethods.getSplitUnionByTableMethod(interfaze, configuration, true, true);
        addMapperAnnotations(method);
        if (context.getPlugins().clientInsertOrDeleteByTableMethodGenerated(method, interfaze, introspectedTable)) {
            addExtraImports(interfaze);
            interfaze.addMethod(method);
        }

    }

    private Method managedMethod(String name, boolean insert) {
        Method method = new Method(name);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);
        method.setReturnType(insert ? FullyQualifiedJavaType.getIntInstance() : FullyQualifiedJavaType.getStringInstance());
        Parameter p1 = new Parameter(configuration.getThisColumn());
        p1.addAnnotation("@Param(\"" + configuration.getThisColumn().getJavaProperty() + "\")");
        method.addParameter(p1);
        Parameter p2 = new Parameter(configuration.getOtherColumn());
        p2.addAnnotation("@Param(\"" + configuration.getOtherColumn().getJavaProperty() + "\")");
        method.addParameter(p2);
        if (insert) {
            Parameter p3 = new Parameter(FullyQualifiedJavaType.getStringInstance(), "relationId", "@Param(\"relationId\")");
            p3.setRemark("关系行主键，写入关系表的 id_ 列；由关系维护逻辑生成。");
            method.addParameter(p3);
        }
        method.setReturnRemark(insert ? "新增关系行数" : "锁定读取的关系行标识，关系不存在时为null");
        context.getCommentGenerator().addMethodJavaDocLine(method, insert
                ? "写入单条受管关系，代理标识由调用方提供"
                : "按两侧关系键当前读取，确认保存点回滚后的业务关系");
        return method;
    }
    public void addMapperAnnotations(Method method) {
        // extension point for subclasses
    }

    public void addExtraImports(Interface interfaze) {
        // extension point for subclasses
    }
}

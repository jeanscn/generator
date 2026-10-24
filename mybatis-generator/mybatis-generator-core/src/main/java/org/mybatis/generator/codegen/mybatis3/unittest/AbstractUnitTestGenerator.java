package org.mybatis.generator.codegen.mybatis3.unittest;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

public abstract class AbstractUnitTestGenerator extends AbstractJavaGenerator {

    public AbstractUnitTestGenerator(String project) {
        super(project);
    }

    @Override
    public abstract List<CompilationUnit> getCompilationUnits();

    protected void addExtendWithAnnotation(TopLevelClass topLevelClass){
        topLevelClass.addAnnotation("@ExtendWith(MockitoExtension.class)");
        topLevelClass.addImportedType("org.junit.jupiter.api.extension.ExtendWith");
        topLevelClass.addImportedType("org.mockito.junit.jupiter.MockitoExtension");
    }

    /**
     * 内部方法
     * 添加方法注释
     * */
    protected void addMethodComment(Method method,boolean isMock,String mockScene){
        CommentGenerator commentGenerator = context.getCommentGenerator();
        String comm1 = VStringUtil.format("被测试方法：{0}", JavaBeansUtil.getFirstCharacterLowercase(
                StringUtility.substringAfter(method.getName(),"test")));
        if (isMock) {
            String comm2 = VStringUtil.format("mock场景：{0}", mockScene);
            commentGenerator.addMethodJavaDocLine(method, false, comm1,comm2);
        }else{
            commentGenerator.addMethodJavaDocLine(method, false, comm1);
        }
    }

    /**
     * 内部方法
     * 创建测试方法
     * */
    protected Method createMethod(String testedMethodName,TopLevelClass testClazz,String methodDescript){
        Method method = new Method("test"+JavaBeansUtil.getFirstCharacterUppercase(testedMethodName));
        method.addAnnotation("@Test");
        testClazz.addImportedType("org.junit.jupiter.api.Test");
        testClazz.addMethod(method);
        if (StringUtility.stringHasValue(methodDescript)) {
            method.addAnnotation("@DisplayName(\""+methodDescript+"\")");
        }
        return method;
    }

    protected Field addField(String fieldName,String typeName,String annotation,TopLevelClass topLevelClass){
        Field field = new Field(fieldName, new FullyQualifiedJavaType(typeName));
        field.setVisibility(JavaVisibility.PRIVATE);
        if (StringUtility.stringHasValue(annotation)) {
            field.addAnnotation(annotation);
        }
        topLevelClass.addImportedType(typeName);
        topLevelClass.addField(field);
        return field;
    }

}

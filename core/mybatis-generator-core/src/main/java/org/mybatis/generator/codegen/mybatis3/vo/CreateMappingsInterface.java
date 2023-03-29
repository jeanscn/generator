package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 17:53
 * @version 3.0
 */
public class CreateMappingsInterface extends AbstractJavaGenerator {

    public static final String subPackageMaps = "maps";

    protected CreateMappingsInterface(IntrospectedTable introspectedTable, String project, ProgressCallback progressCallback, List<String> warnings) {
        super(project);
        this.context = introspectedTable.getContext();
        this.introspectedTable = introspectedTable;
        this.progressCallback = progressCallback;
        this.warnings = warnings;
    }

    public Interface generate() {
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        String baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".") + "."+AbstractVOGenerator.subPackagePojo;
        Interface mappingsInterface = new Interface(String.join(".", baseTargetPackage, subPackageMaps, entityType.getShortName() + "Mappings"));
        mappingsInterface.setVisibility(JavaVisibility.PUBLIC);
        context.getCommentGenerator().addJavaFileComment(mappingsInterface);
        mappingsInterface.addImportedType(entityType);
        mappingsInterface.addImportedType(new FullyQualifiedJavaType("org.mapstruct.Mapper"));
        mappingsInterface.addImportedType(new FullyQualifiedJavaType("org.mapstruct.factory.Mappers"));
        mappingsInterface.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        mappingsInterface.addAnnotation("@Mapper(componentModel = \"spring\",unmappedTargetPolicy = ReportingPolicy.IGNORE)");
        mappingsInterface.addImportedType(new FullyQualifiedJavaType("org.mapstruct.ReportingPolicy"));
        Field instance = new Field("INSTANCE", new FullyQualifiedJavaType(mappingsInterface.getType().getFullyQualifiedName()));
        instance.setInitializationString(VStringUtil.format("Mappers.getMapper({0}.class)", mappingsInterface.getType().getShortName()));
        mappingsInterface.addField(instance);
        return mappingsInterface;
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        List<CompilationUnit> answer = new ArrayList<>();
        answer.add(generate());
        return answer;
    }

}

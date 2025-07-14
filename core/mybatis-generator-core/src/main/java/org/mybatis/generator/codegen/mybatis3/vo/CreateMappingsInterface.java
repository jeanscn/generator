package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.api.dom.java.JavaVisibility.PUBLIC;
import static org.mybatis.generator.custom.ConstantsUtil.*;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 17:53
 * @version 3.0
 */
public class CreateMappingsInterface extends AbstractJavaGenerator {

    public static final String SUB_PACKAGE_MAPS = "maps";
    public final String methodKey;

    public CreateMappingsInterface(IntrospectedTable introspectedTable, String project, ProgressCallback progressCallback, List<String> warnings,String methodKey) {
        super(project);
        this.context = introspectedTable.getContext();
        this.introspectedTable = introspectedTable;
        this.progressCallback = progressCallback;
        this.warnings = warnings;
        this.methodKey = VStringUtil.stringHasValue(methodKey)?methodKey:"";
    }

    public Interface generate() {
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        String baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".") + "."+ AbstractVoGenerator.SUB_PACKAGE_POJO;
        Interface mappingsInterface = new Interface(String.join(".", baseTargetPackage, SUB_PACKAGE_MAPS, entityType.getShortName() + JavaBeansUtil.getFirstCharacterUppercase(methodKey)+"Mappings"));
        mappingsInterface.setVisibility(PUBLIC);
        context.getCommentGenerator().addJavaFileComment(mappingsInterface);
        mappingsInterface.addImportedType(entityType);
        mappingsInterface.addImportedType(new FullyQualifiedJavaType(MAPSTRUCT_MAPPER));
        mappingsInterface.addImportedType(new FullyQualifiedJavaType(MAPSTRUCT_FACTORY_MAPPERS));
        mappingsInterface.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        mappingsInterface.addAnnotation("@Mapper(componentModel = \"spring\",unmappedTargetPolicy = ReportingPolicy.IGNORE)");
        mappingsInterface.addImportedType(new FullyQualifiedJavaType(MAPSTRUCT_REPORTING_POLICY));
        return mappingsInterface;
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        List<CompilationUnit> answer = new ArrayList<>();
        answer.add(generate());
        return answer;
    }

}

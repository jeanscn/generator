package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.tool.core.VArrayUtil;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.RootClassInfo;
import org.mybatis.generator.config.BaseVOGeneratorConfiguration;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansField;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * 生成VO抽象父类
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-07-05 03:29
 * @version 3.0
 */
public class ViewObjectClassGenerator extends AbstractJavaGenerator {

    public static final String subPackageVo = "vo";
    public static final String subPackageMaps = "maps";
    public static final String subPackageAbs = "abs";

    private CommentGenerator commentGenerator;

    private String targetProject;

    private String targetPackage;

    public ViewObjectClassGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        List<CompilationUnit> answer = new ArrayList<>();
        commentGenerator = context.getCommentGenerator();
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.78", table.toString()));
        Plugin plugins = context.getPlugins();
        TableConfiguration tableConfiguration = introspectedTable.getTableConfiguration();
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        BaseVOGeneratorConfiguration configuration = tableConfiguration.getBaseVOGeneratorConfiguration();
        targetProject = configuration.getTargetProject();

        //生成baseVo
        String modelName = "Abstract" + entityType.getShortName()+"VO";
        String baseVoType = String.join(".",configuration.getTargetPackage() ,subPackageAbs,modelName);
        TopLevelClass topLevelClass = new TopLevelClass(baseVoType);
        topLevelClass.setAbstract(true);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        //类文件注释
        commentGenerator.addJavaFileComment(topLevelClass);
        //类注释
        commentGenerator.addModelClassComment(topLevelClass, introspectedTable);
        topLevelClass.addAnnotation("@Getter");
        topLevelClass.addAnnotation("@Setter");
        topLevelClass.addAnnotation("@NoArgsConstructor");
        topLevelClass.addAnnotation("@AllArgsConstructor");
        topLevelClass.addImportedType("lombok.*");
        topLevelClass.addImportedType("com.alibaba.excel.annotation.ExcelProperty");
        //添加属性
        String rootClass = getRootClass();
        String property = configuration.getProperty(PropertyRegistry.ELEMENT_IGNORE_COLUMNS);
        for (IntrospectedColumn introspectedColumn : introspectedTable.getBaseColumns()) {
            if (StringUtility.stringHasValue(property)) {
                if (VArrayUtil.contains(property.split(","), introspectedColumn.getActualColumnName())) {
                    continue;
                }
            }else{
                if (RootClassInfo.getInstance(rootClass, warnings).containsProperty(introspectedColumn)) {
                    continue;
                }
            }

            Field field = getJavaBeansField(introspectedColumn, context, introspectedTable);
            field.addAnnotation("@ExcelProperty(\"" + introspectedColumn.getRemarks() + "\")");
            if (plugins.voModelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable)) {
                topLevelClass.addField(field);
                topLevelClass.addImportedType(field.getType());
            }
        }
        answer.add(topLevelClass);

        targetPackage = configuration.getTargetPackage();
        //生成ExportVO
        String exportModelName = entityType.getShortName() + "ExportVO";
        String exportVoType = String.join(".",targetPackage,subPackageVo,exportModelName);
        TopLevelClass exportVoClass = getTopLevelClass(exportVoType,baseVoType);
        addMethodAnnotation(exportVoClass,"lombok","ExcelProperty");
        if (fileNotExist(subPackageVo,exportModelName)) {
            answer.add(exportVoClass);
        }

        //生成VO类
        FullyQualifiedJavaType rootClassType = new FullyQualifiedJavaType(getRootClass());
        String voModelName = entityType.getShortName() + "VO";
        String voType = String.join(".", targetPackage,subPackageVo,voModelName);
        TopLevelClass voClass = getTopLevelClass(voType,baseVoType);
        addMethodAnnotation(voClass,"lombok","ApiModel","ExcelProperty");
        String format = VStringUtil.format("@ApiModel(value = \"{0}\", description = \"{1}\", parent = {2}.class )", voModelName,
                introspectedTable.getRemarks(), rootClassType.getShortName());
        voClass.addAnnotation(format);
        voClass.addImportedType(rootClassType);
        voClass.addField(builderSerialVersionUID());
        if (fileNotExist(subPackageVo,voModelName)) {
            answer.add(voClass);
        }

        //生成viewVo类
        String viewVOName = entityType.getShortName() + "ViewVO";
        String viewVOType = String.join(".", targetPackage,subPackageVo,viewVOName);
        TopLevelClass viewVOClass = getTopLevelClass(viewVOType,baseVoType);
        addMethodAnnotation(viewVOClass,"lombok","ApiModel","ViewMeta");
        viewVOClass.addAnnotation(format);
        String viewMeta = VStringUtil.format("@ViewMeta(value = \"{0}\",descript = \"{1}\",beanname = \"{2}\")",
                introspectedTable.getTableConfiguration().getTableName(),
                introspectedTable.getRemarks(),
                JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName())+"Impl");
        viewVOClass.addAnnotation(viewMeta);
        viewVOClass.addImportedType(rootClassType);
        viewVOClass.addImportedType(rootClassType);
        viewVOClass.addField(builderSerialVersionUID());
        if (fileNotExist(subPackageVo,viewVOName)) {
            answer.add(viewVOClass);
        }

        //生成mapstruct接口
        String mappingsName = entityType.getShortName() + "Mappings";
        String mappingsType = String.join(".",targetPackage,subPackageMaps,mappingsName);
        Interface mappingsInterface = new Interface(mappingsType);
        mappingsInterface.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(mappingsInterface);
        mappingsInterface.addImportedType(entityType);
        mappingsInterface.addImportedType(new FullyQualifiedJavaType(exportVoType));
        mappingsInterface.addImportedType(new FullyQualifiedJavaType(voType));
        mappingsInterface.addImportedType(new FullyQualifiedJavaType(viewVOType));
        mappingsInterface.addImportedType(new FullyQualifiedJavaType("org.mapstruct.Mapper"));
        mappingsInterface.addImportedType(new FullyQualifiedJavaType("org.mapstruct.factory.Mappers"));
        mappingsInterface.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        mappingsInterface.addAnnotation("@Mapper");
        Field instance = new Field("INSTANCE", new FullyQualifiedJavaType(mappingsType));
        instance.setInitializationString(VStringUtil.format("Mappers.getMapper({0}.class)", mappingsInterface.getType().getShortName()));
        mappingsInterface.addField(instance);
        mappingsInterface.addMethod(addMappingMethod(voClass.getType(), entityType,false));
        mappingsInterface.addMethod(addMappingMethod(entityType,voClass.getType(),false));
        mappingsInterface.addMethod(addMappingMethod(entityType, exportVoClass.getType(),false));
        mappingsInterface.addMethod(addMappingMethod(entityType,viewVOClass.getType(),false));
        mappingsInterface.addMethod(addMappingMethod(voClass.getType(), entityType,true));
        mappingsInterface.addMethod(addMappingMethod(entityType,voClass.getType(),true));
        mappingsInterface.addMethod(addMappingMethod(entityType,viewVOClass.getType(),true));
        if (fileNotExist(subPackageMaps, mappingsName)) {
            answer.add(mappingsInterface);
        }

        return answer;
    }

    private boolean fileNotExist(String subPackage, String fileName) {
        File project = new File(targetProject);
        if (!project.isDirectory()) {
            return true;
        }
        File directory = new File(project, StringUtility.packageToDir(String.join(".",targetPackage,subPackage)));
        if (!directory.isDirectory()) {
            return true;
        }
        File file = new File(directory, fileName + ".java");
        return !file.exists();
    }

    private TopLevelClass getTopLevelClass(String type,String superType) {
        TopLevelClass topLevelClass = new TopLevelClass(type);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(topLevelClass);
        topLevelClass.addImportedType(superType);
        topLevelClass.setSuperClass(superType);
        return topLevelClass;
    }

    private Method addMappingMethod(FullyQualifiedJavaType fromType, FullyQualifiedJavaType toType,boolean isList) {
       String methodName;
        Method method;
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        if (entityType.getFullyQualifiedName().equalsIgnoreCase(toType.getFullyQualifiedName())) {
            methodName = "from"+fromType.getShortName();
        }else if(entityType.getFullyQualifiedName().equalsIgnoreCase(fromType.getFullyQualifiedName())){
            methodName = "to"+ toType.getShortName();
        }else{
            methodName = JavaBeansUtil.getFirstCharacterLowercase(fromType.getShortName())+"To"+toType.getShortName();
        }
        if (isList) {
            methodName = methodName + "s";
            method = new Method(methodName);
            FullyQualifiedJavaType listInstanceFrom = FullyQualifiedJavaType.getNewListInstance();
            listInstanceFrom.addTypeArgument(fromType);
            method.addParameter(new Parameter(listInstanceFrom, JavaBeansUtil.getFirstCharacterLowercase(fromType.getShortName())+"s"));
            FullyQualifiedJavaType listInstanceTo = FullyQualifiedJavaType.getNewListInstance();
            listInstanceTo.addTypeArgument(toType);
            method.setReturnType(listInstanceTo);
        }else{
            method = new Method(methodName);
            method.addParameter(new Parameter(fromType, JavaBeansUtil.getFirstCharacterLowercase(fromType.getShortName())));
            method.setReturnType(toType);
        }
        method.setAbstract(true);
        return method;
    }

    private void addMethodAnnotation(TopLevelClass voClass, String...types) {
        for (String type : types) {
            switch (type){
                case "lombok":
                    voClass.addImportedType("lombok.*");
                    voClass.addAnnotation("@Data");
                    voClass.addAnnotation("@EqualsAndHashCode(callSuper = true)");
                    voClass.addAnnotation("@ToString(callSuper = true)");
                    break;
                case "ApiModel":
                    voClass.addImportedType("io.swagger.annotations.ApiModel");
                    break;
                case "ExcelProperty":
                    voClass.addImportedType("com.alibaba.excel.annotation.ExcelProperty");
                    break;
                case "ViewMeta":
                    voClass.addImportedType("com.vgosoft.core.annotation.ViewMeta");
                    break;
            }
        }

    }
}

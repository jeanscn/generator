package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.config.VOGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.ApiModel;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.internal.util.VoGenService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * VO生成抽象类
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 11:27
 * @version 3.0
 */
public abstract class AbstractVOGenerator extends AbstractJavaGenerator {

    public static final String subPackageVo = "vo";
    public static final String subPackageAbs = "abs";
    public static final String subPackagePojo = "pojo";

    protected final VoGenService voGenService;

    protected final VOGeneratorConfiguration voGeneratorConfiguration;

    protected final CommentGenerator commentGenerator;

    protected final String baseTargetPackage;

    protected final Plugin plugins;

    protected final Interface mappingsInterface;

    protected final FullyQualifiedJavaType entityType;

    public AbstractVOGenerator(IntrospectedTable introspectedTable, String project,ProgressCallback progressCallback, List<String> warnings,Interface mappingsInterface) {
        super(project);
        this.context = introspectedTable.getContext();
        this.commentGenerator = context.getCommentGenerator();
        this.introspectedTable = introspectedTable;
        this.progressCallback = progressCallback;
        this.warnings = warnings;
        this.voGeneratorConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration();
        this.baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".") + "."+subPackagePojo;
        this.voGenService = new VoGenService(introspectedTable);
        this.plugins = context.getPlugins();
        this.mappingsInterface = mappingsInterface;
        this.entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
    }

    abstract TopLevelClass generate();

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        List<CompilationUnit> answer = new ArrayList<>();
        answer.add(generate());
        return answer;
    }

    protected TopLevelClass createTopLevelClass(String type, String superType) {
        TopLevelClass topLevelClass = new TopLevelClass(type);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(topLevelClass);
        if (!VStringUtil.isBlank(superType)) {
            topLevelClass.addImportedType(superType);
            topLevelClass.setSuperClass(superType);
        }
        return topLevelClass;
    }

    //获取AbstractVO类的类信息
    protected FullyQualifiedJavaType getAbstractVOType() {
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        String abstractName = "Abstract" + entityType.getShortName() + "VO";
        return new FullyQualifiedJavaType(baseTargetPackage + "." + subPackageAbs + "." + abstractName);
    }

    protected ApiModel getApiModel(String voModelName) {
        ApiModel apiModel = ApiModel.create(voModelName);
        apiModel.setParent(getAbstractVOType().getShortName() + ".class");
        apiModel.setDescription(introspectedTable.getRemarks(true));
        return apiModel;
    }

    protected boolean isAbstractVOColumn(IntrospectedColumn introspectedColumn) {
        return voGenService.getAbstractVOColumns()
                .stream()
                .map(IntrospectedColumn::getActualColumnName)
                .anyMatch(t -> t.equalsIgnoreCase(introspectedColumn.getActualColumnName()));
    }

    protected Optional<Method> getOverrideGetter(IntrospectedColumn introspectedColumn) {
        if (!voGenService.getAbstractVOColumns().contains(introspectedColumn)) {
            //重写getter，添加validate
            Method javaBeansGetter = JavaBeansUtil.getJavaBeansGetter(introspectedColumn, context, introspectedTable);
            if (isAbstractVOColumn(introspectedColumn)) {
                javaBeansGetter.addAnnotation("@Override");
            }
            return Optional.of(javaBeansGetter);
        }
        return Optional.empty();
    }

    public Method addMappingMethod(FullyQualifiedJavaType fromType, FullyQualifiedJavaType toType, boolean isList) {
       return VOGeneratorUtil.addMappingMethod(fromType, toType, isList, introspectedTable);
    }

}
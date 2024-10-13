package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.config.RelationGeneratorConfiguration;
import org.mybatis.generator.config.VOGeneratorConfiguration;
import org.mybatis.generator.custom.RelationTypeEnum;
import org.mybatis.generator.custom.annotations.ApiModelDesc;
import org.mybatis.generator.custom.annotations.ApiModelPropertyDesc;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.internal.util.VoGenService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

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

    protected abstract TopLevelClass generate();

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

    protected ApiModelDesc addApiModel(String voModelName) {
        ApiModelDesc apiModelDesc = ApiModelDesc.create(voModelName);
        apiModelDesc.setParent(getAbstractVOType().getShortName() + ".class");
        apiModelDesc.setDescription(introspectedTable.getRemarks(true));
        return apiModelDesc;
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

    protected void addJavaCollectionRelation(TopLevelClass topLevelClass,String type) {
        TreeSet<RelationGeneratorConfiguration> configurations = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations();
        Stream<RelationGeneratorConfiguration> stream = configurations.stream().filter(RelationGeneratorConfiguration::isEnableInsert);
        if("update".equals(type)){
            stream = configurations.stream().filter(RelationGeneratorConfiguration::isEnableUpdate);
        }
        stream.forEach(c -> {
            if (!topLevelClass.isContainField(c.getPropertyName())) {
                FullyQualifiedJavaType fullyQualifiedJavaType;
                if (c.getType().equals(RelationTypeEnum.collection)) {
                    fullyQualifiedJavaType = FullyQualifiedJavaType.getNewListInstance();
                    fullyQualifiedJavaType.addTypeArgument(new FullyQualifiedJavaType(c.getVoModelTye()));
                    topLevelClass.addImportedType(FullyQualifiedJavaType.getNewListInstance());
                } else {
                    fullyQualifiedJavaType = new FullyQualifiedJavaType(c.getVoModelTye());
                }
                Field field = new Field(c.getPropertyName(), fullyQualifiedJavaType);
                field.setVisibility(JavaVisibility.PRIVATE);
                field.setRemark(c.getRemark());
                if (c.getType().equals(RelationTypeEnum.collection)) {
                    field.setInitializationString("new ArrayList<>()");
                }
                topLevelClass.addField(field);
                topLevelClass.addImportedType(c.getVoModelTye());
            }
        });
    }

    protected void addWhereConditionResult(TopLevelClass requestVoClass) {
        Field cascade = new Field("anyWhereCondition", FullyQualifiedJavaType.getStringInstance());
        cascade.setVisibility(JavaVisibility.PRIVATE);
        cascade.setRemark("任意过滤条件");
        new ApiModelPropertyDesc(cascade.getRemark(), "field = ‘condition’").addAnnotationToField(cascade, requestVoClass);
        requestVoClass.addField(cascade);
    }

    //增加查询过滤器的filterMap属性
    protected void addFilterMap(TopLevelClass requestVoClass) {
        Field filterMap = new Field("filterParam", new FullyQualifiedJavaType("com.vgosoft.core.adapter.web.FilterParam"));
        filterMap.setVisibility(JavaVisibility.PRIVATE);
        filterMap.setRemark("前端过滤器及类似查询过滤条件");
        new ApiModelPropertyDesc(filterMap.getRemark(), "filterParam = ‘{}’").addAnnotationToField(filterMap, requestVoClass);
        requestVoClass.addImportedType(new FullyQualifiedJavaType("com.vgosoft.core.adapter.web.FilterParam"));
        requestVoClass.addField(filterMap);
    }
}

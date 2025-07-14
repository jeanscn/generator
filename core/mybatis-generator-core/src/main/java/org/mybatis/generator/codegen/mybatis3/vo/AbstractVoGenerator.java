package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.config.RelationGeneratorConfiguration;
import org.mybatis.generator.config.VoGeneratorConfiguration;
import org.mybatis.generator.custom.enums.RelationTypeEnum;
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
 * Vo生成抽象类
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 11:27
 * @version 3.0
 */
public abstract class AbstractVoGenerator extends AbstractJavaGenerator {

    public static final String SUB_PACKAGE_VO = "vo";
    public static final String SUB_PACKAGE_ABS = "abs";
    public static final String SUB_PACKAGE_POJO = "pojo";

    protected final VoGenService voGenService;

    protected final VoGeneratorConfiguration voGeneratorConfiguration;

    protected final CommentGenerator commentGenerator;

    protected final String baseTargetPackage;

    protected final Plugin plugins;

    protected final Interface mappingsInterface;

    protected final FullyQualifiedJavaType entityType;

    public AbstractVoGenerator(IntrospectedTable introspectedTable, String project, ProgressCallback progressCallback, List<String> warnings, Interface mappingsInterface) {
        super(project);
        this.context = introspectedTable.getContext();
        this.commentGenerator = context.getCommentGenerator();
        this.introspectedTable = introspectedTable;
        this.progressCallback = progressCallback;
        this.warnings = warnings;
        this.voGeneratorConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration();
        this.baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".") + "."+ SUB_PACKAGE_POJO;
        this.voGenService = new VoGenService(introspectedTable);
        this.plugins = context.getPlugins();
        this.mappingsInterface = mappingsInterface;
        this.entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
    }

    /**
     * 生成方法 抽象方法
     * @return TopLevelClass
     */
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

    /**
     * 获取AbstractVo类的类信息
     * @return FullyQualifiedJavaType
     */
    protected FullyQualifiedJavaType getAbstractVoType() {
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        String abstractName = "Abstract" + entityType.getShortName() + "Vo";
        return new FullyQualifiedJavaType(baseTargetPackage + "." + SUB_PACKAGE_ABS + "." + abstractName);
    }

    protected ApiModelDesc addApiModel(String voModelName) {
        ApiModelDesc apiModelDesc = ApiModelDesc.create(voModelName);
        apiModelDesc.setParent(getAbstractVoType().getShortName() + ".class");
        apiModelDesc.setDescription(introspectedTable.getRemarks(true));
        return apiModelDesc;
    }

    protected boolean isAbstractVoColumn(IntrospectedColumn introspectedColumn) {
        return voGenService.getAbstractVoColumns()
                .stream()
                .map(IntrospectedColumn::getActualColumnName)
                .anyMatch(t -> t.equalsIgnoreCase(introspectedColumn.getActualColumnName()));
    }

    protected Optional<Method> getOverrideGetter(IntrospectedColumn introspectedColumn) {
        if (!voGenService.getAbstractVoColumns().contains(introspectedColumn)) {
            //重写getter，添加validate
            Method javaBeansGetter = JavaBeansUtil.getJavaBeansGetter(introspectedColumn, context, introspectedTable);
            if (isAbstractVoColumn(introspectedColumn)) {
                javaBeansGetter.addAnnotation("@Override");
            }
            return Optional.of(javaBeansGetter);
        }
        return Optional.empty();
    }

    public Method addMappingMethod(FullyQualifiedJavaType fromType, FullyQualifiedJavaType toType, boolean isList) {
       return VoGeneratorUtil.addMappingMethod(fromType, toType, isList, introspectedTable);
    }

    protected void addJavaCollectionRelation(TopLevelClass topLevelClass,String type) {
        TreeSet<RelationGeneratorConfiguration> configurations = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations();
        Stream<RelationGeneratorConfiguration> stream = configurations.stream().filter(RelationGeneratorConfiguration::isEnableInsert);
        if("update".equals(type)){
            stream = configurations.stream().filter(RelationGeneratorConfiguration::isEnableUpdate);
        }
        stream.forEach(c -> {
            if (topLevelClass.isNotContainField(c.getPropertyName())) {
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
                if (plugins.voModelFieldGenerated(field, topLevelClass, null, introspectedTable)) {
                    topLevelClass.addField(field);
                    topLevelClass.addImportedType(c.getVoModelTye());
                }
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

    protected void addProperty(TopLevelClass topLevelClass,Field field, String strExample,IntrospectedTable introspectedTable) {
        field.setVisibility(JavaVisibility.PRIVATE);
        ApiModelPropertyDesc apiModelPropertyDesc = new ApiModelPropertyDesc(field.getRemark());
        apiModelPropertyDesc.setExample(strExample);
        field.addAnnotation(apiModelPropertyDesc.toAnnotation());
        topLevelClass.addImportedTypes(apiModelPropertyDesc.getImportedTypes());
        if (introspectedTable.getRules().isIntegrateMybatisPlus()) {
            field.addAnnotation("@TableField(exist = false)");
            topLevelClass.addImportedType("com.baomidou.mybatisplus.annotation.TableField");
        }
        topLevelClass.addField(field);
    }
}

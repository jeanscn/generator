package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.mybatis.generate.GenerateSqlTemplate;
import com.vgosoft.mybatis.sqlbuilder.InsertSqlBuilder;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.VoGeneratorConfiguration;
import org.mybatis.generator.config.VoViewGeneratorConfiguration;
import org.mybatis.generator.custom.enums.ScalableElementEnum;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.codegen.mybatis3.vo.AbstractVoGenerator.SUB_PACKAGE_POJO;
import static org.mybatis.generator.codegen.mybatis3.vo.CreateMappingsInterface.SUB_PACKAGE_MAPS;
import static org.mybatis.generator.internal.util.StringUtility.packageToDir;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * 生成Vo抽象父类
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-07-05 03:29
 * @version 3.0
 */
public class ViewObjectClassGenerator extends AbstractJavaGenerator {

    TableConfiguration tc;

    private TopLevelClass requestVoClass;
    private boolean generated = false;

    public ViewObjectClassGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        List<CompilationUnit> answer = new ArrayList<>();
        progressCallback.startTask(getString("Progress.78", introspectedTable.getFullyQualifiedTable().toString()));
        tc = introspectedTable.getTableConfiguration();

        VoGeneratorConfiguration voGeneratorConfiguration = tc.getVoGeneratorConfiguration();
        if (voGeneratorConfiguration == null || !voGeneratorConfiguration.isGenerate()) {
            return answer;
        }

        TopLevelClass abstractVo = new VoAbstractGenerator(introspectedTable, "project", progressCallback, warnings, null).generate();

        List<String> absExampleFields = abstractVo.getFields().stream().map(Field::getName).collect(Collectors.toList());
        introspectedTable.getTopLevelClassExampleFields().put(abstractVo.getType().getShortName(), absExampleFields);

        if (context.getPlugins().voModelAbstractClassGenerated(abstractVo, introspectedTable)) {
            answer.add(abstractVo);
        }

        /*
         * 生成mappings类
         * */
        CreateMappingsInterface createMappingsInterface = new CreateMappingsInterface(introspectedTable, "project", progressCallback, warnings, null);
        Interface mappingsInterface = createMappingsInterface.generate();

        String comment1 = String.format("mapstruct映射接口，%s", introspectedTable.getRemarks(true));
        String comment2 = String.format("%s的Vo/Po转换接口", introspectedTable.getTableConfiguration().getDomainObjectName());
        context.getCommentGenerator().addModelClassComment(mappingsInterface, comment1,false, comment2,"");
        //先添加指定的转换方法
        introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getMappingConfigurations().forEach(mapstructMappingConfiguration -> {
            FullyQualifiedJavaType source = new FullyQualifiedJavaType(mapstructMappingConfiguration.getSourceType());
            mapstructMappingConfiguration.getSourceArguments().forEach(s -> {
                FullyQualifiedJavaType type = new FullyQualifiedJavaType(s);
                source.addTypeArgument(type);
                mappingsInterface.addImportedType(type);
            });
            FullyQualifiedJavaType target = new FullyQualifiedJavaType(mapstructMappingConfiguration.getTargetType());
            mapstructMappingConfiguration.getTargetArguments().forEach(s -> {
                FullyQualifiedJavaType type = new FullyQualifiedJavaType(s);
                target.addTypeArgument(type);
                mappingsInterface.addImportedType(type);
            });
            mappingsInterface.addImportedType(source);
            mappingsInterface.addImportedType(target);
            Method method = VoGeneratorUtil.addMappingMethod(source, target, "list".equals(mapstructMappingConfiguration.getType()), introspectedTable);
            //添加mapping默认忽略属性
            List<String> defaultFields = new ArrayList<>(Arrays.asList("id","persistenceBeanName","persistenceStatus", "modelTempId", "version", "workflowEnabled","restBasePath","viewPath"));
            List<String> absFields = new ArrayList<>(); //与表结构字段一起控制是否允许添加映射或隐藏配置
            Set<String> ignoreFields = new HashSet<>();
            if (mapstructMappingConfiguration.isIgnoreDefault()) {
                ignoreFields.addAll(GlobalConstant.MAPSTRUCT_IGNORE_DEFAULT_FIELD);
            }
            if (mapstructMappingConfiguration.isIgnoreBusiness()){
                ignoreFields.addAll(GlobalConstant.MAPSTRUCT_IGNORE_BUSINESS_FIELD);
            }
            ignoreFields.addAll(defaultFields);
            //忽略字段
            List<String> allTableFields = new ArrayList<>();
            this.context.getIntrospectedTables().forEach(t -> {
                String objectName = t.getTableConfiguration().getDomainObjectName();
                if (target.getShortName().equals(objectName)) {
                    allTableFields.addAll(t.getAllColumns().stream().map(IntrospectedColumn::getJavaProperty).collect(Collectors.toList()));
                    FullyQualifiedJavaType rootClass = new FullyQualifiedJavaType(JavaBeansUtil.getRootClass(t));
                    EntityAbstractParentEnum entityAbstractParentEnum = EntityAbstractParentEnum.ofCode(rootClass.getShortName());
                    if (entityAbstractParentEnum != null) {
                        absFields.addAll(entityAbstractParentEnum.fields());
                    }
                }
            });
            for (String ignoreField : ignoreFields) {
                if (allTableFields.contains(ignoreField) || absFields.contains(ignoreField)) {
                    mapstructMappingConfiguration.getIgnoreFields().remove(ignoreField);
                    method.addAnnotation(String.format("@Mapping(target = \"%s\", ignore = true)", ignoreField));
                    mappingsInterface.addImportedType(new FullyQualifiedJavaType("org.mapstruct.Mapping"));
                }
            }
            for (String ignoreField : mapstructMappingConfiguration.getIgnoreFields()) {
                method.addAnnotation(String.format("@Mapping(target = \"%s\", ignore = true)", ignoreField));
                mappingsInterface.addImportedType(new FullyQualifiedJavaType("org.mapstruct.Mapping"));
            }
            //额外映射字段
            mapstructMappingConfiguration.getAdditionalMappings().forEach(m -> {
                if (m.contains("=")) {
                    String[] split = m.split("=");
                    if (split.length > 1) {
                        split[0] = split[0].trim();
                        split[1] = split[1].trim();
                        if (absFields.contains(split[1]) || allTableFields.contains(split[1])) {
                            method.addAnnotation(String.format("@Mapping(target = \"%s\", source = \"%s\")", split[0], split[1]));
                            mappingsInterface.addImportedType(new FullyQualifiedJavaType("org.mapstruct.Mapping"));
                        }
                    }
                }
            });
            //context.getCommentGenerator().addMappingMethodJavaDocLine(method,source,target);
            mappingsInterface.addMethod(method);
        });

        /*
         * 生成Vo类
         * */
        if (introspectedTable.getRules().isGenerateVoModel()) {
            generated = true;
            TopLevelClass voClass = new VoModelGenerator(introspectedTable, "project", progressCallback, warnings, mappingsInterface).generate();
            if (introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.modelVo.name()) || fileNotExist(voClass.getType().getShortName())) {
                if (context.getPlugins().voModelRecordClassGenerated(voClass, introspectedTable)) {
                    answer.add(voClass);
                }
            }
            //生成example属性列表
            List<String> voExampleFields = voClass.getFields().stream().map(Field::getName).collect(Collectors.toList());
            voExampleFields.addAll(absExampleFields);
            introspectedTable.getTopLevelClassExampleFields().put(voClass.getType().getShortName(), voExampleFields);
        }

        /*
         *  生成createVo类
         *  */
        if (introspectedTable.getRules().isGenerateCreateVo()) {
            generated = true;
            TopLevelClass createVoClass = new VoCreateGenerator(introspectedTable, "project", progressCallback, warnings, mappingsInterface).generate();
            if (introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.createVo.name()) || fileNotExist(createVoClass.getType().getShortName())) {
                if (context.getPlugins().voModelCreateClassGenerated(createVoClass, introspectedTable)) {
                    answer.add(createVoClass);
                }
            }
        }
        /*
         *  生成updateVo类
         *  */
        if (introspectedTable.getRules().isGenerateUpdateVo()) {
            generated = true;
            TopLevelClass updateVoClass = new VoUpdateGenerator(introspectedTable, "project", progressCallback, warnings, mappingsInterface).generate();
            if (introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.updateVo.name()) || fileNotExist(updateVoClass.getType().getShortName())) {
                if (context.getPlugins().voModelUpdateClassGenerated(updateVoClass, introspectedTable)) {
                    answer.add(updateVoClass);
                }
            }
        }
        /*
         * 生成viewVo类
         * */
        if (introspectedTable.getRules().isGenerateViewVo()) {
            generated = true;
            TopLevelClass viewVoClass = new VoViewGenerator(introspectedTable, "project", progressCallback, warnings, mappingsInterface).generate();
            //添加生成viewVo到生成列表及菜单项
            boolean genViewVo = introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.viewVo.name()) || fileNotExist(viewVoClass.getType().getShortName());
            if (genViewVo) {
                VoViewGeneratorConfiguration viewConfiguration = voGeneratorConfiguration.getVoViewConfiguration();
                if (context.getPlugins().voModelViewClassGenerated(viewVoClass, introspectedTable)) {
                    answer.add(viewVoClass);
                    //添加菜单项
                    String parentMenuId = Optional.ofNullable(viewConfiguration.getParentMenuId())
                            .orElse(context.getParentMenuId());
                    if (stringHasValue(parentMenuId) && context.isUpdateMenuData() && introspectedTable.getTableConfiguration().isModules()) {
                        int sort = (context.getSysMenuDataScriptLines().size()+2) * 10;
                        String id = Mb3GenUtil.getDefaultViewId(introspectedTable);
                        String title = viewConfiguration.getTitle() != null ? viewConfiguration.getTitle() : introspectedTable.getRemarks(true);
                        InsertSqlBuilder sqlBuilder = GenerateSqlTemplate.insertSqlForMenu();
                        sqlBuilder.updateStringValues("id_", id);
                        sqlBuilder.updateStringValues("name_", introspectedTable.getTableConfiguration().getDomainObjectName());
                        sqlBuilder.updateStringValues("icon_", viewConfiguration.getViewMenuIcon());
                        sqlBuilder.updateStringValues("parent_id", parentMenuId);
                        sqlBuilder.updateStringValues("sort_", String.valueOf(sort));
                        sqlBuilder.updateStringValues("title_", title);
                        sqlBuilder.updateStringValues("component_", introspectedTable.getTableConfiguration().getDomainObjectName());
                        sqlBuilder.updateStringValues("el_icon", viewConfiguration.getViewMenuElIcon());
                        //构造path
                        String moduleName = this.getContext().getModuleKeyword();
                        String tableName = this.introspectedTable.getTableConfiguration().getTableName();
                        sqlBuilder.updateStringValues("path_", "/" + String.join("/", moduleName, tableName));
                        sqlBuilder.updateStringValues("url_", "viewmgr/" + id + "/open-view");
                        sqlBuilder.updateStringValues("notes_", context.getModuleName() + "->" + title + "默认视图");
                        sqlBuilder.updateStringValues("created_id", GlobalConstant.DEFAULT_SYSTEM_ADMIN_ID);
                        sqlBuilder.updateStringValues("modified_id", GlobalConstant.DEFAULT_SYSTEM_ADMIN_ID);
                        sqlBuilder.updateStringValues("tenant_id", GlobalConstant.DEFAULT_TENANT_ID);
                        if (viewConfiguration.getEnableShortcut()) {
                            sqlBuilder.updateValues("enable_shortcut", "true");
                        }
                        introspectedTable.getContext().addSysMenuDataScriptLines(id, sqlBuilder.toSql() + ";");
                    }
                }

            }
        }
        /*
         *  生成ExcelVo
         *  */
        if (introspectedTable.getRules().isGenerateExcelVo()) {
            TopLevelClass excelVoClass = new VoExcelGenerator(introspectedTable, "project", progressCallback, warnings, mappingsInterface).generate();
            //添加生成excelVo到生成列表
            if (introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.excelVo.name()) || fileNotExist(excelVoClass.getType().getShortName())) {
                if (context.getPlugins().voModelExcelClassGenerated(excelVoClass, introspectedTable)) {
                    answer.add(excelVoClass);
                }
            }
        }

        /*
         * 生成ExcelImportVo
         * */
        if (introspectedTable.getRules().isGenerateExcelVo()) {
            TopLevelClass excelImportVoClass = new VoExcelImportGenerator(introspectedTable, "project", progressCallback, warnings, mappingsInterface).generate();
            //添加生成excelImportVo到生成列表
            if (introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.excelVo.name()) || fileNotExist(excelImportVoClass.getType().getShortName())) {
                if (context.getPlugins().voModelExcelImportClassGenerated(excelImportVoClass, introspectedTable)) {
                    answer.add(excelImportVoClass);
                }
            }
        }

        /*
         * 生成RequestVo
         * */
        if (introspectedTable.getRules().isGenerateRequestVo()) {
            generated = true;
            requestVoClass = new VoRequestGenerator(introspectedTable, "project", progressCallback, warnings, mappingsInterface).generate();
            //增加between的other属性
            voGeneratorConfiguration.getVoRequestConfiguration().getVoNameFragmentGeneratorConfigurations().stream()
                    .filter(c -> "Between".equals(c.getFragment()))
                    .forEach(c -> introspectedTable.getColumn(c.getColumn()).ifPresent(column -> {
                        Field field = JavaBeansUtil.getJavaBeansField(column, context, introspectedTable);
                        field.setVisibility(JavaVisibility.PRIVATE);
                        addFieldToRequestVoClass(field, requestVoClass, abstractVo, column);
                        Field other = JavaBeansUtil.getJavaBeansField(column, context, introspectedTable);
                        other.setName(other.getName() + "Other");
                        other.setVisibility(JavaVisibility.PRIVATE);
                        addFieldToRequestVoClass(other, requestVoClass, abstractVo, column);
                    }));

            if (introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.requestVo.name()) || fileNotExist(requestVoClass.getType().getShortName())) {
                if (context.getPlugins().voModelRequestClassGenerated(requestVoClass, introspectedTable)) {
                    generated = true;
                    answer.add(requestVoClass);
                }
            }
            List<String> requestExampleFields = requestVoClass.getFields().stream().map(Field::getName).collect(Collectors.toList());
            requestExampleFields.addAll(absExampleFields);
            introspectedTable.getTopLevelClassExampleFields().put(requestVoClass.getType().getShortName(), requestExampleFields);
        }

        //生成mapstruct接口
        if (generated) {
            String baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".") + "."+ AbstractVoGenerator.SUB_PACKAGE_POJO;
            boolean overridable = Mb3GenUtil.fileOverridable(getProject(),baseTargetPackage+"."+SUB_PACKAGE_MAPS, mappingsInterface.getType().getShortName()+".java");
            if (overridable) {
                answer.add(mappingsInterface);
            }
        }
        return answer;
    }

    private boolean fileNotExist(String fileName) {
        File project = new File(getProject());
        if (!project.isDirectory()) {
            return true;
        }
        String baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".") + "." + SUB_PACKAGE_POJO;
        File directory = new File(project, packageToDir(String.join(".", baseTargetPackage, AbstractVoGenerator.SUB_PACKAGE_VO)));
        if (!directory.isDirectory()) {
            return true;
        }
        File file = new File(directory, fileName + ".java");
        return !file.exists();
    }

    private void addFieldToRequestVoClass(final Field field, TopLevelClass topLevelClass, TopLevelClass abstractVo, IntrospectedColumn introspectedColumn) {
        if (Stream.of(topLevelClass.getFields().stream(), abstractVo.getFields().stream())
                .flatMap(Function.identity())
                .noneMatch(f -> field.getName().equals(f.getName()))) {
            if (context.getPlugins().voRequestFieldGenerated(field, requestVoClass, introspectedColumn, introspectedTable)) {
                topLevelClass.addField(field);
                topLevelClass.addImportedType(field.getType());
            }
        }
    }
}

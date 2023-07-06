package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.mybatis.generate.GenerateSqlTemplate;
import com.vgosoft.mybatis.sqlbuilder.InsertSqlBuilder;
import com.vgosoft.tool.core.VMD5Util;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.VOGeneratorConfiguration;
import org.mybatis.generator.custom.ScalableElementEnum;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.codegen.mybatis3.vo.AbstractVOGenerator.subPackagePojo;
import static org.mybatis.generator.codegen.mybatis3.vo.CreateMappingsInterface.subPackageMaps;
import static org.mybatis.generator.internal.util.StringUtility.packageToDir;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * 生成VO抽象父类
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

        VOGeneratorConfiguration voGeneratorConfiguration = tc.getVoGeneratorConfiguration();
        if (voGeneratorConfiguration == null || !voGeneratorConfiguration.isGenerate()) {
            return answer;
        }

        TopLevelClass abstractVo = new VOAbstractGenerator(introspectedTable, "project", progressCallback, warnings, null).generate();

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
        //先添加指定的转换方法
        introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getMappingConfigurations().forEach(c -> {
            FullyQualifiedJavaType source = new FullyQualifiedJavaType(c.getSourceType());
            c.getSourceArguments().forEach(s -> {
                FullyQualifiedJavaType type = new FullyQualifiedJavaType(s);
                source.addTypeArgument(type);
                mappingsInterface.addImportedType(type);
            });
            FullyQualifiedJavaType target = new FullyQualifiedJavaType(c.getTargetType());
            c.getTargetArguments().forEach(s -> {
                FullyQualifiedJavaType type = new FullyQualifiedJavaType(s);
                target.addTypeArgument(type);
                mappingsInterface.addImportedType(type);
            });
            mappingsInterface.addImportedType(source);
            mappingsInterface.addImportedType(target);
            mappingsInterface.addMethod(VOGeneratorUtil.addMappingMethod(source, target, c.getType().equals("list"), introspectedTable));
        });

        /*
         * 生成VO类
         * */
        if (introspectedTable.getRules().isGenerateVoModel()) {
            generated = true;
            TopLevelClass voClass = new VOModelGenerator(introspectedTable, "project", progressCallback, warnings, mappingsInterface).generate();
            if (introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.modelVo.name()) || fileNotExist(AbstractVOGenerator.subPackageVo, voClass.getType().getShortName())) {
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
        if (introspectedTable.getRules().isGenerateCreateVO()) {
            generated = true;
            TopLevelClass createVoClass = new VOCreateGenerator(introspectedTable, "project", progressCallback, warnings, mappingsInterface).generate();
            if (introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.createVo.name()) || fileNotExist(AbstractVOGenerator.subPackageVo, createVoClass.getType().getShortName())) {
                if (context.getPlugins().voModelCreateClassGenerated(createVoClass, introspectedTable)) {
                    answer.add(createVoClass);
                }
            }
        }
        /*
         *  生成updateVo类
         *  */
        if (introspectedTable.getRules().isGenerateUpdateVO()) {
            generated = true;
            TopLevelClass updateVoClass = new VOUpdateGenerator(introspectedTable, "project", progressCallback, warnings, mappingsInterface).generate();
            if (introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.updateVo.name()) || fileNotExist(AbstractVOGenerator.subPackageVo, updateVoClass.getType().getShortName())) {
                if (context.getPlugins().voModelUpdateClassGenerated(updateVoClass, introspectedTable)) {
                    answer.add(updateVoClass);
                }
            }
        }
        /*
         * 生成viewVo类
         * */
        if (introspectedTable.getRules().isGenerateViewVO()) {
            generated = true;
            TopLevelClass viewVOClass = new VOViewGenerator(introspectedTable, "project", progressCallback, warnings, mappingsInterface).generate();
            //添加生成viewVO到生成列表及菜单项
            boolean genViewVo = introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.viewVo.name()) || fileNotExist(AbstractVOGenerator.subPackageVo, viewVOClass.getType().getShortName());
            if (genViewVo) {
                if (context.getPlugins().voModelViewClassGenerated(viewVOClass, introspectedTable)) {
                    answer.add(viewVOClass);
                    //添加菜单项
                    String parentMenuId = Optional.ofNullable(voGeneratorConfiguration.getVoViewConfiguration().getParentMenuId())
                            .orElse(context.getParentMenuId());
                    if (stringHasValue(parentMenuId) && context.isUpdateMenuData() && introspectedTable.getTableConfiguration().isModules()) {
                        int sort = context.getSysMenuDataScriptLines().size() + 1;
                        String id = Mb3GenUtil.getDefaultViewId(introspectedTable);
                        String title = introspectedTable.getRemarks(true);
                        InsertSqlBuilder sqlBuilder = GenerateSqlTemplate.insertSqlForMenu();
                        sqlBuilder.updateStringValues("id_", id);
                        sqlBuilder.updateStringValues("name_", title);
                        sqlBuilder.updateStringValues("icon_", voGeneratorConfiguration.getVoViewConfiguration().getViewMenuIcon());
                        sqlBuilder.updateStringValues("parent_id", parentMenuId);
                        sqlBuilder.updateStringValues("sort_", String.valueOf(sort));
                        sqlBuilder.updateStringValues("title_", title);
                        sqlBuilder.updateStringValues("url_", "viewmgr/" + id + "/open-view");
                        sqlBuilder.updateStringValues("notes_", context.getModuleName() + "->" + title + "默认视图");
                        introspectedTable.getContext().addSysMenuDataScriptLines(id, sqlBuilder.toSql() + ";");
                    }
                }

            }
        }
        /*
         *  生成ExcelVO
         *  */
        if (introspectedTable.getRules().isGenerateExcelVO()) {
            TopLevelClass excelVoClass = new VOExcelGenerator(introspectedTable, "project", progressCallback, warnings, mappingsInterface).generate();
            //添加生成excelVO到生成列表
            if (introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.excelVo.name()) || fileNotExist(AbstractVOGenerator.subPackageVo, excelVoClass.getType().getShortName())) {
                if (context.getPlugins().voModelExcelClassGenerated(excelVoClass, introspectedTable)) {
                    answer.add(excelVoClass);
                }
            }
        }

        /*
         * 生成ExcelImportVO
         * */
        if (introspectedTable.getRules().isGenerateExcelVO()) {
            TopLevelClass excelImportVoClass = new VOExcelImportGenerator(introspectedTable, "project", progressCallback, warnings, mappingsInterface).generate();
            //添加生成excelImportVO到生成列表
            if (introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.excelVo.name()) || fileNotExist(AbstractVOGenerator.subPackageVo, excelImportVoClass.getType().getShortName())) {
                if (context.getPlugins().voModelExcelImportClassGenerated(excelImportVoClass, introspectedTable)) {
                    answer.add(excelImportVoClass);
                }
            }
        }

        /*
         * 生成RequestVO
         * */
        if (introspectedTable.getRules().isGenerateRequestVO()) {
            generated = true;
            requestVoClass = new VORequestGenerator(introspectedTable, "project", progressCallback, warnings, mappingsInterface).generate();
            //增加between的other属性
            voGeneratorConfiguration.getVoRequestConfiguration().getVoNameFragmentGeneratorConfigurations().stream()
                    .filter(c -> "Between".equals(c.getFragment()))
                    .forEach(c -> introspectedTable.getColumn(c.getColumn()).ifPresent(column -> {
                        Field field = JavaBeansUtil.getJavaBeansField(column, context, introspectedTable);
                        field.setVisibility(JavaVisibility.PRIVATE);
                        addFieldToTopLevelClass(field, requestVoClass, abstractVo);
                        Field other = JavaBeansUtil.getJavaBeansField(column, context, introspectedTable);
                        other.setName(other.getName() + "Other");
                        other.setVisibility(JavaVisibility.PRIVATE);
                        addFieldToTopLevelClass(other, requestVoClass, abstractVo);
                    }));

            if (introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.requestVo.name()) || fileNotExist(AbstractVOGenerator.subPackageVo, requestVoClass.getType().getShortName())) {
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
            if (introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.maps.name()) || fileNotExist(subPackageMaps, mappingsInterface.getType().getShortName())) {
                answer.add(mappingsInterface);
            }
        }
        return answer;
    }

    private boolean fileNotExist(String subPackage, String fileName) {
        File project = new File(getProject());
        if (!project.isDirectory()) {
            return true;
        }
        String baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".") + "." + subPackagePojo;
        File directory = new File(project, packageToDir(String.join(".", baseTargetPackage, subPackage)));
        if (!directory.isDirectory()) {
            return true;
        }
        File file = new File(directory, fileName + ".java");
        return !file.exists();
    }

    private void addFieldToTopLevelClass(final Field field, TopLevelClass topLevelClass, TopLevelClass abstractVo) {
        if (Stream.of(topLevelClass.getFields().stream(), abstractVo.getFields().stream())
                .flatMap(Function.identity())
                .noneMatch(f -> field.getName().equals(f.getName()))) {
            topLevelClass.addField(field);
            topLevelClass.addImportedType(field.getType());
        }
    }
}

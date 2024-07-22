package org.mybatis.generator.plugins.vue3;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.core.permission.annotation.DataPermission;
import com.vgosoft.tool.TypeConverterTsUtil;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.freeMaker.vue3.GeneratedVueFile;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.VOViewGeneratorConfiguration;
import org.mybatis.generator.custom.FieldItem;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.io.File;
import java.util.*;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 15:44
 * @version 3.0
 */
public class Vue3FilesPlugin extends PluginAdapter {


    @Override
    public List<GeneratedFile> contextGenerateAdditionalWebFiles(IntrospectedTable introspectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        List<GeneratedFile> answer = new ArrayList<>();
        if (!this.isGenerateVueFile(introspectedTable)) {
            return answer;
        }
        if (htmlGeneratorConfiguration != null) {
            String modulesPath = getVueEndProjectBasePath(introspectedTable);
            String objectName = introspectedTable.getTableConfiguration().getDomainObjectName();
            String modelPath = objectName.toLowerCase();
            String tableRemark = introspectedTable.getRemarks(true);
            // 生成vue的路由组件
            String viewPath = String.join(File.separator, (modulesPath + "/views").split("/"));
            String project = this.properties.getProperty("targetProject", viewPath);
            String fileName = introspectedTable.getTableConfiguration().getDomainObjectName();
            Map<String, Object> freeMakerContext = new HashMap<>();
            freeMakerContext.put("componentName", objectName);
            freeMakerContext.put("tableName", introspectedTable.getTableConfiguration().getTableName());
            freeMakerContext.put("modelPath", modelPath);
            freeMakerContext.put("restBasePath", Mb3GenUtil.getControllerBaseMappingPath(introspectedTable));
            freeMakerContext.put("tableRemark", tableRemark);
            // 列渲染
            Map<String, String> columnRenderFunMap = new HashMap<>();
            if (introspectedTable.getRules().isGenerateViewVO()) {
                VOViewGeneratorConfiguration viewConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
                viewConfiguration.getVoColumnRenderFunGeneratorConfigurations().forEach(config -> {
                    config.getFieldNames().forEach(fieldName -> {
                        columnRenderFunMap.putIfAbsent(fieldName, config.getRenderFun());
                    });
                });
                freeMakerContext.put("modelType", viewConfiguration.getTableType());
            }
            freeMakerContext.put("columnRenderFunMap", columnRenderFunMap);
            if (VStringUtil.stringHasValue(fileName)) {
                String vueViewFileNameDev = fileName + ".vue";
                GeneratedVueFile generatedVueFile = new GeneratedVueFile(
                        vueViewFileNameDev,
                        project,
                        "",
                        introspectedTable,
                        "vue_module_list.vue.ftl", freeMakerContext);
                generatedVueFile.setOverWriteFile(htmlGeneratorConfiguration.isOverWriteVueView());
                answer.add(generatedVueFile);
            }

            //生成vue的type文件
            List<FieldItem> fieldsList = introspectedTable.getVoModelFields();
            if (!fieldsList.isEmpty()) {
                String typePath = String.join(File.separator, (modulesPath + "/" + modelPath + "/types").split("/"));
                String projectType = this.properties.getProperty("targetProject", typePath);
                String fileNameType = "T" + introspectedTable.getTableConfiguration().getDomainObjectName();
                Map<String, Object> map = new HashMap<>();
                fieldsList.forEach(fieldItem -> {
                    String type = fieldItem.getType();
                    String tsType = TypeConverterTsUtil.convertToTsType(type);
                    fieldItem.setType(tsType);
                });
                map.put("fields", fieldsList);
                map.put("typeName", fileNameType);
                map.put("tableRemark", tableRemark);
                if (VStringUtil.stringHasValue(fileNameType)) {
                    String vueTypeFileName = fileNameType + ".ts";
                    GeneratedVueFile generatedVueTypeFile = new GeneratedVueFile(
                            vueTypeFileName,
                            projectType,
                            "",
                            introspectedTable,
                            "vue_module_type.ts.ftl", map);
                    generatedVueTypeFile.setOverWriteFile(true);
                    answer.add(generatedVueTypeFile);
                }
            }

            //生成edit组件
            String editPath = String.join(File.separator, (modulesPath + "/" + modelPath+ "/components").split("/"));
            String projectEdit = this.properties.getProperty("targetProject", editPath);
            String fileNameEdit = introspectedTable.getTableConfiguration().getDomainObjectName() + "Edit";
            Map<String, Object> editMap = new HashMap<>();
            editMap.put("modelName", objectName);
            editMap.put("tableName", introspectedTable.getTableConfiguration().getTableName());
            editMap.put("modelPath", modelPath);
            editMap.put("restBasePath", Mb3GenUtil.getControllerBaseMappingPath(introspectedTable));
            editMap.put("tableRemark", tableRemark);
            String vueEditFileName = fileNameEdit + ".vue";
            GeneratedVueFile generatedVueEditFile = new GeneratedVueFile(
                    vueEditFileName,
                    projectEdit,
                    "",
                    introspectedTable,
                    "vue_module_edit.vue.ftl", editMap);
            generatedVueEditFile.setOverWriteFile(htmlGeneratorConfiguration.isOverWriteVueEdit());
            answer.add(generatedVueEditFile);

            //生成detail组件
            String fileNameDetail = introspectedTable.getTableConfiguration().getDomainObjectName() + "Detail";
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("modelName", objectName);
            detailMap.put("tableName", introspectedTable.getTableConfiguration().getTableName());
            detailMap.put("modelPath", modelPath);
            detailMap.put("restBasePath", Mb3GenUtil.getControllerBaseMappingPath(introspectedTable));
            detailMap.put("tableRemark", tableRemark);
            String vueDetailFileName = fileNameDetail + ".vue";
            GeneratedVueFile generatedVueDetailFile = new GeneratedVueFile(
                    vueDetailFileName,
                    projectEdit,
                    "",
                    introspectedTable,
                    "vue_module_detail.vue.ftl", detailMap);
            generatedVueDetailFile.setOverWriteFile(htmlGeneratorConfiguration.isOverWriteVueDetail());
            answer.add(generatedVueDetailFile);

            //生成PrivateTableColumnSlots组件
            String slotsPath = String.join(File.separator, (modulesPath + "/" + modelPath).split("/"));
            String projectSlots = this.properties.getProperty("targetProject", slotsPath);
            String fileNamePrivateTableColumnSlots = "PrivateTableColumnSlots";
            Map<String, Object> privateTableColumnSlotsMap = new HashMap<>();
            privateTableColumnSlotsMap.put("modelName", objectName);
            privateTableColumnSlotsMap.put("tableRemark", tableRemark);
            privateTableColumnSlotsMap.put("workflowEnabled", GenerateUtils.isWorkflowInstance(introspectedTable));
            String vuePrivateTableColumnSlotsFileName = fileNamePrivateTableColumnSlots + ".vue";
            GeneratedVueFile generatedVuePrivateTableColumnSlotsFile = new GeneratedVueFile(
                    vuePrivateTableColumnSlotsFileName,
                    projectSlots,
                    "",
                    introspectedTable,
                    "vue_module_private_column_slots.vue.ftl", privateTableColumnSlotsMap);
            generatedVuePrivateTableColumnSlotsFile.setOverWriteFile(false);
            answer.add(generatedVuePrivateTableColumnSlotsFile);

            //生成PrivateUseFormHooks钩子函数定义文件
            String hooksPath = String.join(File.separator, (modulesPath + "/" + modelPath).split("/"));
            String projectHooks = this.properties.getProperty("targetProject", hooksPath);
            String fileNamePrivateUseFormHooks = "PrivateUseFormHooks";
            Map<String, Object> privateUseFormHooksMap = new HashMap<>();
            privateUseFormHooksMap.put("modelName", objectName);
            privateUseFormHooksMap.put("tableRemark", tableRemark);
            GeneratedVueFile generatedVuePrivateUseFormHooksFile = new GeneratedVueFile(
                    fileNamePrivateUseFormHooks + ".ts",
                    projectHooks,
                    "",
                    introspectedTable,
                    "vue_module_private_form_hooks.vue.ftl", privateUseFormHooksMap);
            generatedVuePrivateUseFormHooksFile.setOverWriteFile(htmlGeneratorConfiguration.isOverWriteJsFile());
            answer.add(generatedVuePrivateUseFormHooksFile);
        }
        return answer;
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (this.isGenerateVueFile(introspectedTable)) {
            FieldItem fieldItem = new FieldItem(field.getName(), field.getType().getShortName(), isOptionalTypeField(introspectedColumn));
            if (introspectedColumn != null) {
                fieldItem.setRemarks(introspectedColumn.getRemarks(false));
            }else{
                fieldItem.setRemarks(field.getRemark());
            }
            introspectedTable.getVoModelFields().add(fieldItem);
        }
        return true;
    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (this.isGenerateVueFile(introspectedTable)) {
            FieldItem fieldItem = new FieldItem(field.getName(), field.getType().getShortName(), isOptionalTypeField(introspectedColumn));
            if (introspectedColumn != null) {
                fieldItem.setRemarks(introspectedColumn.getRemarks(false));
            }else{
                fieldItem.setRemarks(field.getRemark());
            }
            introspectedTable.getVoModelFields().add(fieldItem);
        }
        return true;
    }

    private boolean isOptionalTypeField(IntrospectedColumn introspectedColumn) {
        if (introspectedColumn == null || introspectedColumn.isNullable()) {
            return true;
        } else {
            return EntityAbstractParentEnum.ABSTRACT_WORKFLOW_BUSINESS_NUMBERABLE.fields().stream().anyMatch(f -> f.equals(introspectedColumn.getJavaProperty()));
        }
    }

    private boolean isGenerateVueFile(IntrospectedTable introspectedTable) {
        return VStringUtil.stringHasValue(introspectedTable.getContext().getVueEndProjectPath());
    }

    private String getVueEndProjectBasePath(IntrospectedTable introspectedTable) {
        String vueEndProjectPath = introspectedTable.getContext().getVueEndProjectPath();
        String replace = vueEndProjectPath.replace("\\", "/").replace("//", "/");
        return replace + "/src/modules/" + introspectedTable.getContext().getModuleKeyword();
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
}

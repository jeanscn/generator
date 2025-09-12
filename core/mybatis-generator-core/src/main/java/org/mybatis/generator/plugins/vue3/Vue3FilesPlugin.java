package org.mybatis.generator.plugins.vue3;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.core.constant.enums.db.JavaTypeMapEnum;
import com.vgosoft.tool.core.VMD5Util;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.freemaker.vue3.GeneratedVueFile;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.VoViewGeneratorConfiguration;
import org.mybatis.generator.custom.ConstantsUtil;
import org.mybatis.generator.custom.FieldItem;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.vgosoft.tool.core.VStringUtil.stringHasValue;
import static org.mybatis.generator.internal.util.Mb3GenUtil.isContainOverridableMarker;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 15:44
 * @version 3.0
 */
public class Vue3FilesPlugin extends PluginAdapter {

    @Override
    public List<GeneratedFile> contextGenerateAdditionalWebFiles(AbstractIntrospectedTable abstractIntrospectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        List<GeneratedFile> answer = new ArrayList<>();
        if (!this.isGenerateVueFile(abstractIntrospectedTable)) {
            return answer;
        }

        Map<String, Object> freeMakerContext = getContextProps(abstractIntrospectedTable, htmlGeneratorConfiguration);
        String modulesPath = getVueEndProjectBasePath(abstractIntrospectedTable);
        String modelPath = abstractIntrospectedTable.getTableConfiguration().getDomainObjectName().toLowerCase();

        boolean generateEditHtml = abstractIntrospectedTable.getRules().isGenerateEditHtml();
        if (generateEditHtml && abstractIntrospectedTable.getRules().isGenerateViewVo()) {
            // 生成vue的主列表文件
            String viewPath = String.join(File.separator, (modulesPath + "/views").split("/"));
            String project = this.properties.getProperty("targetProject", viewPath);
            String fileName = abstractIntrospectedTable.getTableConfiguration().getDomainObjectName();
            if (stringHasValue(fileName)) {
                String vueViewFileNameDev = fileName + ".vue";
                GeneratedVueFile generatedVueFile = new GeneratedVueFile(
                        vueViewFileNameDev,
                        project,
                        "",
                        abstractIntrospectedTable,
                        "vue_module_list.vue.ftl", freeMakerContext);

                generatedVueFile.setOverWriteFile(htmlGeneratorConfiguration.isOverWriteVueView() || isContainOverridableMarker(generatedVueFile));
                answer.add(generatedVueFile);
            }

            //生成PrivateTableColumnSlots组件
            String path = String.join("/", modulesPath, modelPath);
            String slotsPath = String.join(File.separator, path.split("/"));
            String projectSlots = this.properties.getProperty("targetProject", slotsPath);
            String fileNamePrivateTableColumnSlots = "PrivateTableColumnSlots";

            String vuePrivateTableColumnSlotsFileName = fileNamePrivateTableColumnSlots + ".vue";
            GeneratedVueFile generatedVuePrivateTableColumnSlotsFile = new GeneratedVueFile(
                    vuePrivateTableColumnSlotsFileName,
                    projectSlots,
                    "",
                    abstractIntrospectedTable,
                    "vue_module_private_column_slots.vue.ftl", freeMakerContext);
            generatedVuePrivateTableColumnSlotsFile.setOverWriteFile(isContainOverridableMarker(generatedVuePrivateTableColumnSlotsFile));
            answer.add(generatedVuePrivateTableColumnSlotsFile);
        }

        //生成vue的type文件
        if (generateEditHtml || abstractIntrospectedTable.getRules().isGenerateAnyVo()) {
            List<FieldItem> fields = abstractIntrospectedTable.getVoModelFields().stream().distinct().collect(Collectors.toList());
            if (!fields.isEmpty()) {
                String typePath = String.join(File.separator, (modulesPath + "/" + modelPath + "/types").split("/"));
                String projectType = this.properties.getProperty("targetProject", typePath);
                String fileNameType = "T" + abstractIntrospectedTable.getTableConfiguration().getDomainObjectName();
                List<FieldItem> fieldsList = new ArrayList<>();
                List<String> noRequiredFields = Arrays.asList("id", "version", "deleteFlag");
                for (FieldItem field : fields) {
                    FieldItem fieldItem = new FieldItem(field.getName(), field.getType());
                    fieldItem.setRemarks(field.getRemarks());
                    if (noRequiredFields.contains(fieldItem.getName())) {
                        fieldItem.setRequired(false);
                    } else {
                        fieldItem.setRequired(field.isRequired());
                    }
                    // 处理带泛型的类型field.getType()，如：List<String>转为String[]
                    if (fieldItem.getType().contains("<") && fieldItem.getType().contains(">")) {
                        String type = fieldItem.getType();
                        int startIndex = type.indexOf("<");
                        int endIndex = type.lastIndexOf(">");
                        String genericType = type.substring(startIndex + 1, endIndex);
                        if (type.startsWith("List")) {
                            String tsType = JavaTypeMapEnum.ofJava(genericType).tsType();
                            fieldItem.setType((stringHasValue(tsType) ? tsType : "any") + "[]");
                        } else {
                            fieldItem.setType("any");
                        }
                    } else {
                        String tsType = JavaTypeMapEnum.ofJava(fieldItem.getType()).tsType();
                        fieldItem.setType(stringHasValue(tsType) ? tsType : "any");
                    }
                    fieldsList.add(fieldItem);
                }
                freeMakerContext.put("fields", fieldsList);
                freeMakerContext.put("typeName", fileNameType);
                if (stringHasValue(fileNameType)) {
                    String vueTypeFileName = fileNameType + ".ts";
                    GeneratedVueFile generatedVueTypeFile = new GeneratedVueFile(
                            vueTypeFileName,
                            projectType,
                            "",
                            abstractIntrospectedTable,
                            "vue_module_type.ts.ftl", freeMakerContext);
                    generatedVueTypeFile.setOverWriteFile(true);
                    answer.add(generatedVueTypeFile);
                }
            }
        }

        if (generateEditHtml) {
            //生成modal文件
            String modalPath = String.join(File.separator, (modulesPath + "/modals").split("/"));
            String projectModal = this.properties.getProperty("targetProject", modalPath);
            String fileNameModal = abstractIntrospectedTable.getTableConfiguration().getDomainObjectName() + "Modal";
            String vueModalFileName = fileNameModal + ".vue";
            GeneratedVueFile generatedVueModalFile = new GeneratedVueFile(
                    vueModalFileName,
                    projectModal,
                    "",
                    abstractIntrospectedTable,
                    "vue_module_modal.vue.ftl", freeMakerContext);
            generatedVueModalFile.setOverWriteFile(true);
            answer.add(generatedVueModalFile);

            //生成SharedVariables共享变量定义文件
            String variablesPath = String.join(File.separator, (modulesPath + "/" + modelPath).split("/"));
            String projectVariables = this.properties.getProperty("targetProject", variablesPath);
            String fileNameSharedVariables = "sharedVariables";

            GeneratedVueFile generatedSharedVariablesFile = new GeneratedVueFile(
                    fileNameSharedVariables + ".ts",
                    projectVariables,
                    "",
                    abstractIntrospectedTable,
                    "vue_module_shared_variables.ts.ftl", freeMakerContext);
            generatedSharedVariablesFile.setOverWriteFile(true);
            answer.add(generatedSharedVariablesFile);
        }

        //生成edit组件
        if (generateEditHtml) {
            String editPath = String.join(File.separator, (modulesPath + "/" + modelPath + "/components").split("/"));
            String projectEdit = this.properties.getProperty("targetProject", editPath);
            String fileNameEdit = abstractIntrospectedTable.getTableConfiguration().getDomainObjectName() + "Edit";
            String vueEditFileName = fileNameEdit + ".vue";
            GeneratedVueFile generatedVueEditFile = new GeneratedVueFile(
                    vueEditFileName,
                    projectEdit,
                    "",
                    abstractIntrospectedTable,
                    "vue_module_edit.vue.ftl", freeMakerContext);
            generatedVueEditFile.setOverWriteFile(htmlGeneratorConfiguration.isOverWriteVueEdit() || isContainOverridableMarker(generatedVueEditFile));
            answer.add(generatedVueEditFile);
        }

        //生成detail组件
        if (generateEditHtml) {
            String editPath = String.join(File.separator, (modulesPath + "/" + modelPath + "/components").split("/"));
            String projectEdit = this.properties.getProperty("targetProject", editPath);
            String fileNameDetail = abstractIntrospectedTable.getTableConfiguration().getDomainObjectName() + "Detail";
            String vueDetailFileName = fileNameDetail + ".vue";
            GeneratedVueFile generatedVueDetailFile = new GeneratedVueFile(
                    vueDetailFileName,
                    projectEdit,
                    "",
                    abstractIntrospectedTable,
                    "vue_module_detail.vue.ftl", freeMakerContext);
            generatedVueDetailFile.setOverWriteFile(htmlGeneratorConfiguration.isOverWriteVueDetail() || isContainOverridableMarker(generatedVueDetailFile));
            answer.add(generatedVueDetailFile);
        }

        //生成PrivateUseFormHooks钩子函数定义文件
        if (generateEditHtml) {
            String hooksPath = String.join(File.separator, (modulesPath + "/" + modelPath).split("/"));
            String projectHooks = this.properties.getProperty("targetProject", hooksPath);
            String fileNamePrivateUseFormHooks = "PrivateUseFormHooks";

            GeneratedVueFile generatedVuePrivateUseFormHooksFile = new GeneratedVueFile(
                    fileNamePrivateUseFormHooks + ".ts",
                    projectHooks,
                    "",
                    abstractIntrospectedTable,
                    "vue_module_private_form_hooks.ts.ftl", freeMakerContext);
            generatedVuePrivateUseFormHooksFile.setOverWriteFile(htmlGeneratorConfiguration.isOverWriteJsFile() || isContainOverridableMarker(generatedVuePrivateUseFormHooksFile));
            answer.add(generatedVuePrivateUseFormHooksFile);
        }
        return answer;
    }

    private Map<String, Object> getContextProps(AbstractIntrospectedTable abstractIntrospectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        Map<String, Object> freeMakerContext = new HashMap<>();
        // 是否工作流业务模块
        boolean workflowInstance = GenerateUtils.isWorkflowInstance(abstractIntrospectedTable);
        freeMakerContext.put("workflowEnabled", workflowInstance);
        // Vue组件名称
        String objectName = abstractIntrospectedTable.getTableConfiguration().getDomainObjectName();
        freeMakerContext.put("componentName", objectName);
        freeMakerContext.put("modelName", objectName);
        String modelPath = objectName.toLowerCase();
        freeMakerContext.put("modelPath", modelPath);
        freeMakerContext.put("restBasePath", Mb3GenUtil.getControllerBaseMappingPath(abstractIntrospectedTable));
        freeMakerContext.put("tableName", abstractIntrospectedTable.getTableConfiguration().getTableName());
        // 表注释
        String tableRemark = abstractIntrospectedTable.getRemarks(true);
        freeMakerContext.put("tableRemark", tableRemark);
        // 权限关键字
        freeMakerContext.put("permissionKey", VMD5Util.MD5_15(abstractIntrospectedTable.getControllerBeanName().toLowerCase()));
        freeMakerContext.put("isHideAction", abstractIntrospectedTable.getRules().isGenerateHideListBin());
        // 列渲染
        Map<String, String> columnRenderFunMap = Mb3GenUtil.getColumnRenderFunMap(abstractIntrospectedTable);
        freeMakerContext.put("columnRenderFunMap", columnRenderFunMap);
        if (abstractIntrospectedTable.getRules().isGenerateViewVo()) {
            addVoViewProps(freeMakerContext, abstractIntrospectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration(), abstractIntrospectedTable);
        }
        freeMakerContext.put("moduleId", Mb3GenUtil.getModelId(abstractIntrospectedTable));
        // innerList
        freeMakerContext.put("hasInnerList", htmlGeneratorConfiguration != null && !htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration().isEmpty());

        freeMakerContext.put("OVERRIDABLE_FLAG", ConstantsUtil.OVERRIDABLE_FLAG);

        return freeMakerContext;
    }

    private void addVoViewProps(Map<String, Object> freeMakerContext, VoViewGeneratorConfiguration viewConfiguration, AbstractIntrospectedTable abstractIntrospectedTable) {
        if (viewConfiguration == null) {
            return;
        }
        freeMakerContext.put("modelType", viewConfiguration.getTableType());
        if (stringHasValue(viewConfiguration.getDefaultSort())) {
            String defaultSort = viewConfiguration.getDefaultSort();
            String[] split = defaultSort.split(",");
            String[] sort = split[0].toLowerCase().split(" ");
            abstractIntrospectedTable.getColumn(sort[0]).ifPresent(column -> {
                freeMakerContext.put("defaultSortColumn", column.getActualColumnName());
                freeMakerContext.put("defaultSortProp", column.getJavaProperty());
            });
            if (sort.length > 1) {
                if ("asc".equals(sort[1])) {
                    freeMakerContext.put("defaultSortOrder", "ascending");
                } else if ("desc".equals(sort[1])) {
                    freeMakerContext.put("defaultSortOrder", "descending");
                } else {
                    freeMakerContext.put("defaultSortOrder", "ascending");
                }
            } else {
                freeMakerContext.put("defaultSortOrder", "ascending");
            }
        } else if (abstractIntrospectedTable.getColumn("created_").isPresent()) {
            freeMakerContext.put("defaultSortColumn", "created_");
            freeMakerContext.put("defaultSortProp", "created");
            freeMakerContext.put("defaultSortOrder", "descending");
        }
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable abstractIntrospectedTable) {
        if (this.isGenerateVueFile(abstractIntrospectedTable)) {
            FieldItem fieldItem = new FieldItem(field.getName(), field.getType().getShortName());
            if (introspectedColumn != null) {
                fieldItem.setRemarks(introspectedColumn.getRemarks(false));
                if (introspectedColumn.isRequired()) {
                    fieldItem.setRequired(true);
                }
            } else {
                fieldItem.setRemarks(field.getRemark());
            }
            abstractIntrospectedTable.getVoModelFields().add(fieldItem);
        }
        return true;
    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable abstractIntrospectedTable) {
        if (this.isGenerateVueFile(abstractIntrospectedTable)) {
            FieldItem fieldItem = new FieldItem(field.getName(), field.getType().getShortName());
            if (introspectedColumn != null) {
                fieldItem.setRemarks(introspectedColumn.getRemarks(false));
                if (introspectedColumn.isRequired()) {
                    fieldItem.setRequired(true);
                }
            } else {
                fieldItem.setRemarks(field.getRemark());
            }
            abstractIntrospectedTable.getVoModelFields().add(fieldItem);
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

    private boolean isGenerateVueFile(AbstractIntrospectedTable abstractIntrospectedTable) {
        return stringHasValue(abstractIntrospectedTable.getContext().getVueEndProjectPath());
    }

    private String getVueEndProjectBasePath(AbstractIntrospectedTable abstractIntrospectedTable) {
        String vueEndProjectPath = abstractIntrospectedTable.getContext().getVueEndProjectPath();
        String replace = vueEndProjectPath.replace("\\", "/").replace("//", "/");
        return replace + "/src/modules/" + abstractIntrospectedTable.getContext().getModuleKeyword();
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
}

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
import org.mybatis.generator.config.TypeScriptTypeDescriptor;
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
public class Vue3OtherFilesPlugin extends PluginAdapter {

    @Override
    public List<GeneratedFile> contextGenerateVue3OtherFiles(AbstractIntrospectedTable introspectedTable) {
        List<GeneratedFile> answer = new ArrayList<>();
        if (!this.isGenerateVueFile(introspectedTable)) {
            return answer;
        }

        Map<String, Object> freeMakerContext = getContextProps(introspectedTable);
        String modulesPath = getVueEndProjectBasePath(introspectedTable);
        String modelPath = introspectedTable.getTableConfiguration().getDomainObjectName().toLowerCase();

        boolean generateEditHtml = introspectedTable.getRules().isGenerateEditHtml();
        if (generateEditHtml && introspectedTable.getRules().isGenerateViewVo()) {
            // 生成vue的主列表文件
            String viewPath = String.join(File.separator, (modulesPath + "/views").split("/"));
            String project = this.properties.getProperty("targetProject", viewPath);
            String fileName = introspectedTable.getTableConfiguration().getDomainObjectName();
            if (stringHasValue(fileName)) {
                String vueViewFileNameDev = fileName + ".vue";
                GeneratedVueFile generatedVueFile = new GeneratedVueFile(
                        vueViewFileNameDev,
                        project,
                        "",
                        introspectedTable,
                        "vue_module_list.vue.ftl", freeMakerContext);

                generatedVueFile.setOverWriteFile( isContainOverridableMarker(generatedVueFile));
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
                    introspectedTable,
                    "vue_module_private_column_slots.vue.ftl", freeMakerContext);
            generatedVuePrivateTableColumnSlotsFile.setOverWriteFile(isContainOverridableMarker(generatedVuePrivateTableColumnSlotsFile));
            answer.add(generatedVuePrivateTableColumnSlotsFile);
        }

        //生成vue的type文件
        if (generateEditHtml || introspectedTable.getRules().isGenerateAnyVo()) {
            List<FieldItem> fields = introspectedTable.getVoModelFields().stream().distinct().collect(Collectors.toList());
            if (!fields.isEmpty()) {
                String typePath = String.join(File.separator, (modulesPath + "/" + modelPath + "/types").split("/"));
                String projectType = this.properties.getProperty("targetProject", typePath);
                String fileNameType = "T" + introspectedTable.getTableConfiguration().getDomainObjectName();

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
                    String tyeArguments = JavaTypeMapEnum.ofJavaGeneric(field.getType());
                    fieldItem.setType(tyeArguments);
                    fieldsList.add(fieldItem);
                }
                freeMakerContext.put("fields", fieldsList);
                freeMakerContext.put("typeName", fileNameType);

                for (TypeScriptTypeDescriptor typeScriptTypeDescriptor : introspectedTable.getTypeScriptTypeDescriptors()) {
                    if (typeScriptTypeDescriptor.getProperties().isEmpty()) {
                        continue;
                    }
                    for (FieldItem field : typeScriptTypeDescriptor.getProperties()) {
                        String tyeArguments = JavaTypeMapEnum.ofJavaGeneric(field.getType());
                        field.setType(tyeArguments);
                    }
                }
                freeMakerContext.put("tsTypeDescriptors", introspectedTable.getTypeScriptTypeDescriptors());

                if (stringHasValue(fileNameType)) {
                    String vueTypeFileName = fileNameType + ".ts";
                    GeneratedVueFile generatedVueTypeFile = new GeneratedVueFile(
                            vueTypeFileName,
                            projectType,
                            "",
                            introspectedTable,
                            "vue_module_type.ts.ftl", freeMakerContext);
                    generatedVueTypeFile.setOverWriteFile(true);
                    answer.add(generatedVueTypeFile);
                }
            }
        }

        String hooksPath = String.join(File.separator, (modulesPath + "/" + modelPath).split("/"));
        if (generateEditHtml) {
            //生成modal文件
            String modalPath = String.join(File.separator, (modulesPath + "/modals").split("/"));
            String projectModal = this.properties.getProperty("targetProject", modalPath);
            String fileNameModal = introspectedTable.getTableConfiguration().getDomainObjectName() + "Modal";
            String vueModalFileName = fileNameModal + ".vue";
            GeneratedVueFile generatedVueModalFile = new GeneratedVueFile(
                    vueModalFileName,
                    projectModal,
                    "",
                    introspectedTable,
                    "vue_module_modal.vue.ftl", freeMakerContext);
            generatedVueModalFile.setOverWriteFile(true);
            answer.add(generatedVueModalFile);

            //生成SharedVariables共享变量定义文件
            String projectVariables = this.properties.getProperty("targetProject", hooksPath);
            String fileNameSharedVariables = "sharedVariables";

            GeneratedVueFile generatedSharedVariablesFile = new GeneratedVueFile(
                    fileNameSharedVariables + ".ts",
                    projectVariables,
                    "",
                    introspectedTable,
                    "vue_module_shared_variables.ts.ftl", freeMakerContext);
            generatedSharedVariablesFile.setOverWriteFile(true);
            answer.add(generatedSharedVariablesFile);
        }

        //生成PrivateUseFormHooks钩子函数定义文件
        if (generateEditHtml) {
            String projectHooks = this.properties.getProperty("targetProject", hooksPath);
            String fileNamePrivateUseFormHooks = "PrivateUseFormHooks";

            GeneratedVueFile generatedVuePrivateUseFormHooksFile = new GeneratedVueFile(
                    fileNamePrivateUseFormHooks + ".ts",
                    projectHooks,
                    "",
                    introspectedTable,
                    "vue_module_private_form_hooks.ts.ftl", freeMakerContext);
            generatedVuePrivateUseFormHooksFile.setOverWriteFile(isContainOverridableMarker(generatedVuePrivateUseFormHooksFile));
            answer.add(generatedVuePrivateUseFormHooksFile);
        }
        return answer;
    }

    private Map<String, Object> getContextProps(AbstractIntrospectedTable introspectedTable) {
        Map<String, Object> freeMakerContext = new HashMap<>();
        // 是否工作流业务模块
        boolean workflowInstance = GenerateUtils.isWorkflowInstance(introspectedTable);
        freeMakerContext.put("workflowEnabled", workflowInstance);
        // Vue组件名称
        String objectName = introspectedTable.getTableConfiguration().getDomainObjectName();
        freeMakerContext.put("componentName", objectName);
        freeMakerContext.put("modelName", objectName);
        String modelPath = objectName.toLowerCase();
        freeMakerContext.put("modelPath", modelPath);
        freeMakerContext.put("restBasePath", Mb3GenUtil.getControllerBaseMappingPath(introspectedTable));
        freeMakerContext.put("tableName", introspectedTable.getTableConfiguration().getTableName());
        // 表注释
        String tableRemark = introspectedTable.getRemarks(true);
        freeMakerContext.put("tableRemark", tableRemark);
        // 权限关键字
        freeMakerContext.put("permissionKey", VMD5Util.MD5_15(introspectedTable.getControllerBeanName().toLowerCase()));
        freeMakerContext.put("isHideAction", introspectedTable.getRules().isGenerateHideListBin());
        // 列渲染
        Map<String, String> columnRenderFunMap = Mb3GenUtil.getColumnRenderFunMap(introspectedTable);
        freeMakerContext.put("columnRenderFunMap", columnRenderFunMap);
        if (introspectedTable.getRules().isGenerateViewVo()) {
            addVoViewProps(freeMakerContext, introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration(), introspectedTable);
        }
        freeMakerContext.put("moduleId", Mb3GenUtil.getModelId(introspectedTable));

        freeMakerContext.put("OVERRIDABLE_FLAG", ConstantsUtil.OVERRIDABLE_FLAG);

        return freeMakerContext;
    }

    private void addVoViewProps(Map<String, Object> freeMakerContext, VoViewGeneratorConfiguration viewConfiguration, AbstractIntrospectedTable introspectedTable) {
        if (viewConfiguration == null) {
            return;
        }
        freeMakerContext.put("modelType", viewConfiguration.getTableType());
        if (stringHasValue(viewConfiguration.getDefaultSort())) {
            String defaultSort = viewConfiguration.getDefaultSort();
            String[] split = defaultSort.split(",");
            String[] sort = split[0].toLowerCase().split(" ");
            introspectedTable.getColumn(sort[0]).ifPresent(column -> {
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
        } else if (introspectedTable.getColumn("created_").isPresent()) {
            freeMakerContext.put("defaultSortColumn", "created_");
            freeMakerContext.put("defaultSortProp", "created");
            freeMakerContext.put("defaultSortOrder", "descending");
        }
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        if (this.isGenerateVueFile(introspectedTable)) {
            FieldItem fieldItem = new FieldItem(field.getName(), field.getType().getShortName());
            if (introspectedColumn != null) {
                fieldItem.setRemarks(introspectedColumn.getRemarks(false));
                if (introspectedColumn.isRequired()) {
                    fieldItem.setRequired(true);
                }
            } else {
                fieldItem.setRemarks(field.getRemark());
            }
            introspectedTable.getVoModelFields().add(fieldItem);
        }
        return true;
    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        if (this.isGenerateVueFile(introspectedTable)) {
            FieldItem fieldItem = new FieldItem(field.getName(), field.getType().getShortName());
            if (introspectedColumn != null) {
                fieldItem.setRemarks(introspectedColumn.getRemarks(false));
                if (introspectedColumn.isRequired()) {
                    fieldItem.setRequired(true);
                }
            } else {
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

    private boolean isGenerateVueFile(AbstractIntrospectedTable introspectedTable) {
        return stringHasValue(introspectedTable.getContext().getVueEndProjectPath());
    }

    private String getVueEndProjectBasePath(AbstractIntrospectedTable introspectedTable) {
        String vueEndProjectPath = introspectedTable.getContext().getVueEndProjectPath();
        String replace = vueEndProjectPath.replace("\\", "/").replace("//", "/");
        return replace + "/src/modules/" + introspectedTable.getContext().getModuleKeyword();
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
}

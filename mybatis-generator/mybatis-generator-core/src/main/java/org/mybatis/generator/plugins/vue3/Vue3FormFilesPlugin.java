package org.mybatis.generator.plugins.vue3;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.core.constant.enums.db.JavaTypeMapEnum;
import com.vgosoft.core.constant.enums.view.HtmlDocumentTypeEnum;
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
public class Vue3FormFilesPlugin extends PluginAdapter {

    @Override
    public List<GeneratedFile> contextGenerateAdditionalWebFiles(AbstractIntrospectedTable introspectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        List<GeneratedFile> answer = new ArrayList<>();
        if (!this.isGenerateVueFile(introspectedTable)) {
            return answer;
        }

        Map<String, Object> freeMakerContext = getContextProps(introspectedTable, htmlGeneratorConfiguration);
        String modulesPath = getVueEndProjectBasePath(introspectedTable);
        String modelPath = introspectedTable.getTableConfiguration().getDomainObjectName().toLowerCase();

        boolean generateEditHtml = introspectedTable.getRules().isGenerateEditHtml();
        //生成edit组件
        String vuePath = String.join(File.separator, (modulesPath + "/" + modelPath + "/components").split("/"));
        if (generateEditHtml) {
            String projectEdit = this.properties.getProperty("targetProject", vuePath);
            String fileNameEdit = introspectedTable.getTableConfiguration().getDomainObjectName() + "Edit";
            String vueEditFileName = fileNameEdit + ".vue";
            GeneratedVueFile generatedVueEditFile = new GeneratedVueFile(
                    vueEditFileName,
                    projectEdit,
                    "",
                    introspectedTable,
                    "vue_module_edit.vue.ftl", freeMakerContext);
            generatedVueEditFile.setOverWriteFile(isContainOverridableMarker(generatedVueEditFile));
            answer.add(generatedVueEditFile);
        }

        //生成detail组件
        boolean generateViewHtml = introspectedTable.getRules().isGenerateViewOnlyHtml();
        if (generateEditHtml || generateViewHtml) {
            String projectEdit = this.properties.getProperty("targetProject", vuePath);
            String fileNameDetail = introspectedTable.getTableConfiguration().getDomainObjectName() + "Detail";
            String vueDetailFileName = fileNameDetail + ".vue";
            GeneratedVueFile generatedVueDetailFile = new GeneratedVueFile(
                    vueDetailFileName,
                    projectEdit,
                    "",
                    introspectedTable,
                    "vue_module_detail.vue.ftl", freeMakerContext);
            generatedVueDetailFile.setOverWriteFile(isContainOverridableMarker(generatedVueDetailFile));
            answer.add(generatedVueDetailFile);
        }
        return answer;
    }

    private Map<String, Object> getContextProps(AbstractIntrospectedTable introspectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
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
        // innerList
        freeMakerContext.put("hasInnerList", htmlGeneratorConfiguration != null && !htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration().isEmpty());

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

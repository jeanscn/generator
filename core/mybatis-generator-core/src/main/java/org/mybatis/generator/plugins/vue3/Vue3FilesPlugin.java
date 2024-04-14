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
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.custom.FieldItem;

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
            // 生成vue的路由组件
            String viewPath = String.join(File.separator, (modulesPath + "/views").split("/"));
            String project = this.properties.getProperty("targetProject", viewPath);
            String fileName = introspectedTable.getTableConfiguration().getDomainObjectName();
            Map<String, Object> freeMakerContext = new HashMap<>();
            freeMakerContext.put("componentName", introspectedTable.getTableConfiguration().getDomainObjectName());
            freeMakerContext.put("tableName", introspectedTable.getTableConfiguration().getTableName());
            if (VStringUtil.stringHasValue(fileName)) {
                String vueViewFileNameDev = fileName + ".vue";
                GeneratedVueFile generatedVueFile = new GeneratedVueFile(
                        vueViewFileNameDev,
                        project,
                        "",
                        introspectedTable,
                        "vue_module_view.vue.ftl", freeMakerContext);
                generatedVueFile.setOverWriteFile(htmlGeneratorConfiguration.isOverWriteVueFile());
                answer.add(generatedVueFile);
            }

            List<FieldItem> fieldsList = introspectedTable.getVoModelFields();
            if (!fieldsList.isEmpty()) {
                String typePath = String.join(File.separator, (modulesPath + "/types").split("/"));
                String projectType = this.properties.getProperty("targetProject", typePath);
                String fileNameType = "I" + introspectedTable.getTableConfiguration().getDomainObjectName();
                Map<String, Object> map = new HashMap<>();
                fieldsList.forEach(fieldItem -> {
                    String type = fieldItem.getType();
                    String tsType = TypeConverterTsUtil.convertToTsType(type);
                    fieldItem.setType(tsType);
                });
                map.put("fields", fieldsList);
                map.put("typeName", fileNameType);
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

        }
        return answer;
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (this.isGenerateVueFile(introspectedTable)) {
            introspectedTable.getVoModelFields().add(new FieldItem(field.getName(), field.getType().getShortName(),isOptionalTypeField(introspectedColumn)));
        }
        return true;
    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (this.isGenerateVueFile(introspectedTable)) {
            introspectedTable.getVoModelFields().add(new FieldItem(field.getName(), field.getType().getShortName(),isOptionalTypeField(introspectedColumn)));
        }
        return true;
    }

    private boolean isOptionalTypeField(IntrospectedColumn introspectedColumn) {
        if (introspectedColumn==null || introspectedColumn.isNullable()) {
            return true;
        }else{
           return EntityAbstractParentEnum.ABSTRACT_WORKFLOW_BUSINESS_NUMBERABLE.fields().stream().anyMatch(f->f.equals(introspectedColumn.getJavaProperty()));
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

package org.mybatis.generator.plugins.html;

import com.vgosoft.core.constant.enums.view.HtmlDocumentTypeEnum;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.codegen.mybatis3.freemaker.html.layui.GeneratedHtmlFragmentsFile;
import org.mybatis.generator.codegen.mybatis3.freemaker.html.layui.InnerListEditTemplate;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.config.HtmlElementInnerListConfiguration;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.InnerListViewConfiguration;
import org.mybatis.generator.custom.enums.UiFrameTypeEnum;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 15:44
 * @version 3.0
 */
public class HtmlFragmentsPlugin extends PluginAdapter {

    @Override
    public List<GeneratedFile> contextGenerateAdditionalFiles(AbstractIntrospectedTable introspectedTable) {
        List<GeneratedFile> answer = new ArrayList<>();
        String project = this.properties.getProperty("targetProject", "src/main/resources/templates");
        List<HtmlGeneratorConfiguration> htmlGeneratorConfigurations = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations()
                .stream()
                .filter(HtmlGeneratorConfiguration::isGenerate)
                .filter(config -> config.getType() != HtmlDocumentTypeEnum.PRINT)
                .filter(config -> config.getLayoutDescriptor() != null
                        && UiFrameTypeEnum.LAYUI.getCode().equalsIgnoreCase(config.getLayoutDescriptor().getUiFrameType()))
                .filter(config -> !config.getHtmlElementInnerListConfiguration().isEmpty())
                .toList();
        if (htmlGeneratorConfigurations.isEmpty()) {
            return answer;
        }

        List<InnerListViewConfiguration> innerListViewConfigurations = getInnerListViewConfigurations(introspectedTable);
        for (HtmlGeneratorConfiguration htmlGeneratorConfiguration : htmlGeneratorConfigurations) {
            for (HtmlElementInnerListConfiguration innerListConfiguration : htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration()) {
                InnerListViewConfiguration innerListViewConfiguration = matchInnerListViewConfiguration(
                        innerListViewConfigurations,
                        innerListConfiguration.getSourceViewPath(),
                        innerListConfiguration.getListKey()
                );
                if (innerListViewConfiguration == null) {
                    innerListViewConfiguration = buildInnerListViewFallback(innerListConfiguration, htmlGeneratorConfiguration);
                } else if (innerListViewConfiguration.getInnerListEditTemplate().isEmpty()) {
                    innerListViewConfiguration.getInnerListEditTemplate().addAll(buildEditTemplatesFromDescriptors(innerListViewConfiguration.getHtmlElements()));
                }

                String innerListFragmentsFileName = Mb3GenUtil.getInnerListFragmentFileName(innerListConfiguration, introspectedTable);
                GeneratedHtmlFragmentsFile generatedHtmlFragmentsFile = new GeneratedHtmlFragmentsFile(
                        innerListFragmentsFileName,
                        project,
                        introspectedTable.getContext().getModuleKeyword() + "/fragments",
                        introspectedTable,
                        "app_html_fragments.html.ftl");
                generatedHtmlFragmentsFile.setInnerListViewConfiguration(innerListViewConfiguration);
                answer.add(generatedHtmlFragmentsFile);
            }
        }
        return answer;
    }

    private static List<InnerListViewConfiguration> getInnerListViewConfigurations(AbstractIntrospectedTable introspectedTable) {
        return Optional.ofNullable(introspectedTable.getTableConfiguration().getVoGeneratorConfiguration())
                .map(vo -> vo.getVoViewConfiguration() == null ? Collections.<InnerListViewConfiguration>emptyList() : vo.getVoViewConfiguration().getInnerListViewConfigurations())
                .orElse(Collections.emptyList());
    }

    private static InnerListViewConfiguration matchInnerListViewConfiguration(List<InnerListViewConfiguration> innerListViewConfigurations,
                                                                              String sourceViewPath,
                                                                              String listKey) {
        return innerListViewConfigurations.stream()
                .filter(cfg -> isMatch(cfg, sourceViewPath, listKey))
                .findFirst()
                .orElse(null);
    }

    private static boolean isMatch(InnerListViewConfiguration cfg, String sourceViewPath, String listKey) {
        return stringEqualsIgnoreCase(cfg.getEditExtendsForm(), sourceViewPath)
                && stringEqualsIgnoreCase(cfg.getListKey(), listKey);
    }

    private static boolean stringEqualsIgnoreCase(String value1, String value2) {
        return value1 == null ? value2 == null : value1.equalsIgnoreCase(value2);
    }

    private static InnerListViewConfiguration buildInnerListViewFallback(HtmlElementInnerListConfiguration innerListConfiguration,
                                                                         HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        InnerListViewConfiguration fallback = new InnerListViewConfiguration();
        fallback.setListKey(innerListConfiguration.getListKey());
        fallback.setEditExtendsForm(innerListConfiguration.getSourceViewPath());
        fallback.setToolbar(new ArrayList<>(innerListConfiguration.getVxeListButtons()));
        fallback.setActionColumn(new ArrayList<>(innerListConfiguration.getActionColumn()));
        if (fallback.getToolbar().isEmpty()) {
            fallback.getToolbar().add("default");
        }
        fallback.getHtmlButtons().addAll(innerListConfiguration.getHtmlButtons());
        fallback.getHtmlElements().addAll(htmlGeneratorConfiguration.getElementDescriptors());
        fallback.getInnerListEditTemplate().addAll(buildEditTemplatesFromDescriptors(htmlGeneratorConfiguration.getElementDescriptors()));
        return fallback;
    }

    private static List<InnerListEditTemplate> buildEditTemplatesFromDescriptors(List<HtmlElementDescriptor> descriptors) {
        List<InnerListEditTemplate> templates = new ArrayList<>();
        for (HtmlElementDescriptor descriptor : descriptors) {
            if (descriptor.getColumn() == null) {
                continue;
            }
            InnerListEditTemplate template = new InnerListEditTemplate();
            String javaProperty = descriptor.getColumn().getJavaProperty();
            template.setFieldName(javaProperty);
            template.setThisFieldName(javaProperty);
            template.setOtherFieldName(descriptor.getOtherFieldName());
            template.setType(descriptor.getTagType());
            template.setFieldType(descriptor.getColumn().getFullyQualifiedJavaType().getShortNameWithoutTypeArguments());
            template.setDataType(descriptor.getDataSource());
            template.setDataUrl(descriptor.getDataUrl());
            template.setTitle(descriptor.getColumn().getRemarks(true));
            template.setListKey(descriptor.getListKey());
            template.setListViewClass(descriptor.getListViewClass());
            template.setCallback(descriptor.getCallback());
            template.setTemplate("TPL-inner-" + javaProperty);
            templates.add(template);
        }
        return templates;
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
}

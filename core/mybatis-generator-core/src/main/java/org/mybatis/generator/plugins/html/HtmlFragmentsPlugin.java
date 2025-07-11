package org.mybatis.generator.plugins.html;

import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.codegen.mybatis3.freeMaker.html.layui.GeneratedHtmlFragmentsFile;
import org.mybatis.generator.config.InnerListViewConfiguration;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 15:44
 * @version 3.0
 */
public class HtmlFragmentsPlugin extends PluginAdapter {

    @Override
    public List<GeneratedFile> contextGenerateAdditionalFiles(IntrospectedTable introspectedTable) {
        List<GeneratedFile> answer = new ArrayList<>();
        //检查innerList是否存在，存在则生成html编辑器模板片段
        String project = this.properties.getProperty("targetProject", "src/main/resources/templates");
        //生成html fragments file
        if (!introspectedTable.getRules().isGenerateInnerTable()) {
            return answer;
        }
        List<InnerListViewConfiguration> innerListViewConfigurations = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations();
        if (innerListViewConfigurations != null && !innerListViewConfigurations.isEmpty()) {
            for (InnerListViewConfiguration innerListViewConfiguration : innerListViewConfigurations) {
                String innerListFragmentsFileName = Mb3GenUtil.getHtmlInnerListFragmentFileName(innerListViewConfiguration,introspectedTable);
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

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
}

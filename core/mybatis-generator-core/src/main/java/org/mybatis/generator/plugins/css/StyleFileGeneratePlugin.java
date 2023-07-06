package org.mybatis.generator.plugins.css;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.codegen.mybatis3.freeMaker.css.GeneratedStyleFile;
import org.mybatis.generator.codegen.mybatis3.freeMaker.js.layui.GeneratedJqueryFile;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.custom.ConstantsUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 15:44
 * @version 3.0
 */
public class StyleFileGeneratePlugin extends PluginAdapter {

    @Override
    public List<GeneratedFile> contextGenerateAdditionalWebFiles(IntrospectedTable introspectedTable,HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        List<GeneratedFile> answer = new ArrayList<>();

        String project = this.properties.getProperty("targetProject", "src/main/resources/static/css");

        //生成jquery file
        if (htmlGeneratorConfiguration != null
                && htmlGeneratorConfiguration.getLayoutDescriptor()!=null
                && (htmlGeneratorConfiguration.getLayoutDescriptor().getBorderWidth()!=ConstantsUtil.HTML_BORDER_WIDTH
                || !htmlGeneratorConfiguration.getLayoutDescriptor().getBorderColor().equals(ConstantsUtil.HTML_BORDER_COLOR_DEFAULT))) {

            String styleFileName = Arrays.stream(htmlGeneratorConfiguration.getViewPath().split("[/\\\\]"))
                    .reduce((first, second) -> second)
                    .orElse("");
            if (VStringUtil.stringHasValue(styleFileName)) {
                String styleFileNameDev = styleFileName + ".css";
                GeneratedStyleFile generatedStyleFile = new GeneratedStyleFile(
                        styleFileNameDev,
                        project,
                        introspectedTable.getContext().getModuleKeyword(),
                        introspectedTable,
                        "app_main_css.css.ftl");
                generatedStyleFile.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                generatedStyleFile.setOverWriteFile(htmlGeneratorConfiguration.isOverWriteCssFile());
                answer.add(generatedStyleFile);

                String min = styleFileName + ".min.css";
                GeneratedStyleFile generatedStyleFileMin = new GeneratedStyleFile(
                        min,
                        project,
                        introspectedTable.getContext().getModuleKeyword(),
                        introspectedTable,
                        "app_main_css.css.ftl");
                generatedStyleFile.setOverWriteFile(htmlGeneratorConfiguration.isOverWriteCssFile());
                generatedStyleFileMin.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                answer.add(generatedStyleFileMin);

            }
        }
        return answer;
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
}

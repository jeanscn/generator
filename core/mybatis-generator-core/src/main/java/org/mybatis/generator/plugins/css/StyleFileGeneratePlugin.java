package org.mybatis.generator.plugins.css;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
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
                styleFileName = styleFileName + ".css";
                GeneratedJqueryFile generatedJqueryFile = new GeneratedJqueryFile(
                        styleFileName,
                        project,
                        introspectedTable.getContext().getModuleKeyword(),
                        introspectedTable,
                        "app_main_css.css.ftl");
                answer.add(generatedJqueryFile);
            }
        }
        return answer;
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
}
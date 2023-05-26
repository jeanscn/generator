package org.mybatis.generator.plugins.jquery;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.codegen.mybatis3.freeMaker.js.layui.GeneratedJqueryFile;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 15:44
 * @version 3.0
 */
public class JQueryPlugin extends PluginAdapter {

    @Override
    public List<GeneratedFile> contextGenerateAdditionalWebFiles(IntrospectedTable introspectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        List<GeneratedFile> answer = new ArrayList<>();

        String project = this.properties.getProperty("targetProject", "src/main/resources/static/js");

        //生成jquery file
        if (htmlGeneratorConfiguration != null && VStringUtil.stringHasValue(htmlGeneratorConfiguration.getViewPath())) {
            String jqueryFileName = Arrays.stream(htmlGeneratorConfiguration.getViewPath().split("[/\\\\]"))
                    .reduce((first, second) -> second)
                    .orElse("");
            if (VStringUtil.stringHasValue(jqueryFileName)) {
                //生成jquery file
                jqueryFileName = jqueryFileName + ".js";
                GeneratedJqueryFile generatedJqueryFile = new GeneratedJqueryFile(
                        jqueryFileName,
                        project,
                        introspectedTable.getContext().getModuleKeyword(),
                        introspectedTable,
                        "app_main_js.js.ftl");
                generatedJqueryFile.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
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

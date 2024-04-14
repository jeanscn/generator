package org.mybatis.generator.codegen.mybatis3.freeMaker.vue3;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.codegen.mybatis3.AbstractGeneratedFile;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;

import java.util.Map;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 16:11
 * @version 3.0
 */
public class GeneratedVueFile extends AbstractGeneratedFile {
    private final IntrospectedTable introspectedTable;

    public final String templateName;

    private Map<String, Object> data;

    private HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    public GeneratedVueFile(String fileName,
                            String targetProject,
                            String targetPackage,
                            IntrospectedTable introspectedTable,
                            String templateName, Map<String,Object> data) {
        super(targetProject, targetPackage, fileName, introspectedTable);
        this.introspectedTable = introspectedTable;
        this.templateName = templateName;
        this.data = data;
    }

    @Override
    public String getFormattedContent() {
        VueViewFreemarkerGenerator freemarkerGenerator = new VueViewFreemarkerGenerator(this.targetProject, this.introspectedTable,this.htmlGeneratorConfiguration, this.data);
        return freemarkerGenerator.generate(templateName);
    }

    public HtmlGeneratorConfiguration getHtmlGeneratorConfiguration() {
        return htmlGeneratorConfiguration;
    }

    public void setHtmlGeneratorConfiguration(HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
    }
}

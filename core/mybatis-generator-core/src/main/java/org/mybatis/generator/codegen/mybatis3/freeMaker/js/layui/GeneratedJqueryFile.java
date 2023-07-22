package org.mybatis.generator.codegen.mybatis3.freeMaker.js.layui;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.codegen.mybatis3.AbstractGeneratedFile;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 16:11
 * @version 3.0
 */
public class GeneratedJqueryFile extends AbstractGeneratedFile {

    private final String templateName;

    private final IntrospectedTable introspectedTable;

    private HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    public GeneratedJqueryFile(String fileName,
                               String targetProject,
                               String targetPackage,
                               IntrospectedTable introspectedTable,
                               String templateName,
                               HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(targetProject,targetPackage,fileName,introspectedTable);
        this.templateName = templateName;
        this.introspectedTable = introspectedTable;
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
    }

    @Override
    public String getFormattedContent() {
        JQueryFreemarkerGenerator freemarkerGenerator = new JQueryFreemarkerGenerator(this.targetProject,this.introspectedTable,this.htmlGeneratorConfiguration);

        return freemarkerGenerator.generate(templateName);
    }

    public HtmlGeneratorConfiguration getHtmlGeneratorConfiguration() {
        return htmlGeneratorConfiguration;
    }

    public void setHtmlGeneratorConfiguration(HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
    }
}

package org.mybatis.generator.codegen.mybatis3.freeMaker.css;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.codegen.mybatis3.AbstractGeneratedFile;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 16:11
 * @version 3.0
 */
public class GeneratedStyleFile extends AbstractGeneratedFile {
    private final IntrospectedTable introspectedTable;

    public final String templateName;

    private HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    public GeneratedStyleFile(String fileName,
                              String targetProject,
                              String targetPackage,
                              IntrospectedTable introspectedTable,
                              String templateName) {
        super(targetProject, targetPackage, fileName, introspectedTable);
        this.introspectedTable = introspectedTable;
        this.templateName = templateName;
    }

    @Override
    public String getFormattedContent() {
        StyleFreemarkerGenerator freemarkerGenerator = new StyleFreemarkerGenerator(this.targetProject, this.introspectedTable,this.htmlGeneratorConfiguration);
        return freemarkerGenerator.generate(templateName);
    }

    public HtmlGeneratorConfiguration getHtmlGeneratorConfiguration() {
        return htmlGeneratorConfiguration;
    }

    public void setHtmlGeneratorConfiguration(HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
    }
}

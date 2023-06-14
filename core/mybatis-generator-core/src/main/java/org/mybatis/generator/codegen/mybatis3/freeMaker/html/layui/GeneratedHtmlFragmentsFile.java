package org.mybatis.generator.codegen.mybatis3.freeMaker.html.layui;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.codegen.mybatis3.AbstractGeneratedFile;
import org.mybatis.generator.config.InnerListViewConfiguration;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 16:11
 * @version 3.0
 */
public class GeneratedHtmlFragmentsFile extends AbstractGeneratedFile {

    private final String templateName;

    private final IntrospectedTable introspectedTable;

    private InnerListViewConfiguration innerListViewConfiguration;

    public GeneratedHtmlFragmentsFile(String fileName,
                                      String targetProject,
                                      String targetPackage,
                                      IntrospectedTable introspectedTable,
                                      String templateName) {
        super(targetProject,targetPackage,fileName,introspectedTable);
        this.templateName = templateName;
        this.introspectedTable = introspectedTable;
    }

    @Override
    public String getFormattedContent() {
        HtmlFreemarkerGenerator freemarkerGenerator = new HtmlFreemarkerGenerator(this.targetProject,this.introspectedTable,this.innerListViewConfiguration);
        return freemarkerGenerator.generate(templateName);
    }

    public InnerListViewConfiguration getInnerListViewConfiguration() {
        return innerListViewConfiguration;
    }

    public void setInnerListViewConfiguration(InnerListViewConfiguration innerListViewConfiguration) {
        this.innerListViewConfiguration = innerListViewConfiguration;
    }
}

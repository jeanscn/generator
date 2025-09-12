package org.mybatis.generator.codegen.mybatis3.freemaker.html.layui;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.codegen.mybatis3.AbstractGeneratedFile;
import org.mybatis.generator.config.InnerListViewConfiguration;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 16:11
 * @version 3.0
 */
@Getter
@Setter
public class GeneratedHtmlFragmentsFile extends AbstractGeneratedFile {

    private final String templateName;

    private InnerListViewConfiguration innerListViewConfiguration;

    public GeneratedHtmlFragmentsFile(String fileName,
                                      String targetProject,
                                      String targetPackage,
                                      AbstractIntrospectedTable introspectedTable,
                                      String templateName) {
        super(targetProject,targetPackage,fileName, introspectedTable);
        this.templateName = templateName;
    }

    @Override
    public String getFormattedContent() {
        HtmlFreemarkerGenerator freemarkerGenerator = new HtmlFreemarkerGenerator(this.targetProject,this.introspectedTable,this.innerListViewConfiguration);
        return freemarkerGenerator.generate(templateName);
    }
}

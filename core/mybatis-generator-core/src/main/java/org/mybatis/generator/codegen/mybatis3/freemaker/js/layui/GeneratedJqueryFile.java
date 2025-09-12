package org.mybatis.generator.codegen.mybatis3.freemaker.js.layui;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.codegen.mybatis3.AbstractGeneratedFile;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 16:11
 * @version 3.0
 */
@Setter
@Getter
public class GeneratedJqueryFile extends AbstractGeneratedFile {

    private final String templateName;

    private HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    public GeneratedJqueryFile(String fileName,
                               String targetProject,
                               String targetPackage,
                               AbstractIntrospectedTable introspectedTable,
                               String templateName,
                               HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(targetProject,targetPackage,fileName, introspectedTable);
        this.templateName = templateName;
        this.introspectedTable = introspectedTable;
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
    }

    @Override
    public String getFormattedContent() {
        JQueryFreemarkerGenerator freemarkerGenerator = new JQueryFreemarkerGenerator(this.targetProject,this.introspectedTable,this.htmlGeneratorConfiguration);

        return freemarkerGenerator.generate(templateName);
    }
}

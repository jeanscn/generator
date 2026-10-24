package org.mybatis.generator.codegen.mybatis3.freemaker.css;

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
public class GeneratedStyleFile extends AbstractGeneratedFile {
    private final AbstractIntrospectedTable introspectedTable;

    public final String templateName;

    private HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    public GeneratedStyleFile(String fileName,
                              String targetProject,
                              String targetPackage,
                              AbstractIntrospectedTable introspectedTable,
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
}

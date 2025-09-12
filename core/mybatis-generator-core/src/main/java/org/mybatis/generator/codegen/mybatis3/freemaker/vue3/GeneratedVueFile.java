package org.mybatis.generator.codegen.mybatis3.freemaker.vue3;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.codegen.mybatis3.AbstractGeneratedFile;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;

import java.util.Map;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 16:11
 * @version 3.0
 */
public class GeneratedVueFile extends AbstractGeneratedFile {

    public final String templateName;

    private final Map<String, Object> data;

    @Setter
    @Getter
    private HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    public GeneratedVueFile(String fileName,
                            String targetProject,
                            String targetPackage,
                            AbstractIntrospectedTable introspectedTable,
                            String templateName, Map<String, Object> data) {
        super(targetProject, targetPackage, fileName, introspectedTable);
        this.introspectedTable = introspectedTable;
        this.templateName = templateName;
        this.data = data;
    }

    @Override
    public String getFormattedContent() {
        VueViewFreemarkerGenerator freemarkerGenerator = new VueViewFreemarkerGenerator(this.targetProject, this.introspectedTable, this.htmlGeneratorConfiguration, this.data);
        return freemarkerGenerator.generate(templateName);
    }

}

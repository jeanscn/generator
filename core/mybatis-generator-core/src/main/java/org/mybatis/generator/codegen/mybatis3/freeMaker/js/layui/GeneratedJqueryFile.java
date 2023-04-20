package org.mybatis.generator.codegen.mybatis3.freeMaker.js.layui;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.codegen.mybatis3.AbstractGeneratedFile;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 16:11
 * @version 3.0
 */
public class GeneratedJqueryFile extends AbstractGeneratedFile {

    private final String templateName;

    private final IntrospectedTable introspectedTable;

    public GeneratedJqueryFile(String fileName,
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
        JQueryFreemarkerGenerator freemarkerGenerator = new JQueryFreemarkerGenerator(this.targetProject,this.introspectedTable);
        return freemarkerGenerator.generate(templateName);
    }

}

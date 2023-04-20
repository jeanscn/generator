package org.mybatis.generator.codegen.mybatis3.freeMaker.css;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.codegen.mybatis3.AbstractGeneratedFile;
import org.mybatis.generator.codegen.mybatis3.freeMaker.js.layui.JQueryFreemarkerGenerator;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 16:11
 * @version 3.0
 */
public class GeneratedStyleFile extends AbstractGeneratedFile {
    private final IntrospectedTable introspectedTable;

    public GeneratedStyleFile(String fileName,
                              String targetProject,
                              String targetPackage,
                              IntrospectedTable introspectedTable) {
        super(targetProject,targetPackage,fileName,introspectedTable);
        this.introspectedTable = introspectedTable;
    }

    @Override
    public String getFormattedContent() {
        StyleFreemarkerGenerator freemarkerGenerator = new StyleFreemarkerGenerator(this.targetProject,this.introspectedTable);
        return freemarkerGenerator.generate("");
    }

}

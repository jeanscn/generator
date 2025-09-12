package org.mybatis.generator.codegen.mybatis3.freemaker.css;

import java.util.List;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.codegen.mybatis3.freemaker.AbstractFreemarkerGenerator;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;

import freemarker.template.Template;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 15:04
 * @version 3.0
 */
public class StyleFreemarkerGenerator extends AbstractFreemarkerGenerator {

    private final HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    public StyleFreemarkerGenerator(String project, AbstractIntrospectedTable introspectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(project);
        this.introspectedTable = introspectedTable;
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
    }

    @Override
    public String generate(String templateName) {
        // 定义Freemarker模板参数
        freeMakerContext.put("packageName", introspectedTable.getContext().getJavaModelGeneratorConfiguration().getTargetPackage());
        // 生成模板
        Template template = getLayuiTemplate(templateName);
        return generatorFileContent(template);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        return null;
    }
}

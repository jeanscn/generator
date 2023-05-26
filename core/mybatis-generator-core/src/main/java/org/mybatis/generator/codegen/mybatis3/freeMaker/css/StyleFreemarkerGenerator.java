package org.mybatis.generator.codegen.mybatis3.freeMaker.css;

import freemarker.template.Template;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.codegen.mybatis3.freeMaker.AbstractFreemarkerGenerator;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.io.StringWriter;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 15:04
 * @version 3.0
 */
public class StyleFreemarkerGenerator extends AbstractFreemarkerGenerator {

    private final IntrospectedTable introspectedTable;

    private final HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    public StyleFreemarkerGenerator(String project, IntrospectedTable introspectedTable,HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
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

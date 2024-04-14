package org.mybatis.generator.codegen.mybatis3.freeMaker.vue3;

import freemarker.template.Template;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.codegen.mybatis3.freeMaker.AbstractFreemarkerGenerator;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 15:04
 * @version 3.0
 */
public class VueViewFreemarkerGenerator extends AbstractFreemarkerGenerator {

    private final Map<String, Object> data;

    public VueViewFreemarkerGenerator(String project, IntrospectedTable introspectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration, Map<String, Object> data) {
        super(project);
        this.introspectedTable = introspectedTable;
        this.data = data;
    }

    @Override
    public String generate(String templateName) {
        // 定义Freemarker模板参数
        freeMakerContext.putAll(data);
        // 生成模板
        Template template = getVue3Template(templateName);
        return generatorFileContent(template);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        return null;
    }
}

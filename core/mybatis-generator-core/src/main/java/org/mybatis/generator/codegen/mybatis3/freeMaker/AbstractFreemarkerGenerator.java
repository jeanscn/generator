package org.mybatis.generator.codegen.mybatis3.freeMaker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.mybatis.generator.codegen.AbstractJavaGenerator;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 15:05
 * @version 3.0
 */
public abstract class AbstractFreemarkerGenerator extends AbstractJavaGenerator {

    protected Map<String,Object> freeMakerContext;
    protected AbstractFreemarkerGenerator(String project) {
        super(project);
        this.freeMakerContext = new HashMap<>();
    }

    public abstract String generate(String templateName);

    protected Template getLayuiTemplate(String name) {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setClassForTemplateLoading(getClass(), "/templates/layui");
        try {
            return configuration.getTemplate(name, "UTF-8");
        } catch (IOException e) {
            warnings.add("获取jquery模板["+name+"]失败");
            return null;
        }
    }

    protected Template getVue3Template(String name) {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setClassForTemplateLoading(getClass(), "/templates/vue3");
        try {
            return configuration.getTemplate(name, "UTF-8");
        } catch (IOException e) {
            warnings.add("获取vue3模板["+name+"]失败");
            return null;
        }
    }

    protected String generatorFileContent(Template template) {
        StringWriter stringWriter = new StringWriter();
        if (template != null) {
            try {
                template.process(freeMakerContext, stringWriter);
                return stringWriter.toString();
            } catch (Exception e) {
                warnings.add(e.getMessage());
                return null;
            }
        }
        return null;
    }
}

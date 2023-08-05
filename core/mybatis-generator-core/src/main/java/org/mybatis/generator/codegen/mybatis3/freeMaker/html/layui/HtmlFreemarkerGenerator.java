package org.mybatis.generator.codegen.mybatis3.freeMaker.html.layui;

import com.vgosoft.tool.core.VStringUtil;
import freemarker.template.Template;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.codegen.mybatis3.freeMaker.AbstractFreemarkerGenerator;
import org.mybatis.generator.codegen.mybatis3.freeMaker.js.layui.CallBackMethod;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.InnerListViewConfiguration;
import org.mybatis.generator.custom.HtmlElementTagTypeEnum;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 15:04
 * @version 3.0
 */
public class HtmlFreemarkerGenerator extends AbstractFreemarkerGenerator {

    private final IntrospectedTable introspectedTable;

    private HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    private final InnerListViewConfiguration innerListViewConfiguration;

    public HtmlFreemarkerGenerator(String project, IntrospectedTable introspectedTable, InnerListViewConfiguration innerListViewConfiguration) {
        super(project);
        this.introspectedTable = introspectedTable;
        this.innerListViewConfiguration = innerListViewConfiguration;
    }

    @Override
    public String generate(String templateName) {
        // 定义Freemarker模板参数
        List<InnerListEditTemplate> editTemplates = innerListViewConfiguration.getInnerListEditTemplate();
        Map<String, List<InnerListEditTemplate>> map = editTemplates.stream().collect(Collectors.groupingBy(InnerListEditTemplate::getType, Collectors.toList()));
        freeMakerContext.put("date", map.containsKey("date")?map.get("date"):new ArrayList<>());
        freeMakerContext.put("select", map.containsKey("select")?map.get("select"):new ArrayList<>());
        freeMakerContext.put("dropdownlist", map.containsKey("dropdownlist")?map.get("dropdownlist"):new ArrayList<>());
        freeMakerContext.put("switch", map.containsKey("switch")?map.get("switch"):new ArrayList<>());
        freeMakerContext.put("radio", map.containsKey("radio")?map.get("radio"):new ArrayList<>());
        freeMakerContext.put("checkbox", map.containsKey("checkbox")?map.get("checkbox"):new ArrayList<>());
        freeMakerContext.put("input", map.containsKey("input")?map.get("input"):new ArrayList<>());
        Template template = getLayuiTemplate(templateName);
        return generatorFileContent(template);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        return null;
    }
}

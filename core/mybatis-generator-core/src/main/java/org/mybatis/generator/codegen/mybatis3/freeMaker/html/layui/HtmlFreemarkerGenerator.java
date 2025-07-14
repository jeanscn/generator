package org.mybatis.generator.codegen.mybatis3.freeMaker.html.layui;

import freemarker.template.Template;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.codegen.mybatis3.freeMaker.AbstractFreemarkerGenerator;
import org.mybatis.generator.config.HtmlButtonGeneratorConfiguration;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.InnerListViewConfiguration;
import org.mybatis.generator.config.VoViewGeneratorConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        freeMakerContext.put("date", map.containsKey("date") ? map.get("date") : new ArrayList<>());
        freeMakerContext.put("select", map.containsKey("select") ? map.get("select") : new ArrayList<>());
        freeMakerContext.put("dropdownlist", map.containsKey("dropdownlist") ? map.get("dropdownlist") : new ArrayList<>());
        freeMakerContext.put("switch", map.containsKey("switch") ? map.get("switch") : new ArrayList<>());
        freeMakerContext.put("radio", map.containsKey("radio") ? map.get("radio") : new ArrayList<>());
        freeMakerContext.put("checkbox", map.containsKey("checkbox") ? map.get("checkbox") : new ArrayList<>());
        freeMakerContext.put("input", map.containsKey("input") ? map.get("input") : new ArrayList<>());
        //设置顶部buttons
        freeMakerContext.put("buttons", getHtmlButtonGeneratorConfigurations());
        //设置行操作
        freeMakerContext.put("columnActions", getRowActionButtonGeneratorConfigurations());
        Template template = getLayuiTemplate(templateName);
        return generatorFileContent(template);
    }

    private List<HtmlButtonGeneratorConfiguration>  getRowActionButtonGeneratorConfigurations() {
        List<String> columnActions = innerListViewConfiguration.getActionColumn();
        List<HtmlButtonGeneratorConfiguration> buttons = new ArrayList<>();
        Map<String, HtmlButtonGeneratorConfiguration> htmlButtonsMap = getHtmlButtonGeneratorConfigurationMap();
        for (String bar : columnActions) {
            if (bar.equalsIgnoreCase("VIEW")) {
                HtmlButtonGeneratorConfiguration view = new HtmlButtonGeneratorConfiguration("view");
                view.setType("a");
                view.setTitle("查看更多");
                view.setClasses("layui-btn layui-btn-primary layui-btn-sm");
                view.setIcon("layui-icon layui-icon-more");
                view.setElIcon("el-icon-more-filled");
                buttons.add(view);
            }
            if (bar.equalsIgnoreCase("SAVE")) {
                HtmlButtonGeneratorConfiguration save = new HtmlButtonGeneratorConfiguration("save");
                save.setType("a");
                save.setTitle("保存行数据");
                save.setClasses("layui-btn layui-btn-primary layui-btn-sm");
                save.setIcon("layui-icon layui-icon-ok");
                save.setElIcon("el-icon-select");
                buttons.add(save);
            }
            if(bar.equalsIgnoreCase("REMOVE")) {
                HtmlButtonGeneratorConfiguration remove = new HtmlButtonGeneratorConfiguration("delete");
                remove.setType("a");
                remove.setTitle("删除行数据");
                remove.setClasses("llayui-btn layui-btn-primary layui-btn-sm");
                remove.setIcon("layui-icon layui-icon-close");
                remove.setElIcon("el-icon-close-bold");
                buttons.add(remove);
            }
            if (htmlButtonsMap.containsKey(bar)) {
                buttons.add(htmlButtonsMap.get(bar));
            }
        }
        return buttons;
    }

    private List<HtmlButtonGeneratorConfiguration> getHtmlButtonGeneratorConfigurations() {
        List<String> toolbar = innerListViewConfiguration.getToolbar();
        List<HtmlButtonGeneratorConfiguration> buttons = new ArrayList<>();
        Map<String, HtmlButtonGeneratorConfiguration> htmlButtonsMap = getHtmlButtonGeneratorConfigurationMap();
        for (String bar : toolbar) {
            if (bar.equalsIgnoreCase("add") || bar.equalsIgnoreCase("default")) {
                HtmlButtonGeneratorConfiguration add = new HtmlButtonGeneratorConfiguration("add");
                add.setTitle("新增");
                add.setClasses("layui-btn layui-btn-sm layui-btn-primary");
                add.setIcon("layui-icon layui-icon-addition");
                add.setElIcon("el-icon-plus");
                buttons.add(add);
            }
            if (bar.equalsIgnoreCase("delete") || bar.equalsIgnoreCase("default")) {
                HtmlButtonGeneratorConfiguration delete = new HtmlButtonGeneratorConfiguration("delete");
                delete.setTitle("删除");
                delete.setClasses("layui-btn layui-btn-sm layui-btn-primary");
                delete.setIcon("layui-icon layui-icon-delete");
                delete.setElIcon("el-icon-delete");
                buttons.add(delete);
            }
            if (htmlButtonsMap.containsKey(bar)) {
                buttons.add(htmlButtonsMap.get(bar));
            }
        }
        return buttons;
    }

    private Map<String, HtmlButtonGeneratorConfiguration> getHtmlButtonGeneratorConfigurationMap() {
        VoViewGeneratorConfiguration voViewConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
        return Stream.of(innerListViewConfiguration.getHtmlButtons().stream(),voViewConfiguration.getHtmlButtons().stream()).flatMap(s -> s)
                .collect(Collectors.toMap(HtmlButtonGeneratorConfiguration::getId, htmlButtonGeneratorConfiguration -> htmlButtonGeneratorConfiguration));
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        return null;
    }
}

package org.mybatis.generator.codegen.mybatis3.freeMaker.js.layui;

import com.vgosoft.core.constant.enums.view.TagNamesEnum;
import com.vgosoft.tool.core.VStringUtil;
import freemarker.template.Template;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.custom.HtmlElementDataSourceEnum;
import org.mybatis.generator.custom.HtmlElementTagTypeEnum;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.codegen.mybatis3.freeMaker.AbstractFreemarkerGenerator;
import org.mybatis.generator.internal.util.StringUtility;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 15:04
 * @version 3.0
 */
public class JQueryFreemarkerGenerator extends AbstractFreemarkerGenerator {

    private final IntrospectedTable introspectedTable;

    private final HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    public JQueryFreemarkerGenerator(String project, IntrospectedTable introspectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(project);
        this.introspectedTable = introspectedTable;
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
    }

    @Override
    public String generate(String templateName) {
        List<CallBackMethod> callBackMethods = new ArrayList<>();
        this.htmlGeneratorConfiguration.getElementDescriptors().forEach(elementDescriptor -> {
            if (StringUtility.stringHasValue(elementDescriptor.getCallback())) {
                CallBackMethod callBackMethod = new CallBackMethod();
                callBackMethod.setTagName(elementDescriptor.getTagType());
                callBackMethod.setColumnName(elementDescriptor.getName());
                callBackMethod.setMethodName(VStringUtil.getFirstCharacterLowercase(elementDescriptor.getCallback()));
                if (elementDescriptor.getTagType().equals(HtmlElementTagTypeEnum.SELECT.getCode())) {
                    // 如果是select标签，且存在selectByTableGeneratorConfiguration配置，生成更新关系的方法
                    Optional<SelectByTableGeneratorConfiguration> byTableGeneratorConfiguration = introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream()
                            .filter(config -> config.getMethodSuffix().equals(elementDescriptor.getCallback()))
                            .findFirst();
                    byTableGeneratorConfiguration.ifPresent(config -> {
                        callBackMethod.setType(0);
                        callBackMethod.setRequestKey(VStringUtil.toHyphenCase(config.getMethodSuffix()));
                        callBackMethod.setThisKey(config.getThisColumn().getJavaProperty());
                        callBackMethod.setOtherKey(config.getOtherColumn().getJavaProperty() + "s");
                    });
                    if (HtmlElementDataSourceEnum.INNER_TABLE.getCode().equals(elementDescriptor.getDataSource())) {
                        callBackMethod.setType(1);
                    }
                    if (!byTableGeneratorConfiguration.isPresent()) {
                        callBackMethod.setRequestKey(VStringUtil.toHyphenCase(elementDescriptor.getCallback()));
                        callBackMethod.setThisKey("param1");
                        callBackMethod.setOtherKey("param2");
                    }
                } else {
                    callBackMethod.setRequestKey(VStringUtil.toHyphenCase(elementDescriptor.getCallback()));
                    callBackMethod.setThisKey("param1");
                    callBackMethod.setOtherKey("param2");
                }
                callBackMethods.add(callBackMethod);
            }
        });
        // 定义Freemarker模板参数
        freeMakerContext.put("callBackMethods", callBackMethods);
        freeMakerContext.put("restBasePath", Mb3GenUtil.getControllerBaseMappingPath(introspectedTable));
        // 页面内列表js生成相关
        if (this.htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration() != null) {
            freeMakerContext.put("innerList", this.htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration());
        }
        Template template = getLayuiTemplate(templateName);
        return generatorFileContent(template);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        return null;
    }
}

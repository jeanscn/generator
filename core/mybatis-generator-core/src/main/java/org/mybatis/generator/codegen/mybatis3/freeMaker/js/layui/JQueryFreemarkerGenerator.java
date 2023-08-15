package org.mybatis.generator.codegen.mybatis3.freeMaker.js.layui;

import com.vgosoft.core.constant.enums.view.TagNamesEnum;
import com.vgosoft.tool.core.VStringUtil;
import freemarker.template.Template;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.custom.HtmlDocumentTypeEnum;
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
        if (!this.htmlGeneratorConfiguration.getType().equals(HtmlDocumentTypeEnum.EDITABLE)) {
            return null;
        }
        this.htmlGeneratorConfiguration.getElementDescriptors().forEach(elementDescriptor -> {
            CallBackMethod callBackMethod = new CallBackMethod(VStringUtil.getFirstCharacterLowercase(elementDescriptor.getCallback()));
            if (StringUtility.stringHasValue(elementDescriptor.getCallback())) {
                callBackMethod.setTagName(elementDescriptor.getTagType());
                callBackMethod.setColumnName(elementDescriptor.getName());
                if (elementDescriptor.getTagType().equals(HtmlElementTagTypeEnum.SELECT.getCode())) {
                    //INNER_TABLE和树形结构的select标签，返回的方法的参数不同
                    if (HtmlElementDataSourceEnum.INNER_TABLE.getCode().equals(elementDescriptor.getDataSource())) {
                        callBackMethod.getRemarks().clear();
                        callBackMethod.getRemarks().add("    /**");
                        callBackMethod.getRemarks().add("    * 数据列表弹窗选择结果后的回调方法.");
                        callBackMethod.getRemarks().add("    * @param {Object} data 回调数据");
                        callBackMethod.getRemarks().add("    *  -data.closeType = -1; layer关闭操作类型，-1。已关闭");
                        callBackMethod.getRemarks().add("    *  -data.rowObject = obj; 待操作的行对象");
                        callBackMethod.getRemarks().add("    *  -data.data = $('body', document).data('selectedItems'); 选中的数据");
                        callBackMethod.getRemarks().add("    *  -data.updateUrl = updateUrl; 更新url");
                        callBackMethod.getRemarks().add("    *  -data.rowData = rowData; 待操作的行数据,或者 data.rowObject.data;");
                        callBackMethod.getRemarks().add("    * */");
                        callBackMethod.setType(0);
                        callBackMethod.setParams("data");
                        callBackMethod.getMethodBodyLines().add("    //nothing to do");
                    }else {
                        callBackMethod.getRemarks().add("    // "+elementDescriptor.getName()+"-select回调方法.执行扩展逻辑,并且返回true以便于继续执行调用方法的默认逻辑");
                        callBackMethod.setType(1);
                        callBackMethod.setParams(" inital , ids ");
                        callBackMethod.getMethodBodyLines().add("    return true;");
                    }
                } else {
                    callBackMethod.getRemarks().add("    // "+elementDescriptor.getName()+"-select回调方法.执行扩展逻辑");
                    callBackMethod.setType(1);
                    callBackMethod.setParams(" o , n ");
                    callBackMethod.getMethodBodyLines().add("    //nothing to do");
                }
                // 如果是select标签，且存在selectByTableGeneratorConfiguration配置，生成更新关系的方法
                Optional<SelectByTableGeneratorConfiguration> byTableGeneratorConfiguration = introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream()
                        .filter(config -> config.getMethodSuffix().equals(elementDescriptor.getCallback()))
                        .findFirst();
                if (byTableGeneratorConfiguration.isPresent()) {
                    SelectByTableGeneratorConfiguration configuration = byTableGeneratorConfiguration.get();
                    callBackMethod.setParams(" o , n ");
                    callBackMethod.getMethodBodyLines().clear();
                    callBackMethod.getMethodBodyLines().add(VStringUtil.format("    return window.relationHandle(o, n, '{0}', '{1}', '{2}');",
                            VStringUtil.toHyphenCase(configuration.getMethodSuffix()), configuration.getThisColumn().getJavaProperty(), configuration.getOtherColumn().getJavaProperty() + "s"));
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

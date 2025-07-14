package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.HtmlElementInnerListConfiguration;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.custom.enums.HtmlDocumentTypeEnum;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

import static com.vgosoft.tool.core.VStringUtil.format;
import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;
import static org.mybatis.generator.custom.ConstantsUtil.V_STRING_UTIL;

public class ViewElementGenerator extends AbstractControllerElementGenerator {

    public ViewElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(V_STRING_UTIL);
        parentElement.addImportedType("java.util.Optional");
        parentElement.addImportedType("org.springframework.web.servlet.ModelAndView");
        parentElement.addImportedType(entityType);
        FullyQualifiedJavaType paraType = new FullyQualifiedJavaType("com.vgosoft.web.pojo.ControllerViewParam");
        //创建方法
        final String methodPrefix = "view";
        Method method = createMethod(methodPrefix);
        //添加参数,返回值,异常
        Parameter parameter = new Parameter(paraType, "viewParam");
        parameter.setRemark("参数，view方法参数对象。");
        method.addParameter(parameter);
        Parameter response = new Parameter(new FullyQualifiedJavaType("javax.servlet.http.HttpServletResponse"), "response");
        response.setRemark("响应对象");
        method.addParameter(response);
        parentElement.addImportedType("javax.servlet.http.HttpServletResponse");
        method.setReturnType(new FullyQualifiedJavaType("ModelAndView"));
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        //添加注解
        method.addAnnotation("@PermitAll");
        parentElement.addImportedType("javax.annotation.security.PermitAll");
        method.addAnnotation(new SystemLogDesc("通过表单查看或创建记录", introspectedTable), parentElement);
        method.addAnnotation(new RequestMappingDesc("view", RequestMethodEnum.GET), parentElement);
        method.addAnnotation(new ApiOperationDesc("获得数据并返回页面视图（可用于普通业务在列表中新建接口）",
                "根据给定id获取单个实体，id为可选参数，当id存在时查询数据，否则直接返回视图"), parentElement);
        //添加注释
        commentGenerator.addMethodJavaDocLine(method, "根据主键获取单个业务实例");
        String entityName = introspectedTable.getRules().isGenerateVoModel() ? this.entityVoType.getShortName() : entityType.getShortName();
        String entityVar = entityName.substring(0, 1).toLowerCase() + entityName.substring(1);
        //函数体
        method.addBodyLine("response.addHeader(\"X-Frame-Options\", \"SAMEORIGIN\");");
        method.addBodyLine("ModelAndView mv = new ModelAndView();");
        method.addBodyLine("String viewName = null;");
        method.addBodyLine("{0} {1} = null;", entityName, entityVar);
        method.addBodyLine("if (viewParam.getId() != null) {");
        method.addBodyLine(format("ServiceResult<{0}> serviceResult = {1}.selectByPrimaryKey(viewParam.getId());", entityType.getShortName(), serviceBeanName));
        method.addBodyLine("if (serviceResult.hasResult()) {");
        method.addBodyLine("{0} {1} = serviceResult.getResult();", entityType.getShortName(), entityType.getShortNameFirstLowCase());
        if (GenerateUtils.isWorkflowInstance(introspectedTable)) { //如果是流程实例
            //为父元素添加当前方法的使用的属性
            parentElement.addImportedType("javax.annotation.Resource");
            FullyQualifiedJavaType qualifiedJavaType = new FullyQualifiedJavaType("com.vgosoft.workflow.adapter.service.IWorkflowTraceInfo");
            qualifiedJavaType.addTypeArgument(entityType);
            Field workflowTraceInfoImpl = new Field("workflowTraceInfoImpl", qualifiedJavaType);
            workflowTraceInfoImpl.addAnnotation("@Resource");
            workflowTraceInfoImpl.setVisibility(JavaVisibility.PROTECTED);
            parentElement.addField(workflowTraceInfoImpl);
            parentElement.addImportedType("com.vgosoft.workflow.adapter.service.IWorkflowTraceInfo");
            //添加审批记录
            method.addBodyLine("if (\"true\".equals(viewParam.getWithTraceInfo())) {");
            method.addBodyLine("// 流程审批记录");
            method.addBodyLine("mv.addObject(\"traceInfo\", workflowTraceInfoImpl.getTraceInfo({0}));", entityType.getShortNameFirstLowCase());
            method.addBodyLine("}");
        }

        introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().forEach(htmlGeneratorConfiguration -> {
            if (htmlGeneratorConfiguration.isGenerate()) {
                if (htmlGeneratorConfiguration.getType().equals(HtmlDocumentTypeEnum.PRINT)) {
                    method.addBodyLine("if (\"print_\".equals(viewParam.getPrefix())) {");
                    setViewName(method, htmlGeneratorConfiguration);
                    method.addBodyLine("mv.addObject(\"viewStatus\", \"0\");");
                    method.addBodyLine("mv.addObject(\"htmlFileName\", \"" + htmlGeneratorConfiguration.getHtmlFileName() + "\");");
                    addBusCommentsToMethod(htmlGeneratorConfiguration, method, parentElement);
                    //附件
                    addAttachmentToModel(htmlGeneratorConfiguration, method, parentElement);
                    // 子表
                    addInnerListToModel(htmlGeneratorConfiguration, method, parentElement);
                    method.addBodyLine("}");
                } else if (htmlGeneratorConfiguration.getType().equals(HtmlDocumentTypeEnum.VIEWONLY)) {
                    method.addBodyLine("if (\"view_\".equals(viewParam.getPrefix())) {");
                    setViewName(method, htmlGeneratorConfiguration);
                    method.addBodyLine("mv.addObject(\"viewStatus\", \"0\");");
                    method.addBodyLine("mv.addObject(\"htmlFileName\", \"" + htmlGeneratorConfiguration.getHtmlFileName() + "\");");
                    addBusCommentsToMethod(htmlGeneratorConfiguration, method, parentElement);
                    //附件
                    addAttachmentToModel(htmlGeneratorConfiguration, method, parentElement);
                    // 子表
                    addInnerListToModel(htmlGeneratorConfiguration, method, parentElement);
                    method.addBodyLine("}");
                } else {
                    method.addBodyLine("if (!VStringUtil.stringHasValue(viewParam.getPrefix()) || \"edit_\".equals(viewParam.getPrefix())) {");
                    setViewName(method, htmlGeneratorConfiguration);
                    method.addBodyLine("mv.addObject(\"viewStatus\", Optional.ofNullable(viewParam.getViewStatus()).orElse(\"1\"));");
                    method.addBodyLine("mv.addObject(\"htmlFileName\", \"" + htmlGeneratorConfiguration.getHtmlFileName() + "\");");
                    addBusCommentsToMethod(htmlGeneratorConfiguration, method, parentElement);
                    //附件
                    addAttachmentToModel(htmlGeneratorConfiguration, method, parentElement);
                    // 子表
                    addInnerListToModel(htmlGeneratorConfiguration, method, parentElement);
                    method.addBodyLine("}");
                }
            }
        });

        if (introspectedTable.getRules().isGenerateVoModel()) {
            method.addBodyLine("{0} = mappings.to{1}Vo(serviceResult.getResult());", entityVar, entityType.getShortName());
        } else {
            method.addBodyLine("{0} = serviceResult.getResult();", entityVar);
        }
        method.addBodyLine("} else {");
        method.addBodyLine("mv.addObject(\"error\", serviceResult.getMessage());");
        method.addBodyLine("}");
        method.addBodyLine("} else {");
        method.addBodyLine("{0} = updateNewInstanceDefaultValue(new {1}());", entityVar, entityName);
        method.addBodyLine("}");
        method.addBodyLine("if ({0} != null) '{'", entityVar);
        method.addBodyLine("{0} {1} = JsonUtil.serializeObject({2});", entityName, entityType.getShortNameFirstLowCase(), entityVar);
        method.addBodyLine("if (viewParam.getRValue() != null && viewParam.getRField() != null) {");
        method.addBodyLine("VReflectionUtil.writeField({0}, viewParam.getRField(), viewParam.getRValue());", entityType.getShortNameFirstLowCase());
        method.addBodyLine("}");
        method.addBodyLine("mv.addObject(\"{0}\", {1});", this.entityNameKey, entityType.getShortNameFirstLowCase());
        method.addBodyLine("}");
        method.addBodyLine("mv.addAllObjects(getCurrentUserInfo());");
        method.addBodyLine("mv.setViewName(viewName);");
        method.addBodyLine("return mv;");

        parentElement.addImportedType("com.vgosoft.tool.core.VReflectionUtil");
        parentElement.addImportedType("com.vgosoft.tool.core.JsonUtil");
        parentElement.addImportedType("java.util.Optional");
        parentElement.addImportedType("com.vgosoft.tool.core.VStringUtil");
        parentElement.addImportedType("com.vgosoft.web.pojo.ControllerViewParam");
        parentElement.addImportedType("org.springframework.web.bind.annotation.RequestParam");
        parentElement.addImportedType(entityVoType);
        parentElement.addMethod(method);
    }

    private void addInnerListToModel(HtmlGeneratorConfiguration htmlGeneratorConfiguration, Method method, TopLevelClass parentElement) {
        // 子表
        List<HtmlElementInnerListConfiguration> listConfiguration = htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration();
        if (!listConfiguration.isEmpty()) {
            HtmlElementInnerListConfiguration innerListConfiguration = listConfiguration.get(0);
            if (innerListConfiguration != null && innerListConfiguration.getSourceListViewClass() != null) {
                method.addBodyLine("String listKey = \"{0}\";", innerListConfiguration.getListKey());
                FullyQualifiedJavaType viewType = new FullyQualifiedJavaType(innerListConfiguration.getSourceListViewClass());
                method.addBodyLine("Layuitable innerList = getInnerList(listKey, {0}.class, 0);", viewType.getShortName());
                method.addBodyLine("if (innerList != null) {");
                method.addBodyLine("List<LayuiTableHeader> tableHeaders = innerList.getCols().get(0);");
                method.addBodyLine("mv.addObject(\"innerListHeaders\", tableHeaders);");
                method.addBodyLine("Map<String,String> headerMap = getViewFieldMap({0}.class);", viewType.getShortName());
                method.addBodyLine("mv.addObject(\"innerListHeaderMap\",headerMap);");
                method.addBodyLine("}");
                parentElement.addImportedType("com.vgosoft.web.plugins.laytable.Layuitable");
                parentElement.addImportedType(innerListConfiguration.getSourceListViewClass());
                parentElement.addImportedType("java.util.Map");
                parentElement.addImportedType("com.vgosoft.web.plugins.laytable.LayuiTableHeader");
            }
        }

    }

    private void addAttachmentToModel(HtmlGeneratorConfiguration htmlGeneratorConfiguration, Method method, TopLevelClass parentElement) {
        if (!htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration().isEmpty()) {
            //为父元素添加当前方法的使用的属性
            parentElement.addImportedType("javax.annotation.Resource");
            Field vbizFileAttachment = new Field("vbizFileAttachmentImpl", new FullyQualifiedJavaType("com.vgosoft.bizcore.service.IVbizFileAttachment"));
            vbizFileAttachment.addAnnotation("@Resource");
            vbizFileAttachment.setVisibility(JavaVisibility.PROTECTED);
            parentElement.addField(vbizFileAttachment);
            parentElement.addImportedType("com.vgosoft.bizcore.service.IVbizFileAttachment");
            //添加附件
            method.addBodyLine("// 附件列表");
            method.addBodyLine("mv.addObject(\"attachments\", vbizFileAttachmentImpl.selectByColumnRecordId(viewParam.getId()));");
        }
    }

    private void addBusCommentsToMethod(HtmlGeneratorConfiguration htmlGeneratorConfiguration, Method method, TopLevelClass parentElement) {
        if (!htmlGeneratorConfiguration.getHtmlApprovalCommentConfigurations().isEmpty()) {
            //为父元素添加当前方法的使用的属性
            parentElement.addImportedType("javax.annotation.Resource");
            FullyQualifiedJavaType qualifiedJavaType = new FullyQualifiedJavaType("com.vgosoft.workflow.adapter.service.IWorkflowTraceInfo");
            qualifiedJavaType.addTypeArgument(entityType);
            Field workflowTraceInfoImpl = new Field("workflowTraceInfoImpl", qualifiedJavaType);
            workflowTraceInfoImpl.addAnnotation("@Resource");
            workflowTraceInfoImpl.setVisibility(JavaVisibility.PROTECTED);
            parentElement.addField(workflowTraceInfoImpl);
            parentElement.addImportedType("com.vgosoft.workflow.adapter.service.IWorkflowTraceInfo");
            //添加审批意见
            method.addBodyLine("// 业务审批意见");
            method.addBodyLine("mv.addObject(\"comments\", workflowTraceInfoImpl.getBusComments({0}));", entityType.getShortNameFirstLowCase());
        }
    }

    private void setViewName(Method method, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        String basePath = introspectedTable.getTableConfiguration().getHtmlBasePath() + "/";
        String viewPath = StringUtility.substringBeforeLast(htmlGeneratorConfiguration.getHtmlFileName(), ".");
        String viewName = basePath + viewPath;
        method.addBodyLine("viewName = \"{0}\";", viewName);
    }
}

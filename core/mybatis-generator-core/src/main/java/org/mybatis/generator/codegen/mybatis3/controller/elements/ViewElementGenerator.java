package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.HtmlElementInnerListConfiguration;
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
        final String methodPrefix = "view";
        StringBuilder sb = new StringBuilder();
        Method method = createMethod(methodPrefix);

        Parameter parameter = new Parameter(paraType, "viewParam");
        parameter.setRemark("参数，view方法参数对象。");
        method.addParameter(parameter);

        Parameter response = new Parameter(new FullyQualifiedJavaType("javax.servlet.http.HttpServletResponse"), "response");
        response.setRemark("响应对象");
        method.addParameter(response);
        parentElement.addImportedType("javax.servlet.http.HttpServletResponse");

        method.setReturnType(new FullyQualifiedJavaType("ModelAndView"));
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        parentElement.addImportedType("javax.annotation.security.PermitAll");
        method.addAnnotation("@PermitAll");
        parentElement.addImportedType("javax.annotation.security.PermitAll");
        method.addAnnotation(new SystemLogDesc("通过表单查看或创建记录", introspectedTable), parentElement);
        method.addAnnotation(new RequestMappingDesc("view", RequestMethodEnum.GET), parentElement);
        addSecurityPreAuthorize(method, methodPrefix, "查看");
        method.addAnnotation(new ApiOperationDesc("获得数据并返回页面视图（可用于普通业务在列表中新建接口）",
                "根据给定id获取单个实体，id为可选参数，当id存在时查询数据，否则直接返回视图"), parentElement);
        commentGenerator.addMethodJavaDocLine(method, "根据主键获取单个业务实例");
        String entityName = introspectedTable.getRules().isGenerateVoModel() ? this.entityVoType.getShortName() : entityType.getShortName();
        //函数体
        method.addBodyLine("response.addHeader(\"X-Frame-Options\", \"SAMEORIGIN\");");

        sb.append("ModelAndView mv = new ModelAndView();");
        method.addBodyLine(sb.toString());
        //method.addBodyLine("JsonUtil.init(SpringContextHolder.getBean(ObjectMapper.class));");
        method.addBodyLine("{0} object = null;", entityName);
        method.addBodyLine("if (viewParam.getId() != null) {");
        method.addBodyLine(format("ServiceResult<{0}> serviceResult = {1}.selectByPrimaryKey(viewParam.getId());", entityType.getShortName(), serviceBeanName));
        method.addBodyLine("if (serviceResult.hasResult()) {");

        if (GenerateUtils.isWorkflowInstance(introspectedTable)) {
            method.addBodyLine("if (\"true\".equals(viewParam.getWithTraceInfo())) {");
            method.addBodyLine("// 流程审批记录");
            method.addBodyLine("{0} {1} = serviceResult.getResult();", entityType.getShortName(), entityType.getShortNameFirstLowCase());
            method.addBodyLine("IWorkflowTraceInfo<{0}> workflowTraceInfo = SpringContextHolder.getBean(IWorkflowTraceInfo.class);", entityType.getShortName());
            method.addBodyLine("List<WorkflowTraceInfo> traceInfo = workflowTraceInfo.getTraceInfo({0});", entityType.getShortNameFirstLowCase());
            method.addBodyLine("mv.addObject(\"traceInfo\", traceInfo);");
            method.addBodyLine("}");
            parentElement.addImportedType("com.vgosoft.workflow.adapter.pojo.dto.WorkflowTraceInfo");
            parentElement.addImportedType("com.vgosoft.core.entity.IWorkflowBaseEntity");
            parentElement.addImportedType("com.vgosoft.workflow.adapter.service.IWorkflowTraceInfo");
        }

        if (GenerateUtils.isBusinessInstance(introspectedTable)) {
            method.addBodyLine("if (\"true\".equals(viewParam.getWithAttachments()) ) {");
            method.addBodyLine("// 附件列表");
            method.addBodyLine("IVbizFileAttachment fileAttachmentService = SpringContextHolder.getBean(IVbizFileAttachment.class);");
            method.addBodyLine("mv.addObject(\"attachments\", fileAttachmentService.selectByColumnRecordId(viewParam.getId()));");
            method.addBodyLine("}");
            parentElement.addImportedType("com.vgosoft.bizcore.service.IVbizFileAttachment");
        }
        List<HtmlElementInnerListConfiguration> listConfiguration = this.htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration();
        if (!listConfiguration.isEmpty()) {
            HtmlElementInnerListConfiguration innerListConfiguration = listConfiguration.get(0);
            if (innerListConfiguration != null && innerListConfiguration.getSourceListViewClass() != null) {
                method.addBodyLine("List<LayuiTableHeader> listHeaders = new ArrayList<>();");

                method.addBodyLine("if (!VStringUtil.stringHasValue(viewParam.getListKey())) {");
                method.addBodyLine("viewParam.setListKey(\""+innerListConfiguration.getListKey()+"\");");
                method.addBodyLine("}");

                method.addBodyLine("if (VStringUtil.stringHasValue(viewParam.getListKey())) {");
                method.addBodyLine("// 内嵌列表");
                FullyQualifiedJavaType javaType = new FullyQualifiedJavaType(innerListConfiguration.getSourceListViewClass());
                parentElement.addImportedType(javaType);
                method.addBodyLine("Layuitable innerList = getInnerList(viewParam.getListKey(), {1}.class, 0);", innerListConfiguration.getListKey(), javaType.getShortName());
                method.addBodyLine("if (innerList != null)  listHeaders = innerList.getCols().get(0);");
                method.addBodyLine("}");
                method.addBodyLine("mv.addObject(\"innerListHeaders\", listHeaders);");
                parentElement.addImportedType("com.vgosoft.web.plugins.laytable.LayuiTableHeader");
                parentElement.addImportedType("com.vgosoft.web.plugins.laytable.Layuitable");

            }
        }

        if (introspectedTable.getRules().isGenerateVoModel()) {
            method.addBodyLine("object = mappings.to{0}VO(serviceResult.getResult());", entityType.getShortName());
        } else {
            method.addBodyLine("object = serviceResult.getResult();", entityType.getShortName());
        }
        method.addBodyLine("}else{");
        method.addBodyLine("mv.addObject(\"error\", serviceResult.getMessage());");
        method.addBodyLine("}");
        method.addBodyLine("}else{");
        method.addBodyLine("object = updateNewInstanceDefaultValue(new {0}());", entityName);
        method.addBodyLine("}");
        method.addBodyLine("if (object != null) {");
        method.addBodyLine("{0} {1} = JsonUtil.serializeObject(object);", entityName, entityType.getShortNameFirstLowCase());
        method.addBodyLine("if (viewParam.getRValue()!=null && viewParam.getRField()!=null) {");
        method.addBodyLine("VReflectionUtil.writeField({0}, viewParam.getRField(), viewParam.getRValue());", entityType.getShortNameFirstLowCase());
        method.addBodyLine("}");
        method.addBodyLine("mv.addObject(\"{0}\", {1});", this.entityNameKey, entityType.getShortNameFirstLowCase());
        method.addBodyLine("}");
        method.addBodyLine("mv.addObject(\"viewStatus\", Optional.ofNullable(viewParam.getViewStatus()).orElse(\"1\"));");

        method.addBodyLine("Map<String, Object> currentUserInfo = getCurrentUserInfo();");
        parentElement.addImportedType("java.util.Map");
        method.addBodyLine("if (currentUserInfo.keySet().isEmpty()) {");
        method.addBodyLine("mv.addObject(\"currentUser\", new OrgUser());");
        method.addBodyLine("mv.addObject(\"currentDept\", new OrgDepartment());");
        parentElement.addImportedType("com.vgosoft.organization.entity.OrgUser");
        parentElement.addImportedType("com.vgosoft.organization.entity.OrgDepartment");
        method.addBodyLine("} else {");
        method.addBodyLine("mv.addAllObjects(getCurrentUserInfo());");
        method.addBodyLine("}");

        sb.setLength(0);
        sb.append("String viewName = VStringUtil.format(\"");
        sb.append(introspectedTable.getTableConfiguration().getHtmlBasePath()).append("/");
        sb.append("{0}");
        sb.append(StringUtility.substringBeforeLast(this.htmlGeneratorConfiguration.getHtmlFileName(), "."));
        sb.append("\",Optional.ofNullable(viewParam.getPrefix()).orElse(\"\"));");
        method.addBodyLine(sb.toString());
        method.addBodyLine("mv.setViewName(viewName);");
        method.addBodyLine("return mv;");

        parentElement.addImportedType("com.vgosoft.tool.core.VReflectionUtil");
        parentElement.addImportedType("com.vgosoft.core.util.JsonUtil");
        parentElement.addImportedType("com.vgosoft.core.util.SpringContextHolder");
        parentElement.addImportedType("com.fasterxml.jackson.databind.ObjectMapper");
        parentElement.addImportedType("java.util.Optional");
        parentElement.addImportedType("com.vgosoft.tool.core.VStringUtil");
        parentElement.addImportedType("com.vgosoft.web.pojo.ControllerViewParam");
        parentElement.addImportedType("org.springframework.web.bind.annotation.RequestParam");
        parentElement.addImportedType(entityVoType);
        parentElement.addMethod(method);
    }
}

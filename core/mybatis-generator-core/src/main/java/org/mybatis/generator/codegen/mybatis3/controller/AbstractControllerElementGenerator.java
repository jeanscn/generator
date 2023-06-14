package org.mybatis.generator.codegen.mybatis3.controller;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractGenerator;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.IAnnotation;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.custom.annotations.CacheAnnotation;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_PAGEHELPER_RESULT;
import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public abstract class AbstractControllerElementGenerator  extends AbstractGenerator {

    protected FullyQualifiedJavaType entityType;

    protected FullyQualifiedJavaType exampleType;

    protected CommentGenerator commentGenerator;

    protected String serviceBeanName;

    protected String entityNameKey;

    protected FullyQualifiedJavaType entityMappings;

    protected FullyQualifiedJavaType entityVoType;

    protected FullyQualifiedJavaType entityViewVoType;

    protected FullyQualifiedJavaType entityCreateVoType;

    protected FullyQualifiedJavaType entityRequestVoType;

    protected FullyQualifiedJavaType entityUpdateVoType;

    protected FullyQualifiedJavaType entityCachePoType;

    protected FullyQualifiedJavaType entityExcelVoType;

    protected FullyQualifiedJavaType entityExcelImportVoType;

    protected FullyQualifiedJavaType responseResult;

    protected FullyQualifiedJavaType responsePagehelperResult;

    protected HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    protected TableConfiguration tc;

    public abstract void addElements(TopLevelClass parentElement);

    public AbstractControllerElementGenerator() {
        super();
    }

    protected void initGenerator(){
        tc = introspectedTable.getTableConfiguration();
        entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        commentGenerator = context.getCommentGenerator();
        serviceBeanName = introspectedTable.getControllerBeanName();
        entityNameKey = GenerateUtils.isWorkflowInstance(introspectedTable)?"business":"entity";
        if (introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().size()>0) {
            htmlGeneratorConfiguration = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().get(0);
        }
        responseResult = new FullyQualifiedJavaType(RESPONSE_RESULT);
        responsePagehelperResult = new FullyQualifiedJavaType(RESPONSE_PAGEHELPER_RESULT);
        String voTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".")+".pojo";
        entityMappings = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"maps",entityType.getShortName()+"Mappings"));
        entityVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"VO"));
        entityViewVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"ViewVO"));
        entityRequestVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"RequestVO"));
        entityCreateVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"CreateVO"));
        entityUpdateVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"UpdateVO"));
        entityExcelVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"ExcelVO"));
        entityExcelImportVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"ExcelImportVO"));
        entityCachePoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"po",entityType.getShortName()+"CachePO"));
    }

    protected Method createMethod(String methodPrefix) {
        Method method = new Method(methodPrefix + entityType.getShortName());
        method.setVisibility(JavaVisibility.PROTECTED);
        return method;
    }
    protected void addAnnotation(IAnnotation annotation,Method method,TopLevelClass parent){
        annotation.toAnnotations().forEach(method::addAnnotation);
        parent.addImportedTypes(annotation.getImportedTypes());
    }

    /**
     * 构造WEB返回的结果对象
     * @param returnType 返回的类型枚举，此处仅对RESPONSE_RESULT_LIST进行判断，ResponseResult<List<returnTypeArgument>>
     *              其他情况返回ResponseResult<returnTypeArgument>
     * @param returnTypeArgument ResponseResult泛型的实际对象
     * @param parentElement 父元素
     *
     * */
    protected FullyQualifiedJavaType getResponseResult(ReturnTypeEnum returnType, FullyQualifiedJavaType returnTypeArgument,CompilationUnit parentElement) {
        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType responseResult = new FullyQualifiedJavaType(RESPONSE_RESULT);
        if (ReturnTypeEnum.ofCode(returnType.code()) == ReturnTypeEnum.RESPONSE_RESULT_LIST) {
            listType.addTypeArgument(returnTypeArgument);
            responseResult.addTypeArgument(listType);
            parentElement.addImportedType(responseResult);
            parentElement.addImportedType(returnTypeArgument);
            parentElement.addImportedType(listType);
        } else {
            responseResult.addTypeArgument(returnTypeArgument);
            parentElement.addImportedType(responseResult);
            parentElement.addImportedType(returnTypeArgument);
        }
        return responseResult;
    }

    protected Parameter buildMethodParameter(MethodParameterDescript descript){
        if (descript.returnFqt==null) {
            descript.returnFqt = getMethodParameterVOType(descript.methodType);
        }
        Parameter parameter;
        if (descript.isList) {
            FullyQualifiedJavaType listInstance;
            if ("put".equalsIgnoreCase(descript.methodType) || "post".equalsIgnoreCase(descript.methodType)) {
                listInstance = new FullyQualifiedJavaType("com.vgosoft.tool.ValidList");
            }else{
                listInstance = FullyQualifiedJavaType.getNewListInstance();
            }
            listInstance.addTypeArgument(descript.returnFqt);
            parameter = new Parameter(listInstance, descript.returnFqt.getShortNameFirstLowCase()+"s");
            descript.parentElement.addImportedType(descript.returnFqt);
            descript.parentElement.addImportedType(listInstance);
        }else{
            parameter = new Parameter(descript.returnFqt, descript.returnFqt.getShortNameFirstLowCase());
            descript.parentElement.addImportedType(descript.returnFqt);
        }
        if (descript.isValid) {
            descript.parentElement.addImportedType("org.springframework.validation.annotation.Validated");
            switch (descript.methodType.toLowerCase()){
                case "put":
                    parameter.addAnnotation("@Validated(value= ValidateUpdate.class)");
                    descript.parentElement.addImportedType("com.vgosoft.core.valid.ValidateUpdate");
                    break;
                case "post":
                    parameter.addAnnotation("@Validated(value= ValidateInsert.class)");
                    descript.parentElement.addImportedType("com.vgosoft.core.valid.ValidateInsert");
                    break;
            }
        }
        if (descript.isRequestBody) {
            parameter.addAnnotation("@RequestBody");
            descript.parentElement.addImportedType("org.springframework.web.bind.annotation.RequestBody");
        }
        return parameter;
    }

    /**
     * 根据method方法获得vo对象
     * @param methodType 方法的类型，get、put（update）、post（create）、delete等
     * */
    protected FullyQualifiedJavaType getMethodParameterVOType(String methodType) {
        FullyQualifiedJavaType type = null;
        switch (methodType.toLowerCase()){
            case "post":
            case "create":
                if(introspectedTable.getRules().isGenerateCreateVO()){
                    type = entityCreateVoType;
                }
                break;
            case "put":
            case "update":
                if (introspectedTable.getRules().isGenerateUpdateVO()) {
                    type = entityUpdateVoType;
                }
                break;
            case "get":
                if (introspectedTable.getRules().isGenerateRequestVO()) {
                    type = entityRequestVoType;
                }
                break;
        }
        if (type == null) {
            if (introspectedTable.getRules().isGenerateVoModel()) {
                type = entityVoType;
            } else{
                type = entityType;
            }
        }
        return type;
    }

    protected void selectByExampleWithPagehelper(TopLevelClass parentElement, Method method) {
        String listEntityVar = entityType.getShortNameFirstLowCase()+"s";
        String requestVOVar = entityRequestVoType.getShortNameFirstLowCase();
        method.addBodyLine("{0} example = buildExample(actionType,{1});",
                exampleType.getShortName(),
                introspectedTable.getRules().isGenerateRequestVO()?requestVOVar:
                        introspectedTable.getRules().isGenerateVoModel()?entityVoType.getShortNameFirstLowCase():entityType.getShortNameFirstLowCase());
        if (introspectedTable.getRules().isGenerateRequestVO()) {
            method.addBodyLine("if ({0}.getPageNo()>0 && {0}.getPageSize()>0) '{'\n" +
                    "            PageHelper.startPage({0}.getPageNo(), {0}.getPageSize());\n" +
                    "        '}'",requestVOVar);
            parentElement.addImportedType("com.github.pagehelper.PageHelper");
        }
        if (introspectedTable.getRules().isGenerateRequestVO() && introspectedTable.getRules().generateRelationWithSubSelected()) {
            method.addBodyLine("ServiceResult<List<{0}>> result;\n" +
                            "        if ({3}.isCascadeResult()) '{'\n" +
                            "            result = ServiceResult.success({2}.selectByExampleWithRelation(example));\n" +
                            "        '}'else'{'\n" +
                            "            result = {2}.selectByExample(example);\n" +
                            "        '}'",
                    entityType.getShortName(), listEntityVar,serviceBeanName,requestVOVar);
        }else{
            method.addBodyLine("ServiceResult<List<{0}>> result = {1}.selectByExample(example);",
                    entityType.getShortName(), serviceBeanName);
        }
        method.addBodyLine("Page<{0}> page = (Page<{0}>)result.getResult();",entityType.getShortName());
        parentElement.addImportedType("com.github.pagehelper.Page");
    }

    protected void addSecurityPreAuthorize(Method method,String methodPrefix,String nameKey) {
        if (introspectedTable.getRules().isIntegrateSpringSecurity()) {
            String l1 = introspectedTable.getContext().getModuleKeyword().toLowerCase();
            String l2 = serviceBeanName.toLowerCase();
            String l3 = methodPrefix.toLowerCase();

            String sb = "@PreAuthorize(\"" + "@uss.hasPermission('" +
                    l1 +
                    ":" +
                    l2 +
                    ":" +
                    l3 +
                    "')\")";
            method.addAnnotation(sb);

            //构造一条permission插入语句
            Map<String,String> map = new LinkedHashMap<>();
            map.put(l1,introspectedTable.getContext().getModuleName());
            map.put(l2,introspectedTable.getRemarks(true));
            map.put(l3,nameKey);
            JavaBeansUtil.setPermissionSqlData(introspectedTable, map);
        }
    }

    protected void addCacheEvictAnnotation(Method method,TopLevelClass parentElement){
        if (introspectedTable.getRules().isGenerateCachePO()) {
            CacheAnnotation cacheAnnotation = new CacheAnnotation(entityType.getShortName());
            method.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
            parentElement.addImportedType("org.springframework.cache.annotation.CacheEvict");
            method.setVisibility(JavaVisibility.PUBLIC);
        }
    }

    protected String getServiceMethodEntityParameter(boolean isMulti,String methodType){
        if ("create".equals(methodType) && introspectedTable.getRules().isGenerateCreateVO()) {
            return VStringUtil.format("mappings.from{0}CreateVO{2}({1}CreateVO{2})", entityType.getShortName(),entityType.getShortNameFirstLowCase(),isMulti?"s":"");
        }if ("update".equals(methodType) && introspectedTable.getRules().isGenerateUpdateVO()) {
            return VStringUtil.format("mappings.from{0}UpdateVO{2}({1}UpdateVO{2})", entityType.getShortName(),entityType.getShortNameFirstLowCase(),isMulti?"s":"");
        }else if (introspectedTable.getRules().isGenerateVoModel()){
            return VStringUtil.format("mappings.from{0}VO{2}({1}VO{2})", entityType.getShortName(),entityType.getShortNameFirstLowCase(),isMulti?"s":"");
        }else{
            return entityType.getShortNameFirstLowCase()+(isMulti?"s":"");
        }
    }

    /**
     * 方法参数构造内部方法的描述
     * isValid 是否需要增加@Valid验证注解
     * isRequestBody 是否增加@RequestBody注解
     * isList 是否为List参数
     * methodType 方法的类型，get、put、post、delete等
     * returnFqt 返回的类型或泛型类型。如果null是按照方法类型进行计算vo对象
     * parentElement 父级方法的父类
     * */
    public static class MethodParameterDescript{
        private  boolean isValid = false;
        private boolean isRequestBody = false;
        private boolean isList = false;
        private String methodType;
        private FullyQualifiedJavaType returnFqt;
        private TopLevelClass parentElement;

        public MethodParameterDescript(TopLevelClass parentElement,String methodType){
            this.parentElement = parentElement;
            this.methodType = methodType;
        }

        public boolean isValid() {
            return isValid;
        }

        public void setValid(boolean valid) {
            isValid = valid;
        }

        public boolean isRequestBody() {
            return isRequestBody;
        }

        public void setRequestBody(boolean requestBody) {
            isRequestBody = requestBody;
        }

        public boolean isList() {
            return isList;
        }

        public void setList(boolean list) {
            isList = list;
        }

        public String getMethodType() {
            return methodType;
        }

        public void setMethodType(String methodType) {
            this.methodType = methodType;
        }

        public FullyQualifiedJavaType getReturnFqt() {
            return returnFqt;
        }

        public void setReturnFqt(FullyQualifiedJavaType returnFqt) {
            this.returnFqt = returnFqt;
        }

        public TopLevelClass getParentElement() {
            return parentElement;
        }

        public void setParentElement(TopLevelClass parentElement) {
            this.parentElement = parentElement;
        }
    }
}

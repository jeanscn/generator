package org.mybatis.generator.codegen.mybatis3.controller;

import com.vgosoft.core.constant.enums.db.DbFiledDefaultValueEnum;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractGenerator;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.custom.enums.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;
import org.mybatis.generator.custom.annotations.IAnnotation;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_PAGEHELPER_RESULT;
import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

/**
 * @author cen_c
 */
@Setter
@Getter
public abstract class AbstractControllerElementGenerator extends AbstractGenerator {

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

    /**
     * 抽象方法：把方法添加到父类元素中
     * @param parentElement 父类元素
     */
    public abstract void addElements(TopLevelClass parentElement);

    public AbstractControllerElementGenerator() {
        super();
    }

    protected void initGenerator() {
        tc = introspectedTable.getTableConfiguration();
        entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        commentGenerator = context.getCommentGenerator();
        serviceBeanName = introspectedTable.getControllerBeanName();
        entityNameKey = GenerateUtils.isWorkflowInstance(introspectedTable) ? "business" : "entity";
        if (!introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().isEmpty()) {
            htmlGeneratorConfiguration = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().get(0);
        }
        responseResult = new FullyQualifiedJavaType(RESPONSE_RESULT);
        responsePagehelperResult = new FullyQualifiedJavaType(RESPONSE_PAGEHELPER_RESULT);
        String voTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".") + ".pojo";
        entityMappings = new FullyQualifiedJavaType(String.join(".", voTargetPackage, "maps", entityType.getShortName() + "Mappings"));
        entityVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage, "vo", entityType.getShortName() + "Vo"));
        entityViewVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage, "vo", entityType.getShortName() + "ViewVo"));
        entityRequestVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage, "vo", entityType.getShortName() + "RequestVo"));
        entityCreateVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage, "vo", entityType.getShortName() + "CreateVo"));
        entityUpdateVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage, "vo", entityType.getShortName() + "UpdateVo"));
        entityExcelVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage, "vo", entityType.getShortName() + "ExcelVo"));
        entityExcelImportVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage, "vo", entityType.getShortName() + "ExcelImportVo"));
        entityCachePoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage, "po", entityType.getShortName() + "CachePo"));
    }

    protected Method createMethod(String methodPrefix) {
        Method method = new Method(methodPrefix + entityType.getShortName());
        method.setVisibility(JavaVisibility.PROTECTED);
        return method;
    }

    protected void addAnnotation(IAnnotation annotation, Method method, TopLevelClass parent) {
        annotation.toAnnotations().forEach(method::addAnnotation);
        parent.addImportedTypes(annotation.getImportedTypes());
    }

    /**
     * 构造WEB返回的结果对象
     *
     * @param returnType         返回的类型枚举，此处仅对RESPONSE_RESULT_LIST进行判断，ResponseResult<List<returnTypeArgument>>
     *                           其他情况返回ResponseResult<returnTypeArgument>
     * @param returnTypeArgument ResponseResult泛型的实际对象
     * @param parentElement      父元素
     */
    protected FullyQualifiedJavaType getResponseResult(ReturnTypeEnum returnType, FullyQualifiedJavaType returnTypeArgument, CompilationUnit parentElement) {
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

    protected Parameter buildMethodParameter(MethodParameterDescriptor descriptor) {
        if (descriptor.returnFqt == null) {
            descriptor.returnFqt = getMethodParameterVoType(descriptor.methodType);
        }
        Parameter parameter;
        if (descriptor.isList) {
            FullyQualifiedJavaType listInstance;
            if ("put".equalsIgnoreCase(descriptor.methodType) || "post".equalsIgnoreCase(descriptor.methodType)) {
                listInstance = new FullyQualifiedJavaType("com.vgosoft.tool.ValidList");
            } else {
                listInstance = FullyQualifiedJavaType.getNewListInstance();
            }
            listInstance.addTypeArgument(descriptor.returnFqt);
            parameter = new Parameter(listInstance, descriptor.returnFqt.getShortNameFirstLowCase() + "s");
            descriptor.parentElement.addImportedType(descriptor.returnFqt);
            descriptor.parentElement.addImportedType(listInstance);
        } else {
            parameter = new Parameter(descriptor.returnFqt, descriptor.returnFqt.getShortNameFirstLowCase());
            descriptor.parentElement.addImportedType(descriptor.returnFqt);
        }
        if (descriptor.isValid) {
            descriptor.parentElement.addImportedType("org.springframework.validation.annotation.Validated");
            switch (descriptor.methodType.toLowerCase()) {
                case "put":
                    parameter.addAnnotation("@Validated(value= ValidateUpdate.class)");
                    descriptor.parentElement.addImportedType("com.vgosoft.core.valid.ValidateUpdate");
                    break;
                case "post":
                    parameter.addAnnotation("@Validated(value= ValidateInsert.class)");
                    descriptor.parentElement.addImportedType("com.vgosoft.core.valid.ValidateInsert");
                    break;
                default:
                    break;
            }
        }
        if (descriptor.isRequestBody) {
            parameter.addAnnotation("@RequestBody");
            descriptor.parentElement.addImportedType("org.springframework.web.bind.annotation.RequestBody");
        }
        return parameter;
    }

    /**
     * 根据method方法获得vo对象
     *
     * @param methodType 方法的类型，get、put（update）、post（create）、delete等
     */
    protected FullyQualifiedJavaType getMethodParameterVoType(String methodType) {
        FullyQualifiedJavaType type = null;
        switch (methodType.toLowerCase()) {
            case "post":
            case "create":
                if (introspectedTable.getRules().isGenerateCreateVo()) {
                    type = entityCreateVoType;
                }
                break;
            case "put":
            case "update":
                if (introspectedTable.getRules().isGenerateUpdateVo()) {
                    type = entityUpdateVoType;
                }
                break;
            case "get":
            case "list":
                if (introspectedTable.getRules().isGenerateRequestVo()) {
                    type = entityRequestVoType;
                }
                break;
            default:
                break;
        }
        if (type == null) {
            if (introspectedTable.getRules().isGenerateVoModel()) {
                type = entityVoType;
            } else {
                type = entityType;
            }
        }
        return type;
    }

    protected void selectByExampleWithPagehelper(TopLevelClass parentElement, Method method) {
        //String listEntityVar = entityType.getShortNameFirstLowCase() + "s";
        String requestVoVar = entityRequestVoType.getShortNameFirstLowCase();
        method.addBodyLine("if (actionType != null) '{' {0}.setActionType(actionType);'}'", introspectedTable.getRules().isGenerateRequestVo() ? entityRequestVoType.getShortNameFirstLowCase() :
                introspectedTable.getRules().isGenerateVoModel() ? entityVoType.getShortNameFirstLowCase() : entityType.getShortNameFirstLowCase());
        method.addBodyLine("{0} example = buildExample({1});",
                exampleType.getShortName(),
                introspectedTable.getRules().isGenerateRequestVo() ? requestVoVar :
                        introspectedTable.getRules().isGenerateVoModel() ? entityVoType.getShortNameFirstLowCase() : entityType.getShortNameFirstLowCase());
        addSelectByExampleWithPagehelper(parentElement, method, requestVoVar);
    }

    public void addSelectByExampleWithPagehelper(TopLevelClass parentElement, Method method, String requestVoVar) {
        method.addBodyLine("ServiceResult<List<{0}>> result;", entityType.getShortName());
        if (introspectedTable.getRules().isGenerateRequestVo() && introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoRequestConfiguration().isIncludePageParam()) {
            method.addBodyLine("Page<{0}> page = null;", entityType.getShortName());
            method.addBodyLine("if ({0}.getPageNo() == 0 || {0}.getPageSize() == 0) '{'", requestVoVar);
            method.addBodyLine("PageHelper.clearPage();");
            addSelectByExample(method, requestVoVar);
            method.addBodyLine("} else {");
            method.addBodyLine("PageHelper.startPage({0}.getPageNo(), {0}.getPageSize());", requestVoVar);
            addSelectByExample(method, requestVoVar);
            method.addBodyLine("page = (Page<{0}>) result.getResult();", entityType.getShortName());
            method.addBodyLine("}");

            parentElement.addImportedType("com.github.pagehelper.Page");
            parentElement.addImportedType("com.github.pagehelper.PageHelper");
        } else {
            addSelectByExample(method, requestVoVar);
        }
    }

    public void addSelectByExample(Method method, String requestVoVar) {
        if (introspectedTable.getRules().isGenerateRequestVo() && introspectedTable.getRules().generateRelationWithSubSelected()) {
            method.addBodyLine("if ({0}.isCascadeResult()) '{'", requestVoVar);
            method.addBodyLine("result = ServiceResult.success({0}.selectByExampleWithRelation(example));", serviceBeanName);
            if (introspectedTable.getColumn(DefaultColumnNameEnum.PARENT_ID.columnName()).isPresent()) {
                method.addBodyLine("'}' else if ({0}.isCountChildren())'{'", requestVoVar);
                method.addBodyLine("result = {0}.selectByExampleWithChildrenCount(example);", serviceBeanName);
            }
            method.addBodyLine("} else {");
            method.addBodyLine("result = {0}.selectByExample(example);", serviceBeanName);
            method.addBodyLine("}");
        } else {
            method.addBodyLine("result = {0}.selectByExample(example);", serviceBeanName);
        }
    }

    protected void addSecurityPreAuthorize(Method method, String methodPrefix, String nameKey) {
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
            Map<String, String> map = new LinkedHashMap<>();
            map.put(l1, introspectedTable.getContext().getModuleName());
            map.put(l2, introspectedTable.getRemarks(true));
            map.put(l3, nameKey);
            Mb3GenUtil.setPermissionSqlData(introspectedTable, map);
        }
    }

    protected void addCacheEvictAnnotation(Method method, TopLevelClass parentElement) {
        if (introspectedTable.getRules().isGenerateCachePo()) {
            CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc(entityType.getShortName());
            method.addAnnotation(cacheAnnotationDesc.toCacheEvictAnnotation(true));
            parentElement.addImportedType("org.springframework.cache.annotation.CacheEvict");
            method.setVisibility(JavaVisibility.PUBLIC);
        }
    }

    protected String getServiceMethodEntityParameter(boolean isMulti, String methodType) {
        if ("create".equals(methodType) && introspectedTable.getRules().isGenerateCreateVo()) {
            return VStringUtil.format("mappings.from{0}CreateVo{2}({1}CreateVo{2})", entityType.getShortName(), entityType.getShortNameFirstLowCase(), isMulti ? "s" : "");
        }
        if ("update".equals(methodType) && introspectedTable.getRules().isGenerateUpdateVo()) {
            return VStringUtil.format("mappings.from{0}UpdateVo{2}({1}UpdateVo{2})", entityType.getShortName(), entityType.getShortNameFirstLowCase(), isMulti ? "s" : "");
        } else if (introspectedTable.getRules().isGenerateVoModel()) {
            return VStringUtil.format("mappings.from{0}Vo{2}({1}Vo{2})", entityType.getShortName(), entityType.getShortNameFirstLowCase(), isMulti ? "s" : "");
        } else {
            return entityType.getShortNameFirstLowCase() + (isMulti ? "s" : "");
        }
    }

    protected static void addIocInitialDefaultValue(IntrospectedTable introspectedTable, Method method, FullyQualifiedJavaType type) {
        List<IntrospectedColumn> initialColumns = introspectedTable.getAllColumns().stream()
                .filter(column -> VStringUtil.stringHasValue(column.getDefaultValue()) && DbFiledDefaultValueEnum.ofCode(column.getDefaultValue()) != null)
                .collect(Collectors.toList());
        if (!initialColumns.isEmpty()) {
            for (IntrospectedColumn initialColumn : initialColumns) {
                String propertyName = initialColumn.getJavaProperty();
                DbFiledDefaultValueEnum defaultValueEnum = DbFiledDefaultValueEnum.ofCode(initialColumn.getDefaultValue());
                if (defaultValueEnum != null) {
                    method.addBodyLine("{0}.{1}({0}.{2}());", type.getShortNameFirstLowCase()
                            , JavaBeansUtil.getSetterMethodName(propertyName)
                            , JavaBeansUtil.getGetterMethodName(propertyName, initialColumn.getFullyQualifiedJavaType()));
                }
            }
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
     */
    public static class MethodParameterDescriptor {
        private boolean isValid = false;
        private boolean isRequestBody = false;
        private boolean isList = false;
        private final String methodType;
        private FullyQualifiedJavaType returnFqt;
        private final TopLevelClass parentElement;

        public MethodParameterDescriptor(TopLevelClass parentElement, String methodType) {
            this.parentElement = parentElement;
            this.methodType = methodType;
        }

        public boolean isValid() {
            return isValid;
        }

        public void setValid(boolean valid) {
            isValid = valid;
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
    }
}

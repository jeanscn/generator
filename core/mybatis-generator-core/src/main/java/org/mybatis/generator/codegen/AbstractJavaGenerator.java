/*
 *    Copyright 2006-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.codegen;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.*;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getGetterMethodName;

public abstract class AbstractJavaGenerator extends AbstractGenerator {

    public abstract List<CompilationUnit> getCompilationUnits();

    private final String project;

    protected AbstractJavaGenerator(String project) {
        this.project = project;
    }

    public String getProject() {
        return project;
    }

    public static Method getGetter(Field field) {
        Method method = new Method(getGetterMethodName(field.getName(), field.getType()));
        method.setReturnType(field.getType());
        method.setVisibility(JavaVisibility.PUBLIC);
        String s = "return " + field.getName() + ';';
        method.addBodyLine(s);
        return method;
    }

    public String getRootClass() {
        String rootClass = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ANY_ROOT_CLASS);
        if (rootClass == null) {
            Properties properties = context.getJavaModelGeneratorConfiguration().getProperties();
            rootClass = properties.getProperty(PropertyRegistry.ANY_ROOT_CLASS);
        }
        return rootClass;
    }

    protected void addDefaultConstructor(TopLevelClass topLevelClass) {
        topLevelClass.addMethod(getDefaultConstructor(topLevelClass));
    }

    protected void addDefaultConstructorWithGeneratedAnnotation(TopLevelClass topLevelClass) {
        topLevelClass.addMethod(getDefaultConstructorWithGeneratedAnnotation(topLevelClass));
    }

    /**
     * 获得service类的抽象实现类
     *
     * @param introspectedTable 生成基类
     */
    protected String getAbstractService(IntrospectedTable introspectedTable) {
        if (GenerateUtils.isBlobInstance(introspectedTable)) {
            String steamOutType = introspectedTable.getConfigPropertyValue(PropertyRegistry.TABLE_JAVA_MODEL_BYTE_STREAM_OUTPUT_MODE);
            if (GenerateUtils.isBusinessInstance(introspectedTable)) {
                switch (steamOutType) {
                    case "bytes":
                        return ABSTRACT_BLOB_BYTES_SERVICE_BUSINESS;
                    case "file":
                        return ABSTRACT_BLOB_FILE_SERVICE_BUSINESS;
                    case "string":
                        return ABSTRACT_BLOB_STRING_SERVICE_BUSINESS;
                }
                return ABSTRACT_SERVICE_BUSINESS;
            } else {
                switch (steamOutType) {
                    case "bytes":
                        return ABSTRACT_MBG_BLOB_BYTES_SERVICE;
                    case "file":
                        return ABSTRACT_MBG_BLOB_FILE_SERVICE;
                    case "string":
                        return ABSTRACT_MBG_BLOB_STRING_SERVICE;
                }
                return ABSTRACT_MBG_BLOB_SERVICE_INTERFACE;
            }
        }
        return ABSTRACT_MBG_SERVICE_INTERFACE;
    }

    private Method getDefaultConstructor(TopLevelClass topLevelClass) {
        Method method = getBasicConstructor(topLevelClass);
        addGeneratedJavaDoc(method);
        return method;
    }

    private Method getDefaultConstructorWithGeneratedAnnotation(TopLevelClass topLevelClass) {
        Method method = getBasicConstructor(topLevelClass);
        addGeneratedAnnotation(method, topLevelClass);
        return method;
    }

    private Method getBasicConstructor(TopLevelClass topLevelClass) {
        Method method = new Method(topLevelClass.getType().getShortName());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setConstructor(true);
        method.addBodyLine("super();"); //$NON-NLS-1$
        return method;
    }

    private void addGeneratedJavaDoc(Method method) {
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
    }

    private void addGeneratedAnnotation(Method method, TopLevelClass topLevelClass) {
        context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable,
                topLevelClass.getImportedTypes());
    }

    /**
     * 根据给定信息构造类方法
     *
     * @param methodName         要构建的方法名称
     * @param returnType         方法返回类型。l-list、m-model、r-ServiceResult、sl-ServiceResult<List>、sm-ServiceResult<model>
     * @param returnTypeArgument 返回的参数类型T。
     *                           returnType为l：List<T>、m：T、r：ServiceResult<T>、sl:ServiceResult<List<T>>、sl:ServiceResult<T>
     * @param parameterType      方法的参数类型
     * @param parameterName      方法的参数名称
     * @param isAbstract         是否为抽象方法
     * @param parameterRemark             参数的文字说明
     * @param parentElement      父元素
     */
    protected Method getMethodByType(String methodName,
                                     ReturnTypeEnum returnType,
                                     FullyQualifiedJavaType returnTypeArgument,
                                     FullyQualifiedJavaType parameterType,
                                     String parameterName,
                                     String parameterRemark,
                                     boolean isAbstract,
                                     CompilationUnit parentElement) {
        Parameter parameter = new Parameter(parameterType, parameterName).setRemark(parameterRemark);
        return getMethodByType(methodName
                ,returnType
                ,returnTypeArgument
                ,Collections.singletonList(parameter)
                ,isAbstract
                ,parentElement);
    }

    protected Method getMethodByType(String methodName,
                                     ReturnTypeEnum returnType,
                                     FullyQualifiedJavaType returnTypeArgument,
                                     List<Parameter> parameters,
                                     boolean isAbstract,
                                     CompilationUnit parentElement) {
        Method method = new Method(methodName);
        if (isAbstract) {
            method.setAbstract(true);
        } else {
            method.setVisibility(JavaVisibility.PUBLIC);
        }
        method.getParameters().addAll(parameters);
        parameters.forEach(p->parentElement.addImportedType(p.getType()));
        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType serviceResult = new FullyQualifiedJavaType(SERVICE_RESULT);
        FullyQualifiedJavaType responseResult = new FullyQualifiedJavaType(RESPONSE_RESULT);
        switch (ReturnTypeEnum.ofCode(returnType.code())) {
            case LIST:
                listType.addTypeArgument(returnTypeArgument);
                method.setReturnType(listType);
                parentElement.addImportedType(listType);
                parentElement.addImportedType(returnTypeArgument);
                break;
            case SERVICE_RESULT_MODEL:
                serviceResult.addTypeArgument(returnTypeArgument);
                method.setReturnType(serviceResult);
                parentElement.addImportedType(serviceResult);
                parentElement.addImportedType(returnTypeArgument);
                break;
            case SERVICE_RESULT_LIST:
                listType.addTypeArgument(returnTypeArgument);
                serviceResult.addTypeArgument(listType);
                method.setReturnType(serviceResult);
                parentElement.addImportedType(serviceResult);
                parentElement.addImportedType(returnTypeArgument);
                parentElement.addImportedType(listType);
                break;
            case RESPONSE_RESULT_LIST:
                listType.addTypeArgument(returnTypeArgument);
                responseResult.addTypeArgument(listType);
                method.setReturnType(responseResult);
                parentElement.addImportedType(responseResult);
                parentElement.addImportedType(returnTypeArgument);
                parentElement.addImportedType(listType);
                break;
            case RESPONSE_RESULT_MODEL:
                responseResult.addTypeArgument(returnTypeArgument);
                method.setReturnType(responseResult);
                parentElement.addImportedType(responseResult);
                parentElement.addImportedType(returnTypeArgument);
                break;
            default:
                method.setReturnType(returnTypeArgument);
                parentElement.addImportedType(returnTypeArgument);
                break;
        }

        List<String> collect = parameters.stream()
                .map(p -> "@param " + p.getName() + " " + p.getRemark())
                .collect(Collectors.toList());
        collect.add(0,"");
        collect.add(0,"这个抽象方法通过定制版Mybatis Generator自动生成");
        collect.add(0,"提示 - @mbg.generated");
        String[] strings = collect.toArray(new String[0]);
        context.getCommentGenerator().addMethodJavaDocLine(method, false,strings);
        return method;
    }


    protected void addCacheConfig(TopLevelClass topLevelClass) {
        topLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.cache.annotation.CacheConfig"));
        topLevelClass.addStaticImport("com.vgosoft.core.constant.CacheConstant.CACHE_MANAGER_NAME");
        topLevelClass.addAnnotation("@CacheConfig(cacheManager = CACHE_MANAGER_NAME)");
    }
}

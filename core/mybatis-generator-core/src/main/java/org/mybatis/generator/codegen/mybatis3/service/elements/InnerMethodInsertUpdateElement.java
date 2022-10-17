package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.RelationTypeEnum;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.pojo.RelationGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.codegen.mybatis3.service.JavaServiceImplGenerator.SUFFIX_INSERT_UPDATE_BATCH;
import static org.mybatis.generator.custom.ConstantsUtil.*;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class InnerMethodInsertUpdateElement extends AbstractServiceElementGenerator {


    public InnerMethodInsertUpdateElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {

        introspectedTable.getRelationGeneratorConfigurations().stream()
                .filter(c -> c.isEnableInsert() || c.isEnableUpdate() || c.isEnableInsertOrUpdate() || c.isEnableDelete())
                .forEach(config -> {
                            List<Parameter> parameters = new ArrayList<>();
                            String methodName = config.getPropertyName() + SUFFIX_INSERT_UPDATE_BATCH;
                            FullyQualifiedJavaType modelType = new FullyQualifiedJavaType(config.getModelTye());
                            Parameter parameter1 = new Parameter(entityType, entityType.getShortNameFirstLowCase());
                            parameter1.setRemark("主记录的对象");
                            parameters.add(parameter1);
                            boolean isCollection = config.getType().equals(RelationTypeEnum.collection);
                            if (isCollection) {
                                FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
                                listInstance.addTypeArgument(modelType);
                                Parameter parameter = new Parameter(listInstance, "items");
                                parameter.setRemark("待处理的子记录集合");
                                parameters.add(parameter);
                            } else {
                                Parameter parameter = new Parameter(modelType, "item");
                                parameter.setRemark("待处理的子记录");
                                parameters.add(parameter);
                            }
                            Parameter parameter = new Parameter(new FullyQualifiedJavaType(ACTION_CATE_ENUM), "actionCate");
                            parameter.setRemark("数据库操作类型的枚举");
                            parameters.add(parameter);

                            FullyQualifiedJavaType serviceResult = new FullyQualifiedJavaType(SERVICE_RESULT);
                            serviceResult.addTypeArgument(modelType);
                            Method innerInsertUpdateMethod = serviceMethods.getMethodByType(methodName
                                    , isCollection ? ReturnTypeEnum.LIST : ReturnTypeEnum.MODEL
                                    , serviceResult
                                    , "影响对象ServiceResult返回"
                                    , parameters
                                    , false
                                    , parentElement);
                            innerInsertUpdateMethod.setVisibility(JavaVisibility.PROTECTED);
                            context.getCommentGenerator().addMethodJavaDocLine(innerInsertUpdateMethod, "级联操作的实现方法");
                            parentElement.addImportedType(ACTION_CATE_ENUM);

                            if (config.getBeanClassFullName() != null) {
                                if (isCollection) {
                                    innerInsertUpdateMethod.addBodyLine("if (items == null || items.size()==0) return Collections.singletonList(ServiceResult.success(null));");
                                    parentElement.addImportedType("java.util.Collections");
                                } else {
                                    innerInsertUpdateMethod.addBodyLine("if (item == null) return ServiceResult.success(null);");
                                }
                                FullyQualifiedJavaType beanClass = new FullyQualifiedJavaType(config.getBeanClassFullName());
                                innerInsertUpdateMethod.addBodyLine("{0} bean = SpringContextHolder.getBean({0}.class);", beanClass.getShortName());

                                if (isCollection) {
                                    innerInsertUpdateMethod.addBodyLine("switch (actionCate.code()) {");
                                    innerInsertUpdateMethod.addBodyLine("    case \"INSERT\":");
                                    innerInsertUpdateMethod.addBodyLine("return items.stream().map(item->{");
                                    printSetRelationValue(innerInsertUpdateMethod, config);
                                    innerInsertUpdateMethod.addBodyLine("    return bean.insertSelective(item);");
                                    innerInsertUpdateMethod.addBodyLine("    }).collect(Collectors.toList());");
                                    innerInsertUpdateMethod.addBodyLine("case \"UPDATE\":");
                                    innerInsertUpdateMethod.addBodyLine("return items.stream().map(item->{");
                                    printSetRelationValue(innerInsertUpdateMethod, config);
                                    innerInsertUpdateMethod.addBodyLine("          if(VStringUtil.isBlank(item.getId())){\n" +
                                            "                        return bean.insertSelective(item);\n" +
                                            "                    }else{\n" +
                                            "                        return bean.updateByPrimaryKeySelective(item);\n" +
                                            "                    }");
                                    innerInsertUpdateMethod.addBodyLine("    }).collect(Collectors.toList());");
                                    innerInsertUpdateMethod.addBodyLine("case \"INSERTORUPDATE\":");
                                    innerInsertUpdateMethod.addBodyLine("return items.stream().map(item->{");
                                    printSetRelationValue(innerInsertUpdateMethod, config);
                                    innerInsertUpdateMethod.addBodyLine("    return bean.insertOrUpdate(item);");
                                    innerInsertUpdateMethod.addBodyLine("    }).collect(Collectors.toList());");
                                    innerInsertUpdateMethod.addBodyLine("case \"DELETE\":");
                                    innerInsertUpdateMethod.addBodyLine("return items.stream().map(i -> {");
                                    innerInsertUpdateMethod.addBodyLine("int ret = bean.deleteByPrimaryKey(i.getId());");
                                    innerInsertUpdateMethod.addBodyLine("ServiceResult<{0}> result = ret > 0 ? ServiceResult.success(i) : ServiceResult.failure(ServiceCodeEnum.FAIL);"
                                            , modelType.getShortName());
                                    innerInsertUpdateMethod.addBodyLine("return result;\n" +
                                            "                        }).collect(Collectors.toList());\n" +
                                            "            default:\n" +
                                            "                return Collections.singletonList(ServiceResult.failure(ServiceCodeEnum.FAIL));");
                                    parentElement.addImportedType("java.util.stream.Collectors");
                                    parentElement.addImportedType("java.util.Collections");
                                } else {
                                    innerInsertUpdateMethod.addBodyLine("ServiceResult<{0}> serviceResult;",modelType.getShortName());
                                    innerInsertUpdateMethod.addBodyLine("switch (actionCate.code()) {");
                                    innerInsertUpdateMethod.addBodyLine("    case \"INSERT\":");
                                    innerInsertUpdateMethod.addBodyLine("serviceResult = bean.insertSelective(item);");
                                    printSetRelationValue2(innerInsertUpdateMethod, config);
                                    innerInsertUpdateMethod.addBodyLine("case \"UPDATE\":");
                                    innerInsertUpdateMethod.addBodyLine("if (VStringUtil.isBlank(item.getId())) {\n" +
                                            "                    serviceResult = bean.insertSelective(item);\n" +
                                            "                } else {\n" +
                                            "                    serviceResult = bean.updateByPrimaryKeySelective(item);\n" +
                                            "                }");
                                    printSetRelationValue2(innerInsertUpdateMethod, config);
                                    innerInsertUpdateMethod.addBodyLine("case \"INSERTORUPDATE\":");
                                    innerInsertUpdateMethod.addBodyLine(" serviceResult = bean.insertOrUpdate(item);");
                                    printSetRelationValue2(innerInsertUpdateMethod, config);
                                    innerInsertUpdateMethod.addBodyLine("case \"DELETE\":\n" +
                                            "                int ret = bean.deleteByPrimaryKey(item.getId());\n" +
                                            "                return ret > 0 ? ServiceResult.success(item) : ServiceResult.failure(ServiceCodeEnum.FAIL);\n" +
                                            "            default:\n" +
                                            "                return ServiceResult.failure(ServiceCodeEnum.FAIL);");
                                }
                                innerInsertUpdateMethod.addBodyLine("}");
                                parentElement.addImportedType(SPRING_CONTEXT_HOLDER);
                                parentElement.addImportedType(beanClass);
                                parentElement.addImportedType(SERVICE_CODE_ENUM);
                            } else {
                                if (isCollection) {
                                    innerInsertUpdateMethod.addBodyLine("return Collections.singletonList(ServiceResult.success(null));");
                                    parentElement.addImportedType("java.util.Collections");
                                } else {
                                    innerInsertUpdateMethod.addBodyLine("ServiceResult.success(null);");
                                }
                            }
                            parentElement.addImportedType("com.vgosoft.tool.core.VStringUtil");
                            parentElement.addMethod(innerInsertUpdateMethod);
                        }
                );
    }

    private void printSetRelationValue(Method method, RelationGeneratorConfiguration configuration) {
        method.addBodyLine("if(VStringUtil.isBlank(item.{0}())) item.{1}({2});"
                , JavaBeansUtil.getGetterMethodName(configuration.getRelationProperty()
                        , configuration.isRelationPropertyIsBoolean() ? FullyQualifiedJavaType.getBooleanPrimitiveInstance() : FullyQualifiedJavaType.getStringInstance())
                , JavaBeansUtil.getSetterMethodName(configuration.getRelationProperty()), entityType.getShortNameFirstLowCase() + ".getId()");
    }

    private void printSetRelationValue2(Method method, RelationGeneratorConfiguration configuration) {
        method.addBodyLine("if (serviceResult.hasResult()) '{'\n" +
                        "                    orgUser.{0}(serviceResult.getResult().getId());\n" +
                        "                '}'\n" +
                        "                return serviceResult;"
                , JavaBeansUtil.getSetterMethodName(configuration.getRelationProperty()));
    }
}

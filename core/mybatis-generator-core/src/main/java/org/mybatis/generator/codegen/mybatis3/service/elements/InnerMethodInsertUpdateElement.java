package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.RelationTypeEnum;
import org.mybatis.generator.custom.ReturnTypeEnum;

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
                            introspectedTable.getColumn(config.getColumn()).ifPresent(column -> {
                                Parameter parameter = new Parameter(column.getFullyQualifiedJavaType(), column.getJavaProperty());
                                parameter.setRemark("主记录的标识");
                                parameters.add(parameter);
                            });
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
                            Method innerInsertUpdateMethod = getMethodByType(methodName
                                    , isCollection ? ReturnTypeEnum.LIST : ReturnTypeEnum.MODEL
                                    , serviceResult
                                    , parameters
                                    , false
                                    , parentElement);
                            innerInsertUpdateMethod.setVisibility(JavaVisibility.PROTECTED);
                            parentElement.addImportedType(ACTION_CATE_ENUM);

                            if (config.getBeanClassFullName() != null) {
                                FullyQualifiedJavaType beanClass = new FullyQualifiedJavaType(config.getBeanClassFullName());
                                innerInsertUpdateMethod.addBodyLine("{0} bean = SpringContextHolder.getBean({0}.class);", beanClass.getShortName());
                                if (isCollection) {
                                    innerInsertUpdateMethod.addBodyLine("switch (actionCate.code()) '{'\n" +
                                            "            case \"INSERT\":\n" +
                                            "                return items.stream().map(bean::insertSelective).collect(Collectors.toList());\n" +
                                            "            case \"UPDATE\":\n" +
                                            "                return items.stream().map(bean::updateByPrimaryKeySelective).collect(Collectors.toList());\n" +
                                            "            case \"INSERTORUPDATE\":\n" +
                                            "                return items.stream().map(bean::insertOrUpdate).collect(Collectors.toList());\n" +
                                            "            case \"DELETE\":\n" +
                                            "                return items.stream()\n" +
                                            "                        .map(i -> '{'\n" +
                                            "                            int ret = bean.deleteByPrimaryKey(i.getId());\n" +
                                            "                            ServiceResult<{0}> result = ret > 0 ? ServiceResult.success(i) : ServiceResult.failure(ServiceCodeEnum.FAIL);\n" +
                                            "                            return result;\n" +
                                            "                        '}').collect(Collectors.toList());\n" +
                                            "            default:\n" +
                                            "                return Collections.singletonList(ServiceResult.failure(ServiceCodeEnum.FAIL));\n" +
                                            "        '}'", modelType.getShortName());
                                    parentElement.addImportedType("java.util.stream.Collectors");
                                } else {
                                    innerInsertUpdateMethod.addBodyLine("switch (actionCate.code()) {\n" +
                                            "            case \"INSERT\":\n" +
                                            "                return bean.insertSelective(item);\n" +
                                            "            case \"UPDATE\":\n" +
                                            "                return bean.updateByPrimaryKey(item);\n" +
                                            "            case \"INSERTORUPDATE\":\n" +
                                            "                return bean.insertOrUpdate(item);\n" +
                                            "            case \"DELETE\":\n" +
                                            "                int ret = bean.deleteByPrimaryKey(item.getId());\n" +
                                            "                return ret > 0 ? ServiceResult.success(item) : ServiceResult.failure(ServiceCodeEnum.FAIL);\n" +
                                            "            default:\n" +
                                            "                return ServiceResult.failure(ServiceCodeEnum.FAIL);\n" +
                                            "        }");
                                }
                                parentElement.addImportedType("java.util.Collections");
                                parentElement.addImportedType(SPRING_CONTEXT_HOLDER);
                                parentElement.addImportedType(beanClass);
                                parentElement.addImportedType(SERVICE_CODE_ENUM);
                            } else {
                                if (isCollection) {
                                    innerInsertUpdateMethod.addBodyLine("Collections.singletonList(ServiceResult.success(null));");
                                    parentElement.addImportedType("java.util.Collections");
                                } else {
                                    innerInsertUpdateMethod.addBodyLine("ServiceResult.success(null);");
                                }
                            }
                            parentElement.addMethod(innerInsertUpdateMethod);
                        }
                );
    }
}

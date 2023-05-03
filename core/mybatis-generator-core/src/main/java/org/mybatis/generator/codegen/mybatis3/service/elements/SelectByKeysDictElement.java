package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;
import org.mybatis.generator.config.VOCacheGeneratorConfiguration;
import org.mybatis.generator.custom.ConstantsUtil;
import org.mybatis.generator.custom.annotations.CacheAnnotation;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_CODE_ENUM;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class SelectByKeysDictElement extends AbstractServiceElementGenerator {

    public SelectByKeysDictElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        VOCacheGeneratorConfiguration voCacheGeneratorConfiguration = tc.getVoCacheGeneratorConfiguration();
        Method selectByKeysDictMethod = serviceMethods.getSelectByKeysDictMethod(parentElement,
                voCacheGeneratorConfiguration,
                false,true);
        selectByKeysDictMethod.addAnnotation("@Override");
        List<IntrospectedColumn> parameterColumns = (new ServiceMethods(context,introspectedTable)).getSelectDictParameterColumns(
                introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration(), introspectedTable);
        CacheAnnotation cacheAnnotation = new CacheAnnotation(entityType.getShortName());
        //cacheAnnotation.setUnless("#result.hasResult()==false");
        cacheAnnotation.setParameters(parameterColumns.size() == 0 ? 1 : parameterColumns.size());
        cacheAnnotation.setUnless("#result==null || #result.getResult()==null || #result.getResult().isEmpty()");
        selectByKeysDictMethod.addAnnotation(cacheAnnotation.toCacheableAnnotation());
        parentElement.addImportedType("org.springframework.cache.annotation.Cacheable");
        parentElement.addImportedType("org.springframework.cache.annotation.CacheEvict");
        selectByKeysDictMethod.addBodyLine("{0}Mappings mappings = {0}Mappings.INSTANCE;", entityType.getShortName()+ ConstantsUtil.MAPPINGS_CACHE_PO_KEY);
        if (introspectedTable.getRules().isGenerateCachePO()) {
            String parameters = parameterColumns.stream().map(IntrospectedColumn::getJavaProperty).collect(Collectors.joining(","));
            selectByKeysDictMethod.addBodyLine("List<{0}> result = mapper.{2}({1});"
                    , entityType.getShortName()
                    , parameters
                    , introspectedTable.getSelectByKeysDictStatementId());
            selectByKeysDictMethod.addBodyLine("if (result.size()>0) {");
            selectByKeysDictMethod.addBodyLine("return ServiceResult.success(mappings.to{0}CachePOs(result));"
                    , entityType.getShortName());
        } else {
            String parameters = introspectedTable.getPrimaryKeyColumns().stream()
                    .map(IntrospectedColumn::getJavaProperty)
                    .collect(Collectors.joining(","));
            selectByKeysDictMethod.addBodyLine("{0} result = mapper.selectByPrimaryKey({1});"
                    , entityType.getShortName()
                    , parameters);
            selectByKeysDictMethod.addBodyLine("if (result!=null) {");
            selectByKeysDictMethod.addBodyLine("return ServiceResult.success(mappings.to{0}CachePO(result));"
                    , entityType.getShortName());
        }
        selectByKeysDictMethod.addBodyLine("}else{");
        selectByKeysDictMethod.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN);");
        selectByKeysDictMethod.addBodyLine("}");
        parentElement.addMethod(selectByKeysDictMethod);
        parentElement.addImportedType(voCacheGeneratorConfiguration.getBaseTargetPackage() + ".maps." + entityType.getShortName() +ConstantsUtil.MAPPINGS_CACHE_PO_KEY+ "Mappings");
        parentElement.addImportedType(SERVICE_CODE_ENUM);
    }
}

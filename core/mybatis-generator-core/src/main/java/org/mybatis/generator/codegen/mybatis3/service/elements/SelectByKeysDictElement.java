package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.config.VoCacheGeneratorConfiguration;
import org.mybatis.generator.codegen.mybatis3.DictCacheOrderResolver;
import org.mybatis.generator.custom.ConstantsUtil;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.List;

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
        parentElement.addImportedType("org.springframework.cache.annotation.Cacheable");
        parentElement.addImportedType("org.springframework.cache.annotation.CacheEvict");
        parentElement.addImportedType("com.vgosoft.core.pojo.parameter.SelDictByKeysParam");
        parentElement.addImportedType(new FullyQualifiedJavaType(SERVICE_CODE_ENUM));
        parentElement.addImportedType(FullyQualifiedJavaType.getOptionalFullyQualifiedJavaType());

        VoCacheGeneratorConfiguration voCacheGeneratorConfiguration = tc.getVoCacheGeneratorConfiguration();

        String cacheMappingsFullName = voCacheGeneratorConfiguration.getBaseTargetPackage() + ".maps." + entityType.getShortName() + ConstantsUtil.MAPPINGS_CACHE_PO_KEY + "Mappings";
        FullyQualifiedJavaType typeCacheMappingType = new FullyQualifiedJavaType(cacheMappingsFullName);

        Method selectByKeysDictMethod = serviceMethods.getSelectByKeysDictMethod(parentElement,
                voCacheGeneratorConfiguration,
                false, true);
        selectByKeysDictMethod.addAnnotation("@Override");
        final List<Parameter> parameters = serviceMethods.getSelectByKeysDictMethodParameters();
        //添加缓存注解
        CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc(entityType.getShortName());
        cacheAnnotationDesc.setOtherKey(cacheMethodIdentity(selectByKeysDictMethod.getName()));
        cacheAnnotationDesc.setParameters(parameters);
        cacheAnnotationDesc.setUnless("#result==null || #result instanceof T(java.lang.Exception) || #result.isEmpty()");
        selectByKeysDictMethod.addAnnotation(cacheAnnotationDesc.toCacheableAnnotation(parentElement));
        IntrospectedColumn keyColumn = DictCacheOrderResolver.requireColumn(introspectedTable,
                voCacheGeneratorConfiguration.getKeyColumn(), "keyColumn");
        String keys = keyColumn.isStringColumn() ? "keys"
                : isBigDecimalType(keyColumn) ? "keys.toPlainString()" : "String.valueOf(keys)";
        IntrospectedColumn typeColumn = introspectedTable.getColumn(
                voCacheGeneratorConfiguration.getTypeColumn()).orElse(null);
        String types = typeColumn == null || typeColumn.isStringColumn() ? "types"
                : isBigDecimalType(typeColumn)
                ? "types == null ? null : types.map(value -> value.toPlainString())"
                : "types == null ? null : types.map(value -> String.valueOf(value))";
        selectByKeysDictMethod.addBodyLine("return queryByKeysDict(" + types + ", " + keys + ");");
        parentElement.addMethod(selectByKeysDictMethod);

        Method prefetchMethod = serviceMethods.getSelectByKeysDictForPrefetchMethod(
                parentElement, voCacheGeneratorConfiguration, false);
        prefetchMethod.addAnnotation("@Override");
        prefetchMethod.addBodyLine("return queryByKeysDict(types, keys);");
        parentElement.addMethod(prefetchMethod);
        Method queryMethod = serviceMethods.getQueryByKeysDictMethod(parentElement, voCacheGeneratorConfiguration);
        addSelectByKeysDictBody(queryMethod, typeCacheMappingType);
        parentElement.addMethod(queryMethod);
        Mb3GenUtil.injectionMappingsInstance(parentElement,typeCacheMappingType);
        parentElement.addImportedType(typeCacheMappingType);
        parentElement.addImportedType("java.util.ArrayList");
    }

    private boolean isBigDecimalType(IntrospectedColumn column) {
        return "java.math.BigDecimal".equals(column.getFullyQualifiedJavaType().getFullyQualifiedName());
    }

    private void addSelectByKeysDictBody(Method method, FullyQualifiedJavaType typeCacheMappingType) {
        method.addBodyLine("SelDictByKeysParam selDictByKeysParam = new SelDictByKeysParam();");
        method.addBodyLine("selDictByKeysParam.addKeyString(keys);");
        method.addBodyLine("if (types!=null && types.isPresent()) {");
        method.addBodyLine("types.ifPresent(selDictByKeysParam::addTypeString);");
        method.addBodyLine("}");
        method.addBodyLine("List<{0}> result = mapper.{1}(selDictByKeysParam);"
                , entityType.getShortName()
                , introspectedTable.getSelectByKeysDictStatementId());
        method.addBodyLine("if (!result.isEmpty()) {");
        method.addBodyLine("return {0}.to{1}CachePos(result);"
                ,typeCacheMappingType.getShortNameFirstLowCase()+"Impl", entityType.getShortName());
        method.addBodyLine("}else{");
        method.addBodyLine("return new ArrayList<>();");
        method.addBodyLine("}");
    }
}

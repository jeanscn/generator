package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.config.VoCacheGeneratorConfiguration;
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
        cacheAnnotationDesc.setParameters(parameters);
        cacheAnnotationDesc.setUnless("#result==null || #result instanceof T(java.lang.Exception) || #result.getResult()==null || #result.getResult().isEmpty()");
        selectByKeysDictMethod.addAnnotation(cacheAnnotationDesc.toCacheableAnnotation());
        //方法体
        //selectByKeysDictMethod.addBodyLine("{0}Mappings mappings = {0}Mappings.INSTANCE;", entityType.getShortName() + ConstantsUtil.MAPPINGS_CACHE_PO_KEY);
        selectByKeysDictMethod.addBodyLine("SelDictByKeysParam selDictByKeysParam = new SelDictByKeysParam();");
        String keyColumn = voCacheGeneratorConfiguration.getKeyColumn();
        IntrospectedColumn column = introspectedTable.getColumn(keyColumn).orElse(null);
        if (column != null && column.isStringColumn()) {
            selectByKeysDictMethod.addBodyLine("selDictByKeysParam.addKeyString(keys);");
        } else  if(column != null && column.isBigDecimalColumn()){
            selectByKeysDictMethod.addBodyLine("selDictByKeysParam.addKeyString(keys.toPlainString());");
        } else {
            selectByKeysDictMethod.addBodyLine("selDictByKeysParam.addKeyString(String.valueOf(keys));");
        }
        selectByKeysDictMethod.addBodyLine("if (types!=null && types.isPresent()) {");
        selectByKeysDictMethod.addBodyLine("types.ifPresent(selDictByKeysParam::addTypeString);");
        selectByKeysDictMethod.addBodyLine("}");
        selectByKeysDictMethod.addBodyLine("List<{0}> result = mapper.{1}(selDictByKeysParam);"
                , entityType.getShortName()
                , introspectedTable.getSelectByKeysDictStatementId());
        selectByKeysDictMethod.addBodyLine("if (!result.isEmpty()) {");
        selectByKeysDictMethod.addBodyLine("return ServiceResult.success({0}.to{1}CachePos(result));"
                ,typeCacheMappingType.getShortNameFirstLowCase()+"Impl", entityType.getShortName());
        selectByKeysDictMethod.addBodyLine("}else{");
        selectByKeysDictMethod.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN, \"未查询到数据\");");
        selectByKeysDictMethod.addBodyLine("}");
        parentElement.addMethod(selectByKeysDictMethod);
        Mb3GenUtil.injectionMappingsInstance(parentElement,typeCacheMappingType);
        parentElement.addImportedType(typeCacheMappingType);
    }
}

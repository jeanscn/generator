package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public class HideBatchElementGenerator extends AbstractControllerElementGenerator {

    public HideBatchElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        final String methodPrefix = "hideListBatch";
        Method method = createMethod(methodPrefix);
        method.setVisibility(JavaVisibility.PUBLIC);
        FullyQualifiedJavaType response = new FullyQualifiedJavaType(RESPONSE_RESULT);
        FullyQualifiedJavaType retType = FullyQualifiedJavaType.getNewListInstance();
        retType.addTypeArgument(new FullyQualifiedJavaType("com.vgosoft.system.pojo.vo.SysPerFilterOutBinVo"));
        response.addTypeArgument(retType);
        method.setReturnType(response);
        method.setReturnRemark("创建的隐藏数据列表Vo的响应对象");
        method.addAnnotation(new RequestMappingDesc("hide", RequestMethodEnum.POST),parentElement);
        method.addAnnotation(new ApiOperationDesc("批量隐藏", "批量设置列表中选择的数据为隐藏"),parentElement);

        // 判断是否有Vo对象
        String entityVar = entityType.getShortNameFirstLowCase();
        String entityVoVar = entityVoType.getShortNameFirstLowCase();
        boolean generateVoModel = introspectedTable.getRules().isGenerateVoModel();
        if (!generateVoModel) {
            entityVoType = entityType;
            entityVoVar = entityVar;
        }
        Parameter parameter = new Parameter(new FullyQualifiedJavaType("List<" + entityVoType.getShortName() + ">"), entityVoVar + "s");
        parameter.addAnnotation("@RequestBody");
        parameter.setRemark("待创建的数据对象列表");
        method.addParameter(parameter);
        commentGenerator.addMethodJavaDocLine(method, "批量设置列表中选择的数据为隐藏");
        FullyQualifiedJavaType binMappingsType = new FullyQualifiedJavaType("com.vgosoft.system.pojo.maps.SysPerFilterOutBinMappings");
        method.addBodyLine("List<SysPerFilterOutBin> sysPerFilterOutBins = mappings.toSysPerFilterOutBins(mappings.from{0}s({1}));",entityVoType.getShortName(),entityVoVar + "s");
        method.addBodyLine("sysPerFilterOutBins.forEach(sysPerFilterOutBin -> sysPerFilterOutBin.setOperateUserId(sysPerFilterOutBin.getOperateUserId()));");
        method.addBodyLine("ServiceResult<List<SysPerFilterOutBin>> listServiceResult = sysPerFilterOutBinImpl.insertBatch(sysPerFilterOutBins);");
        method.addBodyLine("if (listServiceResult.hasResult()) {");
        method.addBodyLine("return success({0}.toSysPerFilterOutBinVos(sysPerFilterOutBins));",binMappingsType.getShortNameFirstLowCase()+"Impl");
        method.addBodyLine("}else{");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_CUSTOM,listServiceResult.getMessage());");
        method.addBodyLine("}");
        parentElement.addMethod(method);

        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        parentElement.addImportedType(entityType);
        parentElement.addImportedType(new FullyQualifiedJavaType("com.vgosoft.system.pojo.vo.SysPerFilterOutBinVo"));
        parentElement.addImportedType(new FullyQualifiedJavaType("com.vgosoft.system.entity.SysPerFilterOutBin"));
        parentElement.addImportedType(entityVoType);
        parentElement.addImportedType(new FullyQualifiedJavaType("com.vgosoft.system.pojo.maps.SysPerFilterOutBinMappings"));

        FullyQualifiedJavaType iSysPerFilterOutBin = new FullyQualifiedJavaType("com.vgosoft.system.service.ISysPerFilterOutBin");
        Field sysPerFilterOutBinImpl = new Field("sysPerFilterOutBinImpl",iSysPerFilterOutBin );
        sysPerFilterOutBinImpl.addAnnotation("@Resource");
        sysPerFilterOutBinImpl.setVisibility(JavaVisibility.PROTECTED);
        parentElement.addField(sysPerFilterOutBinImpl);
        parentElement.addImportedType(iSysPerFilterOutBin);
        Mb3GenUtil.injectionMappingsInstance(parentElement,binMappingsType);
    }
}

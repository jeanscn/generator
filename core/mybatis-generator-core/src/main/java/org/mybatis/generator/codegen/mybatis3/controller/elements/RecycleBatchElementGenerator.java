package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public class RecycleBatchElementGenerator extends AbstractControllerElementGenerator {

    public RecycleBatchElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        parentElement.addImportedType(entityType);
        final String methodPrefix = "recycleBatch";
        Method method = createMethod(methodPrefix);
        method.setVisibility(JavaVisibility.PUBLIC);
        Mb3GenUtil.addTransactionalAnnotation(parentElement,method,"READ_COMMITTED");
        FullyQualifiedJavaType response = new FullyQualifiedJavaType(RESPONSE_RESULT);
        response.addTypeArgument(new FullyQualifiedJavaType("java.lang.Long"));
        method.setReturnType(response);
        method.setReturnRemark("成功移入回收站的记录数");
        method.addAnnotation(new SystemLogDesc("把一条或多条记录移入回收站",introspectedTable),parentElement);
        method.addAnnotation(new RequestMappingDesc("recycle", RequestMethodEnum.POST),parentElement);
        addSecurityPreAuthorize(method,methodPrefix,"移入回收站");
        method.addAnnotation(new ApiOperationDesc("批量移入回收站", "把给定的对象批量移入回收站"),parentElement);
        commentGenerator.addMethodJavaDocLine(method, "批量移入回收站");
        FullyQualifiedJavaType exceptionType = new FullyQualifiedJavaType("java.lang.Exception");
        method.addException(exceptionType);
        method.setExceptionRemark("异常");
        parentElement.addImportedType(exceptionType);

        // 判断是否有VO对象
        String entityVar = entityType.getShortNameFirstLowCase();
        String entityVoVar = entityVoType.getShortNameFirstLowCase();
        boolean generateVoModel = introspectedTable.getRules().isGenerateVoModel();
        if (!generateVoModel) {
            entityVoType = entityType;
            entityVoVar = entityVar;
        }
        Parameter parameter = new Parameter(new FullyQualifiedJavaType("List<" + entityVoType.getShortName() + ">"), entityVoVar + "s");
        parameter.addAnnotation("@RequestBody");
        parameter.setRemark("待移入回收站的数据对象列表");
        method.addParameter(parameter);
        method.addBodyLine("List<String> ids = {0}s.stream().map({1}::getId).filter(Objects::nonNull).distinct().toList();",entityVoVar,entityVoType.getShortName());
        method.addBodyLine("List<{0}> {1}s = {1}Impl.selectByPrimaryKeys(String.join(\",\", ids));",entityType.getShortName(),entityVar);
        method.addBodyLine("");
        method.addBodyLine("List<SysRecycleBin> sysRecycleBins = mappings.toSysRecycleBins({0}s);",entityType.getShortNameFirstLowCase());
        method.addBodyLine("sysRecycleBins.forEach(sysRecycleBin -> sysRecycleBin.setOperateUserId(getCurrentUser().getId()));");
        method.addBodyLine("ServiceResult<List<SysRecycleBin>> result = sysRecycleBinImpl.insertBatch(sysRecycleBins);");
        method.addBodyLine("");
        method.addBodyLine(" if (result.hasResult()) {");
        method.addBodyLine("List<SysRecycleBinRecord> records = sysRecycleBinRecordImpl.createSysRecycleBinRecordFromBusiness({0}s,{1}s,null);",entityVar,entityVoVar);
        method.addBodyLine("ServiceResult<List<SysRecycleBinRecord>> listServiceResult = sysRecycleBinRecordImpl.insertBatch(records);");
        method.addBodyLine("if (listServiceResult.hasResult()) {");
        method.addBodyLine("{0}Example example = new {0}Example();",entityType.getShortName());
        method.addBodyLine("example.createCriteria().andIdIn(ids);");
        method.addBodyLine("ServiceResult<Integer> deleteResult = {0}Impl.deleteByExample(example);",entityVar);
        method.addBodyLine("if (deleteResult.hasResult() && deleteResult.getResult() > 0) {");
        method.addBodyLine("");
        method.addBodyLine("publisher.publishEvent({0}s, EntityEventEnum.RECYCLED);",entityVar);
        method.addBodyLine("");
        method.addBodyLine("return success((long) deleteResult.getResult(), deleteResult.getAffectedRows());");
        method.addBodyLine("} else {");
        method.addBodyLine("throw new VgoException(\"清除原数据失败\");");
        method.addBodyLine("}");
        method.addBodyLine("} else {");
        method.addBodyLine("throw new VgoException(\"回收站记录插入失败\");");
        method.addBodyLine("}");
        method.addBodyLine("} else {");
        method.addBodyLine("throw new VgoException(\"回收站记录插入失败\");");
        method.addBodyLine("}");
        parentElement.addMethod(method);

        parentElement.addImportedType(new FullyQualifiedJavaType("java.util.Objects"));
        parentElement.addImportedType(new FullyQualifiedJavaType("com.vgosoft.core.exception.VgoException"));
        parentElement.addImportedType(new FullyQualifiedJavaType("com.vgosoft.system.entity.SysRecycleBin"));
        parentElement.addImportedType(new FullyQualifiedJavaType("com.vgosoft.system.entity.SysRecycleBinRecord"));
        FullyQualifiedJavaType sysRecycleBinType = new FullyQualifiedJavaType("com.vgosoft.system.service.ISysRecycleBin");
        Field sysRecycleBinImpl = new Field("sysRecycleBinImpl",sysRecycleBinType );
        sysRecycleBinImpl.addAnnotation("@Resource");
        sysRecycleBinImpl.setVisibility(JavaVisibility.PROTECTED);
        parentElement.addField(sysRecycleBinImpl);
        parentElement.addImportedType(sysRecycleBinType);

        FullyQualifiedJavaType sysRecycleBinRecordType = new FullyQualifiedJavaType("com.vgosoft.system.service.ISysRecycleBinRecord");
        Field sysRecycleBinRecordImpl = new Field("sysRecycleBinRecordImpl", sysRecycleBinRecordType);
        sysRecycleBinRecordImpl.addAnnotation("@Resource");
        sysRecycleBinRecordImpl.setVisibility(JavaVisibility.PROTECTED);
        parentElement.addField(sysRecycleBinRecordImpl);
        parentElement.addImportedType(sysRecycleBinRecordType);

        FullyQualifiedJavaType qualifiedJavaType = new FullyQualifiedJavaType("com.vgosoft.core.event.entity.EntityEventPublisher");
        Field publisher = new Field("publisher", qualifiedJavaType);
        publisher.addAnnotation("@Resource");
        publisher.setVisibility(JavaVisibility.PROTECTED);
        parentElement.addField(publisher);
        parentElement.addImportedType(qualifiedJavaType);
        parentElement.addImportedType(new FullyQualifiedJavaType("com.vgosoft.core.constant.enums.core.EntityEventEnum"));
    }
}

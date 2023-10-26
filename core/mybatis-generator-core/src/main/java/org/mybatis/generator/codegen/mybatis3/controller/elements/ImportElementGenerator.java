package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethod;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;

import static org.mybatis.generator.custom.ConstantsUtil.MULTIPART_FILE;
import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public class ImportElementGenerator extends AbstractControllerElementGenerator {

    public ImportElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        FullyQualifiedJavaType responseResult = new FullyQualifiedJavaType(RESPONSE_RESULT);
        FullyQualifiedJavaType multipartFile = new FullyQualifiedJavaType(MULTIPART_FILE);
        parentElement.addImportedType("com.vgosoft.plugins.excel.service.VgoEasyExcel");
        parentElement.addImportedType(entityExcelImportVoType);
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        parentElement.addImportedType(multipartFile);
        parentElement.addImportedType(responseResult);

        String listEntityVar = entityType.getShortNameFirstLowCase() + "s";
        final String methodPrefix = "import";
        Method method = createMethod(methodPrefix);

        Parameter multipartFileParameter = new Parameter(multipartFile, "file");
        multipartFileParameter.addAnnotation("@RequestPart(\"file\")");
        multipartFileParameter.setRemark("文件上传对象");
        method.addParameter(multipartFileParameter);
        responseResult.addTypeArgument(new FullyQualifiedJavaType("java.lang.Integer"));
        method.setReturnType(responseResult);
        method.setReturnRemark("成功导入行数");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        method.setExceptionRemark("导入处理异常，含IO读写异常");

        method.addAnnotation(new SystemLogDesc("数据导入",introspectedTable),parentElement);
        RequestMappingDesc requestMappingDesc = new RequestMappingDesc("import", RequestMethod.POST);
        requestMappingDesc.addConsumes("MediaType.MULTIPART_FORM_DATA_VALUE");
        requestMappingDesc.addProduces("MediaType.APPLICATION_JSON_VALUE");
        method.addAnnotation(requestMappingDesc,parentElement);
        parentElement.addImportedType("org.springframework.http.MediaType");
        addSecurityPreAuthorize(method,methodPrefix,"导入");
        method.addAnnotation(new ApiOperationDesc("Excel数据导入", "Excel数据导入接口"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "Excel数据导入");

        //方法体
        method.addBodyLine("int ret=0;");
        method.addBodyLine("List<{0}> excelVOS = VgoEasyExcel.read(file, {0}.class);",entityExcelImportVoType.getShortName());
        method.addBodyLine("List<{0}> {1} = mappings.from{2}s(excelVOS);",entityType.getShortName(),listEntityVar,entityExcelImportVoType.getShortName());

        method.addBodyLine("for ({0} {1} : {1}s) '{'\n" +
                "            ServiceResult<{0}> insert = {1}Impl.insertOrUpdate({1});\n" +
                "            if (insert.isSuccess()) '{'\n" +
                "                ret = ret+1;\n" +
                "            '}'\n" +
                "        '}'\n" +
                "        return ResponseResult.success(ret,\"导入成功\"+ret+\"/\"+excelVOS.size());",entityType.getShortName(),entityType.getShortNameFirstLowCase());
        parentElement.addMethod(method);
    }


}

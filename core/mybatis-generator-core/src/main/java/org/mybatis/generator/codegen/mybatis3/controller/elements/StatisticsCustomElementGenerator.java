package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.SymbolConstants;
import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import com.vgosoft.core.constant.enums.data.StatTimeDimEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.config.StatisticsCustomGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.enums.ReturnTypeEnum;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

/**
 * @author cen_c
 */
public class StatisticsCustomElementGenerator extends AbstractControllerElementGenerator {

    public StatisticsCustomElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        if (!introspectedTable.getRules().isGenerateCustomStatisticsElement()) {
            return;
        }
        Set<StatisticsCustomGeneratorConfiguration> statisticsConfigurations = introspectedTable.getTableConfiguration().getJavaServiceImplGeneratorConfiguration().getStatisticsCustomConfigurations();
        parentElement.addImportedType(SERVICE_RESULT);
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("java.util.Map<String, Object>");
        parentElement.addImportedType(returnType.getFullyQualifiedNameWithoutTypeParameters());
        for (StatisticsCustomGeneratorConfiguration configuration : statisticsConfigurations) {
            if (!configuration.getGenControllerMethod()) {
                continue;
            }
            IntrospectedColumn statColumn = introspectedTable.getColumn(configuration.getStatColumns().get(0)).orElse(null);
            IntrospectedColumn joinColumn = introspectedTable.getColumn(configuration.getDateDimColumn()).orElse(null);
            if (statColumn == null || joinColumn == null) {
                continue;
            }
            String remark = VStringUtil.format("统计{0}总量,该接口用于统计在指定时间范围内，按照{1}对{0}进行汇总。可以指定最小的时间维度（如按月或按年）",
                    statColumn.getRemarks(true),
                    joinColumn.getRemarks(true));

            Method method = new Method(configuration.getMethodName());
            method.addAnnotation(new RequestMappingDesc(VStringUtil.toHyphenCase(configuration.getMethodName()), RequestMethodEnum.GET),parentElement);
            method.setVisibility(JavaVisibility.PROTECTED);
            addSecurityPreAuthorize(method,"statistics","统计数据");
            method.addAnnotation(new ApiOperationDesc("获取汇总数据",remark),parentElement);
            FullyQualifiedJavaType response = getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST, returnType, parentElement);
            method.setReturnRemark("统计结果列表");
            method.setReturnType(response);

            String defaultTimeDim = VStringUtil.stringHasValue(configuration.getDateDimension()) ? configuration.getDateDimension() : StatTimeDimEnum.MONTH.code();
            String[] pNames;
            if (isContainDateDim(configuration)) {
                pNames = new String[]{"startDate", "endDate", "minDimension","pageNo","pageSize"};
            } else {
                pNames = new String[]{"startDate", "endDate","pageNo","pageSize"};
            }
            //添加参数
            for (String pName : pNames) {
                Parameter parameter = new Parameter(FullyQualifiedJavaType.getStringInstance(), pName);
                switch (pName){
                    case "startDate":
                        parameter.setRemark("开始时间，格式为yyyy-MM-dd");
                        parameter.addAnnotation("@RequestParam");
                        break;
                    case "endDate":
                        parameter.setRemark("结束时间，格式为yyyy-MM-dd");
                        parameter.addAnnotation("@RequestParam");
                        break;
                    case "minDimension":
                        parameter.setRemark("最小时间维度，枚举值：" + Arrays.stream(StatTimeDimEnum.values()).map(StatTimeDimEnum::code)
                                .collect(Collectors.joining("|")) + "，默认值为" + defaultTimeDim);
                        parameter.addAnnotation("@RequestParam(required = false, defaultValue = \""+defaultTimeDim+"\")");
                        break;
                    case "pageNo":
                        parameter.setRemark("页码，默认值0.表示不传分页");
                        parameter.setType(FullyQualifiedJavaType.getIntInstance());
                        parameter.addAnnotation("@RequestParam(required = false, defaultValue = \"0\")");
                        break;
                    case "pageSize":
                        parameter.setType(FullyQualifiedJavaType.getIntInstance());
                        parameter.setRemark("每页记录数，默认值0,表示不传分页");
                        parameter.addAnnotation("@RequestParam(required = false, defaultValue = \"0\")");
                        break;
                    default:
                        parameter.setRemark("");
                }
                method.addParameter(parameter);
            }
            commentGenerator.addMethodJavaDocLine(method, remark);
            method.addBodyLine("if (!VDateUtils.isValidDate(startDate, VDateUtils.CHINESE_PATTERN) || !VDateUtils.isValidDate(endDate, VDateUtils.CHINESE_PATTERN)) {\n" +
                    "            return failure(ApiCodeEnum.FAIL_CUSTOM, \"日期格式错误，请使用yyyy-MM-dd格式\");\n" +
                    "        }\n" +
                    "        if (VDateUtils.compareDate(startDate, endDate) > 0) {\n" +
                    "            return failure(ApiCodeEnum.FAIL_CUSTOM, \"开始日期不能晚于结束日期\");\n" +
                    "        }");
            method.addBodyLine("if (pageNo != 0 && pageSize != 0) {");
            method.addBodyLine("PageHelper.startPage(pageNo, pageSize);");
            if (isContainDateDim(configuration)) {
                method.addBodyLine("List<Map<String,Object>> statDataList = {0}.{1}(startDate, endDate, minDimension);", serviceBeanName, configuration.getMethodName());
            } else {
                method.addBodyLine("List<Map<String,Object>> statDataList = {0}.{1}(startDate, endDate, null);", serviceBeanName, configuration.getMethodName());
            }
            method.addBodyLine("Page<Map<String,Object>> statDataPage = (Page<Map<String,Object>>) statDataList;");
            method.addBodyLine("return ResponsePagehelperResult.success(statDataList, statDataPage);");
            method.addBodyLine("} else {");
            if (isContainDateDim(configuration)) {
                method.addBodyLine("List<Map<String,Object>> statDataList = {0}.{1}(startDate, endDate, minDimension);", serviceBeanName, configuration.getMethodName());
            } else {
                method.addBodyLine("List<Map<String,Object>> statDataList = {0}.{1}(startDate, endDate, null);", serviceBeanName, configuration.getMethodName());
            }
            method.addBodyLine("return success(statDataList);");
            method.addBodyLine("}");
            parentElement.addMethod(method);
            parentElement.addImportedType("com.vgosoft.tool.core.VDateUtils");
            parentElement.addImportedType("com.github.pagehelper.PageHelper");
            parentElement.addImportedType("com.github.pagehelper.Page");
            parentElement.addImportedType("java.util.List");
            parentElement.addImportedType("com.vgosoft.mybatis.pojo.ResponsePagehelperResult");
        }
    }

    private static boolean isContainDateDim(StatisticsCustomGeneratorConfiguration configuration) {
        return VStringUtil.stringHasValue(configuration.getDateDimension()) && !SymbolConstants.NONE.equalsIgnoreCase(configuration.getDateDimension());
    }
}

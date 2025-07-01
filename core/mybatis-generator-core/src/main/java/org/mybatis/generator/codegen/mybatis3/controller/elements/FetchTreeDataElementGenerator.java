package org.mybatis.generator.codegen.mybatis3.controller.elements;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;
import static org.mybatis.generator.custom.ConstantsUtil.Z_TREE_DATA_SIMPLE;
import static org.mybatis.generator.custom.ConstantsUtil.Z_TREE_DATA_SIMPLE_CATE;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;
import org.mybatis.generator.custom.enums.ReturnTypeEnum;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.tool.core.VStringUtil;

public class FetchTreeDataElementGenerator extends AbstractControllerElementGenerator {

    public FetchTreeDataElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType("com.vgosoft.core.pojo.ztree.cast.TreeNodeConverterImpl");
        parentElement.addImportedType("com.vgosoft.core.pojo.ztree.cast.ITreeNodeConverter");
        parentElement.addImportedType("java.util.List");
        parentElement.addImportedType("java.util.Optional");
        parentElement.addImportedType("java.util.stream.Collectors");
        parentElement.addStaticImport("com.vgosoft.tool.core.VStringUtil.*");

        /*
         * fetchTreeData方法定义
         */
        FullyQualifiedJavaType ztreeDataSimpleType = new FullyQualifiedJavaType(Z_TREE_DATA_SIMPLE);
        parentElement.addImportedType(ztreeDataSimpleType);
        final String methodPrefix = "fetchTreeData";
        Method method = createMethod(methodPrefix);
        List<Parameter> parameters = new ArrayList<>();
        Parameter parameter = new Parameter(FullyQualifiedJavaType.getStringInstance(), "keys");
        parameter.addAnnotation("@RequestParam(required = false)");
        parameter.setRemark("要返回的树型记录的关键字或主键标识列表");
        parameters.add(parameter);
        Parameter parameter3 = new Parameter(FullyQualifiedJavaType.getStringInstance(), "eids");
        parameter3.addAnnotation("@RequestParam(required = false)");
        parameter3.setRemark("要排除的标识列表");
        parameters.add(parameter3);
        parameters.forEach(method::addParameter);
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST,
                ztreeDataSimpleType,
                parentElement));
        method.setReturnRemark("tree对象集合结果封装对象");
        method.addAnnotation(new SystemLogDesc("查询树结果数据", introspectedTable), parentElement);
        method.addAnnotation(new RequestMappingDesc("tree", RequestMethodEnum.GET), parentElement);
        method.addAnnotation(new ApiOperationDesc("树形数据查询", "获取指定根或所有的树形结构数据"), parentElement);
        commentGenerator.addMethodJavaDocLine(method, "获取指定根或所有的树形结构数据");
        //函数体
        method.addBodyLine("ITreeNodeConverter<{0}> nodeConverter = new TreeNodeConverterImpl<>();", entityType.getShortName());
        method.addBodyLine("List<{0}> records = {1}.{2}(keys);",entityType.getShortName(), serviceBeanName,introspectedTable.getSelectByPrimaryKeysStatementId());
        boolean isPresent = introspectedTable.getColumn(DefaultColumnNameEnum.SORT.columnName()).isPresent();
        if (isPresent) {
            method.addBodyLine("nodeConverter.setRecords(records.stream().sorted(Comparator.comparing({0}::getSort)).collect(Collectors.toList()));", entityType.getShortName());
            parentElement.addImportedType("java.util.Comparator");
        }else{
            method.addBodyLine("nodeConverter.setRecords(records);");
        }
        method.addBodyLine("List<ZtreeDataSimple> ztreeDataSimples = nodeConverter.convertTreeNodeDataSimple();");
        method.addBodyLine("if (VStringUtil.stringHasValue(eids)) {");
        method.addBodyLine("VStringUtil.splitter(true).splitToList(eids)");
        method.addBodyLine(".forEach(pid -> TreeUtil.setNoCheckedRecursively(ztreeDataSimples, pid));");
        method.addBodyLine("}");
        method.addBodyLine("return ResponseResult.success(ztreeDataSimples);");
        parentElement.addMethod(method);
        parentElement.addImportedType("com.vgosoft.web.plugins.ztree.TreeUtil");

        /*
         * fetchTreeCateGenerate方法定义
         * 生成树形分类数据查询方法
         */
        tc.getJavaControllerGeneratorConfiguration().getTreeViewCateGeneratorConfigurations().forEach(cateTreeConfig -> {
            FullyQualifiedJavaType ztreeDataSimpleCateType = new FullyQualifiedJavaType(Z_TREE_DATA_SIMPLE_CATE);
            parentElement.addImportedType(ztreeDataSimpleCateType);
            final String mPrefix = "fetchTreeCate" + JavaBeansUtil.getCamelCaseString(cateTreeConfig.getPathKeyWord(), true);
            Method methodCate = createMethod(mPrefix);
            parameters.forEach(methodCate::addParameter);
            methodCate.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST,
                    ztreeDataSimpleCateType,
                    parentElement));
            methodCate.setReturnRemark("treeCate对象集合结果封装对象");
            //methodCate.addAnnotation(new SystemLog("查询分类树结果数据", introspectedTable), parentElement);
            String hyphenCase = VStringUtil.toHyphenCase(cateTreeConfig.getPathKeyWord());
            methodCate.addAnnotation(new RequestMappingDesc(hyphenCase, RequestMethodEnum.GET), parentElement);
            methodCate.addAnnotation(new ApiOperationDesc("树形分类数据查询", "获取指定根或所有的树形分类结构数据"), parentElement);
            commentGenerator.addMethodJavaDocLine(methodCate, "获取指定根或所有的树形结构数据");
            //函数体
            methodCate.addBodyLine("ITreeNodeConverter<{0}> nodeConverter = new TreeNodeConverterImpl<>();", entityType.getShortName());
            if (!(cateTreeConfig.getIdProperty().equals(DefaultColumnNameEnum.ID.fieldName()))) {
                methodCate.addBodyLine(VStringUtil.format("nodeConverter.setIdPropertyName(\"{0}\");", cateTreeConfig.getIdProperty()));
            }
            if (!(cateTreeConfig.getNameProperty().equals(DefaultColumnNameEnum.NAME.fieldName()))) {
                methodCate.addBodyLine(VStringUtil.format("nodeConverter.setNamePropertyName(\"{0}\");", cateTreeConfig.getNameProperty()));
            }
            methodCate.addBodyLine(VStringUtil.format("nodeConverter.setRecords({0}.{1}({2}));",
                    serviceBeanName,
                    introspectedTable.getSelectByPrimaryKeysStatementId(),
                    "keys"));

            methodCate.addBodyLine("List<ZtreeDataViewCate> ztreeDataViewCates = nodeConverter.convertTreeNodeDataViewCate(\"{0}\");", cateTreeConfig.getSPeL());
            methodCate.addBodyLine("if (VStringUtil.stringHasValue(eids)) {");
            methodCate.addBodyLine("List<String> split = VStringUtil.splitter(true).splitToList(eids);");
            methodCate.addBodyLine("ztreeDataViewCates = ztreeDataViewCates.stream().filter(t -> !split.contains(t.getId())).collect(Collectors.toList());");
            methodCate.addBodyLine("}");
            methodCate.addBodyLine("return ResponseResult.success(ztreeDataViewCates);");
            parentElement.addMethod(methodCate);
        });


        /*
         * 异步 asyncFetchTreeData方法定义
         */
        final String asyncMethodPrefix = "asyncFetchTreeData";
        Method aMethod = createMethod(asyncMethodPrefix);
        Parameter parameter4 = new Parameter(new FullyQualifiedJavaType("java.lang.Boolean"), "returnPath");
        parameter4.addAnnotation("@RequestParam(required = false)");
        parameter4.setRemark("是否返回路径");
        parameters.add(parameter4);
        parameters.forEach(aMethod::addParameter);
        aMethod.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST,
                entityType,
                parentElement));
        aMethod.setReturnRemark("对象集合结果封装对象,带有子集合数量");
        aMethod.addAnnotation(new SystemLogDesc("查询带有子集合数量数据", introspectedTable), parentElement);
        aMethod.addAnnotation(new RequestMappingDesc("async-tree", RequestMethodEnum.GET), parentElement);
        aMethod.addAnnotation(new ApiOperationDesc("带有子集合数量数据查询", "异步获取指定根或所有的带有子集合数量数据，用于异步树获取数据"), parentElement);
        commentGenerator.addMethodJavaDocLine(aMethod, "异步获取指定根或所有的带有子集合数量数据，用于异步树获取数据");
        //函数体
        aMethod.addBodyLine("{0}Example example = new {0}Example();", entityType.getShortName());
        aMethod.addBodyLine("keys = keys == null ? \"0\" : keys;");
        aMethod.addBodyLine("List<String> parentIds = splitToList(keys);");
        aMethod.addBodyLine("example.createCriteria().andParentIdIn(parentIds);");
        aMethod.addBodyLine(" if (keys.equals(\"0\")) {");
        aMethod.addBodyLine("example.or(example.createCriteria().andParentIdIsNull());");
        aMethod.addBodyLine("}");
        if (introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration().isGenerateChildren()) {
            aMethod.addBodyLine("ServiceResult<List<{0}>> serviceResult = {1}.selectByExampleWithChildrenCount(example);",entityType.getShortName(),serviceBeanName);
            aMethod.addBodyLine("if (!serviceResult.hasResult() || serviceResult.getResult().isEmpty()) {");
            aMethod.addBodyLine("return ResponseResult.success(new ArrayList<>());");
            aMethod.addBodyLine("}");
            aMethod.addBodyLine("List<{0}> result = serviceResult.getResult();",entityType.getShortName());
            aMethod.addBodyLine("result.forEach(record -> {");
            aMethod.addBodyLine("if (record.getChildrenCount() == 0L) record.setLeaf(true);");
            aMethod.addBodyLine("});");
        } else {
            aMethod.addBodyLine("List<{0}> result = {1}.selectByExample(example).getResult();",entityType.getShortName(), serviceBeanName);
            aMethod.addBodyLine("if (result.isEmpty()) {");
            aMethod.addBodyLine("return ResponseResult.success(new ArrayList<>());");
            aMethod.addBodyLine("}");
        }
        aMethod.addBodyLine("List<List<String>> pathValues = new ArrayList<>();");
        aMethod.addBodyLine("if (VStringUtil.stringHasValue(eids) && returnPath != null && returnPath) {");
        aMethod.addBodyLine("List<String> split = splitter(true).splitToList(eids);");
        aMethod.addBodyLine("for (String id : split) {");
        aMethod.addBodyLine("ServiceResult<List<{0}>> listServiceResult = {1}.selectByKeysWithAllParent(Collections.singletonList(id));",entityType.getShortName(), serviceBeanName);
        aMethod.addBodyLine("if (listServiceResult.hasResult()) {");
        aMethod.addBodyLine("result.addAll(listServiceResult.getResult());");
        aMethod.addBodyLine("List<String> path = listServiceResult.getResult().stream()");
        aMethod.addBodyLine("        .map({0}::getId).collect(Collectors.toList());", entityType.getShortName());
        aMethod.addBodyLine("pathValues.add(path);");
        aMethod.addBodyLine("}");
        aMethod.addBodyLine("}");
        aMethod.addBodyLine("}");
        aMethod.addBodyLine("ResponseResult<List<{0}>> success = success(result.stream().distinct().collect(Collectors.toList()));", entityType.getShortName());
        aMethod.addBodyLine("success.addAttribute(\"pathValues\", pathValues);");
        aMethod.addBodyLine("return success;");
        parentElement.addMethod(aMethod);
        parentElement.addImportedType("java.util.stream.Collectors");
        parentElement.addImportedType("java.util.ArrayList");
        parentElement.addImportedType("java.util.List");
    }
}

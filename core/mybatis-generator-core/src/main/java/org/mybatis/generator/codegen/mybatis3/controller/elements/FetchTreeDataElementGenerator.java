package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.core.constant.enums.core.RequestMethod;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.custom.ConstantsUtil.*;

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
        parentElement.addImportedType("java.util.stream.Stream");

        //fetchTreeDataGenerate方法定义
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
        method.addAnnotation(new RequestMappingDesc("tree", RequestMethod.POST), parentElement);
        method.addAnnotation(new ApiOperationDesc("树形数据查询", "获取指定根或所有的树形结构数据"), parentElement);
        commentGenerator.addMethodJavaDocLine(method, "获取指定根或所有的树形结构数据");

        //函数体
        method.addBodyLine("ITreeNodeConverter<{0}> nodeConverter = new TreeNodeConverterImpl<>();", entityType.getShortName());
        method.addBodyLine(VStringUtil.format("nodeConverter.setRecords({0}.{1}({2}));",
                serviceBeanName,
                introspectedTable.getSelectByPrimaryKeysStatementId(),
                "keys"));
        method.addBodyLine("List<ZtreeDataSimple> ztreeDataSimples = nodeConverter.convertTreeNodeDataSimple();");
        method.addBodyLine("if (VStringUtil.stringHasValue(eids)) {");
        method.addBodyLine("List<String> split = VStringUtil.splitter(true).splitToList(eids);");
        method.addBodyLine("ztreeDataSimples = ztreeDataSimples.stream().filter(t -> !split.contains(t.getId())).collect(Collectors.toList());");
        method.addBodyLine("}");
        method.addBodyLine("return ResponseResult.success(ztreeDataSimples);");
        parentElement.addMethod(method);

        //fetchTreeCateGenerate方法定义
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
            methodCate.addAnnotation(new RequestMappingDesc(hyphenCase, RequestMethod.POST), parentElement);
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
    }

    private void addExcludeIdsData() {

    }
}

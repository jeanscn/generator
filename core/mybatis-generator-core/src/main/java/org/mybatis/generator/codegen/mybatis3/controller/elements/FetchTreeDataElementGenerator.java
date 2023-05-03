package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.RequestMethod;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.config.TreeViewCateGeneratorConfiguration;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.ApiOperation;
import org.mybatis.generator.custom.annotations.RequestMapping;
import org.mybatis.generator.custom.annotations.SystemLog;

import static org.mybatis.generator.custom.ConstantsUtil.*;

public class FetchTreeDataElementGenerator extends AbstractControllerElementGenerator {

    public FetchTreeDataElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);

        //fetchTreeDataGenerateDemoTree方法定义
        FullyQualifiedJavaType ztreeDataSimpleType = new FullyQualifiedJavaType(Z_TREE_DATA_SIMPLE);
        parentElement.addImportedType(ztreeDataSimpleType);
        final String methodPrefix = "fetchTreeData";
        Method method = createMethod(methodPrefix);
        Parameter parameter = new Parameter(FullyQualifiedJavaType.getStringInstance(), "ids");
        parameter.addAnnotation("@RequestParam(required = false)");
        parameter.setRemark("要返回的树型记录的标识列表");
        method.addParameter(parameter);
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST,
                ztreeDataSimpleType,
                parentElement));
        method.setReturnRemark("tree对象集合结果封装对象");
        method.addAnnotation(new SystemLog("查询树结果数据", introspectedTable), parentElement);
        method.addAnnotation(new RequestMapping("tree", RequestMethod.POST), parentElement);
        method.addAnnotation(new ApiOperation("树形数据查询", "获取指定根或所有的树形结构数据"), parentElement);
        commentGenerator.addMethodJavaDocLine(method, "获取指定根或所有的树形结构数据");
        //函数体
        method.addBodyLine("SimpleTreeNodeConverter<{0}> nodeConverter = new SimpleTreeNodeConverter<>();",entityType.getShortName());
        method.addBodyLine(VStringUtil.format("nodeConverter.setRecords({0}.selectByMultiStringIds(ids));", serviceBeanName));
        method.addBodyLine("return ResponseResult.success(nodeConverter.convertTreeNodeData());");
        parentElement.addMethod(method);


        //fetchTreeCateGenerateDemoTree方法定义
        TreeViewCateGeneratorConfiguration configuration = tc.getJavaControllerGeneratorConfiguration().getTreeViewCateGeneratorConfiguration();
        if (configuration != null) {
            FullyQualifiedJavaType ztreeDataSimpleCateType = new FullyQualifiedJavaType(Z_TREE_DATA_SIMPLE_CATE);
            parentElement.addImportedType(ztreeDataSimpleCateType);
            final String mPrefix = "fetchTreeCate";
            Method methodCate = createMethod(mPrefix);
            methodCate.addParameter(parameter);
            methodCate.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST,
                    ztreeDataSimpleCateType,
                    parentElement));
            methodCate.setReturnRemark("treeCate对象集合结果封装对象");
            methodCate.addAnnotation(new SystemLog("查询分类树结果数据", introspectedTable), parentElement);
            methodCate.addAnnotation(new RequestMapping("view-cate", RequestMethod.POST), parentElement);
            methodCate.addAnnotation(new ApiOperation("树形分类数据查询", "获取指定根或所有的树形分类结构数据"), parentElement);
            commentGenerator.addMethodJavaDocLine(methodCate, "获取指定根或所有的树形结构数据");
            //函数体
            methodCate.addBodyLine("SimpleTreeNodeConverter<{0}> nodeConverter = new SimpleTreeNodeConverter<>();",entityType.getShortName());
            methodCate.addBodyLine(VStringUtil.format("nodeConverter.setRecords({0}.selectByMultiStringIds(ids));", serviceBeanName));
            methodCate.addBodyLine("return ResponseResult.success(nodeConverter.convertTreeNodeViewCate(\"{0}\"));",configuration.getSPeL());
            parentElement.addMethod(methodCate);
        }
        parentElement.addImportedType("com.vgosoft.core.pojo.ztree.cast.SimpleTreeNodeConverter");
    }
}

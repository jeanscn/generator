/**
* 个性化处理的js文件
* 如果对当前文件进行了修改，请务必修改生成配置的“generateHtml”元素的overWriteJsFile属性值改为false，
*    即overWriteJsFile=“false”，否则再次生成时会被覆盖
*/
$(function () {
//更新标题
    window.updateSubject = function (data) {
        //nothing to do
    }
<#list callBackMethods as methods>
    <#if methods.type == 0>
    //关联操作方法
    window.${methods.methodName} = function (o, n) {
        return window.relationHandle(o, n, '${methods.requestKey}', '${methods.thisKey}', '${methods.otherKey}');
    }
    <#else>
    //回调方法.执行扩展逻辑,并且返回true以便于继续执行调用方法的默认逻辑
    window.${methods.methodName} = function (o, n,data) {
        return true;
    }
    </#if>
</#list>
<#if innerList ??>
    layui.use(['table'], function () {
    let table = layui.table;
    /*
    params['baseUrl'] 目标表的基础url
    params['mainRecordField'] 目标表的主表关系字段名
    params['mainRecordId'] 目标表的主表关系字段值
    params['viewStatus'] 目标表的视图读写状态
    params['dataUrl'] 目标表的数据url
    dataSet 目标数据集合,如果不传入,则从params['dataUrl']获取,如果两者都没有，则使用默认list接口
    $('#${innerList.tagId}').renderInnerTable(params, table,dataSet);
    */
    let params = {};
    params['baseUrl'] = "/${innerList.appKey}/${innerList.sourceBeanName}";
    params['mainRecordField'] = '${innerList.relationField}';
    <#if innerList ?? && innerList.dataUrl ?? && innerList.dataUrl != ''>
        params['dataUrl'] = '${innerList.dataUrl}';
    <#else>
        params['dataUrl'] = '/${innerList.appKey}/${innerList.sourceBeanName}';
    </#if>
    if (!$.isBlank($('#id').val())) {
    params['mainRecordId'] = $('#id').val();
    params['viewStatus'] = $('#viewStatus').val();
    <#if innerList ?? && innerList.dataField ?? && innerList.dataField != '' >
        $('#${innerList.tagId}').renderInnerTable(params, table, JSON.parse(${innerList.dataField}));
    <#else>
        $('#${innerList.tagId}').renderInnerTable(params, table);
    </#if>
    }
    $(document).on('toggleEditMode', function (e) {
    if (!$.isBlank($('#id').val())) {
    params['mainRecordId'] = $('#id').val();
    params['viewStatus'] = $('#viewStatus').val();
    <#if innerList ?? && innerList.dataField ?? && innerList.dataField != '' >
        $('#${innerList.tagId}').renderInnerTable(params, table, JSON.parse(${innerList.dataField}));
    <#else>
        $('#${innerList.tagId}').renderInnerTable(params, table);
    </#if>
    }
    });
    });
</#if>
});

/**
 * 个性化处理的js文件
 */
$(function () {
    //更新标题
    window.updateSubject = function (data) {
    //nothing to do
    }
<#list callBackMethods as methods>
    //用户关联部门
    window.${methods.methodName} = function (o, n) {
    return window.relationHandle(o, n, '${methods.requestKey}', '${methods.thisKey}', '${methods.otherKey}');
    }
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
        $('#detail').renderInnerTable(params, table,dataSet);
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
            $('#detail').renderInnerTable(params, table);
    </#if>
        }
        $(document).on('toggleEditMode', function (e) {
            if (!$.isBlank($('#id').val())) {
                params['mainRecordId'] = $('#id').val();
                params['viewStatus'] = $('#viewStatus').val();
    <#if innerList ?? && innerList.dataField ?? && innerList.dataField != '' >
                $('#${innerList.tagId}').renderInnerTable(params, table, JSON.parse(${innerList.dataField}));
    <#else>
                $('#detail').renderInnerTable(params, table);
    </#if>
            }
        });
    });
</#if>
});

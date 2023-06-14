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
        let $table = $('#${innerList.tagId}');
        let url = '/${innerList.appKey}/${innerList.sourceBeanName}/lay-table-config?viewStatus=' + $('#viewStatus').val();
        $.requestJsonSuccessCallback(url, function (resp) {
            if (resp.status !== 0 || resp.data === null) {
                return;
            }
            let options = resp.data;
        <#if innerList ?? && innerList.dataField ?? && innerList.dataField != '' >
            options.data = JSON.parse(${innerList.dataField});
            $table.data("url", options.url);
            options.url = '';
        <#elseif innerList ?? && innerList.dataUrl ?? && innerList.dataUrl != ''>
            options.url = ;
            options.url = $.updateUrlParameter('${innerList.dataUrl}','${innerList.relationField}',$('#id').val());
        </#if>
            options['parentAttr']['fieldName'] = '${innerList.relationField}';
            options['parentAttr']['fieldValue'] = $('#id').val();
            $table.laytable(options, $('#viewStatus').val() === '1', table);
        }, {
            type: 'GET'
        });
    });
</#if>
});

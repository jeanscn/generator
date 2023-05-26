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
});

/**
 * 个性化处理的js文件
 */
$(function () {
    //更新标题
    window.updateSubject = function (data) {
        let name = data.name?data.name:data.applyHandlerText?data.applyHandlerText?:data.applyUserText?data.applyUserText:'';
        data.subject = "【" + data.fileCategory + "】" + data.name + "的处理单（" + data.applyDate + "）";
    }
});

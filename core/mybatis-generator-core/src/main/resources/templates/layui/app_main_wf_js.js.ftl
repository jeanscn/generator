/**
 * 个性化处理的js文件
 */
$(function () {
    //更新标题
    window.updateSubject = function (data) {
        data.subject = "【" + data.fileCategory + "】" + data.name + "的处理单（" + data.applyDate + "）";
    }
});

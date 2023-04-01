$(function(){//页面加载完后调用：
    $("#uploadForm").submit(upload);
});
/**
 * 功能：将头像上传到七牛云。
 */
function upload() {
    //由于是上传文件，不能用$.post处理，要用更为强大的$.ajax处理
    $.ajax({
        url: "http://upload-z1.qiniup.com",//上传地址，七牛云，华北
        method: "post",//请求方式
        processData: false,//由于是上传文件，不要把表单的内容转换成字符串
        contentType: false,//不让jquary设置上传文件的类型，浏览器自动设置
        data: new FormData($("#uploadForm")[0]),
        success: function(data) {
            if(data && data.code == 0) {//上传成功
                // 更新头像访问路径
                $.post(
                    CONTEXT_PATH + "/user/header/url",
                    {"fileName":$("input[name='key']").val()},
                    function(data) {
                        data = $.parseJSON(data);
                        if(data.code == 0) {//更新成功
                            window.location.reload();//刷新当前的页面
                        } else {
                            alert(data.msg);
                        }
                    }
                );
            } else {
                alert("上传失败!");
            }
        }
    });
    return false;
}
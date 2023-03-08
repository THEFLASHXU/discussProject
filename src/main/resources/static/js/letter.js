$(function () {
    $("#sendBtn").click(send_letter);
    $(".close").click(delete_msg);
});

/**
 * 功能：点击发送私信按钮，将接收方用户名字，私信内容传递给controller方法
 */
function send_letter() {
    $("#sendModal").modal("hide");
    var toName = $("#recipient-name").val();
    var content = $("#message-text").val();
    $.post(
        CONTEXT_PATH + "/letter/send",//发送路径
        {"toName": toName, "content": content},//发送数据
        function (data) {//回调函数
            data = $.parseJSON(data);
            if (data == 0) {
                $("#hintBody").text("发送成功！");
            } else {
                $("#hintBody").text(data.msg);
            }
            $("#hintModal").modal("show");
            setTimeout(function () {
                $("#hintModal").modal("hide");
				location.reload();//刷新当前页面
            }, 2000);
        }
    );

}

function delete_msg() {
    // TODO 删除数据
    $(this).parents(".media").remove();
}
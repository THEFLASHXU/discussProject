$(function(){
	$("#publishBtn").click(publish);//id为publishBtn的按钮点击后，触发publish函数
});

/**
 * 功能：使用JQuery发送AJAX请求，实现发布新帖子
 */
function publish() {
	$("#publishModal").modal("hide");
	//获取标题和内容,使用id选择器，获取前端组件中的内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	//发送异步请求（post）
	$.post(
		CONTEXT_PATH + "/discuss/add",//ajax发送地址
		{"title": title, "content": content},//ajax发送内容
		function (data) {
			data = $.parseJSON(data);
			//在提示框中返回信息
			$("#hintBody").text(data.msg);
			//显示提示框
			$("#hintModal").modal("show");
			//两秒后，自动隐藏提示框
			setTimeout(function () {
				$("#hintModal").modal("hide");
				//如果发布成功，则刷新页面
				if (data.code == 0) {
					window.location.reload();
				}
			}, 2000);
		}
	);

}
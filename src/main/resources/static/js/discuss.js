$(function(){//这个函数里的方法是在页面加载完之后再调用的
    $("#topBtn").click(setTop);//置顶按钮的点击事件函数
    $("#wonderfulBtn").click(setWonderful);//加精按钮的点击事件函数
    $("#deleteBtn").click(setDelete);//删除按钮的点击事件函数
});


// 功能：处理前端点赞请求，异步发送数据
function like(btn,entityType,entityId,entityUserId,postId){
    $.post(
        CONTEXT_PATH + "/like",//功能的访问路径
        {"entityType": entityType, "entityId": entityId,"entityUserId":entityUserId,"postId":postId},
        //回调函数：处理返回的数据
        function (data) {
            //将返回的数据处理成json格式的
            data = $.parseJSON(data);
            if (data.code == 0) {
                //点赞成功，改变前端显示数据
                //因为前端传递来了this指针，标记了当前点击标签的对象，可以通过当前标签的子标签获取修改的数据对象。
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':"赞");
            }else{
                //点赞失败
                alert(data.msg);
            }
        }
    );
}


// 功能：给帖子置顶
function setTop() {
    $.post(//向后端发送异步请求
        CONTEXT_PATH + "/discuss/top",
        {"id":$("#postId").val()},
        function(data) {//回调函数，data是controller方法的返回值。
            data = $.parseJSON(data);
            if(data.code == 0) {//置顶成功
                $("#topBtn").attr("disabled", "disabled");//置顶成功后按钮变为不可用。
            } else {//置顶失败，给提示
                alert(data.msg);
            }
        }
    );
}

// 功能：给帖子加精
function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $("#wonderfulBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 功能：删除帖子
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                location.href = CONTEXT_PATH + "/index";//跳转回首页
            } else {
                alert(data.msg);
            }
        }
    );
}
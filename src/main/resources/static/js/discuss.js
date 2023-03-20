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
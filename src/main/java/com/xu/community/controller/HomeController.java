package com.xu.community.controller;

import com.xu.community.entity.DiscussPost;
import com.xu.community.entity.Page;
import com.xu.community.entity.User;
import com.xu.community.service.DiscussPostService;
import com.xu.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;

    /**
     * 方法功能：查询
     * path = "/index"   指的是访问路径，浏览器访问这个路径的时候就会调用这个函数。
     * method= RequestMethod.GET   指的是请求方式，查询
     * 此方法响应的是网页，所以不用写responseBody了
     * 方法的返回值可以使ModelandView或者是String，如果用String，返回的就是视图的名字
     */
    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page) {
        //方法调用前，SpringMVC会自动实例化方法参数中的所有对象（这里是Model和Page），并将Page注入到Model
        //所以，在thymeleaf中可以直接访问Page对象中的数据，而不需要手动的使用model.addAttribute();将page对象加入到Model中。
        page.setRows(discussPostService.findDiscussPostRows(0));//计算总帖子数，userid=0时代表不以用户id来查询帖子
        page.setStaticPath("/index");//设置分页链接
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        //如果只通过discussPostService.findDiscussPosts方法查找帖子，则只能找到DiscussPost对象，该对象中不包含用户的信息，但是包含用户的userId，因此利用userId在user表中查找user对象
        //List中存放着很多Map，每个map都代表着一个帖子对象和一个用户信息对象，多个map组成了一个list
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        for (DiscussPost post : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("post", post);
            //利用帖子对象中的userId查找user对象，并传入到map中
            User user = userService.findUserById(post.getUserId());
            map.put("user", user);
            //将user对象和discussPost对象一起加入到list中
            discussPosts.add(map);
        }
        //往model中装入数据，传递给前端，让前端访问model中的数据。
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }
}

























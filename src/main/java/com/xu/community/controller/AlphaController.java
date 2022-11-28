package com.xu.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @ResponseBody
    @RequestMapping("/hello")
    public String sayHello(){
        return "say hello";
    }
}

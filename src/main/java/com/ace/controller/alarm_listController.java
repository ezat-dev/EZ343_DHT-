package com.ace.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/alarm_list")
public class alarm_listController {

    @RequestMapping
    public String index() {

        return "/alarm_list/index.jsp";
    }
}

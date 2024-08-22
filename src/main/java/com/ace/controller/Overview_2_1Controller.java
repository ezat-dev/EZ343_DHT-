package com.ace.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/overview_2_1")
public class Overview_2_1Controller {

    @RequestMapping
    public String index() {

        return "/overview_2-1/index.jsp";
    }
}

package com.ace.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/overview_2")
public class Overview_2Controller {

    @RequestMapping
    public String index() {

        return "/overview_2/index.jsp";
    }
}

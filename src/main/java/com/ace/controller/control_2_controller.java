package com.ace.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;



@Controller
@RequestMapping("/control_2")
public class control_2_controller {
	
	 @RequestMapping
	    public String index() {

	        return "/control_2/index.jsp";
	    }

}

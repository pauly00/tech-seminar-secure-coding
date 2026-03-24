package com.techseminar.controller.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/demo")
public class DemoHomeController {

    @GetMapping
    public String page(Model model) {
        model.addAttribute("activeDemo", "home");
        return "demo/index";
    }
}

package com.techseminar.controller;

import com.techseminar.service.BbsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final BbsService bbsService;

    public HomeController(BbsService bbsService) {
        this.bbsService = bbsService;
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        model.addAttribute("recentBbs", bbsService.getList());
        model.addAttribute("loginUser", session.getAttribute("loginUser"));
        model.addAttribute("loginRole", session.getAttribute("loginRole"));
        return "index";
    }
}

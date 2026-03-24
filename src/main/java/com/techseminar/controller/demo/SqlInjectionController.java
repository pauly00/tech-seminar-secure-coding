package com.techseminar.controller.demo;

import com.techseminar.service.BbsService;
import com.techseminar.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * SQL Injection demo controller
 *
 * Scenarios:
 *   1. Login bypass:  admin' --  / (any password)
 *   2. All rows:      %' OR '1'='1
 *   3. UNION attack:  %' UNION SELECT 1,user_id,user_password,user_name,user_email,user_gender,0 FROM user_tb --
 */
@Controller
@RequestMapping("/demo/sql-injection")
public class SqlInjectionController {

    private final BbsService bbsService;
    private final UserService userService;

    public SqlInjectionController(BbsService bbsService, UserService userService) {
        this.bbsService = bbsService;
        this.userService = userService;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("activeDemo", "sql-injection");
        return "demo/sql-injection";
    }

    // ==================== Search SQL Injection ====================

    @PostMapping("/search/vuln")
    public String searchVuln(@RequestParam String keyword, Model model) {
        BbsService.SearchResult result = bbsService.searchVulnerable(keyword);
        model.addAttribute("activeDemo", "sql-injection");
        model.addAttribute("searchVulnResult", result);
        model.addAttribute("searchVulnKeyword", keyword);
        return "demo/sql-injection";
    }

    @PostMapping("/search/secure")
    public String searchSecure(@RequestParam String keyword, Model model) {
        BbsService.SearchResult result = bbsService.searchSecure(keyword);
        model.addAttribute("activeDemo", "sql-injection");
        model.addAttribute("searchSecureResult", result);
        model.addAttribute("searchSecureKeyword", keyword);
        return "demo/sql-injection";
    }

    // ==================== Login SQL Injection ====================

    @PostMapping("/login/vuln")
    public String loginVuln(@RequestParam String userId,
                            @RequestParam String userPassword,
                            Model model) {
        UserService.LoginResult result = userService.loginVulnerable(userId, userPassword);
        model.addAttribute("activeDemo", "sql-injection");
        model.addAttribute("loginVulnResult", result);
        model.addAttribute("loginVulnId", userId);
        model.addAttribute("loginVulnPw", userPassword);
        return "demo/sql-injection";
    }

    @PostMapping("/login/secure")
    public String loginSecure(@RequestParam String userId,
                              @RequestParam String userPassword,
                              Model model) {
        UserService.LoginResult result = userService.loginSecure(userId, userPassword);
        model.addAttribute("activeDemo", "sql-injection");
        model.addAttribute("loginSecureResult", result);
        model.addAttribute("loginSecureId", userId);
        model.addAttribute("loginSecurePw", userPassword);
        return "demo/sql-injection";
    }
}

package com.techseminar.controller.demo;

import com.techseminar.service.BbsService;
import com.techseminar.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * SQL Injection 취약점 데모 컨트롤러.
 *
 * 공격 예시:
 *   로그인 우회  — ID: admin' --   PW: 아무거나
 *   전체 조회   — 검색어: %' OR '1'='1
 *   계정 덤프   — 검색어: %' UNION SELECT 1,user_id,user_password,... FROM user_tb --
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

    @PostMapping("/search/vuln")
    public String searchVuln(@RequestParam String keyword, Model model) {
        model.addAttribute("activeDemo", "sql-injection");
        model.addAttribute("searchVulnResult", bbsService.searchVulnerable(keyword));
        model.addAttribute("searchVulnKeyword", keyword);
        return "demo/sql-injection";
    }

    @PostMapping("/search/secure")
    public String searchSecure(@RequestParam String keyword, Model model) {
        model.addAttribute("activeDemo", "sql-injection");
        model.addAttribute("searchSecureResult", bbsService.searchSecure(keyword));
        model.addAttribute("searchSecureKeyword", keyword);
        return "demo/sql-injection";
    }

    @PostMapping("/login/vuln")
    public String loginVuln(@RequestParam String userId, @RequestParam String userPassword, Model model) {
        model.addAttribute("activeDemo", "sql-injection");
        model.addAttribute("loginVulnResult", userService.loginVulnerable(userId, userPassword));
        model.addAttribute("loginVulnId", userId);
        model.addAttribute("loginVulnPw", userPassword);
        return "demo/sql-injection";
    }

    @PostMapping("/login/secure")
    public String loginSecure(@RequestParam String userId, @RequestParam String userPassword, Model model) {
        model.addAttribute("activeDemo", "sql-injection");
        model.addAttribute("loginSecureResult", userService.loginSecure(userId, userPassword));
        model.addAttribute("loginSecureId", userId);
        model.addAttribute("loginSecurePw", userPassword);
        return "demo/sql-injection";
    }
}

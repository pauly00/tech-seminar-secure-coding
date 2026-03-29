package com.techseminar.controller;

import com.techseminar.service.UserService;
import com.techseminar.service.UserService.LoginResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String userId,
                        @RequestParam String password,
                        HttpSession session,
                        HttpServletResponse response,
                        Model model) {
        LoginResult result = userService.loginSecure(userId, password);
        if (result.isSuccess()) {
            session.setAttribute("loginUser", result.getUserId());

            // [취약] HttpOnly 없이 쿠키 발급 — JS에서 document.cookie로 접근 가능
            // 실제 서비스에서 이렇게 설정하면 XSS로 쿠키 탈취됨
            Cookie userCookie = new Cookie("user", result.getUserId());
            userCookie.setPath("/");
            userCookie.setHttpOnly(false);
            response.addCookie(userCookie);

            return "redirect:/";
        }
        model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();
        Cookie userCookie = new Cookie("user", "");
        userCookie.setPath("/");
        userCookie.setMaxAge(0);
        response.addCookie(userCookie);
        return "redirect:/";
    }
}

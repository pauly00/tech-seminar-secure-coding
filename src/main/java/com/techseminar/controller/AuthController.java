package com.techseminar.controller;

import com.techseminar.model.User;
import com.techseminar.service.UserService;
import com.techseminar.service.UserService.LoginResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ==================== 로그인 ====================

    @GetMapping("/login")
    public String loginForm(HttpSession session) {
        if (session.getAttribute("loginUser") != null) return "redirect:/";
        return "auth/login";
    }

    /**
     * 로그인 처리
     * secure=true  -> PreparedStatement + 세션 저장 (안전)
     * secure=false -> 문자열 연결 SQL + 쿠키 저장 (취약: SQL Injection + 쿠키 탈취)
     */
    @PostMapping("/login")
    public String login(@RequestParam String userId,
                        @RequestParam String userPassword,
                        @RequestParam(defaultValue = "false") boolean secure,
                        HttpSession session,
                        HttpServletResponse response,
                        Model model) {

        LoginResult result = secure
            ? userService.loginSecure(userId, userPassword)
            : userService.loginVulnerable(userId, userPassword);

        if (result.isSuccess()) {
            if (secure) {
                // [안전] HttpSession에 저장
                session.setAttribute("loginUser", result.getUserId());
                session.setAttribute("loginRole", result.getRole());
            } else {
                // [취약] HttpOnly 미설정 쿠키 — JS에서 읽기 가능
                Cookie userCookie = new Cookie("userID", result.getUserId());
                userCookie.setPath("/");
                userCookie.setMaxAge(86400);
                // HttpOnly 미설정 — document.cookie로 접근 가능

                Cookie roleCookie = new Cookie("role", result.getRole());
                roleCookie.setPath("/");
                roleCookie.setMaxAge(86400);

                response.addCookie(userCookie);
                response.addCookie(roleCookie);

                // 앱 기능을 위해 세션에도 저장
                session.setAttribute("loginUser", result.getUserId());
                session.setAttribute("loginRole", result.getRole());
            }
            return "redirect:/";
        } else {
            model.addAttribute("error", result.getMessage());
            model.addAttribute("userId", userId);
            return "auth/login";
        }
    }

    // ==================== 로그아웃 ====================

    @PostMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();
        // 쿠키 삭제
        Cookie c1 = new Cookie("userID", "");
        c1.setMaxAge(0); c1.setPath("/");
        Cookie c2 = new Cookie("role", "");
        c2.setMaxAge(0); c2.setPath("/");
        response.addCookie(c1);
        response.addCookie(c2);
        return "redirect:/";
    }

    // ==================== 회원가입 ====================

    @GetMapping("/signup")
    public String signupForm() {
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String userId,
                         @RequestParam String userPassword,
                         @RequestParam String userName,
                         @RequestParam String userGender,
                         @RequestParam String userEmail,
                         Model model) {
        User user = new User();
        user.setUserId(userId);
        user.setUserPassword(userPassword);
        user.setUserName(userName);
        user.setUserGender(userGender);
        user.setUserEmail(userEmail);

        if (userService.register(user)) {
            return "redirect:/login";
        } else {
            model.addAttribute("error", "회원가입 실패. 이미 존재하는 아이디일 수 있습니다.");
            return "auth/signup";
        }
    }
}

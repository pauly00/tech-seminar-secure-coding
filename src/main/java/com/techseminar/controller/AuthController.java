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

    // ==================== Login ====================

    @GetMapping("/login")
    public String loginForm(HttpSession session) {
        if (session.getAttribute("loginUser") != null) return "redirect:/";
        return "auth/login";
    }

    /**
     * Login handler
     * secure=true  -> PreparedStatement + session (safe)
     * secure=false -> string concat SQL + cookie (vuln: SQL Injection + cookie theft)
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
                // [SAFE] store in HttpSession
                session.setAttribute("loginUser", result.getUserId());
                session.setAttribute("loginRole", result.getRole());
            } else {
                // [VULN] cookie without HttpOnly - readable by JS
                Cookie userCookie = new Cookie("userID", result.getUserId());
                userCookie.setPath("/");
                userCookie.setMaxAge(86400);
                // HttpOnly not set - accessible via document.cookie

                Cookie roleCookie = new Cookie("role", result.getRole());
                roleCookie.setPath("/");
                roleCookie.setMaxAge(86400);

                response.addCookie(userCookie);
                response.addCookie(roleCookie);

                // also store in session for app functionality
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

    // ==================== Logout ====================

    @PostMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();
        // clear cookies
        Cookie c1 = new Cookie("userID", "");
        c1.setMaxAge(0); c1.setPath("/");
        Cookie c2 = new Cookie("role", "");
        c2.setMaxAge(0); c2.setPath("/");
        response.addCookie(c1);
        response.addCookie(c2);
        return "redirect:/";
    }

    // ==================== Signup ====================

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
            model.addAttribute("error", "Registration failed. ID may already exist.");
            return "auth/signup";
        }
    }
}

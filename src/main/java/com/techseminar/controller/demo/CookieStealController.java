package com.techseminar.controller.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * XSS 공격으로 전송된 쿠키를 수신하고 목록으로 보여주는 컨트롤러.
 * 실제 공격에서는 외부 공격자 서버가 이 역할을 담당합니다.
 */
@Controller
@RequestMapping("/demo/steal")
public class CookieStealController {

    private static final LinkedList<String[]> stolen = new LinkedList<>();

    @GetMapping
    public String receive(@RequestParam(required = false) String c, Model model) {
        if (c != null) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            synchronized (stolen) {
                stolen.addFirst(new String[]{time, c});
                if (stolen.size() > 20) stolen.removeLast(); // 최대 20개 유지
            }
        }
        model.addAttribute("stolenList", new ArrayList<>(stolen));
        return "demo/cookie-steal";
    }

    @PostMapping("/clear")
    public String clear() {
        synchronized (stolen) {
            stolen.clear();
        }
        return "redirect:/demo/steal";
    }
}

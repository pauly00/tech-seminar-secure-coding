package com.techseminar.controller.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 쿠키 탈취 수신 컨트롤러 (데모용)
 *
 * XSS 공격으로 전송된 쿠키를 서버에서 수신하고 확인할 수 있게 합니다.
 * 실제 공격에서 이 역할은 외부 공격자 서버가 담당합니다.
 */
@Controller
@RequestMapping("/demo/steal")
public class CookieStealController {

    // 탈취된 쿠키 기록 (최대 20개 유지)
    private static final LinkedList<String[]> stolen = new LinkedList<>();

    @GetMapping
    public String receive(@RequestParam(required = false) String c, Model model) {
        if (c != null) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            synchronized (stolen) {
                stolen.addFirst(new String[]{time, c});
                if (stolen.size() > 20) stolen.removeLast();
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

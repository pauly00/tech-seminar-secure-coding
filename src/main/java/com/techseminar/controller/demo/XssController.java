package com.techseminar.controller.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * XSS(Cross-Site Scripting) 취약점 데모 컨트롤러.
 *
 * Stored XSS:   입력값이 저장된 후 모든 방문자의 브라우저에서 실행됨
 * Reflected XSS: 입력값이 즉시 응답에 반영되어 실행됨
 *
 * 방어: th:text(자동 HTML 인코딩) vs th:utext(원본 출력 → XSS 발생)
 */
@Controller
@RequestMapping("/demo/xss")
public class XssController {

    private final List<String> vulnComments = new CopyOnWriteArrayList<>();
    private final List<String> secureComments = new CopyOnWriteArrayList<>();

    @GetMapping
    public String page(Model model) {
        model.addAttribute("activeDemo", "xss");
        model.addAttribute("vulnComments", new ArrayList<>(vulnComments));
        model.addAttribute("secureComments", new ArrayList<>(secureComments));
        return "demo/xss";
    }

    @PostMapping("/stored/vuln")
    public String storedVuln(@RequestParam String comment, Model model) {
        vulnComments.add(comment); // 입력값 그대로 저장
        model.addAttribute("activeDemo", "xss");
        model.addAttribute("vulnComments", new ArrayList<>(vulnComments));
        model.addAttribute("secureComments", new ArrayList<>(secureComments));
        model.addAttribute("storedTab", "vuln");
        return "demo/xss";
    }

    @PostMapping("/stored/secure")
    public String storedSecure(@RequestParam String comment, Model model) {
        secureComments.add(comment); // 원본 저장, 렌더링 시 th:text로 이스케이프
        model.addAttribute("activeDemo", "xss");
        model.addAttribute("vulnComments", new ArrayList<>(vulnComments));
        model.addAttribute("secureComments", new ArrayList<>(secureComments));
        model.addAttribute("storedTab", "secure");
        return "demo/xss";
    }

    @GetMapping("/reflected")
    public String reflected(@RequestParam(required = false) String name,
                            @RequestParam(defaultValue = "false") boolean secure,
                            Model model) {
        model.addAttribute("activeDemo", "xss");
        model.addAttribute("reflectedName", name);
        model.addAttribute("reflectedSecure", secure);
        model.addAttribute("vulnComments", new ArrayList<>(vulnComments));
        model.addAttribute("secureComments", new ArrayList<>(secureComments));
        return "demo/xss";
    }

    @PostMapping("/clear")
    public String clear() {
        vulnComments.clear();
        secureComments.clear();
        return "redirect:/demo/xss";
    }
}

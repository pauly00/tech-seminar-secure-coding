package com.techseminar.controller.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * XSS (Cross-Site Scripting) demo controller
 *
 * Reflected XSS: input immediately reflected into HTML output
 * Stored XSS:    input stored and executed when other users view it
 *
 * Defense: th:text (auto HTML-encode) vs th:utext (raw output -> XSS)
 */
@Controller
@RequestMapping("/demo/xss")
public class XssController {

    // In-memory comment store (vulnerable)
    private final List<String> vulnComments = new CopyOnWriteArrayList<>();
    // In-memory comment store (secure)
    private final List<String> secureComments = new CopyOnWriteArrayList<>();

    @GetMapping
    public String page(Model model) {
        model.addAttribute("activeDemo", "xss");
        model.addAttribute("vulnComments", new ArrayList<>(vulnComments));
        model.addAttribute("secureComments", new ArrayList<>(secureComments));
        return "demo/xss";
    }

    // ==================== Stored XSS ====================

    @PostMapping("/stored/vuln")
    public String storedVuln(@RequestParam String comment, Model model) {
        vulnComments.add(comment);  // store as-is
        model.addAttribute("activeDemo", "xss");
        model.addAttribute("vulnComments", new ArrayList<>(vulnComments));
        model.addAttribute("secureComments", new ArrayList<>(secureComments));
        model.addAttribute("storedTab", "vuln");
        return "demo/xss";
    }

    @PostMapping("/stored/secure")
    public String storedSecure(@RequestParam String comment, Model model) {
        secureComments.add(comment);  // store raw; escaped by th:text on render
        model.addAttribute("activeDemo", "xss");
        model.addAttribute("vulnComments", new ArrayList<>(vulnComments));
        model.addAttribute("secureComments", new ArrayList<>(secureComments));
        model.addAttribute("storedTab", "secure");
        return "demo/xss";
    }

    // ==================== Reflected XSS ====================

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

    // ==================== Clear Comments ====================

    @PostMapping("/clear")
    public String clear() {
        vulnComments.clear();
        secureComments.clear();
        return "redirect:/demo/xss";
    }
}

package com.techseminar.controller.demo;

import com.techseminar.service.FileService;
import com.techseminar.service.FileService.UploadResult;
import com.techseminar.service.FileService.ZipExtractResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * File Upload vulnerability demo controller
 *
 * Demos:
 *   1. Unrestricted upload (no extension check)
 *   2. Whitelist upload (safe)
 *   3. ZIP Slip - vulnerable extraction (no path check)
 *   4. ZIP Slip - secure extraction (normalize + startsWith)
 */
@Controller
@RequestMapping("/demo/file-upload")
public class FileUploadDemoController {

    private final FileService fileService;

    // Fixed bbsId for demo
    private static final int DEMO_BBS_ID = 999;

    public FileUploadDemoController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("activeDemo", "file-upload");
        return "demo/file-upload";
    }

    // ==================== Basic Upload ====================

    @PostMapping("/vuln")
    public String uploadVuln(@RequestParam MultipartFile file, Model model) {
        UploadResult result = fileService.uploadVulnerable(file, DEMO_BBS_ID);
        model.addAttribute("activeDemo", "file-upload");
        model.addAttribute("vulnResult", result);
        return "demo/file-upload";
    }

    @PostMapping("/secure")
    public String uploadSecure(@RequestParam MultipartFile file, Model model) {
        UploadResult result = fileService.uploadSecure(file, DEMO_BBS_ID);
        model.addAttribute("activeDemo", "file-upload");
        model.addAttribute("secureResult", result);
        return "demo/file-upload";
    }

    // ==================== ZIP Slip ====================

    @PostMapping("/zip/vuln")
    public String zipVuln(@RequestParam MultipartFile file, Model model) {
        ZipExtractResult result = fileService.extractVulnerable(file);
        model.addAttribute("activeDemo", "file-upload");
        model.addAttribute("zipVulnResult", result);
        return "demo/file-upload";
    }

    @PostMapping("/zip/secure")
    public String zipSecure(@RequestParam MultipartFile file, Model model) {
        ZipExtractResult result = fileService.extractSecure(file);
        model.addAttribute("activeDemo", "file-upload");
        model.addAttribute("zipSecureResult", result);
        return "demo/file-upload";
    }
}

package com.techseminar.controller.demo;

import com.techseminar.service.FileService;
import com.techseminar.service.FileService.UploadResult;
import com.techseminar.service.FileService.WebshellResult;
import com.techseminar.service.FileService.ZipExtractResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 취약점 데모 컨트롤러
 *
 * 데모 항목:
 *   1. 무제한 업로드 (확장자 검사 없음)
 *   2. 화이트리스트 업로드 (안전)
 *   3. ZIP Slip: 취약한 압축 해제 (경로 검사 없음)
 *   4. ZIP Slip: 안전한 압축 해제 (normalize + startsWith)
 */
@Controller
@RequestMapping("/demo/file-upload")
public class FileUploadDemoController {

    private final FileService fileService;

    // 데모용 고정 bbsId
    private static final int DEMO_BBS_ID = 999;

    public FileUploadDemoController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("activeDemo", "file-upload");
        return "demo/file-upload";
    }

    // ==================== 기본 업로드 ====================

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

    // ==================== 웹쉘 실행 ====================

    @GetMapping("/webshell")
    public String webshell(@RequestParam(required = false) String cmd, Model model) {
        WebshellResult result = fileService.executeWebshell(cmd);
        model.addAttribute("activeDemo", "file-upload");
        model.addAttribute("webshellResult", result);
        model.addAttribute("webshellCmd", cmd != null ? cmd : "");
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

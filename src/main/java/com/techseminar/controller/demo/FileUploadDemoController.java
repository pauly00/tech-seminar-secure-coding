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
 * 무제한 업로드, 화이트리스트 검증, ZIP Slip 세 가지 시나리오를 다룹니다.
 */
@Controller
@RequestMapping("/demo/file-upload")
public class FileUploadDemoController {

    private final FileService fileService;

    private static final int DEMO_BBS_ID = 999; // 데모용 고정 ID

    public FileUploadDemoController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("activeDemo", "file-upload");
        return "demo/file-upload";
    }

    @PostMapping("/vuln")
    public String uploadVuln(@RequestParam MultipartFile file, Model model) {
        model.addAttribute("activeDemo", "file-upload");
        model.addAttribute("vulnResult", fileService.uploadVulnerable(file, DEMO_BBS_ID));
        return "demo/file-upload";
    }

    @PostMapping("/secure")
    public String uploadSecure(@RequestParam MultipartFile file, Model model) {
        model.addAttribute("activeDemo", "file-upload");
        model.addAttribute("secureResult", fileService.uploadSecure(file, DEMO_BBS_ID));
        return "demo/file-upload";
    }

    @GetMapping("/webshell")
    public String webshell(@RequestParam(required = false) String cmd, Model model) {
        model.addAttribute("activeDemo", "file-upload");
        model.addAttribute("webshellResult", fileService.executeWebshell(cmd));
        model.addAttribute("webshellCmd", cmd != null ? cmd : "");
        return "demo/file-upload";
    }

    @PostMapping("/zip/vuln")
    public String zipVuln(@RequestParam MultipartFile file, Model model) {
        model.addAttribute("activeDemo", "file-upload");
        model.addAttribute("zipVulnResult", fileService.extractVulnerable(file));
        return "demo/file-upload";
    }

    @PostMapping("/zip/secure")
    public String zipSecure(@RequestParam MultipartFile file, Model model) {
        model.addAttribute("activeDemo", "file-upload");
        model.addAttribute("zipSecureResult", fileService.extractSecure(file));
        return "demo/file-upload";
    }
}

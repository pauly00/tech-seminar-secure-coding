package com.techseminar.controller.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;

/**
 * 경로 순회(Path Traversal) 데모 컨트롤러
 *
 * 취약: base=./safe-files/, 입력=../../application.properties
 *       → DB 비밀번호 및 서버 설정 파일 읽기 가능
 *
 * 안전: normalize() + startsWith() 로 디렉토리 탈출 차단
 */
@Controller
@RequestMapping("/demo/path-traversal")
public class PathTraversalController {

    @Value("${app.safe-files.dir:./safe-files}")
    private String safeFilesDir;

    @GetMapping
    public String page(Model model) {
        model.addAttribute("activeDemo", "path-traversal");
        model.addAttribute("safeFilesDir", safeFilesDir);
        return "demo/path-traversal";
    }

    // ==================== 취약한 파일 읽기 ====================

    /**
     * [취약] 사용자 입력을 경로에 직접 연결 — ../ 탈출 가능
     */
    @GetMapping("/read/vuln")
    public String readVuln(@RequestParam String filename, Model model) {
        model.addAttribute("activeDemo", "path-traversal");
        model.addAttribute("safeFilesDir", safeFilesDir);
        model.addAttribute("vulnFilename", filename);

        // [취약] 검증 없이 경로 직접 조합
        String requestedPath = safeFilesDir + "/" + filename;
        model.addAttribute("vulnResolvedPath", requestedPath);

        try {
            String content = Files.readString(Path.of(requestedPath));
            model.addAttribute("vulnContent", content);
        } catch (IOException e) {
            model.addAttribute("vulnError", "읽기 실패: " + e.getMessage());
        }
        return "demo/path-traversal";
    }

    // ==================== 안전한 파일 읽기 ====================

    /**
     * [안전] normalize() + startsWith() 로 경로 탈출 차단
     */
    @GetMapping("/read/secure")
    public String readSecure(@RequestParam String filename, Model model) {
        model.addAttribute("activeDemo", "path-traversal");
        model.addAttribute("safeFilesDir", safeFilesDir);
        model.addAttribute("secureFilename", filename);

        try {
            Path basePath = Path.of(safeFilesDir).toAbsolutePath().normalize();
            Path requestedPath = basePath.resolve(filename).normalize();

            model.addAttribute("secureBasePath", basePath.toString());
            model.addAttribute("secureResolvedPath", requestedPath.toString());

            // [안전] 경로가 기준 디렉토리 내에 있는지 검사
            if (!requestedPath.startsWith(basePath)) {
                model.addAttribute("secureError",
                    "경로 순회 감지! 기준 디렉토리 외부: " + basePath + "\n요청 경로: " + requestedPath);
                return "demo/path-traversal";
            }

            String content = Files.readString(requestedPath);
            model.addAttribute("secureContent", content);
        } catch (IOException e) {
            model.addAttribute("secureError", "읽기 실패: " + e.getMessage());
        }
        return "demo/path-traversal";
    }
}

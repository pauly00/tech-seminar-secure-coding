package com.techseminar.controller.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;

/**
 * 경로 순회(Path Traversal) 취약점 데모 컨트롤러
 *
 * 취약: 사용자 입력을 기준 경로에 직접 연결 → ../로 허용 디렉토리 탈출 가능
 * 안전: normalize() + startsWith()로 탈출 차단
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

    /** [취약] 파일명을 경로에 직접 붙여 ../로 디렉토리 탈출이 가능 */
    @GetMapping("/read/vuln")
    public String readVuln(@RequestParam String filename, Model model) {
        model.addAttribute("activeDemo", "path-traversal");
        model.addAttribute("safeFilesDir", safeFilesDir);
        model.addAttribute("vulnFilename", filename);

        String requestedPath = safeFilesDir + "/" + filename; // 취약: 검증 없음
        model.addAttribute("vulnResolvedPath", requestedPath);

        try {
            model.addAttribute("vulnContent", Files.readString(Path.of(requestedPath)));
        } catch (IOException e) {
            model.addAttribute("vulnError", "읽기 실패: " + e.getMessage());
        }
        return "demo/path-traversal";
    }

    /** [안전] normalize() + startsWith()로 기준 디렉토리 외부 접근 차단 */
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

            if (!requestedPath.startsWith(basePath)) { // 안전: 경로 탈출 차단
                model.addAttribute("secureError",
                    "경로 순회 감지! 기준 경로: " + basePath + "\n요청 경로: " + requestedPath);
                return "demo/path-traversal";
            }

            model.addAttribute("secureContent", Files.readString(requestedPath));
        } catch (IOException e) {
            model.addAttribute("secureError", "읽기 실패: " + e.getMessage());
        }
        return "demo/path-traversal";
    }
}

package com.techseminar.controller.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;

/**
 * Path Traversal demo controller
 *
 * Vulnerable: base=./safe-files/, input=../../application.properties
 *             -> reads DB password and server config
 *
 * Secure: normalize() + startsWith() blocks directory escape
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

    // ==================== Vulnerable File Read ====================

    /**
     * [VULN] User input appended directly to path - ../ escape possible
     */
    @GetMapping("/read/vuln")
    public String readVuln(@RequestParam String filename, Model model) {
        model.addAttribute("activeDemo", "path-traversal");
        model.addAttribute("safeFilesDir", safeFilesDir);
        model.addAttribute("vulnFilename", filename);

        // VULN: no validation, directly build path
        String requestedPath = safeFilesDir + "/" + filename;
        model.addAttribute("vulnResolvedPath", requestedPath);

        try {
            String content = Files.readString(Path.of(requestedPath));
            model.addAttribute("vulnContent", content);
        } catch (IOException e) {
            model.addAttribute("vulnError", "Read failed: " + e.getMessage());
        }
        return "demo/path-traversal";
    }

    // ==================== Secure File Read ====================

    /**
     * [SAFE] normalize() + startsWith() blocks path escape
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

            // SAFE: check path stays within base dir
            if (!requestedPath.startsWith(basePath)) {
                model.addAttribute("secureError",
                    "Path traversal detected! Outside base: " + basePath + "\nRequested: " + requestedPath);
                return "demo/path-traversal";
            }

            String content = Files.readString(requestedPath);
            model.addAttribute("secureContent", content);
        } catch (IOException e) {
            model.addAttribute("secureError", "Read failed: " + e.getMessage());
        }
        return "demo/path-traversal";
    }
}

package com.techseminar.controller;

import com.techseminar.model.Bbs;
import com.techseminar.model.FileInfo;
import com.techseminar.service.BbsService;
import com.techseminar.service.FileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/bbs")
public class BbsController {

    private final BbsService bbsService;
    private final FileService fileService;

    public BbsController(BbsService bbsService, FileService fileService) {
        this.bbsService = bbsService;
        this.fileService = fileService;
    }

    // ==================== 목록 ====================

    @GetMapping
    public String list(Model model, HttpSession session) {
        model.addAttribute("bbsList", bbsService.getList());
        model.addAttribute("loginUser", session.getAttribute("loginUser"));
        model.addAttribute("loginRole", session.getAttribute("loginRole"));
        return "bbs/list";
    }

    // ==================== 조회 ====================

    @GetMapping("/{bbsId}")
    public String view(@PathVariable int bbsId, Model model, HttpSession session) {
        Bbs bbs = bbsService.getById(bbsId);
        if (bbs == null) return "redirect:/bbs";

        String loginUser = (String) session.getAttribute("loginUser");
        String loginRole = (String) session.getAttribute("loginRole");

        // 비밀글 접근 제어
        if (bbs.isPrivate() && !"admin".equals(loginRole) && !bbs.getBbsUserId().equals(loginUser)) {
            model.addAttribute("error", "비밀글입니다.");
            return "bbs/list";
        }

        List<FileInfo> files = fileService.getFilesByBbsId(bbsId);

        model.addAttribute("bbs", bbs);
        model.addAttribute("files", files);
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("loginRole", loginRole);
        return "bbs/view";
    }

    // ==================== 글쓰기 ====================

    @GetMapping("/write")
    public String writeForm(HttpSession session, Model model) {
        if (session.getAttribute("loginUser") == null) return "redirect:/login";
        model.addAttribute("loginUser", session.getAttribute("loginUser"));
        return "bbs/write";
    }

    @PostMapping("/write")
    public String write(@RequestParam String bbsTitle,
                        @RequestParam String bbsContent,
                        @RequestParam(defaultValue = "false") boolean isPrivate,
                        @RequestParam(required = false) MultipartFile file,
                        @RequestParam(defaultValue = "false") boolean secureUpload,
                        HttpSession session,
                        Model model) {

        String loginUser = (String) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        int bbsId = bbsService.writeAndGetId(bbsTitle, loginUser, bbsContent, isPrivate);

        // 파일 업로드
        if (file != null && !file.isEmpty()) {
            if (secureUpload) {
                fileService.uploadSecure(file, bbsId);
            } else {
                fileService.uploadVulnerable(file, bbsId);
            }
        }

        return "redirect:/bbs";
    }

    // ==================== 수정 ====================

    @GetMapping("/{bbsId}/edit")
    public String editForm(@PathVariable int bbsId, HttpSession session, Model model) {
        String loginUser = (String) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        Bbs bbs = bbsService.getById(bbsId);
        if (bbs == null) return "redirect:/bbs";

        model.addAttribute("bbs", bbs);
        model.addAttribute("loginUser", loginUser);
        return "bbs/edit";
    }

    @PostMapping("/{bbsId}/edit")
    public String edit(@PathVariable int bbsId,
                       @RequestParam String bbsTitle,
                       @RequestParam String bbsContent,
                       @RequestParam(defaultValue = "false") boolean isPrivate,
                       HttpSession session) {

        if (session.getAttribute("loginUser") == null) return "redirect:/login";
        bbsService.update(bbsId, bbsTitle, bbsContent, isPrivate);
        return "redirect:/bbs/" + bbsId;
    }

    // ==================== 삭제 ====================

    @PostMapping("/{bbsId}/delete")
    public String delete(@PathVariable int bbsId, HttpSession session) {
        if (session.getAttribute("loginUser") == null) return "redirect:/login";
        fileService.deleteFilesByBbsId(bbsId);
        bbsService.delete(bbsId);
        return "redirect:/bbs";
    }

    // ==================== 검색 ====================

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String keyword,
                         @RequestParam(defaultValue = "false") boolean secure,
                         Model model, HttpSession session) {

        BbsService.SearchResult result = secure
            ? bbsService.searchSecure(keyword)
            : bbsService.searchVulnerable(keyword);

        model.addAttribute("keyword", keyword);
        model.addAttribute("searchResult", result);
        model.addAttribute("secure", secure);
        model.addAttribute("loginUser", session.getAttribute("loginUser"));
        model.addAttribute("loginRole", session.getAttribute("loginRole"));
        return "bbs/search";
    }
}

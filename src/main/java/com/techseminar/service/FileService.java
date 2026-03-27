package com.techseminar.service;

import com.techseminar.model.FileInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FileService {

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    // 허용 확장자 화이트리스트
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "pdf", "txt", "docx", "xlsx", "zip"
    );

    public FileService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ==================== 파일 업로드 (데모) ====================

    /**
     * [취약] 확장자 검사 없음 — 모든 파일 형식 허용
     * → webshell.jsp, malware.exe 등 업로드 가능
     */
    public UploadResult uploadVulnerable(MultipartFile file, int bbsId) {
        UploadResult result = new UploadResult();
        result.setOriginalName(file.getOriginalFilename());

        if (file.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("파일이 비어 있습니다.");
            return result;
        }

        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().resolve(String.valueOf(bbsId));
            Files.createDirectories(dir);

            // [취약] 원본 파일명으로 저장 — 경로 조작 및 웹쉘 업로드 가능
            String originalFilename = file.getOriginalFilename();
            Path dest = dir.resolve(originalFilename);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            saveFileInfo(bbsId, originalFilename, originalFilename);

            result.setSuccess(true);
            result.setSavedName(originalFilename);
            result.setSavedPath(dest.toString());
            result.setMessage("저장 완료 (검증 없음): " + originalFilename);
        } catch (IOException e) {
            result.setSuccess(false);
            result.setMessage("저장 오류: " + e.getMessage());
        }
        return result;
    }

    /**
     * [안전] 화이트리스트 + UUID 파일명
     * → 실행 파일 차단, 파일명 예측 불가
     */
    public UploadResult uploadSecure(MultipartFile file, int bbsId) {
        UploadResult result = new UploadResult();
        result.setOriginalName(file.getOriginalFilename());

        if (file.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("파일이 비어 있습니다.");
            return result;
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename).toLowerCase();

        // [안전] 화이트리스트 검사
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            result.setSuccess(false);
            result.setMessage("허용되지 않는 파일 형식: ." + extension
                + "  허용 목록: " + ALLOWED_EXTENSIONS);
            return result;
        }

        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().resolve("secure").resolve(String.valueOf(bbsId));
            Files.createDirectories(dir);

            // [안전] UUID로 파일명 무작위화
            String savedName = UUID.randomUUID().toString() + "." + extension;
            Path dest = dir.resolve(savedName);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            saveFileInfo(bbsId, originalFilename, savedName);

            result.setSuccess(true);
            result.setSavedName(savedName);
            result.setSavedPath(dest.toString());
            result.setMessage("저장 완료 (화이트리스트 통과): " + originalFilename + " -> " + savedName);
        } catch (IOException e) {
            result.setSuccess(false);
            result.setMessage("저장 오류: " + e.getMessage());
        }
        return result;
    }

    // ==================== ZIP 압축 해제 (ZIP Slip 데모) ====================

    /**
     * [취약] ZIP 해제 시 경로 검증 없음 — ZIP Slip 가능
     * → ../../evil.txt 같은 엔트리 이름으로 해제 디렉토리 탈출 가능
     */
    public ZipExtractResult extractVulnerable(MultipartFile file) {
        ZipExtractResult result = new ZipExtractResult();
        List<String> extracted = new ArrayList<>();

        try {
            Path extractDir = Paths.get(uploadDir, "zip-vuln");
            Files.createDirectories(extractDir);

            try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    // [취약] 엔트리 이름 직접 사용 — ../ 로 extractDir 탈출 가능
                    Path dest = extractDir.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(dest);
                    } else {
                        Files.createDirectories(dest.getParent());
                        Files.copy(zis, dest, StandardCopyOption.REPLACE_EXISTING);
                        extracted.add(entry.getName() + "  ->  " + dest.toAbsolutePath());
                    }
                    zis.closeEntry();
                }
            }
            result.setSuccess(true);
            result.setExtracted(extracted);
            result.setMessage("압축 해제 완료 " + extracted.size() + "개 — 경로 검증 없음");
        } catch (IOException e) {
            result.setSuccess(false);
            result.setMessage("압축 해제 오류: " + e.getMessage());
        }
        return result;
    }

    /**
     * [안전] normalize() + startsWith() 로 ZIP Slip 차단
     */
    public ZipExtractResult extractSecure(MultipartFile file) {
        ZipExtractResult result = new ZipExtractResult();
        List<String> extracted = new ArrayList<>();
        List<String> blocked = new ArrayList<>();

        try {
            Path extractDir = Paths.get(uploadDir, "zip-secure").toAbsolutePath().normalize();
            Files.createDirectories(extractDir);

            try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    // [안전] 경로 정규화 후 extractDir 내부인지 검사
                    Path dest = extractDir.resolve(entry.getName()).normalize();
                    if (!dest.startsWith(extractDir)) {
                        blocked.add(entry.getName() + "  (차단 - 경로 순회)");
                        zis.closeEntry();
                        continue;
                    }
                    if (entry.isDirectory()) {
                        Files.createDirectories(dest);
                    } else {
                        Files.createDirectories(dest.getParent());
                        Files.copy(zis, dest, StandardCopyOption.REPLACE_EXISTING);
                        extracted.add(entry.getName() + "  ->  " + dest);
                    }
                    zis.closeEntry();
                }
            }
            result.setSuccess(true);
            result.setExtracted(extracted);
            result.setBlocked(blocked);
            result.setMessage("해제: " + extracted.size() + "개, 차단: " + blocked.size() + "개");
        } catch (IOException e) {
            result.setSuccess(false);
            result.setMessage("압축 해제 오류: " + e.getMessage());
        }
        return result;
    }

    // ==================== 유틸리티 ====================

    private void saveFileInfo(int bbsId, String filename, String filerealname) {
        String sql = "INSERT INTO user_bbs_file(bbs_id, filename, filerealname) VALUES(?,?,?)";
        jdbcTemplate.update(sql, bbsId, filename, filerealname);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    // ==================== 웹쉘 실행 (데모) ====================

    /**
     * [취약] 업로드된 webshell.jsp가 존재하면 cmd 파라미터를 서버에서 실행
     * → 파일 업로드 취약점 + 서버 실행 가능 환경의 결합을 시연
     */
    public WebshellResult executeWebshell(String cmd) {
        WebshellResult result = new WebshellResult();
        Path shellPath = Paths.get(uploadDir).toAbsolutePath()
                .resolve(String.valueOf(999)).resolve("webshell.jsp");

        result.setShellPath(shellPath.toString());
        result.setShellExists(Files.exists(shellPath));

        if (!result.isShellExists()) {
            result.setSuccess(false);
            result.setOutput("webshell.jsp 가 업로드되지 않았습니다.\n먼저 [취약] 업로드로 webshell.jsp 를 올려주세요.");
            return result;
        }

        if (cmd == null || cmd.trim().isEmpty()) {
            result.setSuccess(true);
            result.setOutput("(명령어를 입력하세요)");
            return result;
        }

        try {
            String os = System.getProperty("os.name").toLowerCase();
            String[] command = os.contains("win")
                    ? new String[]{"cmd.exe", "/c", cmd}
                    : new String[]{"/bin/sh", "-c", cmd};

            Process process = Runtime.getRuntime().exec(command);
            StringBuilder sb = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line).append("\n");
                while ((line = errReader.readLine()) != null) sb.append("[ERR] ").append(line).append("\n");
            }
            process.waitFor();

            result.setSuccess(true);
            result.setOutput(sb.length() > 0 ? sb.toString().trim() : "(출력 없음)");
        } catch (Exception e) {
            result.setSuccess(false);
            result.setOutput("실행 오류: " + e.getMessage());
        }
        return result;
    }

    public static class WebshellResult {
        private boolean success;
        private boolean shellExists;
        private String shellPath;
        private String output;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public boolean isShellExists() { return shellExists; }
        public void setShellExists(boolean shellExists) { this.shellExists = shellExists; }
        public String getShellPath() { return shellPath; }
        public void setShellPath(String shellPath) { this.shellPath = shellPath; }
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
    }

    // ==================== BBS 파일 목록 ====================

    public List<FileInfo> getFilesByBbsId(int bbsId) {
        String sql = "SELECT * FROM user_bbs_file WHERE bbs_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            FileInfo f = new FileInfo();
            f.setId(rs.getInt("id"));
            f.setBbsId(rs.getInt("bbs_id"));
            f.setFilename(rs.getString("filename"));
            f.setFilerealname(rs.getString("filerealname"));
            return f;
        }, bbsId);
    }

    public void deleteFilesByBbsId(int bbsId) {
        jdbcTemplate.update("DELETE FROM user_bbs_file WHERE bbs_id=?", bbsId);
    }

    // ==================== DTO ====================

    public static class UploadResult {
        private boolean success;
        private String originalName;
        private String savedName;
        private String savedPath;
        private String message;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getOriginalName() { return originalName; }
        public void setOriginalName(String originalName) { this.originalName = originalName; }
        public String getSavedName() { return savedName; }
        public void setSavedName(String savedName) { this.savedName = savedName; }
        public String getSavedPath() { return savedPath; }
        public void setSavedPath(String savedPath) { this.savedPath = savedPath; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ZipExtractResult {
        private boolean success;
        private String message;
        private List<String> extracted = new ArrayList<>();
        private List<String> blocked = new ArrayList<>();

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public List<String> getExtracted() { return extracted; }
        public void setExtracted(List<String> extracted) { this.extracted = extracted; }
        public List<String> getBlocked() { return blocked; }
        public void setBlocked(List<String> blocked) { this.blocked = blocked; }
    }
}

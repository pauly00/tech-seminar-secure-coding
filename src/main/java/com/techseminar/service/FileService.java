package com.techseminar.service;

import com.techseminar.model.FileInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    // Allowed extension whitelist
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "pdf", "txt", "docx", "xlsx", "zip"
    );

    public FileService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ==================== File Upload (demo) ====================

    /**
     * [VULN] No extension check - any file type accepted
     * -> webshell.jsp, malware.exe etc. can be uploaded
     */
    public UploadResult uploadVulnerable(MultipartFile file, int bbsId) {
        UploadResult result = new UploadResult();
        result.setOriginalName(file.getOriginalFilename());

        if (file.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("File is empty.");
            return result;
        }

        try {
            Path dir = Paths.get(uploadDir, String.valueOf(bbsId));
            Files.createDirectories(dir);

            // VULN: save with original filename (path manipulation + webshell possible)
            String originalFilename = file.getOriginalFilename();
            Path dest = dir.resolve(originalFilename);
            file.transferTo(dest.toFile());

            saveFileInfo(bbsId, originalFilename, originalFilename);

            result.setSuccess(true);
            result.setSavedName(originalFilename);
            result.setSavedPath(dest.toString());
            result.setMessage("Saved (no validation): " + originalFilename);
        } catch (IOException e) {
            result.setSuccess(false);
            result.setMessage("Save error: " + e.getMessage());
        }
        return result;
    }

    /**
     * [SAFE] Whitelist + UUID filename
     * -> executable files rejected, filename unpredictable
     */
    public UploadResult uploadSecure(MultipartFile file, int bbsId) {
        UploadResult result = new UploadResult();
        result.setOriginalName(file.getOriginalFilename());

        if (file.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("File is empty.");
            return result;
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename).toLowerCase();

        // SAFE: whitelist check
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            result.setSuccess(false);
            result.setMessage("Disallowed file type: ." + extension
                + "  Allowed: " + ALLOWED_EXTENSIONS);
            return result;
        }

        try {
            Path dir = Paths.get(uploadDir, "secure", String.valueOf(bbsId));
            Files.createDirectories(dir);

            // SAFE: randomize filename with UUID
            String savedName = UUID.randomUUID().toString() + "." + extension;
            Path dest = dir.resolve(savedName);
            file.transferTo(dest.toFile());

            saveFileInfo(bbsId, originalFilename, savedName);

            result.setSuccess(true);
            result.setSavedName(savedName);
            result.setSavedPath(dest.toString());
            result.setMessage("Saved (whitelist passed): " + originalFilename + " -> " + savedName);
        } catch (IOException e) {
            result.setSuccess(false);
            result.setMessage("Save error: " + e.getMessage());
        }
        return result;
    }

    // ==================== ZIP Extraction (ZIP Slip demo) ====================

    /**
     * [VULN] No path validation during ZIP extraction - ZIP Slip possible
     * -> entry name like ../../evil.txt can escape the extraction directory
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
                    // VULN: entry name used directly - ../ can escape extractDir
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
            result.setMessage("Extracted " + extracted.size() + " file(s) — no path validation");
        } catch (IOException e) {
            result.setSuccess(false);
            result.setMessage("Extract error: " + e.getMessage());
        }
        return result;
    }

    /**
     * [SAFE] normalize() + startsWith() blocks ZIP Slip
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
                    // SAFE: normalize and verify path stays within extractDir
                    Path dest = extractDir.resolve(entry.getName()).normalize();
                    if (!dest.startsWith(extractDir)) {
                        blocked.add(entry.getName() + "  (BLOCKED - path traversal)");
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
            result.setMessage("Extracted: " + extracted.size() + ", Blocked: " + blocked.size());
        } catch (IOException e) {
            result.setSuccess(false);
            result.setMessage("Extract error: " + e.getMessage());
        }
        return result;
    }

    // ==================== Helpers ====================

    private void saveFileInfo(int bbsId, String filename, String filerealname) {
        String sql = "INSERT INTO user_bbs_file(bbs_id, filename, filerealname) VALUES(?,?,?)";
        jdbcTemplate.update(sql, bbsId, filename, filerealname);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    // ==================== BBS File List ====================

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

package com.techseminar.service;

import com.techseminar.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final JdbcTemplate jdbcTemplate;

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ==================== 로그인 ====================

    /**
     * [취약] SQL Injection — 문자열 연결 로그인
     *
     * 공격 예시:
     *   ID: admin' --   PW: 아무거나  → 비밀번호 검사 우회
     *   ID: ' OR '1'='1             → 첫 번째 사용자 반환
     */
    public LoginResult loginVulnerable(String userId, String password) {
        String sql = "SELECT * FROM user_tb WHERE user_id = '" + userId
                   + "' AND user_password = '" + password + "'";

        LoginResult result = new LoginResult();
        result.setSqlExecuted(sql);

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            if (!rows.isEmpty()) {
                result.setSuccess(true);
                result.setUserId((String) rows.get(0).get("user_id"));
                result.setRole("admin".equals(result.getUserId()) ? "admin" : "user");
            } else {
                result.setSuccess(false);
                result.setMessage("아이디 또는 비밀번호가 올바르지 않습니다.");
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("DB 오류: " + e.getMessage());
        }
        return result;
    }

    /**
     * [안전] PreparedStatement 로그인 — SQL Injection 불가
     */
    public LoginResult loginSecure(String userId, String password) {
        String sql = "SELECT * FROM user_tb WHERE user_id = ? AND user_password = ?";

        LoginResult result = new LoginResult();
        result.setSqlExecuted(sql + "  -> ['" + userId + "', '" + password + "'] bound");

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userId, password);
            if (!rows.isEmpty()) {
                result.setSuccess(true);
                result.setUserId((String) rows.get(0).get("user_id"));
                result.setRole("admin".equals(result.getUserId()) ? "admin" : "user");
            } else {
                result.setSuccess(false);
                result.setMessage("아이디 또는 비밀번호가 올바르지 않습니다.");
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("DB 오류: " + e.getMessage());
        }
        return result;
    }

    // ==================== 회원가입 ====================

    public boolean register(User user) {
        String sql = "INSERT INTO user_tb(user_id, user_password, user_name, user_gender, user_email) VALUES (?,?,?,?,?)";
        try {
            int rows = jdbcTemplate.update(sql,
                user.getUserId(), user.getUserPassword(),
                user.getUserName(), user.getUserGender(), user.getUserEmail());
            return rows > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== 사용자 조회 ====================

    public List<User> getList() {
        String sql = "SELECT * FROM user_tb ORDER BY id DESC";
        return jdbcTemplate.query(sql, userRowMapper());
    }

    public User findById(String userId) {
        String sql = "SELECT * FROM user_tb WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper(), userId);
        return users.isEmpty() ? null : users.get(0);
    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setUserId(rs.getString("user_id"));
            u.setUserPassword(rs.getString("user_password"));
            u.setUserName(rs.getString("user_name"));
            u.setUserGender(rs.getString("user_gender"));
            u.setUserEmail(rs.getString("user_email"));
            return u;
        };
    }

    // ==================== 로그인 결과 DTO ====================

    public static class LoginResult {
        private boolean success;
        private String userId;
        private String role;
        private String message;
        private String sqlExecuted;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getSqlExecuted() { return sqlExecuted; }
        public void setSqlExecuted(String sqlExecuted) { this.sqlExecuted = sqlExecuted; }
    }
}

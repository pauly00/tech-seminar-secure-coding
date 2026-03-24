package com.techseminar.service;

import com.techseminar.model.Bbs;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class BbsService {

    private final JdbcTemplate jdbcTemplate;

    public BbsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ==================== BBS CRUD ====================

    public List<Bbs> getList() {
        String sql = "SELECT * FROM user_bbs ORDER BY bbs_id DESC";
        return jdbcTemplate.query(sql, bbsRowMapper());
    }

    public Bbs getById(int bbsId) {
        String sql = "SELECT * FROM user_bbs WHERE bbs_id = ?";
        List<Bbs> list = jdbcTemplate.query(sql, bbsRowMapper(), bbsId);
        return list.isEmpty() ? null : list.get(0);
    }

    public int getNextId() {
        String sql = "SELECT COALESCE(MAX(bbs_id), 0) + 1 FROM user_bbs";
        Integer next = jdbcTemplate.queryForObject(sql, Integer.class);
        return next == null ? 1 : next;
    }

    public boolean write(String title, String userId, String content, boolean isPrivate) {
        int nextId = getNextId();
        String sql = "INSERT INTO user_bbs(bbs_id, bbs_title, bbs_userId, bbs_date, bbs_content, is_private) VALUES(?,?,?,?,?,?)";
        try {
            return jdbcTemplate.update(sql, nextId, title, userId, LocalDateTime.now(), content, isPrivate) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public int writeAndGetId(String title, String userId, String content, boolean isPrivate) {
        int nextId = getNextId();
        String sql = "INSERT INTO user_bbs(bbs_id, bbs_title, bbs_userId, bbs_date, bbs_content, is_private) VALUES(?,?,?,?,?,?)";
        jdbcTemplate.update(sql, nextId, title, userId, LocalDateTime.now(), content, isPrivate);
        return nextId;
    }

    public boolean update(int bbsId, String title, String content, boolean isPrivate) {
        String sql = "UPDATE user_bbs SET bbs_title=?, bbs_content=?, is_private=? WHERE bbs_id=?";
        try {
            return jdbcTemplate.update(sql, title, content, isPrivate, bbsId) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean delete(int bbsId) {
        String sql = "DELETE FROM user_bbs WHERE bbs_id=?";
        try {
            return jdbcTemplate.update(sql, bbsId) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Search (SQL Injection demo) ====================

    /**
     * [VULN] SQL Injection - keyword concatenated directly into SQL
     *
     * Attack examples:
     *   keyword: %' UNION SELECT 1, user_id, user_password, user_name, user_email, user_gender, 0 FROM user_tb --
     *   keyword: %' OR '1'='1
     */
    public SearchResult searchVulnerable(String keyword) {
        String sql = "SELECT * FROM user_bbs WHERE bbs_title LIKE '%" + keyword + "%'";
        SearchResult result = new SearchResult();
        result.setSqlExecuted(sql);
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            result.setRows(rows);
        } catch (Exception e) {
            result.setError("SQL error: " + e.getMessage());
        }
        return result;
    }

    /**
     * [SAFE] PreparedStatement - SQL Injection not possible
     */
    public SearchResult searchSecure(String keyword) {
        String sql = "SELECT * FROM user_bbs WHERE bbs_title LIKE ?";
        SearchResult result = new SearchResult();
        result.setSqlExecuted(sql + "  -> ['%" + keyword + "%'] bound");
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, "%" + keyword + "%");
            result.setRows(rows);
        } catch (Exception e) {
            result.setError("SQL error: " + e.getMessage());
        }
        return result;
    }

    private RowMapper<Bbs> bbsRowMapper() {
        return (rs, rowNum) -> {
            Bbs b = new Bbs();
            b.setId(rs.getInt("id"));
            b.setBbsId(rs.getInt("bbs_id"));
            b.setBbsTitle(rs.getString("bbs_title"));
            b.setBbsUserId(rs.getString("bbs_userId"));
            b.setBbsContent(rs.getString("bbs_content"));
            b.setPrivate(rs.getBoolean("is_private"));
            java.sql.Timestamp ts = rs.getTimestamp("bbs_date");
            if (ts != null) b.setBbsDate(ts.toLocalDateTime());
            return b;
        };
    }

    // ==================== Search Result DTO ====================

    public static class SearchResult {
        private String sqlExecuted;
        private List<Map<String, Object>> rows;
        private String error;

        public String getSqlExecuted() { return sqlExecuted; }
        public void setSqlExecuted(String sqlExecuted) { this.sqlExecuted = sqlExecuted; }
        public List<Map<String, Object>> getRows() { return rows; }
        public void setRows(List<Map<String, Object>> rows) { this.rows = rows; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public int getCount() { return rows == null ? 0 : rows.size(); }
    }
}

package com.techseminar.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * SQL Injection 데모에서 사용하는 게시글 검색 서비스
 * 취약한 쿼리(문자열 연결)와 안전한 쿼리(파라미터 바인딩)를 비교 제공합니다.
 */
@Service
public class BbsService {

    private final JdbcTemplate jdbcTemplate;

    public BbsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * [취약] 키워드를 SQL에 직접 연결 — UNION 공격으로 다른 테이블 데이터 추출 가능
     * 예시: %' UNION SELECT 1,user_id,user_password,... FROM user_tb --
     */
    public SearchResult searchVulnerable(String keyword) {
        String sql = "SELECT * FROM user_bbs WHERE bbs_title LIKE '%" + keyword + "%'";
        SearchResult result = new SearchResult();
        result.setSqlExecuted(sql);
        try {
            result.setRows(jdbcTemplate.queryForList(sql));
        } catch (Exception e) {
            result.setError("SQL 오류: " + e.getMessage());
        }
        return result;
    }

    /** [안전] 파라미터 바인딩 — 입력값이 SQL 구조가 아닌 데이터로만 처리됨 */
    public SearchResult searchSecure(String keyword) {
        String sql = "SELECT * FROM user_bbs WHERE bbs_title LIKE ?";
        SearchResult result = new SearchResult();
        result.setSqlExecuted(sql + "  -> ['%" + keyword + "%'] bound");
        try {
            result.setRows(jdbcTemplate.queryForList(sql, "%" + keyword + "%"));
        } catch (Exception e) {
            result.setError("SQL 오류: " + e.getMessage());
        }
        return result;
    }

    public static class SearchResult {
        private String sqlExecuted;
        private List<Map<String, Object>> rows;
        private String error;

        public String getSqlExecuted() { return sqlExecuted; }
        public void setSqlExecuted(String v) { this.sqlExecuted = v; }
        public List<Map<String, Object>> getRows() { return rows; }
        public void setRows(List<Map<String, Object>> v) { this.rows = v; }
        public String getError() { return error; }
        public void setError(String v) { this.error = v; }
        public int getCount() { return rows == null ? 0 : rows.size(); }
    }
}

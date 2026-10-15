package com.example.loanApplication.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

/** Stores KYC details that do not have columns in the user's supplied SQL table. */
@Repository
public class KycDocumentDetailsRepository {
    private final JdbcTemplate jdbcTemplate;

    public KycDocumentDetailsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS kyc_review_details (" +
                "document_id INT NOT NULL PRIMARY KEY, " +
                "original_file_name VARCHAR(255), " +
                "content_type VARCHAR(150), " +
                "rejection_reason VARCHAR(1000), " +
                "reviewed_at DATETIME(6), " +
                "created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), " +
                "updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6))");
    }

    public void save(Integer documentId, String originalFileName, String contentType,
                     String rejectionReason, LocalDateTime reviewedAt) {
        jdbcTemplate.update("INSERT INTO kyc_review_details " +
                "(document_id, original_file_name, content_type, rejection_reason, reviewed_at) " +
                "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "original_file_name=COALESCE(VALUES(original_file_name), original_file_name), " +
                "content_type=COALESCE(VALUES(content_type), content_type), " +
                "rejection_reason=VALUES(rejection_reason), reviewed_at=VALUES(reviewed_at), updated_at=CURRENT_TIMESTAMP(6)",
                documentId, originalFileName, contentType, rejectionReason, reviewedAt);
    }

    public Optional<Details> find(Integer documentId) {
        return jdbcTemplate.query("SELECT original_file_name, content_type, rejection_reason, reviewed_at " +
                "FROM kyc_review_details WHERE document_id = ?", (rs, rowNum) -> map(rs), documentId)
                .stream().findFirst();
    }

    public void delete(Integer documentId) {
        jdbcTemplate.update("DELETE FROM kyc_review_details WHERE document_id = ?", documentId);
    }

    private Details map(ResultSet rs) throws SQLException {
        java.sql.Timestamp timestamp = rs.getTimestamp("reviewed_at");
        return new Details(rs.getString("original_file_name"), rs.getString("content_type"),
                rs.getString("rejection_reason"), timestamp == null ? null : timestamp.toLocalDateTime());
    }

    public record Details(String originalFileName, String contentType, String rejectionReason,
                          LocalDateTime reviewedAt) { }
}

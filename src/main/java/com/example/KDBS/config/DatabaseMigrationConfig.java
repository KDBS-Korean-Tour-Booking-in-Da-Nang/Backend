package com.example.KDBS.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DatabaseMigrationConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrateDatabase() {
        try {
            log.info("Checking database migration status...");

            // Check if target_type column needs to be updated
            String checkColumnSql = "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reactions' AND COLUMN_NAME = 'target_type'";

            List<String> columnTypes = jdbcTemplate.queryForList(checkColumnSql, String.class);

            if (!columnTypes.isEmpty()) {
                String columnType = columnTypes.get(0);
                log.info("Current target_type column type: {}", columnType);

                // If column is too small, update it
                if (columnType.contains("varchar(3)") || columnType.contains("varchar(4)")
                        || columnType.contains("varchar(5)")) {
                    log.info("Updating target_type column to VARCHAR(10)...");
                    jdbcTemplate.execute("ALTER TABLE reactions MODIFY COLUMN target_type VARCHAR(10) NOT NULL");
                    log.info("target_type column updated successfully");
                } else {
                    log.info("target_type column is already properly sized");
                }

                // Check reaction_type column
                String checkReactionTypeSql = "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reactions' AND COLUMN_NAME = 'reaction_type'";

                List<String> reactionTypeColumns = jdbcTemplate.queryForList(checkReactionTypeSql, String.class);
                if (!reactionTypeColumns.isEmpty()) {
                    String reactionTypeColumn = reactionTypeColumns.get(0);
                    log.info("Current reaction_type column type: {}", reactionTypeColumn);

                    if (reactionTypeColumn.contains("varchar(3)") || reactionTypeColumn.contains("varchar(4)")
                            || reactionTypeColumn.contains("varchar(5)")) {
                        log.info("Updating reaction_type column to VARCHAR(10)...");
                        jdbcTemplate.execute("ALTER TABLE reactions MODIFY COLUMN reaction_type VARCHAR(10) NOT NULL");
                        log.info("reaction_type column updated successfully");
                    } else {
                        log.info("reaction_type column is already properly sized");
                    }
                }
            }

            log.info("Database migration check completed");

        } catch (Exception e) {
            log.error("Error during database migration: {}", e.getMessage());
        }
    }
}
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
            // Create migration tracking table if it doesn't exist
            createMigrationTableIfNotExists();
            
            // Check if migration already completed
            if (isMigrationCompleted()) {
                log.debug("Database migration already completed, skipping...");
                return;
            }
            
            log.info("Starting database migration...");

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

            // Check and add new tour fields if they don't exist (no longer adds legacy policy columns)
            addTourFieldsIfNotExist();
            // Drop legacy policy columns if they exist
            dropLegacyPolicyColumnsIfExist();
            
            // Fix existing columns that need to be TEXT
            fixTourScheduleColumn();
            
            // Mark migration as completed
            markMigrationCompleted();
            log.info("Database migration completed successfully");

        } catch (Exception e) {
            log.error("Error during database migration: {}", e.getMessage());
        }
    }
    
    private void createMigrationTableIfNotExists() {
        try {
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS db_migrations (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    migration_name VARCHAR(255) NOT NULL,
                    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY unique_migration (migration_name)
                )
                """;
            jdbcTemplate.execute(createTableSql);
        } catch (Exception e) {
            log.error("Error creating migration table: {}", e.getMessage());
        }
    }
    
    private boolean isMigrationCompleted() {
        try {
            String checkSql = "SELECT COUNT(*) FROM db_migrations WHERE migration_name = 'tour_fields_migration'";
            int count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            return count > 0;
        } catch (Exception e) {
            log.debug("Migration status check failed, will proceed with migration: {}", e.getMessage());
            return false;
        }
    }
    
    private void markMigrationCompleted() {
        try {
            String insertSql = "INSERT IGNORE INTO db_migrations (migration_name) VALUES ('tour_fields_migration')";
            jdbcTemplate.execute(insertSql);
        } catch (Exception e) {
            log.error("Error marking migration as completed: {}", e.getMessage());
        }
    }

    private void addTourFieldsIfNotExist() {
        try {
            // Only add fields that are actually needed
            String[] newFields = {
                "booking_deadline DATETIME",
                "surcharges TEXT"
            };

            for (String field : newFields) {
                String fieldName = field.split(" ")[0];
                String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tours' AND COLUMN_NAME = '" + fieldName + "'";
                
                int count = jdbcTemplate.queryForObject(checkSql, Integer.class);
                if (count == 0) {
                    log.info("Adding new field to tours table: {}", field);
                    jdbcTemplate.execute("ALTER TABLE tours ADD COLUMN " + field);
                    log.info("Added field {} to tours table", fieldName);
                }
            }
        } catch (Exception e) {
            log.error("Error adding tour fields: {}", e.getMessage());
        }
    }
    
    private void dropLegacyPolicyColumnsIfExist() {
        try {
            String[] legacyFields = { "surcharge_policy", "cancellation_policy" };
            for (String fieldName : legacyFields) {
                String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tours' AND COLUMN_NAME = '" + fieldName + "'";
                int count = jdbcTemplate.queryForObject(checkSql, Integer.class);
                if (count > 0) {
                    log.info("Dropping legacy field from tours table: {}", fieldName);
                    jdbcTemplate.execute("ALTER TABLE tours DROP COLUMN " + fieldName);
                    log.info("Dropped field {} from tours table", fieldName);
                }
            }
        } catch (Exception e) {
            log.error("Error dropping legacy tour policy fields: {}", e.getMessage());
        }
    }
    
    private void fixTourScheduleColumn() {
        try {
            // Check if tour_schedule column exists and its current data type
            String checkColumnSql = "SELECT DATA_TYPE, CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tours' AND COLUMN_NAME = 'tour_schedule'";
            
            try {
                var result = jdbcTemplate.queryForMap(checkColumnSql);
                String dataType = (String) result.get("DATA_TYPE");
                Object maxLength = result.get("CHARACTER_MAXIMUM_LENGTH");
                
                log.info("tour_schedule column current type: {}, max length: {}", dataType, maxLength);
                
                // If it's VARCHAR with limited length, alter it to TEXT
                if ("varchar".equalsIgnoreCase(dataType) && maxLength != null) {
                    log.info("Altering tour_schedule column from VARCHAR to TEXT");
                    jdbcTemplate.execute("ALTER TABLE tours MODIFY COLUMN tour_schedule TEXT");
                    log.info("Successfully altered tour_schedule column to TEXT");
                } else if ("text".equalsIgnoreCase(dataType)) {
                    log.info("tour_schedule column is already TEXT type");
                } else {
                    log.info("tour_schedule column type is: {}, no change needed", dataType);
                }
            } catch (Exception e) {
                log.warn("Could not check tour_schedule column: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error fixing tour_schedule column: {}", e.getMessage());
        }
    }
}
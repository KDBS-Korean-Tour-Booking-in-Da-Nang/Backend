-- Expand user.status CHECK constraint to allow COMPANY_PENDING (4)
-- This migration is idempotent: it drops user_chk_1 if present, then recreates it to allow 0..4

-- Drop existing CHECK constraint user_chk_1 if it exists
SET @has_check := (
  SELECT COUNT(*)
  FROM information_schema.TABLE_CONSTRAINTS tc
  WHERE tc.CONSTRAINT_SCHEMA = DATABASE()
    AND tc.TABLE_NAME = 'user'
    AND tc.CONSTRAINT_NAME = 'user_chk_1'
    AND tc.CONSTRAINT_TYPE = 'CHECK'
);
SET @drop_sql := IF(@has_check > 0, 'ALTER TABLE `user` DROP CHECK `user_chk_1`', 'DO 0');
PREPARE drop_stmt FROM @drop_sql; EXECUTE drop_stmt; DEALLOCATE PREPARE drop_stmt;

-- Re-create CHECK constraint to allow values 0..4
-- 0=BANNED, 1=UNBANNED, 2=PENDING, 3=UNVERIFIED, 4=COMPANY_PENDING
ALTER TABLE `user`
ADD CONSTRAINT `user_chk_1` CHECK (`status` IN (0,1,2,3,4));



-- Add premium fields to User table
ALTER TABLE User 
ADD COLUMN premium_type VARCHAR(20) DEFAULT 'FREE' AFTER phone,
ADD COLUMN premium_valid_until DATETIME NULL AFTER premium_type;

-- Update existing users to have FREE premium type
UPDATE User SET premium_type = 'FREE' WHERE premium_type IS NULL;

-- Add index for premium queries
CREATE INDEX idx_user_premium_type ON User(premium_type);
CREATE INDEX idx_user_premium_valid_until ON User(premium_valid_until);

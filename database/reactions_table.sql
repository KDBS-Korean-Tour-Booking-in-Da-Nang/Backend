-- Create reactions table for both posts and comments
CREATE TABLE IF NOT EXISTS reactions (
    reaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reaction_type VARCHAR(20) NOT NULL,
    user_id INT NOT NULL,
    target_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    
    -- Foreign key constraints
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate reactions from same user on same target
    UNIQUE KEY unique_user_target_reaction (user_id, target_id, target_type)
);

-- Create index for better performance
CREATE INDEX idx_reactions_target ON reactions(target_id, target_type);
CREATE INDEX idx_reactions_user_id ON reactions(user_id);
CREATE INDEX idx_reactions_type ON reactions(reaction_type);

-- Insert sample data (optional)
-- INSERT INTO reactions (reaction_type, user_id, target_id, target_type, created_at) VALUES
-- ('LIKE', 1, 1, 'POST', NOW()),
-- ('DISLIKE', 2, 1, 'POST', NOW()),
-- ('LIKE', 3, 1, 'COMMENT', NOW());

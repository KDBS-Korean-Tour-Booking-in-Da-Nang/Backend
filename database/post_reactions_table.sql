-- Create post_reactions table
CREATE TABLE IF NOT EXISTS post_reactions (
    reaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reaction_type VARCHAR(20) NOT NULL,
    user_id INT NOT NULL,
    forum_post_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    
    -- Foreign key constraints
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE,
    FOREIGN KEY (forum_post_id) REFERENCES forum_posts(forum_post_id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate reactions from same user on same post
    UNIQUE KEY unique_user_post_reaction (user_id, forum_post_id)
);

-- Create index for better performance
CREATE INDEX idx_post_reactions_post_id ON post_reactions(forum_post_id);
CREATE INDEX idx_post_reactions_user_id ON post_reactions(user_id);
CREATE INDEX idx_post_reactions_type ON post_reactions(reaction_type);

-- Insert sample data (optional)
-- INSERT INTO post_reactions (reaction_type, user_id, forum_post_id, created_at) VALUES
-- ('LIKE', 1, 1, NOW()),
-- ('LOVE', 2, 1, NOW()),
-- ('HAHA', 3, 1, NOW());

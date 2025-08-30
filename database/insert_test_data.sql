-- Insert test data for testing reactions API

-- 1. Insert test user (nếu chưa có)
INSERT INTO User (username, email, password, avatar, phone, is_premium, dob, cccd, big_decimal, gender, create_at, status, role) 
VALUES (
    'testuser',
    'test@example.com',
    '$2a$10$dummy.hash.for.testing',
    'default-avatar.jpg',
    '0123456789',
    false,
    '1990-01-01',
    '123456789012',
    0.00,
    'Nam',
    NOW(),
    'ACTIVE',
    'USER'
) ON DUPLICATE KEY UPDATE username = username;

-- 2. Insert test forum post (nếu chưa có)
INSERT INTO forum_posts (title, content, react, created_at, user_id) 
VALUES (
    'Test Post for Reactions',
    'This is a test post to test the reactions API functionality.',
    0,
    NOW(),
    (SELECT user_id FROM User WHERE email = 'test@example.com' LIMIT 1)
) ON DUPLICATE KEY UPDATE title = title;

-- 3. Insert test forum comment (nếu chưa có)
INSERT INTO forum_comments (content, created_at, user_id, forum_post_id) 
VALUES (
    'Test comment for reactions',
    NOW(),
    (SELECT user_id FROM User WHERE email = 'test@example.com' LIMIT 1),
    (SELECT forum_post_id FROM forum_posts WHERE title = 'Test Post for Reactions' LIMIT 1)
) ON DUPLICATE KEY UPDATE content = content;

-- 4. Kiểm tra dữ liệu đã insert
SELECT 'Users:' as info;
SELECT user_id, username, email, status FROM User WHERE email = 'test@example.com';

SELECT 'Posts:' as info;
SELECT forum_post_id, title, react FROM forum_posts WHERE title = 'Test Post for Reactions';

SELECT 'Comments:' as info;
SELECT comment_id, content FROM forum_comments WHERE content = 'Test comment for reactions';


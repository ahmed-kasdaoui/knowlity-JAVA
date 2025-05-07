-- Drop existing table if it exists
DROP TABLE IF EXISTS blog;

-- Create blog table with LONGBLOB for maximum image size
CREATE TABLE blog (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    creator_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_image LONGBLOB,
    blog_image LONGBLOB
);

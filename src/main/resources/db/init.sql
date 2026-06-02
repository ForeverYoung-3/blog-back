-- ============================================================
-- Blog Database Initialization Script
-- Compatible with MySQL 5.7+ and MySQL 8.x
-- ============================================================

CREATE DATABASE IF NOT EXISTS blog_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE blog_db;

-- ---- 用户表 ----
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL,
    email       VARCHAR(100) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    nickname    VARCHAR(100) DEFAULT NULL,
    avatar      VARCHAR(255) DEFAULT NULL,
    bio         VARCHAR(500) DEFAULT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'ROLE_VIEWER',
    enabled     TINYINT      NOT NULL DEFAULT 1,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---- 标签表 ----
CREATE TABLE IF NOT EXISTS tags (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50) NOT NULL,
    slug        VARCHAR(50) NOT NULL,
    color       VARCHAR(10) NOT NULL DEFAULT '#6366f1',
    description VARCHAR(200) DEFAULT NULL,
    created_at  DATETIME    NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tag_name (name),
    UNIQUE KEY uk_tag_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---- 文章表 ----
CREATE TABLE IF NOT EXISTS posts (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    title        VARCHAR(200) NOT NULL,
    slug         VARCHAR(200) NOT NULL,
    summary      VARCHAR(500) DEFAULT NULL,
    content      LONGTEXT     NOT NULL,
    cover_image  VARCHAR(500) DEFAULT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    pinned       TINYINT      NOT NULL DEFAULT 0,
    view_count   BIGINT       NOT NULL DEFAULT 0,
    published_at DATETIME     DEFAULT NULL,
    author_id    BIGINT       NOT NULL,
    created_at   DATETIME     NOT NULL,
    updated_at   DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_slug (slug),
    INDEX idx_post_status (status),
    INDEX idx_post_author (author_id),
    CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---- 文章标签关联表 ----
CREATE TABLE IF NOT EXISTS post_tags (
    post_id BIGINT NOT NULL,
    tag_id  BIGINT NOT NULL,
    PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_pt_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_pt_tag  FOREIGN KEY (tag_id)  REFERENCES tags  (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 初始化数据
-- ============================================================

-- 初始管理员账号（密码明文: admin123，已 BCrypt 加密）
-- 注意：此 hash 仅为示例，建议通过注册接口创建真实账号
INSERT IGNORE INTO users (username, email, password, nickname, role, enabled, status, created_at, updated_at)
VALUES (
    'admin',
    'admin@blog.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    '管理员',
    'ROLE_ADMIN',
    1,
    'ACTIVE',
    NOW(),
    NOW()
);

-- 初始标签
INSERT IGNORE INTO tags (name, slug, color, description, created_at) VALUES
('Java',        'java',        '#f89820', 'Java 编程语言相关',  NOW()),
('Spring Boot', 'spring-boot', '#6db33f', 'Spring Boot 框架',  NOW()),
('React',       'react',       '#61dafb', 'React 前端框架',    NOW()),
('MySQL',       'mysql',       '#4479a1', 'MySQL 数据库',      NOW()),
('算法',        'algorithm',   '#ef4444', '数据结构与算法',    NOW()),
('随笔',        'essay',       '#8b5cf6', '生活随笔',          NOW());

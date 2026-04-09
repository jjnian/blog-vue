-- Blog backend mock / seed data for PostgreSQL 15+
-- Consolidated from: data.sql, seed_frontend_static.sql

BEGIN;

-- ============================================================
-- Roles
-- ============================================================

INSERT INTO roles (name, code, description) VALUES
('Super Admin', 'SUPER_ADMIN', 'Has all permissions'),
('Admin',       'ADMIN',       'Has most management permissions'),
('Editor',      'EDITOR',      'Can manage article content'),
('User',        'USER',        'Basic user permissions'),
('Guest',       'GUEST',       'Read-only visitor permissions')
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- Menus
-- ============================================================

INSERT INTO menus (name, code, path, icon, sort_order) VALUES
('Home',         'HOME',         '/',            'Home',          1),
('Archives',     'ARCHIVES',     '/archives',    'Clock',         2),
('Tags',         'TAGS',         '/tags',        'Tag',           3),
('Categories',   'CATEGORIES',   '/categories',  'LayoutGrid',    4),
('Music',        'MUSIC',        '/music',       'Music',         5),
('Movies',       'MOVIES',       '/movies',      'Film',          6),
('Wishes',       'WISHES',       '/wishes',      'TreeDeciduous', 7),
('Message Wall', 'MESSAGE_WALL', '/messagewall', 'MessageSquare', 8),
('Tools',        'TOOLS',        '/tools',       'Wrench',        9),
('Links',        'LINKS',        '/link',        'Link',          10),
('About',        'ABOUT',        '/about',       'User',          11)
ON CONFLICT (code) DO NOTHING;

INSERT INTO menus (name, code, path, icon, sort_order, parent_id) VALUES
('Admin', 'ADMIN', '/admin', 'Settings', 100, NULL)
ON CONFLICT (code) DO NOTHING;

WITH admin_menu AS (SELECT id FROM menus WHERE code = 'ADMIN')
INSERT INTO menus (name, code, path, icon, sort_order, parent_id)
SELECT 'User Management',    'ADMIN_USER',    '/admin/users',    'Users',       1, id FROM admin_menu
ON CONFLICT (code) DO NOTHING;

WITH admin_menu AS (SELECT id FROM menus WHERE code = 'ADMIN')
INSERT INTO menus (name, code, path, icon, sort_order, parent_id)
SELECT 'Role Management',    'ADMIN_ROLE',    '/admin/roles',    'Shield',      2, id FROM admin_menu
ON CONFLICT (code) DO NOTHING;

WITH admin_menu AS (SELECT id FROM menus WHERE code = 'ADMIN')
INSERT INTO menus (name, code, path, icon, sort_order, parent_id)
SELECT 'Article Management', 'ADMIN_ARTICLE', '/admin/articles', 'FileText',   3, id FROM admin_menu
ON CONFLICT (code) DO NOTHING;

WITH admin_menu AS (SELECT id FROM menus WHERE code = 'ADMIN')
INSERT INTO menus (name, code, path, icon, sort_order, parent_id)
SELECT 'Comment Management', 'ADMIN_COMMENT', '/admin/comments', 'MessageCircle', 4, id FROM admin_menu
ON CONFLICT (code) DO NOTHING;

WITH admin_menu AS (SELECT id FROM menus WHERE code = 'ADMIN')
INSERT INTO menus (name, code, path, icon, sort_order, parent_id)
SELECT 'System Settings',    'ADMIN_SETTING', '/admin/settings', 'Settings',   5, id FROM admin_menu
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- Permissions
-- ============================================================

INSERT INTO permissions (name, code, resource_type, resource_path) VALUES
('Visit Home',         'MENU:HOME',         'MENU', '/'),
('Visit Archives',     'MENU:ARCHIVES',     'MENU', '/archives'),
('Visit Tags',         'MENU:TAGS',         'MENU', '/tags'),
('Visit Categories',   'MENU:CATEGORIES',   'MENU', '/categories'),
('Visit Music',        'MENU:MUSIC',        'MENU', '/music'),
('Visit Movies',       'MENU:MOVIES',       'MENU', '/movies'),
('Visit Wishes',       'MENU:WISHES',       'MENU', '/wishes'),
('Visit Message Wall', 'MENU:MESSAGE_WALL', 'MENU', '/messagewall'),
('Visit Tools',        'MENU:TOOLS',        'MENU', '/tools'),
('Visit Links',        'MENU:LINKS',        'MENU', '/link'),
('Visit About',        'MENU:ABOUT',        'MENU', '/about'),
('Visit Admin',        'MENU:ADMIN',        'MENU', '/admin')
ON CONFLICT (code) DO NOTHING;

INSERT INTO permissions (name, code, resource_type, resource_path) VALUES
('View Articles',  'ARTICLE:VIEW',   'API', '/api/articles/**'),
('Create Article', 'ARTICLE:CREATE', 'API', '/api/articles'),
('Edit Article',   'ARTICLE:EDIT',   'API', '/api/articles/*'),
('Delete Article', 'ARTICLE:DELETE', 'API', '/api/articles/*')
ON CONFLICT (code) DO NOTHING;

INSERT INTO permissions (name, code, resource_type, resource_path) VALUES
('View Comments',  'COMMENT:VIEW',  'API', '/api/comments/**'),
('Create Comment', 'COMMENT:CREATE','API', '/api/comments'),
('Audit Comment',  'COMMENT:AUDIT', 'API', '/api/comments/*/audit')
ON CONFLICT (code) DO NOTHING;

INSERT INTO permissions (name, code, resource_type, resource_path) VALUES
('Manage Users',    'USER:MANAGE',    'API', '/api/admin/users/**'),
('Manage Roles',    'ROLE:MANAGE',    'API', '/api/admin/roles/**'),
('Manage Menus',    'MENU:MANAGE',    'API', '/api/admin/menus/**'),
('Manage Settings', 'SETTING:MANAGE', 'API', '/api/admin/settings/**')
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- Role-menu assignments
-- ============================================================

-- SUPER_ADMIN gets every menu
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id FROM roles r, menus m WHERE r.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- USER gets all public menus
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id FROM roles r, menus m
WHERE r.code = 'USER'
  AND m.code IN ('HOME','ARCHIVES','TAGS','CATEGORIES','MUSIC','MOVIES','WISHES','MESSAGE_WALL','TOOLS','LINKS','ABOUT')
ON CONFLICT DO NOTHING;

-- ADMIN gets all public menus
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id FROM roles r, menus m
WHERE r.code = 'ADMIN' AND m.code NOT LIKE 'ADMIN\_%'
ON CONFLICT DO NOTHING;

-- ADMIN gets all admin menus
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id FROM roles r, menus m
WHERE r.code = 'ADMIN'
  AND m.code IN ('ADMIN','ADMIN_USER','ADMIN_ROLE','ADMIN_ARTICLE','ADMIN_COMMENT','ADMIN_SETTING')
ON CONFLICT DO NOTHING;

-- ============================================================
-- Admin user  (password: admin123)
-- ============================================================

INSERT INTO users (username, password, email, nickname, avatar, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@example.com', '管理员', NULL, 'ACTIVE')
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- ============================================================
-- Categories
-- ============================================================

INSERT INTO categories (name, slug, description, color, sort_order, article_count)
VALUES
('数据库', 'database', '数据库相关文章', '#3b82f6', 1, 2),
('后端',   'backend',  '后端开发相关文章', '#10b981', 2, 2),
('DevOps', 'devops',   '运维开发相关文章', '#8b5cf6', 3, 1),
('随笔',   'essay',    '生活随笔',       '#ec4899', 4, 2)
ON CONFLICT (slug) DO UPDATE SET
    name         = EXCLUDED.name,
    description  = EXCLUDED.description,
    color        = EXCLUDED.color,
    sort_order   = EXCLUDED.sort_order,
    article_count = EXCLUDED.article_count,
    deleted      = 0;

-- ============================================================
-- Tags
-- ============================================================

INSERT INTO tags (name, slug, color, article_count)
VALUES
('Java',       'java',       '#f87171', 12),
('Spring',     'spring',     '#22c55e',  8),
('MySQL',      'mysql',      '#3b82f6', 15),
('Docker',     'docker',     '#06b6d4',  5),
('PostgreSQL', 'postgresql', '#6366f1',  3),
('Hexo',       'hexo',       '#a855f7',  2),
('生活',       'life',       '#ec4899', 20),
('DBA',        'dba',        '#ca8a04',  4),
('SQL',        'sql',        '#f97316',  7),
('Cloud',      'cloud',      '#38bdf8',  6),
('感悟',       'thoughts',   '#fb7185', 18),
('Blog',       'blog',       '#10b981',  9),
('JVM',        'jvm',        '#84cc16',  1)
ON CONFLICT (slug) DO UPDATE SET
    name         = EXCLUDED.name,
    color        = EXCLUDED.color,
    article_count = EXCLUDED.article_count,
    deleted      = 0;

-- ============================================================
-- Friendly links
-- ============================================================

INSERT INTO links (name, url, description, avatar, status, sort_order, deleted)
SELECT 'GitHub', 'https://github.com', 'Global code hosting platform', 'https://github.githubassets.com/favicons/favicon.svg', 'ACTIVE', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM links WHERE url = 'https://github.com' AND deleted = 0);

INSERT INTO links (name, url, description, avatar, status, sort_order, deleted)
SELECT 'Vue.js', 'https://vuejs.org', '渐进式 JavaScript 框架', 'https://picsum.photos/seed/vue/100/100', 'ACTIVE', 2, 0
WHERE NOT EXISTS (SELECT 1 FROM links WHERE url = 'https://vuejs.org' AND deleted = 0);

INSERT INTO links (name, url, description, avatar, status, sort_order, deleted)
SELECT 'Vite', 'https://vitejs.dev', '下一代前端工具链', 'https://picsum.photos/seed/vite/100/100', 'ACTIVE', 3, 0
WHERE NOT EXISTS (SELECT 1 FROM links WHERE url = 'https://vitejs.dev' AND deleted = 0);

INSERT INTO links (name, url, description, avatar, status, sort_order, deleted)
SELECT 'Tailwind CSS', 'https://tailwindcss.com', '无需离开您的 HTML，即可快速建立现代网站。', 'https://picsum.photos/seed/tailwind/100/100', 'ACTIVE', 4, 0
WHERE NOT EXISTS (SELECT 1 FROM links WHERE url = 'https://tailwindcss.com' AND deleted = 0);

INSERT INTO links (name, url, description, avatar, status, sort_order, deleted)
SELECT 'Lucide', 'https://lucide.dev', '美丽的、一致的图标', 'https://picsum.photos/seed/lucide/100/100', 'ACTIVE', 5, 0
WHERE NOT EXISTS (SELECT 1 FROM links WHERE url = 'https://lucide.dev' AND deleted = 0);

INSERT INTO links (name, url, description, avatar, status, sort_order, deleted)
SELECT 'Spring Boot', 'https://spring.io/projects/spring-boot', 'Spring Boot official website', 'https://spring.io/favicon.ico', 'ACTIVE', 6, 0
WHERE NOT EXISTS (SELECT 1 FROM links WHERE url = 'https://spring.io/projects/spring-boot' AND deleted = 0);

-- ============================================================
-- Articles
-- ============================================================

INSERT INTO articles
    (title, slug, excerpt, content, views, comments, likes, status, is_top, allow_comment,
     author_id, category_id, created_at, published_at, deleted)
VALUES
(
    'MySQL 8.0 深度安装指南', 'mysql8-install',
    '探索 MySQL 8.0 的安装艺术，从 environment 配置到性能优化，为你的数据之旅奠定坚实基础。',
    '探索 MySQL 8.0 的安装艺术，从 environment 配置到性能优化，为你的数据之旅奠定坚实基础。',
    1240, 12, 0, 'PUBLISHED', false, true,
    (SELECT id FROM users      WHERE username = 'admin'    LIMIT 1),
    (SELECT id FROM categories WHERE slug     = 'database' LIMIT 1),
    '2024-10-13', '2024-10-13', 0
),
(
    'Spring AOP：切面编程的魅力', 'spring-aop',
    '深入理解 Spring AOP 的核心哲学，让你的代码在解耦与复用之间找到完美的平衡。',
    '深入理解 Spring AOP 的核心哲学，让你的代码在解耦与复用之间找到完美的平衡。',
    856, 8, 0, 'PUBLISHED', false, true,
    (SELECT id FROM users      WHERE username = 'admin'   LIMIT 1),
    (SELECT id FROM categories WHERE slug     = 'backend' LIMIT 1),
    '2023-07-16', '2023-07-16', 0
),
(
    'Java 虚拟机：内存与性能的交响乐', 'jvm',
    '揭开 JVM 的神秘面纱，探索内存模型与垃圾回收的律动，让你的 Java 应用起舞。',
    '揭开 JVM 的神秘面纱，探索内存模型与垃圾回收的律动，让你的 Java 应用起舞。',
    2105, 24, 0, 'PUBLISHED', false, true,
    (SELECT id FROM users      WHERE username = 'admin'   LIMIT 1),
    (SELECT id FROM categories WHERE slug     = 'backend' LIMIT 1),
    '2023-06-24', '2023-06-24', 0
),
(
    'PostgreSQL：关系型数据库的优雅实践', 'pgsql',
    '在 PostgreSQL 的世界里，每一条查询都是一次优雅的对话，每一张表都是一个结构化的故事。',
    '在 PostgreSQL 的世界里，每一条查询都是一次优雅的对话，每一张表都是一个结构化的故事。',
    942, 15, 0, 'PUBLISHED', false, true,
    (SELECT id FROM users      WHERE username = 'admin'    LIMIT 1),
    (SELECT id FROM categories WHERE slug     = 'database' LIMIT 1),
    '2023-06-23', '2023-06-23', 0
),
(
    'Dockerfile：构建容器化世界的蓝图', 'dockerfile',
    '学习编写高效的 Dockerfile，将你的应用打包成轻量级的艺术品，在云端自由飞翔。',
    '学习编写高效的 Dockerfile，将你的应用打包成轻量级的艺术品，在云端自由飞翔。',
    1560, 19, 0, 'PUBLISHED', false, true,
    (SELECT id FROM users      WHERE username = 'admin'  LIMIT 1),
    (SELECT id FROM categories WHERE slug     = 'devops' LIMIT 1),
    '2023-06-17', '2023-06-17', 0
),
(
    '纪锐鑫：在代码与生活之间寻找诗意', 'first',
    '这是我的第一篇随笔，记录那些在屏幕前闪烁的灵感，以及在生活里流淌的温暖。',
    '这是我的第一篇随笔，记录那些在屏幕前闪烁的灵感，以及在生活里流淌的温暖。',
    3200, 45, 0, 'PUBLISHED', false, true,
    (SELECT id FROM users      WHERE username = 'admin' LIMIT 1),
    (SELECT id FROM categories WHERE slug     = 'essay' LIMIT 1),
    '2023-06-14', '2023-06-14', 0
),
(
    'Hello Hexo：开启一段诗意的博客之旅', 'hello-world',
    '欢迎来到我的数字花园。在这里，我将用文字播种，用代码浇灌，静待灵感的绽放。',
    '欢迎来到我的数字花园。在这里，我将用文字播种，用代码浇灌，静待灵感的绽放。',
    1100, 5, 0, 'PUBLISHED', false, true,
    (SELECT id FROM users      WHERE username = 'admin' LIMIT 1),
    (SELECT id FROM categories WHERE slug     = 'essay' LIMIT 1),
    '2023-06-13', '2023-06-13', 0
)
ON CONFLICT (slug) DO UPDATE SET
    title       = EXCLUDED.title,
    excerpt     = EXCLUDED.excerpt,
    content     = EXCLUDED.content,
    views       = EXCLUDED.views,
    comments    = EXCLUDED.comments,
    category_id = EXCLUDED.category_id,
    status      = EXCLUDED.status,
    published_at = EXCLUDED.published_at,
    deleted     = 0;

-- ============================================================
-- Article-tag associations
-- ============================================================

DELETE FROM article_tags
WHERE article_id IN (
    SELECT id FROM articles
    WHERE slug IN ('mysql8-install','spring-aop','jvm','pgsql','dockerfile','first','hello-world')
);

INSERT INTO article_tags (article_id, tag_id)
SELECT a.id, t.id FROM articles a JOIN tags t ON a.slug = 'mysql8-install' AND t.slug = 'mysql'
UNION ALL SELECT a.id, t.id FROM articles a JOIN tags t ON a.slug = 'mysql8-install' AND t.slug = 'dba'
UNION ALL SELECT a.id, t.id FROM articles a JOIN tags t ON a.slug = 'spring-aop'     AND t.slug = 'spring'
UNION ALL SELECT a.id, t.id FROM articles a JOIN tags t ON a.slug = 'spring-aop'     AND t.slug = 'java'
UNION ALL SELECT a.id, t.id FROM articles a JOIN tags t ON a.slug = 'jvm'            AND t.slug = 'jvm'
UNION ALL SELECT a.id, t.id FROM articles a JOIN tags t ON a.slug = 'jvm'            AND t.slug = 'java'
UNION ALL SELECT a.id, t.id FROM articles a JOIN tags t ON a.slug = 'pgsql'          AND t.slug = 'postgresql'
UNION ALL SELECT a.id, t.id FROM articles a JOIN tags t ON a.slug = 'pgsql'          AND t.slug = 'sql'
UNION ALL SELECT a.id, t.id FROM articles a JOIN tags t ON a.slug = 'dockerfile'     AND t.slug = 'docker'
UNION ALL SELECT a.id, t.id FROM articles a JOIN tags t ON a.slug = 'dockerfile'     AND t.slug = 'cloud'
UNION ALL SELECT a.id, t.id FROM articles a JOIN tags t ON a.slug = 'first'          AND t.slug = 'life'
UNION ALL SELECT a.id, t.id FROM articles a JOIN tags t ON a.slug = 'first'          AND t.slug = 'thoughts'
UNION ALL SELECT a.id, t.id FROM articles a JOIN tags t ON a.slug = 'hello-world'    AND t.slug = 'hexo'
UNION ALL SELECT a.id, t.id FROM articles a JOIN tags t ON a.slug = 'hello-world'    AND t.slug = 'blog'
ON CONFLICT DO NOTHING;

-- ============================================================
-- Message wall
-- ============================================================

INSERT INTO messages (content, guest_name, animation_type, color, font_size, status, deleted)
SELECT v.content, '静态种子', 'scroll', '#ffffff', '1rem', 'ACTIVE', 0
FROM (
    VALUES
    ('没有源码我很难分析啊'),
    ('我在哪'),
    ('大佬真厉害'),
    ('牛牛牛'),
    ('这个页面真的棒'),
    ('网页真漂亮'),
    ('加油！'),
    ('弹幕功能很强！'),
    ('你好啊'),
    ('海上生明月，天涯共此时。')
) AS v(content)
WHERE NOT EXISTS (
    SELECT 1 FROM messages m
    WHERE m.content = v.content AND m.guest_name = '静态种子'
);

-- ============================================================
-- Refresh counters
-- ============================================================

UPDATE categories c
SET article_count = COALESCE((
    SELECT COUNT(*)
    FROM articles a
    WHERE a.category_id = c.id AND a.deleted = 0 AND a.status = 'PUBLISHED'
), 0);

UPDATE tags t
SET article_count = COALESCE((
    SELECT COUNT(*)
    FROM article_tags at
    JOIN articles a ON a.id = at.article_id
    WHERE at.tag_id = t.id AND a.deleted = 0 AND a.status = 'PUBLISHED'
), 0);

COMMIT;

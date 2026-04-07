-- =============================================
-- 博客系统初始数据脚本
-- =============================================

-- =============================================
-- 1. 初始化角色
-- =============================================
INSERT INTO roles (name, code, description) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有所有权限'),
('管理员', 'ADMIN', '拥有大部分管理权限'),
('编辑', 'EDITOR', '文章编辑权限'),
('普通用户', 'USER', '基本用户权限'),
('访客', 'GUEST', '访客权限')
ON CONFLICT (code) DO NOTHING;

-- =============================================
-- 2. 初始化菜单
-- =============================================
INSERT INTO menus (name, code, path, icon, sort_order) VALUES
('主页', 'HOME', '/', 'Home', 1),
('时间线', 'ARCHIVES', '/archives', 'Clock', 2),
('标签', 'TAGS', '/tags', 'Tag', 3),
('分类', 'CATEGORIES', '/categories', 'LayoutGrid', 4),
('音乐', 'MUSIC', '/music', 'Music', 5),
('电影', 'MOVIES', '/movies', 'Film', 6),
('许愿树', 'WISHES', '/wishes', 'TreeDeciduous', 7),
('留言墙', 'MESSAGE_WALL', '/messagewall', 'MessageSquare', 8),
('工具', 'TOOLS', '/tools', 'Wrench', 9),
('友链', 'LINKS', '/link', 'Link', 10),
('关于', 'ABOUT', '/about', 'User', 11)
ON CONFLICT (code) DO NOTHING;

-- 后台管理菜单
INSERT INTO menus (name, code, path, icon, sort_order, parent_id) VALUES
('后台管理', 'ADMIN', '/admin', 'Settings', 100, NULL)
ON CONFLICT (code) DO NOTHING;

-- 获取后台管理菜单ID
WITH admin_menu AS (SELECT id FROM menus WHERE code = 'ADMIN')
INSERT INTO menus (name, code, path, icon, sort_order, parent_id)
SELECT '用户管理', 'ADMIN_USER', '/admin/users', 'Users', 1, id FROM admin_menu
ON CONFLICT (code) DO NOTHING;

WITH admin_menu AS (SELECT id FROM menus WHERE code = 'ADMIN')
INSERT INTO menus (name, code, path, icon, sort_order, parent_id)
SELECT '角色管理', 'ADMIN_ROLE', '/admin/roles', 'Shield', 2, id FROM admin_menu
ON CONFLICT (code) DO NOTHING;

WITH admin_menu AS (SELECT id FROM menus WHERE code = 'ADMIN')
INSERT INTO menus (name, code, path, icon, sort_order, parent_id)
SELECT '文章管理', 'ADMIN_ARTICLE', '/admin/articles', 'FileText', 3, id FROM admin_menu
ON CONFLICT (code) DO NOTHING;

WITH admin_menu AS (SELECT id FROM menus WHERE code = 'ADMIN')
INSERT INTO menus (name, code, path, icon, sort_order, parent_id)
SELECT '评论管理', 'ADMIN_COMMENT', '/admin/comments', 'MessageCircle', 4, id FROM admin_menu
ON CONFLICT (code) DO NOTHING;

WITH admin_menu AS (SELECT id FROM menus WHERE code = 'ADMIN')
INSERT INTO menus (name, code, path, icon, sort_order, parent_id)
SELECT '系统设置', 'ADMIN_SETTING', '/admin/settings', 'Settings', 5, id FROM admin_menu
ON CONFLICT (code) DO NOTHING;

-- =============================================
-- 3. 初始化权限
-- =============================================
-- 菜单权限
INSERT INTO permissions (name, code, resource_type, resource_path) VALUES
('访问主页', 'MENU:HOME', 'MENU', '/'),
('访问时间线', 'MENU:ARCHIVES', 'MENU', '/archives'),
('访问标签', 'MENU:TAGS', 'MENU', '/tags'),
('访问分类', 'MENU:CATEGORIES', 'MENU', '/categories'),
('访问音乐', 'MENU:MUSIC', 'MENU', '/music'),
('访问电影', 'MENU:MOVIES', 'MENU', '/movies'),
('访问许愿树', 'MENU:WISHES', 'MENU', '/wishes'),
('访问留言墙', 'MENU:MESSAGE_WALL', 'MENU', '/messagewall'),
('访问工具', 'MENU:TOOLS', 'MENU', '/tools'),
('访问友链', 'MENU:LINKS', 'MENU', '/link'),
('访问关于', 'MENU:ABOUT', 'MENU', '/about'),
('访问后台', 'MENU:ADMIN', 'MENU', '/admin')
ON CONFLICT (code) DO NOTHING;

-- API权限 - 文章
INSERT INTO permissions (name, code, resource_type, resource_path) VALUES
('查看文章', 'ARTICLE:VIEW', 'API', '/api/articles/**'),
('创建文章', 'ARTICLE:CREATE', 'API', '/api/articles'),
('编辑文章', 'ARTICLE:EDIT', 'API', '/api/articles/*'),
('删除文章', 'ARTICLE:DELETE', 'API', '/api/articles/*')
ON CONFLICT (code) DO NOTHING;

-- API权限 - 评论
INSERT INTO permissions (name, code, resource_type, resource_path) VALUES
('查看评论', 'COMMENT:VIEW', 'API', '/api/comments/**'),
('发表评论', 'COMMENT:CREATE', 'API', '/api/comments'),
('审核评论', 'COMMENT:AUDIT', 'API', '/api/comments/*/audit')
ON CONFLICT (code) DO NOTHING;

-- API权限 - 管理后台
INSERT INTO permissions (name, code, resource_type, resource_path) VALUES
('用户管理', 'USER:MANAGE', 'API', '/api/admin/users/**'),
('角色管理', 'ROLE:MANAGE', 'API', '/api/admin/roles/**'),
('菜单管理', 'MENU:MANAGE', 'API', '/api/admin/menus/**'),
('系统设置', 'SETTING:MANAGE', 'API', '/api/admin/settings/**')
ON CONFLICT (code) DO NOTHING;

-- =============================================
-- 4. 分配菜单给角色
-- =============================================

-- 超级管理员可访问所有菜单
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id FROM roles r, menus m WHERE r.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- 普通用户可访问大部分前台菜单
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id FROM roles r, menus m
WHERE r.code = 'USER' AND m.code IN ('HOME', 'ARCHIVES', 'TAGS', 'CATEGORIES', 'MUSIC', 'MOVIES', 'WISHES', 'MESSAGE_WALL', 'TOOLS', 'LINKS', 'ABOUT')
ON CONFLICT DO NOTHING;

-- 管理员可访问所有前台菜单和后台管理
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id FROM roles r, menus m
WHERE r.code = 'ADMIN' AND m.code NOT LIKE 'ADMIN\_%'
ON CONFLICT DO NOTHING;

INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id FROM roles r, menus m
WHERE r.code = 'ADMIN' AND m.code IN ('ADMIN', 'ADMIN_USER', 'ADMIN_ROLE', 'ADMIN_ARTICLE', 'ADMIN_COMMENT', 'ADMIN_SETTING')
ON CONFLICT DO NOTHING;

-- =============================================
-- 5. 初始化分类
-- =============================================
INSERT INTO categories (name, slug, description, color, sort_order) VALUES
('数据库', 'database', '数据库相关技术文章', '#3b82f6', 1),
('后端', 'backend', '后端开发技术文章', '#10b981', 2),
('DevOps', 'devops', '运维开发相关文章', '#f59e0b', 3),
('随笔', 'essay', '生活随笔', '#8b5cf6', 4)
ON CONFLICT (slug) DO NOTHING;

-- =============================================
-- 6. 初始化标签
-- =============================================
INSERT INTO tags (name, slug, color) VALUES
('Java', 'java', '#f89820'),
('Spring', 'spring', '#6db33f'),
('MySQL', 'mysql', '#4479a1'),
('Docker', 'docker', '#2496ed'),
('PostgreSQL', 'postgresql', '#336791'),
('Hexo', 'hexo', '#0e83cd'),
('生活', 'life', '#ec4899'),
('DBA', 'dba', '#3b82f6'),
('SQL', 'sql', '#f97316'),
('Cloud', 'cloud', '#06b6d4'),
('感悟', 'thoughts', '#a855f7'),
('Blog', 'blog', '#22c55e')
ON CONFLICT (slug) DO NOTHING;

-- =============================================
-- 7. 初始化友链
-- =============================================
INSERT INTO links (name, url, description, avatar, sort_order) VALUES
('GitHub', 'https://github.com', '全球最大代码托管平台', 'https://github.githubassets.com/favicons/favicon.svg', 1),
('Vue.js', 'https://vuejs.org', '渐进式JavaScript框架', 'https://vuejs.org/logo.svg', 2),
('Spring Boot', 'https://spring.io/projects/spring-boot', '简化Spring应用开发', 'https://spring.io/favicon.ico', 3)
ON CONFLICT DO NOTHING;

-- =============================================
-- 8. 创建默认管理员账号
-- 密码: admin123 (BCrypt加密)
-- =============================================
INSERT INTO users (username, password, email, nickname, avatar, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@example.com', '管理员', NULL, 'ACTIVE')
ON CONFLICT (username) DO NOTHING;

-- 给管理员分配超级管理员角色
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;
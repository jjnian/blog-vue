-- Blog backend seed data for PostgreSQL 15+

INSERT INTO roles (name, code, description) VALUES
('Super Admin', 'SUPER_ADMIN', 'Has all permissions'),
('Admin', 'ADMIN', 'Has most management permissions'),
('Editor', 'EDITOR', 'Can manage article content'),
('User', 'USER', 'Basic user permissions'),
('Guest', 'GUEST', 'Read-only visitor permissions')
ON CONFLICT (code) DO NOTHING;

INSERT INTO menus (name, code, path, icon, sort_order) VALUES
('Home', 'HOME', '/', 'Home', 1),
('Archives', 'ARCHIVES', '/archives', 'Clock', 2),
('Tags', 'TAGS', '/tags', 'Tag', 3),
('Categories', 'CATEGORIES', '/categories', 'LayoutGrid', 4),
('Music', 'MUSIC', '/music', 'Music', 5),
('Movies', 'MOVIES', '/movies', 'Film', 6),
('Wishes', 'WISHES', '/wishes', 'TreeDeciduous', 7),
('Message Wall', 'MESSAGE_WALL', '/messagewall', 'MessageSquare', 8),
('Tools', 'TOOLS', '/tools', 'Wrench', 9),
('Links', 'LINKS', '/link', 'Link', 10),
('About', 'ABOUT', '/about', 'User', 11)
ON CONFLICT (code) DO NOTHING;

INSERT INTO menus (name, code, path, icon, sort_order, parent_id) VALUES
('Admin', 'ADMIN', '/admin', 'Settings', 100, NULL)
ON CONFLICT (code) DO NOTHING;

WITH admin_menu AS (SELECT id FROM menus WHERE code = 'ADMIN')
INSERT INTO menus (name, code, path, icon, sort_order, parent_id)
SELECT 'User Management', 'ADMIN_USER', '/admin/users', 'Users', 1, id FROM admin_menu
ON CONFLICT (code) DO NOTHING;

WITH admin_menu AS (SELECT id FROM menus WHERE code = 'ADMIN')
INSERT INTO menus (name, code, path, icon, sort_order, parent_id)
SELECT 'Role Management', 'ADMIN_ROLE', '/admin/roles', 'Shield', 2, id FROM admin_menu
ON CONFLICT (code) DO NOTHING;

WITH admin_menu AS (SELECT id FROM menus WHERE code = 'ADMIN')
INSERT INTO menus (name, code, path, icon, sort_order, parent_id)
SELECT 'Article Management', 'ADMIN_ARTICLE', '/admin/articles', 'FileText', 3, id FROM admin_menu
ON CONFLICT (code) DO NOTHING;

WITH admin_menu AS (SELECT id FROM menus WHERE code = 'ADMIN')
INSERT INTO menus (name, code, path, icon, sort_order, parent_id)
SELECT 'Comment Management', 'ADMIN_COMMENT', '/admin/comments', 'MessageCircle', 4, id FROM admin_menu
ON CONFLICT (code) DO NOTHING;

WITH admin_menu AS (SELECT id FROM menus WHERE code = 'ADMIN')
INSERT INTO menus (name, code, path, icon, sort_order, parent_id)
SELECT 'System Settings', 'ADMIN_SETTING', '/admin/settings', 'Settings', 5, id FROM admin_menu
ON CONFLICT (code) DO NOTHING;

INSERT INTO permissions (name, code, resource_type, resource_path) VALUES
('Visit Home', 'MENU:HOME', 'MENU', '/'),
('Visit Archives', 'MENU:ARCHIVES', 'MENU', '/archives'),
('Visit Tags', 'MENU:TAGS', 'MENU', '/tags'),
('Visit Categories', 'MENU:CATEGORIES', 'MENU', '/categories'),
('Visit Music', 'MENU:MUSIC', 'MENU', '/music'),
('Visit Movies', 'MENU:MOVIES', 'MENU', '/movies'),
('Visit Wishes', 'MENU:WISHES', 'MENU', '/wishes'),
('Visit Message Wall', 'MENU:MESSAGE_WALL', 'MENU', '/messagewall'),
('Visit Tools', 'MENU:TOOLS', 'MENU', '/tools'),
('Visit Links', 'MENU:LINKS', 'MENU', '/link'),
('Visit About', 'MENU:ABOUT', 'MENU', '/about'),
('Visit Admin', 'MENU:ADMIN', 'MENU', '/admin')
ON CONFLICT (code) DO NOTHING;

INSERT INTO permissions (name, code, resource_type, resource_path) VALUES
('View Articles', 'ARTICLE:VIEW', 'API', '/api/articles/**'),
('Create Article', 'ARTICLE:CREATE', 'API', '/api/articles'),
('Edit Article', 'ARTICLE:EDIT', 'API', '/api/articles/*'),
('Delete Article', 'ARTICLE:DELETE', 'API', '/api/articles/*')
ON CONFLICT (code) DO NOTHING;

INSERT INTO permissions (name, code, resource_type, resource_path) VALUES
('View Comments', 'COMMENT:VIEW', 'API', '/api/comments/**'),
('Create Comment', 'COMMENT:CREATE', 'API', '/api/comments'),
('Audit Comment', 'COMMENT:AUDIT', 'API', '/api/comments/*/audit')
ON CONFLICT (code) DO NOTHING;

INSERT INTO permissions (name, code, resource_type, resource_path) VALUES
('Manage Users', 'USER:MANAGE', 'API', '/api/admin/users/**'),
('Manage Roles', 'ROLE:MANAGE', 'API', '/api/admin/roles/**'),
('Manage Menus', 'MENU:MANAGE', 'API', '/api/admin/menus/**'),
('Manage Settings', 'SETTING:MANAGE', 'API', '/api/admin/settings/**')
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id FROM roles r, menus m WHERE r.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id FROM roles r, menus m
WHERE r.code = 'USER' AND m.code IN ('HOME', 'ARCHIVES', 'TAGS', 'CATEGORIES', 'MUSIC', 'MOVIES', 'WISHES', 'MESSAGE_WALL', 'TOOLS', 'LINKS', 'ABOUT')
ON CONFLICT DO NOTHING;

INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id FROM roles r, menus m
WHERE r.code = 'ADMIN' AND m.code NOT LIKE 'ADMIN\_%'
ON CONFLICT DO NOTHING;

INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id FROM roles r, menus m
WHERE r.code = 'ADMIN' AND m.code IN ('ADMIN', 'ADMIN_USER', 'ADMIN_ROLE', 'ADMIN_ARTICLE', 'ADMIN_COMMENT', 'ADMIN_SETTING')
ON CONFLICT DO NOTHING;

INSERT INTO categories (name, slug, description, color, sort_order) VALUES
('Database', 'database', 'Database related posts', '#3b82f6', 1),
('Backend', 'backend', 'Backend development posts', '#10b981', 2),
('DevOps', 'devops', 'Operations and deployment posts', '#f59e0b', 3),
('Essay', 'essay', 'Personal notes', '#8b5cf6', 4)
ON CONFLICT (slug) DO NOTHING;

INSERT INTO tags (name, slug, color) VALUES
('Java', 'java', '#f89820'),
('Spring', 'spring', '#6db33f'),
('MySQL', 'mysql', '#4479a1'),
('Docker', 'docker', '#2496ed'),
('PostgreSQL', 'postgresql', '#336791'),
('Hexo', 'hexo', '#0e83cd'),
('Life', 'life', '#ec4899'),
('DBA', 'dba', '#3b82f6'),
('SQL', 'sql', '#f97316'),
('Cloud', 'cloud', '#06b6d4'),
('Thoughts', 'thoughts', '#a855f7'),
('Blog', 'blog', '#22c55e')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO links (name, url, description, avatar, sort_order) VALUES
('GitHub', 'https://github.com', 'Global code hosting platform', 'https://github.githubassets.com/favicons/favicon.svg', 1),
('Vue.js', 'https://vuejs.org', 'Progressive JavaScript framework', 'https://vuejs.org/logo.svg', 2),
('Spring Boot', 'https://spring.io/projects/spring-boot', 'Spring Boot official website', 'https://spring.io/favicon.ico', 3)
ON CONFLICT DO NOTHING;

-- password: admin123
INSERT INTO users (username, password, email, nickname, avatar, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@example.com', 'Administrator', NULL, 'ACTIVE')
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

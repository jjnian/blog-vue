-- Menu migration / fix for existing database
-- Idempotent: safe to run multiple times

BEGIN;

-- Add the leisure group if missing
INSERT INTO menus (name, code, path, icon, sort_order, visible, status)
SELECT '休闲', 'LEISURE', '#', 'Music', 5, TRUE, 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM menus WHERE code = 'LEISURE'
);

-- Move music / movies under the leisure group
WITH leisure_menu AS (
    SELECT id FROM menus WHERE code = 'LEISURE'
)
UPDATE menus
SET parent_id = (SELECT id FROM leisure_menu),
    sort_order = CASE code
        WHEN 'MUSIC' THEN 1
        WHEN 'MOVIES' THEN 2
        ELSE sort_order
    END
WHERE code IN ('MUSIC', 'MOVIES');

-- Add the Friends menu if missing
INSERT INTO menus (name, code, path, icon, sort_order, visible, status)
SELECT '朋友地图', 'FRIENDS', '/friends', 'Users', 8, TRUE, 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM menus WHERE code = 'FRIENDS'
);

-- Ensure public roles can access the blog navigation
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
JOIN menus m ON m.code IN ('HOME','ARCHIVES','TAGS','CATEGORIES','LEISURE','MUSIC','MOVIES','WISHES','FRIENDS','MESSAGE_WALL','TOOLS','LINKS','ABOUT')
WHERE r.code IN ('GUEST', 'USER')
ON CONFLICT DO NOTHING;

COMMIT;

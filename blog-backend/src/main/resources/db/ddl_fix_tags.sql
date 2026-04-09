-- DDL patch: unify tag table name and ensure required relations exist
BEGIN;

CREATE TABLE IF NOT EXISTS tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    slug VARCHAR(50) UNIQUE,
    color VARCHAR(20),
    article_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'tagentities'
    ) THEN
        -- Insert only rows that do not conflict on id/name/slug to keep reruns safe.
        INSERT INTO tags (id, name, slug, color, article_count, created_at, deleted)
        SELECT t.id, t.name, t.slug, t.color, t.article_count, t.created_at, t.deleted
        FROM tagentities t
        WHERE NOT EXISTS (
            SELECT 1
            FROM tags x
            WHERE x.id = t.id
               OR x.name = t.name
               OR (x.slug IS NOT NULL AND t.slug IS NOT NULL AND x.slug = t.slug)
        );

        -- Sync mutable columns for existing rows matched by stable business keys.
        UPDATE tags tg
        SET color = src.color,
            article_count = src.article_count,
            deleted = src.deleted,
            created_at = COALESCE(src.created_at, tg.created_at),
            slug = COALESCE(tg.slug, src.slug)
        FROM tagentities src
        WHERE tg.name = src.name
           OR (tg.slug IS NOT NULL AND src.slug IS NOT NULL AND tg.slug = src.slug);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'article_tags'
    ) THEN
        CREATE TABLE article_tags (
            article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
            tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
            PRIMARY KEY (article_id, tag_id)
        );
    END IF;
END $$;

DO $$
DECLARE
    seq_name text;
BEGIN
    seq_name := pg_get_serial_sequence('tags', 'id');
    IF seq_name IS NOT NULL THEN
        EXECUTE format(
            'SELECT setval(%L, %s, true)',
            seq_name,
            COALESCE((SELECT MAX(id) FROM tags), 1)
        );
    END IF;
END $$;

COMMIT;

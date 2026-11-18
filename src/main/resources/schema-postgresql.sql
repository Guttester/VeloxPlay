-- Upgrade the enum CHECK on an existing PostgreSQL users table without deleting data.
-- @@ separates this DO block for Spring's SQL script parser.
DO $$
DECLARE
    users_table regclass := to_regclass('users');
    filmes_table regclass := to_regclass('filmes');
    role_column smallint;
    old_constraint record;
BEGIN
    IF users_table IS NOT NULL THEN
        EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS role varchar(20) DEFAULT ''USER''', users_table);
        EXECUTE format('UPDATE %s SET role = ''USER'' WHERE role IS NULL', users_table);
        EXECUTE format('ALTER TABLE %s ALTER COLUMN role SET DEFAULT ''USER'', ALTER COLUMN role SET NOT NULL', users_table);
        EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS demo_account boolean NOT NULL DEFAULT false', users_table);
        SELECT attnum INTO role_column FROM pg_attribute
        WHERE attrelid = users_table AND attname = 'role' AND NOT attisdropped;
        IF role_column IS NOT NULL THEN
            FOR old_constraint IN
                SELECT conname FROM pg_constraint
                WHERE conrelid = users_table AND contype = 'c'
                  AND conkey = ARRAY[role_column]::smallint[]
            LOOP
                EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', users_table, old_constraint.conname);
            END LOOP;
            EXECUTE format('ALTER TABLE %s ADD CONSTRAINT users_role_check CHECK (role IN (''USER'', ''MODERATOR'', ''ADMIN''))', users_table);
        END IF;
    END IF;
    IF filmes_table IS NOT NULL THEN
        EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS url_backdrop varchar(255)', filmes_table);
        EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS destaque boolean DEFAULT false', filmes_table);
        EXECUTE format('UPDATE %s SET destaque = false WHERE destaque IS NULL', filmes_table);
        EXECUTE format('ALTER TABLE %s ALTER COLUMN destaque SET DEFAULT false, ALTER COLUMN destaque SET NOT NULL', filmes_table);
    END IF;
END
$$
@@

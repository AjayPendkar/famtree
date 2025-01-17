-- Drop existing constraint if exists
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'users_role_check'
    ) THEN
        ALTER TABLE users DROP CONSTRAINT users_role_check;
    END IF;
END $$;

-- Add new constraint with SUPER_ADMIN
ALTER TABLE users ADD CONSTRAINT users_role_check 
CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'HEAD', 'MEMBER')); 

INSERT INTO users (user_uid, first_name, last_name, email, mobile, password, role, is_verified)
VALUES ('admin-uid', 'Admin', 'User', 'admin@famtree.com', '1234567890', 
        '$2a$10$rPmVwXKgYX5YrCgIj1.yxuZ0TQh0lHqLQj8TZjcCJGTe6VdvZplGi', -- password: admin123
        'SUPER_ADMIN', true)
ON CONFLICT DO NOTHING; 
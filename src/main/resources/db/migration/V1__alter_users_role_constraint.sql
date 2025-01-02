-- Drop existing check constraint
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

-- Add new check constraint with SUPER_ADMIN
ALTER TABLE users ADD CONSTRAINT users_role_check 
CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'HEAD', 'MEMBER')); 
CREATE ROLE user_mgmt WITH LOGIN PASSWORD 'user_mgmt123';
CREATE DATABASE user_mgmt_db;
GRANT ALL PRIVILEGES ON DATABASE user_mgmt_db TO user_mgmt;

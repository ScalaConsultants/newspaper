CREATE ROLE analyzer WITH LOGIN PASSWORD 'analyzer123';
CREATE DATABASE analyzer_db;
GRANT ALL PRIVILEGES ON DATABASE analyzer_db TO analyzer;

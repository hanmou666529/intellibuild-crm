@echo off
chcp 65001 > nul
"D:\mysql\bin\mysql.exe" -u root -pHae147258369 ry-vue --default-character-set=utf8mb4 < "%~dp0crm_ai_ddl.sql"
echo Done.

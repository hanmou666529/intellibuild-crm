@echo off
chcp 65001 > nul
"D:\mysql\bin\mysql.exe" -u root -pHae147258369 ry-vue --default-character-set=utf8mb4 -t < "%~dp0crm_ai_ddl.sql"
echo VERIFY:
"D:\mysql\bin\mysql.exe" -u root -pHae147258369 ry-vue --default-character-set=utf8mb4 -t < "%~dp0verify2.sql"
pause

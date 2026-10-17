@echo off
set DB_NAME=tcm_exam_selfstudy
cd /d %~dp0backend\target
java -jar exam-backend-1.0.0.jar > backend-run.log 2>&1
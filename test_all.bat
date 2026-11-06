@echo off
setlocal
call scripts\test_all.bat %*
exit /b %ERRORLEVEL%


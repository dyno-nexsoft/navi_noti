@echo off
call "%~dp0..\gradlew.bat" :app:assembleDebug
if errorlevel 1 exit /b %errorlevel%
copy /Y "%~dp0..\app\build\outputs\apk\debug\app-debug.apk" "%USERPROFILE%\Downloads\Navi-Noti-debug.apk"
exit /b %errorlevel%

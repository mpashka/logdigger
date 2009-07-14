@echo off
set HOME_DIR=%~dp0
set OPT=-Xmx128m -Dxml.dir="%HOME_DIR%xml" -Dlog.dir="%HOME_DIR%log" -Djava.ext.dirs="%HOME_DIR%lib"
if "%JAVA_HOME%"=="" (
set JAVA_EXE=javaw
) ELSE (
set JAVA_EXE=%JAVA_HOME%\bin\javaw.exe
)
start /b cmd /c ("%JAVA_EXE%" %OPT% com.iv.logView.Main %*)

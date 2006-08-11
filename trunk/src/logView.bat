@echo off
set HOME_DIR=%~dp0
set CP=%HOME_DIR%lib\looks-1.2.1.jar;%HOME_DIR%lib\logView.jar;%HOME_DIR%lib\TableLayout.jar
java -Xmx128m -classpath "%CP%" -Dxml.dir="%HOME_DIR%xml" com.iv.logView.Main %*

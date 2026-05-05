@echo off
echo Compiling Java source files...
javac -cp "sqlite-jdbc.jar;slf4j-api.jar;flatlaf.jar;src" src\models\*.java src\db\*.java src\services\*.java src\ui\*.java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Compilation failed! Please check the errors above.
    pause
    exit /b
)

echo.
echo Compilation finished successfully! Starting application...
java -cp "sqlite-jdbc.jar;slf4j-api.jar;flatlaf.jar;src" ui.App
pause

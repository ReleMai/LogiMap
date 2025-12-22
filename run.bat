@echo off
REM Compile and run LogiMap

echo Compiling Java files...
call compile.bat

if errorlevel 1 (
    exit /b 1
)

echo.
echo Running LogiMap...
java --module-path lib/javafx-sdk-23/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp bin App

echo.
pause

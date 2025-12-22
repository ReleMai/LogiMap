@echo off
REM START script - Run this to start LogiMap
REM This script will compile and run the game

color 0A
cls

echo.
echo ========================================
echo   LogiMap - Logistics Simulator
echo ========================================
echo.

REM Check if JavaFX SDK exists
if not exist "lib\javafx-sdk-23" (
    echo ERROR: JavaFX SDK not found!
    echo Please run setup.bat first to download JavaFX.
    echo.
    pause
    exit /b 1
)

REM Check if bin directory exists, create if not
if not exist "bin" mkdir bin

echo [1/2] Compiling Java files...
javac --module-path lib/javafx-sdk-23/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics -d bin -encoding UTF-8 src\*.java

if errorlevel 1 (
    echo.
    echo ERROR: Compilation failed!
    echo Please check the errors above.
    echo.
    pause
    exit /b 1
)

echo [2/2] Launching LogiMap...
echo.

REM Run the application
java --module-path lib/javafx-sdk-23/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp bin App

REM Application has closed
echo.
echo LogiMap has closed.
pause

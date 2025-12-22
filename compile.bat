@echo off
REM Compile LogiMap with JavaFX

echo Compiling Java files with JavaFX...
javac --module-path lib/javafx-sdk-23/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics -d bin -encoding UTF-8 src\*.java

if errorlevel 1 (
    echo Compilation failed!
    exit /b 1
)

echo Compilation successful!

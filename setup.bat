@echo off
REM Setup JavaFX for LogiMap

if not exist "lib" mkdir lib

if not exist "lib\javafx-sdk-23" (
    echo Downloading JavaFX SDK 23...
    powershell -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/23.0.1/openjfx-23.0.1_windows-x64_bin-sdk.zip' -OutFile 'lib\javafx-sdk.zip'"
    
    if exist "lib\javafx-sdk.zip" (
        echo Extracting JavaFX SDK...
        powershell -Command "Expand-Archive -Path 'lib\javafx-sdk.zip' -DestinationPath 'lib'"
        
        if exist "lib\javafx-sdk-23.0.1" (
            ren "lib\javafx-sdk-23.0.1" javafx-sdk-23
        )
        
        del "lib\javafx-sdk.zip"
        echo JavaFX SDK installed successfully
    ) else (
        echo Failed to download JavaFX SDK
    )
) else (
    echo JavaFX SDK already installed
)

echo.
echo Setup complete! You can now run: javac -d bin src\*.java
echo Then run: java -cp bin App

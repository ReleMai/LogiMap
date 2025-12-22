# Setup script for LogiMap JavaFX configuration

# Create lib directory if it doesn't exist
if (!(Test-Path "lib")) {
    New-Item -ItemType Directory -Path "lib" | Out-Null
    Write-Host "Created lib directory"
}

# Download JavaFX SDK if not already present
$javafxPath = "lib/javafx-sdk-23"
if (!(Test-Path $javafxPath)) {
    Write-Host "Downloading JavaFX SDK 23..."
    $url = "https://gluonhq.com/download/javafx/23.0.1/openjfx-23.0.1_windows-x64_bin-sdk.zip"
    $zipPath = "lib/javafx-sdk.zip"
    
    # Download using PowerShell
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -Uri $url -OutFile $zipPath
    
    Write-Host "Extracting JavaFX SDK..."
    Expand-Archive -Path $zipPath -DestinationPath "lib"
    
    # Rename to standard name
    Rename-Item -Path "lib/javafx-sdk-23.0.1" -NewName "javafx-sdk-23" -Force
    
    # Remove zip
    Remove-Item $zipPath
    Write-Host "JavaFX SDK installed successfully"
} else {
    Write-Host "JavaFX SDK already exists at $javafxPath"
}

Write-Host "Setup complete! JavaFX is ready to use."

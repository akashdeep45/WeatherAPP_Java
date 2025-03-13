@echo off
setlocal EnableDelayedExpansion

echo ====================================
echo Weather App Setup and Launch Script
echo ====================================

:: Set project directory
set "PROJECT_DIR=%~dp0"
if not defined PROJECT_DIR set "PROJECT_DIR=%cd%"

:: Create required directories if they don't exist
if not exist "%PROJECT_DIR%lib" mkdir "%PROJECT_DIR%lib"
if not exist "%PROJECT_DIR%out\production\WeatherApp" mkdir "%PROJECT_DIR%out\production\WeatherApp"

:: Set paths for build and source files
set "BUILD_DIR=%PROJECT_DIR%out\production\WeatherApp"
set "SRC_DIR=%PROJECT_DIR%src\main\java"
set "WEBAPP_DIR=%PROJECT_DIR%src\main\webapp"
set "LIB_DIR=%PROJECT_DIR%lib"
set "JSON_LIB=%PROJECT_DIR%json.jar"

:: Check for Java installation
echo Checking Java installation...
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java is not installed or not found in PATH
    echo Please install Java JDK 8 or higher from:
    echo https://adoptium.net/ or https://www.oracle.com/java/technologies/downloads/
    echo After installation, make sure Java is added to your PATH
    pause
    exit /b 1
)

:: Copy JSON library if needed
if not exist "%LIB_DIR%\json.jar" (
    if exist "%JSON_LIB%" (
        echo Copying JSON library to lib directory...
        copy "%JSON_LIB%" "%LIB_DIR%\" >nul
    ) else (
        echo [ERROR] json.jar not found!
        echo Please ensure json.jar is in the project root directory
        echo You can download it from: https://mvnrepository.com/artifact/org.json/json/20231013
        pause
        exit /b 1
    )
)

:: Set the classpath
set "CLASSPATH=%BUILD_DIR%;%LIB_DIR%\json.jar"

:: Check for required files
echo Checking required files...
if not exist "%SRC_DIR%\org\example\WeatherApp.java" (
    echo [ERROR] WeatherApp.java not found in %SRC_DIR%\org\example
    echo Please ensure the source code is in the correct location
    pause
    exit /b 1
)

if not exist "%WEBAPP_DIR%\index.html" (
    echo [ERROR] index.html not found in %WEBAPP_DIR%
    echo Please ensure all web files are present
    pause
    exit /b 1
)

if not exist "%WEBAPP_DIR%\result.html" (
    echo [ERROR] result.html not found in %WEBAPP_DIR%
    echo Please ensure all web files are present
    pause
    exit /b 1
)

:: Kill any process using port 8080
echo Checking if port 8080 is available...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    set "PID=%%a"
    echo Port 8080 is in use by process ID: !PID!
    echo Attempting to stop the process...
    taskkill /PID !PID! /F >nul 2>&1
    timeout /t 2 >nul
)

:: Clean and create build directory
echo Cleaning build directory...
if exist "%BUILD_DIR%" rd /s /q "%BUILD_DIR%"
mkdir "%BUILD_DIR%"

:: Copy webapp files
echo Copying web files...
xcopy "%WEBAPP_DIR%\*" "%BUILD_DIR%\webapp\" /E /I /Y >nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Failed to copy web files
    pause
    exit /b 1
)

:: Compile Java files
echo Compiling Java source files...
javac -encoding UTF-8 -cp "%CLASSPATH%" -d "%BUILD_DIR%" "%SRC_DIR%\org\example\WeatherApp.java"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed
    echo Please ensure you have the correct Java version installed ^(JDK 8 or higher^)
    pause
    exit /b 1
)

:: Verify compilation
if not exist "%BUILD_DIR%\org\example\WeatherApp.class" (
    echo [ERROR] Compilation failed - class file not found
    pause
    exit /b 1
)

:: Run the application
echo Starting Weather App server...
echo ====================================
echo The application will open in your default browser
echo To stop the server, close this window or press Ctrl+C
echo ====================================

cd /d "%BUILD_DIR%"
start http://localhost:8080
java -cp "%CLASSPATH%" org.example.WeatherApp

pause

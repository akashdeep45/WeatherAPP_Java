# Weather App

A simple Java-based weather application that shows current weather information for any city.

## Prerequisites

1. Java JDK 8 or higher
   - Download from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)
   - Make sure Java is added to your system's PATH

## Quick Start

1. Double-click `run.bat` to start the application
2. Your default web browser will open automatically to `http://localhost:8080`
3. Enter a city name to get the current weather information

## Manual Setup (if run.bat fails)

1. Ensure you have Java installed:
   ```
   java -version
   ```

2. Check that all required files are present:
   - `src/main/java/org/example/WeatherApp.java`
   - `src/main/webapp/index.html`
   - `src/main/webapp/result.html`
   - `json.jar` (in project root or lib directory)

3. If `json.jar` is missing, download it from [Maven Repository](https://mvnrepository.com/artifact/org.json/json/20231013)

## Troubleshooting

1. **Java not found**
   - Install Java JDK 8 or higher
   - Add Java to your system's PATH
   - Restart your computer after installation

2. **Port 8080 in use**
   - The script will automatically try to free port 8080
   - If it fails, manually close any applications using port 8080

3. **Missing files**
   - Ensure all files are in their correct locations as mentioned in Manual Setup
   - Download missing files if necessary

4. **Compilation errors**
   - Make sure you have the correct Java version installed
   - Check that all required files are present
   - Ensure json.jar is in the lib directory

## Project Structure

```
WeatherApp/
├── lib/
│   └── json.jar
├── src/
│   └── main/
│       ├── java/
│       │   └── org/
│       │       └── example/
│       │           └── WeatherApp.java
│       └── webapp/
│           ├── index.html
│           └── result.html
├── run.bat
└── README.md
```

## Support

If you encounter any issues:
1. Check the troubleshooting section above
2. Ensure all prerequisites are met
3. Try running the manual setup steps 
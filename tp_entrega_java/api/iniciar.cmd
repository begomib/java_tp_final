@echo off
setlocal
cd /d "%~dp0"
if exist "%JAVA_HOME%\bin\java.exe" (
  set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA_CMD=java"
)
set "APP_JAR=target\michi-navidad-fotos-api-1.0.0.jar"
if not exist "%APP_JAR%" set "APP_JAR=target-fotos\michi-navidad-fotos-api-1.0.0.jar"
if not exist "%APP_JAR%" (
  echo Primero compila el proyecto con Maven: mvn clean verify
  exit /b 1
)
"%JAVA_CMD%" -jar "%APP_JAR%" %*
@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ==========================================
echo  CESAVET - Servidor Java y archivo TXT
echo ==========================================
where javac >nul 2>&1
if errorlevel 1 (
 echo ERROR: No se encuentra javac. Instala JDK 17 o superior y anadelo a PATH.
 pause
 exit /b 1
)
if not exist bin mkdir bin
javac -encoding UTF-8 -d bin src\*.java
if errorlevel 1 (
 echo ERROR al compilar el proyecto.
 pause
 exit /b 1
)
start "" "http://localhost:8080"
echo El navegador abrira CESAVET en http://localhost:8080
echo NO CIERRES esta ventana mientras uses el sistema.
echo Los datos se guardan en datos_clinica.txt
java -cp bin Main
pause

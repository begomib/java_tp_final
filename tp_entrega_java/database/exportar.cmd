@echo off
setlocal
set "DUMP_CMD=D:\Programas\MySql\bin\mysqldump.exe"
if not exist "%DUMP_CMD%" (
  echo No se encuentra mysqldump.exe en D:\Programas\MySql\bin.
  echo Exporta techlab desde Workbench y guarda el archivo en database\techlab.sql.
  exit /b 1
)
if exist "%~dp0techlab.sql" (
  echo Ya existe database\techlab.sql. Renombralo si queres conservarlo antes de exportar otra vez.
  exit /b 1
)
echo Exportando techlab desde localhost:3306 con el usuario root.
echo Escribi la clave actual de MySQL cuando aparezca Enter password. No se mostrara.
"%DUMP_CMD%" --host=127.0.0.1 --port=3306 --user=root --password --single-transaction --no-tablespaces --set-gtid-purged=OFF --default-character-set=utf8mb4 --result-file="%~dp0techlab.sql" techlab
if errorlevel 1 (
  echo La exportacion fallo. No entregues el archivo SQL parcial.
  if exist "%~dp0techlab.sql" move /y "%~dp0techlab.sql" "%~dp0techlab-exportacion-fallida.txt" >nul
  exit /b 1
)
echo Exportacion terminada: database\techlab.sql
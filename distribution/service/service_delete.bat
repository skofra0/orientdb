@ECHO OFF
@ECHO.
@ECHO *********************************************
@ECHO ** EXISTING ETL VARIABLES                  **
@ECHO *********************************************
@ECHO ORIENTDB_HOME=%ORIENTDB_HOME%
@ECHO.
@ECHO TRY DELETE orientdb as service
SET CurrentDir=%~dp0
ECHO %CurrentDir%
PUSHD %CurrentDir%
orientdb.exe //DS//orientdb
pause:
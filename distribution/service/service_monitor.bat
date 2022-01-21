@ECHO OFF
@ECHO start orientdb monitor
SET CurrentDir=%~dp0
ECHO %CurrentDir%
PUSHD %CurrentDir%
call orientdbw.exe //MS//orientdb	
@ECHO OFF
@ECHO.
@ECHO ************************************
@ECHO ** NEXUS ORIENT DATABASE          **
@ECHO ************************************
@ECHO ORIENTDB_HOME=%ORIENTDB_HOME%
@ECHO.
@ECHO ************************************
@ECHO ** NEW FOLDER LOCATION            **
@ECHO ************************************
SET CurrentDir=%~dp0
ECHO %CurrentDir%
PUSHD %CurrentDir%
CD ..

SET ORIENTDB_HOME=%CD%
@ECHO ORIENTDB_HOME=%ORIENTDB_HOME%
@ECHO Confirm setting of ORIENTDB_HOME variable
PAUSE:
SETX /m ORIENTDB_HOME "%CD%"
CD service

set SERVICE_PATH=%ORIENTDB_HOME%\service
set SERVICE_NAME=orientdb
set PR_INSTALL=%SERVICE_PATH%\%SERVICE_NAME%.exe
set PR_DISPLAYNAME=Nexus OrientDB
set PR_DESCRIPTION=Nexus OrientDB Service
 
REM Service log configuration
set PR_LOGPREFIX=%SERVICE_NAME%
set PR_LOGPATH=%ORIENTDB_HOME%\log
set PR_STDOUTPUT=%ORIENTDB_HOME%\log\%SERVICE_NAME%_stdout.txt
set PR_STDERROR=%ORIENTDB_HOME%\log\%SERVICE_NAME%_stderr.txt
set PR_LOGLEVEL=Info
 
REM Path to java installation

FOR /F %%a IN ('%JAVA_HOME%\bin\java.exe -version 2^>^&1^|%windir%\system32\find /C "build 1.8"') DO (SET /a ISJAVA8=%%a)
IF %ISJAVA8% == 1 GOTO :USEJAVA8
:USEJAVA11
SET PR_JVM=%JAVA_HOME%\bin\server\jvm.dll
GOTO :CONTINUE
:USEJAVA8
SET PR_JVM=%JAVA_HOME%\jre\bin\server\jvm.dll
:CONTINUE
SET PR_OLD_JVM=auto
SET PR_OLD_CLASSPATH=nexus-orientdb.jar
SET PR_CLASSPATH=%ORIENTDB_HOME%\lib\*
 
REM Startup configuration
SET PR_STARTPATH=%ORIENTDB_HOME%\service
SET PR_STARTUP=auto
SET PR_STARTMODE=jvm
SET PR_STARTCLASS=no.nexus.OrientDbService
SET PR_STARTMETHOD=start
 
REM Shutdown configuration
SET PR_STOPMODE=jvm
SET PR_STOPCLASS=no.nexus.OrientDbService
SET PR_STOPMETHOD=stop
 
REM JVM configuration
SET PR_SERVICEUSER=LocalSystem
SET PR_JVMMS=768
SET PR_JVMMX=768
SET PR_JVMOPTIONS=-Duser.language=NO;-Duser.region=no;-DORIENTDB_HOME=%ORIENTDB_HOME%;-Dorientdb.config.file=%ORIENTDB_HOME%\config\orientdb-server-config.xml;-Dorientdb.www.path=%ORIENTDB_HOME%\www;-Djava.util.logging.config.file=%ORIENTDB_HOME%\config\orientdb-server-log.properties;-Dstorage.diskCache.bufferSize=1000
SET PR_JVMOPTIONS9=--add-opens=java.base/java.lang=ALL-UNNAMED;--add-opens=java.base/java.io=ALL-UNNAMED

 
REM Install service
%SERVICE_PATH%\%SERVICE_NAME%.exe //IS//%SERVICE_NAME%
PAUSE:

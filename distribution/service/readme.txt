*******************************************************************************
                              DEEM ORIENT DATABASE
*******************************************************************************

WINDOWS SERVICE

 QUICK START
---------------
1) Install the service by running the "service_install.bat" command.
   Remember to run the CMD prompt as an "Administrator"
2) Change the root user password in ORIENTDB_HOME\config\orientdb-server-config.xml
   Simple passord example:admin
3) Start the "Deem OrientDB" service
3) To Open Studio Web Tool, open a browser and point it to the URL:
   “http://localhost:2480”
 



 DATABASE EXPORT/IMPORT
-----------------------

1) Export by OriendDB Studio (DB -> Export -> Export Database)

2) Import by console.bat (Both remote and plocal can be used)
   E.g (Create and Import from export file):
   
CD %ORIENTDB_HOME%\bin
console.bat
CREATE DATABASE remote:localhost/test_00001 root admin
CONNECT remote:localhost/test_00001 admin admin
IMPORT DATABASE C:/tmp/desk.gz -preserveClusterIDs=true
CLOSE

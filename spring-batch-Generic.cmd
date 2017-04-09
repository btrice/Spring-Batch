@echo off
set codret=1
REM set jdk path
set JAVA_HOMME="C:\Program Files (x86)\Java\jdk1.6.0_07"

REM set log file path
set dLog=C:\Users\aidara\Desktop\OSS\Spring-batch-Generic\log\batchGeneric.log

REM set directory IN path 
set dIN=C:\Users\aidara\Desktop\OSS\Spring-batch-Generic\IN
REM set directory OUT path
set dOUT=C:\Users\aidara\Desktop\OSS\Spring-batch-Generic\OUT
REM set directory REPORT path
set dRAPORT=C:\Users\aidara\Desktop\OSS\Spring-batch-Generic\REPORT
REM set tag or release version of jar
set tag=v1.0
REM set jar file path
set jarFile=spring-batch-Generic-%tag%-SNAPSHOT.jar

REM execution of the batch
%JAVA_HOMME%\bin\java.exe -jar %jarFile% dOUT=%dOUT% dIN=%dIN% dRAPORT=%dRAPORT% dlog=%dLog%
echo *** code retour %ERRORLEVEL%
If errorlevel 1 goto fin
set codret=0
:fin
echo - Fin execution

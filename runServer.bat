@echo off
@set CURRDIR="%CD%\out\production\Chat\"
CD %CURRDIR%
java -version
@echo ON
@echo ****************************
@echo **------------------------**
@echo * Start chat Server *
@echo **------------------------**
@echo ****************************
@echo OFF
java -cp . com.javarush.Server
@pause
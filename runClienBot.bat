@echo off
@set CURRDIR="%CD%\out\production\Chat\"
CD %CURRDIR%
java -version
@echo ON
@echo ****************************
@echo **------------------------**
@echo * Start chat Client Bot*
@echo **------------------------**
@echo ****************************
@echo OFF
java -cp . com.javarush.client.BotClient
@pause
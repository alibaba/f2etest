@echo off
mode con cols=120
title Selenium Server (ÇëÎð¹Ø±Õ´Ë´°¿Ú)
cd c:\f2etest-webdriver\webdriver\
set f2etestHost="f2etest.xxx.com"
set nodeName="%1"
set browsers="IE 11,Chrome"
set timeout=60
node wdproxy.js %f2etestHost% %nodeName% %browsers% %timeout%
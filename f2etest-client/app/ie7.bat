@echo off

set proxypath="HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings"
reg add %proxypath% /v "ProxyEnable" /t REG_DWORD /d 0 /f>nul 

set proxymode="%1"
set proxyurl=%2
set url=%3
if not defined proxyurl (
	set proxyurl=""
)
if not defined url (
	set url="about:blank"
)

set proxydef=
if %proxyurl% equ "" set proxydef=1
if %proxyurl% equ default set proxydef=1
if %proxyurl% equ "default" set proxydef=1
if defined proxydef set proxyurl="http://f2etest.xxxx.com:4000/getpac?name=%USERNAME%"

if %proxymode% equ "noproxy" (
	set proxyurl=""
)

if %proxyurl% neq "" (
	rem Set Proxy
	reg add %proxypath% /v "AutoConfigURL" /d %proxyurl% /f >nul
) else (
	rem Remove Proxy		
	reg delete %proxypath% /v "AutoConfigURL" /f > nul
)

rem Set ie cache
reg add "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\5.0\Cache\Content" /v "CacheLimit" /t REG_DWORD /d 10240 /f >nul
reg add "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\Cache\Content" /v "CacheLimit" /t REG_DWORD /d 10240 /f >nul

start /MAX "" "C:\Program Files (x86)\Internet Explorer\iexplore.exe" %url%
set isWeb=1
if "%1" equ "" (
	set isWeb=0
)
start "" curl "http://f2etest.xxxx.com/applog?userid=%USERNAME%&appid=ie7&isweb=%isWeb%"
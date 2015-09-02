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

start /MAX "" "C:\360\360Chrome\Chrome\Application\360chrome.exe" %url% --user-data-dir="%APPDATA%\360chrome\User Data"
set isWeb=1
if "%1" equ "" (
	set isWeb=0
)
start "" curl "http://f2etest.xxxx.com/applog?userid=%USERNAME%&appid=360chrome&isweb=%isWeb%"
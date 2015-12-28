@echo off

rem 请这里配置f2etest的域名
set f2etestDomain=f2etest.xxx.com
set appid=ie11

rem 命令行参数
set proxymode="%1"
set proxyurl=%2
set url=%3
set apiKey=%4

rem 探测桌面模式
set isWeb=1
if %url% equ desktop (
	set url="about:blank"
	set isWeb=0
)

rem 设置代理
set proxypath="HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings"
reg add %proxypath% /v "ProxyEnable" /t REG_DWORD /d 0 /f>nul 
set proxydef=
if %proxyurl% equ "" set proxydef=1
if %proxyurl% equ default set proxydef=1
if %proxyurl% equ "default" set proxydef=1
if defined proxydef set proxyurl="http://%f2etestDomain%/getHostsPac?name=%USERNAME%"
if %proxymode% equ "noproxy" (
	set proxyurl=""
)
if %proxyurl% neq "" (
	rem 开启代理
	reg add %proxypath% /v "AutoConfigURL" /d %proxyurl% /f >nul
) else (
	rem 关闭代理
	reg delete %proxypath% /v "AutoConfigURL" /f > nul
)

rem 打开应用
start /MAX "" "c:\Program Files (x86)\Internet Explorer\iexplore.exe" %url%

rem 打点统计
start "" curl "http://%f2etestDomain%/applog?userid=%USERNAME%&appid=%appid%&isweb=%isWeb%"

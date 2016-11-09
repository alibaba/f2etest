@echo off

rem 请这里配置f2etest的域名
set f2etestDomain=f2etest.xxx.com
set appid=edge

rem 命令行参数
set proxymode="%1"
set proxyurl=%2
set url=%3
set apiKey=%4

rem 探测桌面模式
set isWeb=0
if exist "\\tsclient\G\Download" (
	set isWeb=1
)

rem 开启代理
set proxypath="HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings"
reg add %proxypath% /v "ProxyEnable" /t REG_DWORD /d 0 /f>nul 
set proxyurl="http://%f2etestDomain%/getHosts.pac?name=%USERNAME%"
reg add %proxypath% /v "AutoConfigURL" /d %proxyurl% /f >nul

rem 打开应用
start /MAX microsoft-edge:%url%

rem 打点统计
start "" curl "http://%f2etestDomain%/applog?userid=%USERNAME%&appid=%appid%&isweb=%isWeb%"

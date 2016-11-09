@echo off

rem 请这里配置f2etest的域名
set f2etestDomain=f2etest.xxx.com
set appid=hostsshare

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

rem 禁用代理
set proxy="HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings" 
reg add %proxy% /v "ProxyEnable" /t REG_DWORD /d 0 /f>nul 
reg delete "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings" /f /v AutoConfigURL

rem 打开应用
start "" "c:\hostsShare\hostsShare.exe" "http://%f2etestDomain%/" "%USERNAME%" %apiKey%

rem 打点统计
start "" curl "http://%f2etestDomain%/applog?userid=%USERNAME%&appid=%appid%&isweb=%isWeb%"
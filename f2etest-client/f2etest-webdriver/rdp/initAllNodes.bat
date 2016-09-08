@echo off
SETLOCAL ENABLEDELAYEDEXPANSION
for /f %%p in ('dir /b *.rdp') do (
	set p=%%p
	echo start /MAX C:\f2etest-webdriver\webdriver\opennode !p:~4,2! > "opennode!p:~4,2!.bat"
	move "opennode!p:~4,2!.bat" "C:\Users\node!p:~4,2!\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\Startup\opennode!p:~4,2!.bat"
	echo Node inited %%p
)
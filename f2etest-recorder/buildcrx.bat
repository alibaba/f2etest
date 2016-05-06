@echo off
C:\Users\Administrator\AppData\Local\Google\Chrome\Application\chrome.exe --pack-extension=%cd%\chrome-extension --pack-extension-key=%cd%\f2etest-recorder.pem
move chrome-extension.crx nodejs/chrome-extension/f2etest-recorder.crx
echo Build done!
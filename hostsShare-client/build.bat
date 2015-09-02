cd build
7z.exe a -tzip hostsShare.nw ../* -x!git/* -x!build/* -mx0
copy /b d:\node-webkit\nw.exe+hostsShare.nw hostsShare.exe 
copy d:\node-webkit\nw.pak nw.pak
copy d:\node-webkit\icudt.dll icudt.dll
del hostsShare.nw
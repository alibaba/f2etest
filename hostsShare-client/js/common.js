var platform = process.platform;
var isWin = platform.indexOf("win") > -1,
    isLinux = platform.indexOf("linux") > -1,
    isMac = platform.indexOf("darwin") > -1;
var ctrlKey = isMac? 'Cmd' : 'Ctrl';

function isIp(str){
    var match = str.match(/:/g), v6Len = match && match.length;
    if(match && v6Len>0 && v6Len<=7){
        return /^([\da-f]{0,4}(:|::|$)){1,7}$/i.test(str) && (/::/.test(str)?str.match(/::/g).length === 1:true);
    }
    else{
        return /^(\d{1,3}\.){3}\d{1,3}$/.test(str);
    }
}
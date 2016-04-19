var pool = require('./db');
var os = require('os');

// extend object
function extend(target) {
    for (var i = 1; i < arguments.length; i++) {
        var source = arguments[i],
            keys = Object.keys(source)

        for (var j = 0; j < keys.length; j++) {
            var name = keys[j]
            target[name] = source[name]
        }
    }

    return target
}

// check shExp match
function shExpMatch(text, exp){
    exp = exp.replace(/\.|\*|\?/g, function(c){
        return { '.': '\\.', '*': '.*?', '?': '.' }[c];
    });
    try{
        return new RegExp('^'+exp+'$').test(text);
    }
    catch(e){
        return false;
    }
}

function getLocalIP() {
    var ifaces = os.networkInterfaces();
    for (var dev in ifaces) {
        var iface = ifaces[dev];
        for (var i = 0; i < iface.length; i++) {
            var alias = iface[i];
            if (alias.family === 'IPv4' && alias.address !== '127.0.0.1' && !alias.internal){
                return alias.address;
            }
        }
    }
}

function checkApiKey(userid, apiKey, callback){
    pool.query('select count(0) count from appUsers where UserId = ? and ApiKey = ?', [userid, apiKey], function(err, rows){
        if(!err && rows[0].count === 1){
            callback(true);
        }
        else{
            callback(false);
        }
    });
}

// 字符串转换为数组
function str2Reg(arrStrs){
    var arrRegs = [];
    arrStrs.forEach(function(str){
        if(/^\/[^'"]+\/\w*$/.test(str)){
            arrRegs.push(eval(str));
        }
    });
    return arrRegs;
}


function dateFormat(date, format) {
    if(format === undefined){
        format = date;
        date = new Date();
    }
    var map = {
        "M": date.getMonth() + 1, //月份 
        "d": date.getDate(), //日 
        "h": date.getHours(), //小时 
        "m": date.getMinutes(), //分 
        "s": date.getSeconds(), //秒 
        "q": Math.floor((date.getMonth() + 3) / 3), //季度 
        "S": date.getMilliseconds() //毫秒 
    };
    format = format.replace(/([yMdhmsqS])+/g, function(all, t){
        var v = map[t];
        if(v !== undefined){
            if(all.length > 1){
                v = '0' + v;
                v = v.substr(v.length-2);
            }
            return v;
        }
        else if(t === 'y'){
            return (date.getFullYear() + '').substr(4 - all.length);
        }
        return all;
    });
    return format;
}
module.exports = {
    extend: extend,
    shExpMatch: shExpMatch,
    getLocalIP: getLocalIP,
    checkApiKey: checkApiKey,
    str2Reg: str2Reg,
    dateFormat: dateFormat
};
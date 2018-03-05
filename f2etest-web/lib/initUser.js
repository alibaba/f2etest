var crypto = require('crypto');
var async = require('async');
var pool =require('../lib/db.js');
var request = require('request');

var siteInfo = require('../conf/site.json');
var arrServerList = require('../conf/server.json');

function initUser(userid, callback){
    // 生成随机密码
    var md5sum = crypto.createHash('md5');
    md5sum.update(''+Math.random());
    var remotePassword = md5sum.digest('hex');

    // 生成apiKey
    md5sum = crypto.createHash('md5');
    md5sum.update(''+Math.random());
    var apiKey = md5sum.digest('hex');

    // 初始化远程所有账号
    var clientApiKey = siteInfo.clientApiKey || '';
    var arrTasks = [];
    arrServerList.forEach(function(server){
        arrTasks.push(function(callback){
            request('http://'+server.ip+'/setuser.asp?key='+encodeURIComponent(clientApiKey)+'&username='+encodeURIComponent(userid)+'&password='+encodeURIComponent(remotePassword), function(error, response, data){
                callback(error, {
                    ip: server.ip,
                    ret: data
                });
            });
        });
    });
    async.parallel(arrTasks, function(err, results){
        var allOk = true;
        var arrFailed = [];
        var errorMessage;
        for(var i=0,c=results.length;i<c;i++){
            result = results[i];
            if(!result || result.ret !== 'ok'){
                allOk = false;
                arrFailed.push(result.ip);
            }
        }
        if(allOk === true){
            var userInfo = {
                RemotePassword: remotePassword,
                ApiKey: apiKey
            };
            pool.query('update appUsers set ? where UserId = ?', [userInfo, userid]);
        }
        else{
            errorMessage = 'Some account initialize failed: '+arrFailed.join(',')
        }
        result = JSON.stringify(result);
        
        callback && callback(errorMessage, remotePassword)

    });
}

module.exports = initUser;
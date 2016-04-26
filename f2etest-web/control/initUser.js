var crypto = require('crypto');
var async = require('async');
var request = require('request');
var pool =require('../lib/db.js');

module.exports = function(app, config) {
    app.get('/initUser', function(req, res) {
        var query = req.query;
        var callback = query['callback'] || '';

        if(callback){
            res.type('js'); 
        }
        else{
            res.type('json'); 
        }

        var user = req.session.user;
        var userid = user.userid;
        var result = {};
        if(!user.remotePassword){
            // 生成随机密码
            var md5sum = crypto.createHash('md5');
            md5sum.update(''+Math.random());
            var remotePassword = md5sum.digest('hex');

            // 生成apiKey
            md5sum = crypto.createHash('md5');
            md5sum.update(''+Math.random());
            var apiKey = md5sum.digest('hex');

            // 初始化远程所有账号
            var arrServerList = config.arrServerList;
            var siteInfo = config.siteInfo;
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
                var result;
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
                    user.remotePassword = remotePassword;
                    result.message = 'All account initialized.';
                }
                else{
                    result.error = 'Some account initialize failed: '+arrFailed.join(',')
                }
                result = JSON.stringify(result);
                
                if(callback){
                    result = callback+'('+result+');';
                }

                res.end(result);
            });
        }
        else{
            result.error = 'Current user already initialized.';

            result = JSON.stringify(result);
            if(callback){
                result = callback+'('+result+');';
            }

            res.end(result);
        }
    });
};
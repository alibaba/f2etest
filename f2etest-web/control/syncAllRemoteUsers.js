var async = require('async');
var request = require('request');
var pool =require('../lib/db.js');

module.exports = function(app, config) {
    var siteInfo = config.siteInfo;
    var clientApiKey = siteInfo.clientApiKey || '';
    var arrServerList = config.arrServerList;
    app.get('/syncAllRemoteUsers', function(req, res) {
        pool.query('select UserId,RemotePassword from appUsers;', function(err, rows){
            var arrTasks = [];
            res.type('txt');
            res.write('Begin sync all remote users:\r\n');
            res.write('==================================\r\n');
            rows.forEach(function(row){
                var UserId = row.UserId;
                if(/^Administrator$/i.test(UserId) === false){
                    arrTasks.push(function(callback){
                        res.write(UserId+'\r\n');
                        syncRemoteUsers(UserId, row.RemotePassword, function(username, isOk){
                            res.write(UserId+' '+(isOk?'done':'failed')+'!\r\n');
                            res.write('---------------------\r\n');
                            callback();
                        });
                    });
                }
            });
            async.waterfall(arrTasks, function(){
                res.write('==================================\r\n');
                res.end('All done!');
            });
        });
    });

    function syncRemoteUsers(username, password, callback){
        var arrTasks = [];
        arrServerList.forEach(function(server){
            arrTasks.push(function(callback){
                request('http://'+server.ip+'/setuser.asp?key='+encodeURIComponent(clientApiKey)+'&username='+encodeURIComponent(username)+'&password='+encodeURIComponent(password), function(error, response, data){
                    callback(error, {
                        ip: server.ip,
                        ret: data
                    });
                });
            });
        });
        async.parallel(arrTasks, function(err, results){
            var allOk = true;
            var result;
            for(var i=0,c=results.length;i<c;i++){
                result = results[i];
                if(result.ret !== 'ok'){
                    allOk = false;
                    arrFailed.push(result.ip);
                }
            }
            callback(username, allOk);
        });
    }
};


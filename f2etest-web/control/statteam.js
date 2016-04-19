var pool =require('../lib/db.js');
var async = require('async');
var request = require('request');
var crypto = require('crypto');

var apiKey = 'f2etest-SM2_ME2AAI_$8$20Y3Av2a';

module.exports = function(app, config) {
    var siteInfo = config.siteInfo;
    var mapAppList = config.mapAppList;
    app.get('/statteam', function(req, res) {
        var user = req.session.user;
        var userid = user.userid;
        if(pool){
            var before30Day = new Date();
            before30Day.setDate(before30Day.getDate() - 29);
            before30Day = before30Day.getFullYear()+'-'+(before30Day.getMonth()+1)+'-'+before30Day.getDate();
            var Department;
            var ParentDepartment;
            async.waterfall([
                function(callback){
                    // 获取当着用户的部门
                    getBucInfo(userid, function(err, info){
                        if(info){
                            Department = info.depDesc;
                            ParentDepartment = Department.replace(/\-[^\-]*$/, '');
                            callback(null);
                        }
                    });
                },
                function(callback){
                    var arrTasks = [];
                    if(Department){
                        // 小团队次数 / 日
                        arrTasks.push(function(callback){
                            pool.query('select date_format(LogTime,"%Y%m%d") as LogDay,count(0) as value from appLogs left join appUsers on appLogs.UserId = appUsers.UserId where LogTime >= ? and appUsers.Department like ? group by LogDay order by LogDay;', [before30Day, Department+'%'], function(err, rows){
                                rows.forEach(function(row){
                                    row.name = row.LogDay.substr(4);
                                });
                                callback(null, rows);
                            });
                        });
                        // 小团队排名 (30天)
                        arrTasks.push(function(callback){
                            pool.query('select appUsers.Name as name,appUsers.NickName,count(0) as value from appLogs left join appUsers on appLogs.UserId = appUsers.UserId where LogTime >= ? and appUsers.Department like ? group by appLogs.UserId order by value desc limit 30;', [before30Day, Department+'%'], function(err, rows){
                                rows.forEach(function(row){
                                    row.name = row.NickName || row.name;
                                });
                                callback(null, rows);
                            });
                        });
                        // 小团队App排名 (30天)
                        arrTasks.push(function(callback){
                            pool.query('select appLogs.appId as appId,count(0) as value from appLogs left join appUsers on appLogs.UserId = appUsers.UserId where LogTime >= ? and appUsers.Department like ? group by appId order by value desc;', [before30Day, Department+'%'], function(err, rows){
                                rows.forEach(function(row){
                                    var appInfo = mapAppList[row.appId];
                                    if(appInfo){
                                        row.name = appInfo.name;
                                    }
                                });
                                callback(null, rows);
                            });
                        });
                        // 大团队次数 / 日
                        arrTasks.push(function(callback){
                            pool.query('select date_format(LogTime,"%Y%m%d") as LogDay,count(0) as value from appLogs left join appUsers on appLogs.UserId = appUsers.UserId where LogTime >= ? and appUsers.Department like ? group by LogDay order by LogDay;', [before30Day, ParentDepartment+'%'], function(err, rows){
                                rows.forEach(function(row){
                                    row.name = row.LogDay.substr(4);
                                });
                                callback(null, rows);
                            });
                        });
                        // 大团队排名 (30天)
                        arrTasks.push(function(callback){
                            pool.query('select appUsers.Name as name,appUsers.NickName,count(0) as value from appLogs left join appUsers on appLogs.UserId = appUsers.UserId where LogTime >= ? and appUsers.Department like ? group by appLogs.UserId order by value desc limit 30;', [before30Day, ParentDepartment+'%'], function(err, rows){
                                rows.forEach(function(row){
                                    row.name = row.NickName || row.name;
                                });
                                callback(null, rows);
                            });
                        });
                        // 大团队App排名 (30天)
                        arrTasks.push(function(callback){
                            pool.query('select appLogs.appId as appId,count(0) as value from appLogs left join appUsers on appLogs.UserId = appUsers.UserId where LogTime >= ? and appUsers.Department like ? group by appId order by value desc;', [before30Day, ParentDepartment+'%'], function(err, rows){
                                rows.forEach(function(row){
                                    var appInfo = mapAppList[row.appId];
                                    if(appInfo){
                                        row.name = appInfo.name;
                                    }
                                });
                                callback(null, rows);
                            });
                        });
                    }
                    async.parallel(arrTasks, callback);
                }
            ], function(err, results){
                var viewData = req.viewData;
                viewData.statNav = statNav;
                viewData.Department = Department;
                viewData.ParentDepartment = ParentDepartment;
                viewData.logTimeDayCurTeam = results[0];
                viewData.userOrderDayCurTeam = results[1];
                viewData.appOrderDayCurTeam = results[2];
                viewData.logTimeDayParentTeam = results[3];
                viewData.userOrderDayParentTeam = results[4];
                viewData.appOrderDayParentTeam = results[5];
                viewData.navTab = 'browser';
                viewData.navPage = 'stat';
                res.render('statteam', viewData);
            });
        }
    });
};

function getBucInfo(username, callback){
    var now = new Date();
    var Timestamp = now.toISOString();
    var hmac = crypto.createHmac('sha256', '_C6_&jQj_42d(E&@KS80n(&vd_KEYY2hQ@W0v&(x');
    var nonce = now.getTime()+Math.floor(Math.random()*10000);;

    var Signature = hmac.update('POST\n'+Timestamp+'\n'+nonce+'\n/rpc/enhancedUserQuery/getUserByLoginName.json\nloginName='+username).digest('base64');
    request({
        url: 'https://u-api.alibaba-inc.com/rpc/enhancedUserQuery/getUserByLoginName.json',
        form : {
            'loginName':username
        },
        method: 'POST',
        headers: {
            'X-Hmac-Auth-IP': '',
            'X-Hmac-Auth-MAC': '',
            'X-Hmac-Auth-Timestamp': Timestamp,
            'X-Hmac-Auth-Version': '1.0',
            'X-Hmac-Auth-Nonce': nonce,
            'apiKey': apiKey,
            'X-Hmac-Auth-Signature': Signature
        },
        json: true,
        timeout: 10000
    }, function (error, response, body) {
        var err = null;
        var user = null;
        if (!error && response.statusCode == 200) {
            callback(null, body.content)
        }
        else{
            callback(error);
        }
    });
}
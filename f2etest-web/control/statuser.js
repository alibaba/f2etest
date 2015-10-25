var pool =require('../lib/db.js');
var async = require('async');

module.exports = function(app, config) {
    var siteInfo = config.siteInfo;
    var mapAppList = config.mapAppList;
    app.get('/statuser', function(req, res) {
        var user = req.session.user;
        var userid = user.userid;
        if(pool){
            var before30Day = new Date();
            before30Day.setDate(before30Day.getDate() - 29);
            before30Day = before30Day.getFullYear()+'-'+(before30Day.getMonth()+1)+'-'+before30Day.getDate();
            var arrTasks = [];
            // 次数统计 / 日
            arrTasks.push(function(callback){
                pool.query('select date_format(LogTime,"%m%d") as LogDay,count(0) as value from appLogs where LogTime >= ? and UserId = ? group by LogDay order by LogDay;', [before30Day, userid], function(err, rows){
                    rows.forEach(function(row){
                        row.name = row.LogDay;
                    });
                    callback(null, rows);
                });
            });
            // App统计 (30天)
            arrTasks.push(function(callback){
                pool.query('select appId,count(0) as value from appLogs where LogTime >= ? and UserId = ? group by appId order by value desc;', [before30Day, userid], function(err, rows){
                    rows.forEach(function(row){
                        var appInfo = mapAppList[row.appId];
                        if(appInfo){
                            row.name = appInfo.name;
                        }
                    });
                    callback(null, rows);
                });
            });
            async.parallel(arrTasks, function(err, results){
                var viewData = req.viewData;
                viewData.statNav = statNav;
                viewData.logTimeDay = results[0];
                viewData.appOrder30day = results[1];
                res.render('statuser', viewData);
            });
        }
    });
}
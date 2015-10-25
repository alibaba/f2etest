var pool =require('../lib/db.js');
var async = require('async');

module.exports = function(app, config) {
    var siteInfo = config.siteInfo;
    var mapAppList = config.mapAppList;
    statNav = siteInfo.statNav || '';
    app.get('/stat', function(req, res) {
        if(pool){
            var arrTasks = [];
            var before30Day = new Date();
            before30Day.setDate(before30Day.getDate() - 29);
            before30Day = before30Day.getFullYear()+'-'+(before30Day.getMonth()+1)+'-'+before30Day.getDate();
            var before3Month = new Date();
            before3Month.setMonth(before3Month.getMonth()-2);
            before3Month = before3Month.getFullYear()+'-'+(before3Month.getMonth()+1)+'-'+before3Month.getDate();
            // 使用次数 / 日
            arrTasks.push(function(callback){
                pool.query('select date_format(LogTime,"%m%d") as name,count(0) as value from appLogs where LogTime >= ? group by name order by name;', before30Day, function(err, rows){
                    callback(null, rows);
                });
            });
            // 使用人数 / 日
            arrTasks.push(function(callback){
                pool.query('select date_format(LogTime,"%m%d") as name,count(DISTINCT UserId) as value from appLogs where LogTime >= ? group by name order by name;', before30Day, function(err, rows){
                    callback(null, rows);
                });
            });
            // 新增会员 / 日
            arrTasks.push(function(callback){
                pool.query('select date_format(ActiveTime,"%m%d") as ActiveDay,count(0) as value from appUsers where ActiveTime >= ? group by ActiveDay order by ActiveDay;', before30Day, function(err, rows){
                    rows.forEach(function(row){
                        row.name = row.ActiveDay;
                    });
                    callback(null, rows);
                });
            });
            // 活跃App排名 (30天)
            arrTasks.push(function(callback){
                pool.query('select appId as name,count(0) as value from appLogs where LogTime >= ? group by name order by value desc;', before30Day, function(err, rows){
                    rows.forEach(function(row){
                        var appInfo = mapAppList[row.name];
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
                viewData.arrLogTimeDay = results[0];
                viewData.logUserDay = results[1];
                viewData.newUserDay = results[2];
                viewData.appOrderDay = results[3];
                res.render('stat', viewData);
            });
        }
    });
}
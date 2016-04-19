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
            var before24Month = new Date();
            before24Month.setMonth(before24Month.getMonth()-24);
            before24Month = before24Month.getFullYear()+'-'+(before24Month.getMonth()+1)+'-'+before24Month.getDate();
            // 使用次数 / 日
            arrTasks.push(function(callback){
                pool.query('select date_format(LogTime,"%Y%m%d") as name,count(0) as value from appLogs where LogTime >= ? group by name order by name;', before30Day, function(err, rows){
                    rows = rows.filter(function(row){
                        row.name = row.name.substr(4);
                        return row;
                    });
                    callback(null, rows);
                });
            });
            // 使用次数 / 日
            arrTasks.push(function(callback){
                pool.query('select date_format(LogTime,"%Y%m") as name,count(0) as value from appLogs where LogTime >= ? group by name order by name;', before24Month, function(err, rows){
                    rows = rows.filter(function(row){
                        row.name = row.name.substr(4);
                        return row;
                    });
                    callback(null, rows);
                });
            });
            // 使用人数 / 日
            arrTasks.push(function(callback){
                pool.query('select date_format(LogTime,"%Y%m%d") as name,count(DISTINCT UserId) as value from appLogs where LogTime >= ? group by name order by name;', before30Day, function(err, rows){
                    rows = rows.filter(function(row){
                        row.name = row.name.substr(4);
                        return row;
                    });
                    callback(null, rows);
                });
            });
            // 使用人数 / 月
            arrTasks.push(function(callback){
                pool.query('select date_format(LogTime,"%Y%m") as name,count(DISTINCT UserId) as value from appLogs where LogTime >= ? group by name order by name;', before24Month, function(err, rows){
                    rows = rows.filter(function(row){
                        row.name = row.name.substr(4);
                        return row;
                    });
                    callback(null, rows);
                });
            });
            // 新增会员 / 日
            arrTasks.push(function(callback){
                pool.query('select date_format(ActiveTime,"%Y%m%d") as ActiveDay,count(0) as value from appUsers where ActiveTime >= ? group by ActiveDay order by ActiveDay;', before30Day, function(err, rows){
                    rows.forEach(function(row){
                        row.name = row.ActiveDay.substr(4);
                    });
                    callback(null, rows);
                });
            });
            // 新增会员 / 月
            arrTasks.push(function(callback){
                pool.query('select date_format(ActiveTime,"%Y%m") as ActiveDay,count(0) as value from appUsers where ActiveTime >= ? group by ActiveDay order by ActiveDay;', before24Month, function(err, rows){
                    rows.forEach(function(row){
                        row.name = row.ActiveDay.substr(4);
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
                viewData.arrLogTimeMonth = results[1];
                viewData.logUserDay = results[2];
                viewData.logUserMonth = results[3];
                viewData.newUserDay = results[4];
                viewData.newUserMonth = results[5];
                viewData.appOrderDay = results[6];
                viewData.navTab = 'browser';
                viewData.navPage = 'stat';
                res.render('stat', viewData);
            });
        }
    });
}
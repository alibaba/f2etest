var pool =require('../lib/db.js');
var async = require('async');

module.exports = function(app, config) {
    var siteInfo = config.siteInfo;
    var mapAppList = config.mapAppList;
    app.get('/statgroup', function(req, res) {
        if(pool){
            var arrTasks = [];
            var before30Day = new Date();
            before30Day.setDate(before30Day.getDate() - 29);
            before30Day = before30Day.getFullYear()+'-'+(before30Day.getMonth()+1)+'-'+before30Day.getDate();
            var before3Month = new Date();
            before3Month.setMonth(before3Month.getMonth()-2);
            before3Month = before3Month.getFullYear()+'-'+(before3Month.getMonth()+1)+'-'+before3Month.getDate();
            // 活跃会员排名 (30天)
            arrTasks.push(function(callback){
                pool.query('select CONCAT(appLogs.UserId, "(",appUsers.Company,")") as nameCompany,count(0) as value from appLogs left join appUsers on appLogs.UserId = appUsers.UserId where LogTime >= ? group by appLogs.UserId order by value desc limit 30;', before30Day, function(err, rows){
                    rows.forEach(function(row){
                        row.name = row.nameCompany;
                    });
                    callback(null, rows);
                });
            });
            // 活跃BU次数 (3个月)
            arrTasks.push(function(callback){
                pool.query('select appUsers.Company as Company,count(0) as value from appLogs left join appUsers on appLogs.UserId = appUsers.UserId where appLogs.LogTime >= ? and appUsers.Company is not null group by appUsers.Company order by value desc;', before3Month, function(err, rows){
                    rows.forEach(function(row){
                        row.name = row.Company;
                    });
                    callback(null, rows);
                });
            });
            // 活跃BU人数 (3个月)
            arrTasks.push(function(callback){
                pool.query('select appUsers.Company as Company,count(DISTINCT appUsers.UserId) as value from appLogs left join appUsers on appLogs.UserId = appUsers.UserId where appLogs.LogTime >= ? and appUsers.Company is not null group by appUsers.Company order by value desc;', before3Month, function(err, rows){
                    rows.forEach(function(row){
                        row.name = row.Company;
                    });
                    callback(null, rows);
                });
            });
            // BU总人数
            arrTasks.push(function(callback){
                pool.query('select Company, count(0) as value from appUsers where Company is not null and Company <> "" group by Company order by value desc;', function(err, rows){
                    rows.forEach(function(row){
                        row.name = row.Company;
                    });

                    callback(null, rows);
                });
            });
            // 地点总人数
            arrTasks.push(function(callback){
                pool.query('select Location, count(0) as value from appUsers where Location is not null and Location <> "" group by Location order by value desc;', function(err, rows){
                    rows.forEach(function(row){
                        row.name = row.Location;
                    });
                    callback(null, rows);
                });
            });
            // 职位总人数
            arrTasks.push(function(callback){
                pool.query('select Job, count(0) as value from appUsers where Job is not null and Job <> "" group by Job order by value desc limit 30;', function(err, rows){
                    rows.forEach(function(row){
                        row.name = row.Job;
                    });
                    callback(null, rows);
                });
            });
            async.parallel(arrTasks, function(err, results){
                var viewData = req.viewData;
                viewData.statNav = statNav;
                viewData.userOrderDay = results[0];
                viewData.logUserBuTime = results[1];
                viewData.logUserBuNumber = results[2];
                viewData.userBu = results[3];
                viewData.userLocation = results[4];
                viewData.userJob = results[5];
                viewData.navTab = 'browser';
                viewData.navPage = 'stat';
                res.render('statgroup', viewData);
            });
        }
    });
}
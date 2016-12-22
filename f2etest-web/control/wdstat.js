var pool =require('../lib/db.js');
var async = require('async');

module.exports = function(app, config) {
    var siteInfo = config.siteInfo;
    var mapAppList = config.mapAppList;
    statNav = siteInfo.statNav || '';
    app.get('/wd_stat', function(req, res) {
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
                pool.query('select date_format(log_time,"%Y%m%d") as name,count(0) as value from wd_logs where type = "browser" and log_time >= ? group by name order by name;', before30Day, function(err, rows){
                    rows = rows.filter(function(row){
                        row.name = row.name.substr(4);
                        return row;
                    });
                    callback(null, rows);
                });
            });
            // 使用次数 / 月
            arrTasks.push(function(callback){
                pool.query('select date_format(log_time,"%Y%m") as name,count(0) as value from wd_logs where type = "browser" and  log_time >= ? group by name order by name;', before24Month, function(err, rows){
                    rows = rows.filter(function(row){
                        row.name = row.name.substr(4);
                        return row;
                    });
                    callback(null, rows);
                });
            });
            async.parallel(arrTasks, function(err, results){
                var viewData = req.viewData;
                viewData.statNav = statNav;
                viewData.arrLogTimeDay = results[0];
                viewData.arrLogTimeMonth = results[1];
                viewData.navTab = 'webdriver';
                viewData.navPage = 'stat';
                res.render('wdstat', viewData);
            });
        }
    });
}
var pool =require('../lib/db.js');

module.exports = function(app, config) {
    var siteInfo = config.siteInfo;
    app.get('/applog', function(req, res) {
        var query = req.query;
        var userid = query['userid'] || '';
        var appid = query['appid'] || '';
        var isweb = query['isweb'] || '1';
        if(pool && userid && appid && isweb){
            pool.query('select count(0) count from appUsers where UserId = ?', [userid], function(err, rows){
                if(!err && rows[0].count === 1){
                    var objInsert = {
                        UserId: userid,
                        appId: appid,
                        isWeb: isweb
                    };
                    pool.query('insert into appLogs set ?', objInsert);
                    res.end('ok');
                }
                else{
                    res.end('error');
                }
            });
        }
        else{
            res.end('error');
        }
    });
}
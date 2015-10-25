module.exports = function(app, config) {
    var siteInfo = config.siteInfo;
    var mapAppList = config.mapAppList;
    app.get('/openapp', function(req, res) {
        var query = req.query;
        var id = query['id'] || '';
        var url = query['url'] || '';
        var noproxy = query['noproxy'] || '';
        var proxyurl = query['proxyurl'] || '';
        var appInfo = mapAppList[id];
        if(appInfo){
            var user = req.session.user;
            var userid = user.userid;
            var remotePassword = user.remotePassword;
            var apiKey = user.apiKey;
            var baseUrl = 'http://'+req.headers.host+req._parsedUrl.pathname.replace(/\/[^\/]+$/,'\/');
            var icon = baseUrl+'imgs/app/'+appInfo.id+'.png';
            var guacamoleUrl = siteInfo.guacamoleApi + '?id=c%2F'+appInfo.server+'&username='+encodeURIComponent(userid)+'&password='+encodeURIComponent(remotePassword)+'&title='+encodeURIComponent(appInfo.name)+'&icon='+encodeURIComponent(icon);
            var program = appInfo.program;
            if(program){
                program += ' '+(noproxy==='1'?'noproxy':'proxy');
                program += ' "'+(proxyurl?proxyurl:'default')+'"';
                program += ' "'+(url?url:'about:blank')+'"';
                program += ' '+apiKey;
                guacamoleUrl += '&program='+encodeURIComponent(program);
            }
            res.status(302).redirect(guacamoleUrl);
        }
        else{
            res.status(404).end();
        }
    });
}
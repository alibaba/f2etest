var url = require('url');
module.exports = function(app, config) {
    app.get('/anonymousSso/login', function(req, res) {
        var query = req.query;
        var BACK_URL = query['BACK_URL'];
        var cookies = req.cookies;
        var userInfo = cookies['ssoUserInfo'] || '';
        if(userInfo !== ''){
            userInfo = JSON.parse(userInfo);
        }
        else{
            userInfo = {};
        }
        if(userInfo.userid === undefined){
            var Userid = 'User'+(new Date()).getTime();
            userInfo.userid = Userid;
            userInfo.username = Userid;
        }
        userInfo.login = true;
        res.cookie('ssoUserInfo', JSON.stringify(userInfo), { maxAge: 315360000000});
        var SSO_TOKEN = userInfo.userid;
        if(BACK_URL){
            var backUrlInfo = url.parse(BACK_URL);
            res.redirect('http://'+backUrlInfo.host+'/sendSsoToken?SSO_TOKEN='+encodeURIComponent(SSO_TOKEN)+'&BACK_URL='+encodeURIComponent(BACK_URL));
        }
        else{
            res.end('BACK_URL required.');
        }
    });
    app.get('/anonymousSso/logout', function(req, res) {
        var query = req.query;
        var BACK_URL = query['BACK_URL'];
        var cookies = req.cookies;
        var userInfo = cookies['ssoUserInfo'] || '';
        if(userInfo !== ''){
            userInfo = JSON.parse(userInfo);
            userInfo.login = false;
            res.cookie('ssoUserInfo', JSON.stringify(userInfo), { maxAge: 315360000000});
        }
        if(BACK_URL){
            res.redirect(BACK_URL);
        }
        else{
            res.end('BACK_URL required.');
        }
    });
    app.post('/anonymousSso/getUser', function(req, res) {
        var query = req.query;
        var SSO_TOKEN = query['SSO_TOKEN'];
        res.end(JSON.stringify({
            hasError: false,
            content: JSON.stringify({
                emailPrefix: SSO_TOKEN,
                lastName: SSO_TOKEN
            })
        }));
    });
}
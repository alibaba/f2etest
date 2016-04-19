var hostsServer = require('../lib/hostsserver');
var pool = require('../lib/db.js');

function checkApiKey(name, apiKey, callback){
    pool.query('select count(0) count from appUsers where UserId = ? and ApiKey = ?', [name, apiKey], function(err, rows){
        if(!err && rows[0].count === 1){
            callback(true);
        }
        else{
            callback(false);
        }
    });
}

module.exports = function(app, config) {
    // 设置hosts
    app.all('/setHosts', function(req, res) {
        var query = req.query;
        var body = req.body;
        var callback = query['callback'] || '';
        var apiKey = query['apikey'] || body['apikey'] || '';
        var name = query['name'] || body['name'] || '';
        var hosts = query['hosts'] || body['hosts'] || '';
        checkApiKey(name, apiKey, function(isSuccess){
            var result;
            if(isSuccess){
                hostsServer.setHosts(name, hosts);
                result = {
                    message: 'ok'
                };
            }
            else{
                result = {
                    error: 'ApiKey check failed'
                };
            }

            result = JSON.stringify(result);

            if(callback){
                res.type('js'); 
                result = callback+'('+result+');';
            }
            else{
                res.type('json'); 
            }

            res.end(result);
        });
    });

    // 返回配置
    app.all('/getHostsConfig', function(req, res) {
        var query = req.query;
        var body = req.body;
        var callback = query['callback'] || '';
        var apiKey = query['apikey'] || body['apikey'] || '';
        var name = query['name'] || body['name'] || '';
        checkApiKey(name, apiKey, function(isSuccess){
            var result;
            if(isSuccess){
                var config = hostsServer.getConfig(name);
                result = {
                    message: config
                };
            }
            else{
                result = {
                    error: 'ApiKey check failed.'
                };
            }

            result = JSON.stringify(result);

            if(callback){
                res.type('js'); 
                result = callback+'('+result+');';
            }
            else{
                res.type('json'); 
            }

            res.end(result);
        });
    });

    // 设置反向代理模式
    app.all('/setHostsForward', function(req, res) {
        var query = req.query;
        var body = req.body;
        var callback = query['callback'] || '';
        var apiKey = query['apikey'] || body['apikey'] || '';
        var name = query['name'] || body['name'] || '';
        var forwardHost = query['forwardHost'] || body['forwardHost'] || '';
        var forwardPort = query['forwardPort'] || body['forwardPort'] || '';
        checkApiKey(name, apiKey, function(isSuccess){
            var result;
            if(isSuccess){
                if(forwardHost === 'local'){
                    forwardHost = (req.ip || '').replace(/^::ffff:/,'');
                }
                hostsServer.setForward(name, forwardHost, forwardPort);
                result = {
                    message: 'ok'
                };
            }
            else{
                result = {
                    error: 'ApiKey check failed.'
                };
            }

            result = JSON.stringify(result);

            if(callback){
                res.type('js'); 
                result = callback+'('+result+');';
            }
            else{
                res.type('json'); 
            }

            res.end(result);
        });
    });

    // pac接口，为浏览器提供动态代理
    app.all('/getHostsPac', function(req, res) {
        var query = req.query;
        var body = req.body;
        var name = query['name'] || '';
        hostsServer.getProxyPort(name, function(localIp, workPort){
            var pacContent = 'function FindProxyForURL(url, host){return "PROXY '+localIp+':'+workPort+'";}';
            res.end(pacContent);
        });
    });
};
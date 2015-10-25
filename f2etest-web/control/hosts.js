var fs = require('fs');
var os = require('os');
var path = require('path');
var HostsProxy = require('../lib/hostsproxy');
var pool = require('../lib/db.js');

var hostsFilePath = '../mapHosts.json';

var localIp = getLocalIP();

var mapHosts;
try{
    hostsFilePath = path.resolve(__dirname, hostsFilePath);
    mapHosts = fs.readFileSync(hostsFilePath);
    mapHosts = JSON.parse(mapHosts);
}
catch(e){
    mapHosts = {};
}
var mapProxy = {};

// 设置为hosts模式
function setHosts(name, hosts){
    var config = mapHosts[name] || {};
    config.hosts = hosts;
    config.mode = 'hosts';
    mapHosts[name] = config;
    fs.writeFileSync(hostsFilePath, JSON.stringify(mapHosts));
    var proxy = mapProxy[name];
    if(proxy){
        proxy.setHosts(hosts);
    }
    else{
        initProxy(name, config);
    }
}

// 设置为反向代理模式
function setForward(name, forwardHost, forwardPort){
    var config = mapHosts[name] || {};
    config.forwardHost = forwardHost;
    config.forwardPort = forwardPort;
    config.mode = 'forward';
    mapHosts[name] = config;
    fs.writeFileSync(hostsFilePath, JSON.stringify(mapHosts));
    var proxy = mapProxy[name];
    if(proxy){
        proxy.setForward(forwardHost, forwardPort);
    }
    else{
        initProxy(name, config);
    }
}

// 获取配置
function getConfig(name){
    var config = mapHosts[name] || {};
    mapHosts[name] = config;
    fs.writeFileSync(hostsFilePath, JSON.stringify(mapHosts));
    return config;
}

// 初始化代理
function initProxy(name, config, callback){
    var proxy = HostsProxy.createServer(config);
    proxy.on('error', function(e){
    });
    proxy.listen(0, function(msg){
        var port = msg.port;
        console.log('Proxy inited: ', name+' ( '+port+' )');
        if(callback){
            callback({
                port: port
            });
        }
        // console.log(name, config.mode, port)
    });
    mapProxy[name] = proxy;
}

// 返回代理的端口号，用在getpac接口
function getProxyPort(name, callback){
    var config = mapHosts[name] || {};
    mapHosts[name] = config;
    fs.writeFileSync(hostsFilePath, JSON.stringify(mapHosts));
    var proxy = mapProxy[name];
    if(proxy){
        callback(proxy.workPort)
    }
    else{
        initProxy(name, config, function(ret){
            callback(ret.port);
        });
    }
}

function getLocalIP() {
    var ifaces = os.networkInterfaces();
    for (var dev in ifaces) {
        var iface = ifaces[dev];
        for (var i = 0; i < iface.length; i++) {
            var alias = iface[i];
            if (alias.family === 'IPv4' && alias.address !== '127.0.0.1' && !alias.internal){
                return alias.address;
            }
        }
    }
}

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
                setHosts(name, hosts);
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
                var config = getConfig(name);
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
                setForward(name, forwardHost, forwardPort);
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
        getProxyPort(name, function(workPort){
            var pacContent = 'function FindProxyForURL(url, host){return "PROXY '+localIp+':'+workPort+'";}';
            res.end(pacContent);
        });
    });
};
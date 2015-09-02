var express = require('express');
var bodyParser = require('body-parser');
var fs = require('fs');
var os = require('os');
var HostsProxy = require('./lib/hostsproxy');


var listenPort = 4000;
var hostsFilePath = './mapHosts.json';

var localIp = getLocalIP();

var mapHosts;
try{
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

var app = express()

app.use(express.static(__dirname + '/public'));
app.use(bodyParser.urlencoded({ extended: true }));

// 设置hosts
app.all('/sethosts', function(req, res) {
    var query = req.query;
    var body = req.body;
    var name = query['name'] || body['name'] || '';
    var hosts = query['hosts'] || body['hosts'] || '';
    setHosts(name, hosts);
    res.send('ok')
});

// 返回配置
app.all('/getconfig', function(req, res) {
    var query = req.query;
    var body = req.body;
    var name = query['name'] || body['name'] || '';
    var config = getConfig(name);
    res.send(config)
});

// 设置反向代理模式
app.all('/setforward', function(req, res) {
    var query = req.query;
    var body = req.body;
    var name = query['name'] || body['name'] || '';
    var forwardHost = query['forwardHost'] || body['forwardHost'] || '';
    var forwardPort = query['forwardPort'] || body['forwardPort'] || '';
    setForward(name, forwardHost, forwardPort);
    res.send('ok')
});

// pac接口，为浏览器提供动态代理
app.all('/getpac', function(req, res) {
    var query = req.query;
    var body = req.body;
    var name = query['name'] || body['name'] || '';
    getProxyPort(name, function(workPort){
        var pacContent = 'function FindProxyForURL(url, host){return "PROXY '+localIp+':'+workPort+'";}';
        res.send(pacContent);
    });
});

app.listen(listenPort, function(){
    console.log("hostsShare server working on port \033[32m%s\033[39m.", listenPort);
});
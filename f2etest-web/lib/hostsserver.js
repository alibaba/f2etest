var fs = require('fs');
var path = require('path');
var utils = require('./utils');
var HostsProxy = require('./hostsproxy');

var hostsFilePath = '../mapHosts.json';

var localIp = utils.getLocalIP();

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
    // else{
    //     initProxy(name, config);
    // }
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
    // else{
    //     initProxy(name, config);
    // }
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
        proxy.workPort = port;
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
        callback(localIp, proxy.workPort)
    }
    else{
        initProxy(name, config, function(ret){
            callback(localIp, ret.port);
        });
    }
}



module.exports = {
    setHosts: setHosts,
    setForward: setForward,
    getConfig: getConfig,
    getProxyPort: getProxyPort
};
var HostsProxy = require('./lib/hostsproxy');
var request = require('request');
require('colors');

var proxy;
var server, name, apikey, port;

function startProxy(options, onFinish, onError){
    server = options.server;
    name = options.name;
    apikey = options.apikey;
    port = options.port || 0;
    var config = {
        mode: options.mode || 'hosts',
        hosts: options.hosts || '',
        forwardHost: options.forwardHost || '',
        forwardPort: options.forwardPort || ''
    }
    proxy = HostsProxy.createServer(config);
    proxy.on('error', function(e){
        onError && onError(e);
    });
    proxy.listen(port, function(msg){
        var port = msg.port;
        console.log('f2etest-local listen on: %s'.green, String(port).cyan);
        if(server && name && apikey){
            regToF2etest(function(error){
                if(error){
                    console.log(error.red);
                }
                else{
                    console.log('Your f2etest account '.green+name.cyan+' is now set proxy to local.'.green);
                    doHeartBeatToF2etest();
                }
                onFinish(msg);
            });
        }
        else{
            onFinish(msg);
        }
    });
}

// 关闭代理
function stopProxy(callback){
    proxy.close();
    unregToF2etest(function(){
        callback && callback();
    });
}

// 保持心跳，以保证IP有效性，解决IP变化的问题
function doHeartBeatToF2etest(){
    setInterval(function(){
        getF2etestConfig(function(error, ret){
            if(ret && ret.message && ret.message.mode === 'forward'){
                regToF2etest();
            }
        });
    }, 30000)
}

// 获得F2etest的hosts配置
function getF2etestConfig(callback){
    var url = server+'getHostsConfig?apikey='+apikey+'&name='+name;
    getJson(url, function(error, ret){
        if(ret && ret.error){
            error = ret.error;
        }
        callback && callback(error, ret);
    });
}

// 注册到F2etest
function regToF2etest(callback){
    var url = server+'setHostsForward?apikey='+apikey+'&name='+name+'&forwardHost=local&forwardPort='+port;
    getJson(url, function(error, ret){
        if(ret && ret.error){
            error = ret.error;
        }
        callback && callback(error);
    });
}

// 取消注册，恢复为hosts模式
function unregToF2etest(callback){
    getF2etestConfig(function(error, ret){
        if(ret && ret.message){
            var url = server+'setHosts?apikey='+apikey+'&name='+name;
            request.post({
                url: url,
                json: true,
                form: {
                    hosts: ret.message.hosts
                }
            },
            function(error, response, data){
                callback((error && error.toString()) || data.error);
            });
        }
        else{
            callback(error || ret.error);
        }
    });
}

// 获得HTTP JSON
function getJson(url, callback){
    request({
        url: url,
        json: true
    }, function(error, response, data){
        callback(error?error.toString():null, data);
    });
}

module.exports = {
    start: startProxy,
    stop: stopProxy
};

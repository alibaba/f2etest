var http = require('http');
var pkg = require('../package.json');

function getHttp(url, callback){
    var req = http.get(url, function(res){
        var arrBuffer = [];
        res.on('data', function(chunk){
            arrBuffer.push(chunk.toString())
        });
        res.on('end', function(){
            callback(null, arrBuffer.join(''))
        });
    }).on('error', function(error){
        callback(error);
    });
    req.on('socket', function (socket) {
        socket.setTimeout(500);
        socket.on('timeout', function() {
            req.abort();
        });
    });
}

function checkUpdate(callback){
    var registryUrl = pkg.publishConfig && pkg.publishConfig.registry || 'http://registry.npmjs.com';
    var npmUrl = registryUrl + '/f2etest-recorder/latest';

    getHttp(npmUrl, function(error, content){
        try{
            content = JSON.parse(content);
            var newVersion = content && content.version;
            var curVersion = pkg.version;
            if(newVersion && newVersion !== curVersion){
                console.log('[i] 发现新版本: v%s (当前版本: v%s)'.yellow, newVersion, curVersion);
                console.log('');
            }
        }
        catch(e){}
        callback();
    });
}

module.exports = checkUpdate;

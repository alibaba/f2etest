var http = require('http');
var WebDriver = require('./webdriver');
var utils = require('./utils');
var hostsServer = require('./hostsserver');
var async = require('async');

var mapSession = {};
function startHub(){
    var hubServer = http.createServer(function(req, res){
        if(req.url === '/wd/hub/session'){
            var body = [];
            req.on('data', function(chunk) {
                body.push(chunk);
            }).on('end', function() {
                body = Buffer.concat(body).toString();
                try{
                    var json = JSON.parse(body);
                    newWdSession(req.headers, json, function(error, result){
                        if(error){
                            endRes(error);
                        }
                        else{
                            endRes(null, result);
                        }
                    });
                }
                catch(e){
                    endRes('Capabilities json parse error!');
                }
            });
        }
        else{
            var match = req.url.match(/^\/wd\/hub\/session\/([^\/]+)/);
            if(match){
                var sessionInfo = mapSession[match[1]];
                if(sessionInfo){
                    var wdHost = sessionInfo.wdHost;
                    var wdPort = sessionInfo.wdPort;
                    var wdServer = http.request({
                        host: wdHost,
                        port: wdPort,
                        method: req.method,
                        path: req.url,
                        headers: req.headers,
                        agent: false
                    }, function (wdRes) {
                        res.writeHead(wdRes.statusCode, wdRes.headers);
                        wdRes.pipe(res);
                    });
                    wdServer.on('error', function (err) {
                        res.end();
                    });
                    req.pipe(wdServer);
                }
                else{
                    endRes('Session id match failed!');
                }
            }
            else{
                endRes('Session id required!');
            }
        }
        function endRes(error, result){
            if(error){
                res.writeHead(500, {'Content-Type': 'text/json'});
                res.end(JSON.stringify({
                    value: {
                        systemInformation: 'f2etest',
                        additionalInformation: '',
                        message: error
                    }
                }));
            }
            else{
                res.writeHead(200, {'Content-Type': 'text/json'});
                res.end(JSON.stringify(result));
            }
        }
    });
    hubServer.timeout = 1800000; // 请求30分钟超时
    hubServer.listen(4444);
}


function newWdSession(headers, json, callback){
    var desiredCapabilities = json.desiredCapabilities;
    var userid = desiredCapabilities['f2etest.userid'];
    var apiKey = desiredCapabilities['f2etest.apiKey'];
    var hosts = desiredCapabilities['f2etest.hosts'];
    var proxy = desiredCapabilities['f2etest.proxy'];
    delete desiredCapabilities['f2etest.userid'];
    delete desiredCapabilities['f2etest.apiKey'];
    delete desiredCapabilities['f2etest.hosts'];
    delete desiredCapabilities['f2etest.proxy'];
    var browserName = desiredCapabilities.browserName || '';
    browserName = browserName.toLowerCase();
    if(browserName === 'internet explorer'){
        browserName = 'ie';
    }
    var browserVersion = desiredCapabilities.version || '';
    browserVersion = browserVersion.toLowerCase();
    if(browserVersion === 'any'){
        browserVersion = '';
    }
    utils.checkApiKey(userid, apiKey, function(isSuccess){
        if(isSuccess){
            WebDriver.applyWdNode(userid, browserName, browserVersion, function(error, nodeInfo){
                if(error){
                    callback(error);
                }
                else{
                    var browserId = nodeInfo.browserId;
                    var wdHost = nodeInfo.wdHost;
                    var wdPort = nodeInfo.wdPort;
                    var arrTasks = [];
                    var hostsServerName = 'wdnodes_'+browserId;
                    // 初始化hosts&proxy
                    arrTasks.push(function(callback){
                        if(proxy){
                            var arrProxy = proxy.split(':');
                            hostsServer.setForward(hostsServerName, arrProxy[0], arrProxy[1]);
                        }
                        else{
                            hostsServer.setHosts(hostsServerName, hosts || '');
                        }
                        hostsServer.getProxyPort(hostsServerName, function(localIp, workPort){
                            callback(null, localIp+':'+workPort);
                        });
                    });
                    // 初始化session
                    arrTasks.push(function(proxy, callback){
                        browserName = browserName === 'ie' ? 'internet explorer' : browserName.toLowerCase();
                        desiredCapabilities.browserName = browserName;
                        desiredCapabilities.proxy = {
                            'proxyType': 'manual',
                            'httpProxy': proxy,
                            'sslProxy': proxy
                        };
                        body = JSON.stringify(json);
                        headers['content-length'] = body.length;
                        headers.host = wdHost+':'+wdPort;
                        var wdServer = http.request({
                            host: wdHost,
                            port: wdPort,
                            method: 'POST',
                            path: '/wd/hub/session',
                            headers: headers,
                            agent: false
                        }, function (wdRes) {
                            var body = [];
                            wdRes.on('data', function(chunk) {
                                body.push(chunk);
                            }).on('end', function() {
                                body = Buffer.concat(body).toString();
                                var sessionJson;
                                try{
                                    sessionJson = JSON.parse(body);
                                }
                                catch(e){
                                    return callback('New session json parse error!');
                                }
                                if(sessionJson.status === 0){
                                    var sessionId = sessionJson.sessionId;
                                    var capabilities = sessionJson.value;
                                    capabilities['f2etest.browserId'] = browserId;
                                    capabilities['f2etest.wdHost'] = wdHost;
                                    capabilities['f2etest.wdPort'] = wdPort;
                                    mapSession[sessionId] = {
                                        wdHost: wdHost,
                                        wdPort: wdPort
                                    };
                                    callback(null, {
                                        state: null,
                                        status: 0,
                                        sessionId: sessionId,
                                        value: capabilities
                                    });
                                }
                                else{
                                    callback(sessionJson.value.message);
                                }
                            });
                        });
                        wdServer.write(body);
                        wdServer.end();
                    });
                    async.waterfall(arrTasks, function(err, result){
                        if(err){
                            callback(err);
                        }
                        else{
                            pool.query('insert into wd_logs set type = "browser", userid = ?, data = ?, log_time = now()', [userid, browserId]);
                            callback(null, result);
                        }
                    });
                }
            });
        }
        else{
            callback('ApiKey check failed!');
        }
    });
}

module.exports = {
    start: startHub
}
var http = require('http');
var WebDriver = require('./webdriver');
var utils = require('./utils');

function startHub(){
    var mapSession = {};
    var hubServer = http.createServer(function(req, res){
        if(req.url === '/wd/hub/session'){
            var body = [];
            req.on('data', function(chunk) {
                body.push(chunk);
            }).on('end', function() {
                body = Buffer.concat(body).toString();
                try{
                    var json = JSON.parse(body);
                    var desiredCapabilities = json.desiredCapabilities;
                    var userid = desiredCapabilities['f2etest.userid'];
                    var apiKey = desiredCapabilities['f2etest.apiKey'];
                    var hosts = desiredCapabilities['f2etest.hosts'];
                    var proxy = desiredCapabilities['f2etest.proxy'];
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
                            WebDriver.applyWdBrowser(userid, browserName, browserVersion, hosts, proxy, function(err, result){
                                if(err){
                                    endRes(err);
                                }
                                else{
                                    mapSession[result.sessionId] = result;
                                    endRes(null, {
                                        state: null,
                                        status: 0,
                                        sessionId: result.sessionId,
                                        value: result.capabilities
                                    });
                                }
                                
                            });
                        }
                        else{
                            endRes('ApiKey check failed!');
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
                    var capabilities = sessionInfo.capabilities;
                    var wdHost = capabilities['f2etest.wdHost'];
                    var wdPort = capabilities['f2etest.wdPort'];
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

module.exports = {
    start: startHub
}
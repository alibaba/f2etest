var http = require('http');
var path = require('path');
var cp = require('child_process');

var proxyPath = 'HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings';

var nodeId = process.argv[2];
if(nodeId){
    nodeId = parseInt(nodeId, 10);
}
else{
    console.log('Please input nodeid!');
    process.exit(1);
}
var timeout = process.argv[3] || 60;

var proxyPort = 4000 + nodeId;
var webdriverPort = 5000 + nodeId;
var server = http.createServer(function(req, res){
    var wdServer;
    if(req.url === '/wd/hub/session'){
        var body = [];
        req.on('data', function(chunk) {
            body.push(chunk);
        }).on('end', function() {
            body = Buffer.concat(body).toString();
            try{
                var json = JSON.parse(body);
                var desiredCapabilities = json.desiredCapabilities;
                var browserName = desiredCapabilities.browserName.toLowerCase();
                var proxy = desiredCapabilities.proxy;
                if(proxy && browserName === 'internet explorer'){
                    var proxyType = proxy.proxyType || '';
                    proxyType = proxyType.toLowerCase()
                    var proxyAutoconfigUrl = proxy.proxyAutoconfigUrl;
                    var httpProxy = proxy.httpProxy;
                    switch(proxyType){
                        case 'manual':
                            if(httpProxy){
                                setProxy(httpProxy);
                            }
                            break;
                        case 'pac':
                            if(proxyAutoconfigUrl){
                                setPac(proxyAutoconfigUrl);
                            }
                            break;
                        default:
                            disableProxy();
                    }
                    desiredCapabilities.proxy = {
                        'proxyType': 'SYSTEM'
                    };
                    body = JSON.stringify(json);
                }
            }
            catch(e){}
            var headers = req.headers;
            headers['content-length'] = body.length;
            wdServer = http.request({
                host: '127.0.0.1',
                port: webdriverPort,
                method: req.method,
                path: req.url,
                headers: headers,
                agent: false
            }, function (wdRes) {
                res.writeHead(wdRes.statusCode, wdRes.headers);
                wdRes.pipe(res);
            });
            wdServer.write(body);
            wdServer.end();
        });
    }
    else{
        wdServer = http.request({
            host: '127.0.0.1',
            port: webdriverPort,
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
});

server.listen(proxyPort, function(){
    console.log('F2etest WebDriver proxy is ready: %s', proxyPort);
    var jarPath = path.resolve(__dirname, './selenium-server-standalone-2.53.0.jar');
    cp.spawn('java', [
            '-jar',
            jarPath,
            '-port',
            webdriverPort,
            '-timeout',
            timeout,
            '-browserTimeout',
            timeout
        ], {
            stdio: 'inherit'
        }
    );
});

function setProxy(proxyHost){
    cp.execSync('reg add "'+proxyPath+'" /v "ProxyEnable" /t REG_DWORD /d "1" /f >nul');
    cp.execSync('reg add "'+proxyPath+'" /v "AutoConfigURL" /d "" /f >nul');
    cp.execSync('reg add "'+proxyPath+'" /v "ProxyServer" /d "'+proxyHost+'" /f >nul');
	console.log('System proxy inited:', proxyHost);
}

function setPac(pacUrl){
    cp.execSync('reg add "'+proxyPath+'" /v "ProxyEnable" /t REG_DWORD /d "0" /f >nul');
    cp.execSync('reg add "'+proxyPath+'" /v "AutoConfigURL" /d "'+pacUrl+'" /f >nul');
	console.log('System proxy inited:', pacUrl);
}

function disableProxy(){
    cp.execSync('reg add "'+proxyPath+'" /v "ProxyEnable" /t REG_DWORD /d "0" /f >nul');
    cp.execSync('reg add "'+proxyPath+'" /v "AutoConfigURL" /d "" /f >nul');
	console.log('System proxy disabled');
}
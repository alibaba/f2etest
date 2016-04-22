var http = require('http');
var path = require('path');
var url = require('url');
var cp = require('child_process');

var proxyPath = 'HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings';

var f2etestHost = process.argv[2];
var nodeName = process.argv[3];
var nodeId;
if(nodeName){
    nodeId = parseInt(nodeName, 10);
}
else{
    console.log('Please input nodeid!');
    process.exit(1);
}
var browsers = process.argv[4];
var timeout = process.argv[5] || 60;

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
    setTimeout(checkWorkStatus, 3000);
});

function checkWorkStatus(){
    var wdServer = http.request({
        host: '127.0.0.1',
        port: webdriverPort,
        method: 'GET',
        path: '/wd/hub/sessions',
        agent: false
    }, function (wdRes) {
        var body = [];
        wdRes.on('data', function(chunk) {
            body.push(chunk);
        }).on('end', function() {
            body = Buffer.concat(body).toString();
            var wdStatus = 0;
            try{
                var json = JSON.parse(body);
                if(json.status === 0){
                    wdStatus = json.value.length > 0 ? 2: 1;
                }
            }
            catch(e){}
            reportToF2etest(wdStatus);

        });
    }).on('error', function(){
        reportToF2etest(0);
    });
    wdServer.end();
    setTimeout(checkWorkStatus, 5000);
}

function reportToF2etest(wdStatus){
    var reportUrl = 'http://'+f2etestHost+'/reportWdNode?nodename='+nodeName+'&browsers='+encodeURIComponent(browsers)+'&rdp=1&status='+wdStatus;
    var urlInfo = url.parse(reportUrl);
    http.get({
        hostname: urlInfo.hostname,
        port: urlInfo.port,
        path: urlInfo.path,
        agent: false
    }, function(res){
        if(res.statusCode === 200){
            // console.log('Report to f2etest: '+wdStatus);
        }
        else{
            console.log('Report to f2etest failed!');
        }
    }).on('error', function(e){
        console.log('Report to f2etest failed!');
    });
}

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
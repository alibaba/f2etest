var request = require('request');
var extend = require('xtend');

function runJsUnitMulti(config, callback){
    var browsers = config.browsers;
    if(browsers){
        var arrBrowsers = browsers.split(/\s*,\s*/);
        var browserCount = arrBrowsers.length;
        var runCount = 0;
        arrBrowsers.forEach(function(browser){
            var arrBrowser = browser.split(/\s+/);
            var newConfig = extend(config, {
                browserName: arrBrowser[0],
                browserVersion: arrBrowser[1]
            })
            runJsUnit(newConfig, function(error, result){
                runCount ++;
                callback(error, result, runCount === browserCount);
            });
        });
    }
    else{
        callback('Parameter browsers missed!', null, 1)
    }
}


function runJsUnit(config, callback){
    var f2etestServer = config.server;
    if(f2etestServer.substr(-1) !== '/'){
        f2etestServer += '/';
    }
    var userid = config.userid;
    var apiKey = config.apiKey;
    var url = config.url;
    var browserName = config.browserName;
    var browserVersion = config.browserVersion;
    var hosts = config.hosts;
    var coverageInclude = config.coverageInclude;
    var coverageExclude = config.coverageExclude;
    var coverageBeautify = config.coverageBeautify;
    var timeout = config.timeout;
    var delay = config.delay;
    if(f2etestServer && userid && apiKey && url && browserName){
        var mapQueryString = {
            userid: userid,
            apiKey: apiKey,
            url: url,
            browserName: browserName,
            browserVersion: browserVersion,
            hosts: hosts,
            coverageInclude: coverageInclude,
            coverageExclude: coverageExclude,
            coverageBeautify: coverageBeautify,
            timeout: timeout,
            delay: delay
        };
        request.get({
            url: f2etestServer + 'runJsUnit',
            qs: mapQueryString,
            timeout: 600000
        },
        function(error, response, body){
            if(error){
                doCallback(error, config);
            }
            else{
                try{
                    var data = JSON.parse(body);
                    if(data.error){
                        doCallback(data.error, config);
                    }
                    else{
                        doCallback(null, data.message);
                    }
                }
                catch(e){
                    doCallback(body, config);
                }
            }
        });
    }
    else{
        doCallback('Some parameter missed: server, userid, apiKey, url, browserName');
    }
    function doCallback(error, result){
        if(callback){
            callback(error, result);
        }
    }
}

module.exports = {
    run: runJsUnitMulti
};

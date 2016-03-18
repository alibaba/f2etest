var JWebDriver = require('jWebDriver');
var request = require('request');
var co = require('co');

function runBrowser(config, callback){
    return new Promise(function(resolve, reject){
        var f2etestServer = config.server;
        var userid = config.userid;
        var apiKey = config.apiKey;
        var browserName = config.browserName;
        var browserVersion = config.browserVersion;
        var hosts = config.hosts;
        var proxy = config.proxy;
        var logLevel = config.logLevel;
        var nocolor = config.nocolor;
        var speed = config.speed;
        if(f2etestServer && userid && apiKey && browserName){
            var mapQueryString = {
                userid: userid,
                apiKey: apiKey,
                browserName: browserName,
                browserVersion: browserVersion,
                hosts: hosts,
                proxy: proxy
            };
            request.get({
                url: f2etestServer + 'applyWdBrowser',
                qs: mapQueryString,
                json: true
            },
            function(error, response, data){
                if(error || data.error){
                    doCallback(error || data.error);
                }
                else{
                    var wdInfo = data.message;
                    var driver = new JWebDriver({
                        'host': wdInfo.wdHost,
                        'port': wdInfo.wdPort,
                        'logLevel': logLevel,
                        'nocolor': nocolor,
                        'speed': speed
                    });
                    driver.session({
                      sessionId: wdInfo.wdSessionId
                    }, function*(error, browser){
                        browser.browserId = wdInfo.browserId;
                        doCallback(error, browser);
                    });
                }
            });
        }
        else{
            doCallback('Some parameter missed: server, userid, apiKey, browserName');
        }
        function doCallback(error, result){
            if(callback){
                co(callback(error, result));
            }
            error?reject(error):resolve(result);
        }
    });
}

module.exports = {
    run: runBrowser
};

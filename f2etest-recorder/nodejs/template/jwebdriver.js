var expect  = require('expect.js');
var faker  = require('faker');

module.exports = function(browser, testVars){

    before(function(){
        browser = browser || this.browser;
        testVars = testVars || this.testVars;
    });

{$testCodes}
};

if(module.parent && /mocha\.js/.test(module.parent.id)){
    runThisSpec();
}

function runThisSpec(){
    var JWebDriver = require('jwebdriver');
    require('mocha-generators').install();
    var fs = require('fs');
    var path = require('path');

    // read config
    var config = require('./config.json');
    var f2etestConfig = config.f2etest;
    var testVars = config.vars;
    var browsers = f2etestConfig.browsers;
    browsers = browsers.replace(/^\s+|\s+$/g, '');

    // read hosts
    var hostsPath = './hosts';
    var hosts = '';
    if(fs.existsSync(hostsPath)){
        hosts = fs.readFileSync(hostsPath).toString();
    }

    var filename = path.basename(__filename);

    browsers.split(/\s*,\s*/).forEach(function(browser){
        var browserInfo = browser.split(' ');
        var browserName = browserInfo[0];
        var browserVersion = browserInfo[1];

        describe(filename + ' : ' + browser, function(){

            this.timeout(600000);

            var browser;
            before(function*(){
                var driver = new JWebDriver({
                    'host': f2etestConfig.server
                });
                browser = yield driver.session({
                    'f2etest.userid': f2etestConfig.userid,
                    'f2etest.apiKey': f2etestConfig.apiKey,
                    'f2etest.hosts': hosts,
                    'browserName': browserName,
                    'version': browserVersion,
                    'ie.ensureCleanSession': true
                });
                yield browser.maximize();
                var browerInfo = yield browser.info();
                var browserId = browerInfo['f2etest.browserId'];
                var f2etestUrl = 'http://'+f2etestConfig.server+'/openWdBrowser?browserId='+browserId;
                // browserId && openUrl(f2etestUrl);
                this.browser = browser;
                this.testVars = testVars;
            });

            module.exports();

            after(function*(){
                if(browser){
                    yield browser.close();
                }
            });

        });
    });
}

function callSpec(name){
    try{
        require('./'+name)();
    }
    catch(e){
        console.log(e)
        process.exit(1);
    }
}

function openUrl(url){
    var startCmd = '';
    switch(process.platform){
        case 'win32':
        case 'win64':
            startCmd = 'start ""';
        break;
        case 'unix':
        case 'linux':
            startCmd = 'xdg-open';
        break;
        // case 'darwin':
        default:
            startCmd = 'open';
    }
    require('child_process').exec(startCmd + ' "' + url+'"');
}

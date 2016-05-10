var path = require('path');
var fs = require('fs');
var os = require('os');
var http = require('http');
var url = require('url');
var cp = require('child_process');
var inquirer = require('inquirer');
var JWebDriver = require('jwebdriver');
var async = require('async');
var co = require('co');
var expect = require('expect.js');
require('colors');

var symbols = {
  ok: '✓',
  err: '✖'
};
if (process.platform === 'win32') {
  symbols.ok = '\u221A';
  symbols.err = '\u00D7';
}

function startRecorder(){
    var configFile = path.resolve('config.json');
    var configJson = {};
    if(fs.existsSync(configFile)){
        var content = fs.readFileSync(configFile).toString();
        try{
            configJson = JSON.parse(content);
        }
        catch(e){
            console.log('config.json json parse failed.'.red);
            process.exit(1);
        }
    }
    else{
        console.log('config.json missed!'.red);
        process.exit(1);
    }
    var f2etestConfig = configJson.f2etest;
    var testVars = configJson.vars;
    var hostsFile = path.resolve('hosts');
    var hosts = '';
    if(fs.existsSync(hostsFile)){
        hosts = fs.readFileSync(hostsFile).toString();
    }
    var questions = [
        {
            'type': 'input',
            'name': 'fileName',
            'message': '请输入测试文件名',
            'default': 'test.spec.js',
            'validate': function(input){
                return input !== '';
            }
        },
        {
            'type': 'confirm',
            'name': 'checker',
            'message': '打开同步校验浏览器？',
            'default': true
        },
        {
            'type': 'input',
            'name': 'xpathAttrs',
            'message': 'XPath属性配置',
            'default': 'id,name,type,data-id,data-name,data-type,data-role'
        }
    ];
    inquirer.prompt(questions).then(function(anwsers){
        var fileName = anwsers.fileName;
        var openChecker = anwsers.checker;
        var xpathAttrs = anwsers.xpathAttrs;

        var arrTestCodes = [];
        var recorderBrowser, checkerBrowser;
        var lastWindowId = 0;
        var lastFrameId = null;
        function pushTestCode(title, codes){
            arrTestCodes.push('it("'+title.replace(/"/g, '\\"')+'", function*(){');
            if(Array.isArray(codes)){
                codes.forEach(function(line){
                    arrTestCodes.push('    '+line);
                });
            }
            else{
                arrTestCodes.push('    '+codes);
            }
            arrTestCodes.push("});");
            arrTestCodes.push("");
            title = title.replace(/^\w+:/, function(all){
                return all.cyan;
            });
            console.log(title);
        }
        var cmdQueue = async.queue(function(cmdInfo, next) {
            var window = cmdInfo.window;
            var frame = cmdInfo.frame;
            var cmd = cmdInfo.cmd;
            var data = cmdInfo.data;
            var arrTasks = [];
            arrTasks.push(function(callback){
                function doNext(){
                    if(checkerBrowser){
                        console.log(' '+symbols.ok.green+' Check successed.'.green);
                    }
                    callback();
                }
                function catchError(error){
                    if(checkerBrowser){
                        console.log(' '+symbols.err.red+' Check failed!'.red, error);
                    }
                    callback();
                }
                if(window !== lastWindowId){
                    lastWindowId = window;
                    lastFrameId = null;
                    pushTestCode('switchWindow: '+window, 'yield browser.switchWindow('+window+');')
                    checkerBrowser && checkerBrowser.switchWindow(window).then(doNext).catch(catchError) || callback();
                }
                else{
                    callback();
                }
            });
            arrTasks.push(function(callback){
                function doNext(){
                    if(checkerBrowser){
                        console.log(' '+symbols.ok.green+' Check successed.'.green);
                    }
                    callback();
                }
                function catchError(error){
                    if(checkerBrowser){
                        console.log(' '+symbols.err.red+' Check failed!'.red, error);
                    }
                    callback();
                }
                if(frame !== lastFrameId){
                    lastFrameId = frame;
                    var arrCodes = [];
                    arrCodes.push('yield browser.switchFrame(null);');
                    if(frame !== null){
                        arrCodes.push('yield browser.wait("'+frame+'", 30000).switchFrame("'+frame+'");');
                    }
                    pushTestCode('switchFrame: ' + frame, arrCodes);
                    checkerBrowser && checkerBrowser.switchFrame(null, function(error){
                        if(frame !== null){
                            return checkerBrowser.wait(frame).switchFrame(frame);
                        }
                    }).then(doNext).catch(catchError) || doNext();
                }
                else{
                    callback();
                }

            });
            arrTasks.push(function(callback){
                function doNext(){
                    if(checkerBrowser){
                        console.log(' '+symbols.ok.green+' Check successed.'.green);
                    }
                    callback();
                }
                function catchError(error){
                    if(checkerBrowser){
                        console.log(' '+symbols.err.red+' Check failed!'.red, error);
                    }
                    callback();
                }
                var arrCodes = [];
                switch(cmd){
                    case 'url':
                        pushTestCode('url: ' + data.url, 'yield browser.url("'+data.url+'");');
                        checkerBrowser && checkerBrowser.url(data.url).then(doNext).catch(catchError) || doNext();
                        break;
                    case 'closeWindow':
                        pushTestCode('closeWindow:', 'yield browser.closeWindow();');
                        checkerBrowser && checkerBrowser.closeWindow().then(doNext).catch(catchError) || doNext();
                        break;
                    case 'sleep':
                        pushTestCode('sleep:' + data.time, 'yield browser.sleep('+data.time+');');
                        checkerBrowser && checkerBrowser.sleep(data.time).then(doNext).catch(catchError) || doNext();
                        break;
                    case 'target':
                        arrCodes = [];
                        arrCodes.push('var element = yield browser.wait("'+data.xpath+'", 30000);');
                        arrCodes.push('expect(element.length).to.be(1);');
                        arrCodes.push('yield browser.mouseMove("'+data.xpath+'");');
                        pushTestCode('target:' + data.xpath, arrCodes);
                        checkerBrowser && checkerBrowser.wait(data.xpath, 30000).then(function(element){
                            expect(element.length).to.be(1);
                        }).mouseMove(data.xpath).then(doNext).catch(catchError) || doNext();
                        break;
                    case 'mouseMove':
                        if(data.x !== undefined){
                            pushTestCode('mouseMove: '+data.xpath+', '+data.x+', '+data.y, 'yield browser.mouseMove("'+data.xpath+'", '+data.x+', '+data.y+');');
                        }
                        else{
                            pushTestCode('mouseMove: '+data.xpath, 'yield browser.mouseMove("'+data.xpath+'");');
                        }
                        checkerBrowser && checkerBrowser.mouseMove(data.xpath, data.x, data.y).then(doNext).catch(catchError) || doNext();
                        break;
                    case 'mouseDown':
                        pushTestCode('mouseDown: ' + data.xpath + ', ' + data.x + ', ' + data.y + ', ' + data.button, 'yield browser.mouseMove("'+data.xpath+'", '+data.x+', '+data.y+').mouseDown('+data.button+');');
                        checkerBrowser && checkerBrowser.mouseMove(data.xpath, data.x, data.y).mouseDown(data.button).then(doNext).catch(catchError) || doNext();
                        break;
                    case 'mouseUp':
                        pushTestCode('mouseUp: ' + data.xpath + ', ' + data.x + ', ' + data.y + ', ' + data.button, 'yield browser.mouseMove("'+data.xpath+'", '+data.x+', '+data.y+').mouseUp('+data.button+');');
                        checkerBrowser && checkerBrowser.mouseMove(data.xpath, data.x, data.y).mouseUp(data.button).then(doNext).catch(catchError) || doNext();
                        break;
                    case 'click':
                        pushTestCode('click: ' + data.xpath + ', ' + data.x + ', ' + data.y + ', ' + data.button, 'yield browser.mouseMove("'+data.xpath+'", '+data.x+', '+data.y+').click('+data.button+');');
                        checkerBrowser && checkerBrowser.mouseMove(data.xpath, data.x, data.y).click(data.button).then(doNext).catch(catchError) || doNext();
                        break;
                    case 'dblClick':
                        pushTestCode('dblClick: ' + data.xpath + ', ' + data.x + ', ' + data.y, 'yield browser.mouseMove("'+data.xpath+'", '+data.x+', '+data.y+').click().dblClick();');
                        checkerBrowser && checkerBrowser.mouseMove(data.xpath, data.x, data.y).click().dblClick().then(doNext).catch(catchError) || doNext();
                        break;
                    case 'sendKeys':
                        pushTestCode('sendKeys: ' + data.text, 'yield browser.sendKeys("'+data.text.replace(/"/g, '\\"')+'");');
                        checkerBrowser && checkerBrowser.sendKeys(data.text).then(doNext).catch(catchError) || doNext();
                        break;
                    case 'keyDown':
                        pushTestCode('keyDown: ' + data.character, 'yield browser.keyDown("'+data.character+'");');
                        checkerBrowser && checkerBrowser.keyDown(data.character).then(doNext).catch(catchError) || doNext();
                        break;
                    case 'keyUp':
                        pushTestCode('keyUp: ' + data.character, 'yield browser.keyUp("'+data.character+'");');
                        checkerBrowser && checkerBrowser.keyUp(data.character).then(doNext).catch(catchError) || doNext();
                        break;
                    case 'scrollTo':
                        pushTestCode('scrollTo: '+ data.x + ', ' + data.y, 'yield browser.scrollTo('+data.x+', '+data.y+');');
                        checkerBrowser && checkerBrowser.scrollTo(data.x, data.y).then(doNext).catch(catchError) || doNext();
                        break;
                    case 'select':
                        arrCodes = [];
                        arrCodes.push('yield browser.find("'+data.xpath+'").then(function(element){');
                        arrCodes.push('    return element.select({');
                        arrCodes.push('        type: "'+data.type+'",');
                        arrCodes.push('        value: "'+data.value+'"');
                        arrCodes.push('    });');
                        arrCodes.push('});');
                        pushTestCode('select:' + data.xpath + ', ' + data.type + ', ' + data.value, arrCodes);
                        checkerBrowser && checkerBrowser.find(data.xpath, function(error, element){
                            if(!error){
                                return element.select({
                                    type: data.type,
                                    value: data.value
                                });
                            }
                        }).then(doNext).catch(catchError) || doNext();
                        break;
                    case 'acceptAlert':
                        pushTestCode('acceptAlert: ', 'yield browser.acceptAlert();');
                        checkerBrowser && checkerBrowser.acceptAlert().then(doNext).catch(catchError) || doNext();
                        break;
                    case 'dismissAlert':
                        pushTestCode('dismissAlert: ', 'yield browser.dismissAlert();');
                        checkerBrowser && checkerBrowser.dismissAlert().then(doNext).catch(catchError) || doNext();
                        break;
                    case 'setAlert':
                        pushTestCode('setAlert: ' + data.text, 'yield browser.setAlert("'+data.text+'");');
                        checkerBrowser && checkerBrowser.setAlert(data.text).then(doNext).catch(catchError) || doNext();
                        break;
                    case 'uploadFile':
                        arrCodes = [];
                        arrCodes.push('yield browser.wait("'+data.xpath+'").then(function*(element){');
                        arrCodes.push('    yield element.sendKeys("c:/uploadFiles/'+data.filename+'");');
                        arrCodes.push('});');
                        pushTestCode('uploadFile: ' + data.xpath + ', ' + data.filename, arrCodes);
                        checkerBrowser && checkerBrowser.wait(data.xpath, function*(error, element){
                            if(!error){
                                yield element.sendKeys('c:/uploadFiles/'+data.filename);
                            }
                        }).then(doNext).catch(catchError) || doNext();
                        break;
                    // 添加断言
                    case 'expect':
                        co(function*(){
                            var expectType = data.type;
                            var expectParams = data.params;
                            var expectCompare = data.compare;
                            var expectTo = data.to;
                            arrCodes = [];
                            var reDomRequire = /^(val|text|displayed|enabled|selected|attr|css)$/;
                            var reParamRequire = /^(attr|css|cookie|localStorage|sessionStorage)$/;
                            if(reDomRequire.test(expectType)){
                                arrCodes.push('var element = yield browser.wait("'+expectParams[0]+'");');
                                arrCodes.push('expect(element.length).to.be(1);');
                            }
                            switch(expectType){
                                case 'val':
                                    arrCodes.push('var value = yield element.val();');
                                    break;
                                case 'text':
                                    arrCodes.push('var value = yield element.text();');
                                    break;
                                case 'displayed':
                                    arrCodes.push('var value = yield element.displayed();');
                                    break;
                                case 'enabled':
                                    arrCodes.push('var value = yield element.enabled();');
                                    break;
                                case 'selected':
                                    arrCodes.push('var value = yield element.selected();');
                                    break;
                                case 'attr':
                                    arrCodes.push('var value = yield element.attr("'+expectParams[1]+'");');
                                    break;
                                case 'css':
                                    arrCodes.push('var value = yield element.css("'+expectParams[1]+'");');
                                    break;
                                case 'url':
                                    arrCodes.push('var value = yield browser.url();');
                                    break;
                                case 'title':
                                    arrCodes.push('var value = yield browser.title();');
                                    break;
                                case 'cookie':
                                    arrCodes.push('var value = yield browser.cookie("'+expectParams[0]+'");');
                                    break;
                                case 'localStorage':
                                    arrCodes.push('var value = yield browser.localStorage("'+expectParams[0]+'");');
                                    break;
                                case 'sessionStorage':
                                    arrCodes.push('var value = yield browser.sessionStorage("'+expectParams[0]+'");');
                                    break;
                            }
                            switch(expectCompare){
                                case 'equal':
                                    arrCodes.push('expect(value).to.equal('+(/^(true|false)$/.test(expectTo)?expectTo:'"'+expectTo.replace(/"/g, '\\"')+'"')+');');
                                    break;
                                case 'contain':
                                    arrCodes.push('expect(value).to.contain("'+expectTo+'");');
                                    break;
                                case 'regexp':
                                    arrCodes.push('expect(value).to.match('+expectTo+');');
                                    break;
                            }
                            pushTestCode('expect: ' + expectType + ', ' + JSON.stringify(expectParams) + ', ' + expectCompare + ', ' + expectTo, arrCodes);
                            if(checkerBrowser){
                                var element, value;
                                if(reDomRequire.test(expectType)){
                                    element = yield checkerBrowser.wait(expectParams[0]);
                                    expect(element.length).to.be(1);
                                }
                                switch(expectType){
                                    case 'val':
                                        value = yield element.val();
                                        break;
                                    case 'text':
                                        value = yield element.text();
                                        break;
                                    case 'displayed':
                                        value = yield element.displayed();
                                        break;
                                    case 'enabled':
                                        value = yield element.enabled();
                                        break;
                                    case 'selected':
                                        value = yield element.selected();
                                        break;
                                    case 'attr':
                                        value = yield element.attr(expectParams[1]);
                                        break;
                                    case 'css':
                                        value = yield element.css(expectParams[1]);
                                        break;
                                    case 'url':
                                        value = yield checkerBrowser.url();
                                        break;
                                    case 'title':
                                        value = yield checkerBrowser.title();
                                        break;
                                    case 'cookie':
                                        value = yield checkerBrowser.cookie(expectParams[0]);
                                        break;
                                    case 'localStorage':
                                        value = yield checkerBrowser.localStorage(expectParams[0]);
                                        break;
                                    case 'sessionStorage':
                                        value = yield checkerBrowser.sessionStorage(expectParams[0]);
                                        break;
                                }
                                switch(expectCompare){
                                    case 'equal':
                                        expectTo = /^(true|false)$/.test(expectTo)?eval(expectTo):expectTo;
                                        expect(value).to.equal(expectTo);
                                        break;
                                    case 'contain':
                                        expect(value).to.contain(expectTo);
                                        break;
                                    case 'regexp':
                                        expect(value).to.match(eval(expectTo));
                                        break;
                                }
                            }
                        }).then(doNext).catch(catchError);
                        break;
                    // 设置变量
                    case 'setvar':
                        arrCodes = [];
                        arrCodes.push('var element = yield browser.wait("'+data.xpath+'");');
                        arrCodes.push('yield element.clear().val(testVars["'+data.name+'"]);');
                        pushTestCode('setvar: ' + data.xpath + ', ' + data.name, arrCodes);
                        checkerBrowser && checkerBrowser.wait(data.xpath, function(error, element){
                            return element.clear().val(testVars[data.name]);
                        }).then(doNext).catch(catchError) || doNext();
                        break;
                }
            });
            async.series(arrTasks, function(){
                next();
            });
        }, 1);
        function onReady(){
            var localIp = getLocalIP();
            hosts += '\r\n'+localIp +' f2etest-recorder-server';
            // recorder browser
            newChromeBrowser(f2etestConfig, hosts, true, function*(browser){
                recorderBrowser = browser;
                yield browser.url('chrome-extension://njkfhfdkecbpjlnfmminhmdcakopmcnc/start.html');
                var browerInfo = yield recorderBrowser.info();
                var browserId = browerInfo['f2etest.browserId'];
                var f2etestUrl = 'http://'+f2etestConfig.server+'/openWdBrowser?browserId='+browserId;
                browserId && openUrl(f2etestUrl);
                console.log('Recorder browser opened:'.green, f2etestUrl);
                pushTestCode('maximize: ', 'yield browser.maximize();');
                for(var i=0;i<900;i++){
                    if(recorderBrowser){
                        yield recorderBrowser.size();
                        yield recorderBrowser.sleep(2000);
                    }
                    else{
                        break;
                    }
                }
            });
            // checker browser
            if(openChecker){
                newChromeBrowser(f2etestConfig, hosts, false, function*(browser){
                    checkerBrowser = browser;
                    var browerInfo = yield checkerBrowser.info();
                    var browserId = browerInfo['f2etest.browserId'];
                    var f2etestUrl = 'http://'+f2etestConfig.server+'/openWdBrowser?browserId='+browserId;
                    browserId && openUrl(f2etestUrl);
                    console.log('Checker browser opened:'.green, f2etestUrl);
                    for(var i=0;i<900;i++){
                        if(checkerBrowser){
                            yield checkerBrowser.size();
                            yield checkerBrowser.sleep(2000);
                        }
                        else{
                            break;
                        }
                    }
                });
            }
        }
        var arrSendKeys = [];
        var lastCmdInfo0 = null;
        var lastCmdInfo1 = null;
        var lastCmdInfo2 = null;
        var dblClickFilterTimer = null;
        function onCommand(cmdInfo){
            // 合并命令流
            function sendKeysFilter(cmdInfo){
                // 合并连续的sendKeys
                var cmd = cmdInfo.cmd;
                var data = cmdInfo.data;
                if(cmd === 'sendKeys' && /\{\w+\}/.test(data.text) === false){
                    arrSendKeys.push(data.text);
                }
                else{
                    if(arrSendKeys.length > 0){
                        // 满足条件，进行合并
                        clickFilter({
                            window: lastCmdInfo0.window,
                            frame: lastCmdInfo0.frame,
                            cmd: 'sendKeys',
                            data: {
                                text: arrSendKeys.join('')
                            }
                        });
                        arrSendKeys = [];
                    }
                    clickFilter(cmdInfo);
                }
                lastCmdInfo0 = cmdInfo;
            }
            function clickFilter(cmdInfo){
                // 合并为click，增加兼容性 (mouseDown不支持button参数)
                var cmd = cmdInfo.cmd;
                var data = cmdInfo.data;
                if(lastCmdInfo1 && lastCmdInfo1.cmd === 'mouseDown'){
                    var lastCmdData = lastCmdInfo1.data;
                    if(cmd === 'mouseUp' &&
                        cmdInfo.window === lastCmdInfo1.window &&
                        cmdInfo.frame === lastCmdInfo1.frame &&
                        lastCmdData.xpath === data.xpath &&
                        Math.abs(lastCmdData.x - data.x) < 20 &&
                        Math.abs(lastCmdData.y - data.y) < 20
                    ){
                        // 条件满足，合并为click
                        cmdInfo = {
                            window : cmdInfo.window,
                            frame : cmdInfo.frame,
                            cmd: 'click',
                            data: data
                        };
                    }
                    else{
                        // 不需要合并，恢复之前旧的mouseDown
                        dblClickFilter(lastCmdInfo1);
                        // mouseDown后加延迟，以增加兼容性
                        dblClickFilter({
                            window: cmdInfo.window,
                            frame: cmdInfo.frame,
                            cmd: 'sleep',
                            data: {
                                time: 300
                            }
                        });
                    }
                }
                if(cmdInfo.cmd !== 'mouseDown'){
                    // mouseDown 缓存到下一次，确认是否需要合并click，非mouseDown立即执行
                    dblClickFilter(cmdInfo);
                }
                lastCmdInfo1 = cmdInfo;
            }
            function dblClickFilter(cmdInfo){
                // 合并为dblClick，增加兼容性, 某些浏览器不支持连续的两次click
                var cmd = cmdInfo.cmd;
                var data = cmdInfo.data;
                if(lastCmdInfo2 && lastCmdInfo2.cmd === 'click'){
                    var lastCmdData = lastCmdInfo2.data;
                    clearTimeout(dblClickFilterTimer);
                    if(cmd === 'click' &&
                        cmdInfo.window === lastCmdInfo2.window &&
                        cmdInfo.frame === lastCmdInfo2.frame &&
                        lastCmdData.xpath === data.xpath &&
                        Math.abs(lastCmdData.x - data.x) < 20 &&
                        Math.abs(lastCmdData.y - data.y) < 20
                    ){
                        // 条件满足，合并为click
                        cmdInfo = {
                            window : cmdInfo.window,
                            frame : cmdInfo.frame,
                            cmd: 'dblClick',
                            data: data
                        };
                    }
                    else{
                        // 不需要合并，恢复之前旧的click
                        cmdQueue.push(lastCmdInfo2);
                    }
                }
                if(cmdInfo.cmd !== 'click'){
                    // click 缓存到下一次，确认是否需要合并dblClick，非click立即执行
                    cmdQueue.push(cmdInfo);
                }
                else{
                    // 500毫秒以内才进行dblClick合并
                    dblClickFilterTimer = setTimeout(function(){
                        cmdQueue.push(lastCmdInfo2);
                        lastCmdInfo2 = null;
                    }, 500);
                }
                lastCmdInfo2 = cmdInfo;
            }
            sendKeysFilter(cmdInfo);
        }
        function onEnd(){
            recorderBrowser.close(function(){
                recorderBrowser = null;
                saveTestFile();
                console.log('Recorder browser closed.'.green);
                checkerBrowser && checkerBrowser.close(function(){
                    checkerBrowser = null;
                    console.log('Checker browser closed.'.green);

                });
            });
        }
        var recorderConfig = {
            xpathAttrs : xpathAttrs,
            testVars: testVars
        };
        startRecorderServer(recorderConfig, onReady, onCommand, onEnd);
        function saveTestFile(){
            var tempalteFile = path.resolve(__dirname, '../template/jwebdriver.js');
            var templateContent = fs.readFileSync(tempalteFile).toString();
            var testFile = path.resolve(fileName);
            arrTestCodes = arrTestCodes.map(function(line){
                return '        '+ line;
            });
            templateContent = templateContent.replace(/\{\$(\w+)\}/g, function(all, name){
                switch(name){
                    case 'testCodes':
                        return arrTestCodes.join('\r\n');
                }
                return all;
            });
            fs.writeFileSync(testFile, templateContent);
            console.log('Test file: '.green+fileName.bold+' saved.'.green);
        }
    });
}

// start recorder server
function startRecorderServer(config, onReady, onCommand, onEnd){
    var serverPort = 9765;
    var server = http.createServer(function(req, res){
        var urlInfo = url.parse(req.url, true);
        var pathname = urlInfo.pathname;
        var query = urlInfo.query;
        switch(pathname){
            case '/getConfig':
                res.end(JSON.stringify(config));
                break
            case '/endRecorder':
                server.close(function(){
                    console.log('Recorder server closed.'.green);
                    onEnd();
                });
                res.end('ok');
                break
            case '/saveCmd':
                var cmdInfo = query['cmdInfo'];
                try{
                    cmdInfo = JSON.parse(cmdInfo);
                }
                catch(e){}
                onCommand(cmdInfo);
                res.end('ok');
                break;
        }
    });
    server.listen(serverPort, function(){
        console.log('Recorder server listend on %s'.green, serverPort);
        onReady();
    });
}

function newChromeBrowser(f2etestConfig, hosts, isRecorder, callback){
    var driver = new JWebDriver({
        'host': f2etestConfig.server
    });
    var capabilities = {
        'f2etest.userid': f2etestConfig.userid,
        'f2etest.apiKey': f2etestConfig.apiKey,
        'f2etest.hosts': hosts,
        'browserName': 'chrome'
    };
    if(isRecorder){
        var crxPath = path.resolve(__dirname, '../chrome-extension/f2etest-recorder.crx');
        var extContent = fs.readFileSync(crxPath).toString('base64');
        capabilities.chromeOptions = {
            // args:['load-extension=E:\\github\\f2etest\\f2etest-recorder\\chrome-extension'],
            extensions: [extContent]
        };
    }
    driver.session(capabilities, function*(error, browser){
        if(error){
            console.log('F2etest chrome created failed!'.red);
            console.log(error);
            process.exit(1);
        }
        else{
            yield browser.maximize();
            yield callback(browser);
        }
    }).catch(function(e){});
}

// get local ip
function getLocalIP() {
    var ifaces = os.networkInterfaces();
    for (var dev in ifaces) {
        var iface = ifaces[dev];
        for (var i = 0; i < iface.length; i++) {
            var alias = iface[i];
            if (alias.family === 'IPv4' && alias.address !== '127.0.0.1' && !alias.internal){
                return alias.address;
            }
        }
    }
}

// open url in local browser
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
    cp.exec(startCmd + ' "' + url+'"');
}

module.exports = startRecorder;

var F2ETESTAPI = 'http://f2etest-recorder-server:9765';
var ENABLE_ICON1 = 'img/icon.png';
var ENABLE_ICON2 = 'img/icon-record.png';
var DISABLE_ICON = 'img/icon-disable.png';

var isWorking = true;
var workIcon = 1;
var workIconTimer = null;

// set recorder work status
function setRecorderWork(enable){
    isWorking = enable;
    if(isWorking){
        chrome.browserAction.setTitle({title: '录制中……点击结束录制'});
        chrome.browserAction.setIcon({path: workIcon===1?ENABLE_ICON1:ENABLE_ICON2});
        workIcon *= -1;
        workIconTimer = setTimeout(function(){
            setRecorderWork(true);
        }, 1000);
    }
    else{
        clearTimeout(workIconTimer);
        chrome.browserAction.setTitle({title: '录制已结束'});
        chrome.browserAction.setIcon({path: DISABLE_ICON});
    }
}

var arrTasks = [];
var lastTargetLocation = null;
var lastWindow = null;
var allKeyMap = {};
var allMouseMap = {};
// save recoreded command
function saveCommand(windowId, frame, cmd, data){
    if( cmd === 'target'){
        // filter duplication target
        var targetLocation = ''+windowId+frame+data.xpath;
        if(targetLocation === lastTargetLocation){
            return;
        }
        lastTargetLocation = targetLocation;
    }
    var cmdInfo = {
        window: windowId,
        frame: frame,
        cmd: cmd,
        data: data,
        fix: false
    };

    switch(cmd){
        case 'keyDown':
            allKeyMap[data.character] = cmdInfo;
            break;
        case 'keyUp':
            delete allKeyMap[data.character];
            break;
        case 'mouseDown':
            allMouseMap[data.button] = cmdInfo;
            break;
        case 'mouseUp':
            delete allMouseMap[data.button];
            break;
    }

    checkLostKey(windowId);

    execNextCommand(cmdInfo);
}

// 补足丢失的事件
function checkLostKey(windowId){
    if(windowId !== lastWindow){
        var cmdInfo;
        for(var key in allKeyMap){
            cmdInfo = allKeyMap[key];
            execNextCommand({
                window: cmdInfo.window,
                frame: cmdInfo.frame,
                cmd: 'keyUp',
                data: cmdInfo.data,
                fix: true
            });
        }
        allKeyMap = {};
        for(var button in allMouseMap){
            cmdInfo = allMouseMap[button];
            execNextCommand({
                window: cmdInfo.window,
                frame: cmdInfo.frame,
                cmd: 'mouseUp',
                data: cmdInfo.data,
                fix: true
            });
        }
        allMouseMap = {};
        lastWindow = windowId;
        lastTargetLocation = null;
    }
}

var isRunning = false;
function execNextCommand(newCmdInfo){
    if(newCmdInfo){
        arrTasks.push(newCmdInfo);
    }
    if(arrTasks.length > 0 && isRunning === false){
        var cmdInfo = arrTasks.shift();
        console.log('cmd: { window: '+cmdInfo.window+', frame: '+cmdInfo.frame+', cmd: '+cmdInfo.cmd+ ', data:', JSON.stringify(cmdInfo.data) + ', fix: '+cmdInfo.fix+' }');
        var strCmdInfo = JSON.stringify(cmdInfo);
        var url = F2ETESTAPI + '/saveCmd?cmdInfo='+encodeURIComponent(strCmdInfo);
        isRunning = true;
        getHttp(url, function(){
            isRunning = false;
            execNextCommand();
        });
    }
}

function getHttp(url, callback){
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState === 4 && xmlhttp.status === 200) {
            callback(null, xmlhttp.responseText);
        }
        else{
            callback('error');
        }
    };
    xmlhttp.open("GET", url, true);
    xmlhttp.send(null);
}

// manage window id
var arrWindows = [];
function getWindowId(tabId){
    for(var i=0,len=arrWindows.length;i<len;i++){
        if(arrWindows[i] === tabId){
            return i;
        }
    }
    return -1;
}
function addWindowId(tabId){
    arrWindows.push(tabId);
    var windowId = arrWindows.length -1;
    checkLostKey(windowId);
    console.log('newWindow { id: '+ windowId + ' }');
    return windowId;
}
function delWindowId(tabId){
    var windowId = getWindowId(tabId);
    if(windowId !== -1){
        arrWindows.splice(windowId, 1);
        console.log('closeWindow: { id: '+ windowId + ' }');
    }
}

// catch incognito window
chrome.tabs.onUpdated.addListener(function(tabId, changeInfo, tab) {
    if (!tab.incognito && isWorking) {
        var windowId = getWindowId(tabId);
        if(windowId === -1){
            windowId = addWindowId(tabId);
        }
    }
});

// catch url
chrome.webNavigation.onCommitted.addListener(function(navInfo){
    if(isWorking){
        var tabId = navInfo.tabId;
        var type = navInfo.transitionType;
        var url = navInfo.url;
        var windowId = getWindowId(tabId);
        if(windowId !== -1 && /^(typed|reload|auto_bookmark)$/.test(type) && /^https?:\/\//.test(url)){
            checkLostKey(-1);
            saveCommand(windowId, null, 'url', {
                url: url
            });
        }
    }
});

// catch window close
chrome.tabs.onRemoved.addListener(function(tabId, removeInfo){
    var windowId = getWindowId(tabId);
    if(windowId !== -1){
        delWindowId(tabId);
        if(windowId !== 0 ){
            saveCommand(windowId, null, 'closeWindow');
        }
    }
    if(arrWindows.length === 0){
        setRecorderWork(false);
    }
});

// catch current window events
chrome.runtime.onMessage.addListener(function (request, sender, sendResponse) {
    if(isWorking && sender && sender.tab){
        var tabId = sender.tab.id;
        var windowId = getWindowId(tabId);
        if(windowId !== -1){
            chrome.tabs.query({currentWindow: true, active: true}, function(tabs){
                if(tabs.length > 0 && tabId === tabs[0].id){
                    var type = request.type;
                    var data = request.data;
                    switch(type){
                        case 'end':
                            endRecorder();
                            break;
                        case 'getConfig':
                            getHttp(F2ETESTAPI + '/getConfig', function(error, result){
                                if(result){
                                    try{
                                        var recordConfig = JSON.parse(result);
                                        sendResponse(recordConfig);
                                    }
                                    catch(e){}
                                }
                            });
                            break;
                        case 'command':
                            saveCommand(windowId, data.frame, data.cmd, data.data);
                            break;

                    }
                }
            });
            return true;
        }
    }
});

// on action clicked
chrome.browserAction.onClicked.addListener(function(tab){
    if(isWorking){
        endRecorder();
    }
    else{
        alert('录制已经结束，请从客户端重新初始化！');
    }
});

// end recorder
function endRecorder(){
    setRecorderWork(false);
    getHttp(F2ETESTAPI + '/endRecorder');
}

// Global events port
var mapPorts = {};
var maxPortId = 0;
chrome.extension.onConnect.addListener(function(port) {
    var portId = maxPortId++;
    mapPorts[portId] = port;
    port.onMessage.addListener(function(msg) {
        for(var portId in mapPorts){
            mapPorts[portId].postMessage(msg);
        }
    });
    port.onDisconnect.addListener(function(port){
        delete mapPorts[portId];
    });
});

setRecorderWork(true);


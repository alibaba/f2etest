(function(){
    var isIframe = self !== top;
    var isRecording = false;
    var isStopEvent = false;
    var isBodyReady = false;
    var isOnload = false;

    // dom selector
    var isDomSelecter = false;
    var divDomSelector = null;
    var lastSelectDom = null;
    var domSelectorCallback = null;
    var spanShowDomPath = null;
    var expectGetValueCallback = null;

    // 全局配置
    var testVars = {};
    var arrXPathAttrs = ['id', 'data-id', 'name', 'data-name', 'type', 'data-type', 'data-role'];

    // 全局事件
    var mapGlobalEvents = {};
    var eventPort = chrome.extension.connect();
    var GlobalEvents = {
        on: function(type, handler){
            var arrEvents = mapGlobalEvents[type] || [];
            arrEvents.push(handler);
            mapGlobalEvents[type] = arrEvents;
        },
        emit: function(type, data){
            eventPort.postMessage({
                type: type,
                data: data
            });
        },
        _emit: function(type, data){
            var arrEvents = mapGlobalEvents[type] || [];
            arrEvents.forEach(function(handler){
                handler(data);
            });
        }
    };
    eventPort.onMessage.addListener(function(msg) {
        GlobalEvents._emit(msg.type, msg.data);
    });    

    // load config
    chrome.runtime.sendMessage({
        type: 'getConfig'
    }, function(config){
        if(config.testVars){
            testVars = config.testVars;
        }
        if(config.xpathAttrs){
            arrXPathAttrs = config.xpathAttrs.split(',');
        }
    });

    // 读取cookie
    function getCookie(name){
        var mapCookies = {};
        var cookie = document.cookie || '';
        cookie.replace(/([^=]+)\s*=\s*([^;]*)\s*;?\s*/g, function(all, name, value){
            mapCookies[name] = value;
        });
        return mapCookies[name];
    }

    var reHoverClass = /(^|[^a-z0-9])(on)?(hover|over|active|current)([^a-z0-9]|$)/i;

    // get selector path or XPath
    function getDomPath(target){
        var selectorPath = getSelectorPath(target);
        var XPath = getXPath(target);
        if(selectorPath){
            var match = selectorPath.match(/>/g);
            var selectorLevelCount = match ? match.length + 1 : 1;
            match = XPath.match(/\//g);
            var XPathLevelCount = match ? match.length - 1 : 1;
            return XPathLevelCount === 1 && selectorLevelCount > 1 ? XPath : selectorPath;
        }
        else{
            return XPath;
        }
    }

    // get selector path
    function getSelectorPath(target){
        var current = target;
        var selectorPath = '';
        var levelCount = 0;
        while(current !== null && levelCount < 4){
            if(current.nodeName !== 'HTML'){
                selectorPath = getSelectorElement(current, selectorPath);
                if(selectorPath.substr(0,1) === '!'){
                    return selectorPath.substr(1);
                }
                current = current.parentNode;
                levelCount ++;
            }
            else{
                current = null;
            }
        }
        return null;
    }
    function getSelectorElement(target, childPath){
        var tagName = target.nodeName.toLowerCase();
        var tempPath;
        var relativeClass = null;
        var idValue = target.getAttribute && target.getAttribute('id');
        if(idValue && /^[a-z]/i.test(idValue)){
            tempPath = '#'+idValue;
            if(childPath){
                tempPath += ' > ' + childPath;
            }
            if(checkUniqueSelector(tempPath)){
                return '!' + tempPath;
            }
        }
        var classValue = target.getAttribute && target.getAttribute('class');
        if(classValue){
            var arrClass = classValue.split(/\s+/);
            for(var i in arrClass){
                var className = arrClass[i];
                if(className && reHoverClass.test(className) === false){
                    tempPath = tagName + '.'+arrClass[i];
                    if(childPath){
                        tempPath += ' > ' + childPath;
                    }
                    if(checkUniqueSelector(tempPath)){
                        return '!' + tempPath;
                    }
                    // 无法绝对定位,再次测试是否可以在父节点中相对定位自身
                    else{
                        var parent = target.parentNode;
                        if(parent){
                            var element = parent.querySelectorAll('.'+className);
                            if(element.length === 1){
                                relativeClass = className;
                            }
                        }
                    }
                }
            }
        }
        tempPath = tagName;
        if(relativeClass){
            tempPath += '.' + relativeClass;
        }
        else{
            var index = getElementIndex(target);
            if(index !== -1){
                tempPath += ':nth-child('+(index+1)+')';
            }
        }
        if(childPath){
            tempPath += ' > ' + childPath;
        }
        if(checkUniqueSelector(tempPath) && /^\w+:nth-child/.test(tempPath) === false){
            tempPath = '!' + tempPath;
        }
        return tempPath;
    }
    function checkUniqueSelector(path){
        var elements = document.querySelectorAll(path);
        return elements.length === 1 && /[\.#]/.test(path);
    }
    function findDomPathElement(path){
        return /^\/\//.test(path) ? findXPathElement(path) : document.querySelectorAll(path);
    }

    // XPath tools
    function getXPath(target){
        var current = target;
        var path = '';
        while(current !== null){
            if(current.nodeName !== 'HTML'){
                path  = getElementXPath(target, current, path);
                if(/^\/\//.test(path)){
                    return path;
                }
                current = current.parentNode;
            }
            else{
                current = null;
            }
        }
        return null;
    }
    function getElementXPath(target, el, path){
        var newPath = null;
        if(el.attributes){
            // make map
            var mapAttrs = {};
            var attrs = el.attributes;
            var attr;
            for (var i = 0, len=attrs.length; i < len; i++) {
                attr = attrs[i];
                mapAttrs[attr.name] = attr.value;
            }
            // test locator
            var document = target.ownerDocument;
            var ownerSVGElement = target.ownerSVGElement;
            var nodeName = el.nodeName.toLowerCase();
            var arrAttrs = [];
            var attrName, attrValue;
            var arrResults;
            for(var i=0,len=arrXPathAttrs.length;i<len;i++){
                attrName = arrXPathAttrs[i];
                attrValue = mapAttrs[attrName];
                if(attrName === 'class' && (/\s+/.test(attrValue) || reHoverClass.test(attrValue))){
                    attrValue = '';
                }
                if(attrValue){
                    arrAttrs.push({
                        name: attrName,
                        value: attrValue
                    });
                    if(checkAttrsUnique(el, arrAttrs)){
                        if(el.ownerSVGElement !== undefined){
                            newPath = '/*[name()="'+nodeName+'" and '+arrAttrs.map(function(attr){
                                return '@' + attr.name + "=" + encodeAttrValue(attr.value);
                            }).join(' and ')+']' + path;
                        }
                        else{
                            newPath = '/' + nodeName + '['+arrAttrs.map(function(attr){
                                return '@' + attr.name + "=" + encodeAttrValue(attr.value);
                            }).join(' and ')+']' + path;
                        }
                        arrResults = findXPathElement('/'+newPath, document);
                        if(arrResults.length === 1 && arrResults[0] == target){
                            return '/'+newPath;
                        }
                    }
                }
            }
            // index mode
            if(newPath === null){
                var index = getElementIndex(el);
                if(el.ownerSVGElement !== undefined){
                    newPath = '/*[name()="'+nodeName+'"]'+ (index>-1?'['+(index+1)+']':'') + path;
                }
                else{
                    newPath = '/'+ nodeName + (index>-1?'['+(index+1)+']':'') + path;
                }
                // index模式非body不允许作为根
                if(nodeName === 'body'){
                    arrResults = findXPathElement('/'+newPath, document);
                    // 必需有属性值，或者是从/body开始的绝对路径
                    if(arrResults.length === 1 && arrResults[0] == target && (/@/.test(newPath) || /^\/body/.test(newPath))){
                        return '/'+newPath;
                    }
                }
            }
        }
        return newPath;
    }
    function checkAttrsUnique(el, arrAttrs){
        var nodeName = el.nodeName;
        var childNodes = el.parentNode.childNodes;
        var brother;
        var brotherCount = 0;
        var attrDiffCount = 0;
        for (var i = 0, len1=childNodes.length; i < len1; i++) {
            brother = childNodes[i];
            if(brother.nodeName === nodeName){
                attrDiffCount = 0;
                for(var j=0,len2 = arrAttrs.length;j<len2;j++){
                    var attr = arrAttrs[j];
                    if(attr.value !== brother.getAttribute(attr.name)){
                        attrDiffCount ++;
                    }
                }
                if(attrDiffCount === 0){
                    brotherCount ++;
                }
            }
        }
        return brotherCount===1?true:false;
    }
    function getElementIndex(el){
        var index = -1;
        var parentNode = el.parentNode;
        if(parentNode){
            var childNodes = parentNode.childNodes;
            var total = 0;
            for (var i = 0, len=childNodes.length; i < len; i++) {
                var child = childNodes[i];
                if (child.nodeName == el.nodeName) {
                    if (child == el) {
                        index = total;
                    }
                    total++;
                }
            }
        }
        if(total === 1){
            index = -1;
        }
        return index;
    }
    function encodeAttrValue(value){
        if (value.indexOf("'") < 0) {
            return "'" + value + "'";
        } else if (value.indexOf('"') < 0) {
            return '"' + value + '"';
        } else {
            var result = 'concat(';
            var part = "";
            while (true) {
                var apos = value.indexOf("'");
                var quot = value.indexOf('"');
                if (apos < 0) {
                    result += "'" + value + "'";
                    break;
                } else if (quot < 0) {
                    result += '"' + value + '"';
                    break;
                } else if (quot < apos) {
                    part = value.substring(0, apos);
                    result += "'" + part + "'";
                    value = value.substring(part.length);
                } else {
                    part = value.substring(0, quot);
                    result += '"' + part + '"';
                    value = value.substring(part.length);
                }
                result += ',';
            }
            result += ')';
            return result;
        }
    }
    function findXPathElement(locator, doc){
        doc = doc || document;
        var arrResults = [];
        try {
            var xpath_obj = doc.evaluate(locator, doc, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);
            for(var i=0,len=xpath_obj.snapshotLength;i<len;i++){
                arrResults.push(xpath_obj.snapshotItem(i));
            }
        } catch (e) {}
        return arrResults;
    }

    // get frame id
    function getFrameId(){
        var frame = null;
        if(isIframe){
            try{
                var frameElement = window.frameElement;
                if(frameElement !== null){
                    frame = getDomPath(frameElement);
                }
                else{
                    var parentFrames = parent.frames;
                    for(var i=0,len=parentFrames.length;i<len;i++){
                        if(parentFrames[i] === window){
                            frame = i;
                            break;
                        }
                    }
                }
            }
            catch(e){}
        }
        return frame;
    }

    // save command
    function saveCommand(cmd, data){
        var frameId = getFrameId();
        var cmdData = {
            frame: frameId,
            cmd: cmd,
            data: data
        };
        if(typeof frameId === 'number'){
            parent.postMessage({
                type: 'f2etestFrameCommmand',
                data: cmdData
            }, '*');
        }
        else{
            chrome.runtime.sendMessage({
                type: 'command',
                data: cmdData
            });
        }
    }

    window.addEventListener('message', function(e){
        var data = e.data;
        var type = data && data.type;
        if(type === 'f2etestAlertCommand'){
            var cmdInfo = data.cmdInfo;
            saveCommand(cmdInfo.cmd, cmdInfo.data);
        }
        else if(type === 'f2etestFrameCommmand'){
            data = data.data;
            // fix frameId to path
            var frameWindow = window.frames[data.frame];
            var arrIframes = document.getElementsByTagName("iframe");
            var frameDom = null;
            for(var i =0, len = arrIframes.length;i<len;i++){
                frameDom = arrIframes[i];
                if(frameDom.contentWindow === frameWindow){
                    break;
                }
            }
            data.frame = getDomPath(frameDom);
            chrome.runtime.sendMessage({
                type: 'command',
                data: data
            });
        }
    }, true); 

    function simulateMouseEvent(target, type, bubbles, cancelable, view, detail, screenX, screenY, clientX, clientY){
        try{
            var customEvent = document.createEvent("MouseEvents");
            customEvent.initMouseEvent(type, bubbles, cancelable, view, detail, screenX, screenY, clientX, clientY);
            target.dispatchEvent(customEvent);
        }
        catch(e){}
    }

    // 计算字节长度,中文两个字节
    function byteLen(text){
        var count = 0;
        for(var i=0,len=text.length;i<len;i++){
            char = text.charCodeAt(i);
            count += char > 255 ? 2 : 1;
        }
        return count;
    }

    // 从左边读取限制长度的字符串
    function leftstr(text, limit){
        var substr = '';
        var count = 0;
        var char;
        for(var i=0,len=text.length;i<len;i++){
            char = text.charCodeAt(i);
            substr += text.charAt(i);
            count += char > 255 ? 2 : 1;
            if(count >= limit){
                return substr;
            }
        }
        return substr;
    }

    function getTargetText(target){
        var nodeName = target.nodeName;
        var id = target.getAttribute('id');
        var text = '';
        if(nodeName === 'INPUT'){
            var type = target.getAttribute('type');
            switch(type){
                case 'button':
                case 'reset':
                case 'submit':
                    text = target.getAttribute('value');
                    break;
                default:
                    var parentNode = target.parentNode;
                    if(parentNode.nodeName === 'LABEL'){
                        text = parentNode.textContent;
                    }
                    else if(id){
                        var labelForElement = findXPathElement('//label[@for="'+id+'"]');
                        if(labelForElement.length > 0){
                            text = labelForElement[0].textContent;
                        }
                        else{
                            text = target.getAttribute('name');
                        }
                    }
                    else{
                        text = target.getAttribute('name');
                    }
            }
        }
        else if(nodeName === 'SELECT'){
            text = target.getAttribute('name');
        }
        else{
            text = target.textContent;
        }
        text = text || '';
        text = text.replace(/^\s+|\s+$/g, '');
        text = text.replace(/\s*\r?\n\s*/g,' ');
        var textLen = byteLen(text);
        if(textLen <= 60){
            text = textLen > 20 ? leftstr(text, 20) + '...' : text;
        }
        else{
            text = '';
        }
        return text;
    }

    function addActionTarget(target){
        if(/^(HTML|IFRAME)$/i.test(target.tagName) === false){
            var path = getDomPath(target);
            if(path !== null){
                GlobalEvents.emit('showDomPath', path);
                saveCommand('target', {
                    path: path,
                    text: getTargetText(target)
                });
            }
        }
    }

    // show loading
    var divLoading;
    function showLoading(){
        divLoading = document.createElement("div");
        divLoading.id = 'f2etest-loading';
        divLoading.innerHTML = '<style>#f2etest-loading{display:block;position:fixed;z-index:999999999;left:0;top:0;width:100%;height:100%;}#f2etest-loading div{z-index:0;background:#000;width:100%;height:100%;opacity:0.6}#f2etest-loading span{z-index:1;position:fixed;top:50%;left:50%;margin-left:-165px;margin-top:-20px;color:white;font-size:30px;}</style><div></div><span>正在等待加载，请稍候……</span>';
        document.body.appendChild(divLoading);
    }
    
    function onBodyReady(){
        isBodyReady = true;
        hookAlert();
        if(isOnload === false && isIframe === false){
            // 主窗口显示loading
            showLoading();
        }
    }

    function onLoad(){
        isOnload = true;
        if(divLoading){
            divLoading.style.display = 'none';
        }
        if(isIframe === false){
            saveCommand('wait', {
                path: 'body'
            });
        }
        if(isIframe && location.href === 'about:blank'){
            // 富文本延后初始化
            setTimeout(function(){
                initRecorderEvent();
                initRecorderDom();
            }, 500);
        }
        else{
            initRecorderEvent();
            initRecorderDom();
        }
    }

    function checkBodyReady(){
        var body = document.getElementsByTagName("body");
        if(body && body.length===1){
            onBodyReady();
        }
        else{
            setTimeout(checkBodyReady, 10);
        }
    }
    
    checkBodyReady();

    if(document.readyState === 'complete'){
        onLoad();
    }
    else{
        window.addEventListener('load', onLoad, true);
    }

    // 工作模式变更
    GlobalEvents.on('modeChange', function(mode){
        switch(mode){
            case 'record':
                removeDomSelecter();
                isRecording = true;
                isStopEvent = false;
                isDomSelecter = false;
                break;
            case 'pauseAll':
                removeDomSelecter();
                isRecording = false;
                isStopEvent = true;
                isDomSelecter = false;
                break;
            case 'pauseRecord':
                removeDomSelecter();
                isRecording = false;
                isStopEvent = false;
                isDomSelecter = false;
                break;
            case 'select':
                initDomSelecter();
                isRecording = false;
                isStopEvent = true;
                isDomSelecter = true;
        }
    });
    // 设置全局工作模式
    function setGlobalWorkMode(mode){
        GlobalEvents.emit('modeChange', mode);
    }

    // dom选择器hover事件
    GlobalEvents.on('selecterHover', function(event){
        var frameId = getFrameId();
        if(frameId !== event.frame){
            // 清空选择器其余的iframe浮层
            divDomSelector.style.display = 'none';
        }
        if(isIframe === false){
            // 主窗口显示path路径
            spanShowDomPath.innerHTML = event.path;
        }
    });

    // 添加悬停命令
    GlobalEvents.on('addHover', function(event){
        var frameId = getFrameId();
        if(frameId === event.frame){
            var elements = findDomPathElement(event.path);
            if(elements.length === 1){
                var target = elements[0];
                addActionTarget(target);
                simulateMouseEvent(target, 'mouseover', true, true, null);
                simulateMouseEvent(target, 'mousemove', true, true, null, 1, event.screenX, event.screenY, event.clientX, event.clientY);
            }
        }
    });

    // 插入变量
    GlobalEvents.on('setVar', function(event){
        var frameId = getFrameId();
        if(frameId === event.frame){
            var path = event.path;
            var elements = findDomPathElement(path);
            if(elements.length === 1){
                var target = elements[0];
                target.focus();
                target.value = event.value;
                addActionTarget(target);
                saveCommand('setvar', {
                    path: path,
                    name: event.name,
                    text: getTargetText(target)
                });
            }
        }
    });

    // 获取断言默认值
    GlobalEvents.on('getExpectValue', function(event){
        var domInfo = event.domInfo;
        var frameId = getFrameId();
        if(frameId === domInfo.frame){
            var path = domInfo.path;
            var elements = findDomPathElement(path);
            if(elements.length === 1){
                var expectTarget = elements[0];
                var type = event.type;
                var param = event.param;
                var expectValue = '';
                switch(type){
                    case 'val':
                        expectValue = expectTarget.value || '';
                        break;
                    case 'text':
                        var text = expectTarget.textContent || '';
                        text = text.replace(/^\s+|\s+$/g, '');
                        expectValue = text;
                        break;
                    case 'displayed':
                        expectValue = 'true';
                        break;
                    case 'enabled':
                        expectValue = expectTarget.disabled ? 'false' : 'true';
                        break;
                    case 'selected':
                        expectValue = expectTarget.checked ? 'true' : 'false';
                        break;
                    case 'attr':
                        if(param){
                            expectValue = expectTarget.getAttribute(param) || '';
                        }
                        break;
                    case 'css':
                        if(param){
                            var styles = window.getComputedStyle(expectTarget, null);
                            expectValue = styles.getPropertyValue(param) || '';
                        }
                        break;
                }
                GlobalEvents.emit('returnExpectValue', expectValue);
            }
        }
    });

    // 添加断言命令
    GlobalEvents.on('addExpect', function(event){
        var frameId = getFrameId();
        if(frameId === event.frame){
            saveCommand('expect', event.data);
        }
    });

    // 主窗口
    if(isIframe === false){
        // DOM选择器点击事件
        GlobalEvents.on('selecterClick', function(event){
            domSelectorCallback({
                frame: event.frame,
                path: event.path
            }, event.ctrlKey);
        });
        // 返回断言默认值
        GlobalEvents.on('returnExpectValue', function(value){
            expectGetValueCallback(value);
        });
        function getExpectValue(type, domInfo, param, callback){
            expectGetValueCallback = callback;
            GlobalEvents.emit('getExpectValue', {
                type: type,
                domInfo: domInfo,
                param: param
            });
        }
        // 显示target的path
        GlobalEvents.on('showDomPath', function(path){
            spanShowDomPath.innerHTML = path;
        });
    }

    function hookAlert(){
        // eval with unsafe window
        function unsafeEval(str){
            var head = document.getElementsByTagName("head")[0];
            var script = document.createElement("script");
            script.innerHTML = '('+str+')();';
            head.appendChild(script);
            head.removeChild(script);
        }

        // hook alert, confirm, prompt
        function hookAlertFunction(){
            var rawAlert = window.alert;
            function sendAlertCmd(cmd, data){
                var cmdInfo = {
                    cmd: cmd,
                    data: data || {}
                };
                window.postMessage({
                    'type': 'f2etestAlertCommand',
                    'cmdInfo': cmdInfo
                }, '*');
            }
            window.alert = function(str){
                var ret = rawAlert.call(this, str);
                sendAlertCmd('acceptAlert');
                return ret;
            }
            var rawConfirm = window.confirm;
            window.confirm = function(str){
                var ret = rawConfirm.call(this, str);
                sendAlertCmd(ret?'acceptAlert':'dismissAlert');
                return ret;
            }
            var rawPrompt = window.prompt;
            window.prompt = function(str){
                var ret = rawPrompt.call(this, str);
                if(ret === null){
                    sendAlertCmd('dismissAlert');
                }
                else{
                    sendAlertCmd('setAlert', {
                        text: ret
                    });
                    sendAlertCmd('acceptAlert');
                }
                return ret;
            }
            function wrapBeforeUnloadListener(oldListener){
                var newListener = function(e){
                    var returnValue = oldListener(e);
                    if(returnValue){
                        sendAlertCmd('beforeUnload');
                        setTimeout(function(){
                            sendAlertCmd('cancelBeforeUnload');
                        }, 500);
                    }
                    return returnValue;
                }
                return newListener;
            }
            var rawAddEventListener = window.addEventListener;
            window.addEventListener = function(type, listener, useCapture){
                if(type === 'beforeunload'){
                    listener = wrapBeforeUnloadListener(listener);
                }
                return rawAddEventListener.call(window, type, listener, useCapture);
            };
            setTimeout(function(){
                var oldBeforeunload = window.onbeforeunload;
                if(oldBeforeunload){
                    window.onbeforeunload = wrapBeforeUnloadListener(oldBeforeunload)
                }
            }, 500);
        }
        unsafeEval(hookAlertFunction.toString());
    }
    // 初始化选择器
    function initDomSelecter(){
        divDomSelector = document.createElement("div");
        divDomSelector.id = 'f2etest-selecter-mask';
        divDomSelector.className = 'f2etest-recorder';
        divDomSelector.innerHTML = '<style>#f2etest-selecter-mask{display:none;position:fixed;z-index:2147483550;background:rgba(151, 232, 81,0.5)}</style>';
        divDomSelector.addEventListener('click', function(event){
            if(lastSelectDom !== null){
                var frameId = getFrameId();
                setGlobalWorkMode('pauseAll');
                GlobalEvents.emit('selecterClick', {
                    frame: frameId,
                    path: getDomPath(lastSelectDom),
                    ctrlKey: event.ctrlKey
                });
            }
            event.stopPropagation();
            event.preventDefault();
        });
        document.body.appendChild(divDomSelector);
    }

    // 卸载选择器
    function removeDomSelecter(){
        if(divDomSelector){
            document.body.removeChild(divDomSelector);
            divDomSelector = null;
        }
    }

    // 判断事件是否在工具面板
    function isNotInToolsPannel(target){
        while(target){
            if(/f2etest-recorder/.test(target.className)){
                return false;
            }
            target = target.parentNode;
        }
        return true;
    }

    // 初始化事件
    function initRecorderEvent(){
        document.addEventListener('mousemove', function(event){
            var target = event.target;
            if(isDomSelecter){
                divDomSelector.style.display = 'none';
                var newSelectDom = document.elementFromPoint(event.clientX, event.clientY);
                if(isNotInToolsPannel(newSelectDom) && /^(HTML|IFRAME)$/i.test(newSelectDom.tagName) === false){
                    divDomSelector.style.display = 'block';
                    if(newSelectDom !== lastSelectDom){
                        var rect = newSelectDom.getBoundingClientRect();
                        divDomSelector.style.left = rect.left+'px';
                        divDomSelector.style.top = rect.top+'px';
                        divDomSelector.style.width = rect.width+'px';
                        divDomSelector.style.height = rect.height+'px';
                        var frameId = getFrameId();
                        GlobalEvents.emit('selecterHover', {
                            frame: frameId,
                            path: getDomPath(newSelectDom)
                        });
                        lastSelectDom = newSelectDom;
                    }
                }
                event.stopPropagation();
                event.preventDefault();
            }
            else if(isNotInToolsPannel(target) && !isRecording && isStopEvent){
                event.stopPropagation();
                event.preventDefault();
            }
        }, true);

        document.addEventListener('mouseover', function(event){
            if(isNotInToolsPannel(event.target) && !isRecording && isStopEvent){
                event.stopPropagation();
                event.preventDefault();
            }
        }, true);

        document.addEventListener('mouseout', function(event){
            if(isNotInToolsPannel(event.target) && !isRecording && isStopEvent){
                event.stopPropagation();
                event.preventDefault();
            }
        }, true);

        document.addEventListener('dblclick', function(event){
            if(isNotInToolsPannel(event.target) && !isRecording && isStopEvent){
                event.stopPropagation();
                event.preventDefault();
            }
        }, true);

        // catch event
        document.addEventListener('mousedown', function(event){
            var target = event.target;
            if(isNotInToolsPannel(target)){
                if(isRecording){
                    if(/^(html|select|optgroup|option)$/i.test(target.tagName) === false && isFileInput(target) === false){
                        saveParentsOffset(target);
                        addActionTarget(target);
                        var offset = target.getBoundingClientRect();
                        var path = getDomPath(target);
                        if(path !== null){
                            addActionTarget(target);
                            saveCommand('mouseDown', {
                                path: path,
                                x: event.clientX-offset.left,
                                y: event.clientY-offset.top,
                                button: event.button,
                                text: getTargetText(target)
                            });
                        }
                    }
                }
                else if(isStopEvent){
                    event.stopPropagation();
                    event.preventDefault();
                }
            }
        }, true);

        // save all parents offset
        var mapParentsOffset = {};
        function saveParentsOffset(target){
            var documentElement = document.documentElement;
            mapParentsOffset = {};
            while(target !== null){
                var nodeName = target.nodeName.toLowerCase();
                var path = getDomPath(target);
                var rect = target.getBoundingClientRect();
                mapParentsOffset[path] = {
                    left: rect.left,
                    top: rect.top
                };
                if(nodeName === 'html'){
                    target = null;
                }
                else{
                    target = target.parentNode;
                }
            }
        }

        // get the fixed offset parent
        function getFixedParent(target){
            var documentElement = document.documentElement;
            var node = target;
            var nodeName, path, offset, left, top, savedParent;
            while(node !== null){
                nodeName = node.nodeName.toLowerCase();
                path = getDomPath(node);
                if(path === null){
                    break;
                }
                offset = node.getBoundingClientRect();
                left = offset.left;
                top = offset.top;
                savedParent = mapParentsOffset[path];
                if(savedParent && left === savedParent.left && top === savedParent.top){
                    return {
                        path: path,
                        left: left,
                        top: top
                    };
                }
                if(nodeName === 'html'){
                    node = null;
                }
                else{
                    node = node.parentNode;
                }
            }
            path = getDomPath(target);
            if(path !== null){
                offset = target.getBoundingClientRect();
                return {
                    path: path,
                    left: offset.left,
                    top: offset.top
                };
            }
            else{
                return null;
            }
        }

        document.addEventListener('mouseup', function(event){
            var target = event.target;
            if(isNotInToolsPannel(target)){
                if(isRecording){
                    var tagName = target.tagName;
                    if(/^(html|select|optgroup|option)$/i.test(tagName) === false && isFileInput(target) === false){
                        // get offset of the fixed parent
                        var fixedParent = getFixedParent(target);
                        if(fixedParent !== null){
                            addActionTarget(target);
                            saveCommand('mouseUp', {
                                path: fixedParent.path,
                                x: event.clientX-fixedParent.left,
                                y: event.clientY-fixedParent.top,
                                button: event.button,
                                text: getTargetText(target)
                            });
                        }
                    }
                }
                else if(isStopEvent){
                    event.stopPropagation();
                    event.preventDefault();
                }
            }
        }, true);    

        var modifierKeys = {
            17: 'CTRL', // Ctrl
            18: 'ALT', // Alt
            16: 'SHIFT', // Shift
            91: 'META' // Command/Meta
        };

        var NonTextKeys = {
            8: 'BACK_SPACE', // BACK_SPACE
            9: 'TAB', // TAB
            13: 'ENTER', // ENTER
            19: 'PAUSE', // PAUSE
            27: 'ESCAPE', // ESCAPE
            33: 'PAGE_UP', // PAGE_UP
            34: 'PAGE_DOWN', // PAGE_DOWN
            35: 'END', // END
            36: 'HOME', // HOME
            37: 'LEFT', // LEFT
            38: 'UP', // UP
            39: 'RIGHT', // RIGHT
            40: 'DOWN', // DOWN
            45: 'INSERT', // INSERT
            46: 'DELETE' // DELETE
        };

        // catch keydown event
        var lastModifierKeydown = null;
        document.addEventListener('keydown', function(event){
            var target = event.target;
            if(isNotInToolsPannel(target)){
                var keyCode = event.keyCode;
                var modifierKey = modifierKeys[keyCode];
                var NonTextKey = NonTextKeys[keyCode];
                if(isRecording){
                    if(modifierKey){
                        // 控制键只触发一次keyDown
                        if(modifierKey !== lastModifierKeydown){
                            lastModifierKeydown = modifierKey;
                            addActionTarget(target);
                            saveCommand('keyDown', {
                                character: modifierKey
                            });
                        }
                    }
                    else if(NonTextKey){
                        addActionTarget(target);
                        saveCommand('sendKeys', {
                            keys: '{'+NonTextKey+'}'
                        });
                    }
                    else if(event.ctrlKey || event.altKey || event.shiftKey || event.metaKey){
                        var typedCharacter = String.fromCharCode(keyCode);
                        if(typedCharacter !== '' && /^[azcxv]$/i.test(typedCharacter) === true){
                            addActionTarget(target);
                            saveCommand('sendKeys', {
                                keys: typedCharacter.toLowerCase()
                            });
                        }
                    }
                }
                else if(isStopEvent){
                    event.stopPropagation();
                    event.preventDefault();
                }
            }
        }, true);
        
        // catch keyup event
        document.addEventListener('keyup', function(event){
            var target= event.target;
            if(isNotInToolsPannel(target)){
                var modifierKey = modifierKeys[event.keyCode];
                if(isRecording){
                    if(modifierKey){
                        lastModifierKeydown = null;
                        addActionTarget(target);
                        saveCommand('keyUp', {
                            character: modifierKey
                        });
                    }
                }
                else{
                    if(!isRecording && event.keyCode === 27){
                        setGlobalWorkMode('record');
                    }
                    if(isStopEvent){
                        event.stopPropagation();
                        event.preventDefault();
                    }
                }
            }
        }, true);

        // catch keypress event
        document.addEventListener('keypress', function(event){
            var target = event.target;
            if(isNotInToolsPannel(target) && /^(HTML|IFRAME)$/i.test(target.tagName) === false){
                if(isRecording){
                    var typedCharacter = String.fromCharCode(event.keyCode);
                    if(typedCharacter !== '' && /[\r\n]/.test(typedCharacter) === false){
                        addActionTarget(target);
                        saveCommand('sendKeys', {
                            keys: typedCharacter
                        });
                    }
                }
                else if(isStopEvent){
                    event.stopPropagation();
                    event.preventDefault();
                }
            }
        }, true);

        document.addEventListener('compositionend', function(event){
            var target = event.target;
            if(isNotInToolsPannel(target)){
                if(isRecording){
                    addActionTarget(target);
                    saveCommand('sendKeys', {
                        keys:event.data
                    });
                }
                else if(isStopEvent){
                    event.stopPropagation();
                    event.preventDefault();
                }
            }
        }, true);

        var lastScroll = {};
        var scrollEventTimer = null;
        document.addEventListener('scroll', function(event){
            var target = event.target;
            if(isNotInToolsPannel(target)){
                if(isRecording){
                    var pageOffset = {
                        x: window.pageXOffset,
                        y: window.pageYOffset
                    };
                    if(pageOffset.x !== lastScroll.x || pageOffset.y !== lastScroll.y){
                        scrollEventTimer && clearTimeout(scrollEventTimer);
                        scrollEventTimer = setTimeout(function(){
                            saveCommand('scrollTo', pageOffset);
                        }, 500);
                        lastScroll = pageOffset;
                    }
                }
                else if(isStopEvent){
                    event.stopPropagation();
                    event.preventDefault();
                }
            }
        }, true);

        // catch file change
        function isFileInput(target){
            return target.tagName === 'INPUT' && target.getAttribute('type') === 'file';
        }
        document.addEventListener('change', function(event){
            var target = event.target;
            if(isNotInToolsPannel(target)){
                if(isRecording){
                    if(isFileInput(target)){
                        var path = getDomPath(target);
                        var filepath = target.value || '';
                        var match = filepath.match(/[^\\\/]+$/);
                        if(path !== null && match !== null){
                            GlobalEvents.emit('showDomPath', path);
                            saveCommand('uploadFile', {
                                path: path,
                                filename: match[0],
                                text: getTargetText(target)
                            });
                        }
                    }
                    else if(target.tagName === 'SELECT'){
                        var rect = target.getBoundingClientRect();
                        if(rect.width > 0 && rect.height > 0){
                            // no record invisible select
                            var path = getDomPath(target);
                            if(path !== null){
                                var index = target.selectedIndex;
                                var option = target.options[index];
                                var value = option.getAttribute('value');
                                var type;
                                if(value){
                                    type = 'value';
                                }
                                else{
                                    type = 'index';
                                    value = index;
                                }
                                addActionTarget(target);
                                saveCommand('select', {
                                    path: path,
                                    type: type,
                                    value: value,
                                    text: getTargetText(target)
                                });
                            }
                        }
                    }
                }
                else if(isStopEvent){
                    event.stopPropagation();
                    event.preventDefault();
                }
            }
        }, true);
    }

    // 初始化dom
    function initRecorderDom(){
        var recorderLoaded = document.getElementById('f2etestloaded');
        if(recorderLoaded){
            // 定时探测DOM是否被破坏
            setTimeout(initRecorderDom, 200);
            return;
        }

        // 加载探测
        recorderLoaded = document.createElement("span");
        recorderLoaded.id = 'f2etestloaded';
        recorderLoaded.style.display = 'none';
        document.body.appendChild(recorderLoaded);

        // 初始化工具面板
        function initToolsPannel(){
            // tools pannel
            var baseUrl = chrome.extension.getURL("/");
            var divDomToolsPannel = document.createElement("div");
            divDomToolsPannel.id = 'f2etest-tools-pannel';
            divDomToolsPannel.className = 'f2etest-recorder';
            var arrHTML = [
                '<div style="padding:5px;color:#666"><strong>DomPath: </strong><span id="f2etest-path"></span></div>',
                '<div><span class="f2etest-button"><a name="f2etest-hover"><img src="'+baseUrl+'img/hover.png" alt="">添加悬停</a></span><span class="f2etest-button"><a name="f2etest-expect"><img src="'+baseUrl+'img/expect.png" alt="">添加断言</a></span><span class="f2etest-button"><a name="f2etest-vars"><img src="'+baseUrl+'img/vars.png" alt="">插入变量</a></span><span class="f2etest-button"><a name="f2etest-end"><img src="'+baseUrl+'img/end.png" alt="">结束录制</a></span></div>',
                '<style>#f2etest-tools-pannel{position:fixed;z-index:9999999;padding:20px;width:570px;box-sizing:border-box;border:1px solid #ccc;line-height:1;background:rgba(241,241,241,0.8);box-shadow: 5px 5px 10px #888888;bottom:20px;left:20px;cursor:move;}#f2etest-path{border-bottom: dashed 1px #ccc;padding:2px;color:#FF7159;}.f2etest-button{cursor:pointer;margin: 8px;}.f2etest-button a{text-decoration: none;color:#333333;font-family: arial, sans-serif;font-size: 13px;color: #777;text-shadow: 1px 1px 0px white;background: -webkit-linear-gradient(top, #ffffff 0%,#dfdfdf 100%);border-radius: 3px;box-shadow: 0 1px 3px 0px rgba(0,0,0,0.4);padding: 6px 12px;}.f2etest-button a:hover{background: -webkit-linear-gradient(top, #ffffff 0%,#eee 100%);box-shadow: 0 1px 3px 0px rgba(0,0,0,0.4);}.f2etest-button a:active{background: -webkit-linear-gradient(top, #dfdfdf 0%,#f1f1f1 100%);box-shadow: 0px 1px 1px 1px rgba(0,0,0,0.2) inset, 0px 1px 1px 0 rgba(255,255,255,1);}.f2etest-button a img{padding-right: 8px;position: relative;top: 2px;vertical-align:baseline;}</style>'
            ];
            divDomToolsPannel.innerHTML = arrHTML.join('');
            var diffX = 0, diffY =0;
            var isDrag = false;
            divDomToolsPannel.addEventListener('selectstart', function(event){
                event.stopPropagation();
                event.preventDefault();
            });
            divDomToolsPannel.addEventListener('mousedown', function(event){
                diffX = event.clientX - divDomToolsPannel.offsetLeft;
                diffY = event.clientY - divDomToolsPannel.offsetTop;
                isDrag = true;
                event.stopPropagation();
                event.preventDefault();
            });
            document.addEventListener('mousemove', function(event){
                if(isDrag && event.x > 0 && event.y > 0){
                    divDomToolsPannel.style.left = event.clientX - diffX + 'px';
                    divDomToolsPannel.style.top = event.clientY - diffY + 'px';
                    divDomToolsPannel.style.bottom = 'auto';
                    divDomToolsPannel.style.right = 'auto';
                    event.stopPropagation();
                    event.preventDefault();
                }
            });
            divDomToolsPannel.addEventListener('mouseup', function(event){
                isDrag = false;
                event.stopPropagation();
                event.preventDefault();
            });
            divDomToolsPannel.addEventListener('click', function(event){
                event.stopPropagation();
                event.preventDefault();
                var target = event.target;
                if(target.tagName === 'IMG'){
                    target = target.parentNode;
                }
                var name = target.name;
                switch(name){
                    case 'f2etest-hover':
                        hideDialog();
                        showSelector(function(domInfo, requirePause){
                            // 使事件可以触发
                            setGlobalWorkMode('pauseRecord');
                            // 添加悬停
                            GlobalEvents.emit('addHover', domInfo);
                            // 恢复录制或暂停
                            setGlobalWorkMode(requirePause?'pauseAll':'record');
                        });
                        break;
                    case 'f2etest-expect':
                        hideDialog();
                        showSelector(function(domInfo, requirePause){
                            showExpectDailog(domInfo, function(frameId, expectData){
                                GlobalEvents.emit('addExpect', {
                                    frame: frameId,
                                    data: expectData
                                })
                                setGlobalWorkMode(requirePause?'pauseAll':'record');
                            });
                        });
                        break;
                    case 'f2etest-vars':
                        hideDialog();
                        showSelector(function(domInfo, requirePause){
                            showVarsDailog(function(varInfo){
                                GlobalEvents.emit('setVar', {
                                    frame: domInfo.frame,
                                    path: domInfo.path,
                                    name: varInfo.name,
                                    value: varInfo.value
                                });
                                setGlobalWorkMode(requirePause?'pauseAll':'record');
                            });
                        });
                        break;
                    case 'f2etest-end':
                        chrome.runtime.sendMessage({
                            type: 'end'
                        });
                        break;
                }
            });
            function showSelector(callback){
                domSelectorCallback = callback;
                setGlobalWorkMode('select');
            }
            document.body.appendChild(divDomToolsPannel);
            spanShowDomPath = document.getElementById('f2etest-path');
            // 对话框
            var divDomDialog = document.createElement("div");
            var okCallback = null;
            var cancelCallback = null;
            divDomDialog.id = 'f2etest-dialog';
            divDomDialog.className = 'f2etest-recorder';
            var arrHTML = [
                '<h2 id="f2etest-dialog-title"></h2>',
                '<div id="f2etest-dialog-content"></div>',
                '<div style="padding-bottom:10px;text-align:center;"><span class="f2etest-button"><a name="f2etest-ok"><img src="'+baseUrl+'img/ok.png" alt="">确认添加</a></span><span class="f2etest-button"><a name="f2etest-cancel"><img src="'+baseUrl+'img/cancel.png" alt="">取消添加</a></span></div>',
                '<style>#f2etest-dialog{display:none;position:fixed;z-index:9999999;padding:20px;top:50%;left:50%;width:450px;margin-left:-225px;margin-top:-160px;box-sizing:border-box;border:1px solid #ccc;background:rgba(241,241,241,1);box-shadow: 5px 5px 10px #888888;}#f2etest-dialog h2{padding-bottom:10px;border-bottom: solid 1px #ccc;margin-bottom:10px;color:#333;}#f2etest-dialog ul{list-style:none;padding:0;}#f2etest-dialog li{padding: 5px 0 5px 30px;}#f2etest-dialog li label{display:inline-block;width:80px;color:#666}#f2etest-dialog li input,#f2etest-dialog li select,#f2etest-dialog li textarea{font-size:16px;border:1px solid #ccc;border-radius:2px;padding:5px;}#f2etest-dialog li input,#f2etest-dialog li textarea{width:250px;}</style>'
            ];
            divDomDialog.innerHTML = arrHTML.join('');
            document.body.appendChild(divDomDialog);
            var domDialogTitle = document.getElementById('f2etest-dialog-title');
            var domDialogContent = document.getElementById('f2etest-dialog-content');
            divDomDialog.addEventListener('click', function(event){
                event.stopPropagation();
                event.preventDefault();
                var target = event.target;
                if(target.tagName === 'IMG'){
                    target = target.parentNode;
                }
                var name = target.name;
                switch(name){
                    case 'f2etest-ok':
                        hideDialog();
                        okCallback();
                        break;
                    case 'f2etest-cancel':
                        hideDialog();
                        cancelCallback();
                        break;
                }
            });
            // 显示对话框
            function showDialog(title, content, events){
                domDialogTitle.innerHTML = title;
                domDialogContent.innerHTML = content;
                var onInit = events.onInit;
                if(onInit){
                    onInit();
                }
                okCallback = events.onOk;
                cancelCallback = events.onCancel;
                divDomDialog.style.display = 'block';
            }
            // 隐藏对话框
            function hideDialog(){
                domDialogTitle.innerHTML = '';
                domDialogContent.innerHTML = '';
                divDomDialog.style.display = 'none';
            }
            function showExpectDailog(expectTarget, callback){
                var arrHtmls = [
                    '<ul>',
                    '<li><label>断言类型: </label><select id="f2etest-expect-type" value=""><option>val</option><option>text</option><option>displayed</option><option>enabled</option><option>selected</option><option>attr</option><option>css</option><option>url</option><option>title</option><option>cookie</option><option>localStorage</option><option>sessionStorage</option></select></li>',
                    '<li id="f2etest-expect-dom-div"><label>断言DOM: </label><input id="f2etest-expect-dom" type="text" readonly /></li>',
                    '<li id="f2etest-expect-param-div"><label>断言参数: </label><input id="f2etest-expect-param" type="text" /></li>',
                    '<li><label>比较方式: </label><select id="f2etest-expect-compare"><option>equal</option><option>contain</option><option>regexp</option></select></li>',
                    '<li><label>断言结果: </label><textarea id="f2etest-expect-to"></textarea></li>',
                    '</ul>'
                ];
                var domExpectDomDiv, domExpectParamDiv, domExpectType, domExpectDom, domExpectParam, domExpectCompare, domExpectTo;
                var reDomRequire = /^(val|text|displayed|enabled|selected|attr|css)$/;
                var reParamRequire = /^(attr|css|cookie|localStorage|sessionStorage)$/;
                showDialog('添加断言：', arrHtmls.join(''), {
                    onInit: function(){
                        // 初始化dom及事件
                        domExpectDomDiv = document.getElementById('f2etest-expect-dom-div');
                        domExpectParamDiv = document.getElementById('f2etest-expect-param-div');
                        domExpectType = document.getElementById('f2etest-expect-type');
                        domExpectDom = document.getElementById('f2etest-expect-dom');
                        domExpectParam = document.getElementById('f2etest-expect-param');
                        domExpectCompare = document.getElementById('f2etest-expect-compare');
                        domExpectTo = document.getElementById('f2etest-expect-to');
                        domExpectType.onchange = function(){
                            var type = domExpectType.value;
                            domExpectDomDiv.style.display = reDomRequire.test(type) ? 'block' : 'none';
                            domExpectParamDiv.style.display = reParamRequire.test(type) ? 'block' : 'none';
                            refreshToValue();
                        };
                        domExpectParam.onchange = refreshToValue
                        function refreshToValue(){
                            var type = domExpectType.value;
                            var param = domExpectParam.value;
                            switch(type){
                                case 'url':
                                    domExpectTo.value = location.href;
                                    break;
                                case 'title':
                                    domExpectTo.value = document.title;
                                    break;
                                case 'cookie':
                                    if(param){
                                        domExpectTo.value = getCookie(param) || '';
                                    }
                                    break;
                                case 'localStorage':
                                    if(param){
                                        domExpectTo.value = localStorage.getItem(param) || '';
                                    }
                                    break;
                                case 'sessionStorage':
                                    if(param){
                                        domExpectTo.value = sessionStorage.getItem(param) || '';
                                    }
                                    break;
                                default:
                                    // 到iframe中获取默认值
                                    getExpectValue(type, expectTarget, param, function(value){
                                        domExpectTo.value = value;
                                    });                        
                            }
                        }
                        // 初始化默认值
                        domExpectType.value = 'val';
                        domExpectDom.value = expectTarget.path;
                        domExpectParam.value = '';
                        domExpectCompare.value = 'equal';
                        domExpectTo.value = '';
                        domExpectType.onchange();
                    },
                    onOk: function(){
                        var type = domExpectType.value;
                        var arrParams = [];
                        reDomRequire.test(type) && arrParams.push(domExpectDom.value);
                        reParamRequire.test(type) && arrParams.push(domExpectParam.value);
                        var compare = domExpectCompare.value;
                        var to = domExpectTo.value;
                        if(compare === 'regexp'){
                            try{
                                eval(to);
                            }
                            catch(e){
                                domExpectTo.focus();
                                return alert('请输入合法的正则表达式！');
                            }
                        }
                        var expectData = {
                            type: type,
                            params: arrParams,
                            compare:compare,
                            to: to
                        };
                        callback(expectTarget.frame, expectData);
                    },
                    onCancel: function(){
                        setGlobalWorkMode('record');
                    }
                });
            }
            function showVarsDailog(callback){
                var arrHtmls = [
                    '<ul>',
                    '<li><label>变量名: </label><select id="f2etest-vars-name" value="">',
                ];
                for(var name in testVars){
                    arrHtmls.push('<option>'+name+'</option>');
                }
                arrHtmls.push('</select></li>');
                arrHtmls.push('<li><label>变量值: </label><input id="f2etest-vars-value" type="text" readonly /></li>');
                arrHtmls.push('</ul>');
                var domVarsName, domVarsValue;
                showDialog('插入变量：', arrHtmls.join(''), {
                    onInit: function(){
                        // 初始化dom及事件
                        domVarsName = document.getElementById('f2etest-vars-name');
                        domVarsValue = document.getElementById('f2etest-vars-value');
                        domVarsName.onchange = function(){
                            var value = testVars[domVarsName.value];
                            domVarsValue.value = value;
                        };
                        domVarsName.onchange();
                    },
                    onOk: function(){
                        var varName = domVarsName.value;
                        callback({
                            name: varName,
                            value: testVars[varName]
                        });
                    },
                    onCancel: function(){
                        setGlobalWorkMode('record');
                    }
                });
            }
        }

        if(isIframe === false){
            initToolsPannel();
        }
        isRecording = true;

        // 定时探测DOM是否被破坏
        setTimeout(initRecorderDom, 200);
    }

})();
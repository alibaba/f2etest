(function(){
    var f2etestUrl = localStorage.getItem('f2etestUrl') || '';
    var browsersApi = '/getAllBrowsers';
    var initApi = '/initUser';

    // 默认浏览器选择列表
    var defautlSelectList = {
        'hostsshare': true,
        'ie6': true,
        'ie7': true,
        'ie8': true,
        'ie9': true,
        'ie10': true,
        'ie11': true,
        'edge': true,
        'chrome': true,
        'firefox': true
    };
    var selectList = defautlSelectList;
    var savedSelectList = localStorage.getItem('selectList');
    if(savedSelectList){
        selectList = JSON.parse(savedSelectList);
    }
    var mode = 'work';
    var arrBrowsers = [];

    var jMessage = $('#message');
    var jBrowsers = $('#browsers');
    var jListContainer = $('#list-container');
    var jBtnMore = $('#btnMore');

    // 显示消息
    function showMessage(html){
        jBrowsers.hide();
        jMessage.html(html).show();
    }

    // 初始化浏览器界面
    function initBrowsers(){
        if(!f2etestUrl){
            return showMessage('初次使用，请<a href="options.html" target="_blank">先初始化</a>。');
        }
        util.getJson(f2etestUrl + browsersApi, function(error, content){
            if(error === null){
                arrBrowsers = content.arrBrowsers;
                if(content.remoteInited){
                    showBrowsers();
                }
                else{
                    // 首次使用，初始化账号
                    showMessage('<p><img src="img/loading.gif"> 首次使用，正在初始化账号……</p><p>请勿关闭当前提示信息。</p>');
                    util.getJson(f2etestUrl + initApi, function(error, content){
                        if(error === null){
                            if(!content.error){
                                showBrowsers();
                            }
                            else{
                                showMessage(content.error);
                            }
                        }
                        else{
                            showMessage(error);
                            
                        }
                    });
                }
            }
            else{
                if(error === 'redirect'){
                    showMessage('您未登录F2etest，请<a href="'+f2etestUrl+'" target="_blank">点击这里</a>登录');
                }
                else{
                    showMessage('错误码：'+ error);
                }
            }
        });
        jListContainer.on('click','a',function(e){
            var ctrlKey = e.ctrlKey || e.metaKey;
            if(mode === 'more'){
                $(this).parent().toggleClass('select');
            }
            else{
                var browserId = $(this).closest('li').data('id');
                chrome.tabs.getSelected(null, function(tab) {
                    var url = tab.url;
                    url = /^https?:\/\//.test(url) && !/f2etest/.test(url) ? url : 'about:blank';
                    var f2etestLink = f2etestUrl+'/openapp?id='+encodeURIComponent(browserId)+'&url='+encodeURIComponent(url);
                    chrome.tabs.create({url: f2etestLink, active: !ctrlKey});
                });                
            }
            return false;
        });
        jBtnMore.click(function(){
            mode = mode === 'work' ? 'more' : 'work';
            if(mode === 'work'){
                var newSelectList = {};
                jListContainer.find('.select').each(function(i, target){
                    newSelectList[$(target).data('id')] = true;
                });
                selectList = newSelectList;
                localStorage.setItem('selectList', JSON.stringify(selectList));
                jListContainer.removeClass('more');
                jBtnMore.text('选择更多');
            }
            else{
                jListContainer.addClass('more');
                jBtnMore.text('保存');
            }
            return false;
        });
    }

    // 显示浏览器列表
    function showBrowsers(){
        var arrHtml = [];
        arrBrowsers.forEach(function(browser){
            var browserId = browser.id;
            var selectClass  = selectList[browserId] ? 'select' : '';
            arrHtml.push('<li class="'+selectClass+'" data-id="'+browserId+'"><a href="#" title="'+browser.name+'"><span><img src="http:'+browser.icon+'" width="32" height="32" /></span><span>'+(browser.shortname?browser.shortname:browser.name)+'</span></a><span class="selmark">√</span></li>');
        });
        jListContainer.html(arrHtml.join(''));
        jMessage.hide();
        jBrowsers.show();
    }

    initBrowsers();
})();
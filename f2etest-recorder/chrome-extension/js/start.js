(function(){
    var frmStart = document.getElementById('formStart');
    var txtUrl = document.getElementById('url');
    txtUrl.focus();
    frmStart.onsubmit = function(){
        var url = txtUrl.value;
        if(/^([\w-]+\.)+(com|net|org)/.test(url)){
            url = 'http://' + url;
        }
        if(/^https?:\/\//i.test(url)){
            chrome.runtime.sendMessage({
                type: 'command',
                data: {
                    frame: null,
                    cmd: 'url',
                    data: {
                        url: url
                    }
                }
            });
            location.href = url;
        }
        else{
            alert('请输入标准的url。');
        }
        return false;
    }
})();


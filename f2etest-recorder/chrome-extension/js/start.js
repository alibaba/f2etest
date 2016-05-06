(function(){
    var frmStart = document.getElementById('formStart');
    var txtUrl = document.getElementById('url');
    txtUrl.focus();
    frmStart.onsubmit = function(){
        var url = txtUrl.value;
        if(/^https?:\/\//i.test(url)){
            chrome.runtime.sendMessage({
                frame: null,
                cmd: 'url',
                data: {
                    url: url
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


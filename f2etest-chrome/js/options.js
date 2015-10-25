(function(){
    var f2etestUrl = localStorage.getItem('f2etestUrl') || '';
    var jF2etestUrl = $('#f2etestUrl');
    var jBtnSave = $('#btnSave');

    jF2etestUrl.val(f2etestUrl).focus();
    jBtnSave.click(function(){
        var newF2etestUrl = jF2etestUrl.val();
        if(newF2etestUrl){
            if(/^\s*https?:\/\/[^\/]+$/.test(newF2etestUrl)){
                var versionUrl = newF2etestUrl+'/version';
                util.getJson(versionUrl, function(error, content){
                    if(error === null && content.name === 'f2etest'){
                        localStorage.setItem('f2etestUrl', newF2etestUrl);
                        alert('保存成功！');
                        window.close();
                    }
                    else{
                        alert('F2etest站点校验失败：' + versionUrl);
                    }
                });
            }
            else{
                jF2etestUrl.focus();
                alert('F2etest Url必需为如下格式：http://xxx.xxx.com');
            }
        }
        else{
            jF2etestUrl.focus();
            alert('请输入F2etest Url');
        }
    });

})();
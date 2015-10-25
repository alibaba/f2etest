(function(){
    var util = {
        getJson: function(url, callback){
            this.getUrl(url, function(error, content){
                if(error === null){
                    try{
                        content = JSON.parse(content);
                    }
                    catch(e){
                        error = 'json';
                        content = null;
                    }
                }
                callback(error, content);
            });
        },
        getUrl: function(url, callback){
            url = url + '?rnd=' + new Date().getTime();
            var xmlhttp = new XMLHttpRequest();
            xmlhttp.onreadystatechange = function() {
                if (xmlhttp.readyState === 4) {
                    if(xmlhttp.status === 200){
                        if(xmlhttp.responseURL === url){
                            callback(null, xmlhttp.responseText);
                        }
                        else{
                            callback('redirect');
                        }
                    }
                    else{
                        callback(xmlhttp.status);
                    }
                }
            };
            xmlhttp.open("GET", url, true);
            xmlhttp.send(null);
        }
    }
    window.util = util;
})();
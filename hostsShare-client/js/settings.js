(function(_win,undefined){
    var settings={};
    settings.get = function(key){
        return localStorage.getItem(key)
    }

    settings.set = function(key, value){
        localStorage.setItem(key, value);
    }
    _win.settings = settings;
})(window);
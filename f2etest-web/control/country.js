module.exports = function(app, config) {

    var arrCountryProxy = config.arrCountryProxy;
    var mapCountryProxy = {};
    arrCountryProxy.forEach(function(country){
        mapCountryProxy[country.name] = country.proxy;
    });
    var countryProxyWhiteList = config.siteInfo.countryProxyWhiteList;
    
    // pac接口，返回国家代理地址
    app.all('/getCountryProxy.pac', function(req, res) {
        var query = req.query;
        var body = req.body;
        var name = query['name'] || '';
        var proxy = mapCountryProxy[name];
        if(proxy){
            res.writeHead(200, { 'Content-Type': 'text/plain' });
            var pacContent;
            if(countryProxyWhiteList){
                pacContent = 'function FindProxyForURL(url, host){if('+countryProxyWhiteList+'.test(host))return "PROXY '+proxy+'";\r\nreturn "PROXY 127.0.0.1:1111"}';
            }
            else{
                pacContent = 'function FindProxyForURL(url, host){return "PROXY '+proxy+'";}';;
            }
            res.end(pacContent);
        }
        else{
            res.end('Country name search failed!');
        }
    });
};
module.exports = function(app, config) {
    var indexAppList = [];
    config.arrAppList.forEach(function(appInfo){
        indexAppList.push({
            id: appInfo.id,
            name: appInfo.name,
            icon: appInfo.icon
        });
    });
    app.get('/', function(req, res){
        var viewData = req.viewData;
        viewData.appList = indexAppList;
        viewData.counryList = config.arrCountryProxy;
        viewData.host = req.headers['host'];
        viewData.navTab = 'browser';
        viewData.navPage = '';
        res.render('index', viewData);
    });
}
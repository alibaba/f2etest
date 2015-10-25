var ejs = require('ejs');

module.exports = function(app, config) {
    var siteInfo = config.siteInfo;
    var arrAppList = config.arrAppList;
    var arrServerList = config.arrServerList;
    app.get('/rdfeed-:apikey.xml', function(req, res) {
        var params = req.params;
        var apikey = params['apikey'] || '';
        app.render('rdfeed',{
            arrAppList: arrAppList,
            arrServerList: arrServerList,
            apikey: apikey
        }, function(err, content){
            res.type('xml').end('\ufeff'+content);
        });
    });
}
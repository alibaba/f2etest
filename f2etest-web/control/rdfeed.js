var ejs = require('ejs');

module.exports = function(app, config) {
    var siteInfo = config.siteInfo;
    var arrAppList = config.arrAppList;
    var arrServerList = config.arrServerList;
    app.get('/rdfeed.xml', function(req, res) {
        app.render('rdfeed',{
            arrAppList: arrAppList,
            arrServerList: arrServerList
        }, function(err, content){
            res.type('xml').end('\ufeff'+content);
        });
    });
}
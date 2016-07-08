
module.exports = function(app, config) {

    app.get('/autotest', function(req, res){
        var viewData = req.viewData;
        viewData.host = req.headers['host'];
        viewData.hostname = req.hostname;
        var user = req.session.user;
        viewData.userid = user.userid;
        viewData.apiKey = user.apiKey;
        viewData.navTab = 'autotest';
        viewData.navPage = '';
        res.render('autotest', viewData);
    });

}
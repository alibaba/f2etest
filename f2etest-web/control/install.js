module.exports = function(app, config) {
    app.get('/install', function(req, res) {
        var viewData = req.viewData;
        var user = req.session.user;
        viewData.remotePassword = user.remotePassword;
        viewData.apiKey = user.apiKey;
        viewData.host = req.headers['host'];
        res.render('install', viewData);
    });
}
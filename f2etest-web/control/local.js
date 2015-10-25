module.exports = function(app, config) {
    app.get('/local', function(req, res) {
        var viewData = req.viewData;
        var user = req.session.user;
        viewData.apiKey = user.apiKey;
        viewData.host = req.headers['host'];
        res.render('local', viewData);
    });
}
module.exports = function(app, config) {
    app.get('/api', function(req, res) {
        var viewData = req.viewData;
        viewData.host = req.headers['host'];
        var user = req.session.user;
        viewData.userid = user.userid;
        viewData.apiKey = user.apiKey;
        viewData.userIp = (req.ip || '').replace(/^::ffff:/,'');
        res.render('api', viewData);
    });
};
module.exports = function(app, config) {
    app.get('/download', function(req, res) {
        var viewData = req.viewData;
        var user = req.session.user;
        viewData.remotePassword = user.remotePassword;
        viewData.hostname = req.hostname;
        res.render('download', viewData);
    });
}
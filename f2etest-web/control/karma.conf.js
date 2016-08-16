module.exports = function(app, config) {
    app.get('/karma.conf.js', function(req, res) {
        var user = req.session.user;
        var userid = user.userid;
        var viewData = req.viewData;
        viewData.apiKey = user.apiKey;
        viewData.host = req.headers['host'];
        app.render('karma_conf',viewData, function(err, content){
            res.attachment('karma.conf.js').end(content);
        });
    });
}
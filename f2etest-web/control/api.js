module.exports = function(app, config) {
    app.get('/api', function(req, res) {
        var viewData = req.viewData;
        viewData.host = req.headers['host'];
        res.render('api', viewData);
    });
};
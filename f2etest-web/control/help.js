module.exports = function(app, config) {
    app.get('/help', function(req, res) {
        var viewData = req.viewData;
        viewData.navTab = 'browser';
        viewData.navPage = 'help';
        res.render('help', req.viewData);
    });
}
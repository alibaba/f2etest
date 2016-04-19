module.exports = function(app, config) {
    app.get('/changelog', function(req, res) {
        var viewData = req.viewData;
        viewData.navTab = '';
        viewData.navPage = '';
        res.render('changelog', req.viewData);
    });
}
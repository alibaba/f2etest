module.exports = function(app, config) {
    app.get('/help', function(req, res) {
        res.render('help', req.viewData);
    });
}
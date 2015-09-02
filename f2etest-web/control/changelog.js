module.exports = function(app, config) {
    app.get('/changelog', function(req, res) {
        res.render('changelog', req.viewData);
    });
}
module.exports = function(app, config) {
    app.get('/version', function(req, res) {
        var query = req.query;
        var callback = query['callback'] || '';

        var package = config.package;
        var result = {
            name: 'f2etest',
            version: package.version,
            author: package.author,
            license: package.license
        };

        result = JSON.stringify(result);

        if(callback){
            res.type('js'); 
            result = callback+'('+result+');';
        }
        else{
            res.type('json'); 
        }

        res.end(result);
    });
}
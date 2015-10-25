var crypto = require('crypto');
var async = require('async');
var request = require('request');
var pool =require('../lib/db.js');

module.exports = function(app, config) {
    app.get('/getAllBrowsers', function(req, res) {
        var query = req.query;
        var callback = query['callback'] || '';

        var arrBrowsers = [];
        var host = req.headers['host'];
        config.arrAppList.forEach(function(appInfo){
            arrBrowsers.push({
                id: appInfo.id,
                name: appInfo.name,
                shortname: appInfo.shortname,
                icon: '//'+host+'/imgs/app/'+appInfo.id+'.png'
            });
        });

        var result = {
            remoteInited: req.viewData.remoteInited,
            arrBrowsers: arrBrowsers
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
};
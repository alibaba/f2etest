var crypto = require('crypto');
var async = require('async');
var pool =require('../lib/db.js');
var initUser = require('../lib/initUser.js')

module.exports = function(app, config) {
    app.get('/initUser', function(req, res) {
        var query = req.query;
        var callback = query['callback'] || '';

        if(callback){
            res.type('js'); 
        }
        else{
            res.type('json'); 
        }

        var user = req.session.user;
        var userid = user.userid;
        var result = {};
        if(!user.remotePassword){
            initUser(userid, function(errorMessage, remotePassword){
                var result = {};
                if(errorMessage){
                    result.error = errorMessage;
                } else {
                    user.remotePassword = remotePassword;
                    result.message = 'All account initialized.'
                }
                result = JSON.stringify(result);
                
                if(callback){
                    result = callback+'('+result+');';
                }

                res.end(result);
            })
        }
        else{
            result.error = 'Current user already initialized.';

            result = JSON.stringify(result);
            if(callback){
                result = callback+'('+result+');';
            }

            res.end(result);
        }
    });
};
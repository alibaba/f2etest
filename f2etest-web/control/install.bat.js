module.exports = function(app, config) {
    var arrServerList = config.arrServerList;
    app.get('/install.bat', function(req, res) {
        var user = req.session.user;
        var userid = user.userid;
        var remotePassword = user.remotePassword;
        res.attachment('install.bat');
        var arrContent = [];
        arrServerList.forEach(function(server){
            arrContent.push('cmdkey /generic:'+server.ip+' /user:'+userid+' /pass:'+remotePassword);
        });
        res.end(arrContent.join('\r\n'));
    });
}
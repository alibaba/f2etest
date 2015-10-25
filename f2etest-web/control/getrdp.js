module.exports = function(app, config) {
    var mapAppList = config.mapAppList;
    var mapServerList = config.mapServerList;
    app.get('/getrdp', function(req, res) {
        var query = req.query;
        var apikey = query['apikey'] || '';
        var id = query['id'] || '';
        var appInfo = mapAppList[id];
        if(appInfo){
            var arrRdp = [
                'screen mode id:i:2',
                'session bpp:i:24',
                'compression:i:1',
                'keyboardhook:i:2',
                'audiomode:i:2',
                'redirectdrives:i:0',
                'redirectprinters:i:0',
                'redirectcomports:i:0',
                'redirectsmartcards:i:0',
                'displayconnectionbar:i:1',
                'autoreconnection enabled:i:1',
                'authentication level:i:0',
                'domain:s:',
                'shell working directory:s:',
                'allow font smoothing:i:1',
                'disable wallpaper:i:1',
                'disable full window drag:i:1',
                'disable menu anims:i:1',
                'disable themes:i:0',
                'disable cursor setting:i:0',
                'bitmapcachepersistenable:i:1',
                'drivestoredirect:s:*'
            ];
            var serverInfo = mapServerList[appInfo.server];
            arrRdp.push('full address:s:'+serverInfo.ip);
            var program = appInfo.program
            if(program){
                var cmdParameter = 'proxy default desktop '+apikey+'';
                arrRdp.push('alternate shell:s:'+program+' '+cmdParameter);
                arrRdp.push('remoteapplicationprogram:s:'+program);
                arrRdp.push('remoteapplicationcmdline:s:'+cmdParameter);
                if(serverInfo.remoteApp){
                    arrRdp.push('remoteapplicationmode:i:1');
                }
            }
            res.type('rdp');
            res.end(arrRdp.join('\r\n'));
        }
        else{
            res.status(404).end('App id match failed.');
        }
    });
}
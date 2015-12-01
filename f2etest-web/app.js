var cluster = require('cluster');
var fs = require('fs');
var express = require('express');
var bodyParser = require('body-parser');
var cookieParser = require('cookie-parser');
var session = require('cookie-session');
var ejs = require('ejs');
var i18n = require('i18n');
var request = require('request');
var sso = require('./lib/sso.js');

var env = process.env.NODE_ENV || 'development'

// init config
var config = {};
config.siteInfo = require('./conf/site.json');
config.arrAppList = require('./conf/app.json');
config.arrServerList = require('./conf/server.json');
config.package = require('./package.json');
var workerId = Number(cluster.worker && cluster.worker.id || 0);
config.workerId = workerId;

var mapAppList = {};
config.arrAppList.forEach(function(appInfo){
    mapAppList[appInfo.id] = appInfo;
});
config.mapAppList = mapAppList;
var mapServerList = {};
config.arrServerList.forEach(function(serverInfo){
    mapServerList[serverInfo.id] = serverInfo;
});
config.mapServerList = mapServerList;

var pool =require('./lib/db.js');

var app = express()

app.use(session({
    keys: ['f2etest']
}));
app.use(express.static(__dirname + '/public', { maxAge: 180000 }));
app.use(cookieParser());
app.use(bodyParser.urlencoded({
    extended: true,
    limit: '50mb'
}));
app.engine('html', ejs.renderFile);
app.set('views', __dirname + '/views');
app.set('view engine', 'html');

if(env === 'production'){
    app.set('view cache', true);
}

// SSO登录
sso(app);

// i18n
i18n.configure({
    locales:['en', 'zh-cn', 'zh-tw'],
    defaultLocale: 'en',
    directory: __dirname + '/i18n',
    updateFiles: false,
    extension: '.js'
});
app.set('trust proxy', '127.0.0.1');
app.use(i18n.init);

// 初始化新用户
app.use(function(req ,res, next){
    var package = config.package;
    var siteInfo = config.siteInfo;
    var user = req.session.user;
    // 已登录
    if(user){
        var userid = user.userid;
        var username = user.username;
        // 初始化模板公共数据
        req.viewData = {
            appVersion: package.version,
            siteName: siteInfo.name,
            siteAbout: siteInfo.about,
            siteIcon: siteInfo.icon,
            siteFooter: siteInfo.footer,
            userid: userid,
            username: username
        };
        pool.query('select RemotePassword,ApiKey from appUsers where UserId = ?;', userid, function(err, rows){
            if(rows.length > 0){
                var remotePassword = rows[0].RemotePassword;
                user.remotePassword = remotePassword;
                user.apiKey = rows[0].ApiKey;
                req.viewData.remoteInited = remotePassword?true:false;
                pool.query('update appUsers set LastTime = now(),LastIp = ? where UserId = ?', [req.ip,userid]);
                next();
            }
            else{
                user.remotePassword = null;
                req.viewData.remoteInited = false;
                // 创建新用户
                pool.query('insert into appUsers set ?', {UserId: userid}, function(){
                    next();
                });
            }
        });
    }
    else{
        next();
    }
});

// init control
var files = fs.readdirSync('./control/');
files.forEach(function(file){
    require('./control/'+file)(app, config);
});

app.listen(config.siteInfo.port);

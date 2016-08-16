var pool = require('../lib/db.js');
var hostsServer = require('../lib/hostsserver');
var async = require('async');
var siteInfo = require('../conf/site.json');
var WebDriver = require('../lib/webdriver');
var WebDriverHub = require('../lib/webdriverhub');
var utils = require('../lib/utils');

var checkNodesInterval = siteInfo.wdCheckNodesInterval || 5000; // 定时检查节点频率
var checkNodesParallelLimit = siteInfo.wdCheckNodesParallelLimit || 16; // 定时检查节点并发限制

// 定时检查所有节点工作状态
function checkNodeAlive(){
    pool.query('update wd_nodes set work_status = 0 where work_status > 0 and timestampdiff(second,last_report_time, now()) > 5 and (isnull(last_apply_time) or timestampdiff(second,last_apply_time, now()) > 35)');
}
setInterval(checkNodeAlive, 1000);

WebDriverHub.start();

module.exports = function(app, config) {

    app.get('/openWdBrowser', function(req, res){
        var query = req.query;
        var browserId = query['browserId'] || '';
        pool.query('select b.browser_name,b.browser_version,n.node_ip,n.node_name,n.rdp_support from wd_browsers as b left join wd_nodes as n on b.node_id = n.node_id where browser_id = ?', browserId, function(err, rows){
            var row = rows[0];
            if(row && row.rdp_support){
                var siteInfo = config.siteInfo;
                var nodeIp = row.node_ip;
                var nodeName = row.node_name;
                var userid = 'node'+nodeName;
                var password = 'hello1234';
                var browserName = row.browser_name;
                var browserFilename = browserName.toLowerCase();
                if(row.browser_version){
                    browserName += ' ' + row.browser_version;
                    browserFilename += row.browser_version;
                }
                var baseUrl = 'http://'+req.headers.host+req._parsedUrl.pathname.replace(/\/[^\/]+$/,'\/');
                var icon = baseUrl+'imgs/app/'+browserFilename+'.png';

                var guacamoleUrl = siteInfo.guacamoleApi + '?id=c%2F'+nodeIp+'&username='+encodeURIComponent(userid)+'&password='+encodeURIComponent(password)+'&title='+encodeURIComponent(browserName)+'&icon='+encodeURIComponent(icon);

                res.status(302).redirect(guacamoleUrl);
            }
            else{
                res.status(404).end('browserId match failed or rdp no supported.');
            }
        });       
    });

    app.get('/getWdPac', function(req, res){
        var query = req.query;
        var nodeId = query['nodeId'] || '';
        var hostsServerName = 'wdnodes_'+nodeId;
        hostsServer.getProxyPort(hostsServerName, function(localIp, workPort){
            var pacContent = 'function FindProxyForURL(url, host){return "PROXY '+localIp+':'+workPort+'";}';
            res.end(pacContent);
        });
    });

    app.get('/listWdBrowsers', function(req, res){
        var query = req.query;
        var callback = query['callback'] || '';

        pool.query('select b.browser_name as browserName,b.browser_version as browserVersion,count(case when n.work_status =1 then 1 end) as idleCount,group_concat(concat(CAST(browser_id as char),":",CAST(n.work_status AS CHAR))) as workStatus from wd_browsers as b left join wd_nodes as n on b.node_id = n.node_id group by browser_name,browser_version order by b.browser_name;', function(err, rows){

            var result = {
                arrBrowsers: rows || []
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

    });

    app.get('/webdriver', function(req, res){
        var viewData = req.viewData;
        viewData.host = req.headers['host'];
        viewData.hostname = req.hostname;
        var user = req.session.user;
        viewData.userid = user.userid;
        viewData.apiKey = user.apiKey;
        viewData.navTab = 'webdriver';
        viewData.navPage = '';
        res.render('webdriver', viewData);
    });

    app.get('/wd_api', function(req, res){
        var viewData = req.viewData;
        viewData.host = req.headers['host'];
        var user = req.session.user;
        viewData.userid = user.userid;
        viewData.apiKey = user.apiKey;
        viewData.navTab = 'webdriver';
        viewData.navPage = 'wdapi';
        res.render('wdapi', viewData);
    });

    app.get('/wd_nodejs', function(req, res){
        var viewData = req.viewData;
        viewData.host = req.headers['host'];
        viewData.hostname = req.hostname;
        var user = req.session.user;
        viewData.userid = user.userid;
        viewData.apiKey = user.apiKey;
        viewData.navTab = 'webdriver';
        viewData.navPage = 'wdnodejs';
        res.render('wdnodejs', viewData);
    });

    app.get('/reportWdNode', function(req, res){
        var query = req.query;
        var clientIp = req.ip || '';
        var nodeName = query['nodename'] || '';
        var rdp = query['rdp'] || '';
        var status = query['status'] || '';
        var browsers = query['browsers'] || '';
        if(clientIp && nodeName !== '' && rdp !== '' && status !== '' && browsers !== ''){
            status = parseInt(status, 10);
            pool.query('update wd_nodes set work_status = ?, last_report_time = now() where node_ip = ? and node_name = ? and (isnull(last_apply_time) or timestampdiff(second,last_apply_time, now()) > 30)', [status, clientIp, nodeName], function(error, result){
                if(!error && result.changedRows === 0){
                    var mapInsert = {
                        work_status: status,
                        node_ip: clientIp,
                        node_name: nodeName,
                        rdp_support: rdp
                    };
                    pool.query('insert into wd_nodes set last_report_time = now(), ?', mapInsert, function(error, result){
                        if(!error && result){
                            var nodeId = result.insertId;
                            var arrBrowsers = browsers.split(/,\s*/);
                            arrBrowsers.forEach(function(browser){
                                var arrBrowserInfo = browser.split(' ');
                                var browserName = arrBrowserInfo[0];
                                var brwoserVersion = arrBrowserInfo[1] || '';
                                var mapInsert = {
                                    browser_name: browserName,
                                    browser_version: brwoserVersion,
                                    node_id: nodeId
                                };
                                pool.query('insert into wd_browsers set ?', mapInsert);
                            });
                        }
                    });
                }
            });
            res.end('ok');
        }
        else{
            res.end('error');
        }
    });
}
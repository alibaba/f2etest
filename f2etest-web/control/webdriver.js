var pool = require('../lib/db.js');
var hostsServer = require('../lib/hostsserver');
var JWebDriver = require('jwebdriver');
var async = require('async');
var siteInfo = require('../conf/site.json');
var WebDriver = require('../lib/webdriver');
var utils = require('../lib/utils');

var checkNodesInterval = siteInfo.wdCheckNodesInterval || 5000; // 定时检查节点频率
var checkNodesParallelLimit = siteInfo.wdCheckNodesParallelLimit || 16; // 定时检查节点并发限制

// 定时检查所有节点工作状态
function checkNodes(){
    pool.query('select node_id, node_ip, node_name from wd_nodes', function(err, rows){
        var arrTasks = [];
        if(rows){
            rows.forEach(function(row){
                arrTasks.push(function(callback){
                    var driver = new JWebDriver({
                        'host': row.node_ip,
                        'port': '40'+row.node_name
                    });
                    driver.sessions(function(error, arrSessions){
                        var work_status = error ? 0 : arrSessions.length > 0 ? 2 : 1;
                        pool.query('update wd_nodes set work_status = ? where node_id = ?', [work_status, row.node_id], callback);
                    });
                });
            });
        }
        async.parallelLimit(arrTasks, checkNodesParallelLimit, function(err, results){
            setTimeout(checkNodes, checkNodesInterval);
        });
    });
}
checkNodes();

module.exports = function(app, config) {
    app.all('/applyWdBrowser', function(req, res) {
        var query = req.query;
        var body = req.body;
        var callback = query['callback'] || '';
        var userid = query['userid'] || body['userid'] || '';
        var apiKey = query['apiKey'] || body['apiKey'] || '';
        var browserName = query['browserName'] || body['browserName'] || '';
        var browserVersion = query['browserVersion'] || body['browserVersion'] || '';
        var hosts = query['hosts'] || body['hosts'] || '';
        var proxy = query['proxy'] || body['proxy'] || '';
        utils.checkApiKey(userid, apiKey, function(isSuccess){
            var result;
            if(isSuccess){
                WebDriver.applyWdBrowser(userid, browserName, browserVersion, hosts, proxy, function(err, result){
                    if(err){
                        result = {
                            error: err
                        };
                    }
                    else{
                        result = {
                            message: result
                        };
                    }
                    endRes(result);
                });
            }
            else{
                result = {
                    error: 'ApiKey check failed!'
                };
                endRes(result);
            }
        });
        function endRes(result){
            result = JSON.stringify(result);

            if(callback){
                res.type('js'); 
                result = callback+'('+result+');';
            }
            else{
                res.type('json'); 
            }

            res.end(result);
        }
    });

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
                var program = 'c:\\f2etest\\webdriver\\opennode.bat '+nodeName+'';

                var guacamoleUrl = siteInfo.guacamoleApi + '?id=c%2F'+nodeIp+'&username='+encodeURIComponent(userid)+'&password='+encodeURIComponent(password)+'&title='+encodeURIComponent(browserName)+'&icon='+encodeURIComponent(icon);
                guacamoleUrl += '&program='+encodeURIComponent(program);

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
        var user = req.session.user;
        viewData.userid = user.userid;
        viewData.apiKey = user.apiKey;
        viewData.navTab = 'webdriver';
        viewData.navPage = 'wdnodejs';
        res.render('wdnodejs', viewData);
    });
}
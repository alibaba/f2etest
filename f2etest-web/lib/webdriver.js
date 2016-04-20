var pool = require('./db');
var hostsServer = require('./hostsserver');
var JWebDriver = require('jwebdriver');
var async = require('async');

var mapNodeWait = {};

// 申请wd节点
function applyWdNode(userid, browserName, browserVersion, callback){
    var strBrowserSql = 'b.browser_name = ?';
    var arrBrowserInfo = [browserName];
    if(browserVersion){
        strBrowserSql += ' and b.browser_version = ?';
        arrBrowserInfo.push(browserVersion);
    }
    pool.query('select b.browser_id,b.browser_name,b.browser_version,b.node_id,n.node_ip,n.node_name from wd_browsers as b left join wd_nodes as n on b.node_id = n.node_id where n.work_status = 1 and '+strBrowserSql+' order by b.browser_id limit 1;', arrBrowserInfo, function(err, rows){
        if(rows.length === 1){
            var row = rows[0];
            var browserId = row.browser_id;
            var browserName = row.browser_name;
            var browserVersion = row.browser_version;
            var browserNameId = browserName + browserVersion;
            if(browserName === 'IE'){
                if(mapNodeWait[browserNameId]){
                    setTimeout(function(){
                        applyWdNode(userid, browserName, browserVersion, callback);
                    }, 1000);
                }
                else{
                    mapNodeWait[browserNameId] = true;
                    doNodeCallback(userid, row, function(error, result){
                        callback(error, result);
                        setTimeout(function(){
                            mapNodeWait[browserNameId] = false;
                        }, 6000);
                    });
                }
            }
            else{
                doNodeCallback(userid, row, callback);
            }
            
        }
        else{
            callback('No matched idle browser, please try again later.');
        }
    });
}

function doNodeCallback(userid, row, callback){
    var browserId = row.browser_id;
    var browserName = row.browser_name;
    var browserVersion = row.browser_version;
    var wdNodeId = row.node_id;
    var wdHost = row.node_ip;
    var wdPort = '40'+row.node_name;
    pool.query('update wd_nodes set work_status = 2, last_apply_userid = ?, last_apply_time = now() where node_id = ?', [userid, wdNodeId], function(err){
        pool.query('insert into wd_logs set type = "node", userid = ?, data = ?, log_time = now()', [userid, wdNodeId]);
        callback(null, {
            browserId: browserId,
            browserName: browserName,
            browserVersion: browserVersion,
            wdNodeId: wdNodeId,
            wdHost: wdHost,
            wdPort: wdPort
        });
    });
}

// 申请浏览器
function applyWdBrowser(userid, browserName, browserVersion, hosts, proxy, callback){
    applyWdNode(userid, browserName, browserVersion, function(error, nodeInfo){
            if(error){
                callback(error);
            }
            else{
                var browserId = nodeInfo.browserId;
                var wdNodeId = nodeInfo.wdNodeId;
                var wdHost = nodeInfo.wdHost;
                var wdPort = nodeInfo.wdPort;
                var arrTasks = [];
                var hostsServerName = 'wdnodes_'+browserId;
                // 初始化hosts&proxy
                arrTasks.push(function(callback){
                    if(proxy){
                        var arrProxy = proxy.split(':');
                        hostsServer.setForward(hostsServerName, arrProxy[0], arrProxy[1]);
                    }
                    else{
                        hostsServer.setHosts(hostsServerName, hosts || '');
                    }
                    hostsServer.getProxyPort(hostsServerName, function(localIp, workPort){
                        callback(null, localIp+':'+workPort);
                    });
                });
                // 初始化session
                arrTasks.push(function(proxy, callback){
                    var driver = new JWebDriver({
                        'host': wdHost,
                        'port': wdPort
                    });
                    driver.session({
                        browserName: browserName,
                        browserVersion: browserVersion,
                        proxy: {
                            'proxyType': 'manual',
                            'httpProxy': proxy,
                            'sslProxy': proxy
                        }
                    }, function*(error, browser){
                        if(error){
                            callback(error);
                        }
                        else{
                            var capabilities = browser.capabilities;
                            capabilities['f2etest.browserId'] = browserId;
                            capabilities['f2etest.wdHost'] = wdHost;
                            capabilities['f2etest.wdPort'] = wdPort;
                            // yield browser.close();
                            callback(null, {
                                sessionId: browser.sessionId,
                                capabilities: capabilities
                            });
                        }
                    });
                });
                async.waterfall(arrTasks, function(err, result){
                    if(err){
                        callback(err);
                    }
                    else{
                        pool.query('insert into wd_logs set type = "browser", userid = ?, data = ?, log_time = now()', [userid, browserId]);
                        callback(null, result);
                    }
                });
            }
    });
}

module.exports = {
    applyWdNode: applyWdNode,
    applyWdBrowser: applyWdBrowser
};
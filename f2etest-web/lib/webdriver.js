var pool = require('./db');
var async = require('async');

var mapNodeWait = {};

var applyQueue = async.queue(function(applyInfo, next) {
    var userid = applyInfo.userid;
    var browserName = applyInfo.browserName;
    var browserVersion = applyInfo.browserVersion;
    var callback = applyInfo.callback;
    var strBrowserSql = 'b.browser_name = ?';
    var arrBrowserInfo = [browserName];
    if(browserVersion){
        strBrowserSql += ' and b.browser_version = ?';
        arrBrowserInfo.push(browserVersion);
    }
    function endApply(error, result){
        callback(error, result);
        next();
    }
    pool.query('select b.browser_id,b.browser_name,b.browser_version,b.node_id,n.node_ip,n.node_name from wd_browsers as b left join wd_nodes as n on b.node_id = n.node_id where n.work_status = 1 and '+strBrowserSql+' order by b.browser_id limit 1;', arrBrowserInfo, function(err, rows){
        if(rows.length === 1){
            var row = rows[0];
            var browserId = row.browser_id;
            var browserName = row.browser_name;
            var browserVersion = row.browser_version;
            var browserNameId = browserName + browserVersion;
            if(browserName === 'IE'){
                function endIeApply(){
                    mapNodeWait[browserNameId] = true;
                    doNodeCallback(userid, row, function(error, result){
                        endApply(error, result);
                        setTimeout(function(){
                            mapNodeWait[browserNameId] = false;
                        }, 6000);
                    });
                }
                if(mapNodeWait[browserNameId]){
                    var _waitTimer = setInterval(function(){
                        if(mapNodeWait[browserNameId] === false){
                            clearInterval(_waitTimer);
                            endIeApply();
                        }
                    }, 1000);
                }
                else{
                    endIeApply();
                }
            }
            else{
                doNodeCallback(userid, row, endApply);
            }
        }
        else{
            endApply('No matched idle browser, please try again later.');
        }
    });
}, 1);

// 申请wd节点
function applyWdNode(userid, browserName, browserVersion, callback){
    applyQueue.push({
        userid:userid,
        browserName:browserName,
        browserVersion:browserVersion,
        callback: callback
    });
}

function doNodeCallback(userid, row, callback){
    var browserId = row.browser_id;
    var browserName = row.browser_name;
    var browserVersion = row.browser_version;
    var wdNodeId = row.node_id;
    var wdHost = row.node_ip;
    var wdPort = '40'+row.node_name;
    pool.query('update wd_nodes set work_status = 2, last_apply_userid = ?, last_report_time = now(), last_apply_time = now() where node_id = ?', [userid, wdNodeId], function(err){
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

module.exports = {
    applyWdNode: applyWdNode
};
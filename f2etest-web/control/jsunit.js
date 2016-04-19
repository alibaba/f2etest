var pool = require('../lib/db');
var WebDriver = require('../lib/webdriver');
var utils = require('../lib/utils');
var async = require('async');
var cp = require('child_process');
var path = require('path');

module.exports = function(app, config) {
    app.get('/jsunit', function(req, res){
        var pageSize = 10;
        var query = req.query;
        var page = query['page'] || 1;
        page = parseInt(page, 10);
        page = page > 0 ? page: 1;
        var endOffset = pageSize * page;
        var startOffset = endOffset - pageSize;
        pool.query('select count(0) as count from wd_jsunit', function(error, rows){
            var taskCount = rows[0].count;
            var maxPage = Math.ceil( taskCount / pageSize );
            pool.query('select task_id,url,browser_name,browser_version,run_status,test_type,test_success,test_passed_count,test_all_count,test_ratio,line_cover,branch_cover,function_cover,add_time from wd_jsunit order by task_id desc limit ?,?', [startOffset, pageSize], function(error, rows){
                rows.forEach(function(row){
                    var appId = row.browser_name.toLowerCase() + (row.browser_version || '');
                    var appName = row.browser_name;
                    if(row.browser_version){
                        appName += ' ' + row.browser_version;
                    }
                    row.appId = appId;
                    row.appName = appName;
                    row.add_time = utils.dateFormat(row.add_time, 'yyyy-MM-dd hh:mm:ss');
                });
                var viewData = req.viewData;
                var user = req.session.user;
                viewData.userid = user.userid;
                viewData.navTab = 'jsunit';
                viewData.navPage = '';
                viewData.taskList = rows;
                var leftPage = page - 5;
                leftPage = leftPage > 0 ? leftPage : 1
                var rightPage = page + 4;
                rightPage = rightPage <= maxPage ? rightPage: maxPage;
                viewData.page = page;
                viewData.maxPage = maxPage;
                viewData.leftPage = leftPage;
                viewData.rightPage = rightPage;
                res.render('jsunit', viewData);
            });
        });
    });

    app.get('/jsunit_result', function(req, res){
        var query = req.query;
        var id = query['id'];
        pool.query('select task_id, url, browser_name, browser_version, browser_id, add_time, run_status, end_time, actual_browser_name, actual_browser_version, test_type, test_success, test_all_count, test_failed_count, test_passed_count, test_ratio, line_cover, branch_cover, function_cover, test_result_data from wd_jsunit where task_id = ?', [id], function(error, rows){
            if(!error && rows && rows.length > 0){
                var viewData = req.viewData;
                var user = req.session.user;
                viewData.userid = user.userid;
                viewData.navTab = 'jsunit';
                viewData.navPage = '';
                var result = rows[0];
                var appId = result.browser_name.toLowerCase() + (result.browser_version || '');
                var appName = result.browser_name;
                if(result.browser_version){
                    appName += ' ' + result.browser_version;
                }
                result.appId = appId;
                result.appName = appName;
                result.add_time = utils.dateFormat(result.add_time, 'yyyy-MM-dd hh:mm:ss');
                if(result.end_time){
                    result.end_time = utils.dateFormat(result.end_time, 'yyyy-MM-dd hh:mm:ss');
                }
                var test_result_data = result.test_result_data;
                var arrNewConsoles = [], testEvents = [], coverFiles = [];
                try{
                    test_result_data = JSON.parse(test_result_data);
                    var arrConsoles = test_result_data.console || [];
                    arrNewConsoles = [];
                    arrConsoles.forEach(function(log){
                        arrNewConsoles.push(log.type+'( '+log.message+' )');
                    });
                    var testResult = test_result_data.testResult;
                    testEvents = testResult && testResult.events || [];
                    var coverResult = test_result_data.coverResult;
                    coverFiles = coverResult && coverResult.files || [];
                }
                catch(e){}
                result.arrConsoles = arrNewConsoles;
                var arrHtmlCases = [];
                testEvents.forEach(function(event){
                    switch(event.type){
                        case 'suiteStart':
                            arrHtmlCases.push('<ul><li class="suite"><h1>'+event.title+'</h1>');
                            break;
                        case 'testEnd':
                            arrHtmlCases.push('<ul><li class="test '+event.status+' '+(event.speed?event.speed:'fast')+'"><h2>'+event.title);
                            if(event.duration){
                                arrHtmlCases.push('<span class="duration">'+event.duration+'ms</span> ');
                            }
                            arrHtmlCases.push('</h2>');
                            if(event.errors){
                                arrHtmlCases.push('<pre class="error">'+event.errors+'</pre>');
                            }
                            arrHtmlCases.push('</li></ul>');
                            break;
                        case 'suiteEnd':
                            arrHtmlCases.push('</ul>');
                            break;
                    }
                });
                result.strHtmlCases = arrHtmlCases.join('');
                result.coverFiles = coverFiles;
                viewData.testResult = result;
                res.render('jsunit_result', viewData);
            }
            else{
                res.end('Task id query failed.');
            }
        });
    });

    app.get('/jsunit_api', function(req, res){
        var viewData = req.viewData;
        viewData.host = req.headers['host'];
        var user = req.session.user;
        viewData.userid = user.userid;
        viewData.apiKey = user.apiKey;
        viewData.navTab = 'jsunit';
        viewData.navPage = 'jsunit_api';
        res.render('jsunitapi', viewData);
    });

    app.get('/jsunit_nodejs', function(req, res){
        var viewData = req.viewData;
        viewData.host = req.headers['host'];
        var user = req.session.user;
        viewData.userid = user.userid;
        viewData.apiKey = user.apiKey;
        viewData.navTab = 'jsunit';
        viewData.navPage = 'jsunit_nodejs';
        res.render('jsunit_nodejs', viewData);
    });

    app.get('/runJsUnit', function(req, res){
        var query = req.query;
        var body = req.body;
        var callback = query['callback'] || '';
        var format = query['format'] || '';
        var userid = query['userid'] || body['userid'] || '';
        var apiKey = query['apiKey'] || body['apiKey'] || '';
        var url = query['url'] || body['url'] || '';
        var browserName = query['browserName'] || body['browserName'] || '';
        var browserVersion = query['browserVersion'] || body['browserVersion'] || '';
        var hosts = query['hosts'] || body['hosts'] || '';
        var coverageInclude = query['coverageInclude'] || body['coverageInclude'] || '';
        var coverageExclude = query['coverageExclude'] || body['coverageExclude'] || '';
        var coverageBeautify = query['coverageBeautify'] || body['coverageBeautify'] || '';
        var timeout = query['timeout'] || body['timeout'] || '';
        var delay = query['delay'] || body['delay'] || '';
        var clientIp = req.ip || '';
        utils.checkApiKey(userid, apiKey, function(isSuccess){
            if(isSuccess){
                WebDriver.applyWdNode(userid, browserName, browserVersion, function(error, nodeInfo){
                    if(nodeInfo){
                        var arrTasks = [];
                        arrTasks.push(function(callback){
                            var mapInsert = {
                                userid : userid,
                                client_ip: clientIp,
                                url: url,
                                browser_name: nodeInfo.browserName,
                                browser_version: nodeInfo.browserVersion,
                                coverage_include: coverageInclude,
                                coverage_exclude: coverageExclude,
                                coverage_beautify: coverageBeautify,
                                hosts: hosts,
                                browser_id: nodeInfo.browserId
                            };
                            pool.query('insert into wd_jsunit set add_time = now(), ?', [mapInsert], function(error, result){
                                callback(error, result && result.insertId);
                            });
                        });
                        arrTasks.push(function(taskId, callback){
                            // 替换本机IP为局域网IP
                            url = url.replace(/(127.0.0.1|localhost)/g, clientIp);
                            hosts = hosts.replace(/(127.0.0.1)/g, clientIp);
                            var cpTask = cp.fork(path.resolve(__dirname, '../cp/jsunit.js'));
                            cpTask.on('message', function(result) {
                                var error = result.error;
                                var jsUnitResult = result.jsUnitResult;
                                if(!error){
                                    var testResult = jsUnitResult.testResult;
                                    var testSummary = testResult.summary;
                                    var mapUpdate = {
                                        run_status: 1,
                                        actual_browser_name: jsUnitResult.browserName,
                                        actual_browser_version: jsUnitResult.browserVersion,
                                        test_type: testResult.type,
                                        test_success: testSummary.all > 0 && testSummary.failed === 0,
                                        test_all_count: testSummary.all,
                                        test_failed_count: testSummary.failed,
                                        test_passed_count: testSummary.passed,
                                        test_ratio: testSummary.all > 0 ? getFix(testSummary.passed / testSummary.all * 100) : 0,
                                        test_result_data: JSON.stringify(jsUnitResult)
                                    };
                                    var coverResult = jsUnitResult.coverResult;
                                    if(coverResult && coverResult.summary){
                                        var coverSummary = coverResult.summary;
                                        mapUpdate.line_cover = coverSummary.lineRatio;
                                        mapUpdate.branch_cover = coverSummary.branchRatio;
                                        mapUpdate.function_cover = coverSummary.functionRatio;
                                    }
                                    pool.query('update wd_jsunit set end_time = now(), ? where task_id = ?', [mapUpdate, taskId], function(error, result){
                                        if(error){
                                            callback(JSON.stringify(error));
                                        }
                                        else{
                                            pool.query('insert into wd_logs set type = "jsunit", userid = ?, data = ?, log_time = now()', [userid, url]);
                                            callback(null, {
                                                taskId: taskId,
                                                result: jsUnitResult
                                            });
                                        }
                                    });
                                }
                                else{
                                    pool.query('update wd_jsunit set run_status = 2 where task_id = ?', taskId);
                                    callback(error);
                                }
                            });
                            cpTask.send({
                                wdHost: nodeInfo.wdHost,
                                wdPort: nodeInfo.wdPort,
                                url: url,
                                browserName: browserName,
                                browserVersion: browserVersion,
                                coverageInclude: coverageInclude,
                                coverageExclude: coverageExclude,
                                coverageBeautify: coverageBeautify == '1'?true:false,
                                hosts: hosts,
                                timeout: timeout,
                                delay: delay
                            });
                        });
                        async.waterfall(arrTasks, function(error, result){
                            var result;
                            if(!error){
                                result = {
                                    message: result
                                };
                            }
                            else{
                                result = {
                                    error: error
                                };
                            }
                            endRes(result);
                        });
                    }
                    else{
                        endRes({
                            error: error
                        });
                    }
                });
            }
            else{
                endRes({
                    error: 'ApiKey check failed!'
                });
            }
        });
        function getFix(num){
            return Math.round(num*100)/100;
        }
        function endRes(result){
            if(format === '1'){
                result = JSON.stringify(result, null, 4);
            }
            else{
                result = JSON.stringify(result);
            }

            if(callback){
                res.setHeader('content-type', 'application/javascript;charset=utf-8');
                result = callback+'('+result+');';
            }
            else{
                res.setHeader('content-type', 'application/json;charset=utf-8');
            }

            res.end(result);
        }
    });
};

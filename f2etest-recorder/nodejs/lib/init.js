var fs = require('fs');
var path = require('path');
var inquirer = require('inquirer');

function initConfig(){

    var configFile = path.resolve('config.json');
    var config = {};
    if(fs.existsSync(configFile)){
        var content = fs.readFileSync(configFile).toString();
        try{
            config = JSON.parse(content);
        }
        catch(e){}
    }
    var f2etest = config.f2etest;
    var server = '', userid = '', apiKey = '', browsers = 'chrome, ie 11';
    if(f2etest){
        server = f2etest.server;
        userid = f2etest.userid;
        apiKey = f2etest.apiKey;
        browsers = f2etest.browsers;
    }

    var questions = [
        {
            'type': 'input',
            'name': 'server',
            'message': '请输入f2etest的域名或IP，例如：f2etest.xxx.com',
            'default': server,
            'validate': function(input){
                return input !== '' && /^https?:\/\//.test(input) === false;
            }
        },
        {
            'type': 'input',
            'name': 'userid',
            'message': '请输入f2etest userid',
            'default': userid,
            'validate': function(input){
                return input !== '';
            }
        },
        {
            'type': 'input',
            'name': 'apiKey',
            'message': '请输入f2etest apiKey',
            'default': apiKey,
            'validate': function(input){
                return input !== '';
            }
        },
        {
            'type': 'input',
            'name': 'browsers',
            'message': '请输入需要同时测试的浏览器列表',
            'default': browsers,
            'validate': function(input){
                return input !== '';
            }
        }
    ];
    inquirer.prompt(questions).then(function(anwsers){
        anwsers.server = anwsers.server.replace(/^\s+|\s+$/g, '');
        anwsers.userid = anwsers.userid.replace(/^\s+|\s+$/g, '');
        anwsers.apiKey = anwsers.apiKey.replace(/^\s+|\s+$/g, '');
        anwsers.browsers = anwsers.browsers.replace(/^\s+|\s+$/g, '');
        var configJson = {
            f2etest: anwsers,
            vars: config.vars || {}
        };
        fs.writeFileSync(configFile, JSON.stringify(configJson, null, 4));
        console.log('config.json'.bold+'文件保存成功'.green);
        var hostsFile = path.resolve('hosts');
        if(fs.existsSync(hostsFile) === false){
            fs.writeFileSync(hostsFile, '');
            console.log('hosts'.bold+'文件已创建'.green);
        }
    });
}

module.exports = initConfig;

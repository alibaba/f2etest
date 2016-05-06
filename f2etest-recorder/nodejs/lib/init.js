var fs = require('fs');
var path = require('path');
var inquirer = require('inquirer');

function initConfig(){
    var questions = [
        {
            'type': 'input',
            'name': 'server',
            'message': '请输入f2etest的域名或IP',
            'default': 'f2etest.xxx.com',
            'validate': function(input){
                return input !== '' && /^https?:\/\//.test(input) === false;
            }
        },
        {
            'type': 'input',
            'name': 'userid',
            'message': '请输入f2etest userid',
            'validate': function(input){
                return input !== '';
            }
        },
        {
            'type': 'input',
            'name': 'apiKey',
            'message': '请输入f2etest apiKey',
            'validate': function(input){
                return input !== '';
            }
        },
        {
            'type': 'input',
            'name': 'browsers',
            'message': '请输入需要测试的浏览器列表',
            'default': 'Chrome, IE 11',
            'validate': function(input){
                return input !== '';
            }
        }
    ];
    inquirer.prompt(questions).then(function(anwsers){
        var configJson = {
            f2etest: anwsers,
            vars: {}
        };
        var configFile = path.resolve('config.json');
        fs.writeFileSync(configFile, JSON.stringify(configJson, null, 4));
        console.log('config.json'.bold+' writed.'.green);
        var hostsFile = path.resolve('hosts');
        fs.writeFileSync(hostsFile, '');
        console.log('hosts'.bold+' writed.'.green);
    });
}

module.exports = initConfig;

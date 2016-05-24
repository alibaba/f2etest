var initConfig = require('./lib/init.js');
var startRecorder = require('./lib/start.js');
var checkUpdate = require('./lib/update.js');

module.exports = {
    init: initConfig,
    start: startRecorder,
    checkUpdate: checkUpdate
};

var cluster = require('cluster');
var path = require('path');
var cpuCount = require('os').cpus().length;

var appPath = path.join(__dirname, 'app.js');

cluster.setupMaster({
    exec: appPath
});

cluster.on('fork', function (worker) {
    console.log('[%s] [worker:%d] #%s new worker start', new Date(), worker.process.pid, worker.id);
});

cluster.on('online', function (worker) {
    console.log('[%s] [worker:%d] #%s worker online', new Date(), worker.process.pid, worker.id);
});

cluster.on('listening', function (worker, addr) {
    console.log('[%s] [worker:%d] #%s worker listening on %s:%s:%s',
        new Date(), worker.process.pid, worker.id, addr.address, addr.port, addr.addressType);
});

cluster.on('disconnect', function (worker) {
    var w = cluster.fork();
    console.error('[%s] [master:%s] #%s wroker:%s disconnect! new #%s worker:%s fork',
        new Date(), process.pid, worker.id, worker.process.pid, w.id, w.process.pid);
});

cluster.on('exit', function (worker, code, signal) {
    var exitCode = worker.process.exitCode;
    var err = new Error(util.format('#%s worker:%s died (code: %s, signal: %s)',
        worker.id, worker.process.pid, exitCode, signal));
    err.name = 'WorkerDiedError';
    console.error(err);
});

for (var i = 0; i < cpuCount; i++) {
    cluster.fork();
}

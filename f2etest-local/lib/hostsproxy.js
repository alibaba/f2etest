var net = require('net');
var http = require('http');
var url = require('url');
var util = require('util');
var EventEmitter = require('events').EventEmitter;
var utils = require('./utils');
var extend =utils.extend;
var shExpMatch = utils.shExpMatch;

process.on('uncaughtException', function(err) {
  console.log('Caught exception: ', err);
});

var defaultConfig = {
    hosts: ''
};

var HostsProxyServer = function(){
    var self = this;
    return self._init.apply(self, arguments);
}
util.inherits(HostsProxyServer, EventEmitter);

var HostsProxyServerPrototype = HostsProxyServer.prototype;

HostsProxyServerPrototype._init = function(config){
    var self = this;
    var config = extend({}, defaultConfig, config);

    self.port = null;
    self.workPort = null;
    self.httpPort = null;

    self._portSever = null;
    self._httpServer = null;

    if(config.mode === 'forward'){
        self.setForward(config.forwardHost, config.forwardPort);
    }
    else{
        self.setHosts(config.hosts);
    }
};

// set hosts
HostsProxyServerPrototype.setHosts = function(strHosts){
    var self = this;
    self.forwardHost = null;
    self.forwardPort = null;
    var mapHosts = {};
    var arrLines = strHosts.split(/\r?\n/);
    arrLines.forEach(function(line){
        var match = line.match(/^\s*([\da-z\-\.]{3,})\s+([^#]+)/i);
        if(match){
            match[2].trim().split(/\s+/).forEach(function(domain){
                domain = domain.toLowerCase();
                if(mapHosts[domain] === undefined){
                    mapHosts[domain] = match[1];
                }
            });
        }
    });
    self._mapHosts = mapHosts;
};

// set forward proxy
HostsProxyServerPrototype.setForward = function(forwardHost, forwardPort){
    var self = this;
    self.forwardHost = forwardHost;
    self.forwardPort = forwardPort;
}

// get new hostname
HostsProxyServerPrototype._getNewHostname = function(hostname){
    var self = this;
    var mapHosts = self._mapHosts;
    for(var domain in mapHosts){
        if(shExpMatch(hostname, domain)){
            return mapHosts[domain];
        }
    }
    return hostname;
}

// start proxy server
HostsProxyServerPrototype.listen = function(port, callback){
    var self = this;
    self.port = port;

    if(callback !== undefined){
        self.on('ready', callback);
    }
    // create port proxy
    var portSever = self._portSever = net.createServer(function(client) {
        var forwardPort = self.forwardPort || self.httpPort;
        var forwardHost = self.forwardHost || '127.0.0.1';
        var forwardServer = net.connect(forwardPort, forwardHost, function() {
            forwardServer.pipe(client);
        });
        client.pipe(forwardServer);
        forwardServer.on('error', function(err){
            client.end();
            self.emit('error', err);
        })
        client.on('error', function(err){
            forwardServer.end();
            self.emit('error', err);
        });
    });
    portSever.listen(port, '0.0.0.0', function() {
        var workPort = self.workPort =portSever.address().port;
        self.emit('ready', {
            port: workPort
        });
    });
    portSever.on('error', function(err){
        self.emit('error', err);
    });
    // create http proxy
    var httpServer = self._httpServer = http.createServer(function (clientRequest, clientResponse) {
        var urlInfo = url.parse(clientRequest.url);
        var userIp = clientRequest.connection.remoteAddress ||
            clientRequest.socket.remoteAddress ||
            clientRequest.connection.socket.remoteAddress;
        clientRequest.headers['X-Forwarded-For'] = userIp;
        var reqOptions = {
            hostname: self._getNewHostname(urlInfo.hostname),
            port: urlInfo.port || 80,
            method: clientRequest.method,
            path: urlInfo.path,
            headers: clientRequest.headers,
            agent: false
        }
        var remoteServer = http.request(reqOptions, function (remoteResponse) {
            clientResponse.writeHead(remoteResponse.statusCode, remoteResponse.headers);
            remoteResponse.pipe(clientResponse);
        });
        remoteServer.on('error', function (err) {
            clientResponse.end();
            self.emit('error', err);
        });
        clientRequest.pipe(remoteServer);
    });
    // create https proxy
    httpServer.on('connect', function (httpRequest, reqSocket) {
        var urlInfo = url.parse('http://' + httpRequest.url);
        var remoteSocket = net.connect(urlInfo.port, self._getNewHostname(urlInfo.hostname), function () {
            reqSocket.write("HTTP/1.1 200 Connection established\r\n\r\n");
            remoteSocket.pipe(reqSocket).pipe(remoteSocket);
        });
        remoteSocket.on('error', function (err) {
            reqSocket.end();
            self.emit('error', err);
        });
    });
    httpServer.on('error', function(err){
        self.emit('error', err);
    });
    httpServer.listen(0, '0.0.0.0', function() {
        var httpPort = self.httpPort = httpServer.address().port;
        self.emit('httpReady', {
            port: httpPort
        });
    });
};

// close proxy
HostsProxyServerPrototype.close = function(){
    var self = this;
    var httpServer = self._httpServer;
    if(httpServer !== null){
        httpServer.close();
        self._httpServer = null;
        self.emit('close');
    }
};

var hostsproxy = {
    Server: HostsProxyServer,
    createServer: function(config){
        return new HostsProxyServer(config);
    }
};

module.exports = hostsproxy;

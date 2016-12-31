var fs = require('fs');
var cp = require('child_process');

var mapNodeOffCount = {};

console.log('Node watcher started!');
checkAllNodeAlive();

function checkAllNodeAlive(){
	var result = cp.spawnSync('query.exe', ['session']);
	var sessionList = result.stdout.toString();
	
	var mapOpened = {};
	var arrLines = sessionList.split('\n');
	arrLines.forEach(function(line){
		var match = line.match(/node(\d+)/);
		if(match){
			var nodeid = match[1];
			mapOpened[nodeid] = true;
			if(/rdp-tcp#/.test(line) === true){
				mapNodeOffCount[nodeid] = 0;
			}
			else{
				mapNodeOffCount[nodeid] = mapNodeOffCount[nodeid] ? mapNodeOffCount[nodeid] + 1 : 1;
			}
		}
	});
	
	var arrFiles = fs.readdirSync('.');
	arrFiles.forEach(function(file){
		var match = file.match(/node(\d+)/);
		var nodeid = match && match[1];
		if(nodeid && (!mapOpened[nodeid] || mapNodeOffCount[nodeid] !== undefined && mapNodeOffCount[nodeid] >= 3)){
			mapNodeOffCount[nodeid] = 0;
			reOpenNode(nodeid);
		}
	});
	setTimeout(checkAllNodeAlive, 5000);
}

function reOpenNode(nodeId){
	var nodeName = 'node' + addZero(nodeId, 2);
	var result = cp.execSync('WMIC PROCESS WHERE (name="mstsc.exe" AND CommandLine like "%'+nodeName+'.rdp%") GET processid').toString();
	var match = result.match(/ProcessId\s+(\d+)/i);
	if(match){
		cp.execSync('tskill '+match[1]);
		console.log('Old mstsc killed!');
	}
	cp.exec('start /min "" mstsc '+nodeName+'.rdp');
	console.log(nodeName+' reopen successed!');
}

function addZero(str,length){
    return new Array(length - str.length + 1).join("0") + str;              
}
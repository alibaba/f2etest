var PageJsUnit = require('pagejsunit');
process.on('message', function(config) {
    var coverageInclude = config.coverageInclude;
    var coverageExclude = config.coverageExclude;
    var timeout = config.timeout;
    var delay = config.delay;
    try{
        if(coverageInclude){
            coverageInclude = eval(coverageInclude);
            config.coverageInclude = coverageInclude;
        }
        if(coverageExclude){
            coverageExclude = eval(coverageExclude);
            config.coverageExclude = coverageExclude;
        }
        if(timeout){
            timeout = parseInt(timeout, 10);
            config.timeout = timeout;
        }
        if(delay){
            delay = parseInt(delay, 10);
            config.delay = delay;
        }
    }
    catch(e){}
    PageJsUnit.run(config, function(error, jsUnitResult){
        process.send({
            error: error,
            jsUnitResult: jsUnitResult
        });
    });
});
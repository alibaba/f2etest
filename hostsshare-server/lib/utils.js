// extend object
function extend(target) {
    for (var i = 1; i < arguments.length; i++) {
        var source = arguments[i],
            keys = Object.keys(source)

        for (var j = 0; j < keys.length; j++) {
            var name = keys[j]
            target[name] = source[name]
        }
    }

    return target
}

// check shExp match
function shExpMatch(text, exp){
    exp = exp.replace(/\.|\*|\?/g, function(c){
        return { '.': '\\.', '*': '.*?', '?': '.' }[c];
    });
    try{
        return new RegExp('^'+exp+'$').test(text);
    }
    catch(e){
        return false;
    }
}

module.exports = {
    extend: extend,
    shExpMatch: shExpMatch
};
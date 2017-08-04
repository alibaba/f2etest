(function(){
    var bgId = localStorage.getItem('bgid') || 1;
    var maxId = 4;
    var jBackground = $('#background');
    var jPrevbg = $('#prevbg');
    var jNextbg = $('#nextbg');
    jPrevbg.on('click', function(){
        if(bgId > 1){
            bgId--;
            refreshBg();
        }
    });
    jNextbg.on('click', function(){
        if(bgId < maxId){
            bgId++;
            refreshBg();
        }
    });
    function refreshBg(noAni){
        localStorage.setItem('bgid', bgId);
        var bgUrl = 'imgs/bg/'+bgId+'.jpg';
        if(!noAni){
            // preload bg
            $('<img />').attr('src', bgUrl);
            jBackground.fadeOut(1000, function(){
                jBackground.attr('src', bgUrl).fadeIn(1000);
            });
        }
        else{
            jBackground.attr('src', bgUrl);
        }
        jPrevbg.css('background-position','0px '+(bgId <= 1?'0':'-40')+'px');
        jNextbg.css('background-position','-24px '+(bgId >= maxId?'0':'-40')+'px');
    }
    refreshBg(true);
})();
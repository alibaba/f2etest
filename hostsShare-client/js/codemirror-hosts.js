CodeMirror.defineMode("hosts", function() {
	return {
		token: function(stream, state) {
			var ch = stream.peek(), match;
			if(stream.sol()){
				state.tokenID = -1;
			}
			if (ch === "#") {
				stream.skipToEnd();
				var string = stream.string;
				return /^\s*#\s*=+[^=]+=+/.test(string)?'group': (/^\s*#!/.test(string)?'disabled':'comment');
			}
			else if(stream.eatSpace()){
				return null;
			}
			else{
				state.tokenID++;
				if(match = stream.match(/[^\s#]+/)){
					if(state.tokenID === 0){
						return isIp(match[0])?'ip':'name';
					}
					return null;
				}
			}

			stream.next();
			return null;
		},
		startState:function(){
			return {tokenID:0};
		}
	};
});


//w-io的websocket第二版封装
function createMyWebSocketObj(json){
	var myWebSocket = new Object();
	myWebSocket.wiowebsocketsJson = json;
	myWebSocket.wiowebsocket = function(){
		var wiowebsocketsJson = this.wiowebsocketsJson;
		if(wiowebsocketsJson['url'] == undefined){
			console.log('url is undefined');
			return;
		}
		if(wiowebsocketsJson['path'] == undefined || wiowebsocketsJson['path'] == ''){
			console.log('open but path is empty');
			return;
		}
		var wiowebsockets = new WebSocket(wiowebsocketsJson['url']);
		this.wiowebsockets = wiowebsockets;
		wiowebsockets.onopen = function(evt) { 
			wiowebsockets.send("{'sessionId':'"+wiowebsocketsJson['sessionId']+"','path':'"+wiowebsocketsJson['path']+"','type':'open'}");
		}
		if(wiowebsocketsJson.message != undefined){
			wiowebsockets.onmessage = function(evt){
				if(evt.data == "openSuccess"){
					if(wiowebsocketsJson['data'] != undefined && wiowebsocketsJson['data']!=''){
						wiowebsockets.send("{'sessionId':'"+wiowebsocketsJson['sessionId']+"','type':'message','message':'"+wiowebsocketsJson['data']+"'}");
					}
					return;
				}
				wiowebsocketsJson.message(evt.data);//执行success的方法
			}
		}
		if(wiowebsocketsJson.close != undefined){
			wiowebsockets.onclose = function(evt) {
				wiowebsocketsJson.close(evt.data);
				wiowebsockets = undefined;
			}
		}
		return myWebSocket;
	}
	myWebSocket.wioSendMessage = function(sessionId,message){
		if(myWebSocket.wiowebsockets!=undefined){
			myWebSocket.wiowebsockets.send("{'sessionId':'"+sessionId+"','path':'','type':'message','message':'"+message+"'}");
		}else{
			console.log("wiowebsockets is undefined");
		}
	}
	myWebSocket.wioWebsocketClose = function(){
		if(myWebSocket.wiowebsockets!=undefined){
			myWebSocket.wiowebsockets.send("{'sessionId':'','path':'','type':'close','message':''}");
		}else{
			console.log("wiowebsockets is undefined");
		}
	}
	myWebSocket.wioWebsocketChange = function(sessionId,path,message){
		var wiowebsocketsJson = this.wiowebsocketsJson;
		if(wiowebsocketsJson['path'] == undefined || wiowebsocketsJson['path'] == ''){
			console.log('open but path is empty');
			return;
		}
		if(myWebSocket.wiowebsockets!=undefined){
			var changeJson = {'sessionId':'123123','path':path+'','type':'change'};
			changeJson['message'] = message==undefined || message == null ? '' : message;
			myWebSocket.wiowebsockets.send(JSON.stringify(changeJson));
		}else{
			console.log("wiowebsockets is undefined");
		}
	}
	return myWebSocket;
}
//需要在jquery的基础上
$(function(){
	
})
var wiowebsockets;
var wiowebsocketsJson;
//{'url':'','path':'','data':'','sessionId':'','message',function(){},'close':function(){}}
function wiowebsocket(json){
	wiowebsocketsJson = json;
	if(wiowebsocketsJson['url'] == undefined){
		console.log('url is undefined');
		return;
	}
	if(wiowebsocketsJson['path'] == undefined || wiowebsocketsJson['path'] == ''){
		console.log('open but path is empty');
		return;
	}
	wiowebsockets = new WebSocket(wiowebsocketsJson['url']);
	wiowebsockets.onopen = function(evt) { 
		wiowebsockets.send("{'sessionId':'"+wiowebsocketsJson['sessionId']+"','path':'"+wiowebsocketsJson['path']+"','type':'open'}");
		if(wiowebsocketsJson['data'] != undefined && wiowebsocketsJson['data']!=''){
			wiowebsockets.send("{'sessionId':'"+wiowebsocketsJson['sessionId']+"','type':'message','message':'"+wiowebsocketsJson['data']+"'}");
		}
	}
	if(wiowebsocketsJson.message != undefined){
		wiowebsockets.onmessage = function(evt){
			wiowebsocketsJson.message(evt.data);//执行success的方法
		}
	}
	if(wiowebsocketsJson.close != undefined){
		wiowebsockets.onclose = function(evt) {
			wiowebsocketsJson.close(evt.data);
			wiowebsockets = undefined;
		}
	}
}
function wioSendMessage(sessionId,message){
	if(wiowebsockets!=undefined){
		wiowebsockets.send("{'sessionId':'"+sessionId+"','path':'','type':'message','message':'"+message+"'}");
	}else{
		console.log("wiowebsockets is undefined");
	}
}
function wioWebsocketClose(){
	if(wiowebsockets!=undefined){
		wiowebsockets.send("{'sessionId':'','path':'','type':'close','message':''}");
	}else{
		console.log("wiowebsockets is undefined");
	}
}
function wioWebsocketChange(sessionId,path){
	//ws.send("{'sessionId':'123123','path':'/test2webSocket','type':'change','message':''}");
	if(wiowebsocketsJson['path'] == undefined || wiowebsocketsJson['path'] == ''){
		console.log('open but path is empty');
		return;
	}
	if(wiowebsockets!=undefined){
		wiowebsockets.send("{'sessionId':'123123','path':'"+path+"','type':'change','message':''}");
	}else{
		console.log("wiowebsockets is undefined");
	}
}
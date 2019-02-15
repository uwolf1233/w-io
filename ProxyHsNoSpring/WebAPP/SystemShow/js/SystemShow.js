function handle_http(li){
	$.ajax({
		url:'/getSystemConfigHttp',
		data:{'sessionId':'123123'},
		type:'get',
		dataType:'json',
		success:function(data){
			tabshow(1,li);
			var serverBeanList = data['serverBeanList'];
			var serverBeanListLen = serverBeanList.length;
			for(var i=0;i<serverBeanListLen;i++){
				var cd = serverBeanList[i];
				var tbody = $("#serverstable").find("tbody");
				var tr = $("<tr></tr>");
				tr.append("<td>"+cd['path']+"</td>");
				tr.append("<td>"+cd['lockNames']+"</td>");
				tr.append("<td>"+cd['secondnum']+"</td>");
				tr.append("<td>"+cd['geWait']+"</td>");
				if(cd['trans']){
					tr.append("<td>是</td>");
				}else{
					tr.append("<td>否</td>");
				}
				tr.append("<td>"+cd['tranCommitMinute']+"</td>");
				tr.append("<td>"+cd['tranRollbackMinute']+"</td>");
				tr.append("<td>"+cd['threadPoolNumMax']+"</td>");
				if(cd['fileIn']){
					tr.append("<td>是</td>");
				}else{
					tr.append("<td>否</td>");
				}
				tbody.append(tr);
			}
			var httpSetForm = $("#httpSetForm");
			fillForm_new(httpSetForm,data);
		}
	})
}
function handle_websocket(li){
	$.ajax({
		url:'/getSystemConfigWebSocket',
		data:{'sessionId':'123123'},
		type:'get',
		dataType:'json',
		success:function(data){
			//console.log(JSON.stringify(data));
			tabshow(2,li);
			var serverBeanList = data['serverBeanList'];
			var serverBeanListLen = serverBeanList.length;
			for(var i=0;i<serverBeanListLen;i++){
				var cd = serverBeanList[i];
				var tbody = $("#websockettable").find("tbody");
				var tr = $("<tr></tr>");
				tr.append("<td>"+cd['path']+"</td>");
				tr.append("<td>"+cd['lockNames']+"</td>");
				tr.append("<td>"+cd['secondnum']+"</td>");
				tr.append("<td>"+cd['geWait']+"</td>");
				if(cd['trans']){
					tr.append("<td>是</td>");
				}else{
					tr.append("<td>否</td>");
				}
				tr.append("<td>"+cd['tranCommitMinute']+"</td>");
				tr.append("<td>"+cd['tranRollbackMinute']+"</td>");
				tr.append("<td>"+cd['threadPoolNumMax']+"</td>");
				if(cd['fileIn']){
					tr.append("<td>是</td>");
				}else{
					tr.append("<td>否</td>");
				}
				tbody.append(tr);
			}
			var httpSetForm = $("#websocketSetForm");
			fillForm_new(httpSetForm,data);
		}
	})
}
function handle_server(li){
	$.ajax({
		url:'/getSystemConfigServer',
		data:{'sessionId':'123123'},
		type:'get',
		dataType:'json',
		success:function(data){
			tabshow(3,li);
			var serverBeanList = data['serverBeanList'];
			var serverBeanListLen = serverBeanList.length;
			for(var i=0;i<serverBeanListLen;i++){
				var cd = serverBeanList[i];
				var tbody = $("#inServertable").find("tbody");
				var tr = $("<tr></tr>");
				tr.append("<td>"+cd['path']+"</td>");
				tr.append("<td>"+cd['lockNames']+"</td>");
				tr.append("<td>"+cd['secondnum']+"</td>");
				tr.append("<td>"+cd['geWait']+"</td>");
				if(cd['trans']){
					tr.append("<td>是</td>");
				}else{
					tr.append("<td>否</td>");
				}
				tr.append("<td>"+cd['tranCommitMinute']+"</td>");
				tr.append("<td>"+cd['tranRollbackMinute']+"</td>");
				tr.append("<td>"+cd['threadPoolNumMax']+"</td>");
				if(cd['fileIn']){
					tr.append("<td>是</td>");
				}else{
					tr.append("<td>否</td>");
				}
				tbody.append(tr);
			}
			var httpSetForm = $("#inServerSetForm");
			fillForm_new(httpSetForm,data);
		}
	})
}
function handle_log(li){
	
}
function tabshow(type,li){
	var title_ul_id = $("#title_ul_id");
	title_ul_id.find(".uk-active").removeClass("uk-active");
	li.attr('class','uk-active');
	if(type==1){
		$("#httpContent").show();
		$("#webSocketContent").hide();
		$("#inServerContent").hide();
	}else if(type==2){
		$("#httpContent").hide();
		$("#webSocketContent").show();
		$("#inServerContent").hide();
	}else if(type==3){
		$("#httpContent").hide();
		$("#webSocketContent").hide();
		$("#inServerContent").show();
	}
}
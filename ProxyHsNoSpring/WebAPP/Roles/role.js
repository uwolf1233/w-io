function showRoleLeft(){
	$("#roleMgLeft").show();
	$("#systemRole").click(function(){
		getPermis();
	})
}
function getPermis(){
	$.ajax({
		url:'/permi_getAllPermi',
		data:{'sessionId':'123123'},
		type:'get',
		dataType:'json',
		success:function(data){
			roleTabShow(data);
		}
	})
}
function roleTabShow(data){
	var roleContent = $("<div class='layui-tab-item layui-show' style='margin-left:30px;margin-top:5px;'></div>");
	addTab($("#layuiBodyTab"),$("#layuiBodyTabcontent"),"role","权限管理",roleContent);
	var deptsmd = $("<div class='layui-col-xs6 layui-col-sm6 layui-col-md3'></div>");
	var usersmd = $("<div class='layui-col-xs6 layui-col-sm6 layui-col-md3'></div>");
	var rolesmd = $("<div class='layui-col-xs6 layui-col-sm6 layui-col-md3'></div>");
	var permismd = $("<div class='layui-col-xs6 layui-col-sm6 layui-col-md3'></div>");
	var datasmd = $("<div class='layui-col-xs12 layui-col-sm12 layui-col-md12'></div>");
	roleContent.append(deptsmd);
	roleContent.append(usersmd);
	roleContent.append(rolesmd);
	roleContent.append(permismd);
	roleContent.append(datasmd);
	permismd.append("<div class='left'><div class='layui-col-xs12 layui-col-sm12 layui-col-md12 searchinputdiv'>" +
			"<input type='text' id='permirolesearchname' />" +
			"<img src='/imgs/search.ico' class=''></img></div></div><div rolebtgroup='permismd' class='rolebtnp layui-col-xs12 layui-col-sm12 layui-col-md12'>" +
			"<button class='layui-btn layui-btn-xs layui-btn-primary'>反向授权</button>" +
			"<button class='layui-btn layui-btn-xs layui-btn-primary'>批量删除</button></div>" +
			"<div class='layui-col-xs12 layui-col-sm12 layui-col-md12'><ul id='permistree' class='ztree'></ul></div>");
	
	rolesmd.append("<div class='left'><div class='layui-col-xs12 layui-col-sm12 layui-col-md12 searchinputdiv'>" +
			"<input type='text' id='rolerolesearchname' /> " +
			"<img src='/imgs/search.ico' class=''></img></div></div><div rolebtgroup='rolesmd' class='rolebtnp layui-col-xs12 layui-col-sm12 layui-col-md12'>" +
			"<button class='layui-btn layui-btn-xs layui-btn-primary'>授权</button>" +
			"<button class='layui-btn layui-btn-xs layui-btn-primary'>反向授权</button>" +
			"<button class='layui-btn layui-btn-xs layui-btn-primary'>批量删除</button></div>" +
			"<div class='layui-col-xs12 layui-col-sm12 layui-col-md12'><ul id='rolestree' class='ztree'></ul></div>");
	
	usersmd.append("<div class='left'><div class='layui-col-xs12 layui-col-sm12 layui-col-md12 searchinputdiv'>" +
			"<input type='text' id='userrolesearchname' /> " +
			"<img src='/imgs/search.ico' class=''></img></div></div><div rolebtgroup='usersmd' class='rolebtnp layui-col-xs12 layui-col-sm12 layui-col-md12'>" +
			"<button class='layui-btn layui-btn-xs layui-btn-primary'>授权</button>" +
			"<button class='layui-btn layui-btn-xs layui-btn-primary'>反向授权</button>" +
			"<button class='layui-btn layui-btn-xs layui-btn-primary'>批量删除</button></div>" +
			"<div class='layui-col-xs12 layui-col-sm12 layui-col-md12'><ul id='userstree' class='ztree'></ul></div>" +
			"");
	
	deptsmd.append("<div class='left'><div class='layui-col-xs12 layui-col-sm12 layui-col-md12 searchinputdiv'>" +
			"<input type='text' id='deptrolesearchname' /> " +
			"<img src='/imgs/search.ico' class=''></img></div></div><div rolebtgroup='deptsmd' class='rolebtnp layui-col-xs12 layui-col-sm12 layui-col-md12' >" +
			"<button class='layui-btn layui-btn-xs layui-btn-primary'>授权</button>" +
			"<button class='layui-btn layui-btn-xs layui-btn-primary'>批量删除</button></div>" +
			"<div class='layui-col-xs12 layui-col-sm12 layui-col-md12' style='margin-top:5px;'><ul id='deptstree' class='ztree'></ul></div>");
	
	$.getScript('/Roles/deptRole.js',function(){
		deptTreeShow(data['deptDatas']);
	})
	$.getScript('/Roles/userRole.js',function(){
		userTreeShow(data['userDatas']);
	})
	$.getScript('/Roles/roleRole.js',function(){
		roleTreeShow(data['roleDatas']);
	})
	$.getScript('/Roles/permiRole.js',function(){
		permiTreeShow(data['permiDatas']);
	})
}
function permiTreeShow(data){
	var setting = {
			check: {
				enable: true
			},
			data: {
				simpleData: {
					enable: true
				}
			}
		};

	var zNodes = [];
	var dataLen = data.length;
	for(var i=0;i<dataLen;i++){
		zNodes[i] = {'id':data[i]['id'],'pId':data[i]['pid'],'name':data[i]['name']};
	}
	$.fn.zTree.init($("#permistree"), setting, zNodes);
}
function roleTreeShow(data){
	var setting = {
			check: {
				enable: true
			},
			data: {
				simpleData: {
					enable: true
				}
			}
		};

	var zNodes = [];
	var dataLen = data.length;
	for(var i=0;i<dataLen;i++){
		zNodes[i] = {'id':data[i]['id'],'pId':data[i]['pid'],'name':data[i]['name']};
	}
	$.fn.zTree.init($("#rolestree"), setting, zNodes);
}














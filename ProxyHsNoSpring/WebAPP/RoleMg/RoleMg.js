function getUsersRole(){
	$("#roleMgLeft").show();
	$("#systemRole").click(function(){
		getUserRoleData();
	})
}
function getUserRoleData(){
	$.ajax({
		url:'/role_getRoles',
		data:{'sessionId':'123123'},
		type:'get',
		dataType:'json',
		success:function(data){
			console.log("1---"+JSON.stringify(data));
			roleTabShow(data);
		}
	})
}
function roleTabShow(data){
	var roleContent = $("<div class='layui-tab-item layui-show'></div>");
	addTab($("#layuiBodyTab"),$("#layuiBodyTabcontent"),"role","权限管理",roleContent);
	var usersmd = $("<div class='layui-col-xs6 layui-col-sm6 layui-col-md4'></div>");
	var rolesmd = $("<div class='layui-col-xs6 layui-col-sm6 layui-col-md4'></div>");
	var permismd = $("<div class='layui-col-xs6 layui-col-sm6 layui-col-md4'></div>");
	var datasmd = $("<div class='layui-col-xs12 layui-col-sm12 layui-col-md12'></div>");
	roleContent.append(usersmd);
	roleContent.append(rolesmd);
	roleContent.append(permismd);
	roleContent.append(datasmd);
	usersmd.append("<div class='zTreeDemoBackground left'><ul id='userstree' class='ztree'></ul></div>");
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

	var zNodes = [{'id':'1','pId':'0','name':'用户'}];
	var usersJson = data['users'];
	var usersJsonLen = usersJson.length;
	for(var i=0;i<usersJsonLen;i++){
		zNodes[i+1] = {'id':usersJson[i]['id'],'pId':'1','name':usersJson[i]['name']};
	}
	$.fn.zTree.init($("#userstree"), setting, zNodes);

}
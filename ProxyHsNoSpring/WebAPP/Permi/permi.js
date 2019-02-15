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
	permismd.append("<div class='zTreeDemoBackground left'><ul id='permistree' class='ztree'></ul></div>");
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
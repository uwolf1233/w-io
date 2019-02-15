var userRolesHandleType;
var systemUserRoleData;
function userTreeShow(data){
	systemUserRoleData = data;
	var bodydivs = $("#bodydivs");
	var rightmouse = $("<div id='userrMenu' class='rMenu'><ul><li data-method='setTop' id='addUserWindow'>增加节点</li>" +
			"<li data-method='setTop' id='updateUserWindow'>修改节点</li><li id='deleteOneUser'>删除节点</li></ul></div>");
	bodydivs.append(rightmouse);
	addUserWindow();
	
	var setting = {
			check: {
				enable: true
			},
			data: {
				simpleData: {
					enable: true
				}
			},
			callback: {
				onRightClick: userRoleTreeRightClick,
				onClick:userroleTreeOnClick
			}
		};

	var zNodes = [];
	var dataLen = data.length;
	for(var i=0;i<dataLen;i++){
		zNodes[i] = {'id':data[i]['id'],'pId':data[i]['pid'],'name':data[i]['name'],'isend':data[i]['isend']};
	}
	$.fn.zTree.init($("#userstree"), setting, zNodes);
	var userrolesearchname = $("#userrolesearchname");
	userrolesearchname.on('input',function(){
		wiosearch({
			'url':'/search',//url
			'data':{},//参数
			'outinput':[userrolesearchname],//回绑的对象
			'title':['name'],//表头对应的key
			'titlename':['用户名称'],//表头的名称
			'inputobj':userrolesearchname,//输入的对象
		    'pobj':$("#bodydivs"),//上层对象
		    'width':'200',
		    'height':'200',
		    'callback':function(){
		    	searchUserRole();
		    }
		},systemUserRoleData);
	});
	function userroleTreeOnClick(event, treeId, treeNode, clickFlag) {
		$.ajax({
      		url:'/user_userGetRoles',
      		data:{'userid':treeNode.id,'sessionId':123123},
      		type:'post',
      		dataType:'json',
      		success:function(data){
      			var roleZTree = $.fn.zTree.getZTreeObj("rolestree");
      			var checkedNodes = roleZTree.getCheckedNodes(true);
      			var checkedNodesLen = checkedNodes.length;
      			for(var i=0;i<checkedNodesLen;i++){
      				roleZTree.checkNode(checkedNodes[i],false,true);
      			}
      			var dataLen = data.length;
      			for(var j=0;j<dataLen;j++){
  					var roleid = data[j]['roleid'];
  					var node = roleZTree.getNodeByParam('id',roleid,null);
  					if(node != null){
  						node.checked = true;
  						roleZTree.updateNode(node,true);
  						roleZTree.selectNode(node);
  						roleZTree.cancelSelectedNode(node);
  					}
  				}
      		}
      	})
	}
}
function userRoleTreeRightClick(event, treeId, treeNode){
	var zTree = $.fn.zTree.getZTreeObj("userstree");
	if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
		zTree.cancelSelectedNode();
		usershowRMenu("root", event.clientX, event.clientY);
	} else if (treeNode && !treeNode.noR) {
		zTree.selectNode(treeNode);
		usershowRMenu("node", event.clientX, event.clientY);
	}
}
function usershowRMenu(type, x, y) {
	$("#userrMenu ul").show();
	if (type=="root") {
		$("#m_del").hide();
		$("#m_check").hide();
		$("#m_unCheck").hide();
	} else {
		$("#m_del").show();
		$("#m_check").show();
		$("#m_unCheck").show();
	}

    y += document.body.scrollTop;
    x += document.body.scrollLeft;
    $("#userrMenu").css({"top":y+"px", "left":x+"px", "visibility":"visible"});

	$("body").bind("mousedown", useronBodyMouseDown);
}
function useronBodyMouseDown(event){
	if (!(event.target.id == "userrMenu" || $(event.target).parents("#userrMenu").length>0)) {
		$("#userrMenu").css({"visibility" : "hidden"});
	}
}
function searchUserRole(){
	var val = $("#userrolesearchname").val();
	var zTree = $.fn.zTree.getZTreeObj("userstree");
	var nodes = zTree.getNodesByParamFuzzy('name',val,null);
	if(nodes.length > 0){
		zTree.selectNode(nodes[0]);
	}
}
function addUserWindow(){
	//触发事件
    $('#addUserWindow').on('click', function(){
    	$("#userrMenu").css({"visibility" : "hidden"});
    	userRolesHandleType = 'add';
        var othis = $(this), method = othis.data('method');
        userTreeEditactive[method] ? userTreeEditactive[method].call(this, othis) : '';
        layui.use('form', function(){
        	  var form = layui.form; //只有执行了这一步，部分表单元素才会自动修饰成功
        	  form.render();
        }); 
    });
    $('#updateUserWindow').on('click', function(){
    	$("#userrMenu").css({"visibility" : "hidden"});
    	userRolesHandleType = 'update';
    	var othis = $(this), method = othis.data('method');
        userTreeEditactive[method] ? userTreeEditactive[method].call(this, othis) : '';
        layui.use('form', function(){
        	  var form = layui.form; //只有执行了这一步，部分表单元素才会自动修饰成功
        	  form.render();
        });
    });
    $("#deleteOneUser").on('click',function(){
    	$("#userrMenu").css({"visibility" : "hidden"});
    	deleteOneUserRoleData();
    });
    //按钮组
    $("[rolebtgroup='usersmd']").find("button").eq(1).click(function(){//用户对部门授权
    	var deptZTree = $.fn.zTree.getZTreeObj("deptstree");
    	var deptSelects = deptZTree.getCheckedNodes(true);
    	var deptSelectsLen = deptSelects.length;
    	var deptZTreeSelectJson = [];
    	for(var i=0;i<deptSelectsLen;i++){
    		var cnode = deptSelects[i];
    		if(cnode.isend == '1'){
	    		var id = cnode.id;
	    		deptZTreeSelectJson[deptZTreeSelectJson.length] = id;
    		}
    	}
    	var userZTree = $.fn.zTree.getZTreeObj("userstree");
    	var userSelects = userZTree.getCheckedNodes(true);
    	var userSelectsLen = userSelects.length;
    	var userSelectsSelectJson = [];
    	for(var i=0;i<userSelectsLen;i++){
    		var cnode = userSelects[i];
    		if(cnode.isend == '1'){
	    		var cnode = userSelects[i];
	    		var id = cnode.id;
	    		userSelectsSelectJson[userSelectsSelectJson.length] = id;
    		}
    	}
    	var jsons = {};
    	if(userSelectsSelectJson.length>1){
    		layer.open({
    			content:"抱歉，当前按钮只允许单个用户对多个部门进行授权"
    		})
    		return;
    	}
    	var deptZTreeSelectJsonLen = deptZTreeSelectJson.length;
    	var deptZTreeSelects = '';
    	for(var i=0;i<deptZTreeSelectJsonLen;i++){
    		deptZTreeSelects += deptZTreeSelects == '' ? deptZTreeSelectJson[i] : (","+deptZTreeSelectJson[i]);
    	}
    	jsons['userid'] = userSelectsSelectJson[0];
    	jsons['deptids'] = deptZTreeSelects;
    	jsons['sessionId'] = '123123';
    	$.ajax({
    		url:'/user_userSetDept',
    		type:'post',
    		data:jsons,
    		dataType:'json',
    		beforeSend:function (R) {
    			R.setRequestHeader('Connection', 'Keep-Alive');//复用连接
    		},
    		success:function(data){  
    			if(data['type'] == '1'){
    				layer.msg("操作成功");
    			}else{
    				layer.msg("操作失败");
    			}
    		}
    	})
    });
	$("[rolebtgroup='usersmd']").find("button").eq(0).click(function(){//用户对角色授权
    	var roleZTree = $.fn.zTree.getZTreeObj("rolestree");
    	var roleSelects = roleZTree.getCheckedNodes(true);
    	var roleSelectsLen = roleSelects.length;
    	var roleZTreeSelectJson = [];
    	for(var i=0;i<roleSelectsLen;i++){
    		var cnode = roleSelects[i];
    		if(cnode.isend == '1'){
	    		var id = cnode.id;
	    		roleZTreeSelectJson[roleZTreeSelectJson.length] = id;
    		}
    	}
    	var userZTree = $.fn.zTree.getZTreeObj("userstree");
    	var userSelects = userZTree.getCheckedNodes(true);
    	var userSelectsLen = userSelects.length;
    	var userSelectsSelectJson = [];
    	for(var i=0;i<userSelectsLen;i++){
    		var cnode = userSelects[i];
    		if(cnode.isend == '1'){
	    		var cnode = userSelects[i];
	    		var id = cnode.id;
	    		userSelectsSelectJson[userSelectsSelectJson.length] = id;
    		}
    	}
    	var jsons = {};
    	if(userSelectsSelectJson.length>1){
    		layer.open({
    			content:"抱歉，当前按钮只允许单个用户对多个角色进行授权"
    		})
    		return;
    	}
    	var roleZTreeSelectJsonLen = roleZTreeSelectJson.length;
    	var roleZTreeSelects = '';
    	for(var i=0;i<roleZTreeSelectJsonLen;i++){
    		roleZTreeSelects += roleZTreeSelects == '' ? roleZTreeSelectJson[i] : (","+roleZTreeSelectJson[i]);
    	}
    	jsons['userid'] = userSelectsSelectJson[0];
    	jsons['roleids'] = roleZTreeSelects;
    	jsons['sessionId'] = '123123';
    	$.ajax({
    		url:'/user_userSetRole',
    		type:'post',
    		data:jsons,
    		dataType:'json',
    		beforeSend:function (R) {
    			R.setRequestHeader('Connection', 'Keep-Alive');//复用连接
    		},
    		success:function(data){  
    			if(data['type'] == '1'){
    				layer.msg("操作成功");
    			}else{
    				layer.msg("操作失败");
    			}
    		}
    	})
    })
}
var userTreeEditactive = {
	    setTop: function(){
	      var arrays  = new Array();
	      var str = showEditUserForm(arrays);
	      var that = this; 
	      //多窗口模式，层叠置顶
	      var index = layer.open({
	        type: 1 
	        ,title: '添加用户'
	        ,area: ['580px', '560px']
	        ,shade: 0
	        ,maxmin: true
	        ,offset: [ 
	          Math.random()*($(window).height()-300)
	          ,Math.random()*($(window).width()-390)
	        ] 
	        ,content: str
	        ,btn: ['保存', '全部关闭'] //只是为了演示
	        ,yes: function(){
	        	var addUserJson = $("#"+arrays[0]).serializeObject();
	        	var pid = $("#"+arrays[0]).attr("pId");
	        	addUserJson['pid'] = pid;
	        	addUserJson['sessionId'] = '123123';
	        	addUserJson['systemname'] = 'system';
	        	var userRolesHandleType = $("#"+arrays[0]).attr("userRolesHandleType");
	        	//树节点操作
	        	var handleTreeFun;
	        	var zTree = $.fn.zTree.getZTreeObj("userstree");
	  			var selectedNodes = zTree.getSelectedNodes();
	        	if(userRolesHandleType == 'add'){
	        		var urls = '/user_addUser';
	        		handleTreeFun = function(newNode){
	        			zTree.addNodes(selectedNodes[0], newNode);
	        			systemUserRoleData[systemUserRoleData.length] = newNode;
	        		}
	        	}else if(userRolesHandleType == 'update'){
	        		var urls = '/user_updateUser';
	        		addUserJson['id'] = addUserJson['pid'];
	        		handleTreeFun = function(newNode){
	        			selectedNodes[0].name = newNode.name;
	        			zTree.updateNode(selectedNodes[0]);
	        			var systemUserRoleDataLength = systemUserRoleData.length;
	        			for(var i=0;i<systemUserRoleDataLength;i++){
	        				if(systemUserRoleData[i].id == newNode.id){
	        					systemUserRoleData[i] = newNode;
	        					break;
	        				}
	        			}
	        		}
	        	}
	        	$.ajax({
		      		url:urls,
		      		data:addUserJson,
		      		type:'post',
		      		dataType:'json',
		      		success:function(data){
		      			var newNode = data['retBean'][0];
		      			newNode['pId'] = pid;
		      			handleTreeFun(newNode);//修改或者新增
		      			var msg;
		      			if(data['type'] == 'success'){
		      				msg = '操作成功';
		      				layer.close(index);
		      			}else{
		      				msg = '操作失败';
		      			}
		      			layer.open({
		      			   content: msg
		      			});
		      		}
		      	})
	        }
	        ,btn2: function(){
	          layer.closeAll();
	        }
	        ,zIndex: layer.zIndex //重点1
	        ,success: function(layero){
		          layer.setTop(layero); //重点2
		          var forms = $("#"+arrays[0]);
	      			
	      			if($("#"+arrays[0]).attr("userRolesHandleType") == 'update'){
	      				$.ajax({
	      		      		url:'/user_getOneUserRole',
	      		      		data:{'sessionId':'123123','id':$("#"+arrays[0]).attr("pId")},
	      		      		type:'get',
	      		      		dataType:'json',
	      		      		success:function(data){
	      		      			var datas = data['data'][0];
	      		      			fillForm(forms,datas);
		      		      		layui.use('form', function(){
		  		  	          	  var form = layui.form; //只有执行了这一步，部分表单元素才会自动修饰成功
		  		  	          	  form.render();
		  		  	    		}); 
	      		      		}
	      		      	})
	      			}else{
		      			layui.use('form', function(){
		  	          	  var form = layui.form; //只有执行了这一步，部分表单元素才会自动修饰成功
		  	          	  form.render();
		  	    		}); 
	      			}
	        }
	      });
	    }
	  }
function showEditUserForm(array){
	var id = uuid(32,10);
	array[0] = id;
	var zTree = $.fn.zTree.getZTreeObj("userstree");
	var selectedNodes = zTree.getSelectedNodes();
	return "<form class='layui-form' style='margin-top:10px;' id='"+id+"' pId='"+selectedNodes[0].id+"' " +
			"userRolesHandleType='"+userRolesHandleType+"'>" +
				"<div class='layui-form-item'>" +
					"<label class='layui-form-label'>人员名称</label>" +
					"<div class='layui-input-block'>" +
						"<input type='text' name='name' lay-verify='title' " +
							"autocomplete='off' placeholder='请输入人员名称' class='layui-input'>" +
					"</div>" +
				"</div>" +
				"<div class='layui-form-item' style='display:none;'>" +
					"<div class='layui-input-block'>" +
						"<input type='text' name='pid' lay-verify='title' class='layui-input'>" +
					"</div>" +
				"</div>" +
				"<div class='layui-form-item'>" +
					"<label class='layui-form-label'>是否末级</label>" +
					"<div class='layui-input-block'>" +
						"<select name='isend' lay-filter=''>" +
							"<option value='0'>否</option>" +
							"<option value='1'>是</option>" +
						"</select>" +
					"</div>" +
				"</div>" +
			"</form>";
}
function deleteOneUserRoleData(){
	var zTree = $.fn.zTree.getZTreeObj("userstree");
	var selectedNodes = zTree.getSelectedNodes();
	var id = selectedNodes[0].id;
	$.ajax({
  		url:"/user_deleteDatas",
  		data:{'id':id,'sessionId':'123123'},
  		type:'post',
  		dataType:'json',
  		success:function(data){
  			if(data['type'] == 'success'){
  				zTree.removeNode(selectedNodes[0]);
  				var systemUserRoleDataLength = systemUserRoleData.length;
  				for(var i=0;i<systemUserRoleDataLength;i++){
    				if(systemUserRoleData[i].id == id){
    					systemUserRoleData.splice(i,1);
    					break;
    				}
    			}
  			}
  		}
	})
}

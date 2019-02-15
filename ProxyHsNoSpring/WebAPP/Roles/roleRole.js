var roleRolesHandleType;
var systemRoleRoleData;
function roleTreeShow(data){
	systemRoleRoleData = data;
	var bodydivs = $("#bodydivs");
	var rightmouse = $("<div id='rolerMenu' class='rMenu'><ul><li data-method='setTop' id='addRoleWindow'>增加节点</li>" +
			"<li data-method='setTop' id='updateRoleWindow'>修改节点</li><li id='deleteOneRole'>删除节点</li></ul></div>");
	bodydivs.append(rightmouse);
	addRoleWindow();
	
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
				onRightClick: roleRoleTreeRightClick,
				onClick:rolepermiTreeOnClick
			}
		};

	var zNodes = [];
	var dataLen = data.length;
	for(var i=0;i<dataLen;i++){
		zNodes[i] = {'id':data[i]['id'],'pId':data[i]['pid'],'name':data[i]['name'],'isend':data[i]['isend']};
	}
	$.fn.zTree.init($("#rolestree"), setting, zNodes);
	var rolerolesearchname = $("#rolerolesearchname");
	rolerolesearchname.on('input',function(){
		wiosearch({
			'url':'/search',//url
			'data':{},//参数
			'outinput':[rolerolesearchname],//回绑的对象
			'title':['name'],//表头对应的key
			'titlename':['角色名称'],//表头的名称
			'inputobj':rolerolesearchname,//输入的对象
		    'pobj':$("#bodydivs"),//上层对象
		    'width':'200',
		    'height':'200',
		    'callback':function(){
		    	searchRoleRole();
		    }
		},systemRoleRoleData);
	});
	function rolepermiTreeOnClick(event, treeId, treeNode, clickFlag) {
		$.ajax({
      		url:'/role_roleGetPermis',
      		data:{'roleid':treeNode.id,'sessionId':123123},
      		type:'post',
      		dataType:'json',
      		success:function(data){
      			var permiZTree = $.fn.zTree.getZTreeObj("permistree");
      			var checkedNodes = permiZTree.getCheckedNodes(true);
      			var checkedNodesLen = checkedNodes.length;
      			for(var i=0;i<checkedNodesLen;i++){
      				permiZTree.checkNode(checkedNodes[i],false,true);
      			}
      			var dataLen = data.length;
      			for(var j=0;j<dataLen;j++){
  					var permiid = data[j]['permiid'];
  					var node = permiZTree.getNodeByParam('id',permiid,null);
  					if(node != null){
  						node.checked = true;
  						permiZTree.updateNode(node,true);
  						permiZTree.selectNode(node);
  						permiZTree.cancelSelectedNode(node);
  					}
  				}
      		}
      	})
	}
}
function roleRoleTreeRightClick(event, treeId, treeNode){
	var zTree = $.fn.zTree.getZTreeObj("rolestree");
	if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
		zTree.cancelSelectedNode();
		roleshowRMenu("root", event.clientX, event.clientY);
	} else if (treeNode && !treeNode.noR) {
		zTree.selectNode(treeNode);
		roleshowRMenu("node", event.clientX, event.clientY);
	}
}
function roleshowRMenu(type, x, y) {
	$("#rolerMenu ul").show();
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
    $("#rolerMenu").css({"top":y+"px", "left":x+"px", "visibility":"visible"});

	$("body").bind("mousedown", roleonBodyMouseDown);
}
function roleonBodyMouseDown(event){
	if (!(event.target.id == "rolerMenu" || $(event.target).parents("#rolerMenu").length>0)) {
		$("#rolerMenu").css({"visibility" : "hidden"});
	}
}
function searchRoleRole(){//查询部门
	var val = $("#rolerolesearchname").val();
	var zTree = $.fn.zTree.getZTreeObj("rolestree");
	var nodes = zTree.getNodesByParamFuzzy('name',val,null);
	if(nodes.length > 0){
		zTree.selectNode(nodes[0]);
	}
}
function addRoleWindow(){
	//触发事件
    $('#addRoleWindow').on('click', function(){
    	$("#rolerMenu").css({"visibility" : "hidden"});
    	roleRolesHandleType = 'add';
        var othis = $(this), method = othis.data('method');
        roleTreeEditactive[method] ? roleTreeEditactive[method].call(this, othis) : '';
        layui.use('form', function(){
        	  var form = layui.form; //只有执行了这一步，部分表单元素才会自动修饰成功
        	  form.render();
        }); 
    });
    $('#updateRoleWindow').on('click', function(){
    	$("#rolerMenu").css({"visibility" : "hidden"});
    	roleRolesHandleType = 'update';
    	var othis = $(this), method = othis.data('method');
        roleTreeEditactive[method] ? roleTreeEditactive[method].call(this, othis) : '';
        layui.use('form', function(){
        	  var form = layui.form; //只有执行了这一步，部分表单元素才会自动修饰成功
        	  form.render();
        });
    });
    $("#deleteOneRole").on('click',function(){
    	$("#rolerMenu").css({"visibility" : "hidden"});
    	deleteOneRoleRoleData();
    });
	$("[rolebtgroup='rolesmd']").find("button").eq(1).click(function(){//角色对用户授权
    	var userZTree = $.fn.zTree.getZTreeObj("userstree");
    	var userSelects = userZTree.getCheckedNodes(true);
    	var userSelectsLen = userSelects.length;
    	var userZTreeSelectJson = [];
    	for(var i=0;i<userSelectsLen;i++){
    		var cnode = userSelects[i];
    		if(cnode.isend == '1'){
	    		var id = cnode.id;
	    		userZTreeSelectJson[userZTreeSelectJson.length] = id;
    		}
    	}
    	var roleZTree = $.fn.zTree.getZTreeObj("rolestree");
    	var roleSelects = roleZTree.getCheckedNodes(true);
    	var roleSelectsLen = roleSelects.length;
    	var roleSelectsSelectJson = [];
    	for(var i=0;i<roleSelectsLen;i++){
    		var cnode = roleSelects[i];
    		if(cnode.isend == '1'){
	    		var cnode = roleSelects[i];
	    		var id = cnode.id;
	    		roleSelectsSelectJson[roleSelectsSelectJson.length] = id;
    		}
    	}
    	var jsons = {};
    	if(roleSelectsSelectJson.length>1){
    		layer.open({
    			content:"抱歉，当前按钮只允许单个角色对多个用户进行授权"
    		})
    		return;
    	}
		if(roleSelectsSelectJson.length == 0){
			layer.open({
    			content:"抱歉，请选择一个角色"
    		})
    		return;
		}
    	var userZTreeSelectJsonLen = userZTreeSelectJson.length;
    	var userZTreeSelects = '';
    	for(var i=0;i<userZTreeSelectJsonLen;i++){
    		userZTreeSelects += userZTreeSelects == '' ? userZTreeSelectJson[i] : (","+userZTreeSelectJson[i]);
    	}
    	jsons['roleid'] = roleSelectsSelectJson[0];
    	jsons['userids'] = userZTreeSelects;
    	jsons['sessionId'] = '123123';
    	$.ajax({
    		url:'/role_roleSetUser',
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
	$("[rolebtgroup='rolesmd']").find("button").eq(0).click(function(){//角色对权限授权
    	var permiZTree = $.fn.zTree.getZTreeObj("permistree");
    	var permiSelects = permiZTree.getCheckedNodes(true);
    	var permiSelectsLen = permiSelects.length;
    	var permiZTreeSelectJson = [];
    	for(var i=0;i<permiSelectsLen;i++){
    		var cnode = permiSelects[i];
    		if(cnode.isend == '1'){
	    		var id = cnode.id;
	    		permiZTreeSelectJson[permiZTreeSelectJson.length] = id;
    		}
    	}
    	var roleZTree = $.fn.zTree.getZTreeObj("rolestree");
    	var roleSelects = roleZTree.getCheckedNodes(true);
    	var roleSelectsLen = roleSelects.length;
    	var roleSelectsSelectJson = [];
    	for(var i=0;i<roleSelectsLen;i++){
    		var cnode = roleSelects[i];
    		if(cnode.isend == '1'){
	    		var cnode = roleSelects[i];
	    		var id = cnode.id;
	    		roleSelectsSelectJson[roleSelectsSelectJson.length] = id;
    		}
    	}
    	var jsons = {};
    	if(roleSelectsSelectJson.length>1){
    		layer.open({
    			content:"抱歉，当前按钮只允许单个角色对多个权限进行授权"
    		})
    		return;
    	}
		if(permiZTreeSelectJson.length == 0){
			layer.open({
    			content:"抱歉，请至少选择一个权限节点"
    		})
    		return;
		}
    	var permiZTreeSelectJsonLen = permiZTreeSelectJson.length;
    	var permiZTreeSelects = '';
    	for(var i=0;i<permiZTreeSelectJsonLen;i++){
    		permiZTreeSelects += permiZTreeSelects == '' ? permiZTreeSelectJson[i] : (","+permiZTreeSelectJson[i]);
    	}
    	jsons['roleid'] = roleSelectsSelectJson[0];
    	jsons['permiids'] = permiZTreeSelects;
    	jsons['sessionId'] = '123123';
    	$.ajax({
    		url:'/role_roleSetPermi',
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
var roleTreeEditactive = {
    setTop: function(){
      var arrays  = new Array();
      var str = showEditRoleForm(arrays);
      var that = this; 
      //多窗口模式，层叠置顶
      var index = layer.open({
        type: 1 
        ,title: '添加角色'
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
        	var addRoleJson = $("#"+arrays[0]).serializeObject();
        	var pid = $("#"+arrays[0]).attr("pId");
        	addRoleJson['pid'] = pid;
        	addRoleJson['sessionId'] = '123123';
        	addRoleJson['systemname'] = 'system';
        	var roleRolesHandleType = $("#"+arrays[0]).attr("roleRolesHandleType");
        	//树节点操作
        	var handleTreeFun;
        	var zTree = $.fn.zTree.getZTreeObj("rolestree");
  			var selectedNodes = zTree.getSelectedNodes();
        	if(roleRolesHandleType == 'add'){
        		var urls = '/role_addRole';
        		handleTreeFun = function(newNode){
        			zTree.addNodes(selectedNodes[0], newNode);
        			systemRoleRoleData[systemRoleRoleData.length] = newNode;
        		}
        	}else if(roleRolesHandleType == 'update'){
        		var urls = '/role_updateRole';
        		addRoleJson['id'] = addRoleJson['pid'];
        		handleTreeFun = function(newNode){
        			selectedNodes[0].name = newNode.name;
        			zTree.updateNode(selectedNodes[0]);
        			var systemRoleRoleDataLength = systemRoleRoleData.length;
        			for(var i=0;i<systemRoleRoleDataLength;i++){
        				if(systemRoleRoleData[i].id == newNode.id){
        					systemRoleRoleData[i] = newNode;
        					break;
        				}
        			}
        		}
        	}
        	$.ajax({
	      		url:urls,
	      		data:addRoleJson,
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
    			if($("#"+arrays[0]).attr("roleRolesHandleType") == 'update'){
    				$.ajax({
    		      		url:'/role_getOneRoleRole',
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
function showEditRoleForm(array){//改到这里
	var id = uuid(32,10);
	array[0] = id;
	var zTree = $.fn.zTree.getZTreeObj("rolestree");
	var selectedNodes = zTree.getSelectedNodes();
	return "<form class='layui-form' style='margin-top:10px;' id='"+id+"' pId='"+selectedNodes[0].id+"' " +
			"roleRolesHandleType='"+roleRolesHandleType+"'>" +
				"<div class='layui-form-item'>" +
					"<label class='layui-form-label'>角色名称</label>" +
					"<div class='layui-input-block'>" +
						"<input type='text' name='name' lay-verify='title' " +
							"autocomplete='off' placeholder='请输入角色名称' class='layui-input'>" +
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
function deleteOneRoleRoleData(){
	var zTree = $.fn.zTree.getZTreeObj("rolestree");
	var selectedNodes = zTree.getSelectedNodes();
	var id = selectedNodes[0].id;
	$.ajax({
  		url:"/role_deleteDatas",
  		data:{'id':id,'sessionId':'123123'},
  		type:'post',
  		dataType:'json',
  		success:function(data){
  			if(data['type'] == 'success'){
  				zTree.removeNode(selectedNodes[0]);
  				var systemRoleRoleDataLength = systemRoleRoleData.length;
  				for(var i=0;i<systemRoleRoleDataLength;i++){
    				if(systemRoleRoleData[i].id == id){
    					systemRoleRoleData.splice(i,1);
    					break;
    				}
    			}
  			}
  		}
	})
}
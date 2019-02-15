var permiRolesHandleType;
var systemPermiRoleData;
function permiTreeShow(data){
	systemPermiRoleData = data;
	var bodydivs = $("#bodydivs");
	var rightmouse = $("<div id='permirMenu' class='rMenu'><ul><li data-method='setTop' id='addPermiWindow'>增加节点</li>" +
			"<li data-method='setTop' id='updatePermiWindow'>修改节点</li><li id='deleteOnePermi'>删除节点</li></ul></div>");
	bodydivs.append(rightmouse);
	addPermiWindow();
	
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
				onRightClick: permipermiTreeRightClick
			}
		};

	var zNodes = [];
	var dataLen = data.length;
	for(var i=0;i<dataLen;i++){
		zNodes[i] = {'id':data[i]['id'],'pId':data[i]['pid'],'name':data[i]['name'],'isend':data[i]['isend']};
	}
	$.fn.zTree.init($("#permistree"), setting, zNodes);
	var permirolesearchname = $("#permirolesearchname");
	permirolesearchname.on('input',function(){
		wiosearch({
			'url':'/search',//url
			'data':{},//参数
			'outinput':[permirolesearchname],//回绑的对象
			'title':['name'],//表头对应的key
			'titlename':['权限'],//表头的名称
			'inputobj':permirolesearchname,//输入的对象
		    'pobj':$("#bodydivs"),//上层对象
		    'width':'200',
		    'height':'200',
		    'callback':function(){
		    	searchPermiRole();
		    }
		},systemPermiRoleData);
	})
}
function permipermiTreeRightClick(event, treeId, treeNode){
	var zTree = $.fn.zTree.getZTreeObj("permistree");
	if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
		zTree.cancelSelectedNode();
		permishowRMenu("root", event.clientX, event.clientY);
	} else if (treeNode && !treeNode.noR) {
		zTree.selectNode(treeNode);
		permishowRMenu("node", event.clientX, event.clientY);
	}
}
function permishowRMenu(type, x, y) {
	$("#permirMenu ul").show();
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
    $("#permirMenu").css({"top":y+"px", "left":x+"px", "visibility":"visible"});

	$("body").bind("mousedown", permionBodyMouseDown);
}
function permionBodyMouseDown(event){
	if (!(event.target.id == "permirMenu" || $(event.target).parents("#permirMenu").length>0)) {
		$("#permirMenu").css({"visibility" : "hidden"});
	}
}
function searchPermiRole(){//查询部门
	var val = $("#permirolesearchname").val();
	var zTree = $.fn.zTree.getZTreeObj("permistree");
	var nodes = zTree.getNodesByParamFuzzy('name',val,null);
	if(nodes.length > 0){
		zTree.selectNode(nodes[0]);
	}
}
function addPermiWindow(){
	//触发事件
    $('#addPermiWindow').on('click', function(){
    	$("#permirMenu").css({"visibility" : "hidden"});
    	permiRolesHandleType = 'add';
        var othis = $(this), method = othis.data('method');
        permiTreeEditactive[method] ? permiTreeEditactive[method].call(this, othis) : '';
        layui.use('form', function(){
        	  var form = layui.form; //只有执行了这一步，部分表单元素才会自动修饰成功
        	  form.render();
        }); 
    });
    $('#updatePermiWindow').on('click', function(){
    	$("#permirMenu").css({"visibility" : "hidden"});
    	permiRolesHandleType = 'update';
    	var othis = $(this), method = othis.data('method');
    	permiTreeEditactive[method] ? permiTreeEditactive[method].call(this, othis) : '';
        layui.use('form', function(){
        	  var form = layui.form; //只有执行了这一步，部分表单元素才会自动修饰成功
        	  form.render();
        });
    });
    $("#deleteOnePermi").on('click',function(){
    	$("#permirMenu").css({"visibility" : "hidden"});
    	deleteOnePermiRoleData();
    });
	//按钮组
	$("[rolebtgroup='permismd']").find("button").eq(0).click(function(){//权限对角色授权
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
    	var permiZTree = $.fn.zTree.getZTreeObj("permistree");
    	var permiSelects = permiZTree.getCheckedNodes(true);
    	var permiSelectsLen = permiSelects.length;
    	var permiSelectsSelectJson = [];
    	for(var i=0;i<permiSelectsLen;i++){
    		var cnode = permiSelects[i];
    		if(cnode.isend == '1'){
	    		var cnode = permiSelects[i];
	    		var id = cnode.id;
	    		permiSelectsSelectJson[permiSelectsSelectJson.length] = id;
    		}
    	}
    	var jsons = {};
    	if(permiSelectsSelectJson.length>1){
    		layer.open({
    			content:"抱歉，当前按钮只允许单个权限对多个角色进行授权"
    		})
    		return;
    	}
		if(roleZTreeSelectJson.length == 0){
			layer.open({
    			content:"抱歉，请选择一个角色"
    		})
    		return;
		}
    	var roleZTreeSelectJsonLen = roleZTreeSelectJson.length;
    	var roleZTreeSelects = '';
    	for(var i=0;i<roleZTreeSelectJsonLen;i++){
    		roleZTreeSelects += roleZTreeSelects == '' ? roleZTreeSelectJson[i] : (","+roleZTreeSelectJson[i]);
    	}
    	jsons['permiid'] = permiSelectsSelectJson[0];
    	jsons['roleids'] = roleZTreeSelects;
    	jsons['sessionId'] = '123123';
    	$.ajax({
    		url:'/permi_permiSetRole',
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
}
var permiTreeEditactive = {
    setTop: function(){
      var arrays  = new Array();
      var str = showEditPermiForm(arrays);
      var that = this; 
      //多窗口模式，层叠置顶
      var index = layer.open({
        type: 1 
        ,title: '添加权限'
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
        	var addPermiJson = $("#"+arrays[0]).serializeObject();
        	var pid = $("#"+arrays[0]).attr("pId");
        	addPermiJson['pid'] = pid;
        	addPermiJson['sessionId'] = '123123';
        	addPermiJson['systemname'] = 'system';
        	var permiRolesHandleType = $("#"+arrays[0]).attr("permiRolesHandleType");
        	//树节点操作
        	var handleTreeFun;
        	var zTree = $.fn.zTree.getZTreeObj("permistree");
  			var selectedNodes = zTree.getSelectedNodes();
        	if(permiRolesHandleType == 'add'){
        		var urls = '/permi_addPermi';
        		handleTreeFun = function(newNode){
        			zTree.addNodes(selectedNodes[0], newNode);
        			systemPermiRoleData[systemPermiRoleData.length] = newNode;
        		}
        	}else if(permiRolesHandleType == 'update'){
        		var urls = '/permi_updatePermi';
        		addPermiJson['id'] = addPermiJson['pid'];
        		handleTreeFun = function(newNode){
        			selectedNodes[0].name = newNode.name;
        			zTree.updateNode(selectedNodes[0]);
        			var systemPermiRoleDataLength = systemPermiRoleData.length;
        			for(var i=0;i<systemPermiRoleDataLength;i++){
        				if(systemPermiRoleData[i].id == newNode.id){
        					systemPermiRoleData[i] = newNode;
        					break;
        				}
        			}
        		}
        	}
        	$.ajax({
	      		url:urls,
	      		data:addPermiJson,
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
      			
      			if($("#"+arrays[0]).attr("permiRolesHandleType") == 'update'){
      				$.ajax({
      		      		url:'/permi_getOnePermiRole',
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
function showEditPermiForm(array){
	var id = uuid(32,10);
	array[0] = id;
	var zTree = $.fn.zTree.getZTreeObj("permistree");
	var selectedNodes = zTree.getSelectedNodes();
	return "<form class='layui-form' style='margin-top:10px;' id='"+id+"' pId='"+selectedNodes[0].id+"' " +
			"permiRolesHandleType='"+permiRolesHandleType+"'>" +
				"<div class='layui-form-item'>" +
					"<label class='layui-form-label'>权限名称</label>" +
					"<div class='layui-input-block'>" +
						"<input type='text' name='name' lay-verify='title' " +
							"autocomplete='off' placeholder='请输入权限名称' class='layui-input'>" +
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
function deleteOnePermiRoleData(){
	var zTree = $.fn.zTree.getZTreeObj("permistree");
	var selectedNodes = zTree.getSelectedNodes();
	var id = selectedNodes[0].id;
	$.ajax({
  		url:"/permi_deleteDatas",
  		data:{'id':id,'sessionId':'123123'},
  		type:'post',
  		dataType:'json',
  		success:function(data){
  			if(data['type'] == 'success'){
  				zTree.removeNode(selectedNodes[0]);
  				var systemPermiRoleDataLength = systemPermiRoleData.length;
  				for(var i=0;i<systemPermiRoleDataLength;i++){
    				if(systemPermiRoleData[i].id == id){
    					systemPermiRoleData.splice(i,1);
    					break;
    				}
    			}
  			}
  		}
	})
}
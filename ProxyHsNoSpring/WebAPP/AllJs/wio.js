$(function(){
	layui.use('layer', function(){ //独立版的layer无需执行这一句
		  var $ = layui.jquery, layer = layui.layer; //独立版的layer无需执行这一句
	});
})
//添加tab
function addTab(tabobj,tabcontentobj,tabv,tabName,tabContent){
	var isadd = true;
	var tabNamesTypeLen = tabNamesType.length;
	for(var i=0;i<tabNamesTypeLen;i++){
		if(tabNamesType[i] == tabv){
			isadd = false;
			break;
		}
	}
	if(!isadd){return;}
	tabNamesType[tabNamesType.length]=tabv;
	tabobj.find(".layui-this").removeClass();
	if(tabobj.find("[v='"+tabv+"']").length>0){
		tabobj.find("[v='"+tabv+"']").addClass("layui-this");
	}else{
		tabobj.append("<li class='layui-this'>"+tabName+"</li>");
	}
	tabcontentobj.find(".layui-show").removeClass();
	if(tabcontentobj.find("[v='"+tabv+"']").length>0){
		tabcontentobj.find("[v='"+tabv+"']").addClass("layui-show");
	}else{
		tabcontentobj.append(tabContent);
	}
}
//模糊搜索
//wiosearch({
//	'url':'/search',
//	'data':{},
//	'outinput':[obj,obj],
//	'title':['id','name'],
//	'titlename':['编号','名字']
//	'inputobj':inputobj,
//  'pobj':pobj,
//	'callback',callback,
//},datas);
function wiosearch(json,jsonData){
	inputobj = json['inputobj'];//输入对象
	var inputval = inputobj.val();//输入的数据
	if(jsonData==undefined){
		//获取
	}
	pobj = json['pobj'];//上一层的对象
	var marginLeft = inputobj.css('margin-left');
	marginLeft = marginLeft == 'auto' ? "0" : parseInt(marginLeft);
	if($("#search_select").length > 0){$("#search_select").remove();}//如果已经存在，就删除
	var searchselect=$("<div id='search_select' style='position: absolute;border:1px solid gray;background:#FFF;width:"+json['width']+"px;overflow:auto;left:"
			+(inputobj.offset().left+marginLeft)+"px;top:"+(inputobj.offset().top+inputobj.height()+3)+"px;height:"+json['height']+"px;z-index:99999999;'></div>");
	pobj.append(searchselect);//添加外框
	
	var title = json['title'];//表头的内容
	var titleName = json['titlename'];
	var outinputArray = json['outinput'];//将要回绑的对象
	var callback = json['callback'];
	
	var sgtable = $("<table style='width:100%;border-collapse:collapse;'></table>");//表格
	var sgtheadtr = $("<tr style='background:#9bb1cc;height:32px;'></tr>");//表头
	searchselect.append(sgtable);
	sgtable.append(sgtheadtr);
	for(var i=0;i<title.length;i++){
		sgtheadtr.append("<td style='text-align:center;'>"+titleName[i]+"</td>");
	}
	var dataLen = jsonData.length;//数据
	for(var i=0;i<dataLen;i++){
		var cd = jsonData[i];
		
		var sgtbodytr = $("<tr style='cursor:pointer;' onmouseover=search_select_add_mousein($(this)) " +
			"onmouseout=search_select_add_mouseout($(this))></tr>");//表格内容
		
		var iscontinue = false;//是否包含数据
		for(var j=0;j<title.length;j++){
			var tbval = cd[title[j]];
			if(tbval.indexOf(inputval) != -1){//判断是否包含
				iscontinue = true;
			}
			sgtbodytr.append("<td style='text-align:center;'>"+tbval+"</td>");//添加数据
		}
		if(!iscontinue){//如果不包含就跳过
			continue;
		}
		sgtable.append(sgtbodytr);
		sgtbodytr.click(function(){//表格点击事件
			search_select_add_outinputArray($(this),outinputArray,searchselect);
			if(callback!=undefined){callback();}
		})
	}
	
	$("body").bind("mousedown", search_otherdown);//点击在表格之外
	function search_otherdown(){
		if (!(event.target.id == "search_select" || event.target.id == "search_select" || $(event.target).parents("#search_select").length>0)) {
			search_select_remove(searchselect);
			$("body").unbind("mousedown", search_otherdown);
		}
	}
	function search_select_remove(selectobj){//删除对象
		selectobj.remove();
	}
	function search_select_add_outinputArray(tr,outinputArray,selectobj){//数据回绑
		var outinputArrayLen = outinputArray.length;
		for(var i=0;i<outinputArrayLen;i++){
			outinputArray[i].val(tr.find('td').eq(i).text());
		}
		search_select_remove(selectobj);
	}
}
function search_select_add_mousein(obj){//变色
	obj.css('background','#d1e4d1');
}
function search_select_add_mouseout(obj){//变色
	obj.css('background','#FFF');
}
//用于刷新页面缓存
function sys_RefResh(){
	$.ajax({
		url:'/systemRefreshStatic',
		type:'get',
		data:{'sessionId':'123123'},
		dataType:'text',
		beforeSend:function (R) {
			R.setRequestHeader('Connection', 'Keep-Alive');//复用连接
		},
		success:function(data){
			location.reload(true);   
		}
	})
}











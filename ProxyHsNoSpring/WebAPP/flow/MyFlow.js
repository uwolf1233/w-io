$(function(){
	$.getScript("/AllJs/formhandle.js",function(){
		var divs = $("#divs");
		//window1(parentId,poppeId,titlename,poppetitleid,poppecontentid,zindex,isclose)
		window1("divs","showcontent","流程设置","showcontentt","showcontentc",20,60,500,430,600,true);
		window1("divs","flowdatas","工作流数据表","flowdatast","flowdatasc",30,60,700,400,600,true);
	})
});

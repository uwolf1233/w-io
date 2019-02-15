/**
 * 数据回绑定 陈泽
 */
function fillForm(form,data){
	for(var key in data){
		var cdoc = form.find("[name='"+key+"']");
		if(cdoc.is("input[type='text']")||cdoc.is("input[type='hidden']")||cdoc.is('textarea')){
			cdoc.val(data[key]);
		}else if(cdoc.is('select')){
			var selected;
			cdoc.find("option").each(function(){
				var cvar = $(this).val();
				if(cvar == data[key]) {
					selected = $(this);
				}
				$(this).attr("selected",false);
			})
			if(selected!=undefined){
				selected.attr("selected",true);
			}
		}
	}
}
/**
 * 数据回绑定 陈泽
 */
function fillFormqz(form,data,qz){
	for(var key in data){
		var cdoc = form.find("[name='"+qz+"."+key+"']");
		if(cdoc.is("input[type='text']")||cdoc.is("input[type='hidden']")||cdoc.is('textarea')){
			cdoc.val(data[key]);
		}else if(cdoc.is('select')){
			var selected;
			cdoc.find("option").each(function(){
				var cvar = $(this).val();
				if(cvar == data[key]) {
					selected = $(this);
				}
				$(this).attr("selected",false);
			})
			if(selected!=undefined){
				selected.attr("selected",true);
			}
		}
	}
}
function fillForm_new(form,data){
	for(var key in data){
		var cdoc = form.find("[name='"+key+"']");
		if(cdoc.length==0){
			cdoc = form.find("[sname='"+key+"']");
		}
		if(cdoc.length==0){continue;}
		if(cdoc.is("input[type='text']")||cdoc.is("input[type='hidden']")||cdoc.is('textarea')){
			cdoc.val(data[key]);
		}else if(cdoc.is('select')){
			var selected;
			cdoc.find("option").each(function(){
				var cvar = $(this).val();
				if(cvar == data[key]) {
					selected = $(this);
				}
				$(this).prop("selected",false);
			})
			if(selected!=undefined){
				selected.prop("selected",true);
			}
		}
	}
}
/**
 * 数据回绑定 陈泽
 */
function fillFormspan(form,data,qz){
	var objqueue = [];
	var dataqueue = [];
	var lenqueue = [];
	var i=0;
	form.find(".formdatabd").each(function(){
		var cdoc = $(this);
		var key = cdoc.attr("name");
		var cd = data[key];
		cd = (cd == null || cd == 'null') ? '' : cd;
		var cdLen = cd.length;
		if(cdoc.length>0){
			dataqueue[i] = cd;
			objqueue[i] = cdoc;
			lenqueue[i] = cdLen;
			i++;
		}
	});
	appdataSpanarray(objqueue, dataqueue, lenqueue);
}
function appdataSpanarray(objqueue,dataqueue,lenqueue){
	var z = 0;
	var i = 0;
	var obj = objqueue[0];
	var data = dataqueue[0];
	var len = lenqueue[0];
	if(setIntervalId==undefined){
		setIntervalId = setInterval(function(){
			if(i >= len){
				z++;
				if(z == objqueue.length){
					window.clearInterval(setIntervalId);
					return;
				}
				obj = objqueue[z];
				data = dataqueue[z];
				len = lenqueue[z];
				i=0;
			}
			appdataSpan(obj, data, len, i);
			i++;
		},80);
	}
}
function appdataSpan(obj,cd,cdlen,i){
	if(i >= cdlen){
		if(cdlen == 0){
			return;
		}
		return;
	}
	obj.append(cd[i]);
}
$.fn.serializeObject = function(){    
   var o = {};    
   var a = this.serializeArray();    
   $.each(a, function() {    
       if (o[this.name]) {    
           if (!o[this.name].push) {    
               o[this.name] = [o[this.name]];    
           }    
           o[this.name].push(this.value || '');    
       } else {    
           o[this.name] = this.value || '';    
       }    
   });    
   return o;    
}; 
$.fn.serializeObject_noinArray = function(noinArray){    
   var o = {};    
   var a = this.serializeArray();    
   $.each(a, function() {    
       if (o[this.name]) {    
           if (!o[this.name].push) {    
               o[this.name] = [o[this.name]];    
           }    
           o[this.name].push(this.value || '');    
       } else {    
           o[this.name] = this.value || '';    
       }    
   });
   var noinArrayLen = noinArray.length;
   for(var i=0;i<noinArrayLen;i++){
	   for(var key in o){
		   if(key == noinArray[i]){
			   delete o[key];
		   }
	   }
   }
   return o;    
};
//数据清空
function clearData(win) {
	$("#" + win + " input[type='text']").each(function() {
		$(this).val('');
	});
	$("#" + win + " input[type='hidden']").each(function() {
		$(this).val('');
	});
	$("#" + win + " input[type='checkbox']").each(function() {
		$(this).attr("checked", false);
	});
	$("#" + win + " textarea").each(function() {
		$(this).val("");
	});
	$("#" + win + " select").each(function() {
		$(this).val("");
	});
	$("#" + win + " img").each(function() {
		$(this).attr("src","");
	});
	$("#" + win + " input[type='file']").each(function() {
		$(this).val('');
	});
	$("#" + win + " [type='input']").each(function() {
		$(this).text('');
	});
}
//jquery监听器

$.fn.extend({
    addEvent:function( type, handle, bool){
        var el, thisLen=this.length;
        bool?bool=bool:bool=false;
        if( thisLen == 1){
            el = this[0];        //jquery对象转成 js对象

            el.addEventListener ? el.addEventListener(type, handle, bool ):
                    el.attachEvent('on'+type, handle);
        }else {
            for( var i=0;i<thisLen;i++ ){
                el = this[i];
                el.addEventListener ? el.addEventListener(type, handle, bool ):
                        el.attachEvent('on'+type, handle);
            }
        }
    }
})
function uuid(len, radix) {
    var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');
    var uuid = [], i;
    radix = radix || chars.length;
 
    if (len) {
      // Compact form
      for (i = 0; i < len; i++) uuid[i] = chars[0 | Math.random()*radix];
    } else {
      // rfc4122, version 4 form
      var r;
 
      // rfc4122 requires these characters
      uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
      uuid[14] = '4';
 
      // Fill in random data.  At i==19 set the high bits of clock sequence as
      // per rfc4122, sec. 4.1.5
      for (i = 0; i < 36; i++) {
        if (!uuid[i]) {
          r = 0 | Math.random()*16;
          uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
        }
      }
    }
 
    return uuid.join('');
}
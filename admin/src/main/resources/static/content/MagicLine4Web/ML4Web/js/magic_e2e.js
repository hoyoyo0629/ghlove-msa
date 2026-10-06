!function(){function a(a){function b(a,b){d(this,"InitError",a,100,b)}function c(a,b){d(this,"ProcessError",a,101,b)}function d(a,b,c,d,e){a.name=b,a.func=c,a.code=d,"string"==typeof e?a.message=e:(a.message=e.message,void 0!==e.errors&&(a.detail=e.errors))}b.prototype=Error.prototype,c.prototype=Error.prototype,a.version="1.0.0.0",a.session={id:null,svrCert:null,alg:null,key:null,iv:null},a.init=function(a){try{magicjs.init(a)}catch(c){throw new b("mle2e.init",c)}},a.makeCertReqMsg=function(){var a={ver:"1",id:"certreq"};return JSON.stringify(a)},a.encrypt=function(b){try{var d=a.session;a.session.key=null,a.session.iv=null;var e={ver:"1",id:"enc",result:"0",session:d.id};if(void 0==d.key||null==d.key){if(null==d.svrcert)throw new Error("The CertReq is required.");d.key=magicjs.generateRandomBytes(16).getBytes(),d.iv=magicjs.generateRandomBytes(16).getBytes();var f=magicjs.x509Cert.create(d.svrcert),g=null;g=d.key,g+=d.iv;var h=null;h=f.pubKey.encrypt(g,{scheme:"RSA-OAEP",md:"sha256"}),d.alg.indexOf("seed")&&(d.alg="seed");var i=magicjs.cipher.create(!0,d.alg+"-cbc",d.key);i.init(d.iv),i.update(d.id);var j=i.finish().getBytes();if(h+=j,304!=h.length)throw new Error("The length of cipher data is not the expected length.");e.hand=magicjs.base64.encode(h)}var i=magicjs.cipher.create(!0,d.alg+"-cbc",d.key);i.init(d.iv),i.update(magicjs.utf8.encode(b));var k=i.finish();return e.enc=magicjs.base64.encode(k),JSON.stringify(e)}catch(l){throw new c("mle2e.encrypt",l)}},a.decrypt=function(b){try{var d=JSON.parse(b);if("1"!=d.ver)throw new Error("The version of message is not 1.");var e=Number(d.result);if(0!=e)throw new Error("The result of message is not success. (result ="+e+")");var f=a.session;if("certreq"==d.id){f.id=d.session,f.svrcert=d.cert;var g=d.algo.toUpperCase();return"ARIA"==g?f.alg="ARIA128":f.alg=g,f.key=null,null}if("enc"==d.id){if(void 0==f.key||null==f.key)throw new Error("A handshake is required.");var h=magicjs.cipher.create(!1,f.alg+"-cbc",f.key);return h.init(f.iv),h.update(magicjs.base64.decode(d.enc)),magicjs.utf8.decode(h.finish().getBytes())}throw new Error("The id of message is neither certreq nor enc.")}catch(i){throw new c("mle2e.decrypt",i)}}}return"undefined"==typeof mle2e&&(mle2e={}),a(mle2e)}();
document.write("<form id='encFrm' name='encFrm' method='post'><input type='hidden' id='encData' name='encData'></form>");
"object"!=typeof JSON&&(JSON={}),function(){"use strict";var rx_one=/^[\],:{}\s]*$/,rx_two=/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g,rx_three=/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,rx_four=/(?:^|:|,)(?:\s*\[)+/g,rx_escapable=/[\\\"\u0000-\u001f\u007f-\u009f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,rx_dangerous=/[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,gap,indent,meta,rep;function f(t){return t<10?"0"+t:t}function this_value(){return this.valueOf()}function quote(t){return rx_escapable.lastIndex=0,rx_escapable.test(t)?'"'+t.replace(rx_escapable,function(t){var e=meta[t];return"string"==typeof e?e:"\\u"+("0000"+t.charCodeAt(0).toString(16)).slice(-4)})+'"':'"'+t+'"'}function str(t,e){var r,n,o,u,f,a=gap,i=e[t];switch(i&&"object"==typeof i&&"function"==typeof i.toJSON&&(i=i.toJSON(t)),"function"==typeof rep&&(i=rep.call(e,t,i)),typeof i){case"string":return quote(i);case"number":return isFinite(i)?String(i):"null";case"boolean":case"null":return String(i);case"object":if(!i)return"null";if(gap+=indent,f=[],"[object Array]"===Object.prototype.toString.apply(i)){for(u=i.length,r=0;r<u;r+=1)f[r]=str(r,i)||"null";return o=0===f.length?"[]":gap?"[\n"+gap+f.join(",\n"+gap)+"\n"+a+"]":"["+f.join(",")+"]",gap=a,o}if(rep&&"object"==typeof rep)for(u=rep.length,r=0;r<u;r+=1)"string"==typeof rep[r]&&(o=str(n=rep[r],i))&&f.push(quote(n)+(gap?": ":":")+o);else for(n in i)Object.prototype.hasOwnProperty.call(i,n)&&(o=str(n,i))&&f.push(quote(n)+(gap?": ":":")+o);return o=0===f.length?"{}":gap?"{\n"+gap+f.join(",\n"+gap)+"\n"+a+"}":"{"+f.join(",")+"}",gap=a,o}}"function"!=typeof Date.prototype.toJSON&&(Date.prototype.toJSON=function(){return isFinite(this.valueOf())?this.getUTCFullYear()+"-"+f(this.getUTCMonth()+1)+"-"+f(this.getUTCDate())+"T"+f(this.getUTCHours())+":"+f(this.getUTCMinutes())+":"+f(this.getUTCSeconds())+"Z":null},Boolean.prototype.toJSON=this_value,Number.prototype.toJSON=this_value,String.prototype.toJSON=this_value),"function"!=typeof JSON.stringify&&(meta={"\b":"\\b","\t":"\\t","\n":"\\n","\f":"\\f","\r":"\\r",'"':'\\"',"\\":"\\\\"},JSON.stringify=function(t,e,r){var n;if(gap="",indent="","number"==typeof r)for(n=0;n<r;n+=1)indent+=" ";else"string"==typeof r&&(indent=r);if(rep=e,e&&"function"!=typeof e&&("object"!=typeof e||"number"!=typeof e.length))throw new Error("JSON.stringify");return str("",{"":t})}),"function"!=typeof JSON.parse&&(JSON.parse=function(text,reviver){var j;function walk(t,e){var r,n,o=t[e];if(o&&"object"==typeof o)for(r in o)Object.prototype.hasOwnProperty.call(o,r)&&(void 0!==(n=walk(o,r))?o[r]=n:delete o[r]);return reviver.call(t,e,o)}if(text=String(text),rx_dangerous.lastIndex=0,rx_dangerous.test(text)&&(text=text.replace(rx_dangerous,function(t){return"\\u"+("0000"+t.charCodeAt(0).toString(16)).slice(-4)})),rx_one.test(text.replace(rx_two,"@").replace(rx_three,"]").replace(rx_four,"")))return j=eval("("+text+")"),"function"==typeof reviver?walk({"":j},""):j;throw new SyntaxError("JSON.parse")})}();
var MagicE2E = function(cert){


	/********************************************************************/
	//				init		                   															  //
	/********************************************************************/
	function Init( cert )
	{
		if( cert != null){
			mle2e.init('MIH1BAE2BAdNYWdpY0pTBBvtlZzqta3sp4Dsl63soJXrs7TqsJzrsJzsm5AEFkRTVFMtMTExMTEtMjQwOTMwLTAxNTgEAAQABAAEgYA3QjFEMEY4RUU3NTA5QkEyRDhGRjUzNDMyODZEQ0M1N0JBNTE5REE2N0Q3QjhFRTc0QzQxNkE2MDAxREREQjc3MDUyNTkzMTEyMEI2NjlGQkU0MjBBRDdFNkRFRUM1OTREMDEyRkVFOTU3RTg0Njg4NDQwMjFFMEQyNUY0QThDN6MpBCdpbG92ZWdvaHlhbmcuZ28ua3IsKi5pbG92ZWdvaHlhbmcuZ28ua3I=');
			mle2e.makeCertReqMsg();
			mle2e.decrypt(JSON.stringify(cert));
		}
	}
	Init( cert );


	/********************************************************************/
	//		       Envelop Data																	//
	/********************************************************************/
	function EnvelopedData( form )
	{
		$('#encData').val( encryptString( $('#'+form.id).serialize().split('&') ) + ',' );
		$('#encFrm').attr({action:$('#'+form.id).attr('action'), method:'post'}).submit();
	}


	/********************************************************************/
	//		      Encrypt Form																							//
	/********************************************************************/
	function Encrypt(data)
	{
		return mle2e.encrypt(data);
	}

	/********************************************************************/
	//		      Decrypt Data																							//
	/********************************************************************/

	function Decrypt(encData)
	{
		return mle2e.decrypt(encData);
	}


	function encryptString(data){
		var lo_array = [];
		for(var j=0; j<data.length; j++){
			lo_array[j] = mle2e.encrypt(data[j]);
		}
		return lo_array;
	}

	function queryString(ars1, ars2){
		var lo_array1 = ars1.split("&");
		var lo_array2 = new Array;
		var lo_result = "";
		var division = ars2;

		for(i=0;i<lo_array1.length;i++){
			lo_array2 = lo_array1[i].split(",");

			if(i<lo_array1.length-1){
				lo_result += lo_array2 + division;
			}else{
				lo_result += lo_array2;
			}
		}

		return lo_result;
	}
	return{
		Init:Init,
		EnvelopedData:EnvelopedData,
		Encrypt:Encrypt,
		Decrypt:Decrypt,
		encryptString:encryptString,
		queryString:queryString

	};
}

"object"!=typeof JSON&&(JSON={}),function(){"use strict";var rx_one=/^[\],:{}\s]*$/,rx_two=/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g,rx_three=/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,rx_four=/(?:^|:|,)(?:\s*\[)+/g,rx_escapable=/[\\\"\u0000-\u001f\u007f-\u009f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,rx_dangerous=/[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,gap,indent,meta,rep;function f(t){return t<10?"0"+t:t}function this_value(){return this.valueOf()}function quote(t){return rx_escapable.lastIndex=0,rx_escapable.test(t)?'"'+t.replace(rx_escapable,function(t){var e=meta[t];return"string"==typeof e?e:"\\u"+("0000"+t.charCodeAt(0).toString(16)).slice(-4)})+'"':'"'+t+'"'}function str(t,e){var r,n,o,u,f,a=gap,i=e[t];switch(i&&"object"==typeof i&&"function"==typeof i.toJSON&&(i=i.toJSON(t)),"function"==typeof rep&&(i=rep.call(e,t,i)),typeof i){case"string":return quote(i);case"number":return isFinite(i)?String(i):"null";case"boolean":case"null":return String(i);case"object":if(!i)return"null";if(gap+=indent,f=[],"[object Array]"===Object.prototype.toString.apply(i)){for(u=i.length,r=0;r<u;r+=1)f[r]=str(r,i)||"null";return o=0===f.length?"[]":gap?"[\n"+gap+f.join(",\n"+gap)+"\n"+a+"]":"["+f.join(",")+"]",gap=a,o}if(rep&&"object"==typeof rep)for(u=rep.length,r=0;r<u;r+=1)"string"==typeof rep[r]&&(o=str(n=rep[r],i))&&f.push(quote(n)+(gap?": ":":")+o);else for(n in i)Object.prototype.hasOwnProperty.call(i,n)&&(o=str(n,i))&&f.push(quote(n)+(gap?": ":":")+o);return o=0===f.length?"{}":gap?"{\n"+gap+f.join(",\n"+gap)+"\n"+a+"}":"{"+f.join(",")+"}",gap=a,o}}"function"!=typeof Date.prototype.toJSON&&(Date.prototype.toJSON=function(){return isFinite(this.valueOf())?this.getUTCFullYear()+"-"+f(this.getUTCMonth()+1)+"-"+f(this.getUTCDate())+"T"+f(this.getUTCHours())+":"+f(this.getUTCMinutes())+":"+f(this.getUTCSeconds())+"Z":null},Boolean.prototype.toJSON=this_value,Number.prototype.toJSON=this_value,String.prototype.toJSON=this_value),"function"!=typeof JSON.stringify&&(meta={"\b":"\\b","\t":"\\t","\n":"\\n","\f":"\\f","\r":"\\r",'"':'\\"',"\\":"\\\\"},JSON.stringify=function(t,e,r){var n;if(gap="",indent="","number"==typeof r)for(n=0;n<r;n+=1)indent+=" ";else"string"==typeof r&&(indent=r);if(rep=e,e&&"function"!=typeof e&&("object"!=typeof e||"number"!=typeof e.length))throw new Error("JSON.stringify");return str("",{"":t})}),"function"!=typeof JSON.parse&&(JSON.parse=function(text,reviver){var j;function walk(t,e){var r,n,o=t[e];if(o&&"object"==typeof o)for(r in o)Object.prototype.hasOwnProperty.call(o,r)&&(void 0!==(n=walk(o,r))?o[r]=n:delete o[r]);return reviver.call(t,e,o)}if(text=String(text),rx_dangerous.lastIndex=0,rx_dangerous.test(text)&&(text=text.replace(rx_dangerous,function(t){return"\\u"+("0000"+t.charCodeAt(0).toString(16)).slice(-4)})),rx_one.test(text.replace(rx_two,"@").replace(rx_three,"]").replace(rx_four,"")))return j=eval("("+text+")"),"function"==typeof reviver?walk({"":j},""):j;throw new SyntaxError("JSON.parse")})}();
// 로컬
var mlLocationPort     = location.port;//location.port;
var mlLocationHostname = location.hostname;
var mlLocationProtocol = location.protocol;
var cpUrl     = mlLocationPort != "" ? mlLocationHostname + ":" + mlLocationPort:mlLocationHostname;
var mlMainUrl = mlLocationProtocol + "//" +(mlLocationPort != "" ? mlLocationHostname + ":" + mlLocationPort:mlLocationHostname);
var mlDirPath = "/content/MagicLine4Web/ML4Web/";
var childHtml = "Child.html";		

$(document).ready(function(){
	var loaderGif = mlMainUrl + mlDirPath;
	
	var body   = document.getElementsByTagName('body')[0];
	var div    = document.createElement('div');
	var iframe = document.createElement('iframe');

	iframe.setAttribute("id",          "dscert");
	iframe.setAttribute("name",        "dscert");
	iframe.setAttribute("src",         "");
	iframe.setAttribute("scrolling",   "no");
	iframe.setAttribute("width",       "100%");
	iframe.setAttribute("height",      "100%");
	iframe.setAttribute("frameBorder", "2");
	iframe.setAttribute("translate",   "yes");
	iframe.setAttribute("style",       "position:fixed; z-index:100010; top:0px; left:0px; width:100%; height:100%;");
	iframe.onload = function() { };
	
	div.id    = "dscertContainer";
	div.style = "display: none";
	div.appendChild(iframe);
	
	if (document.getElementById("dscertContainer") == null) {
		body.appendChild(div);
	}
	
	$('#dscertContainer').hide();
	
	$.blockUI({
		message:'<div><div><img src="' + loaderGif + 'UI/images/loader.gif" alt="로딩중입니다."/></div><p style="display:inline-block; padding-top:4px; font-size:11px; color:#333; font-weight:bold;">잠시만 기다려 주세요.</p></div>',
		css:{left:(($(window).width()/2)-75)+'px'}
	});
	
	magicline.uiapi.ML_funProcInitCheck(function(code,data){
		if( code == 0 ){
			magicline.uiapi.completeInit();
			if(typeof(checkCallback) == "function"){
				magicline.uiapi.checkInstall(checkCallback);
			}
		}
	});
	
	magicline.uiapi.ML_checkInit();
});

var magicline = {
		uiapi : "",
		initCallback : "",
		is_ML_Sign_Init:false
}
var magiclineApi = function(){
	var callback="";
	var defaultOptions = {
			sign:{signType:"MakeSignData",msg:"",messageType:"",signOpt:{ds_pki_sign:['OPT_USE_CONTNET_INFO'], ds_pki_rsa:'rsa15', ds_pki_hash:'sha256',ds_msg_decode:"false",ds_pki_sign_type:"signeddata"}},
			signPdfOpt:{ds_pki_sign:['OPT_USE_CONTNET_INFO','OPT_USE_PKCS7','OPT_NO_CONTENT','OPT_HASHED_CONTENT'], ds_pki_rsa:'rsa15', ds_pki_hash:'sha256',ds_msg_decode:"true"},
			encOpt:{ds_pki_rsa:'rsa15'},
			signedenvOpt:{ds_pki_sign:['OPT_USE_CONTNET_INFO'], ds_pki_rsa:'rsa15', ds_pki_algo:'SEED-CBC'},
			// 추가
			idn : "",
			vidType : "",
			certOidfilter:"", //1.2.410.100001.2.2.1,1.2.410.200005.1.1.4
			certExpirefilter:true, //false:만료 인증서 보여주기, true:보여주지 않기
			//인증서 매체 리스트
			storageList:"",	//지원 스토리지 리스트 ( 순서대로 UI 그림, JSON)
			//공동,금융인증 리스트
			storageList_lnb:"", //[]:날개안보여주기 , share:공동인증, finance:금융인증
			//mrs2 옵션 설정
			saveStorageList : ["web","hdd"],
			exportStorageList : ["web", "hdd"],
			exportStorageSelect : "web",
			browser_notice_show	: false,
			//특허청 전자서명 옵션
			kipoSignOpt:{signType:"MakeSignData",msg:"",messageType:"",signOpt:{ds_pki_sign:['OPT_USE_CONTNET_INFO', 'OPT_HASHED_CONTENT'], ds_pki_rsa:'rsa15', ds_pki_hash:'sha256',ds_msg_decode:"hash",ds_pki_sign_type:"signeddata"}},
	}
	
	function CommonResopnseProcess( json ){
		
		var response = JSON.parse( json );
		var close = response.close;

		if( response.close  == 'closeDialog'){
			$('#dscertContainer').hide();
		}
	}
	
	/**
	 * send 할 메시지를 생성
	 */
	function MakeRequestJsonMessage( functionName, functionParameter, option ){
		var temp = 
			{
				"funcName" : functionName,
				"funcParam" : functionParameter
			}
		return JSON.stringify( temp );
	}
	
	//20200806. UCPIDReqeuestInfo (공인인증서를 이용한 본인확인 서비스 가이드라인 부속서 v1.3)
	/**
	 * var moduleInfo  = {
	 *		name : "module name",
	 *		vendorName : "vender name"};
	 *
	 * var moduleVersion  = {
	 *		major : 1,
	 *		minor : 1,
	 *		build : 0,		// OPTION 
	 *		revision : 0 }; // OPTION 
	 * 

	 * @param userAgreement		개인정보제공 및 활용동의 약관 (struct)
	 * @param ucpidRealName		ISP에서 필요로 하는 개인정보 항목 - 이름
	 * @param ucpidGender		ISP에서 필요로 하는 개인정보 항목 - 성별
	 * @param ucpidNationalInfo	ISP에서 필요로 하는 개인정보 항목 - 국적
	 * @param ucpidBirthDate	ISP에서 필요로 하는 개인정보 항목 - 생년월일
	 * @param ucpidCi			ISP에서 필요로 하는 개인정보 항목 - 주민번호(ci)
	 * @param ucpidNonce		랜덤값 (array)
	 * @param ispUrlInfo sheme 정의부와 uri 정의부를 제외한 url (문자열)
	 */
	function makeUCPIDRequestInfo(userAgreement, ucpidRealName, ucpidGender, ucpidNationalInfo, ucpidBirthDate, ucpidCi, ucpidNonce, ispUrlInfo, callback) {
		magiclineApi.callback = callback;
		var param = {signType:"MakeSignData",msg:"",messageType:"",signOpt:{ds_pki_sign:['OPT_USE_CONTNET_INFO'], ds_pki_rsa:'rsa15', ds_pki_hash:'sha256',ds_msg_decode:"true",ds_pki_sign_type:"signeddata"}};
		param.signType = "MakeSignData";
		
		var _0x4127			= ['charAt','indexOf','39244PPzLvP','name','revision','vendorName','ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=','139183OTZlpm','join','205777nFKene','160116bceysy','18935MgzcmE','major','replace','5JOANGH','length','push','85785urBQfy','build','concat','charCodeAt','minor','fromCharCode','226829zxpEsv'];(function(_0x439d3e,_0x2736a5){var _0x22fd27=_0x581b;while(!![]){try{var _0x4f0063=parseInt(_0x22fd27(0x7e))+parseInt(_0x22fd27(0x6d))+-parseInt(_0x22fd27(0x7f))+parseInt(_0x22fd27(0x7b))+parseInt(_0x22fd27(0x82))*-parseInt(_0x22fd27(0x76))+-parseInt(_0x22fd27(0x73))+parseInt(_0x22fd27(0x7d));if(_0x4f0063===_0x2736a5)break;else _0x439d3e['push'](_0x439d3e['shift']());}catch(_0x779298){_0x439d3e['push'](_0x439d3e['shift']());}}}(_0x4127,0x2458d));function genModuleInfo(_0x483605,_0x4d7c4a){var _0x5547d7=_0x581b,_0x3a50ab=setASN1(getBytes(_0x483605[_0x5547d7(0x77)]),0xc,0x0),_0x1e3a7c=setASN1(getBytes(_0x483605[_0x5547d7(0x79)]),0xc,0x0),_0x87a40a=genModuleVersion(_0x4d7c4a),_0x463b7b=_0x3a50ab[_0x5547d7(0x6f)](_0x1e3a7c);_0x463b7b=_0x463b7b[_0x5547d7(0x6f)](_0x87a40a);var _0x5e0682=setASN1(_0x463b7b,0x30,0x0);return _0x5e0682;}function genModuleVersion(_0x5af5f3){var _0x6e0b67=_0x581b,_0x43e6d4=setASN1(int2Bytes(_0x5af5f3[_0x6e0b67(0x80)]),0x2,0x0),_0x26d27d=setASN1(int2Bytes(_0x5af5f3[_0x6e0b67(0x71)]),0x2,0x0),_0x46e947=null,_0x3b9710=null;if(_0x5af5f3['build']!=undefined){var _0xc6ab7d=setASN1(int2Bytes(_0x5af5f3[_0x6e0b67(0x6e)]),0x2,0x0);_0x46e947=setASN1(_0xc6ab7d,0xa0,0x0);}if(_0x5af5f3[_0x6e0b67(0x78)]!=undefined){var _0xc6ab7d=setASN1(int2Bytes(_0x5af5f3[_0x6e0b67(0x78)]),0x2,0x0);_0x3b9710=setASN1(_0xc6ab7d,0xa1,0x0);}var _0x551587=_0x43e6d4[_0x6e0b67(0x6f)](_0x26d27d);if(_0x46e947!=null)_0x551587=_0x551587[_0x6e0b67(0x6f)](_0x46e947);if(_0x3b9710!=null)_0x551587=_0x551587[_0x6e0b67(0x6f)](_0x3b9710);var _0x17c0fc=setASN1(_0x551587,0x30,0x0);return _0x17c0fc;}function genPersonInfoReq(_0x3d257f,_0x4b571d,_0x3e96aa,_0x2623ac,_0x15d05e,_0x50872c){var _0x5ad80a=_0x581b,_0x53c22f=0x80,_0x577b52=0x40,_0x194f32=0x20,_0x1b54cc=0x10,_0x3dbd95=0x8,_0x56a15d=new Array(0x1),_0x5d4739=0x3;_0x56a15d[0x0]=(_0x4b571d==!![]?_0x53c22f:0x0)|(_0x3e96aa==!![]?_0x577b52:0x0)|(_0x2623ac==!![]?_0x194f32:0x0)|(_0x15d05e==!![]?_0x1b54cc:0x0)|(_0x50872c==!![]?_0x3dbd95:0x0);var _0x6d4ef8=setASN1(getBytes(_0x3d257f),0xc,0x0),_0x3b57a1=setASN1(_0x56a15d,0x3,_0x5d4739),_0x125b89=_0x6d4ef8[_0x5ad80a(0x6f)](_0x3b57a1),_0x33c015=setASN1(_0x125b89,0x30,0x0);return _0x33c015;}function makeUCPIDMsg(_0x1347a9,_0x595b67,_0x380e3a,_0xa66839,_0x3d1aa3,_0x4a0609){var _0x2681cf=genPersonInfoReq(_0x1347a9,_0x595b67,_0x380e3a,_0xa66839,_0x3d1aa3,_0x4a0609);return base64_encode(_0x2681cf);}function _0x581b(_0x34aceb,_0x142478){_0x34aceb=_0x34aceb-0x6b;var _0x412745=_0x4127[_0x34aceb];return _0x412745;}function setASN1(_0xc6c0a7,_0x171e90,_0x35e5f6,_0x386c1d){var _0x43180e=_0x581b,_0x238ad5=0x0,_0x5aa3b2,_0x5d8991=_0xc6c0a7['length'],_0x237b2d=new Array();_0x237b2d[_0x43180e(0x6c)](_0x171e90);_0x171e90==0x3&&_0x5d8991++;if(_0x5d8991<=0x7f)_0x237b2d[_0x43180e(0x6c)](_0x5d8991);else{var _0x238ad5=_0xc6c0a7[_0x43180e(0x6b)],_0x51c721='';do{_0x51c721+=String[_0x43180e(0x72)](_0x238ad5&0xff),_0x238ad5=_0x238ad5>>>0x8;}while(_0x238ad5>0x0);_0x237b2d['push'](_0x51c721[_0x43180e(0x6b)]|0x80);for(var _0x5aa3b2=_0x51c721[_0x43180e(0x6b)]-0x1;_0x5aa3b2>=0x0;--_0x5aa3b2){_0x237b2d['push'](_0x51c721['charCodeAt'](_0x5aa3b2));}}if(_0x171e90==0x3)_0x237b2d[_0x43180e(0x6c)](_0x35e5f6);return _0x237b2d=_0x237b2d['concat'](_0xc6c0a7),_0x237b2d;}function int2Bytes(_0x2298b7){var _0x4fabef=_0x581b,_0x19ddf1=new Array();if(_0x2298b7<=0x7f)_0x19ddf1[_0x4fabef(0x6c)](_0x2298b7);else{var _0x35fdb2=_0x2298b7,_0x2aa9bd='';do{_0x2aa9bd+=String[_0x4fabef(0x72)](_0x35fdb2&0xff),_0x35fdb2=_0x35fdb2>>>0x8;}while(_0x35fdb2>0x0);if(_0x2aa9bd['charCodeAt'](_0x2aa9bd[_0x4fabef(0x6b)]-0x1)&0x80)_0x19ddf1[_0x4fabef(0x6c)](0x0);for(var _0x5d69c2=_0x2aa9bd[_0x4fabef(0x6b)]-0x1;_0x5d69c2>=0x0;--_0x5d69c2){_0x19ddf1[_0x4fabef(0x6c)](_0x2aa9bd[_0x4fabef(0x70)](_0x5d69c2));}}return _0x19ddf1;}function getBytes(_0x2fca34){var _0xbe5c33=_0x581b,_0x1c131e=encode_utf8(_0x2fca34),_0x5242d1=new Array(_0x1c131e[_0xbe5c33(0x6b)]);for(var _0x473b95=0x0;_0x473b95<_0x1c131e[_0xbe5c33(0x6b)];_0x473b95++)_0x5242d1[_0x473b95]=_0x1c131e[_0xbe5c33(0x70)](_0x473b95);return _0x5242d1;}function encode_utf8(_0x4026a8){return unescape(encodeURIComponent(_0x4026a8));}function decode_utf8(_0x2d9917){return decodeURIComponent(escape(_0x2d9917));}function base64_encode(_0x36b44f){var _0x164983=_0x581b,_0x251c32='ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=',_0x1df0eb=0x0,_0x2f66ba=_0x36b44f[_0x164983(0x6b)],_0x4632e7,_0x32c593,_0x4c4fdd,_0x1a1732,_0x3e8a61,_0x47c5f3,_0x497212,_0x49289a=[];while(_0x1df0eb<_0x2f66ba){_0x4632e7=_0x36b44f[_0x1df0eb++],_0x32c593=_0x36b44f[_0x1df0eb++],_0x4c4fdd=_0x36b44f[_0x1df0eb++],_0x1a1732=_0x4632e7>>0x2,_0x3e8a61=(_0x4632e7&0x3)<<0x4|_0x32c593>>0x4,_0x47c5f3=(_0x32c593&0xf)<<0x2|_0x4c4fdd>>0x6,_0x497212=_0x4c4fdd&0x3f;if(isNaN(_0x32c593))_0x47c5f3=_0x497212=0x40;else isNaN(_0x4c4fdd)&&(_0x497212=0x40);_0x49289a[_0x164983(0x6c)](_0x1a1732,_0x3e8a61,_0x47c5f3,_0x497212);}return _0x49289a['map'](function(_0x3a89d6){var _0x684222=_0x164983;return _0x251c32[_0x684222(0x74)](_0x3a89d6);})[_0x164983(0x7c)]('');}function base64_decode(_0x137aa3){var _0x1edbdd=_0x581b,_0x33fe8a=_0x1edbdd(0x7a),_0x1d39ff,_0x16b500,_0x47f422,_0x11b435,_0x3d7ac6,_0x8bd6e7,_0x1070b8,_0x55cd58,_0x3a38ad=0x0,_0xa6237=0x0,_0x1637f5='',_0x35796b=[];if(!_0x137aa3)return _0x137aa3;_0x137aa3+='';do{_0x11b435=_0x33fe8a['indexOf'](_0x137aa3[_0x1edbdd(0x74)](_0x3a38ad++)),_0x3d7ac6=_0x33fe8a[_0x1edbdd(0x75)](_0x137aa3[_0x1edbdd(0x74)](_0x3a38ad++)),_0x8bd6e7=_0x33fe8a[_0x1edbdd(0x75)](_0x137aa3[_0x1edbdd(0x74)](_0x3a38ad++)),_0x1070b8=_0x33fe8a[_0x1edbdd(0x75)](_0x137aa3['charAt'](_0x3a38ad++)),_0x55cd58=_0x11b435<<0x12|_0x3d7ac6<<0xc|_0x8bd6e7<<0x6|_0x1070b8,_0x1d39ff=_0x55cd58>>0x10&0xff,_0x16b500=_0x55cd58>>0x8&0xff,_0x47f422=_0x55cd58&0xff;if(_0x8bd6e7==0x40)_0x35796b[_0xa6237++]=String[_0x1edbdd(0x72)](_0x1d39ff);else _0x1070b8==0x40?_0x35796b[_0xa6237++]=String[_0x1edbdd(0x72)](_0x1d39ff,_0x16b500):_0x35796b[_0xa6237++]=String[_0x1edbdd(0x72)](_0x1d39ff,_0x16b500,_0x47f422);}while(_0x3a38ad<_0x137aa3['length']);return _0x1637f5=_0x35796b['join'](''),_0x1637f5[_0x1edbdd(0x81)](/\0+$/,'');}
		var moduleVersion	= { major : 1, minor : 3, build : 0, revision : 0 };			// Deremsecurity DS_UCPID_Client module version (*Do not modify)		
		var moduleInfo		= { name  : "DS_UCPID_Client", vendorName : "Dreamsecurity" };	// Deremsecurity DS_UCPID_Client module information (*Do not modify)		
		var version			= [0x02];														// version 2
		var f_version		= setASN1(version, 0x02, 0);		
		var f_ucpidNonde	= setASN1(ucpidNonce, 0x04, 0);									// ucpidNonce
		var f_personInfoReq = genPersonInfoReq(userAgreement, ucpidRealName, ucpidGender, ucpidNationalInfo, ucpidBirthDate, ucpidCi);	// personInfoReq
		var f_moduleInfo	= genModuleInfo(moduleInfo, moduleVersion);						// moduleInfo
		var f_ispUrlInfo	= setASN1(getBytes(ispUrlInfo), 0x0c, 0);						// ispUrlInfo
		var node			= f_version.concat(f_ucpidNonde);
		
		node = node.concat(f_personInfoReq);
		node = node.concat(f_moduleInfo);
		node = node.concat(f_ispUrlInfo);
		
		var seq  = setASN1(node, 0x30, 0);
		var msg  = base64_encode(seq);
		
		// Param Mapping
		if( msg!=null && typeof(msg)!='undefined' && msg!='' ){
			param.msg = msg;
		}
		
		if(typeof(msg) == 'object' && typeof(msg.signData) != "undefined"){
			if(typeof(msg.signData.length) != "undefined"){
				param.msg = new Array();
				for(var i = 0; i < msg.signData.length; i++){
					param.msg[i] = msg.signData[i].value;
				}
			}else{
				param.msg = msg.signData.value;
			}
		}
		
		if(msg.idn != null && msg.idn != ""){
			param.idn = msg.idn.value;
		}
		
		if(msg.vidType != null && msg.vidType != ""){
			param.vidType = msg.vidType.value;							
		}
		
		param.signOpt.ds_pki_sign_type	 = "signeddata";
		param.signOpt.cert_filter_expire = defaultOptions.certExpirefilter;
		param.signOpt.cert_filter_oid	 = defaultOptions.certOidfilter;

		param.certOidfilter = defaultOptions.certOidfilter;
		param.certExpirefilter = defaultOptions.certExpirefilter;
		
		var funcName = param.signType;
		var option = null;
		
		var request = MakeRequestJsonMessage(funcName, param, option );
		
		addEventLisner( callback );
		ML_sendPostMessage( request );		
	}
	
	
	/**
	 * getCertManager
	 */
	function getCertManager(callback){
		//magicline.callback = callback;
		if(magicline.is_ML_Sign_Init){
			var param = defaultOptions.sign;
			param.key = "getCertManager";
			
			var funcName = param.key;
			var option = null;
			
			var request = MakeRequestJsonMessage(funcName, param, option );
		
			addEventLisner( callback );
			ML_sendPostMessage( request );
		}else{
			alert("초기화 중입니다. 잠시 후 다시 시도해 주세요.");
		}
	}
	
	
	/**
	 * SignedData
	 */
	function MakeSignData( msg , signOpt, callback ){
		
		magiclineApi.callback = callback;
		var param = defaultOptions.sign;
		param.signType = "MakeSignData";
		
		// Param Mapping
		if( msg!=null && typeof(msg)!='undefined' && msg!='' ){
			param.msg = msg;
		}
		
		// 본인확인 (IDN) 입력시 서명 원문 맵핑
//		if(msg instanceof HTMLFormElement){
		if(typeof(msg) == 'object' && typeof(msg.signData) != "undefined"){
			//param.msg = msg.signData.value;
			
			if(typeof(msg.signData.length) != "undefined"){
				param.msg = new Array();
				for(var i = 0; i < msg.signData.length; i++){
					param.msg[i] = msg.signData[i].value;
				}
			}else{
				param.msg = msg.signData.value;
			}
		}
		
		if(msg.idn != null && msg.idn != ""){
			param.idn = msg.idn.value;
		}
		
		if(msg.vidType != null && msg.vidType != ""){
			param.vidType = msg.vidType.value;							
		}
		
		param.signOpt.ds_pki_sign_type = "signeddata";
		param.signOpt.cert_filter_expire = defaultOptions.certExpirefilter;
		param.signOpt.cert_filter_oid = defaultOptions.certOidfilter;
		
		param.certOidfilter = defaultOptions.certOidfilter;
		param.certExpirefilter = defaultOptions.certExpirefilter;
		
		param.STORAGELIST = defaultOptions.storageList;
		param.STORAGELIST_LNB = defaultOptions.storageList_lnb;
		
		var funcName = param.signType;
		var option = null;
		
		var request = MakeRequestJsonMessage(funcName, param, option );
		
		addEventLisner( callback );
		ML_sendPostMessage( request );
	}
	
		//211015 간편인증 단독 동작 추가 
	function MakeEZSignData(msg, url, callback){
		//211018 signOpt, callback 인자도 추가 (미사용) 
		magiclineApi.callback = callback;
		var param = defaultOptions.sign;
		param.signType = "MakeEZSignData";
		
		// Param Mapping
		if( msg!=null && typeof(msg)!='undefined' && msg!='' ){
			param.msg = msg;
		}
		
		if( url!=null && typeof(url)!='undefined' && url!='' ){
			param.ezServiceUrl = url;
		}
		
		/*
		// 본인확인 (IDN) 입력시 서명 원문 맵핑
//		if(msg instanceof HTMLFormElement){
		if(typeof(msg) == 'object' && typeof(msg.signData) != "undefined"){
			//param.msg = msg.signData.value;
			
			if(typeof(msg.signData.length) != "undefined"){
				param.msg = new Array();
				for(var i = 0; i < msg.signData.length; i++){
					param.msg[i] = msg.signData[i].value;
				}
			}else{
				param.msg = msg.signData.value;
			}
		}
		
		if(msg.idn != null && msg.idn != ""){
			param.idn = msg.idn.value;
		}
		
		if(msg.vidType != null && msg.vidType != ""){
			param.vidType = msg.vidType.value;							
		}
		
		param.signOpt.ds_pki_sign_type = "signeddata";
		param.signOpt.cert_filter_expire = defaultOptions.certExpirefilter;
		param.signOpt.cert_filter_oid = defaultOptions.certOidfilter;

		param.certOidfilter = defaultOptions.certOidfilter;
		param.certExpirefilter = defaultOptions.certExpirefilter;*/
		
		var funcName = param.signType;
		var option = null;
		
		var request = MakeRequestJsonMessage(funcName, param, option );
		addEventLisner( callback );
		ML_sendPostMessage( request );
	}
	
	// TSA 연동 함수
	function MakePdfSignData( msg , signOpt, callback ){
		magiclineApi.callback = callback;
		var param = {signType:"MakeSignData",msg:"",messageType:"",signOpt:{ds_pki_sign:['OPT_USE_CONTNET_INFO','OPT_HASHED_CONTENT'], ds_pki_rsa:'rsa15', ds_pki_hash:'sha256',ds_msg_decode:"true",ds_pki_sign_type:"signeddata"}};
		param.signType = "MakeSignData";
		
		// Param Mapping
		if( msg!=null && typeof(msg)!='undefined' && msg!='' ){
			param.msg = msg;
		}
		
		// 본인확인 (IDN) 입력시 서명 원문 맵핑
//		if(msg instanceof HTMLFormElement){
		if(typeof(msg) == 'object' && typeof(msg.signData) != "undefined"){
			//param.msg = msg.signData.value;
			
			if(typeof(msg.signData.length) != "undefined"){
				param.msg = new Array();
				for(var i = 0; i < msg.signData.length; i++){
					param.msg[i] = msg.signData[i].value;
				}
			}else{
				param.msg = msg.signData.value;
			}
		}
		
		if(msg.idn != null && msg.idn != ""){
			param.idn = msg.idn.value;
		}
		
		if(msg.vidType != null && msg.vidType != ""){
			param.vidType = msg.vidType.value;							
		}
		
		param.signOpt.ds_pki_sign_type = "signeddata";
		param.signOpt.cert_filter_expire = defaultOptions.certExpirefilter;
		param.signOpt.cert_filter_oid = defaultOptions.certOidfilter;

		param.certOidfilter = defaultOptions.certOidfilter;
		param.certExpirefilter = defaultOptions.certExpirefilter;
		
		var funcName = param.signType;
		var option = null;
		
		var request = MakeRequestJsonMessage(funcName, param, option );
		
		addEventLisner( callback );
		ML_sendPostMessage( request );
	}
	/**
	 * SinatureData
	 */
	function MakeSignatureData( msg , signOpt, callback ){
		magiclineApi.callback = callback;
		
		var param = defaultOptions.sign;		
		param.signType = "MakeSignData";
		
		if( msg!=null && typeof(msg)!='undefined' && msg!='' ){
			param.msg = msg;
		}
		
//		if(msg instanceof HTMLFormElement){
		if(typeof(msg) == 'object'){
			param.msg = magicline.extraceFormToString(msg);
		}
		
		if(msg.signData != null && msg.signData != ""){
			param.signData = msg.signData.value;
		}
		
		if(msg.idn != null && msg.idn != ""){
			param.idn = msg.idn.value;
		}
		
		if(msg.vidType != null && msg.vidType != ""){
			param.vidType = msg.vidType.value;							
		}
		
		param.signOpt.ds_pki_sign_type = "sign";
		param.signOpt.cert_filter_expire = defaultOptions.certExpirefilter;
		param.signOpt.cert_filter_oid = defaultOptions.certOidfilter;

		param.certOidfilter = defaultOptions.certOidfilter;
		param.certExpirefilter = defaultOptions.certExpirefilter;
		
		var funcName = param.signType;
		var option = null;
		
		var request = MakeRequestJsonMessage(funcName, param, option );
		
		addEventLisner( callback );
		ML_sendPostMessage( request );
	}
	/**
	 * MakeAddSignData
	 */
	function MakeAddSignData( msg , signOpt, callback ){
		
		magiclineApi.callback = callback;
		var param = defaultOptions.sign;
		param.signType = "MakeSignData";
		
		// Param Mapping
		if( msg!=null && typeof(msg)!='undefined' && msg!='' ){
			param.msg = msg;
		}
		// 본인확인 (IDN) 입력시 서명 원문 맵핑
//		if(msg instanceof HTMLFormElement){
		if(typeof(msg) == 'object'){
			//param.msg = magicline.extraceFormToString(msg);
			param.msg = msg.signData.value;
		}
		
		if(msg.idn != null && msg.idn != ""){
			param.idn = msg.idn.value;
		}
		
		if(msg.vidType != null && msg.vidType != ""){
			param.vidType = msg.vidType.value;							
		}
		
		param.signOpt.ds_pki_sign_type = "signeddata";
		var funcName = param.signType;
		param.signOpt.ds_pki_signData = param.msg;
		param.signOpt.ds_pki_signdata = param.msg;
		
		param.signOpt.cert_filter_expire = defaultOptions.certExpirefilter;
		param.signOpt.cert_filter_oid = defaultOptions.certOidfilter;

		param.certOidfilter = defaultOptions.certOidfilter;
		param.certExpirefilter = defaultOptions.certExpirefilter;
		
		var option = null;
		
		var request = MakeRequestJsonMessage(funcName, param, option );
		
		addEventLisner( callback );
		ML_sendPostMessage( request );
	}
	
	/**
	 * ntsCertAuth
	 */
	function ntsCertAuth(msg, signOpt, callback){
		
		magiclineApi.callback = callback;
		
		var param = defaultOptions.sign;
		param.signType = "NTSCertAuth";
		
		if( msg!=null && typeof(msg)!='undefined' && msg!='' ){
			param.msg = msg;
		}
		
//		if(msg instanceof HTMLFormElement){
		if(typeof(msg) == 'object'){
			param.msg = magicline.extraceFormToString(msg);
		}
		
		if(msg.signData != null && msg.signData != ""){
			param.signData = msg.signData.value;
		}
		
		if(msg.idn != null && msg.idn != ""){
			param.idn = msg.idn.value;
		}
		
		if(msg.vidType != null && msg.vidType != ""){
			param.vidType = msg.vidType.value;							
		}
		
		param.signOpt.ds_pki_sign_type = "sign";
		
		param.signOpt.cert_filter_expire = defaultOptions.certExpirefilter;
		param.signOpt.cert_filter_oid = defaultOptions.certOidfilter;

		param.certOidfilter = defaultOptions.certOidfilter;
		param.certExpirefilter = defaultOptions.certExpirefilter;
		
		var funcName = param.signType;
		var option = null;
		
		var request = MakeRequestJsonMessage(funcName, param, option );
		
		addEventLisner( callback );
		ML_sendPostMessage( request );
	}
	
	function keyBoardSecurityUse(strKeyboard, callback){
		magiclineApi.callback = callback;
		
		if(magicline.is_ML_Sign_Init){
			
			var param = {};
			param.layer = "UI";
			param.strKeyboard = strKeyboard;
			
			var option = null;			
			var request = MakeRequestJsonMessage("keyBoardSecurityUse", param, option);
			
			ML_sendUtilMessage(request);
			
		}else{
			alert("초기화 중입니다. 잠시 후 다시 시도해 주세요.");
		}
	}
	
	function tranx2PEM(callback){
		magiclineApi.callback = callback;
		
		if(magicline.is_ML_Sign_Init){
			
			var param = {};
			param.layer = "UI";			
			var option = null;
			
			var request = MakeRequestJsonMessage("tranx2PEM", param, option);			
			ML_sendUtilMessage(request);
			
		}else{
			alert("초기화 중입니다. 잠시 후 다시 시도해 주세요.");
		}
		
	}
	
	function getRandomfromPrivateKey(callback){
		magiclineApi.callback = callback;
		
		if(magicline.is_ML_Sign_Init){
		
			var param = {};
			param.layer = "UI";
			var option = null;
			
			var request = MakeRequestJsonMessage("getVIDRandom", param, option);
			ML_sendUtilMessage(request);
			
		}else{
			alert("초기화 중입니다. 잠시 후 다시 시도해 주세요.");
		}
	}
	
	function setSessionID(strSessionID, callback){		
		magiclineApi.callback = callback;
		
		if(magicline.is_ML_Sign_Init){
			
			var param = {};
			param.layer = "UI";
			param.strSessionID = strSessionID;
			var option = null;
			
			var request = MakeRequestJsonMessage("setSessionID", param, option);
			ML_sendUtilMessage(request);
			
		}else{
			alert("초기화 중입니다. 잠시 후 다시 시도해 주세요.");
		}	
	}
	
	function getSignDN(callback){
		magiclineApi.callback = callback;
		
		if(magicline.is_ML_Sign_Init){
			
			var param = {};
			param.layer = "UI";
			var option = null;
			var request = MakeRequestJsonMessage("getSignDN", param, option);
			
			ML_sendUtilMessage(request);
			
		}else{
			alert("초기화 중입니다. 잠시 후 다시 시도해 주세요.");
		}
	}
	
	function signatureData(dn, callback){
		magiclineApi.callback = callback;
		
		if(magicline.is_ML_Sign_Init){
			
			var param = {};
			param.layer = "UI";
			param.msg = dn;
			var option = null;
			
			var request = MakeRequestJsonMessage("signatureData", param, option);
			
			ML_sendUtilMessage(request);
		}else{
			alert("초기화 중입니다. 잠시 후 다시 시도해 주세요.");
		}
		
	}
	
	/**
	 * 인증서 저장을 위한 함수
	 */
	function saveCertToStorage(certbag, stgArr, callback){
		magiclineApi.callback = callback;
		
		if(magicline.is_ML_Sign_Init){			
			var param = {};						
			param.certbag = certbag;
			param.stgArr  = defaultOptions.saveStorageList;
			
			var option = null;			
			var request = MakeRequestJsonMessage("saveCertToStorage", param, option);			
			addEventLisner( callback );
			ML_sendPostMessage( request );
		}else{
			alert('초기화 중입니다. 잠시 후 다시 시도해 주세요.');
		}	
	}
	
	/**
	 * 인증서 이동을 위한 함수
	 */
	function getSelectCert( msg , signOpt, callback ){
		
		magiclineApi.callback = callback;
		var param = defaultOptions.sign;
		param.signType = "MakeSignData";
		
		// Param Mapping
		if( msg!=null && typeof(msg)!='undefined' && msg!='' ){
			param.msg = msg;
		}
		
		// 본인확인 (IDN) 입력시 서명 원문 맵핑
		if(typeof(msg) == 'object'){
			param.msg = msg.signData.value;
		}
		
		if(msg.idn != null && msg.idn != ""){
			param.idn = msg.idn.value;
		}
		
		if(msg.vidType != null && msg.vidType != ""){
			param.vidType = msg.vidType.value;							
		}
		
		param.signOpt.ds_pki_sign_type = "signeddata";
		param.signOpt.cert_filter_expire = defaultOptions.certExpirefilter;
		param.signOpt.cert_filter_oid = defaultOptions.certOidfilter;

		param.certOidfilter = defaultOptions.certOidfilter;
		param.certExpirefilter = defaultOptions.certExpirefilter;
		
		// 인증서 이동시 인증서를 불러오기 위한 저장매체 설정 - 인증서내보내기
		param.STORAGELIST			= defaultOptions.exportStorageList;
		param.STORAGESELECT			= defaultOptions.exportStorageSelect;
		param.BROWSER_NOTICE_SHOW	= defaultOptions.browser_notice_show;
		
		var funcName = param.signType;
		var option = null;
		
		var request = MakeRequestJsonMessage(funcName, param, option );
		
		addEventLisner( callback );
		ML_sendPostMessage( request );
	}
	
	/**
	 * 특허청 파일 해쉬 전자서명
	 */
	function MakeHashSignData( msg , signOpt, callback ){
		magiclineApi.callback = callback;
		var param = defaultOptions.kipoSignOpt;
		param.signType = "MakeSignData";
		
		// Param Mapping
		if( msg!=null && typeof(msg)!='undefined' && msg!='' ){
			param.msg = msg;
		}
		
		// 본인확인 (IDN) 입력시 서명 원문 맵핑
		if(typeof(msg) == 'object' && typeof(msg.signData) != "undefined"){
			//param.msg = msg.signData.value;
			
			if(typeof(msg.signData.length) != "undefined"){
				param.msg = new Array();
				for(var i = 0; i < msg.signData.length; i++){
					param.msg[i] = msg.signData[i].value;
				}
			}else{
				param.msg = msg.signData.value;
			}
		}
		
		if(msg.idn != null && msg.idn != ""){
			param.idn = msg.idn.value;
		}
		
		if(msg.vidType != null && msg.vidType != ""){
			param.vidType = msg.vidType.value;							
		}
		
		param.signOpt.ds_pki_sign_type = "signeddata";
		param.signOpt.cert_filter_expire = defaultOptions.certExpirefilter;
		param.signOpt.cert_filter_oid = defaultOptions.certOidfilter;

		param.certOidfilter = defaultOptions.certOidfilter;
		param.certExpirefilter = defaultOptions.certExpirefilter;
		
		var funcName = param.signType;
		var option = null;
		
		var request = MakeRequestJsonMessage(funcName, param, option );
		
		addEventLisner( callback );
		ML_sendPostMessage( request );
	}
	
	function closeDialog(event){
		$('#dscertContainer').hide();
		
		var obj = JSON.parse( event.data );
		if( obj.key  == 'closeDialog'){
			$('#dscertContainer').hide();
			/*취소버튼 누르고 event 받고 싶을 때 code 값 준다
			obj.code = 999;
			magiclineApi.callback( obj.code , obj );*/
		}else if( obj.key  == 'closeEZDialog'){
			$('#dscertContainer').hide();
		}else if( obj.resultMsg != null && obj.resultMsg !== "" ){
			magiclineApi.callback( obj.code , obj.resultMsg );	
		}else if(obj.opcode != null && obj.opcode !== ""){
			magicmrsApi.callback(obj);
		}else{
			magiclineApi.callback( obj.code , obj );
		}
	}
	
	function addEventLisner( callback ){
		if(window.addEventListener){
			window.addEventListener("message",closeDialog, false);
		}else if(window.attachEvent){
			window.attachEvent("onmessage", closeDialog );
		}
	}
	
	function ML_sendPostMessage ( requestStr ){
		
		var dialogTitle = "전자서명";
		$('#dscertContainer').show();
		var iframeWindow = document.getElementById('dscert').contentWindow;
		
		iframeWindow.postMessage(requestStr, mlMainUrl);
	}
	
	function ML_sendUtilMessage( requestStr ){
		
		var iframeWindow = document.getElementById('dscert').contentWindow;		
		iframeWindow.postMessage(requestStr, mlMainUrl);
	}
	
	function ML_funProcInitCheck (callback){
		magiclineApi.callback = callback;
		var childUrl = mlMainUrl + mlDirPath + childHtml +"?lgUrl="+cpUrl+"&random=" + Math.random() * 99999;
		$('#dscert').attr("src", childUrl);
		addEventLisner( callback );
	}
	
	function completeInit(){
		magicline.is_ML_Sign_Init = true;
		if( typeof magicline.initCallback == "function" ){
			magicline.initCallback(0, 'completeInit');
		}
		$.unblockUI();
	}
	
	function ML_checkInit(){
		setTimeout(function(){
			if( magicline.is_ML_Sign_Init ){
				$.unblockUI();
			}else{
				ML_checkInit();
			}
		},1500);
	}
	
	/*
	 * jquery 충돌로 인해 blockUI를 업무페이지에서 사용하기 위한 함수
	 */
	function blockUI(){
		$.blockUI({
			message:'<div><div><img src="' + mlDirPath + 'UI/images/loader.gif" alt="로딩중입니다."/></div><p style="display:inline-block; padding-top:4px; font-size:11px; color:#333; font-weight:bold;">잠시만 기다려 주세요.</p></div>',
			css:{left:(($(window).width()/2)-75)+'px'}
		});
	}
	
	function checkInstall(callback){
		magiclineApi.callback = callback;
		
		if(magicline.is_ML_Sign_Init){
			var param = null;
			var option = null;
			var request = MakeRequestJsonMessage("checkInstall", param, option);
			ML_sendUtilMessage(request);
		}else{
			alert("초기화 중입니다. 잠시 후 다시 시도해 주세요.");
		}
	}
	
	function genHash(algorithm, msg, callback){
		magiclineApi.callback = callback;
		
		if(magicline.is_ML_Sign_Init){
			var param = {};
			param.algorithm = algorithm;
			param.msg = msg;
			
			var option = null;
			var request = MakeRequestJsonMessage("genHash", param, option);
			ML_sendUtilMessage(request);
		}else{
			alert("초기화 중입니다. 잠시 후 다시 시도해 주세요.");
		}
	}
	
	return {
		MakeSignData:MakeSignData,
		MakeEZSignData:MakeEZSignData, //211015 간편인증 추가
		MakePdfSignData:MakePdfSignData,
		MakeSignatureData:MakeSignatureData,
		MakeAddSignData:MakeAddSignData,		
		MakeRequestJsonMessage:MakeRequestJsonMessage,
		ML_sendUtilMessage : ML_sendUtilMessage,
		ML_funProcInitCheck : ML_funProcInitCheck,
		completeInit:completeInit,
		ML_checkInit:ML_checkInit,
		saveCertToStorage:saveCertToStorage,
		getSelectCert:getSelectCert,
		MakeHashSignData:MakeHashSignData,
		/* NTS */
		ntsCertAuth:ntsCertAuth,
		keyBoardSecurityUse:keyBoardSecurityUse,
		tranx2PEM:tranx2PEM,
		getRandomfromPrivateKey:getRandomfromPrivateKey,
		getSignDN:getSignDN,
		signatureData:signatureData,
		setSessionID:setSessionID,		
		blockUI:blockUI,
		checkInstall:checkInstall,
		genHash:genHash,
		makeUCPIDRequestInfo:makeUCPIDRequestInfo,
		getCertManager:getCertManager
	}
}

magicline.uiapi = new magiclineApi();
var readLength = function(b) {

	  var b2 = b.getByte();
	  if(b2 === 0x80) {
	    return undefined;
	  }

	  // see if the length is "short form" or "long form" (bit 8 set)
	  var length;
	  var longForm = b2 & 0x80;
	  if(!longForm) {
	    // length is just the first byte
	    length = b2;
	  } else {
	    // the number of bytes the length is specified in bits 7 through 1
	    // and each length byte is in big-endian base-256
	    length = b.getInt((b2 & 0x7F) << 3);
	  }
	  return length;
	}


var readValue = function(tag, bytes) {

	if(bytes.length() < 2) {
	    throw new Error('Too few bytes to parse DER.');
	  }

	if (bytes.getByte() != tag) {
	           throw new Error('Invalid format.'); } 
	var length = readLength (bytes); return bytes.getBytes(length); 
}
function getSignedData(sigDataBase64){
	magicjs.init();
	var sigData = sigDataBase64;
	var decoded = magicline.base64Util.decode64(sigData);  // Base64 인코딩되어있는 피노텍 데이터.
	var buff = magicjs.ByteStringBuffer.create(decoded);
	readValue(0x30, buff); // 
	var signedData = decoded.slice(0, buff.read);  // SignedData 획득 부분입니다.
	signedData = magicline.base64Util.encode64(signedData);
	var fileName = magicjs.utf8.decode(readValue(0x0C, buff)); 
	var fileGenTime = dreamsecurity.asn1.utcTimeToDate(readValue(0x17, buff)); 
	var pdfFile = readValue(0x04, buff);
	document.reqForm.signData.value= magicline.base64Util.encode64(pdfFile);
	if(document.getElementById("addSigner").value == null || document.getElementById("addSigner").value == ""){
		document.reqForm.addSigner.value = signedData;
	}
}
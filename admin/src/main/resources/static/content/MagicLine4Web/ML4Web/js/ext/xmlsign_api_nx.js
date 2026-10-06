// 로컬
var xml_cpUrl = location.port != ""?location.hostname+":"+location.port:location.hostname;
var xml_mlMainUrl = location.protocol+"//"+( location.port != ""?location.hostname+":"+location.port:location.hostname );
var xml_mlDirPath = "/MagicLine4Web/ML4Web/";
var xml_childHtml = "Child.html";		

$(document).ready(function(){	
	$('#dscertContainer').hide();
	
	$.blockUI({
		message:'<div><div><img src="' + xml_mlDirPath + 'UI/images/loader.gif" alt="로딩중입니다."/></div><p style="display:inline-block; padding-top:4px; font-size:11px; color:#333; font-weight:bold;">잠시만 기다려 주세요.</p></div>',
		css:{left:(($(window).width()/2)-75)+'px'}
	});
	
	
	magicline.xsnxapi.ML_funProcInitCheck(function(code,data){
		if( code == 0 ){
			magicline.xsnxapi.completeInit();
		}
	});
	
	magicline.xsnxapi.ML_checkInit();

});

if(typeof(magicline) == 'undefined'){
	magicline = {};
}

var magiclineXMLApi = function(){
	var callback = "";
	var defaultOptions = {
			sign:{signType:"MakeSignData",msg:"",messageType:"",signOpt:{ds_pki_sign:['OPT_USE_CONTNET_INFO'], ds_pki_rsa:'rsa15', ds_pki_hash:'sha256',ds_msg_decode:"false",ds_pki_sign_type:"signeddata"}},
			signPdfOpt:{ds_pki_sign:['OPT_USE_CONTNET_INFO','OPT_USE_PKCS7','OPT_NO_CONTENT','OPT_HASHED_CONTENT'], ds_pki_rsa:'rsa15', ds_pki_hash:'sha256',ds_msg_decode:"true"},
			encOpt:{ds_pki_rsa:'rsa15'},
			signedenvOpt:{ds_pki_sign:['OPT_USE_CONTNET_INFO'], ds_pki_rsa:'rsa15', ds_pki_algo:'SEED-CBC'},
			// 추가
			idn : "",
			vidType : "",
			certOidfilter:"", //1.2.410.100001.2.2.1,1.2.410.200005.1.1.4
			certExpirefilter:true
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
	
	function closeDialog(event){
		$('#dscertContainer').hide();
		
		var obj = JSON.parse( event.data );
		if( obj.key  == 'closeDialog'){
			$('#dscertContainer').hide();
		}else if( obj.resultMsg != null && obj.resultMsg !== "" ){
			magiclineXMLApi.callback( obj.code , obj.resultMsg );	
		}/*else if(obj.opcode != null && obj.opcode !== ""){
		magicmrsApi.callback(obj);
		}*/else{
			magiclineXMLApi.callback( obj.code , obj );	
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
		
		iframeWindow.postMessage(requestStr, xml_mlMainUrl);
	}
	
	function ML_sendUtilMessage( requestStr ){
		var iframeWindow = document.getElementById('dscert').contentWindow;		
		iframeWindow.postMessage(requestStr, xml_mlMainUrl);
	}
	
	function ML_funProcInitCheck (callback){
		magiclineXMLApi.callback = callback;
		var childUrl = xml_mlMainUrl + xml_mlDirPath + xml_childHtml +"?lgUrl="+ xml_cpUrl + "&random=" + Math.random() * 99999;
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
	
	function Init(callback){
		magiclineXMLApi.callback = callback;
		
		if(magicline.is_ML_Sign_Init){
			
			var param = {};
			//추가 default option
			param.STORAGELIST					= ["web","hdd","token","mobile","unisign","smartcert"];
			param.STORAGESELECT					= "web";
			param.USEVIRTUALKEYBOARD			= false; 	//가상키보드 사용 여부 true,false
			param.VIRTUALKEYBOARDTYPE			= ""; 	//가상키보드 사용 여부 NSHC,RAON,INCA
			param.WinClientVersion 				= "1.0.0.14";
			param.MacClientVersion 				= "1.0.0.13";
			param.Lin64ClientVersion 			= "1.0.0.12";
			param.Lin32ClientVersion 			= "1.0.0.12";
			param.MobileOption					= ["ubikey", "mobisign"];
			
			var option = null;
			var request = MakeRequestJsonMessage("setUserOption", param, option);
			
			ML_sendUtilMessage(request);
			
		}else{
			alert("초기화 중입니다. 잠시 후 다시 시도해 주세요.");
		}
		
	}
	
	function MakeXMLDSIG(msg, callback){
		var XS_gOidPolicy = "";    // OID에 해당하는 인증서만 출력하도록 프로퍼티 설정 ("," = 구분자) 
		//###금융결제원 - 2개
		XS_gOidPolicy += "1.2.410.200005.1.1.5,"          ;
		XS_gOidPolicy += "1.2.410.200005.1.1.6.8,"        ;

		//###한국증권전산 - 1개
		XS_gOidPolicy += "1.2.410.200004.5.1.1.7,"    ;

		//###한국전산원 - 2개
		XS_gOidPolicy += "1.2.410.200004.5.3.1.1,"        ;
		XS_gOidPolicy += "1.2.410.200004.5.3.1.2,"        ;

		//###한국정보인증 - 1개
		XS_gOidPolicy += "1.2.410.200004.5.2.1.1,"      ;
		//2009.12.07추가 - 24개(한국정보인증)
		XS_gOidPolicy += "1.2.410.200004.5.2.1.3,"      ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.5,"      ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.5.12,"   ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.5.28,"   ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.5.30,"   ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.5.32,"   ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.114,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.115,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.183,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.229,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.237,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.245,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.246,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.250,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.251,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.254,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.48,"   ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.70,"   ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.73,"   ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.90,"   ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.93,"   ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.99,"   ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.257,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.255,"  ;
		//2009.12.18추가 - 4개(한국정보인증)
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.65,"   ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.221,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.249,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.252,"  ;
		//2010.9.27추가 - 7개(한국정보인증)
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.248,"   ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.275,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.235,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.279,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.113,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.284,"  ;
		XS_gOidPolicy += "1.2.410.200004.5.2.1.6.285,"  ;

		//###한국전자인증 - 1개
		XS_gOidPolicy += "1.2.410.200004.5.4.1.2,"      ;
		//2009.12.07추가 - 17개(한국전자인증)
		XS_gOidPolicy += "1.2.410.200004.5.4.1.19,"     ;
		XS_gOidPolicy += "1.2.410.200004.5.4.1.31,"     ;
		XS_gOidPolicy += "1.2.410.200004.5.4.1.35,"     ;
		XS_gOidPolicy += "1.2.410.200004.5.4.1.50,"     ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.11,"     ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.60,"     ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.65,"     ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.67,"     ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.69,"     ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.300,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.302,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.304,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.305,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.306,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.307,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.308,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.310,"    ;
		//2009.12.28추가 - 10개(한국전자인증)
		XS_gOidPolicy += "1.2.410.200004.5.4.2.64,"     ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.66,"     ;
		XS_gOidPolicy += "1.2.410.200004.5.4.1.14,"     ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.42,"     ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.36,"     ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.25,"     ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.312,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.311,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.55,"     ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.313,"    ;
		//2010.02.05추가 - 2개(한국전자인증)
		XS_gOidPolicy += "1.2.410.200004.5.4.2.315,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.317,"    ;
		//2010.05.06추가 - 13개(한국전자인증)
		XS_gOidPolicy += "1.2.410.200004.5.4.2.318,"    ;   //20100506
		XS_gOidPolicy += "1.2.410.200004.5.4.2.321,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.322,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.323,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.324,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.325,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.326,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.327,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.328,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.329,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.330,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.331,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.332,"    ;
		//2010.05.06추가 - 2개(한국전자인증)
		XS_gOidPolicy += "1.2.410.200004.5.4.2.57,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.50,"    ;
		//2010.06.10추가 - 2개(한국전자인증)
		XS_gOidPolicy += "1.2.410.200004.5.4.2.80,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.337,"    ;
		//2010.10.07추가 - 1개(한국전자인증)
		XS_gOidPolicy += "1.2.410.200004.5.4.2.340,"    ;
		//2010.11.18추가 - 1개(한국전자인증)
		XS_gOidPolicy += "1.2.410.200004.5.4.2.343,"    ;
		//2010.12.20추가 - 1개(한국전자인증)
		XS_gOidPolicy += "1.2.410.200004.5.4.2.350,"    ;
		//2011.03.02추가 - 1개(한국전자인증)
		XS_gOidPolicy += "1.2.410.200004.5.4.2.360,"    ;
		//2011.06.13추가 - 1개(한국전자인증)
		XS_gOidPolicy += "1.2.410.200004.5.4.2.367,"    ;
		//2012.06.13추가 - 2개(한국전자인증)
		XS_gOidPolicy += "1.2.410.200004.5.4.2.377,"    ;
		XS_gOidPolicy += "1.2.410.200004.5.4.2.379,"    ;

		//###한국무역정보통신 - 1개
		XS_gOidPolicy += "1.2.410.200012.1.1.3,"        ;
		//2009.12.01추가 - 14개(한국무역정보통신)
		XS_gOidPolicy += "1.2.410.200012.5.19.1.1,"     ;
		XS_gOidPolicy += "1.2.410.200012.5.13.1.1,"     ;
		XS_gOidPolicy += "1.2.410.200012.5.1.1.171,"    ;
		XS_gOidPolicy += "1.2.410.200012.5.6.1.31,"     ;
		XS_gOidPolicy += "1.2.410.200012.1.1.411,"      ;
		XS_gOidPolicy += "1.2.410.200012.5.11.1.81,"    ;
		XS_gOidPolicy += "1.2.410.200012.5.21.1.11,"    ;
		XS_gOidPolicy += "1.2.410.200012.5.20.1.21,"    ;
		XS_gOidPolicy += "1.2.410.200012.5.1.1.191,"    ; //20100506 변경
		XS_gOidPolicy += "1.2.410.200012.1.1.801,"      ;
		XS_gOidPolicy += "1.2.410.200012.1.1.9,"        ;
		XS_gOidPolicy += "1.2.410.200012.5.18.1.21,"    ;
		XS_gOidPolicy += "1.2.410.200012.5.17.1.11,"    ;
		XS_gOidPolicy += "1.2.410.200012.5.15.1.11,"    ;
		//2009.12.22추가 - 8개(한국무역정보통신)
		XS_gOidPolicy += "1.2.410.200012.5.4.1.61,"     ;
		XS_gOidPolicy += "1.2.410.200012.1.1.431,"      ;
		XS_gOidPolicy += "1.2.410.200012.1.1.441,"      ;
		XS_gOidPolicy += "1.2.410.200012.1.1.451,"      ;
		XS_gOidPolicy += "1.2.410.200012.1.1.401,"      ;
		XS_gOidPolicy += "1.2.410.200012.5.1.1.321,"    ;
		XS_gOidPolicy += "1.2.410.200012.5.1.1.281,"    ;
		XS_gOidPolicy += "1.2.410.200012.5.1.1.331,"    ;
		//2010.02.22추가 - 3개(한국무역정보통신)
		XS_gOidPolicy += "1.2.410.200012.5.27.1.1,"     ;
		XS_gOidPolicy += "1.2.410.200012.5.26.1.11,"    ;
		XS_gOidPolicy += "1.2.410.200012.1.1.471,"      ;
		//2010.03.04추가 - 1개(한국무역정보통신)
		XS_gOidPolicy += "1.2.410.200012.5.4.1.21,"     ;
		//2010.05.06추가 - 7개(한국무역정보통신)
		XS_gOidPolicy += "1.2.410.200012.1.1.4101,"     ;    //20100506 7개 추가
		XS_gOidPolicy += "1.2.410.200012.1.1.481,"      ;
		XS_gOidPolicy += "1.2.410.200012.1.1.491,"      ;
		XS_gOidPolicy += "1.2.410.200012.1.1.4111,"     ;
		XS_gOidPolicy += "1.2.410.200012.5.14.1.11,"    ;
		XS_gOidPolicy += "1.2.410.200012.5.14.1.21,"    ;                               
		XS_gOidPolicy += "1.2.410.200012.5.11.1.101,"   ; 
		//2011.04.15추가 - 3개(한국무역정보통신)
		XS_gOidPolicy += "1.2.410.200012.5.28.1.21,"   ;
		XS_gOidPolicy += "1.2.410.200012.5.18.1.31,"   ;
		XS_gOidPolicy += "1.2.410.200012.5.18.1.33,"   ;
		//2011.06.13추가 - 1개(한국무역정보통신)
		XS_gOidPolicy += "1.2.410.200012.1.1.4151,"     ;
		//2014.05.16 추가
		XS_gOidPolicy += "1.2.410.200012.5.4.1.141,"     ;

		// 아랫부분의 주석처리된 부분은 추가하지 말라는 이현진 조사관님의 지시로 막음. (2015.02.26)
		//발행허용 HOMETAX 인증서
		// 한국증권전산
		//XS_gOidPolicy += "1.2.410.200004.5.1.1.12;"                     ;
		// 한국정보인증
		XS_gOidPolicy += "1.2.410.200004.5.2.1.5001,"                    ;
		// 한국전자인증
		//XS_gOidPolicy += "1.2.410.200004.5.4.2.52;"                     ;

		// HOMETAX 사업자번호 인증서
		//한국증권전산
		//XS_gOidPolicy += "1.2.410.200004.5.1.1.10;"                     ;
		//XS_gOidPolicy += "1.2.410.200004.5.1.1.12;"                     ;
		// 금융결제원
		//XS_gOidPolicy += "1.2.410.200005.1.1.2;"             ;
		// 한국전산원
		//XS_gOidPolicy += "1.2.410.200004.5.3.1.5;"                      ;
		// 한국전자인증
		//XS_gOidPolicy += "1.2.410.200004.5.4.2.52;"                     ;
		
		//XS_gOidPolicy = XS_gOidPolicy.replace(/\;/g,",");
		
		magiclineXMLApi.callback = callback;
		
		var param = defaultOptions.sign;
		param.signType = "XMLSignature";
		
		param.signOpt.ds_pki_sign_type		= "sign";
		param.signOpt.ds_pki_xmlsign_type	= "1";
		
		if( msg!=null && typeof(msg)!='undefined' && msg!='' ){
			param.msg = msg;
		}
		
		param.signOpt.cert_filter_oid = XS_gOidPolicy;
		param.signOpt.cert_filter_expire = defaultOptions.certExpirefilter;

		param.certOidfilter = XS_gOidPolicy;
		param.certExpirefilter = defaultOptions.certExpirefilter;
		
		var funcName = param.signType;
		var option = null;
		
		var request = MakeRequestJsonMessage(funcName, param, option );
		
		addEventLisner( callback );
		ML_sendPostMessage( request );
	}
	
	function MakeXMLEncryption(kmCert, msg, callback){
		magiclineXMLApi.callback = callback;
		
		if(magicline.is_ML_Sign_Init){
		
			var param = {};
			param.layer	 = "UI";
			param.kmCert = kmCert;
			param.msg 	 = msg;			
			var option = null;
			
			var request = MakeRequestJsonMessage("MakeXMLEncryption", param, option);
			ML_sendUtilMessage(request);			
		}else{
			alert("초기화 중입니다. 잠시 후 다시 시도해 주세요.");
		}
	}
	
	function GetRandom(kmCert, callback){
		magiclineXMLApi.callback = callback;
		
		if(magicline.is_ML_Sign_Init){
		
			var param = {};
			param.layer = "UI";
			param.kmCert = kmCert;
			var option = null;
			
			var request = MakeRequestJsonMessage("getVIDRandom", param, option);
			ML_sendUtilMessage(request);
			
		}else{
			alert("초기화 중입니다. 잠시 후 다시 시도해 주세요.");
		}
	}
	
	return{
		Init:Init,
		MakeXMLDSIG:MakeXMLDSIG,
		MakeXMLEncryption:MakeXMLEncryption,
		GetRandom:GetRandom,
		MakeRequestJsonMessage:MakeRequestJsonMessage,
		ML_sendUtilMessage : ML_sendUtilMessage,
		ML_funProcInitCheck : ML_funProcInitCheck,
		completeInit:completeInit,
		ML_checkInit:ML_checkInit,
	}
}

magicline.xsnxapi = new magiclineXMLApi(); 
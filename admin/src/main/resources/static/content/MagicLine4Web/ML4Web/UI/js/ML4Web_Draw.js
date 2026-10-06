var ML4WebDraw = {
	selectMedia : "",
/* 휴대전화 인증서 목록 이름 정의(인증서 선택창에서 휴대전화 선택시 노출되는 목록)
 */
//--------------------------
	TITLE_MOB_SVC_UBIKEY : $.i18n.prop("TS064"),
	TITLE_MOB_SVC_UBIKEYNX : $.i18n.prop("TS096"),
	TITLE_MOB_SVC_MOBSIGN : $.i18n.prop("TS065"),
	TITLE_MOB_SVC_DREAMCS : $.i18n.prop("TS066"),
//--------------------------
/* 각 스토리지별 CS 설치 진행 메시지 정의
 */
//-------------------------
	MSG_MOB_INSTALL     : $.i18n.prop("ES033"),
	MSG_MOB_CANCEL      : $.i18n.prop("ES034"),
	MSG_MOB_UPDATE      : $.i18n.prop("ES035"),

	MSG_SMT_INSTALL     : $.i18n.prop("ES036"),
	MSG_SMT_FAIL        : $.i18n.prop("ES037"),

	MSG_MOB_SIGN_INSTALL : $.i18n.prop("ES038"),
	MSG_MOB_SIGN_CANCEL  : $.i18n.prop("ES039"),
	MSG_MOB_SIGN_UPDATE  : $.i18n.prop("ES040"),
//-------------------------
/* 경고 메시지 정의
 */
//-------------------------
	MSG_ALERT_LOWER_IE9 : $.i18n.prop("ES041"),
//-------------------------

/* 스토리지 선책창을 만드는 HTML 태그들을 정의 
 * '.value.' 부분은 스크립트에서 동적으로 처리
 * */
//------------------------
	// web
	STG_HTML_WEB      : "<li class='ML_storage_browser'><a href='#' id='.id_key.' stgIdx='.idx.' class='stg_01' title='브라우저'><span id='MSG_TS701'></span></a></li>",
	//pfx
	STG_HTML_PFX      : "<li class='ML_storage_sch'><a href='#' id='.id_key.' stgIdx='.idx.' class='stg_01'><span id='MSG_TS702'></span></a></li>",
	STG_HTML_PFX_IE   : "<li class='ML_storage_sch'><a href='#' id='.id_key.' stgIdx='.idx.'><span id='MSG_TS702' class='stg_01'></span></a></li>",
	STG_HTML_PFX_INS  : "<li class='ML_storage_sch ins'><a href='#' stgIdx='.idx.' onClick='installCheck(&#34;.mode.&#34;)'><span id='MSG_TS702' class='stg_01'></span></a></li>",
	//hdd
	STG_HTML_HDD      : "<li class='ML_storage_hdd'><a href='#' id='.id_key.' stgIdx='.idx.' class='stg_01' title='하드디스크/이동식'><span id='MSG_TS703'></span></a></li>",
	STG_HTML_HDD_INS  : "<li class='ML_storage_hdd ins'><a href='#' stgIdx='.idx.' onClick='installCheck(&#34;.mode.&#34;)' class='stg_01' title='하드디스크/이동식'><span id='MSG_TS703'></span></a></li>",
	//shdd
	STG_HTML_SDD      : "<li class='ML_storage_shdd'><a href='#' id='.id_key.' stgIdx='.idx.' class='stg_01'><span id='MSG_TS704'></span></a></li>",
	STG_HTML_SDD_INS  : "<li class='ML_storage_shdd ins'><a href='#' stgIdx='.idx.' onClick='installCheck(&#34;.mode.&#34;)' class='stg_01'><span id='MSG_TS704'></span></a></li>",
	//token
	STG_HTML_TKN      : "<li class='ML_storage_token'><a href='#' id='.id_key.' stgIdx='.idx.' class='stg_01' title='보안토큰'><span id='MSG_TS705'></span></a></li>",
	STG_HTML_TKN_INS  : "<li class='ML_storage_token ins'><a href='#' stgIdx='.idx.' onClick='installCheck(&#34;.mode.&#34;)' class='stg_01' title='보안토큰'><span id='MSG_TS705'></span></a></li>",
	//mobile
	STG_HTML_MOB      : "<li class='ML_storage_phone'><a href='#' id='.id_key.' stgIdx='.idx.' class='stg_01' title='휴대전화'><span id='MSG_TS706'></span></a></li>",
	STG_HTML_MOB_INS  : "<li class='ML_storage_phone ins'><a href='#' stgIdx='.idx.' onClick='installCheck(&#34;.mode.&#34;);' class='stg_01' title='휴대전화'><span id='MSG_TS706'></span></a></li>",
	//smartcert 
	STG_HTML_SMC      : "<li class='ML_storage_smartcert'><a href='#' id='.id_key.' stgIdx='.idx.' class='stg_01' title='스마트인증'><span id='MSG_TS707'></span></a></li>",
	STG_HTML_SMC_INS  : "<li class='ML_storage_smartcert ins'><a href='#' stgIdx='.idx.' onClick='installCheck(&#34;.mode.&#34;);' class='stg_01' title='스마트인증'><span id='MSG_TS707'></span></a></li>",
	STG_HTML_SMC_MGMT : "<li class='ML_storage_smartcert off'><a href='#' style='cursor:default;' stgIdx='.idx.' class='stg_01'><span id='MSG_TS707'></span></a></li>",
	//smartcertnx 
	STG_HTML_SMCNX	  : "<li class='ML_storage_smartcert'><a href='#' id='.id_key.' stgIdx='.idx.' class='stg_01' title='스마트인증'><span id='MSG_TS707'></span></a></li>",
	//cloud
	STG_HTML_CLD      : "<li class='ML_storage_cloud'><a href='#' id='.id_key.' stgIdx='.idx.' class='stg_01' title='클라우드'><span id='MSG_TS708'></span></a></li>",
	STG_HTML_CLD_INS  : "<li class='ML_storage_cloud ins'><a href='#' stgIdx='.idx.' onClick='installCheck(&#34;.mode.&#34;);' class='stg_01' title='클라우드'><span id='MSG_TS708'></span></a></li>",
	//unisign
	STG_HTML_UNI      : "<li class='ML_storage_unisign'><a href='#' id='.id_key.' stgIdx='.idx.' class='stg_01' title='클라우드'><span id='MSG_TS708'></span></a></li>",
	STG_HTML_UNI_INS  : "<li class='ML_storage_unisign ins'><a href='#' stgIdx='.idx.' onClick='installCheck(&#34;.mode.&#34;);' class='stg_01' title='클라우드'><span id='MSG_TS708'></span></a></li>",
//--------------------------


/* UI 스크립트를 여기서 일괄 호출 
 * 기존 InitEventHadnler() 는  ML4Web_Main.js 에서 처리
 * */
	initDraw : function() {
		
		// 인증서 선택 창 그리기
		ML4WebDraw._createWindow();
		
		// 메시지 초기화
		MessageVO.applyMessage($('[id^="MSG_"]'));
		
		//key event 
		$(this).keydown(function(e){
			if(e.keyCode == "9"){
				var targetElement = $(document.activeElement).attr("id");
				var parentElement = $(document.activeElement).parent().attr("id");
				//console.log("targetElement.id === " + targetElement);
				//console.log("parentElement.id === " + parentElement);
	
				if(targetElement == "stg_smartcert" ){
					var selection = $("#dataTable").MLjquiDataTable('getSelection');
					if(selection && selection.length>0){

					}else{

					}
				} else if(targetElement == "btn_cancel" ){
					$(".ML_storage_area>ul>li:first-child").focus();
				} else if(targetElement == "btn_file_cs" ){
					$("#import_pfx_password").focus();
				} else if(targetElement == "file_route" ){
	
				} else if(targetElement == null || targetElement == 'undefined'){
					
				}
			}else if (e.keyCode == "27"){
				console.log("Esc keydown Esc keydown Esc keydown Esc keydown");
			}
		});
	
		// Slide L&R Button
		$(".customNavigation").click(function(){
			var X=$(this).attr('id');
			if(X==1){
				$(this).children(".str_add_li").removeClass('pr');
				$(this).children(".str_add_li").addClass('ne');
				$(this).attr('id', '0');
	
				$("#wrap_stg_01").animate({left:"0px"}, 250);
				$("#wrap_stg_02").animate({left:"375px"}, 250);
			}else{
				$(".str_add_li").attr('class', 'str_add_li pr');
				$(this).children(".str_add_li").removeClass('ne');
				$(this).children(".str_add_li").addClass('pr');
				$(".customNavigation").attr('id', '0');
				$(this).attr('id', '1');
				
				$("#wrap_stg_01").animate({left:"-375px"}, 250);
				$("#wrap_stg_02").animate({left:"0px"}, 250);
			}
		});
	
		$(".stg_01").focus(function(){
			$("#wrap_stg_01").animate({left:"0px"}, 250);
			$("#wrap_stg_02").animate({left:"375px"}, 250);
			$(".customNavigation").children(".str_add_li").removeClass('pr');
			$(".customNavigation").children(".str_add_li").addClass('ne');
			$(".customNavigation").attr('id', '0')
		});
	
		$(".stg_02").focus(function(){
			$("#wrap_stg_01").animate({left:"-375px"}, 250);
			$("#wrap_stg_02").animate({left:"0px"}, 250);
			$(".customNavigation").children(".str_add_li").removeClass('ne');
			$(".customNavigation").children(".str_add_li").addClass('pr');
			$(".customNavigation").attr('id', '1')
		});
	},
	loadCSS : function(){
		var unisign = false;
		var os_ver = ML4WebApi.getProperty("os");
		var dt_height = "";
		var isMobile = false;
		var currentOS = magiclineUtil.getOS();
		if(os_ver=="Android" || os_ver=="IPHONE" || os_ver=="BlackBerry" || os_ver=="IPAD"){
			isMobile = true;
			
			if(ML4WebApi.getProperty("storageList_m").indexOf("smartcertnx") > -1){
				if(!document.getElementById('smartcertnx')){
					var head = document.getElementsByTagName('head')[0];
					var script = document.createElement('script');
					script.type= 'text/javascript';
					script.id = "smartcertnx";
					var webSmartCertURL = ML4WebApi.getProperty("web_smartcert_url");
					if(webSmartCertURL == null || typeof(webSmartCertURL) == 'undefined' || webSmartCertURL == ""){
						script.src = "https://cdn.smartcert.kr/SmartCertWeb/API/js/jSmartCertNP2.js";
					}else{
						script.src = webSmartCertURL;
					}
					head.appendChild(script);
				}
			}
		}
		for( var i = 0; i < ML4WebApi.webConfig.storageList.length; i++){
			if( ML4WebApi.webConfig.storageList[i] === "unisign" ){
				unisign = true;
				if (document.createStyleSheet)
				{
					document.createStyleSheet(contextPath + '/MagicLine4Web/ML4Web/UI/css/ML_common_nts.css?v=1');	
				    document.createStyleSheet(contextPath + '/PFH/css/comm/cloudsign/certcommon.css?v=1');
				}else{
					if( isMobile ){					
						$("#ML_common_css").attr("href", "UI/css/ML_common_mobile.css");						
						$("#MLjquibase_css").attr("href", "UI/css/MLjquibase_mobile.css");						
					}else{
						$('head').append( $('<link rel="stylesheet" type="text/css" />').attr('href', contextPath + '/MagicLine4Web/ML4Web/UI/css/ML_common_nts.css?v=1' ) );
					}
					
					$('head').append( $('<link rel="stylesheet" type="text/css" />').attr('href', contextPath + '/PFH/css/comm/cloudsign/certcommon.css?v=1' ) );	
				}
				
				$.cachedScript( contextPath + "/PFH/js/comm/cloudsign/unisignweb/js/unisignwebclient.js?v=1" ).done(function(){
					$.cachedScript( contextPath + "/PFH/unisignweb_document/UniSignWeb_Multi_Init_PluginFree.js?v=1" ).done(function(){
						$.cachedScript( contextPath + "/PFH/js/comm/cloudsign/unisignweb/js/functions.js?v=1" ).done(function(){
							
						});
					});
				});				
			}else if( ML4WebApi.webConfig.storageList[i] === "smartcertnx" ) {
				if(!document.getElementById('smartcertnx')){
					var head = document.getElementsByTagName('head')[0];
					var script = document.createElement('script');
					script.type= 'text/javascript';
					script.id = "smartcertnx";
					var webSmartCertURL = ML4WebApi.getProperty("web_smartcert_url");
					if(webSmartCertURL == null || typeof(webSmartCertURL) == 'undefined' || webSmartCertURL == ""){
						script.src = "https://cdn.smartcert.kr/SmartCertWeb/API/js/jSmartCertNP2.js";
					}else{
						script.src = webSmartCertURL;
					}
					head.appendChild(script);
				}
			} else if( ML4WebApi.webConfig.storageList[i] === "mobile" ) {
				if(ML4WebApi.MobileOption.indexOf("ubikeynx") > -1){
					if(!document.getElementById("ubikeynx")){
						var head = document.getElementsByTagName('head')[0];
						var script = document.createElement('script');
						script.type= 'text/javascript';
						script.id = "ubikeynx";
						var csUbikeyURL = ML4WebApi.getProperty("cs_ubikey_url");
						if(csUbikeyURL == null || typeof(csUbikeyURL) == 'undefined' || csUbikeyURL == ""){
							script.src = "/UBikeyWeb/js/infovineHTML.js";
						}else{
							script.src = csUbikeyURL;
						}
						head.appendChild(script);
					}
				}
			}
		}
		if( unisign == false ){
			if (document.createStyleSheet){
				document.createStyleSheet('/content/MagicLine4Web/ML4Web/UI/css/ML_common.css?v=1' );
			}else{
				if( isMobile ){
					$("#ML_common_css").attr("href", "UI/css/ML_common_mobile.css");						
					$("#MLjquibase_css").attr("href", "UI/css/MLjquibase_mobile.css");		
				}else{
					$('head').append( $('<link rel="stylesheet" type="text/css" />').attr('href', '/content/MagicLine4Web/ML4Web/UI/css/ML_common.css?v=1' ) );
				}
			}
		}
		
		if(ML4WebApi.getProperty("storageList").indexOf("web_kftc") > -1 || ML4WebApi.getProperty("storageList").indexOf("kftc") > -1 || ML4WebApi.getProperty("storageList").indexOf("web") > -1){
			if( ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
				if (document.createStyleSheet){
					document.createStyleSheet(contextPath + '/raonsecure/transkey/transkey.css');
				}else{
					$('head').append( $('<link rel="stylesheet" type="text/css" />').attr('href', contextPath + '/raonsecure/transkey/transkey.css' ) );
				}
				$.cachedScript( contextPath + "/raonsecure/transkey/rsa_oaep_files/rsa_oaep-min.js" ).done(function(){
					$.cachedScript( contextPath + "/raonsecure/transkey/jsbn/jsbn-min.js" ).done(function(){
						$.cachedScript( contextPath + "/raonsecure/transkey/TranskeyLibPack_op.js" ).done(function(){
							$.cachedScript( contextPath + "/raonsecure/transkey/transkey.js" ).done(function(){
								if(typeof initTranskey=="function") initTranskey();				
							});
						});
					});
				});
			}else if( ML4WebApi.webConfig.virtualKeyboardType === "NSHC" ){	
				$.cachedScript( contextPath + "/nfilter/js/include_open_nFilter.js" ).done(function(){
					initializedNFilter();	
				});
			}else if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
				if(!magiclineUtil.isMobile(magiclineUtil.getOS())){
					$.cachedScript( contextPath + "/pluginfree/js/nppfs-1.11.0.js" ).done(function(){
						npPfsCtrl.hideLoading();
						npPfsStartup(null, false, false, false, true, "npkencrypt", "on");
					});
				}
			}else if( ML4WebApi.webConfig.virtualKeyboardType === "DREAM" ){
				if (document.createStyleSheet){
					document.createStyleSheet(contextPath + '/magicVKeypad/magicVKeypad.css');
				}else{
					$('head').append( $('<link rel="stylesheet" type="text/css" />').attr('href', contextPath + '/magicVKeypad/magicVKeypad.css' ) );
				}
				
				$.cachedScript( contextPath + "/magicVKeypad/magicVKeypad.js" ).done(function(){
					ML4WebVKey.init();
				});
			}
		}else{
			if( ML4WebApi.webConfig.useVirtualKeyboard ){
				if( ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
					if (document.createStyleSheet){
						document.createStyleSheet(contextPath + '/raonsecure/transkey/transkey.css');
					}else{
						$('head').append( $('<link rel="stylesheet" type="text/css" />').attr('href', contextPath + '/raonsecure/transkey/transkey.css' ) );
					}
					$.cachedScript( contextPath + "/raonsecure/transkey/rsa_oaep_files/rsa_oaep-min.js" ).done(function(){
						$.cachedScript( contextPath + "/raonsecure/transkey/jsbn/jsbn-min.js" ).done(function(){
							$.cachedScript( contextPath + "/raonsecure/transkey/TranskeyLibPack_op.js" ).done(function(){
								$.cachedScript( contextPath + "/raonsecure/transkey/transkey.js" ).done(function(){
									if(typeof initTranskey=="function") initTranskey();				
								});
							});
						});
					});
				}else if( ML4WebApi.webConfig.virtualKeyboardType === "NSHC" ){	
					$.cachedScript( contextPath + "/nfilter/js/include_open_nFilter.js" ).done(function(){
						initializedNFilter();	
					});
				}else if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
					$.cachedScript( contextPath + "/pluginfree/js/nppfs-1.11.0.js" ).done(function(){
						npPfsCtrl.hideLoading();
						npPfsStartup(null, false, false, false, true, "npkencrypt", "on");
					});
				}else if( ML4WebApi.webConfig.virtualKeyboardType === "DREAM" ){
					if (document.createStyleSheet){
						document.createStyleSheet(contextPath + '/magicVKeypad/magicVKeypad.css');
					}else{
						$('head').append( $('<link rel="stylesheet" type="text/css" />').attr('href', contextPath + '/magicVKeypad/magicVKeypad.css' ) );
					}
					$.cachedScript( contextPath + "/magicVKeypad/magicVKeypad.js" ).done(function(){
						ML4WebVKey.init();
					});
				}
			}
		}

		// 간편인증 리스트
		if(ML4WebApi.webConfig.storageList_lnb == null || ML4WebApi.webConfig.storageList_lnb == "") {
			//STORAGELIST_LNB = null 값이면 왼쪽 메뉴 날개 div 없애기
			$(".ML_lnb").css('display', 'none');
		} else {
			for( var i = 0; i < ML4WebApi.webConfig.storageList_lnb.length; i++){
				if( ML4WebApi.webConfig.storageList_lnb[i] === "finance" ){
					//금융인증SDK
					if( ML4WebApi.webConfig.fincert_org_code != "" && ML4WebApi.webConfig.fincert_url != "" && ML4WebApi.webConfig.fincert_apikey != "" ){
						function getToday(){var e=new Date;return e.getFullYear()+("0"+(1+e.getMonth())).slice(-2)+("0"+e.getDate()).slice(-2)};
						
						var head = document.getElementsByTagName('head')[0];
						var script = document.createElement('script');
						script.type= 'text/javascript';
						script.id = "fincert";
						var finCertURL = ML4WebApi.getProperty('fincert_url') + "?dt=" + getToday();
						script.src = finCertURL;
						head.appendChild(script);
					}
				}
			}
		}
	},
	/*
	 * 인증서 선택창을 그리는 함수
	 */
	_createWindow : function() {
		if( ! ML4WebApi.getProperty("banner") ){
			$(".ML_cp_AD").remove();
		}
		var window_height = 558;
		var window_width = 0;
		
		if(ML4WebApi.webConfig.storageList_lnb == null || ML4WebApi.webConfig.storageList_lnb == ""){
			window_width = 420;
		} else {
			window_width = 526;
		}
		
		var popupWidth = document.body.clientWidth;
		var os_ver = ML4WebApi.getProperty("os");
		var jqxWidget = $('#ML_window');
		var offset = jqxWidget.offset();
		var xValue = offset.left + (($(window).width()/2)-(window_width/2));
		var position = { x: xValue, y: offset.top + 50};
		var popupPosition =  {x: offset.left + ($(window).width()/2) - 200, y: offset.top + 200};
		
		if((os_ver=="Android" && navigator.userAgent.toUpperCase().indexOf("MOBILE") >-1)|| os_ver=="IPHONE" || os_ver=="BlackBerry" ){
			isBanner = false;
			position = 'center';
			popupPosition = 'center';
			popupWidth = 350;
		}else if( os_ver=="IPAD" || os_ver=="Android" ){
			isBanner = false;
			position = 'center';
			popupWidth = 398;
			
		}else{
			popupWidth = 398;
		}
		
		//-00. Main
		$("#ML_window").MLjquiWindow({
			title: $.i18n.prop("TS000"),
			resizable: false,
			position: position,
			showCloseButton:false,
			isModal: true,
			modalOpacity: 0.3,
			modalZIndex: 9995,
			modalBackgroundZIndex: 9995,
			showCollapseButton: false,
			keyboardCloseKey: 0,
			maxHeight: 600, maxWidth:900,
			minHeight: 200, minWidth: 200,
			height: window_height, width: window_width,
			initContent: function () {
				$('#ML_window').MLjquiWindow('focus');
				if( os_ver === "IPHONE" ){
					var orientation = window.orientation;
					if( orientation == 0 ){
						var iWidth  = screen.width;
						var iHeight = screen.height;
						
						if(iHeight >= 800){
							iHeight = 170;
						}else if(iHeight >= 600){
							iHeight = 150;
						}else if(iHeight >= 500){
							iHeight = 140;
						}
					
						iWidth  = iWidth  + "px";
						iHeight = iHeight + "vw";
											
						window.parent.document.getElementById('dscert').style.width  = iWidth;
						window.parent.document.getElementById('dscert').style.height = iHeight;
					}else if(orientation == 90){
						iHeight = (screen.width) - 323;
						iHeight = iHeight + 'vw';
	
						window.parent.document.getElementById('dscert').style.height = iHeight;
					}else if(orientation == -90){
						iHeight = (screen.width) - 323;
						iHeight = iHeight + 'vw';
						
						window.parent.document.getElementById('dscert').style.height = iHeight;
					}
				}
			}
		});
	
		$('#popup_alert').MLjquiWindow({
			title: $.i18n.prop("TS042"),
			resizable: false,
			position: popupPosition,
			showCloseButton:false,
			zIndex:9999,
			showCollapseButton: false,
			keyboardCloseKey: 0,
			isModal: true,
			maxHeight: 600, maxWidth:420,
			minHeight: 200, minWidth: 200,
			height: 148, width: popupWidth,
			initContent: function () {
				$('#popup_alert').MLjquiWindow('close');
			}
		});
	
		//-02. Common
		//$('#ML_Dialog_common').show();
		$('#ML_Dialog_common').MLjquiWindow({
	//		autoOpen: false,
	//		title:'비밀번호변경',
			resizable: false,
			//position: 'center',
			position: popupPosition,
			showCloseButton:false,
	//		closeButtonSize: 35,
	//		closeButtonAction:'close',
	//		draggable:false,
			isModal: true,
			modalOpacity: 0.3,
			modalZIndex: 9996,
			keyboardCloseKey: 0,
			modalBackgroundZIndex: 9996,
			showCollapseButton: false,
			width: popupWidth,
			initContent: function () {
				$('#ML_Dialog_common').MLjquiWindow('close');
			}
		});
		
		//-03. Cs install dialog
		$('#ML_dialog_cs_install').MLjquiWindow({
			resizable: false,
			//position: 'center',
			position: popupPosition,
			showCloseButton:false,
			isModal: false,
			modalOpacity: 0.3,
			modalZIndex: 9996,
			keyboardCloseKey: 0,
			modalBackgroundZIndex: 9996,
			showCollapseButton: false,
			width: popupWidth,
			initContent: function () {
				$('#ML_dialog_cs_install').MLjquiWindow('close');
			}
		});
	},

	initWebMainEvent : function() {
		ML4WebLog.log("ML4Web_Draw.js - initWebMainEvent() called...");
		
		// 인증 플랫폼 선택
		$('[id^="ML_lnb_"]').click(function(){
			var lnb_id = this.id.split("ML_lnb_")[1];

			$('.ML_lnb_area>ul>[class^="ML_cert_"]').removeClass("sel");
			$(this).parent().addClass("sel");
			
			if(lnb_id == "share"){
				$("#MagicLine_lnb").css('display', 'none');
				$("#MagicLine_lnb").empty();
				$("#MagicLine").css('display', 'block');
				// 매직라인 초기화
				$('#temp').val("0");
				$('#flagInit').val("");
				$("#stg_" + ML4WebApi.getProperty("defaultStorage")).click();
				
			}else{
				// 브라우저 안내문 제거 후
				$("#ML_window").css("width", "526px");

				if(lnb_id == "finance"){	
					magiclineController.InitFinCertAPI(function(){
						ML4WebApi.signFinCertAPI(ML4WebDraw.saveParam, function(code, jsonObj){
							if(code == 0){
								var requestStr = JSON.stringify(jsonObj);
								// - cross domain, send data
								magiclineController.sendResultMessage(requestStr);
							} else {
								ML4WebDraw.errorHandler("main", jsonObj, null, null);
							}
						});
					});
					$('#ML_lnb_share').click();
						
				}else if(lnb_id == "pass" || lnb_id == "payco" || lnb_id =="toss"){	
					//패스 인증서 or 페이코 인증서 추가
					
					 $("#MagicLine").css('display', 'none');
					
					$("#MagicLine_lnb").empty();
					$("#MagicLine_lnb").css('display', 'none');
					$("#MagicLine_lnb").load("UI/ML4Web_" + lnb_id + ".html");
					$("#MagicLine_lnb").css('display', 'block');

					
					
				}
				else{
					$("#MagicLine").css('display', 'none');
					$("#MagicLine_lnb").css('display', 'block');
					$("#MagicLine_lnb").empty();
					
					$("#MagicLine_lnb").load("UI/ML4Web_" + lnb_id + ".html");
				}
			}
			
		});	
		
		//인증서 선택창 스토리지 선택
		$('[id^="stg_"]').click(function () {
			var stg_id = this.id.split("stg_")[1];
			//ML4WebLog.log("storage selected.... storageId = "+stg_id)
			selectMedia = stg_id;
			
			if(stg_id == "web" || stg_id == "kftc" || stg_id == "web_kftc"){
				// 브라우저 안내 옵션 처리				
				if(ML4WebApi.getProperty('browserNoticeShow')){
					//20220217 왼쪽날개옵션 CSS추가
					if(ML4WebApi.webConfig.storageList_lnb == null || ML4WebApi.webConfig.storageList_lnb == ""){
						$("#ML_window").css("width", "670px");
						$(".MLjqui-window-header").css("width", "410px");
					}else{
						$("#ML_window").css('width', '776px');	
					}
				}else{
					$("#ML_window").css('width', '526px');
				}			
				$("#get_cert").html("<img src='UI/images/icon_search.png' alt=''>"+$.i18n.prop("TS035"));				
				$("#in_browser").css('width', '190px');
				
				if(stg_id == "web_kftc"){
					$("#btn_deleteCert").show();
					$("#btn_CopyCert").show();
					
					var kftcRelayURL 	= ML4WebApi.getProperty("kftc_script_url_relay");
					var kftcOpenCertURL = ML4WebApi.getProperty("kftc_script_url_opencert");
					var kftcCorpCode 	= ML4WebApi.getProperty("kftc_corp_code");
					
					var opencertURL = ""; 
					var relayURL = ""; 
					var today = new Date();
					today = ML4WebUtil.yyyymmdd(today);
					
					if( kftcRelayURL != "" && kftcOpenCertURL != "" && kftcCorpCode != ""){
						opencertURL = kftcOpenCertURL + "?dt=" + today + "&corp=" + kftcCorpCode;
						relayURL = kftcRelayURL + "?dt=" + today + "&corp=" + kftcCorpCode;
					}else{
						opencertURL = "https://fidoweb.yessign.or.kr:3100/v2/opencert.js?dt=" + today + "&corp=099";
						relayURL = "https://fidoweb.yessign.or.kr:3100/v2/relay.js?dt=" + today + "&corp=099";
					}
					
					if(!document.getElementById('opencerturl') || !document.getElementById('relayurl')){
						var head = document.getElementsByTagName('head')[0];
						var scriptOpenCert = document.createElement('script');
						scriptOpenCert.type= 'text/javascript';
						scriptOpenCert.id = "opencerturl";
						scriptOpenCert.src = opencertURL;
						head.appendChild(scriptOpenCert);
						
						var scriptRelay = document.createElement('script');
						scriptRelay.type= 'text/javascript';
						scriptRelay.id = "relayurl";
						scriptRelay.src = relayURL;
						head.appendChild(scriptRelay);
					}
					
					if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
						var npLength = npVCtrl.keypadObject.length;
						if(npLength > 1){
							for (var i=0; i<npLength; i++){
								if("nppfs-keypad-input_cert_pw" == npVCtrl.keypadObject[i]._uuid){
									npVCtrl.keypadObject.splice(i,1);
									break;
								}
							}
						}
					}
				}
				
				if( ML4WebApi.webConfig.useVirtualKeyboard ){
					if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
						var npLength = npVCtrl.keypadObject.length;
						if(npLength > 1){
							for (var i=0; i<npLength; i++){
								if("nppfs-keypad-input_cert_pw" == npVCtrl.keypadObject[i]._uuid){
									npVCtrl.keypadObject.splice(i,1);
									break;
								}
							}
						}
					}
				}
				
				if( !ML4WebApi.webConfig.useVirtualKeyboard && (ML4WebApi.webConfig.virtualKeyboardType === "INCA" || ML4WebApi.webConfig.virtualKeyboardType === "DREAM") ){
					$('#input_cert_pw_new').hide();
					$('#input_cert_pw').show();
					$('#keyboardOn').show();
				}
				// 210611 기재부: 키보드 이미지 사라짐
				/*else if(ML4WebApi.webConfig.virtualKeyboardType === "RAON"){
					$('#keyboardOn').hide();
				}*/
				
			}else{
				//20220217 왼쪽날개옵션 CSS추가
				if(ML4WebApi.webConfig.storageList_lnb == null || ML4WebApi.webConfig.storageList_lnb == ""){
					$("#ML_window").css('width', '420px');
					$(".MLjqui-window-header").css("width", "410px");
				} else {
					$("#ML_window").css('width', '526px');	
				}
				// 공동인증서 가져오기 문구 css
				$("#get_cert").html("<img src='UI/images/icon_search.png' alt=''>"+$.i18n.prop("TS036"));
				$("#in_browser").css('width', '150px');
				
				$("#btn_deleteCert").hide();
				$("#btn_CopyCert").hide();
				
				if( !ML4WebApi.webConfig.useVirtualKeyboard && (ML4WebApi.webConfig.virtualKeyboardType === "INCA" || ML4WebApi.webConfig.virtualKeyboardType === "DREAM") ){
					$('#input_cert_pw').hide();
					$('#keyboardOn').hide();
					if(!document.getElementById('input_cert_pw_new')){
						$('#input_cert_pw').after("<input type='password' id='input_cert_pw_new' class='passwd_input' placeholder='비밀번호를 입력하시오.' onKeypress='signEnterKeyEvent(event)' onClick='newPasswdClick();' >");
					}else{
						$('#input_cert_pw_new').show();
					}
					
					// CapsLock
					var browser = ML4WebApi.getProperty('browser');
					if((browser != 'MSIE 8') && (browser != 'MSIE 7') && (browser != 'MSIE 6')) {
						document.querySelector('#input_cert_pw_new').addEventListener('keyup', checkCapsLock);
						document.querySelector('#input_cert_pw_new').addEventListener('mousedown', checkCapsLock);
					}
					
					$('#input_cert_pw_new').blur(function(e){
						$("#capslock").hide();
					});
				}
				
				if( ML4WebApi.webConfig.useVirtualKeyboard ){
					if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
						var npLength = npVCtrl.keypadObject.length;
						if(npLength > 1){
							for (var i=0; i<npLength; i++){
								if("nppfs-keypad-input_cert_pw" == npVCtrl.keypadObject[i]._uuid){
									npVCtrl.keypadObject.splice(i,1);
									break;
								}
							}
						}
					}
				}
			}
			
			// 브라우저 안내 옵션 처리
			if(ML4WebApi.getProperty('browserNoticeShow')){
				ML4WebDraw.browser_menu();
			}
		
			if(stg_id == "token"){
				$("#ML_dp_04").text($.i18n.prop("TS037"));
			}else{
				$("#ML_dp_04").text($.i18n.prop("TS038"));
			}
			
			//UI driver 선택창 위치 조정
			var stg_idx = $(this).attr("stgIdx");
			$('#driver_div').removeClass("pos_01").removeClass("pos_02").removeClass("pos_03").removeClass("pos_04").removeClass("pos_05");
			$('#driver_div').addClass("pos_0"+stg_idx);
	
			//UI css selection 처리
			$('[class^="ML_storage_"]').removeClass("on");
			$(this).parent().addClass("on");
	
			//인증서 목록 무조건 초기화
			ML4WebDraw.MakeCertiListDiv(null);
			
			// 드라이버 선택창 닫기
			closeDriverDialog();
	
			//스토리지 정보 조회

			setTimeout(function(){
				ML4WebUI.selectStorageInfo(stg_id, function(resultCode, jsonObj){
					//ML4WebLog.log("[Callback]selectStorage()  .... resultCode === "+resultCode);
					if( resultCode == 0) {
						if(jsonObj == null || $.isEmptyObject(jsonObj)){
							var certOpt = {"storageName":stg_id};
							
							if(stg_id=="smartcert" && ML4WebApi.getProperty("smartcert_type")!='C'){
								// WEB SmartCert 구동
								$('#btn_confirm').onclick();
								return;
							}else{
								ML4WebUI.getStorageCertList(certOpt, function(resultCode, jsonObj) {
									if( resultCode == 0) {//성공
										//ML4WebLog.log("[getStorageCertList() callback === " + JSON.stringify(jsonObj));
										ML4WebDraw.MakeCertiListDiv(jsonObj.cert_list);
									}else{	//실패
										ML4WebLog.log("[ERROR!!! - "+resultCode+" ] errCode === " + jsonObj.errCode);
										
										ML4WebDraw.MakeCertiListDiv(null);
									}
								});
							}
						} else {
							ML4WebLog.log("[SUCCESS!!!] Select Drive. === " + stg_id);
							
							openDriverDialog(stg_id, stg_idx, jsonObj);
						}
					} else{
						
						ML4WebLog.log("[ERROR!!! - "+resultCode+" ] errCode === " + jsonObj.errCode + ", errMsg === " +  jsonObj.errMsg);
						EmptyCertDiv();
					}
				});
			},200);
			
		});
		
		$(document).on("click", function(event){
			var targetId = event.target.id;
			if($("#sub_drv_list").length > 0){
				closeDriverDialog();
			}
			//20200310 mj
			if(targetId.substring(0,4) == "stg_"){
				$('#stg_'+selectMedia).focus();
			}
			
			if(targetId == "manual_close"){
				$("#manual_img").removeAttr("tabindex");
				$("#manual_close").removeAttr("tabindex");
				$("#browser_manual").attr("style", "display:none");
				//20220217 왼쪽날개옵션 CSS추가
				if(ML4WebApi.webConfig.storageList_lnb == null || ML4WebApi.webConfig.storageList_lnb == ""){
					$("#ML_window").css('width', '420px');
				}else{
					$("#ML_window").css('width', '526px');	
				}
			}else{
				if($("#sub_drv_list").length > 0){
					if(targetId == "MSG_TS701" || targetId == "stg_web"){
						$("#browser_manual").attr("style", "display:inline");						
						if(ML4WebApi.getProperty('browserNoticeShow')){
							if(ML4WebApi.webConfig.storageList_lnb == null || ML4WebApi.webConfig.storageList_lnb == "") {
								$("#ML_window").css("width", "670px");
								$(".MLjqui-window-header").css("width", "410px");
							}else{
								$("#ML_window").css('width', '776px');	
							}
						}else{
							$("#ML_window").css('width', '526px');
						}						
					}else if(targetId == "MSG_TS703" || targetId == "MSG_TS705" || targetId == "MSG_TS706" || targetId == "MSG_TS707" || targetId == "stg_hdd" || targetId == "stg_token" || targetId == "stg_mobile" || targetId == "stg_smartcert"){
						$("#manual_img").removeAttr("tabindex");
						$("#manual_close").removeAttr("tabindex");
						$("#manual_close").attr("style", "display:none");
						
						//20220217 왼쪽날개옵션 CSS추가
						if(ML4WebApi.webConfig.storageList_lnb == null || ML4WebApi.webConfig.storageList_lnb == "") {
							$("#ML_window").css("width", "420px");
							$(".MLjqui-window-header").css("width", "410px");
						} else {
							$("#ML_window").css('width', '526px');	
						}
					}
					/*if(targetId == "MSG_TS703" || targetId == "MSG_TS705"|| targetId == "MSG_TS706" || targetId == "MSG_TS707"){
						$("#manual_close").attr("style", "display:none");
						$("#ML_window").css('width', '526px');
					}else if(targetId == "MSG_TS701"|| targetId == "stg_web"){
						$("#browser_manual").attr("style", "display:inline");
						$("#ML_window").css('width', '776px');
					}*/
				}
			}
		});	
	
	},
	// 모바일전용 
	initWebMainEvent_M : function(){
		ML4WebLog.log("ML4Web_Draw.js - initWebMainEvent_M() called...");
		
		$('[id^="stg_"]').click(function () {
			var stg_id = this.id.split("stg_")[1];
			selectMedia = stg_id;
			
			if(stg_id == "web_kftc"){
				var kftcRelayURL 	= ML4WebApi.getProperty("kftc_script_url_relay");
				var kftcOpenCertURL = ML4WebApi.getProperty("kftc_script_url_opencert");
				var kftcCorpCode 	= ML4WebApi.getProperty("kftc_corp_code");
				
				var opencertURL = ""; 
				var relayURL = ""; 
				var today = new Date();
				today = ML4WebUtil.yyyymmdd(today);
				
				if( kftcRelayURL != "" && kftcOpenCertURL != "" && kftcCorpCode != ""){
					opencertURL = kftcOpenCertURL + "?dt=" + today + "&corp=" + kftcCorpCode;
					relayURL = kftcRelayURL + "?dt=" + today + "&corp=" + kftcCorpCode;
				}else{
					opencertURL = "https://fidoweb.yessign.or.kr:3100/v2/opencert.js?dt=" + today + "&corp=099";
					relayURL = "https://fidoweb.yessign.or.kr:3100/v2/relay.js?dt=" + today + "&corp=099";
				}
				
				if(!document.getElementById('opencerturl') || !document.getElementById('relayurl')){
					var head = document.getElementsByTagName('head')[0];
					var scriptOpenCert = document.createElement('script');
					scriptOpenCert.type= 'text/javascript';
					scriptOpenCert.id = "opencerturl";
					scriptOpenCert.src = opencertURL;
					head.appendChild(scriptOpenCert);
					
					var scriptRelay = document.createElement('script');
					scriptRelay.type= 'text/javascript';
					scriptRelay.id = "relayurl";
					scriptRelay.src = relayURL;
					head.appendChild(scriptRelay);
				}
			}else if(stg_id == "fincert"){
				magiclineController.InitFinCertAPI(function(){
					ML4WebApi.signFinCertAPI(ML4WebDraw.saveParam, function(code, jsonObj){
						if(code == 0){
							var requestStr = JSON.stringify(jsonObj);
							// - cross domain, send data
							magiclineController.sendResultMessage(requestStr);
						} else {
							ML4WebDraw.errorHandler("main", jsonObj, null, null);
						}
					});
				});
			}
			
			//UI driver 선택창 위치 조정
			var stg_idx = $(this).attr("stgIdx");
			$('#driver_div').removeClass("pos_01").removeClass("pos_02").removeClass("pos_03").removeClass("pos_04").removeClass("pos_05");
			$('#driver_div').addClass("pos_0"+stg_idx);
			
			//UI css selection 처리
			$('[class^="ML_storage_"]').removeClass("on");
			$(this).parent().addClass("on");
			
			//인증서 목록 무조건 초기화
			ML4WebDraw.MakeCertiListDiv(null);
			
			// 드라이버 선택창 닫기
			closeDriverDialog();
			
			//스토리지 정보 조회
			setTimeout(function(){
				ML4WebUI.selectStorageInfo(stg_id, function(resultCode, jsonObj){
					//ML4WebLog.log("[Callback]selectStorage()  .... resultCode === "+resultCode);
					if( resultCode == 0) {
						if(jsonObj == null || $.isEmptyObject(jsonObj)){
							//ML4WebLog.log( "[click]옵션없음 인증서 목록 조회해라....");
							var certOpt = {"storageName":stg_id};
							
							if(stg_id=="smartcert" && ML4WebApi.getProperty("smartcert_type")!='C'){
								// WEB SmartCert 구동
								$('#btn_confirm').onclick();
								return;
							}else{
								ML4WebUI.getStorageCertList(certOpt, function(resultCode, jsonObj) {
									if( resultCode == 0) {//성공
										//ML4WebLog.log("[getStorageCertList() callback === " + JSON.stringify(jsonObj));
										
										ML4WebDraw.MakeCertiListDiv(jsonObj.cert_list);
									}else{	//실패
										//ML4WebLog.log("[ERROR!!! - "+resultCode+" ] errCode === " + jsonObj.errCode + ", errMsg === " +  jsonObj.errMsg);
										ML4WebLog.log("[ERROR!!! - "+resultCode+" ] errCode === " + jsonObj.errCode);
										
										ML4WebDraw.MakeCertiListDiv(null);
									}
								});
							}
						} else {
							ML4WebLog.log("[SUCCESS!!!] Select Drive. === " + stg_id);
							//ML4WebLog.log( "[click]옵션있으니 드라이브 선택처리해라... drivers.length === " + jsonObj.drivers.length);
							
							openDriverDialog(stg_id, stg_idx, jsonObj);
						}
					} else{
						
						ML4WebLog.log("[ERROR!!! - "+resultCode+" ] errCode === " + jsonObj.errCode + ", errMsg === " +  jsonObj.errMsg);
						EmptyCertDiv();
					}
				});
			},200);
			
		});
		
		var pixel_width = "418px";
		
		$(document).on("click", function(event){
			var targetId = event.target.id;
			if($("#sub_drv_list").length > 0){
				closeDriverDialog();
			}
			
			//20200324
			if(targetId.substring(0,4) == "stg_" || targetId == ""){
				$('#stg_'+selectMedia).focus();
			}
			
			if(targetId == "manual_close"){
				$("#manual_img").removeAttr("tabindex");
				$("#manual_close").removeAttr("tabindex");
				$("#browser_manual").attr("style", "display:none");
				$("#ML_window").css('width', '418px');
			}else{
				if($("#sub_drv_list").length > 0){
					if(targetId == "MSG_TS701" || targetId == "stg_web"){
						//$("#browser_manual").attr("style", "display:inline");
						//$("#ML_window").css('width', '668px');
					}else if(targetId == "MSG_TS703" || targetId == "MSG_TS705" || targetId == "MSG_TS706" || targetId == "MSG_TS707" || targetId == "stg_hdd" || targetId == "stg_token" || targetId == "stg_mobile" || targetId == "stg_smartcert"){
						//$("#manual_close").attr("style", "display:none");
						//$("#ML_window").css('width', '418px');
					}
					/*if(targetId == "MSG_TS703" || targetId == "MSG_TS705"|| targetId == "MSG_TS706" || targetId == "MSG_TS707"){
						$("#manual_close").attr("style", "display:none");
						$("#ML_window").css('width', '418px');
					}else if(targetId == "MSG_TS701"|| targetId == "stg_web"){
						$("#browser_manual").attr("style", "display:inline");
						$("#ML_window").css('width', '668px');
					}*/
				}
			}
		});
	},
	
/*
 * 브라우저 인증서 선택시 선택창 옆에 나오는 안내 이미지 출력 함수
 */
	browser_menu : function() {
		var child ='';
		
		if(ML4WebApi.getProperty('browserNoticeImg')){
			// img
			if(selectMedia == "web" || selectMedia == "web_kftc" ){
				if(ML4WebApi.webConfig.storageList_lnb == null || ML4WebApi.webConfig.storageList_lnb == "") {
					child += '<div id="browser_manual1" style="width:250px; top:0px;left:420px;  position:absolute;z-index:10002; display:block">';
				}else{
					child += '<div id="browser_manual1" style="width:250px; top:0px;left:526px;  position:absolute;z-index:10002; display:block">';
				}	
				child += '<img id="manual_img" src="UI/images/browser_menual3.png" style="width:250px; heigth:550px; " tabindex="0" ';
				child += 'alt="브라우저 인증서 사용 절차 안내 첫번째 공동인증서 가져오기 버튼 선택 두번째 하드디스크에 있는 인증서 파일 선택 세번재 브라우저에 있는 인증서 저장 체크 또는 체크 해제 후 인증서 비밀번호 입력 브라우저 인증서는 브라우저 캐시를 삭제하면 지워짐">';
				child += '<img id="manual_close" src="UI/images/btn_hd_close.png" style="width:20px; heigth:20px;position:absolute; left:230px; " alt="브라우저 인증서 사용방법 닫기버튼" title="닫기버튼" tabindex="0" onKeyPress="manualClose(event);">';
			}else{
				child += '<div id="browser_manual1" style="width:250px; top:0px;left:526px;  position:absolute;z-index:10002; display:none">';
				child += '<img id="manual_img" src="UI/images/browser_menual3.png" style="width:250px; heigth:550px; " ';
				child += 'alt="브라우저 인증서 사용 절차 안내 첫번째 공동인증서 가져오기 버튼 선택 두번째 하드디스크에 있는 인증서 파일 선택 세번재 브라우저에 있는 인증서 저장 체크 또는 체크 해제 후 인증서 비밀번호 입력 브라우저 인증서는 브라우저 캐시를 삭제하면 지워짐">';
				child += '<img id="manual_close" src="UI/images/btn_hd_close.png" style="width:20px; heigth:20px;position:absolute; left:230px; " alt="브라우저 인증서 사용방법 닫기버튼" title="닫기버튼" onKeyPress="manualClose(event);">';
			}
			child += '</div>';
		}else{
			//child.html에 css 파일 추가
			if($('#br_common_css').length == 0){
				var head = document.getElementsByTagName('head')[0];
				var script = document.createElement('link');
				script.id = 'br_common_css';
				script.rel = "stylesheet";
				script.type= 'text/css';
				script.href = 'UI/css/br_common.css';
				head.appendChild(script);
			}
			
			// txt
			if(selectMedia == "web" || selectMedia == "web_kftc" ){
				child += '<div id="browser_manual1" style="width:250px; top:0px;left:526px;  position:absolute;z-index:10002; display:block">';
				child += '<div class="br-modal-dialog02" style="width:250px;">';
				child += '    <div class="br-modal-header">';
				child += '		<img id="manual_close" src="UI/images/btn_hd_close.png" style="width:20px; heigth:20px;position:absolute; left:220px; " alt="브라우저 인증서 사용방법 닫기버튼" title="닫기버튼" tabindex="0" onKeyPress="manualClose(event);">';
				
			}else{
				child += '<div id="browser_manual1" style="width:250px; top:0px;left:526px;  position:absolute;z-index:10002; display:none">';
				child += '<div class="br-modal-dialog02" style="width:250px;">';
				child += '    <div class="br-modal-header">';
				child += '		<img id="manual_close" src="UI/images/btn_hd_close.png" style="width:20px; heigth:20px;position:absolute; left:220px; " alt="브라우저 인증서 사용방법 닫기버튼" title="닫기버튼" tabindex="-1" onKeyPress="manualClose(event);">';
			}
			child += '		<div class="br_hd_tit"><i class="la la-certificate"></i>브라우저 인증서 사용 안내</div>';
			child += '		<div class="br_hd_con">브라우저 인증서는 하드디스크 등에 있는 공동인증서를 브라우저에 저장하는 것입니다.</div>';
			child += '	</div>';
			child += '	<div class="br-modal-body">';
			child += '		<div class="br_con_box">';
			child += '			<div class="con_list">브라우저 인증서가 저장되어 있지 않을 경우, 아래의 절차대로 인증서를 선택하여 주세요.</div>';
			child += '			<div class="con_step_box">';
			child += '				<ul>';
			child += '					<li><span class="step_cir">1</span>[공동인증서 가져오기] 버튼클릭</li>';
			child += '					<li><span class="step_cir">2</span>하드디스크에 있는 인증서 파일 선택';
			child += '						<ul>';
			child += '							<li>파일종류 : *.pfx, *.p12, 파일2개(signCert.der/signPri.key)</li>';
			child += '							<li>선택방법 : drag&drop 또는 열기버튼 이용</li>';
			child += '						</ul>';
			child += '					</li>';
			child += '					<li><span class="step_cir">3</span>브라우저에 인증서 저장 체크 또는 체크해제';
			child += '						<ul>';
			child += '							<li>체&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;크 : 브라우저에 인증서를 저장 최초 1회만 저장하면 계속 이용가능</li>';
			child += '							<li>체크해제 : 브라우저에 인증서를 저장하지 않음</li>';
			child += '						</ul>';
			child += '					</li>';
			child += '				</ul>';
			child += '				<div class="con_list">인증서 비밀번호 입력</div>';
			child += '				<div class="con_list">브라우저 인증서는 브라우저 캐시를 삭제하면 지워짐</div>';
			child += '			<div class="con_last"><i class="la la-check-circle"></i>자세한 사항은 <span class="br_txt_bold">[브라우저 인증서 사용방법]</span> 선택</div>';
			child += '			</div></div></div></div></div>';
		}
		
		
		
		if ( $("#browser_manual1").length > 0 ) {
			//$('#ML_Dialog_common').MLjquiWindow('destroy');
			var select = document.getElementById('ML_container');
			select.removeChild(select.lastChild);
		}
		
		var node = document.createElement("div");
		node.innerHTML=child;
		document.getElementById('ML_container').appendChild(node);
		/*$('#driver_div').empty().html(child);
		$('#driver_div').show();*/
		
	},

/*
 * 기존 Main.js 에 있던 인증서 선택창에서 스토리지 목록을 만드는 함수
 */
/*
viewoptObj.defaultStorage;
viewoptObj.storageList; //[]
viewoptObj.installcheck;
viewoptObj.updatecheck;
viewoptObj.browserInfo;
*/
	// 파라미터 저장을 위한
	saveParam : function(obj) {
		this.saveParam = "";
		if (typeof(obj) != "undefined") {
			this.saveParam = obj;
		}
	},
	
	MakeStorageListDiv : function(obj) {
		var default_stg = obj.defaultStorage;
		var isCsInstall = (obj == "mgmt") ? ML4WebApi.getProperty("is_cs_install") : obj.installcheck;
		var isCsUpdate = (obj == "mgmt") ? ML4WebApi.getProperty("is_cs_update") : obj.updatecheck;
		var stgArr_lnb = obj.storageList_lnb;
		var stgArr = (obj == "mgmt") ? ML4WebApi.getProperty("storageListMgmt") : obj.storageList;
		var browser = obj.browserInfo;
		
		//간편인증서비스 리스트
		var stgHtml_lnb = '<ul>';
		
		if(stgArr_lnb != null && stgArr_lnb.length > 0 && obj != "mgmt"){
			//목록그리기
			var stg_length = stgArr_lnb.length;
			
			for(var i=0; i<stg_length; i++){
				
				stgHtml_lnb += '<li class="ML_cert_' + stgArr_lnb[i];
				
				if(i == 0){
					stgHtml_lnb += ' sel';
				}
				stgHtml_lnb += '"><a href="#" id="ML_lnb_' + stgArr_lnb[i] + '"><span>';
				
				switch(stgArr_lnb[i]){
					case 'share': 
						stgHtml_lnb += '공동';
						break;
					case 'finance': 
						stgHtml_lnb += '금융';
						break;
					case 'pass': 
						stgHtml_lnb += 'PASS';	
						break;
					case 'payco': 
						stgHtml_lnb += 'PAYCO';	
						break;
					case 'toss':
						stgHtml_lnb += 'TOSS';
						break;
				} // end of switch
				
				stgHtml_lnb += '<br>인증서</span></a></li>';
					
			} // end of for
			
			//왼쪽날개 draw 끝	
			stgHtml_lnb += '</ul>';
			document.getElementsByClassName('ML_lnb_area')[0].innerHTML = stgHtml_lnb;
		}
				
		//저장소 리스트
		stgHtml = '	<div id="wrap_stg_01" class="ML_storage_area">';
		stgHtml += '		<ul>';
		
		if(stgArr != null && stgArr.length > 0){
			
			//목록그리기
			var stg_length = stgArr.length < 5 ? stgArr.length : 5;
			var mode = (obj =="mgmt") ? "mgmt" : "main";
			var id_key = (mode=="main") ? "stg_" : "stg_admin_";
			
			
			if(stgArr.length < 6){
				$(".ML_storage_box_sub").css("margin-left","11px"); 
				$(".customNavigation").css("display","none"); 
			}
			
			for(var i=0; i<stg_length; i++){
				
				var idx = i+1;
				
				switch(stgArr[i]){
				
				// 스토리지 종류 별로 미리 정의된 html 태그를 replace
				case 'web_kftc': 
					stgHtml += ML4WebDraw.STG_HTML_WEB.replace(".id_key.", id_key + "web_kftc").replace(".idx.", idx);	
					break;
				case 'kftc': 
					stgHtml += ML4WebDraw.STG_HTML_WEB.replace(".id_key.", id_key + "kftc").replace(".idx.", idx);	
					break;
				case 'web': 
					stgHtml += ML4WebDraw.STG_HTML_WEB.replace(".id_key.", id_key + "web").replace(".idx.", idx);	
					break;
				case 'pfx':
					if(browser != "MSIE 9" && browser != "MSIE 8" && browser != "MSIE 7" && browser != "MSIE 6"){
						stgHtml += ML4WebDraw.STG_HTML_PFX.replace(".id_key.", id_key + "pfx").replace(".idx.", idx);
					}else if(!isCsInstall || isCsUpdate){
						stgHtml += ML4WebDraw.STG_HTML_PFX_INS.replace(".idx.", idx).replace(".mode.", mode);
					}else{
						stgHtml += ML4WebDraw.STG_HTML_PFX_IE.replace(".id_key.", id_key + "pfx").replace(".idx.", idx);
					}
					break;
				case 'hdd':
					if(!isCsInstall || isCsUpdate){
						stgHtml += ML4WebDraw.STG_HTML_HDD_INS.replace(".idx.", idx).replace(".mode.", mode);
					}else{
						stgHtml += ML4WebDraw.STG_HTML_HDD.replace(".id_key.", id_key + "hdd").replace(".idx.", idx);
					}
					break;
				case 'shdd':
					if(!isCsInstall || isCsUpdate){
						stgHtml += ML4WebDraw.STG_HTML_SDD_INS.replace(".idx.", idx).replace(".mode.", mode);
					}else{
						stgHtml += ML4WebDraw.STG_HTML_SDD.replace(".id_key.", id_key + "shdd").replace(".idx.", idx);
					}
					break;
				case 'token':
					if(!isCsInstall || isCsUpdate){
						stgHtml += ML4WebDraw.STG_HTML_TKN_INS.replace(".idx.", idx).replace(".mode.", mode);
					}else{
						stgHtml += ML4WebDraw.STG_HTML_TKN.replace(".id_key.", id_key + "token").replace(".idx.", idx);
					}
					break;
				case 'mobile':
					if((!isCsInstall || isCsUpdate) && ML4WebApi.MobileOption.indexOf("ubikeynx") ==-1){
						stgHtml += ML4WebDraw.STG_HTML_MOB_INS.replace(".idx.", idx).replace(".mode.", mode);
					}else{
						stgHtml += ML4WebDraw.STG_HTML_MOB.replace(".id_key.", id_key + "mobile").replace(".idx.", idx);
					}
					break;
				case 'smartcert':
					if(!isCsInstall || isCsUpdate){
						stgHtml += ML4WebDraw.STG_HTML_SMC_INS.replace(".idx.", idx).replace(".mode.", mode);
					}else{
						stgHtml += ML4WebDraw.STG_HTML_SMC.replace(".id_key.", id_key + "smartcert").replace(".idx.", idx);
					}
					break;
				case 'smartcertnx':
					stgHtml += ML4WebDraw.STG_HTML_SMCNX.replace(".id_key.", id_key + "smartcertnx").replace(".idx.", idx);
					break;
				case 'cloud':
					if(!isCsInstall || isCsUpdate){
						stgHtml += ML4WebDraw.STG_HTML_CLD_INS.replace(".idx.", idx).replace(".mode.", mode);
					}else{
						stgHtml += ML4WebDraw.STG_HTML_CLD.replace(".id_key.", id_key + "cloud").replace(".idx.", idx);
					}
					break;
				case 'unisign':
					stgHtml += ML4WebDraw.STG_HTML_UNI.replace(".id_key.", id_key + "unisign").replace(".idx.", idx);
					break;
				} // end of switch
					
			} // end of for
			
			if(stgArr.length > 5){
				//stgHtml += '		</ul>';
				//stgHtml += '	</div>';
				//stgHtml += '</div>';
				//stgHtml += '	<div id="wrap_stg_02" class="ML_storage_area">';
				//stgHtml += '		<ul>';
				
				for(var idx=5; idx<stgArr.length; idx++){
					
					switch(stgArr[i]){
						case 'web_kftc': 
							stgHtml += ML4WebDraw.STG_HTML_WEB.replace(".id_key.", id_key + "web_kftc").replace(".idx.", idx);	
							break;
						case 'kftc': 
							stgHtml += ML4WebDraw.STG_HTML_WEB.replace(".id_key.", id_key + "kftc").replace(".idx.", idx);	
							break;
						case 'web': 
							stgHtml += ML4WebDraw.STG_HTML_WEB.replace(".id_key.", id_key + "web").replace(".idx.", idx);	
							break;
						case 'pfx':
							if(browser != "MSIE 9" && browser != "MSIE 8" && 
							   browser != "MSIE 7" && browser != "MSIE 6"){
								stgHtml += ML4WebDraw.STG_HTML_PFX.replace(".id_key.", idx + "pfx").replace(".idx.", idx);
							}else if(!isCsInstall || isCsUpdate){
								stgHtml += ML4WebDraw.STG_HTML_PFX_INS.replace(".idx.", id_key).replace(".mode.", mode);
							}else{
								stgHtml += ML4WebDraw.STG_HTML_PFX_IE.replace(".id_key.", id_key + "pfx").replace(".idx.", idx);
							}
							break;
						case 'hdd':
							if(!isCsInstall || isCsUpdate){
								stgHtml += ML4WebDraw.STG_HTML_HDD_INS.replace(".idx.", idx).replace(".mode.", mode);
							}else{
								stgHtml += ML4WebDraw.STG_HTML_HDD.replace(".id_key.", id_key + "hdd").replace(".idx.", idx);
							}
							break;
						case 'shdd':
							if(!isCsInstall || isCsUpdate){
								stgHtml += ML4WebDraw.STG_HTML_SDD_INS.replace(".idx.", idx).replace(".mode.", mode);
							}else{
								stgHtml += ML4WebDraw.STG_HTML_SDD.replace(".id_key.", id_key + "shdd").replace(".idx.", idx);
							}
							break;
						case 'token':
							if(!isCsInstall || isCsUpdate){
								stgHtml += ML4WebDraw.STG_HTML_TKN_INS.replace(".idx.", idx).replace(".mode.", mode);
							}else{
								stgHtml += ML4WebDraw.STG_HTML_TKN.replace(".id_key.", id_key + "token").replace(".idx.", idx);
							}
							break;
						case 'mobile':
							if(!isCsInstall || isCsUpdate){
								stgHtml += ML4WebDraw.STG_HTML_MOB_INS.replace(".idx.", idx).replace(".mode.", mode);
							}else{
								stgHtml += ML4WebDraw.STG_HTML_MOB.replace(".id_key.", id_key + "mobile").replace(".idx.", idx);
							}
							break;
						case 'smartcert':
							if(!isCsInstall || isCsUpdate){
								stgHtml += ML4WebDraw.STG_HTML_SMC_INS.replace(".idx.", idx).replace(".mode.", mode);
							}else{
								stgHtml += ML4WebDraw.STG_HTML_SMC.replace(".id_key.", id_key + "smartcert").replace(".idx.", idx);
							}
							break;
						case 'smartcertnx':
							stgHtml += ML4WebDraw.STG_HTML_SMCNX.replace(".id_key.", id_key + "smartcertnx").replace(".idx.", idx);
							break;
						case 'cloud':
							if(!isCsInstall || isCsUpdate){
								stgHtml += ML4WebDraw.STG_HTML_CLD_INS.replace(".idx.", idx).replace(".mode.", mode);
							}else{
								stgHtml += ML4WebDraw.STG_HTML_CLD.replace(".id_key.", id_key + "cloud").replace(".idx.", idx);
							}
							break;
						case 'unisign':
							stgHtml += ML4WebDraw.STG_HTML_UNI.replace(".id_key.", id_key + "unisign").replace(".idx.", idx);
							break;
						} // end of switch
					} // end of for
					
				} // end of if(stgArr.length >5)\
			
			}else{ 
				// 목록 없음
				stgHtml += '<li class="">'+$.i18n.prop("TS084")+'</li>';
			}
		
		stgHtml += '		</ul>';
		stgHtml += '	</div>';
		stgHtml += '</div>';
		
		if(obj == "mgmt"){
			document.getElementById('MLstrSlide_admin').innerHTML = stgHtml;
		}else{
			document.getElementById('MLstrSlide').innerHTML = stgHtml;
			
			$("#dataTable").MLjquiDataTable({
				theme:"DSdatatable",
				width: "398",
				height: "150",
				columnsHeight:25,
				altRows: true, // 행 백그라운드 교차 //
				autoRowHeight: false,
				sortable: true,
				pageable: false,
				enableHover: true,
				columnsResize: true,
				selectionMode: "singleRow",
				columns: [
					{ text: $.i18n.prop("TS059"), dataField: 'Cn', width: 140, align: 'center' },
					{ text: $.i18n.prop("TS060"), dataField: 'Policy', width: 100, align: 'center' },
					{ text: $.i18n.prop("TS061"), dataField: 'Issuer', cellsAlign: 'left', align: 'center', cellsFormat: 'c2' },
					{ text: $.i18n.prop("TS062"), dataField: 'enddate', width: 78, cellsAlign: 'left', align: 'center', cellsFormat: 'd'}
				],
				ready:function(){
					$("#dataTable").MLjquiDataTable('selectRow', 0);
				}
			});
		}
		
		//메세지 처리 적용.
		MessageVO.applyMessage($('[id^="MSG_"]'));
	},	
	MakeStorageListDiv_M : function(obj){
		
		var default_stg = obj.defaultStorage;		
		var stgArr = obj.storageList;
		var browser = obj.browserInfo;
		
		stgHtml = '	<div id="wrap_stg_01" class="ML_storage_area">';
		stgHtml += '		<ul>';
		
		if(stgArr != null && stgArr.length > 0){
			
			var stg_length = stgArr.length < 5 ? stgArr.length : 5;
			var id_key = "stg_";
			var mode = "main";
			
			for(var i=0; i<stg_length; i++){				
				var idx = i+1;
				
				switch(stgArr[i]){
				case 'web': 
					stgHtml += ML4WebDraw.STG_HTML_WEB.replace(".id_key.", id_key + "web").replace(".idx.", idx);	
					break;
				case 'web_kftc': 
					stgHtml += ML4WebDraw.STG_HTML_WEB.replace(".id_key.", id_key + "web_kftc").replace(".idx.", idx);	
					break; 
				case 'smartcert' :
					stgHtml += ML4WebDraw.STG_HTML_SMC.replace(".id_key.", id_key + "smartcert").replace(".idx.", idx);
					break;
				case 'smartcertnx':
					stgHtml += ML4WebDraw.STG_HTML_SMCNX.replace(".id_key.", id_key + "smartcertnx").replace(".idx.", idx);
					break;
				case 'fincert':
					stgHtml += ML4WebDraw.STG_HTML_WEB.replace(".id_key.", id_key + "fincert").replace(".idx.", idx);
					break;
				default :
					break;
				}
			}
		}else{
			stgHtml += '<li class="">'+$.i18n.prop("TS084")+'</li>';
		}
		
		stgHtml += '		</ul>';
		stgHtml += '	</div>';
		stgHtml += '</div>';
		document.getElementById('MLstrSlide').innerHTML = stgHtml;
		
		$("#dataTable").MLjquiDataTable({
			theme:"DSdatatable",
			width: "398",
			height: "150",
			columnsHeight:25,
			altRows: true, // 행 백그라운드 교차 //
			autoRowHeight: false,
			sortable: true,
			pageable: false,
			enableHover: true,
			columnsResize: true,
			selectionMode: "singleRow",
			columns: [
				{ text: $.i18n.prop("TS059"), dataField: 'Cn', width: 140, align: 'center' },
				{ text: $.i18n.prop("TS060"), dataField: 'Policy', width: 100, align: 'center' },
				{ text: $.i18n.prop("TS061"), dataField: 'Issuer', cellsAlign: 'left', align: 'center', cellsFormat: 'c2' },
				{ text: $.i18n.prop("TS062"), dataField: 'enddate', width: 78, cellsAlign: 'left', align: 'center', cellsFormat: 'd'}
			],
			ready:function(){
				$("#dataTable").MLjquiDataTable('selectRow', 0);
			}
		});
		
		//메세지 처리 적용.
		MessageVO.applyMessage($('[id^="MSG_"]'));
		
	},
	MakeStorageListDiv_old : function(mode) {
		ML4WebLog.log("ML4Web_Draw.js - MakeStroageListDiv() called....");
		
		var isCsInstall = ML4WebApi.getProperty("is_cs_install");
		var isCsUpdate = ML4WebApi.getProperty("is_cs_update");
		var smartcertType = ML4WebApi.getProperty("smartcert_type");
		var libType = ML4WebApi.getProperty("libType");
		
		var os_ver = ML4WebApi.getProperty("os"); // windows x, MAC, UBUNTU64, UBUNTU32, FEDORA64, FEDORA32
		
		var stgArr = [];
		
		if(os_ver=="Android" || os_ver=="IPHONE" || os_ver=="IPAD" || os_ver=="BlackBerry"){
			stgArr = ML4WebApi.getProperty("storageList_m");
		}else{
			if(os_ver.indexOf('windows') == -1){
				ML4WebApi.setProperty("storageList",["web","hdd"]);
			}
			if(mode == "mgmt"){
				stgArr = ML4WebApi.getProperty("storageListMgmt");
			}else{
				stgArr = ML4WebApi.getProperty("storageList");
			}
		}
		
		stgHtml = '	<div id="wrap_stg_01" class="ML_storage_area">';
		stgHtml += '		<ul>';
		
		
		if(stgArr != null && stgArr.length > 0){
			
			//목록그리기
			var stg_length = stgArr.length < 5 ? stgArr.length : 5;
			var id_key = (mode=="main") ? "stg_" : "stg_admin_";
			var browser = ML4WebApi.getProperty('browser');
			
			if(stgArr.length < 6){
				$(".ML_storage_box_sub").css("margin-left","11px"); 
				$(".customNavigation").css("display","none"); 
			}
			
			for(var i=0; i<stg_length; i++){
				
				var idx = i+1;
				
				switch(stgArr[i]){
				
				// 스토리지 종류 별로 미리 정의된 html 태그를 replace
				case 'web': 
						stgHtml += ML4WebDraw.STG_HTML_WEB.replace(".id_key.", id_key + "web").replace(".idx.", idx);	
						break;
				case 'pfx':
						if(browser != "MSIE 9" && browser != "MSIE 8" && browser != "MSIE 7" && browser != "MSIE 6"){
							stgHtml += ML4WebDraw.STG_HTML_PFX.replace(".id_key.", id_key + "pfx").replace(".idx.", idx);
						}else if(!isCsInstall || isCsUpdate){
							stgHtml += ML4WebDraw.STG_HTML_PFX_INS.replace(".idx.", idx).replace(".mode.", mode);
						}else{
							stgHtml += ML4WebDraw.STG_HTML_PFX_IE.replace(".id_key.", id_key + "pfx").replace(".idx.", idx);
						}
						break;
				case 'hdd':
					if(!isCsInstall || isCsUpdate){
						stgHtml += ML4WebDraw.STG_HTML_HDD_INS.replace(".idx.", idx).replace(".mode.", mode);
					}else{
						stgHtml += ML4WebDraw.STG_HTML_HDD.replace(".id_key.", id_key + "hdd").replace(".idx.", idx);
					}
					break;
				case 'shdd':
					if(!isCsInstall || isCsUpdate){
						stgHtml += ML4WebDraw.STG_HTML_SDD_INS.replace(".idx.", idx).replace(".mode.", mode);
					}else{
						stgHtml += ML4WebDraw.STG_HTML_SDD.replace(".id_key.", id_key + "shdd").replace(".idx.", idx);
					}
					break;
				case 'token':
					if(!isCsInstall || isCsUpdate){
						stgHtml += ML4WebDraw.STG_HTML_TKN_INS.replace(".idx.", idx).replace(".mode.", mode);
					}else{
						stgHtml += ML4WebDraw.STG_HTML_TKN.replace(".id_key.", id_key + "token").replace(".idx.", idx);
					}
					break;
				case 'mobile':
					if(!isCsInstall || isCsUpdate){
						stgHtml += ML4WebDraw.STG_HTML_MOB_INS.replace(".idx.", idx).replace(".mode.", mode);
					}else{
						stgHtml += ML4WebDraw.STG_HTML_MOB.replace(".id_key.", id_key + "mobile").replace(".idx.", idx);
					}
					break;
				case 'smartcert':
					if(!isCsInstall || isCsUpdate){
						stgHtml += ML4WebDraw.STG_HTML_SMC_INS.replace(".idx.", idx).replace(".mode.", mode);
					}else{
						stgHtml += ML4WebDraw.STG_HTML_SMC.replace(".id_key.", id_key + "smartcert").replace(".idx.", idx);
					}
					break;
				case 'cloud':
					stgHtml += ML4WebDraw.STG_HTML_CLD;
					break;
				} // end of switch
					
			} // end of for
			
			if(stgArr.length > 5){
				stgHtml += '		</ul>';
				stgHtml += '	</div>';
				stgHtml += '</div>';
				//stgHtml += '<div class="item">';
				stgHtml += '	<div id="wrap_stg_02" class="ML_storage_area">';
				stgHtml += '		<ul>';
				
				for(var idx=5; idx<stgArr.length; idx++){
					
					switch(stgArr[i]){
						case 'web': 
							stgHtml += ML4WebDraw.STG_HTML_WEB.replace(".id_key.", idx + "web").replace(".idx.", idx);	
							break;
						case 'pfx':
							if(browser != "MSIE 9" && browser != "MSIE 8" && 
							   browser != "MSIE 7" && browser != "MSIE 6"){
								stgHtml += ML4WebDraw.STG_HTML_PFX.replace(".id_key.", idx + "pfx").replace(".idx.", idx);
							}else if(!isCsInstall || isCsUpdate){
								stgHtml += ML4WebDraw.STG_HTML_PFX_INS.replace(".idx.", idx).replace(".mode.", mode);
							}else{
								stgHtml += ML4WebDraw.STG_HTML_PFX_IE.replace(".id_key.", idx + "pfx").replace(".idx.", idx);
							}
							break;
						case 'hdd':
							if(!isCsInstall || isCsUpdate){
								stgHtml += ML4WebDraw.STG_HTML_HDD_INS.replace(".idx.", idx).replace(".mode.", mode);
							}else{
								stgHtml += ML4WebDraw.STG_HTML_HDD.replace(".id_key.", idx + "pfx").replace(".idx.", idx);
							}
						case 'shdd':
							if(!isCsInstall || isCsUpdate){
								stgHtml += ML4WebDraw.STG_HTML_SDD_INS.replace(".idx.", idx).replace(".mode.", mode);
							}else{
								stgHtml += ML4WebDraw.STG_HTML_SDD.replace(".id_key.", idx + "pfx").replace(".idx.", idx);
							}
						case 'token':
							if(!isCsInstall || isCsUpdate){
								stgHtml += ML4WebDraw.STG_HTML_TKN_INS.replace(".idx.", idx).replace(".mode.", mode);
							}else{
								stgHtml += ML4WebDraw.STG_HTML_TKN.replace(".id_key.", idx + "pfx").replace(".idx.", idx);
							}
						case 'mobile':
							if(!isCsInstall || isCsUpdate){
								stgHtml += ML4WebDraw.STG_HTML_MOB_INS.replace(".idx.", idx).replace(".mode.", mode);
							}else{
								stgHtml += ML4WebDraw.STG_HTML_MOB.replace(".id_key.", idx + "pfx").replace(".idx.", idx);
							}
						case 'smartcert':
							if(!isCsInstall || isCsUpdate){
								stgHtml += ML4WebDraw.STG_HTML_SMC_INS.replace(".idx.", idx).replace(".mode.", mode);
							}else{
								stgHtml += ML4WebDraw.STG_HTML_SMC.replace(".id_key.", idx + "pfx").replace(".idx.", idx);
							}
						case 'cloud':
							stgHtml += ML4WebDraw.STG_HTML_CLD;
							break;
						} // end of switch
					} // end of for
					
				} // end of if(stgArr.length >5)\
			
			}else{ 
				// 목록 없음
				stgHtml += '<li class="">'+$.i18n.prop("TS084")+'</li>';
			}
		
		stgHtml += '		</ul>';
		stgHtml += '	</div>';
		stgHtml += '</div>';
		document.getElementById('MLstrSlide').innerHTML = stgHtml;
		
		//메세지 처리 적용.
		MessageVO.applyMessage($('[id^="MSG_"]'));
		
	},


/*
 * ML4Web_Main.js 에서 옮겨옴. 인증서 목록을 출력하는 함수
 */
	MakeCertiListDiv : function(listObj) {
		//ML4WebLog.log("MakeCertiListDiv() called... listObj === " + JSON.stringify(listObj));
		//비번입력창 활성화/비활성화
		if(listObj != null && listObj.length > 0){
			$("#input_cert_pw").val('').prop("disabled",false);
			$("#input_cert_pw").css({ 'pointer-events': 'auto' });
			$("#keyboardOn").css({ 'pointer-events': 'auto' });
			if(document.getElementById('input_cert_pw_new')){
				$("#input_cert_pw_new").val('').prop("disabled",false);
				$("#input_cert_pw_new").css({ 'pointer-events': 'auto' });
				$("#keyboardOn").css({ 'pointer-events': 'auto' });
			}
		} else {
			$("#input_cert_pw").val('').prop("disabled",true);
			$("#input_cert_pw").css({ 'pointer-events': 'none' });
			$("#keyboardOn").css({ 'pointer-events': 'none' });
			if(document.getElementById('input_cert_pw_new')){
				$("#input_cert_pw_new").val('').prop("disabled",true);
				$("#input_cert_pw_new").css({ 'pointer-events': 'none' });
				$("#keyboardOn").css({ 'pointer-events': 'none' });
			}
		}
		
		var source = makeSourceData(listObj);
		var dataAdapter = new $.MLjqui.dataAdapter(source);
	
		//모바일 화면 적용 패치
		var os_ver = ML4WebApi.getProperty("os");
		var dt_height = "";
		if(os_ver=="Android" || os_ver=="IPHONE" || os_ver=="BlackBerry" || os_ver=="IPAD"){
			if(os_ver=="IPAD" || (os_ver=="Android" && navigator.userAgent.toUpperCase().indexOf("MOBILE") == -1)){
				dt_height = 350; 
			}else{
				dt_height = (($(window).height()) - 238);
			}
			
			 $("#dataTable").MLjquiDataTable({
	            theme:"DSdatatable",
				width: "100%",
				height: dt_height,
	            columnsHeight:25,
				altRows: false, // 행 백그라운드 교차 //
				autoRowHeight: false,
	            sortable: true,
	            pageable: false,
	            source: dataAdapter,
				enableHover: true,
	            columnsResize: true,
				selectionMode: "singleRow",
	            columns: [
	              { text: $.i18n.prop("TS063"), dataField: 'sectionValues', cellsAlign: 'left', align: 'center', cellsFormat: 'c2',
	                     cellsRenderer: function (row, column, value, rowData) {
	                          var container = '<div style="width: 100%; height: 100%;">'
	                          var leftcolumn = '<div style="float: left; width: 90px;">';
	                          var rightcolumn = '<div style="overflow:hidden;">';
	                  
	                          var image = "<div style='display:inline-block; padding-top:6px; padding-left:17px;'>";
	                          var isExpired = ML4WebUtil.isDateExpired(rowData.enddatetime);
	      					  if(isExpired){
	      						  imgurl = 'UI/images/ML_cert_expire_mobile.png';
	      					  }else{
	      						  imgurl = 'UI/images/ML_cert_normalcy_mobile.png';
	      					  }
	      					  //mj(인증서읽기)
	                          var img = '<a title="'+ rowData.Cn +'" tabindex="-1"><img width="80" height="98" style="display:block;" src="' + imgurl + '" alt=""/></>';
	                          image += img;
	                          image += "</div>";
	                          
	                          leftcolumn += image;
	                          leftcolumn += "</div>";
	
	                          var NamesValues = "<div style='margin: 10px; font-size:1.2em;'><b>" + rowData.Cn + "</b></div>";
	                          var issueValues = "<div style='margin: 10px; font-size:1.1em;'>발급자:" + rowData.Issuer + "</div>";
	                          var sectionValues = "<div style='margin: 10px; font-size:1.1em;'>구&nbsp;&nbsp;&nbsp;분:" + rowData.Policy + "</div>";
	                          var endDateValues = "<div style='margin: 10px; font-size:1.1em;'>만료일:" + rowData.enddate + "</div>";
	
	                          rightcolumn += NamesValues;
	                          rightcolumn += issueValues;
	                          rightcolumn += sectionValues;
	                          rightcolumn += endDateValues;
	                          rightcolumn += "</div>";
	                          container += leftcolumn;
	                          container += rightcolumn;
	                          container += "</div>";
							  
	                          return container;
	                     }			  
	              }
	            ],
	            ready:function(){
					if(listObj != null && listObj.length > 0){
						$("#dataTable").MLjquiDataTable('selectRow', 0);
					}
				}
	        });
		}else{
			$("#dataTable").MLjquiDataTable({
				theme:"DSdatatable",
				width: "398",
				height: "150",
				columnsHeight:25,
				altRows: true, // 행 백그라운드 교차 //
				autoRowHeight: false,
				sortable: true,
				pageable: false,
				source: dataAdapter,
				enableHover: true,
				columnsResize: true,
				selectionMode: "singleRow",
				columns: [
					{ text: $.i18n.prop("TS059"), dataField: 'Cn', width: 140, align: 'center',
						cellsRenderer: function (row, column, value, rowData) {
							var image = "<div style='display:inline-block; padding-right:5px;'>";
	
							var imgurl = '';
							var isExpired = ML4WebUtil.isDateExpired(rowData.enddatetime);
							if(isExpired){
								imgurl = 'UI/images/icon_cert_expire.png';
							}else{
								var rowEndDate = rowData.enddatetime;
								var a = rowEndDate.split(" ");
								var d = a[0].split("-");
								var t = a[1].split(":");
								var endDate = new Date(d[0],(d[1]-1),d[2],t[0],t[1],t[2]);
								var today = new Date();
								if (today < endDate) {
									diff = Math.floor(( Date.parse(endDate) - Date.parse(today) ) / 86400000);
									if(diff < 31){
										//시계 이미지
										imgurl = 'UI/images/icon_cert_expiring.png';
									}else{
										imgurl = 'UI/images/icon_cert_default.png';
									}
								}
							}
							if(typeof(rowData.cloud) != "undefined"){
								if(rowData.cloud){
									imgurl = 'UI/images/cert_selected_in_cloud.png';
								}
							}
	
							var img = '<img width="16" height="16" style="display:block;" src="' + imgurl + '" alt=""/>';
							image += img;
							var tooltip = "</div><span onclick='clickList(event);' style='vertical-align:top; padding-top:2px;' title='" + rowData.Cn + "'>" + rowData.Cn + "</span>";
							return image+=tooltip;
						}
					},
					{ text: $.i18n.prop("TS060"), dataField: 'Policy', width: 100, align: 'center',
						cellsRenderer: function (row, column, value, rowData) {
	
							var image = "<div style='display:inline-block; padding-right:5px;'>";
	
							var imgurl = '';
							var isExpired = ML4WebUtil.isDateExpired(rowData.enddatetime);
							if(isExpired){
								imgurl = 'UI/images/icon_cert_expire.png';
							}else{
								imgurl = 'UI/images/icon_cert_default.png';
							}
							image += "</div><span style=' vertical-align:top; padding-top:2px;' title='" + rowData.Policy + "'>" + rowData.Policy + "</span>";
							//var img = '<img width="16" height="16" style="display:block;" src="' + imgurl + '"/>';
							//image += img;
							//image += "</div><span style='display:inline-block; vertical-align:top; padding-top:2px;'>" + rowData.Policy + "</span>";
							//image += "</div><span style='display:inline-block; vertical-align:top; padding-top:2px;' title='" + rowData.Policy + "'>" + rowData.Policy + "</span>";
							return image;
						}
					},
					{ text: $.i18n.prop("TS061"), dataField: 'Issuer', cellsAlign: 'left', align: 'center', cellsFormat: 'c2' },
					{ text: $.i18n.prop("TS062"), dataField: 'enddate', width: 78, cellsAlign: 'left', align: 'center', cellsFormat: 'd',
						cellsrenderer : function(row, column, value, rowData) {
							var enddateSpan = "";
							enddateSpan = "<span style='vertical-align:top; padding-top:2px;'>" + rowData.enddate + "</span>";
							enddateSpan += "<input type='hidden' id='rowEnddate' value='" + rowData.enddatetime + "'>";
							return enddateSpan;
						}
					}
				],
				ready:function(){
					if(listObj != null && listObj.length > 0){
						$("#dataTable").MLjquiDataTable('selectRow', 0);
					}
				}
			});
		}
	
		if (listObj!=null && $.isEmptyObject($("#dataTable").MLjquiDataTable('selectRow')) && listObj.length>0){
			$("#dataTable").MLjquiDataTable('selectRow', 0);
			
			/*// 220316 issuer 명칭 변경
			for(var i=0; i< $("#tabledataTable tr").length; i++){
				var korIssuer = ""
				var engIssuer = $("#row"+i+"dataTable td:eq(2)").text();
				
				
				if( engIssuer == "CA131100001" ){	// 공무원
					korIssuer = "공무원";		// 한글명 ""에 설정
					$("#row"+i+"dataTable td:eq(2)").text(korIssuer);
				}//else if( engIssuer == "" ){} .... 다른 발급기관명 변경시 추가하기
			}*/
		}
		
		// 인증서 만료일 한달전에 해당 인증서 마우스 오버시 유효기간 공지 
		$("#dataTable tbody").on("mouseover", "tr", function(event){
			var selection = $("#dataTable").MLjquiDataTable('getSelection');
			var selectionCnt = selection.length;
			
			if( selection && selectionCnt > 0 ){
				var diff = '';
				//선택한 row의 enddate 갖고오기
				var rowEndDate = $(this).find('input[type="hidden"]').val();
				var a = rowEndDate.split(" ");
				var d = a[0].split("-");
				var t = a[1].split(":");
				var endDate = new Date(d[0],(d[1]-1),d[2],t[0],t[1],t[2]);
				var today = new Date();
				if (today < endDate) {
					diff = Math.floor(( Date.parse(endDate) - Date.parse(today) ) / 86400000);
					//alert(diff + "일 남았습니다.");
					if(diff < 31){
						// 마우스오버된 
						var rowID = $(this).context.getAttribute("id");
						rowID = "#" + rowID;
//						var strAlert = "선택하신 인증서는 "
//										+ rowEndDate + "(남은 기간 " + diff + "일)에 만료 예정입니다.\n"
//										+ "인증서를 발급받은 기관의 공동인증센터에서 인증서를 갱신하시기 바랍니다.";
//						$(rowID).attr("title", strAlert);
						
						var strAlert = "<div style='padding:2px 2px 0px 4px;'>선택하신 인증서는 "
										+ rowEndDate + "(남은 기간 " + diff + "일)에 만료 예정입니다.</div>"
										+ "<div style='padding:0px 2px 2px 4px;'>인증서를 발급받은 기관의 공동인증센터에서 인증서를 갱신하시기 바랍니다.</div>";
						var thisIsTop = $(this).offset().top + 25;
						var thisIsLeft = $(this).offset().left;
						var divArea = $("<div name='speechBubbleArea' id='"+rowID+"' style='position:absolute;border:1px solid #333;height:39px;z-index:18004;background:#F1F1F1;'><span>"+strAlert+"</span></div>");
						divArea.css("top",thisIsTop+"px");
						divArea.css("left",thisIsLeft+"px");
						$("body").append(divArea);
					}
				} else {
					// 만료된 인증서 마우스 오버시 만료안내 공지
					var rowID = $(this).context.getAttribute("id");
					rowID = "#" + rowID;
					var strAlert = "<div style='padding:2px 2px 0px 4px;'>선택하신 인증서는 만료되었습니다.</div>";
					var thisIsTop = $(this).offset().top + 25;
					var thisIsLeft = $(this).offset().left;
					var divArea = $("<div name='speechBubbleArea' id='"+rowID+"' style='position:absolute;border:1px solid #333;height:20px;z-index:18004;background:#F1F1F1;'><span>"+strAlert+"</span></div>");
					divArea.css("top",thisIsTop+"px");
					divArea.css("left",thisIsLeft+"px");
					$("body").append(divArea);
				}
			}
		});
		
		$("#dataTable tbody").on("mouseout", "tr", function(event){
			$("body").find("div[name='speechBubbleArea']").remove();
		});
		
		//table row(인증서) 선택 event catch
		$("#dataTable").on('rowSelect', function(e){
			var args = e.args;
			var index = args.index;
			var rowData = args.row;
			var rowKey = args.key;
	//		ML4WebLog.log("dataTable.rowSelect.args === " +args);
	//		ML4WebLog.log("dataTable.rowSelect.index === " +index);
	//		ML4WebLog.log("dataTable.rowSelect.rowData === " + JSON.stringify(rowData));
	//		ML4WebLog.log("dataTable.rowSelect.rowKey === " +rowKey);
		});
	
		//웹접근성 포커스 유지를 위한 아래 내용 주석처리
		//$("#dataTable").focus();
	},
	openCSInstallDialog : function(mode){
		
		var browser = ML4WebApi.getProperty('browser');
		var os_ver = ML4WebApi.getProperty("os");
		
		if(os_ver == ""){
			os_ver = getOS();
		}
		
		if(os_ver.indexOf("LINUX") > -1){
			$("#csContainer").load("UI/ML4Web_Cs_Linux_Install.html?random=" + Math.random() * 99999, function(){
				$('#ML_install').MLjquiWindow('open', function(e){});
				
			});
		}else{
			var popOption = {
				mode: mode,
				title:$.i18n.prop("TS039"),
				showCert:false,
				certData:null,
				onclick:installProgram,
				contentKey:"install_cs",
				dialogHeight:"180px"
			};
		}
		
		//20200313 mj
		DSDialog.openDialog(popOption, null, function(code,jsonObj){});
	},
	openCSUpdateDialog : function(mode){
		
		var browser = ML4WebApi.getProperty('browser');
		var os_ver = ML4WebApi.getProperty("os");
		
		if(os_ver == ""){
			os_ver = getOS();
		}
		
		if(os_ver.indexOf("LINUX") > -1){
			$("#csContainer").load("UI/ML4Web_Cs_Linux_Install.html?random=" + Math.random() * 99999, function(){
				$('#ML_install').MLjquiWindow('open', function(e){});				
			});
		}else{
			var popOption = {
					mode: mode,
					title:$.i18n.prop("TS040"),
					showCert:false,
					certData:null,
					onclick:installProgram,
					contentKey:"install_update",
					dialogHeight:"225px"
			};
		}
		
		//20200313 mj
		DSDialog.openDialog(popOption, null, function(code,jsonObj){});
	},
	
// 에러 alert dialog 처리
	errorHandler : function(mode, errCode, obj, callback) {
		
		// TODO : 에러 코드별로 분기 처리
		/* 
		 * switch(errCode){
		 * 	case 1:
		 *  case 2:
		 *  	//
		 *  case 3:
		 *  	//
		 *  default :
		 *  	//
		 * }
		 * 
		 */
		
		// alert 호출
		if(typeof(errCode) != "undefined"){
			DSAlert.openAlert(mode, errCode, obj);
		}
		
		
		// 함수 처리
		if(typeof(callback) === "function"){		
			// TODO
			callback();
		}
	},
	confirm : function( message, callback ){
		
		var popOption = {
			mode:"main",
			title:$.i18n.prop("TS014"),
			showCert:false,
			certData:{},
			contentKey:"confirm",
			dialogHeight:190,
			message:message
		};
		//20200310 mj
		var btnObj = $("#stg_"+ selectMedia);
		
		DSDialog.openConfirm(popOption, btnObj, function(code,jsonObj){
			$("#btn_common_confirm").unbind().click( function(){
				callback(0,true);
				//20200310 mj
				btnObj.focus();
				return;
			});
			$("#btn_common_cancle").unbind().click( function(){
				callback(1,false);
				return;
			});
		});
	}
};


var ML4WebSaveCertDraw = {
		
	hddOptionMap : {
		// HDD 스토리지 선택 정보 저장을 위해 사용
	},		
	initDraw : function(){
		
		// 인증서 저장 창 그리기
		this._createWindow();
			
		// 메시지 초기화
		MessageVO.applyMessage($('[id^="MSG_"]'));
		
		//key event 
		$(this).keydown(function(e){
			if(e.keyCode == "9"){
				var targetElement = $(document.activeElement).attr("id");
				var parentElement = $(document.activeElement).parent().attr("id");
				//console.log("targetElement.id === " + targetElement);
				//console.log("parentElement.id === " + parentElement);
	
				if(targetElement == "stg_smartcert" ){
					var selection = $("#dataTable").MLjquiDataTable('getSelection');
					if(selection && selection.length>0){

					}else{

					}
				} else if(targetElement == "btn_cancel" ){
					$(".ML_storage_area>ul>li:first-child").focus();
				} else if(targetElement == "btn_file_cs" ){
					$("#import_pfx_password").focus();
				} else if(targetElement == "file_route" ){
	
				} else if(targetElement == null || targetElement == 'undefined'){
					
				}
			}else if (e.keyCode == "27"){
				console.log("Esc keydown Esc keydown Esc keydown Esc keydown");
			}
		});
		
		// Slide L&R Button
		$(".customNavigation").click(function(){
			var X=$(this).attr('id');
			if(X==1){
				$(this).children(".str_add_li").removeClass('pr');
				$(this).children(".str_add_li").addClass('ne');
				$(this).attr('id', '0');
	
				$("#wrap_stg_01").animate({left:"0px"}, 250);
				$("#wrap_stg_02").animate({left:"375px"}, 250);
			}else{
				$(".str_add_li").attr('class', 'str_add_li pr');
				$(this).children(".str_add_li").removeClass('ne');
				$(this).children(".str_add_li").addClass('pr');
				$(".customNavigation").attr('id', '0');
				$(this).attr('id', '1');
				
				$("#wrap_stg_01").animate({left:"-375px"}, 250);
				$("#wrap_stg_02").animate({left:"0px"}, 250);
			}
		});
	
		$(".stg_01").focus(function(){
			$("#wrap_stg_01").animate({left:"0px"}, 250);
			$("#wrap_stg_02").animate({left:"375px"}, 250);
			$(".customNavigation").children(".str_add_li").removeClass('pr');
			$(".customNavigation").children(".str_add_li").addClass('ne');
			$(".customNavigation").attr('id', '0')
		});
	
		$(".stg_02").focus(function(){
			$("#wrap_stg_01").animate({left:"-375px"}, 250);
			$("#wrap_stg_02").animate({left:"0px"}, 250);
			$(".customNavigation").children(".str_add_li").removeClass('ne');
			$(".customNavigation").children(".str_add_li").addClass('pr');
			$(".customNavigation").attr('id', '1')
		});
		
	},
	_createWindow : function() {
			
		if( ! ML4WebApi.getProperty("banner") ){
			$(".ML_cp_AD").remove();
		}
		var window_height = 558;
		var window_width = 418;
		var popupWidth = document.body.clientWidth;
		
		var os_ver = ML4WebApi.getProperty("os");
		var jqxWidget = $('#ML_window');
		var offset = jqxWidget.offset();
		var xValue = offset.left + (($(window).width()/2)-(window_width/2));
		var position = { x: xValue, y: offset.top + 50};
		var popupPosition =  {x: offset.left + ($(window).width()/2) - 200, y: offset.top + 200};
		
		if((os_ver=="Android" && navigator.userAgent.toUpperCase().indexOf("MOBILE") >-1)|| os_ver=="IPHONE" || os_ver=="BlackBerry" || os_ver=="IPAD" || os_ver=="Android"){
			isBanner = false;
			position = 'center';
			popupPosition = 'center';
			popupWidth = 300;
		}else{
			popupWidth = 398;
		}
		
		//-00. Main
		$("#ML_window").MLjquiWindow({
			title: $.i18n.prop("TS085"),
			resizable: false,
			position:position,
			showCloseButton:false,
			isModal: true,
			modalOpacity: 0.3,
			modalZIndex: 9995,
			modalBackgroundZIndex: 9995,
			showCollapseButton: false,
			draggable: false,
			keyboardCloseKey: 0,
			maxHeight: 600, maxWidth:720,
			minHeight: 200, minWidth: 200,
			height: window_height, width: window_width,
			initContent: function () {
				$('#ML_window').MLjquiWindow('focus');
				if( os_ver === "IPHONE" ){
					var orientation = window.orientation;
					if( orientation == 0 ){
						var iWidth  = screen.width;
						var iHeight = screen.height;
						
						if(iHeight >= 800){
							iHeight = 170;
						}else if(iHeight >= 600){
							iHeight = 150;
						}else if(iHeight >= 500){
							iHeight = 140;
						}
					
						iWidth  = iWidth  + "px";
						iHeight = iHeight + "vw";
											
						window.parent.document.getElementById('dscert').style.width  = iWidth;
						window.parent.document.getElementById('dscert').style.height = iHeight;
					}else if(orientation == 90){
						iHeight = (screen.width) - 323;
						iHeight = iHeight + 'vw';
	
						window.parent.document.getElementById('dscert').style.height = iHeight;
					}else if(orientation == -90){
						iHeight = (screen.width) - 323;
						iHeight = iHeight + 'vw';
						
						window.parent.document.getElementById('dscert').style.height = iHeight;
					}
				}
			}
		});
		
		$('#popup_alert').MLjquiWindow({
			title: $.i18n.prop("TS042"),
			resizable: false,	
			position: popupPosition,
			showCloseButton:false,
			zIndex:9990,
			showCollapseButton: false,
			keyboardCloseKey: 0,
			maxHeight: 600, maxWidth:420,
			minHeight: 200, minWidth: 200,
			height: 148, width: popupWidth,
			isModal: true,
			modalOpacity: 0.3,
			modalZIndex: 9995,
			draggable: false,
			modalBackgroundZIndex: 9995,
			initContent: function () {
				$('#popup_alert').MLjquiWindow('close');
			}
		});
		
		//-02. Common
		//$('#ML_Dialog_common').show();
		$('#ML_Dialog_common').MLjquiWindow({
		//	autoOpen: false,
		//	title:'비밀번호변경',
			resizable: false,
			//position: 'center',
			position:popupPosition,
			showCloseButton:false,
		//	closeButtonSize: 35,
		//	closeButtonAction:'close',
		//	draggable:false,
			isModal: true,
			modalOpacity: 0.3,
			modalZIndex: 9996,
			keyboardCloseKey: 0,
			modalBackgroundZIndex: 9996,
			showCollapseButton: false,
			width: popupWidth,
			initContent: function () {
				$('#ML_Dialog_common').MLjquiWindow('close');
			}
		});
			
		//-03. Cs install dialog
		$('#ML_dialog_cs_install').MLjquiWindow({
			resizable: false,
			//position: 'center',
			position: popupPosition,
			showCloseButton:false,
			isModal: false,
			modalOpacity: 0.3,
			modalZIndex: 9996,
			keyboardCloseKey: 0,
			modalBackgroundZIndex: 9996,
			showCollapseButton: false,
			width: popupWidth,
			initContent: function () {
				$('#ML_dialog_cs_install').MLjquiWindow('close');
			}
		});
	},
	MakeSaveCertDiv : function(certInfo) {
		
		// 인증서 목록에 해당 정보 출력
		var listObj = new Array();
		listObj.push(certInfo);
		var source = makeSourceData(listObj);
		var dataAdapter = new $.MLjqui.dataAdapter(source);
		var certOpt = new Object();
		certOpt.storageName = "";
		
		$("#dataTable").MLjquiDataTable({
			theme:"DSdatatable",
			width: "398",
			height: "150",
			columnsHeight:25,
			altRows: true, // 행 백그라운드 교차 //
			autoRowHeight: false,
			sortable: true,
			pageable: false,
			source: dataAdapter,
			enableHover: true,
			columnsResize: true,
			selectionMode: "singleRow",
			columns: [
				{ text: $.i18n.prop("TS059"), dataField: 'Cn', width: 140, align: 'center',
					cellsRenderer: function (row, column, value, rowData) {
						var image = "<div style='display:inline-block; padding-right:5px;'>";

						var imgurl = '';
						var isExpired = ML4WebUtil.isDateExpired(rowData.enddatetime);
						if(isExpired){
							imgurl = 'UI/images/icon_cert_expire.png';
						}else{
							imgurl = 'UI/images/icon_cert_default.png';
						}
						
						//20200324
						var img = '<img width="16" height="16" style="display:block;" src="' + imgurl + '" alt=""/>';
						image += img;
						var tooltip = "</div><span style='vertical-align:top; padding-top:2px;' title='" + rowData.Cn + "'>" + rowData.Cn + "</span>";
						return image+=tooltip;
					}
				},
				{ text: $.i18n.prop("TS060"), dataField: 'Policy', width: 100, align: 'center',
					cellsRenderer: function (row, column, value, rowData) {

						var image = "<div style='display:inline-block; padding-right:5px;'>";

						var imgurl = '';
						var isExpired = ML4WebUtil.isDateExpired(rowData.enddatetime);
						if(isExpired){
							imgurl = 'UI/images/icon_cert_expire.png';
						}else{
							imgurl = 'UI/images/icon_cert_default.png';
						}
						image += "</div><span style=' vertical-align:top; padding-top:2px;' title='" + rowData.Policy + "'>" + rowData.Policy + "</span>";
						//var img = '<img width="16" height="16" style="display:block;" src="' + imgurl + '"/>';
						//image += img;
						//image += "</div><span style='display:inline-block; vertical-align:top; padding-top:2px;'>" + rowData.Policy + "</span>";
						//image += "</div><span style='display:inline-block; vertical-align:top; padding-top:2px;' title='" + rowData.Policy + "'>" + rowData.Policy + "</span>";
						return image;
					}
				},
				{ text: $.i18n.prop("TS061"), dataField: 'Issuer', cellsAlign: 'left', align: 'center', cellsFormat: 'c2' },
				{ text: $.i18n.prop("TS062"), dataField: 'enddate', width: 78, cellsAlign: 'left', align: 'center', cellsFormat: 'd'}
			], ready:function(){
				$("#dataTable").MLjquiDataTable('selectRow', 0);
			}
		});
		
		// 선택 강제
		if(listObj != null && listObj.length > 0){
			$("#dataTable").MLjquiDataTable('selectRow', 0);
		}
		
		
		$('[id^="stg_"]').click(function () {
			
			//UI driver 선택창 위치 조정
			var stg_idx 		= $(this).attr("stgIdx");
			var selectedStorage = this.id.split("stg_")[1];
			
			selectMedia = selectedStorage;
			
			// btn_confirm 참조 값인 storage 명 셋팅
			certOpt.storageName = selectedStorage;
			ML4WebUI.selectedStorage.current_option = certOpt;
			
			$('#driver_div').removeClass("pos_01").removeClass("pos_02").removeClass("pos_03").removeClass("pos_04").removeClass("pos_05");
			$('#driver_div').addClass("pos_0"+stg_idx);
			
			//UI css selection 처리
			$('[class^="ML_storage_"]').removeClass("on");
			$(this).parent().addClass("on");

			// 드라이버 선택창 닫기
			closeDriverDialog();
			
			if(!magiclineUtil.isMobile(magiclineUtil.getOS())){
				if( ML4WebApi.webConfig.useVirtualKeyboard ){
					if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
						var npLength = npVCtrl.keypadObject.length;
						if(npLength > 1){
							for (var i=0; i<npLength; i++){
								if("nppfs-keypad-input_cert_pw" == npVCtrl.keypadObject[i]._uuid){
									npVCtrl.keypadObject.splice(i,1);
									break;
								}
							}
						}
					}
				}
			}
			
			if(selectedStorage == "hdd"){
				
				ML4WebApi.ml4web_storage_api.SelectStorageInfo("hdd", function(resultCode, jsonObj){					
					
					if(resultCode == 0){
						
						if(jsonObj != null){
							
							// 디폴트 스토리지 HDD 선택시, 디스크 아이콘 선택시 첫번쨰 디스크 선택 된것으로 간주							
							ML4WebUI.selectedStorage.option = jsonObj;																					
							ML4WebSaveCertDraw.hddOptionMap = new Object();
							
							for(var i=0; i<jsonObj.hddOpt.length; i++){								
								ML4WebSaveCertDraw.hddOptionMap[jsonObj.hddOpt[i].diskname] = "DUMMY";								
							}
							
							ML4WebUI.selectedStorage.current_option.storageOpt = jsonObj.hddOpt[0].diskname;
							ML4WebSaveCertDraw.showSelectedStorageName();
							
						}else{
							//no list
							//ML4WebSaveCertDraw.openDriverDialog(selectedStorage, null);
						}
						
					}else{
						// Nothing To do
					}
				});
			}
			
			ML4WebSaveCertDraw.showSelectedStorageName();
			
			// 스토리지 선택 노출 이벤트 처리
			$(document).on("click", function(event){
				
				// 1. HDD 디스크 옵션 선택 처리
				// 2. HDD 디스크 옵션 하위 메뉴 출력 처리
				var currentText = event.target.innerText;
				
				if($("#sub_drv_list").length > 0 && ML4WebSaveCertDraw.hddOptionMap[currentText]){										
					ML4WebUI.selectedStorage.current_option.storageOpt = currentText;															
					ML4WebSaveCertDraw.showSelectedStorageName();
					closeDriverDialog();
				}else if(ML4WebUI.selectedStorage.current_option.storageName == "hdd" && event.target.id.indexOf("stg_") == 0 && ML4WebUI.selectedStorage.option != null){
					ML4WebSaveCertDraw.openDriverDialog(selectedStorage, ML4WebUI.selectedStorage.option);
				}											
			});
			
		});
		
		//메세지 처리 적용.
		MessageVO.applyMessage($('[id^="MSG_"]'));
		
	},
	showSelectedStorageName : function(){
		
		var htmlStream = "인증서 저장 위치 : ";
		
		switch(ML4WebUI.selectedStorage.current_option.storageName){
			case "web" :
				htmlStream += "브라우저";
				break;
			case "hdd" :												
				htmlStream += ML4WebUI.selectedStorage.current_option.storageOpt;												
				break;
			default :
				break;
		}
		$("#ML_dp_03").empty().html(htmlStream);
		
	},		
	openDriverDialog : function(id, obj){		
		var htmlStream = "";
		
		if(id == "mobile"){
			htmlStream += '<ul id="sub_drv_list" class="drive_position_menu wdh_220">';
		}else if(id == "hdd"){
			htmlStream += '<ul id="sub_drv_list" class="drive_position_menu wdh_210">';
		}else{
			htmlStream += '<ul id="sub_drv_list" class="drive_position_menu wdh_150">';
		}
		
		if(id == "hdd"){
			
			var hddOptCnt = obj.hddOpt.length;			
			for(var i=0; i<hddOptCnt; i++){
				
				if(i==(obj.hddOpt.length-1)){
					htmlStream += '	<li id="'+id+'_driver_'+i+'" ><a href="#" class="wdh_210" onblur="closeSubDriverList();">'+obj.hddOpt[i].diskname+'</a></li>';					
				}else{
					htmlStream += '	<li id="'+id+'_driver_'+i+'" ><a href="#" class="wdh_210" >'+obj.hddOpt[i].diskname+'</a></li>';
				}				
			}
		}		
		htmlStream += "</ul>";
			
		$('#driver_div').empty().html(htmlStream);
		$('#driver_div').show();
			
		$('#sub_drv_list>li:first-child>a').focus();
		
	}
	
};
window.onorientationchange = function() {
	var jqxwidget2 = $('#ML_window');
	var offset2 = jqxwidget2.offset();
	
	var os_ver = ML4WebApi.getProperty("os");
	if(os_ver == "IPHONE"){
		var orientation = window.orientation;
		if(orientation == 0){
			var iWidth  = screen.width;
			var iHeight = screen.height;
			
			if(iHeight >= 800){
				iHeight = 170;
			}else if(iHeight >= 600){
				iHeight = 150;
			}else if(iHeight >= 500){
				iHeight = 140;
			}

			iWidth  = iWidth  + "px";
			iHeight = iHeight + "vw";
			
			window.parent.document.getElementById('dscert').style.width  = iWidth;
			window.parent.document.getElementById('dscert').style.height = iHeight;
			
			detailWidth = screen.width;
			detailHeight = screen.height;
			
		}else{
			if(screen.width <= 323){
				iHeight = (screen.width) - 265;
			}else{
				iHeight = (screen.width) - 323;
			}
			iHeight = iHeight + 'vw';
			
			window.parent.document.getElementById('dscert').style.width  = screen.height + 'px';
			window.parent.document.getElementById('dscert').style.height = iHeight;
			
			if(screen.height >= 800){
				detailWidth = screen.height - 100
			}else{
				detailWidth = screen.height;
			}
			detailHeight = screen.width;
		}
	}else if(os_ver == "IPAD"){
		var orientation = window.orientation;
		if(orientation == 0){
			var iWidth  = screen.width;
			var iHeight = screen.height;
			
			iWidth  = iWidth  + "px";
			iHeight = iHeight-100 + "px";
			
			window.parent.document.getElementById('dscert').style.width  = iWidth;
			window.parent.document.getElementById('dscert').style.height = iHeight;
		}else{
			iHeight = screen.width-100 + 'px';
			
			window.parent.document.getElementById('dscert').style.width  = screen.height + 'px';
			window.parent.document.getElementById('dscert').style.height = iHeight;
		}
	}else{
		detailWidth = screen.width;
		detailHeight = screen.height;
	}
	$('#ML_window_detail').MLjquiWindow({
		position:{x: offset2.left, y: offset2.top},
		width:detailWidth,height:detailHeight
	});
}

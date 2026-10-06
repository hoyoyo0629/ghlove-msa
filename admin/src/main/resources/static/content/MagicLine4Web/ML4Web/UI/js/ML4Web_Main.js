
var keyboardCheck = false;
var keyPair = "";

$(document).ready(function(){
	var browser = ML4WebApi.getProperty('browser');
	
	// IE 8,7,6일 때 CapsLock 작동 안함
	if((browser != 'MSIE 8') && (browser != 'MSIE 7') && (browser != 'MSIE 6') && (browser.toLowerCase().indexOf("safari") < 0)) {
		if(document.getElementById('input_cert_pw')){
			document.querySelector('#input_cert_pw').addEventListener('keyup', checkCapsLock);
			document.querySelector('#input_cert_pw').addEventListener('mousedown', checkCapsLock);
		}else if(document.getElementById('input_cert_pw_save')){
			document.querySelector('#input_cert_pw_save').addEventListener('keyup', checkCapsLock);
			document.querySelector('#input_cert_pw_save').addEventListener('mousedown', checkCapsLock);
		}
	} 
	
	$('#input_cert_pw').blur(function(e){
		$("#capslock").hide();
	});
	
	$('#input_cert_pw_save').blur(function(e){
		$("#capslock").hide();
	});
	
});

//tab으로 이동시 포커스 함께 이동
var rowIndexCheck = 0;
function tabFocus(e){
	//재정렬된 리스트 첫번째 행 클릭
	if(e.parentNode.rowIndex == 0 && e.cellIndex == 0){
		//$("#dataTable").MLjquiDataTable('_handleKey', e);
		rowIndexCheck = e.parentNode.rowIndex;
	} else if(rowIndexCheck < e.parentNode.rowIndex){
		// 다음줄 이동
		if(e.cellIndex == 0){
			//e.keyCode = 40;
			//$("#dataTable").MLjquiDataTable('_handleKey', e);
			rowIndexCheck = e.parentNode.rowIndex;
		}
	} else if(rowIndexCheck > e.parentNode.rowIndex){
		// 이전줄 이동
		if(e.cellIndex == 3){
			//e.keyCode = 38;
			//$("#dataTable").MLjquiDataTable('_handleKey', e);
			rowIndexCheck = e.parentNode.rowIndex;
		}
	}
}

//인증서 리스트 셀 클릭 시 포커스 효과
function certListClick(e){
	rowIndexCheck = e.parentNode.rowIndex;
	//$(e).focus();
	//$(e).css("outline", "none");
}

// CapsLock 상태 알림
function checkCapsLock(e) {
	var caps_lock_on = e.getModifierState('CapsLock');
	
	if(caps_lock_on == true){
		if(((ML4WebUI.selectedStorage.key == 'web_kftc' || ML4WebUI.selectedStorage.key == 'web') 
			&& !ML4WebApi.webConfig.useVirtualKeyboard && ML4WebApi.webConfig.virtualKeyboardType != "")
			|| (ML4WebApi.webConfig.useVirtualKeyboard && ML4WebApi.webConfig.virtualKeyboardType != ""))
			$("#capslock").hide();
		else if ((!ML4WebApi.webConfig.useVirtualKeyboard && ML4WebApi.webConfig.virtualKeyboardType != "")
				|| (ML4WebApi.webConfig.useVirtualKeyboard && ML4WebApi.webConfig.virtualKeyboardType === "")
				|| (!ML4WebApi.webConfig.useVirtualKeyboard && ML4WebApi.webConfig.virtualKeyboardType === ""))
			$("#capslock").show();
	} else
		$("#capslock").hide();
}

function add_checkCapsLock(e) {
	var caps_lock_on = e.getModifierState('CapsLock');

	if(caps_lock_on == true && !ML4WebApi.webConfig.useVirtualKeyboard && ML4WebApi.webConfig.virtualKeyboardType === "") 
		$("#add_capslock").show();
	else
		$("#add_capslock").hide();
}

function keypadOn(){
	if($("#file_route2").val() != ""){	
		$('#add_browser_password').attr("style","ime-mode:active;");
		
		if( ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
			keyboardCheck = true;
			if(typeof initTranskey=="function") {
				transkey["add_browser_password"] = null;
				initTranskey();
			}
			//raon keypad 위치 가운데로 수정
			var addPwdTop = $("#add_browser_password").offset().top;
			var addPwdLeft = $("#add_browser_password").offset().left;
			addPwdTop = addPwdTop + 30;
			addPwdLeft = addPwdLeft - 205;
			$('#add_browser_password').attr("data-tk-kbdxy", addPwdLeft + " " + addPwdTop);
			
			tk.onKeyboard(document.getElementById('add_browser_password'));
		}else if( ML4WebApi.webConfig.virtualKeyboardType === "DREAM" ){
			ML4WebVKey.showKeyboard("add_browser_password");
		}
	}
}

function initKeyPad(){
	$('#add_browser_password').removeAttr("style");
	keyboardCheck = false;
	if(!keyboardCheck){
		if(ML4WebApi.getProperty("storageList").indexOf("web_kftc") > -1 || ML4WebApi.getProperty("storageList").indexOf("kftc") > -1 || ML4WebApi.getProperty("storageList").indexOf("web") > -1){
			if( ML4WebApi.webConfig.virtualKeyboardType === "NSHC" ){
				$('#add_browser_password').val("");
				keyboardCheck = true;
			}
			if( ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
				 $("#add_browser_password").val('').prop("disabled",true);
				 $('#add_browser_password').val("");
			}
			if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
				$('#add_browser_password').val("");
				$("#form1").attr("style", "display:block;");
				$("#nppfs-keypad-add_browser_password").css("z-index", 111111);
			}
		}else{
			if( ML4WebApi.webConfig.useVirtualKeyboard ){
				if( ML4WebApi.webConfig.virtualKeyboardType === "NSHC" ){
					$('#add_browser_password').val("");
					keyboardCheck = true;
				}
				if( ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
					 $("#add_browser_password").val('').prop("disabled",true);
					 $('#add_browser_password').val("");
				}
				if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
					$('#add_browser_password').val("");
					$("#form1").attr("style", "display:block;");
					$("#nppfs-keypad-add_browser_password").css("z-index", 111111);
				}
			}
		}
	}
}

/*
 * ML4Web_Main.jsp 처리하는 이벤트 핸들러 정의
 */
// 가상키보드 활성화
$('#keyboardOn').click(function(){
	var selection = $("#dataTable").MLjquiDataTable('getSelection');
	var selectionCnt = selection.length;
	
	var pw_id = 'input_cert_pw';
	
	if(selectionCnt > 0){
		if(document.getElementById('input_cert_pw_save') != null){
			pw_id = 'input_cert_pw_save';
		}
		
		$('#'+pw_id).attr("style","ime-mode:active;");
		keyboardCheck = true;
		if(selectMedia === "web_kftc" || selectMedia === "web"){
			if( ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
				tk.onKeyboard(document.getElementById(pw_id));
			}else if( ML4WebApi.webConfig.virtualKeyboardType === "DREAM" ){
				ML4WebVKey.showKeyboard(pw_id);
			}
		}else{
			if( ML4WebApi.webConfig.useVirtualKeyboard && ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
				tk.onKeyboard(document.getElementById(pw_id));
			}else if( ML4WebApi.webConfig.useVirtualKeyboard && ML4WebApi.webConfig.virtualKeyboardType === "DREAM" ){
				ML4WebVKey.showKeyboard(pw_id);
			}
		}
	}
});

// 가상키보드로 입력 한 경우 value 초기화
$('#input_cert_pw').click(function(){

	$('#input_cert_pw').removeAttr("style");
	keyboardCheck = false;
	if(!magiclineUtil.isMobile(magiclineUtil.getOS())){
		if(!keyboardCheck){
			if(selectMedia === "web_kftc" || selectMedia === "web"){
				if( ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
					// 브라우저 저장소에서 비밀번호 입력칸 클릭시 라온 가상키패드 출력
					$("#input_cert_pw").val('').prop("disabled",true);
					
					var selection = $("#dataTable").MLjquiDataTable('getSelection');
					var selectionCnt = selection.length;
					
					var pw_id = 'input_cert_pw';
					
					if(selectionCnt > 0){
						if(document.getElementById('input_cert_pw_save') != null){
							pw_id = 'input_cert_pw_save';
						}
						
						$('#'+pw_id).attr("style","ime-mode:active;");
						keyboardCheck = true;
						tk.onKeyboard(document.getElementById(pw_id));
					}
					
				}else if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
					$('#input_cert_pw').val("");
					$("#form1").attr("style", "display:block;");
					$("#nppfs-keypad-input_cert_pw").css("z-index", 111111);
				}else if( ML4WebApi.webConfig.virtualKeyboardType === "DREAM" ){
					//ML4WebVKey.showKeyboard();
				}
			}else{
				if( ML4WebApi.webConfig.useVirtualKeyboard ){
					if( ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
						$("#input_cert_pw").val('').prop("disabled",true);
					}else if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
						$('#input_cert_pw').val("");
						$("#form1").attr("style", "display:block;");
						$("#nppfs-keypad-input_cert_pw").css("z-index", 111111);
					}else if( ML4WebApi.webConfig.virtualKeyboardType === "DREAM" ){
						//ML4WebVKey.showKeyboard();
					}
				}
			}
		}
	}
	$('#input_cert_pw').val("");
	
});
// 엔터키 처리
function signEnterKeyEvent(e) {
	if (e.keyCode == 13) {
		$('#btn_confirm_iframe').click();
	} else {
		e.keyCode == 0;
		return;
	}
}

function manualClose(e){
	if(e.keyCode == 13){
		$("#manual_close").removeAttr("tabindex");
		$("#manual_img").removeAttr("tabindex");
		$("#browser_manual").attr("style", "display:none");
		$("#ML_window").css('width', '526px');
	}
}

window.ondragstart = function() { return false; } 
/*
 * 기존 InitEventHandler
 */ 
//인증서 보기
$('#btn_viewCert').click(function () {
	var selection = $("#dataTable").MLjquiDataTable('getSelection');
	var selectionCnt = selection.length;
	if(selection && selectionCnt>0){
		for(var i=0 ; i<selectionCnt; i++){
			var rowData = selection[i];
			openCertViewDialog(rowData);
		}
	}else{
		ML4WebLog.log($.i18n.prop("ES021"));
		//20200310 mj
		var btnObj = $('#btn_viewCert');
		ML4WebDraw.errorHandler("main", $.i18n.prop("ES021"), btnObj, null);
	}
});

function newPasswdClick(){
	$('#input_cert_pw_new').val("");
}

//확인
$('#btn_confirm_iframe').click(function () {
	clickConfirmBtn();
});

function confirmProcess( data ){
	var resultobj = {};
	//alert("base64 디코딩 값 : " + data);
	var cert_pw = data;
	var ml4web_crypto_api = ML4WebApi.getCryptoApi();
	cert_pw = ml4web_crypto_api.HD_api( cert_pw );

	var selection = $("#dataTable").MLjquiDataTable('getSelection');
	var selectionCnt = selection.length;
	if( selection && selectionCnt > 0 ){
		for(var i=0 ; i<selectionCnt; i++){
			var rowData = selection[i];
			
			var selectedStorage = ML4WebUI.selectedStorage.current_option.storageName;
			var stgEncCertIdx 	= "";
			var signcert 		= "";
			var signpri 		= "";
			var kmcert 			= "";
			var kmpri 			= "";
			
			//인증서를 갖고 오기 위한 idx
			stgEncCertIdx = rowData.storageEncCertIdx;
			ML4WebLog.log("stgEncCertIdx :: " + stgEncCertIdx);
			
			if(ML4WebUI.selectedStorage.current_option.storageName == "web"
				|| ML4WebUI.selectedStorage.current_option.storageName == "kftc"
				|| ML4WebUI.selectedStorage.current_option.storageName == "web_kftc"
				||	ML4WebUI.selectedStorage.current_option.storageName == "pfx")
			{
				storageRawCertIdx = JSON.parse(ML4WebApi.dsDecrypt(stgEncCertIdx));
				if (storageRawCertIdx.storageName == "kftc") {
					storageRawCertIdx.storageOpt.fingerprint = storageRawCertIdx.storageCertIdx;
					storageRawCertIdx.storageOpt.password = cert_pw;	
				}
				storageRawCertIdx = JSON.stringify(storageRawCertIdx);
				stgEncCertIdx = ML4WebApi.dsencrypt(storageRawCertIdx);
			}
			
			resultobj.rowData = rowData;
			resultobj.rowData.storageRawCertIdx = stgEncCertIdx;
			resultobj.pw = cert_pw;
			resultobj.selectedStg = selectedStorage;
			
			if(ML4WebUI.selectedStorage.current_option.storageName == "token"){
				resultobj.rowData.storageRawCertIdx = JSON.parse(stgEncCertIdx);
			}
			
			ML4WebUI.getCertString(stgEncCertIdx, function(code, obj){				
				if(code == 0){
					resultobj.certbag = new Object();
					
					if(obj.cert.kmcert != ""){
						resultobj.kmcert = obj.cert.kmcert;
						resultobj.certbag.kmcert = obj.cert.kmcert;
					}
					if(obj.cert.kmpri != ""){
						resultobj.kmpri = obj.cert.kmpri;
						resultobj.certbag.kmpri = obj.cert.kmpri;
					}
					resultobj.signcert = obj.cert.signcert;
					resultobj.signpri = obj.cert.signpri;
					
					resultobj.certbag.signcert = obj.cert.signcert;
					resultobj.certbag.signpri = obj.cert.signpri;
					
					ML4WebUI.callback(0, resultobj);
				} else {					
					ML4WebUI.callback(code, obj);
				}
			});
		}
	}else{
		ML4WebLog.log("선택된 인증서가 없습니다.");
		//openAlertDialog($.i18n.prop("ES021"));
		//20200310 mj
		ML4WebDraw.errorHandler("main", $.i18n.prop("ES021"), $('#btn_confirm_iframe'), null);
		//DSAlert.openAlert("main", $.i18n.prop("ES021"), null);
		return;
	}
}
//취소
$('#btn_cancel').click(function () {
	closeCertDialog('main');
});

//TODO IE8, IE9일때 pfx 버튼 숨기기
/*
var browser = ML4WebApi.getProperty('browser');

if(( browser == 'MSIE 8' ) || (browser == 'MSIE 9' )){
	$('#btn_PFX').hide();
}
*/
//TODO 브라우저 인증서 저장
$('#btn_browser').click(function(){
	//브라우저인증서저장 버튼 활성화/비활성화 flag
	var select_stg = ML4WebUI.selectedStorage.key;
	var stg_id = "web";
	var browser = ML4WebApi.getProperty('browser');
	
	if((browser == 'MSIE 8') || (browser == 'MSIE 9')||(browser == 'MSIE 7') || (browser == 'MSIE 6')){
		//20200310 mj
		ML4WebDraw.errorHandler("main", ML4WebDraw.MSG_ALERT_LOWER_IE9, $('#btn_browser'), null);
	}else{
		if(ML4WebApi.getProperty("storageList").indexOf("web_kftc") > -1 || ML4WebApi.getProperty("storageList").indexOf("kftc") > -1 || ML4WebApi.getProperty("storageList").indexOf("web") > -1){
			if(ML4WebApi.webConfig.virtualKeyboardType === "INCA"){
				// 키패드 계속 생성되는 문제로 인한 초기화 패치(2019/06/14)
				var npLength = npVCtrl.keypadObject.length;
				for (var i=0; i<npLength; i++){
					if("nppfs-keypad-add_browser_password" == npVCtrl.keypadObject[i]._uuid){
						npVCtrl.keypadObject.splice(i,1);
						break;
					}
				}
			}
		}else{
			// 키패드 계속 생성되는 문제로 인한 초기화 패치(2019/06/14)
			if( ML4WebApi.webConfig.useVirtualKeyboard && ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
				var npLength = npVCtrl.keypadObject.length;
				for (var i=0; i<npLength; i++){
					if("nppfs-keypad-add_browser_password" == npVCtrl.keypadObject[i]._uuid){
						npVCtrl.keypadObject.splice(i,1);
						break;
					}
				}
			}
		}
		
		//$("#stg_web").click();
		if(ML4WebApi.getProperty("storageList").indexOf("web_kftc") > -1){
			 $("#stg_web_kftc").click(); 
		}else{ 
			 $("#stg_web").click();
		}
		
		//브라우저인증서저장 버튼 활성화/비활성화 flag
		openImportCertDialog(select_stg);
		
		// CapsLock 상태 알림
		document.querySelector('#add_browser_password').addEventListener('keyup', add_checkCapsLock);
		document.querySelector('#add_browser_password').addEventListener('mousedown', add_checkCapsLock);
		
		$('#add_browser_password').blur(function(e){
			$("#add_capslock").hide();
		});
	}
});

$('#btn_browser_manual').click(function(){
	window.open("/MagicLine4Web/ML4Web/install_bin/brower_cert_guide.pdf", "WebBrowserGuide", "width=600,height=650,location=no,toolbar=no,resizable=yes");
});

function clickList(event){
	var key;
	if(typeof(event.target.parentElement.parentElement.rowIndex) == "undefined"){
		key = event.target.parentElement.parentElement.parentElement.rowIndex;
	}else{
		key = event.target.parentElement.parentElement.rowIndex;
	}
	var rowNum = parseInt(key);
	$("#dataTable").MLjquiDataTable("selectRow", rowNum);
}

// ----->
function setMobile(id){
	var resultobj = {};
	var storageRawCertIdx = {};
	
	if(id == "smartcert"){
		storageRawCertIdx.storageName = 'smartcert';
		storageRawCertIdx.storageOpt = {};
		storageRawCertIdx.storageOpt.smartCertOpt = {};
		storageRawCertIdx.storageCertIdx = '';

		storageRawCertIdx.storageOpt.smartCertOpt.servicename = 'dreamCS';
		storageRawCertIdx.storageOpt.smartCertOpt.serviceOpt = {};
		storageRawCertIdx.storageOpt.smartCertOpt.serviceOpt.USIMServerIP = ML4WebApi.getProperty('cs_smartcert_serverip');
		storageRawCertIdx.storageOpt.smartCertOpt.serviceOpt.USIMServerPort = ML4WebApi.getProperty('cs_smartcert_serverport');
		storageRawCertIdx.storageOpt.smartCertOpt.serviceOpt.USIMSiteDomain = ML4WebApi.getProperty('cs_smartcert_sitedomain');
		storageRawCertIdx.storageOpt.smartCertOpt.serviceOpt.USIMInstallURL = ML4WebApi.getProperty('cs_smartcert_installurl');
		storageRawCertIdx.storageOpt.smartCertOpt.serviceOpt.USIMRaonSiteCode = ML4WebApi.getProperty('cs_smartcert_raonsitecode');
		
		resultobj.pw = '11111';
		
	}else if(id == "smartcertnx"){
		storageRawCertIdx.storageName = 'smartcertnx';
		storageRawCertIdx.storageOpt = {};
		storageRawCertIdx.storageOpt.smartCertOpt = {};
		storageRawCertIdx.storageCertIdx = '';
		
		storageRawCertIdx.storageOpt.smartCertOpt.servicename = 'dreamWEB';
		storageRawCertIdx.storageOpt.smartCertOpt.serviceOpt = {};
		
		storageRawCertIdx.storageOpt.smartCertOpt.serviceOpt.subject = ML4WebApi.getProperty("web_smartcert_subject");
		storageRawCertIdx.storageOpt.smartCertOpt.serviceOpt.issuer  = ML4WebApi.getProperty("web_smartcert_issuer");
		storageRawCertIdx.storageOpt.smartCertOpt.serviceOpt.serial  = ML4WebApi.getProperty("web_smartcert_serial");
		
		resultobj.pw = '11111';
	
	}else if(id == "mobisign"){
		storageRawCertIdx.storageName = 'mobile';
		storageRawCertIdx.storageCertIdx = "0";
		
		resultobj.pw = 'mobisign';
		id = 'mobile';
		
	}else if(id == "unisign"){
		storageRawCertIdx.storageName = 'unisign';
		storageRawCertIdx.storageOpt = {};
		storageRawCertIdx.storageOpt.smartCertOpt = {};
		storageRawCertIdx.storageCertIdx = '';
		resultobj.pw = '11111';
	}
	
	resultobj.rowData = {};
	resultobj.rowData.storageRawCertIdx = storageRawCertIdx;
	resultobj.selectedStg = id;
	
	return resultobj;
}

function openDriverDialog(id, idx, obj){
	var str = '';
	
	//console.info('id======='+id+(id=="smartcert"));
		if(id == "mobile"){
			str += '<ul id="sub_drv_list" class="drive_position_menu wdh_220">';
		}else if(id == "hdd"){
			str += '<ul id="sub_drv_list" class="drive_position_menu wdh_210">';
		}else{
			str += '<ul id="sub_drv_list" class="drive_position_menu wdh_150">';
		}
		
		if(id=="pfx"){
			var libType = obj.pfxOpt[0].libType;
			var browser = ML4WebApi.getProperty('browser');
			
			if(libType=='javascript' && browser!='MSIE 8'){
				openSearchPfxDialog();//인증서찾기창
			}else{
				var certOpt = {"storageName":"pfx","pfxOpt":{"libType":"c"}};
				ML4WebUI.getStorageCertList(certOpt, function(resultCode, jsonObj) {
					//ML4WebLog.log("getStorageCertList() PFX callback === " + JSON.stringify(jsonObj));
					if( resultCode == 0) {
						ML4WebDraw.MakeCertiListDiv(jsonObj.cert_list);
					}else{
						ML4WebDraw.MakeCertiListDiv(null);
						//20200310 mj
						ML4WebDraw.errorHandler("main", $.i18n.prop("ER110"), $("#stg_"+id), null);
					}
				});
			}
		}else if( id=="hdd" ){
			var hddOptCnt = obj.hddOpt.length;
			for(var i=0; i<hddOptCnt; i++){
				//$("#sel_5132_02").append("<option value='"+JSON.stringify(result.hddOpt[i])+"'>"+result.hddOpt[i].diskname+":");
				if(i==(obj.hddOpt.length-1)){
					str += '	<li id="'+id+'_driver_'+i+'" tabindex="'+(i+1)+'"><a href="#" class="wdh_210" onblur="closeSubDriverList();" >'+obj.hddOpt[i].diskname+'</a></li>';
				}else{
					str += '	<li id="'+id+'_driver_'+i+'" tabindex="'+(i+1)+'"><a href="#" class="wdh_210" >'+obj.hddOpt[i].diskname+'</a></li>';
				}
				
			}
		}else if( id=="token" ){
			//{"tokenOpt":[{"tokenname":"A-Token","driver":"C","driverPath":"C:/", "tokenpasswd":""},{"tokenname":"B-Token","driver":"D","driverPath":"D:/", "tokenpasswd":""}]}			
			var tokenOptCnt = obj.tokenOpt.length;
			
			obj.tokenOpt.sort(function(a, b){								
				if (b.driver == 'USIM_0002') {
					return true;
				}
			});
			
			for(var i=0; i<tokenOptCnt; i++){
				if(i==(obj.tokenOpt.length-1)){
					str += '	<li id="'+id+'_driver_'+i+'" ><a href="#" class="wdh_150" onblur="closeSubDriverList();">'+obj.tokenOpt[i].tokenname+'</a></li>';
				}else{
					str += '	<li id="'+id+'_driver_'+i+'" ><a href="#" class="wdh_150">'+obj.tokenOpt[i].tokenname+'</a></li>';
				}
			}
		}else if( id=="mobile" ){
			var isCsInstall = ML4WebApi.getProperty("is_cs_install");
			var isCsUpdate = ML4WebApi.getProperty("is_cs_update");
			var mode = "main";
			
			//{"phoneOpt":[{"servicename":"ubikey","serviceOpt":{"version":"V2.0.1","popupURL":"http://aaa.co.kr","UbikeyWParam":"aaa1","UbikeylParam":"aaa2"}},{"servicename":"mobiSign","serviceOpt":{"mobileKeyURL":"http://bbb.co.kr"}}]}
			var phoneOptCnt = obj.phoneOpt.length;
			var servicename = "";
			
			for (var i=obj.phoneOpt.length-1; i>=0; i--) {
				var find = 0;
				for (var j=0; j<ML4WebApi.MobileOption.length; j++){
					if (obj.phoneOpt[i].servicename == ML4WebApi.MobileOption[j]){
						find = 1;
						break;
					}
				}
				if (find == 0){
					obj.phoneOpt.splice(i, 1);
				}
			}
			
			phoneOptCnt = obj.phoneOpt.length;
			
			for(var i=0; i<phoneOptCnt; i++){
				if(obj.phoneOpt[i].servicename.indexOf("ubikeynx") > -1){
					obj.phoneOpt[i].servicename = ML4WebDraw.TITLE_MOB_SVC_UBIKEYNX;
				}else if(obj.phoneOpt[i].servicename.indexOf("ubikey") > -1){
					obj.phoneOpt[i].servicename = ML4WebDraw.TITLE_MOB_SVC_UBIKEY;
				} else if(obj.phoneOpt[i].servicename.indexOf("mobisign") > -1){
					obj.phoneOpt[i].servicename = ML4WebDraw.TITLE_MOB_SVC_MOBSIGN;
				}else if(obj.phoneOpt[i].servicename.indexOf("dreamCS") > -1){
					obj.phoneOpt[i].servicename = ML4WebDraw.TITLE_MOB_SVC_DREAMCS;
				}

				if(i==(obj.phoneOpt.length-1)){
					if((!isCsInstall || isCsUpdate) && (obj.phoneOpt[i].servicename != ML4WebDraw.TITLE_MOB_SVC_UBIKEYNX)){
						str += '	<li style="display:block;"><a href="#" onClick="installCheck(\''+mode+'\');" class="wdh_245" onblur="closeSubDriverList();">'+obj.phoneOpt[i].servicename+'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="UI/images/download.png" /></a></li>';
					}else{
						str += '	<li id="'+id+'_driver_'+i+'" style="display:block;"><a href="#" class="wdh_245" onblur="closeSubDriverList();">'+obj.phoneOpt[i].servicename+'</a></li>';
					}
				}else{
					if((!isCsInstall || isCsUpdate) && (obj.phoneOpt[i].servicename != ML4WebDraw.TITLE_MOB_SVC_UBIKEYNX)){
						str += '	<li style="display:block;"><a href="#" onClick="installCheck(\''+mode+'\');" class="wdh_245">'+obj.phoneOpt[i].servicename+'&nbsp;&nbsp;&nbsp;&nbsp;<img src="UI/images/download.png" /></a></li>';
					}else{
						str += '	<li id="'+id+'_driver_'+i+'" style="display:block;"><a href="#" class="wdh_245">'+obj.phoneOpt[i].servicename+'</a></li>';
					}
				}
			}
		}else if( id=="smartcert" ){
			//{"smartCertOpt":{"servicename":"dreamCS","serviceOpt":{"USIMServerIP":"","USIMServerPort":"","USIMSiteDomain":"","USIMRaonSiteCode":"","USIMInstallURL":"","USIMTokenInstallURL":""}}}
			//api.js에서 셋팅하던 부분 main.js로 이동
			resultobj = setMobile(id);
			ML4WebUI.callback(0, resultobj);
		}else if( id=="smartcertnx" ){
			//{"smartCertOpt":{"servicename":"dreamCS","serviceOpt":{"USIMServerIP":"","USIMServerPort":"","USIMSiteDomain":"","USIMRaonSiteCode":"","USIMInstallURL":"","USIMTokenInstallURL":""}}}
			//api.js에서 셋팅하던 부분 main.js로 이동
			resultobj = setMobile(id);
			ML4WebUI.callback(0, resultobj);
		}else if( id=="cloud" ){
			//{"cloudOpt":[{"servicename":"Dropbox","id":"dreamuser","passwd":"dreampw"},{"servicename":"google","id":"dreamuser","passwd":"dreampw"}]}
			var cloudOptCnt = obj.cloudOpt.length;
			for(var i=0; i<cloudOptCnt; i++){
				if(i==(obj.cloudOpt.length-1)){
					str += '	<li id="'+id+'_driver_'+i+'" ><a href="#" onblur="closeSubDriverList();">'+obj.cloudOpt[i].servicename+'</a></li>';
				}else{
					str += '	<li id="'+id+'_driver_'+i+'" ><a href="#" >'+obj.cloudOpt[i].servicename+'</a></li>';
				}
			}
		}else if( id=="unisign" ){
			//{"smartCertOpt":{"servicename":"dreamCS","serviceOpt":{"USIMServerIP":"","USIMServerPort":"","USIMSiteDomain":"","USIMRaonSiteCode":"","USIMInstallURL":"","USIMTokenInstallURL":""}}}
			//api.js에서 셋팅하던 부분 main.js로 이동
			resultobj = setMobile(id);
			resultobj.certObj = obj;
			ML4WebUI.callback(0, resultobj);
			
		}
		str += '</ul>';

	$('#driver_div').empty().html(str);
	$('#driver_div').show();

	// 수정 : 팝업 접속시 처음 접속 구분위한 flag,temp값 설정 (ML4Web_Main.html에 hidden필드값
	var flag = $('#flagInit').val();
	var temp = $('#temp').val();
	
	//event binding
	//ML4WebLog.log("[driver select storage option]event binding prepared... "+id+" === " + JSON.stringify(obj));
	ML4WebLog.log("[driver select storage option]event binding prepared... "+id);
	if( id=="hdd" ){
		var hddOptCnt = obj.hddOpt.length;
		for(var i=0; i<hddOptCnt; i++){
			$("#"+id+"_driver_"+i).bind("click", function () {
//				20180524 수정
				var disk = $(this).find("a").html();
				
				if(disk.indexOf("&amp;") > -1){
					disk = disk.replace("&amp;", "&");
				}
				var strVal = '{"diskname":"'+disk+'"}';
				selectDriver(id, strVal);
			});
		}
		if(flag == 0 && (temp == '' || temp == 0)){
			var strDisk = JSON.stringify(obj.hddOpt[0]);
			selectDriver(id, strDisk);
		}
		$('#temp').val(flag);
		$('#flagInit').val("1");
//		checkCount++;
//		console.log("flag : " + flag + ", temp : " + temp + ", checkCount : " + checkCount);
	}else if( id=="token" ){
		var tokenOptCnt = obj.tokenOpt.length;
		for(var i=0; i<tokenOptCnt; i++){
			var optVal = JSON.stringify(obj.tokenOpt[i]);
			//$("#"+id+"_driver_"+i).attr("value", optVal);
			document.getElementById(id+"_driver_"+i).setAttribute("optStr", optVal);

			$("#"+id+"_driver_"+i).bind("click", function () {
				//selectDriver(id, this.getAttribute("optStr"));
				var toeknOptStr = this.getAttribute("optStr");

				if(toeknOptStr!=null && toeknOptStr.length!=0){
					var toeknOpt = JSON.parse(toeknOptStr);

					if(toeknOpt.driverPath==null || toeknOpt.driverPath.length==0){
						var smartCertURL = ML4WebApi.getProperty('cs_smartcert_installurl');
						var width = 500;
						var height = 380;
						var top = (screen.availHeight/2) - (height/2);
						var left =(screen.availWidth/2) - (width/2);
						
						window.open(smartCertURL,'_blank','scrollbars=no,toolbar=no,resizable=no, width='+ width +', height='+ height +', top=' + top + ', left=' + left);
					}else{
						selectDriver(id, toeknOptStr);
					}
				}else{
					// 설치 드라이버가 없는 경우
				}
			});
		}
	}else if( id=="mobile" ){
		var phoneOptCnt = obj.phoneOpt.length;
		for(var i=0; i<phoneOptCnt; i++){
			//var optVal = JSON.stringify(obj.phoneOpt[i]);
			$("#"+id+"_driver_"+i).bind("click", function () {
				if($(this).find("a").html() == ML4WebDraw.TITLE_MOB_SVC_UBIKEY){
					obj.phoneOpt[0].serviceOpt.popupURL = ML4WebApi.getProperty('cs_ubikey_popupurl');
					obj.phoneOpt[0].serviceOpt.UbikeylParam = ML4WebApi.getProperty('cs_ubikey_lparam');
					obj.phoneOpt[0].serviceOpt.UbikeyWParam = ML4WebApi.getProperty('cs_ubikey_wparam');
					obj.phoneOpt[0].serviceOpt.version = ML4WebApi.getProperty('cs_ubikey_version');
					
					selectDriver(id, JSON.stringify(obj.phoneOpt[0]));
				}
				else if($(this).find("a").html() == ML4WebDraw.TITLE_MOB_SVC_UBIKEYNX){
					obj.phoneOpt[0].serviceOpt.popupURL = "";
					
					selectDriver(id, JSON.stringify(obj.phoneOpt[0]));
				}else if($(this).find("a").html() == ML4WebDraw.TITLE_MOB_SVC_MOBSIGN){
					obj.phoneOpt[1].serviceOpt.popupURL = ML4WebApi.getProperty('cs_mobisign_popupurl');
					obj.phoneOpt[1].serviceOpt.siteCode = ML4WebApi.getProperty('cs_mobisign_sitecode');
					obj.phoneOpt[1].serviceOpt.version = ML4WebApi.getProperty('cs_mobisign_version');
					
					selectDriver(id, JSON.stringify(obj.phoneOpt[1]));
				}else if($(this).find("a").html() == ML4WebDraw.TITLE_MOB_SVC_DREAMCS){
					obj.phoneOpt[2].serviceOpt.USIMRaonSiteCode = ML4WebApi.getProperty('cs_smartcert_raonsitecode');
					obj.phoneOpt[2].serviceOpt.USIMServerIP = ML4WebApi.getProperty('cs_smartcert_serverip');
					obj.phoneOpt[2].serviceOpt.USIMServerPort = ML4WebApi.getProperty('cs_smartcert_serverport');
					obj.phoneOpt[2].serviceOpt.USIMSiteDomain = ML4WebApi.getProperty('cs_smartcert_sitedomain');
					id = "smartcert";
					selectDriver(id, JSON.stringify(obj.phoneOpt[2]));
				}
				/*}else if($(this).find("a").html() == 'Mobilekey Key 휴대폰 저장 서비스'){
					var MobileKeyURL = ML4WebApi.getProperty('cs_mobilekey_popupurl');
					var width = 400;
					var height = 456;
					var top = (screen.availHeight/2) - (height/2);
					var left =(screen.availWidth/2) - (width/2);
					
					window.open(MobileKeyURL,'_blank','scrollbars=no,toolbar=no,resizable=no, width='+ width +', height='+ height +', top=' + top + ', left=' + left);
				}else if($(this).find("a").html() == '휴대폰 인증서 저장(금융결제원)'){
					obj.phoneOpt[2].serviceOpt.popupURL = ML4WebApi.getProperty('cs_mobisign_popupurl');
					obj.phoneOpt[2].serviceOpt.siteCode = ML4WebApi.getProperty('cs_mobisign_sitecode');
					obj.phoneOpt[2].serviceOpt.version = ML4WebApi.getProperty('cs_mobisign_version');
					
					selectDriver(id, JSON.stringify(obj.phoneOpt[2]));
				}*/
			});
		}
	}else if( id=="cloud" ){
		var cloudOptCnt = obj.cloudOpt.length;
		for(var i=0; i<cloudOptCnt; i++){
			var optVal = JSON.stringify(obj.cloudOpt[i]);
			$("#"+id+"_driver_"+i).bind("click", function () {
				selectDriver(id, optVal);
			});
		}
	}

	//ul 리스트의 첫번째 a 에 포커스 주기. 
	$('#sub_drv_list>li:first-child>a').focus();
}

//드라이버 선택창 닫기
function closeDriverDialog(){
	ML4WebLog.log("closeDriverDialog() called... ");

	$('#driver_div').hide();
}

// 스토리지 드라이버 고른 후 
function selectDriver(key, optVal){
	
	ML4WebLog.log("selectDriver() called... "+key+" === " + optVal);
	
	var temp = $('#temp').val();
	if(temp != '' && temp != 0){
		ML4WebUI.blockUI();
	}
	
	var certOpt = {"storageName":key};

	switch(key){
		case 'pfx' :
			certOpt.pfxOpt = optVal;
			break;
		case 'hdd' :
			certOpt.hddOpt = JSON.parse(optVal);
			break;
		case 'shdd' :
			certOpt.shddOpt = JSON.parse(optVal);
			break;
		case 'token' :
			certOpt.tokenOpt = JSON.parse(optVal);
			break;
		case 'mobile' :
			certOpt.phoneOpt = JSON.parse(optVal);
			break;
		case 'smartcert' :
			certOpt.smartCertOpt = JSON.parse(optVal);
			break;
		case 'cloud' :
			certOpt.cloudOpt = JSON.parse(optVal);
			break;
		case 'unisign':
			certOpt.unisignOpt = JSON.parse(optVal);
			break;
	}
	
	if(key == 'mobile' && certOpt.phoneOpt.serviceOpt.popupURL == "https://mobi.yessign.or.kr/mobisignInstall.htm" ){
		certOpt.phoneOpt.servicename = 'mobisign';
		
		resultobj = setMobile('mobisign');
		resultobj.rowData.storageRawCertIdx.storageOpt = certOpt; 
		ML4WebUI.callback(0, resultobj);
		ML4WebUI.unblockUI();
		
	}else if(key == 'smartcert'){
		resultobj = setMobile(key);
		ML4WebUI.callback(0, resultobj);
		ML4WebUI.unblockUI();
	}else if(key == 'web'){
		ML4WebUI.unblockUI();
		ML4WebUI.callback(0, resultobj);
	}else{
	
		setTimeout(function(){
			ML4WebUI.getStorageCertList(certOpt, function(resultCode, jsonObj) {
				ML4WebUI.unblockUI();
				if( resultCode == 0) {
					//ML4WebLog.log("selectDriver.getStorageCertList() Result === " + JSON.stringify(jsonObj));
					if(key == 'pfx'){
						DSDialog.closeDialog(function(code, obj){});
					}
					
					if($('#driver_admin_div').length > 0){
						makeAdminCertiListDiv(jsonObj.cert_list);
					}else if($('#driver_div').length > 0){
						ML4WebDraw.MakeCertiListDiv(jsonObj.cert_list);
					}
				}else if(jsonObj.errCode == 30070){
					ML4WebDraw.confirm( ML4WebDraw.MSG_MOB_INSTALL, function( code, boolean ){
						$("#btn_common_confirm").unbind();
						$("#btn_common_cancle").unbind();
						if( code == 0 ){
							var ubikeyURL = ML4WebApi.getProperty('cs_ubikey_popupurl');
							var width = 500;
							var height = 380;
							var top = (screen.availHeight/2) - (height/2);
							var left =(screen.availWidth/2) - (width/2);
							
							window.open(ubikeyURL,'_blank','scrollbars=no,toolbar=no,resizable=no, width='+ width +', height='+ height +', top=' + top + ', left=' + left);
						}
					});
				}else if(jsonObj.errCode == 30071){// ubikey
					//20200310 mj
					ML4WebDraw.errorHandler("main",ML4WebDraw.MSG_MOB_CANCEL,$("#stg_mobile"),null);
				}else if(jsonObj.errCode == 30072){
					ML4WebDraw.confirm( ML4WebDraw.MSG_MOB_UPDATE, function( code, boolean ){
						$("#btn_common_confirm").unbind();
						$("#btn_common_cancle").unbind();
						if( code == 0){
							var ubikeyURL = ML4WebApi.getProperty('cs_ubikey_popupurl');
							var width = 500;
							var height = 380;
							var top = (screen.availHeight/2) - (height/2);
							var left =(screen.availWidth/2) - (width/2);

							window.open(ubikeyURL,'_blank','scrollbars=no,toolbar=no,resizable=no, width='+ width +', height='+ height +', top=' + top + ', left=' + left);
						}
					});
				}else if(jsonObj.errCode == 30006 || jsonObj.errCode == 202){
					if($('#driver_admin_div').length > 0){
						makeAdminCertiListDiv(null);
					}else if($('#driver_div').length > 0){
						ML4WebDraw.MakeCertiListDiv(null);
					}
					EmptyCertDiv();
				}else{	//실패
					ML4WebLog.log("[ERROR!!! - "+resultCode+" ] errCode === " + jsonObj.errCode + ", errMsg === " +  jsonObj.errMsg);
					if($('#driver_admin_div').length > 0){
						makeAdminCertiListDiv(null);
					}else if($('#driver_div').length > 0){
						ML4WebDraw.MakeCertiListDiv(null);
					}
					EmptyCertDiv();
					
					//alert
					//openAlertDialog(jsonObj.errMsg);
					if(key == 'pfx' && jsonObj.errCode == 42101){
						//openAlertDialog("PFX 인증서 비밀번호가 틀렸습니다.");
						ML4WebDraw.errorHandler("main", $.i18n.prop("ER106"), $("#import_pfx_password"), null);
						//DSAlert.openAlert("main", $.i18n.prop("ER106"), $("#import_pfx_password"));
					}else{
						//openAlertDialog("해당 인증서 목록 조회에 실패하였습니다.");
						ML4WebDraw.errorHandler("main", $.i18n.prop("ER107"), $("#stg_"+key), null);
						//DSAlert.openAlert("main", $.i18n.prop("ER107"), null);
					}
				}
			});
		},200);
	}
	
	if($('#driver_admin_div').length > 0){// && $('#driver_admin_div').is(":visible")){
		$('#driver_admin_div').hide();
	}else if($('#driver_div').length > 0){// && $('#driver_div').is(":visible")){
		$('#driver_div').hide();
	}
}

function closeSubDriverList(){
	closeDriverDialog();
}

function EmptyCertDiv() {
	$("#dataTable").MLjquiDataTable('clear');
}


function getFormatDate(paramDate, type){
	var date, year, month, day, hour, minute, second = '';
	
	if(type == '1'){
		date = paramDate.split("-");
		year = date[0];
		month = date[1];
		day = date[2];
		
		if(month < 10 && month.length < 2){
			month = '0' + month;
		}
		if(day < 10 && day.length < 2){
			day = '0' + day;
		}
		
		return  year + '-' + month + '-' + day;
	}else{
		var today = new Date();
		year = today.getFullYear();
		month = today.getMonth()+1;
		day = today.getDate();
		hour = today.getHours();
		minute = today.getMinutes();
		second = today.getSeconds();
		
		if((month+"").length < 2){
			month = '0' + month;
		}
		
		if((day+"").length < 2){
			day = '0' + day;
		}
		
		if((hour+"").length < 2){
			hour = '0' + hour;
		}
		
		if((minute+"").length < 2){
			minute = '0' + minute;
		}
		
		if((second+"").length < 2){
			second = '0' + second;
		}
		
		return year +''+ month +''+ day +''+ hour +''+ minute +''+ second;
	}

}
function makeSourceData(listObj){
	var data = new Array();
	if(listObj != null && listObj.length > 0){
		for (var i = 0; i < listObj.length; i++) {
			var row = {};
			var filter_or_operator = 1; // 상단고정 //
			
			//var someDate = listObj[i].enddate;
			var dateFormated = getFormatDate(listObj[i].enddate, '1');

			row[DS_CERT_INFO.AIA] = listObj[i].aia;
			row[DS_CERT_INFO.AUTHKEYID] = listObj[i].authkeyid;
			row[DS_CERT_INFO.CERTPOLICY] = listObj[i].certpolicy;
			row[DS_CERT_INFO.CRLDP] = listObj[i].crldp;
			//row[DS_CERT_INFO.ENDDATE] = listObj[i].enddate;
			row[DS_CERT_INFO.ENDDATE] = dateFormated;
			row[DS_CERT_INFO.ENDDATETIME] = listObj[i].enddatetime;
			row[DS_CERT_INFO.ISSUERNAME] = listObj[i].issuername;
			row[DS_CERT_INFO.KEYUSAGE] = listObj[i].keyusage;
			row[DS_CERT_INFO.POLICYID] = listObj[i].policyid;
			row[DS_CERT_INFO.POLICYNOTICE] = listObj[i].policynotice;
			row[DS_CERT_INFO.PUBKEY] = listObj[i].pubkey;
			row[DS_CERT_INFO.PUBKEYALGORITHM] = listObj[i].pubkeyalgorithm;
			row[DS_CERT_INFO.REALNAME] = listObj[i].realname;
			row[DS_CERT_INFO.SERIALNUM] = listObj[i].serialnum;
			row[DS_CERT_INFO.SIGNATUREALGORITHM] = listObj[i].signaturealgorithm;
			row[DS_CERT_INFO.STARTDATE] = listObj[i].startdate;
			row[DS_CERT_INFO.STARTDATETIME] = listObj[i].startdatetime;
			row[DS_CERT_INFO.SUBJECTALTNAME] = listObj[i].subjectaltname;
			row[DS_CERT_INFO.SUBJECTNAME] = listObj[i].subjectname;
			row[DS_CERT_INFO.SUBKEYID] = listObj[i].subkeyid; 
			row[DS_CERT_INFO.VERSION] = listObj[i].version;
			row["storageEncCertIdx"] = listObj[i].storageEncCertIdx;
			//
			var tempSubjectname = listObj[i].subjectname.split(",");
			var realSubjectname = "";
			$.each(tempSubjectname, function() {
				var tempVal1 = this.split("=");
				if( tempVal1[0].toLowerCase() == "ou" ){					
					var tempVal2 = tempVal1[1].toLowerCase();
					if( tempVal2.toLowerCase() == "personal" ){
						realSubjectname = $.i18n.prop("OID_personal");
					}else if( tempVal2.toLowerCase() == "personalb" ){
						realSubjectname = $.i18n.prop("OID_personalb");
					}else if( tempVal2.toLowerCase() == "corporation4ec" ){
						realSubjectname = $.i18n.prop("OID_corporation4ec");
					}else if( tempVal2.toLowerCase() == "corporation4ecb" ){
						realSubjectname = $.i18n.prop("OID_corporation4ecb");
					}
				}
			});
			
			//row["Policy"] = $.i18n.prop("OID_"+ listObj[i].PolicyIdentifier.split(".").join("_"));
			if(realSubjectname.length == 0) {
				row["Policy"] = $.i18n.prop("OID_"+ listObj[i].policyid.split(".").join("_"));
			}else{
				row["Policy"] = realSubjectname;
			}

			if(typeof(listObj[i].source) != 'undefined'){
				row["source"] = listObj[i].source;
			}
			if(typeof(listObj[i].cloud) != 'undefined'){
				row["cloud"] = listObj[i].cloud;
			}

			var tempArr = listObj[i].issuername.split(",");
			var tempIssuer = "";
			$.each(tempArr, function() {
				var tempArr11 = this.split("=");
				if( tempArr11[0].toLowerCase() == "cn" ){
					tempIssuer = tempArr11[1];
				}
			});
			row["Issuer"] = tempIssuer;

			//cn=조성아()0020049200510177057911,ou=WOORI,ou=personal4IB,o=yessign,c=kr
			//cn=gpkiuser01,ou=GPKI,o=Government of Korea,c=KR
			var fullsubjectname = listObj[i].subjectname.toLowerCase();
			var tempArr2 = listObj[i].subjectname.split(",");
			var tempCn = "";
			if( fullsubjectname.indexOf("cn") >=0 ){
				$.each(tempArr2, function() {
					var tempArr22 = this.split("=");
					if( tempArr22[0].toLowerCase() == "cn" ){
						tempCn = tempArr22[1];
					}
				});
			}else{
				/*$.each(tempArr2, function() {
					var tempArr22 = this.split("=");
					if( tempArr22[0].toLowerCase() == "ou" ){
						tempCn = tempArr22[1];
					}
				});*/
				tempCn = listObj[i].realname;
			}
			row["Cn"] = tempCn;
			
			//unused path
			if(typeof(listObj[i].certpath) != 'undefined'){
				row["certpath"] = listObj[i].certpath;
			}

			data[i] = row;
		}
	}

	var source = {
		localData: data,
		dataType: "array",
		dataFields:
		[
			{ name: DS_CERT_INFO.VERSION, type: 'string' },
			{ name: DS_CERT_INFO.SERIALNUM, type: 'string' },
			{ name: DS_CERT_INFO.SIGNATUREALGORITHM, type: 'string' },
			{ name: DS_CERT_INFO.ISSUERNAME, type: 'string' },
			{ name: DS_CERT_INFO.STARTDATE, type: 'string' },
			{ name: DS_CERT_INFO.STARTDATETIME, type: 'string' },
			{ name: DS_CERT_INFO.ENDDATE, type: 'string' },
			{ name: DS_CERT_INFO.ENDDATETIME, type: 'string' },
			{ name: DS_CERT_INFO.SUBJECTALTNAME, type: 'string' },
			{ name: DS_CERT_INFO.SUBJECTNAME, type: 'string' },
			{ name: DS_CERT_INFO.PUBKEY, type: 'string' },
			{ name: DS_CERT_INFO.PUBKEYALGORITHM, type: 'string' },
			{ name: DS_CERT_INFO.KEYUSAGE, type: 'string' },
			{ name: DS_CERT_INFO.CERTPOLICY, type: 'string' },
			{ name: DS_CERT_INFO.POLICYID, type: 'string' },
			{ name: DS_CERT_INFO.POLICYNOTICE, type: 'string' },
			{ name: DS_CERT_INFO.AUTHKEYID, type: 'string' },
			{ name: DS_CERT_INFO.SUBKEYID, type: 'string' },
			{ name: DS_CERT_INFO.CRLDP, type: 'string' },
			{ name: DS_CERT_INFO.AIA, type: 'string' },
			{ name: DS_CERT_INFO.REALNAME, type: 'string' },
			{ name: 'storageRawCertIdx', type: 'string' },
			{ name: 'storageEncCertIdx', type: 'string' },
			{ name: 'Policy', type: 'string' },
			{ name: 'Cn', type: 'string' },
			{ name: 'Issuer', type: 'string' },
			{ name: 'source', type: 'string' },
			{ name: 'cloud', type: 'string' },
			{ name: 'certpath', type: 'string' }	//unused path
		]
	};

	return source;
}

//인증서 선택
function SelectCert(index){

	for(var i=0 ; i<listCnt ; i++) {
		if( i != index ){
			$("#ML4WebCert"+i).removeClass("list_on");
		}else{
			$("#ML4WebCert"+i).addClass("ML_cert_list list_on");
		}
	}

	ML4WebApi.saveSelectCert(listObj.Get(index));

}

//인증서 보기창 열기
function openCertView() {
	//MagicLine5.CertView();
	try{
		$("#MagicLine5_View_dialog").load("UI/ML4Web_Cert_View.html",function(){
			$(this).dialog({
				width:400,
				Position:'top',
				closeOnEscape:false,
				open: function(event, ui){
					$(".ui-dialog-titlebar-close").hide();
				}
			});
		});
	} catch(ex) {
		MagicLine5Log.log(ex.message);
		callback( e.message, 1006 );
	}
}

//인증서 선택창 닫기
function closeCertDialog(mode){
	try{
		if(mode=="main"){
			if ( $("#ML_Dialog_common").length > 0 ) {
				$('#ML_Dialog_common').MLjquiWindow('destroy');
			}
			if ( $("#ML_dialog_cs_install").length > 0 ) {
				$('#ML_dialog_cs_install').MLjquiWindow('destroy');
			}
			if ( $("#popup_alert").length > 0 ) {
				$('#popup_alert').MLjquiWindow('destroy');
			}
			if ( $("#ML_window").length > 0 ) {
				$('#ML_window').MLjquiWindow('destroy');
			}
			if ( $("#browser_manual").length > 0 ) {
				$('#browser_manual').MLjquiWindow('destroy');
			}
		}else if(mode=="mgmt"){
			if ( $("#ML_Dialog_mgmt_common").length > 0 ) {
				$('#ML_Dialog_mgmt_common').MLjquiWindow('destroy');
			}
			if ( $("#popup_mgmt_alert").length > 0 ) {
				$('#popup_mgmt_alert').MLjquiWindow('destroy');
			}
			if ( $("#ML_window_admin").length > 0 ) {
				$('#ML_window_admin').MLjquiWindow('destroy');
			}
		}else if(mode=="cs"){
			if ( $("#popup_alert").length > 0 ) {
				$('#popup_alert').MLjquiWindow('destroy');
			}
			if ( $("#ML_cert_sign_window").length > 0 ) {
				$('#ML_cert_sign_window').MLjquiWindow('destroy');
			}
		}

		ML4WebUI.closeMainDialog(mode);
	}catch(ex){
		ML4WebLog.log(ex.message);
	}
}


//스마트 인증 WEB 호출 함수
function OnSmartCertComplete(trID, returnCode, telco, phonenumber, signedData,subjectDN){
	// 서명 결과 처리 페이지
	DS_SmartCert_Dialog.closeWindow();

	var dsOption = ML4WebUI.getDSOption();
	//console.info('>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>');

	// 임시
	if(signedData!=null && signedData!='' && signedData!='null'){
		var storageRawIdxObj = {};

		storageRawIdxObj.storageName = "smartcert";
		storageRawIdxObj.storageCertIdx = subjectDN;
		storageRawIdxObj.storageOpt = {};

		storageRawIdxObj.storageOpt.servicename='smartcertWEB';
		storageRawIdxObj.storageOpt.serviceOpt={};

		// 예비
		storageRawIdxObj.storageOpt.serviceOpt.mobileConnURL='';
		storageRawIdxObj.storageOpt.serviceOpt.cryptoAPI='';
		storageRawIdxObj.storageOpt.serviceOpt.signScheme='';
		storageRawIdxObj.storageOpt.serviceOpt.encScheme='';
		storageRawIdxObj.storageOpt.serviceOpt.mdAlg='';


		var rawCertIdxStr = JSON.stringify(storageRawIdxObj);
		//console.info(">>>>>>>>>>>>>>>>>>>>>>>>"+rawCertIdxStr);
//		var rawUECertIdxStr = encodeURIComponent(rawCertIdxStr);

		dsOption.callback(0,  {storageCertIdx:ML4WebApi.makeEncMssage(rawCertIdxStr), encMsg: signedData});
	}else{
		
		//dsOption.callback(ML4WebLog.getErrCode("Storage_smartcert_Sign"), {"errCode": "888", "errMsg": '취소 되었습니다.'});
	}

}

//************************ [START]    sub dialog event     **********************************/
function getOS(){
	try{
		var OsVersion = navigator.userAgent;
		OsVersion = OsVersion.toUpperCase();
		if( OsVersion.indexOf("NT 5.1") != -1 ) return "windows XP";
		else if( OsVersion.indexOf("NT 6.0") != -1 ) return "windows vista";
		else if( OsVersion.indexOf("NT 6.1") != -1 ) return "windows 7";
		else if( OsVersion.indexOf("NT 6.2") != -1 ) return "windows 8";
		else if( OsVersion.indexOf("NT 6.3") != -1 ) return "windows 8.1";
		else if( OsVersion.indexOf("NT 10.0") != -1 ) return "windows 10";
		else if( OsVersion.indexOf("IPAD") != -1 ) return "IOS";
		else if( OsVersion.indexOf("ANDROID") != -1 ) return "Android";
		else if( OsVersion.indexOf("BLACKBERRY") != -1 ) return "BlackBerry";
		else if( OsVersion.indexOf("MAC") != -1 ) return "MAC";
		else if( OsVersion.indexOf("SYMBIAN") != -1 ) return "Symbian";
		else if( OsVersion.indexOf("UBUNTU") != -1 ){
			if( OsVersion.indexOf("86_64") != -1 ){
				return "LINUX64_UBUNTU64";
			}else{
				return "LINUX32_UBUNTU32";
			}
		}else if( OsVersion.indexOf("FEDORA") != -1 ){
			if( OsVersion.indexOf("86_64") != -1 ){
				return "LINUX64_FEDORA64";
			}else{
				return "LINUX32_FEDORA32";
			}
		}
		else if( OsVersion.indexOf("LINUX") != -1 ){
			if( OsVersion.indexOf("86_64") != -1 ){
				return "LINUX64";
			}else{
				return "LINUX32";
			}
		}
		else return "Unknown";
	}catch( ex ) {
		return "Unknown";
	}
}

function installCheck(mode){
	var os_ver = ML4WebApi.getProperty("os");
	if(os_ver=="Android" || os_ver=="IPHONE" || os_ver=="IPAD" || os_ver=="BlackBerry"){
		ML4WebDraw.errorHandler("main", "해당 사이트 스마트 인증은<br> 모바일 기기를 지원하지 않습니다.", $("#stg_web"), null);
		//DSAlert.openAlert("main", "해당 사이트 스마트 인증은<br> 모바일 기기를 지원하지 않습니다.", null);
		return;
	}else {
		var default_stg = ML4WebApi.getProperty("defaultStorage");
		var stgArr = ML4WebApi.getProperty("storageList");
		var browser = ML4WebApi.get_browser_info();
		
		var checkobj = {};
		checkobj.defaultStorage = default_stg;
		checkobj.storageList = stgArr;
		checkobj.browserInfo = browser;
		
		ML4WebApi.getCsManager().sendURLScheme(function(){
			
		});
		
		ML4WebCS.installCheck(mode, function(code, obj){
			if(code == 0){
				ML4WebCS.updateCheck(mode, function(code, updateObj){
					if(updateObj.is_cs_update == true){
						openCSUpdateDialog(mode);
					}else{
						checkobj.installcheck = updateObj.is_cs_install;
						checkobj.updatecheck = updateObj.is_cs_update;
						
						ML4WebDraw.MakeStorageListDiv(checkobj);
						ML4WebDraw.initWebMainEvent();
						if(default_stg != null && default_stg != ""){
							$("#stg_" + default_stg).click();
						}else{
							ML4WebDraw.MakeCertiListDiv(null);
						}
					}
				});
			}else{
				if(obj.csUpdateChk == "upgrade"){ // update
					openCSUpdateDialog(mode);
				}else{
					openCSInstallDialog(mode);
				}
			}
		});
	}
}

//CS Update 팝업
function openCSUpdateDialog(mode){
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
				title: $.i18n.prop("TS040"),
				showCert:false,
				certData:null,
				onclick:installProgram,
				contentKey:"install_update",
				dialogHeight:"225px"
		};
	}
	//새로
	var btnObj = $("#stg_" + ML4WebApi.webConfig.storageList[0]);
	DSDialog.openDialog(popOption, btnObj, function(code,jsonObj){});
}

//CS 설치유도 팝업
function openCSInstallDialog(mode){
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
			title: $.i18n.prop("TS039"),
			showCert:false,
			certData:null,
			onclick:installProgram,
			contentKey:"install_cs",
			dialogHeight:"180px"
		};
	}
	//20200310 mj
	var btnObj = $("#stg_" + ML4WebApi.webConfig.storageList[0]);
	DSDialog.openDialog(popOption, btnObj, function(code,jsonObj){});
}

function installProgram(){
	//alert("installProgram.....");
	$("#jqxLoader").MLjquiLoader({ width: 100, height: 60, imagePosition: 'top' });
	$('#jqxLoader').MLjquiLoader('open');

	var installPageUrl = ML4WebApi.getProperty("cs_install_page_url");
	
	var downloadCount = 0;
	
	try{
		if( downloadCount < 2 ){
			downloadCount++;
			var os_ver = ML4WebApi.getProperty("os");
			var installType = ML4WebApi.getProperty("cs_install_type");

			if(installType == "download"){
				if( os_ver.indexOf("MAC")> -1 ){
					document.location.href = ML4WebApi.getProperty("cs_download_mac");
				}else if( os_ver.indexOf("UBUNTU64")> -1 ){
					document.location.href = ML4WebApi.getProperty("cs_download_linux_ubuntu64");
				}else if( os_ver.indexOf("UBUNTU32")> -1 ){
					document.location.href = ML4WebApi.getProperty("cs_download_linux_ubuntu32");
				}else if( os_ver.indexOf("FEDORA64")> -1 ){
					document.location.href = ML4WebApi.getProperty("cs_download_linux_fedora64");
				}else if( os_ver.indexOf("FEDORA32")> -1 ){
					document.location.href = ML4WebApi.getProperty("cs_download_linux_fedora32");
				}else {
					document.location.href = ML4WebApi.getProperty("cs_download_win");
				}
				DSDialog.closeDialog(function(code, obj){});
			}else{
				window.top.location.href = installPageUrl;
			}
		}else{
			return;
		}
	}catch(e){
		if(installType == "download"){
			document.location.href = downloadURL + downloadWinFileName;
			DSDialog.closeDialog(function(code, obj){});
		}else{
			window.top.location.href = installPageUrl;
		}
	}
}

//인증서 상세보기
function openCertViewDialog(data){
	var jqxwidget2 = $('[id^="ML_window"]');
	var offset2 = jqxwidget2.offset();
	
	if ( $("#ML_window_detail").length > 0 ) {
		$('#ML_window_detail').MLjquiWindow('title', $.i18n.prop("TS006"));

		$('#ML_window_detail').MLjquiWindow('open', function(e){
			refreshCertView(data);
		});
	}else{
		//$("#certDetailContainer").load("../ML4Web/UI/ML4Web_Cert_View.html", function(){
		$("#certDetailContainer").load("../ML4Web/UI/ML4Web_Cert_View.html?random="+Math.random() * 99999, function(){
			if(!magiclineUtil.isMobile(magiclineUtil.getOS())){
				$('#ML_window_detail').MLjquiWindow({position:{x: offset2.left + 15, y: offset2.top + 50}});
			}else{
				var os_ver = ML4WebApi.getProperty("os");
				var detailWidth;
				var detailHeight;
				var orientation = window.orientation;
				if( orientation == 0 ){
					detailWidth = screen.width;
					if(os_ver == "IPHONE" || os_ver == "IPAD" ){
						if(screen.height >= 500){
							detailHeight = screen.height-50;
						}
					}else{
						detailHeight = screen.height;
					}
				}else{
					if(os_ver == "IPHONE" || os_ver == "IPAD" ){
						if(screen.height >= 800){
							detailWidth = screen.height - 100
						}else{
							detailWidth = screen.height;
						}
						detailHeight = screen.width;
					}else{
						detailWidth = screen.width;
						detailHeight = screen.height;
					}
				}
				$('#ML_window_detail').MLjquiWindow({
					position:{x: offset2.left, y: offset2.top},
					width:detailWidth,height:detailHeight
				});
				
			}
			$('#ML_window_detail').MLjquiWindow('open', function(e){
				initDetailEventHandler();
				refreshCertView(data);
			});
		});
	}
}

//인증서 삭제(비번확인 없는 경우)
function deleteCertNonePwd(mode, key, data){
	//ML4WebLog.log("[ML4Web_Main.js]deleteCertNonePwd() data === " + JSON.stringify(data));
	var confirm_txt = "";
	
	if(key == "token"){
		confirm_txt = "confirm_txt_token";
	}else{
		confirm_txt = "confirm_txt";
	}
	
	var popOption = {
		mode: mode,
		title:"인증서 삭제",
		showCert:true,
		certData:data,
		onclick:procDeleteCertNonePwd,
		contentKey:confirm_txt,
		dialogHeight:"200px"
	};
	
	//20200310 mj
	var btnObj = $('#btn_deleteCert');
	DSDialog.openDialog(popOption, btnObj, function(code,jsonObj){});
}

//인증서 삭제(비번확인 없는 경우)
function procDeleteCertNonePwd(data) {
	var mode = DSDialog.getProperty("mode");
	var storageCertIdx = data.storageEncCertIdx;
	var stgKey = ML4WebUI.selectedStorage.key;
	var passwd = "";
	if(stgKey == "token"){
		var passwd = $('#input_token_pw_confirm').val();
		
		if(passwd==null || passwd==""){
			if(mode=='main') {
				//openAlertDialog("인증서 비밀번호를 입력하세요.");
				ML4WebDraw.errorHandler("main", $.i18n.prop("ES026"), $("#input_token_pw_confirm"), null);
			} else if(mode=='mgmt') {
				//openMgmtAlertDialog("인증서 비밀번호를 입력하세요.");
				ML4WebDraw.errorHandler("mgmt", $.i18n.prop("ES026"), $("#input_token_pw_confirm"), null);
			}
			return false;
		}
	}
	
	ML4WebUI.deleteStorageCert( storageCertIdx, passwd, function(code, obj){
		if( code == 0) {
			//ML4WebLog.log("[deleteCertNonePwd success!!!] Result === " + JSON.stringify(obj));
			if(obj.result == true){
				DSDialog.closeDialog(function(code, obj){
					//목록 초기화
					ML4WebUI.getStorageCertList(null, function(resultCode, jsonObj) {
						if( resultCode == 0) {//성공
							if(mode=='main'){
								ML4WebDraw.MakeCertiListDiv(jsonObj.cert_list);
								//20200310 mj
								DSAlert.openAlert("main", $.i18n.prop("ES009"), $('#btn_deleteCert'));
								
							}else if(mode=='mgmt'){
								makeAdminCertiListDiv(jsonObj.cert_list);
								//20200310 mj
								DSAlert.openAlert("mgmt", $.i18n.prop("ES009"), $('#btn_deleteCert'));
							}
						}else{	//실패
							ML4WebLog.log("[ERROR!!! - "+resultCode+" ] errCode === " + jsonObj.errCode + ", errMsg === " +  jsonObj.errMsg);
							if(mode=='main'){
								ML4WebDraw.MakeCertiListDiv(null);
								//20200310 mj
								DSAlert.openAlert("main", $.i18n.prop("ES009"), $('#btn_deleteCert'));
							}else if(mode=='mgmt'){
								makeAdminCertiListDiv(null);
								//20200310 mj
								DSAlert.openAlert("mgmt", $.i18n.prop("ES009"), $('#btn_deleteCert'));
							}
						}
					});
				});

				//backup localStorage
				ML4WebUtil.setBackupCertList(null);
			}else{
				if(mode=='main') {
					//openAlertDialog("삭제에 실패하였습니다.");
					DSAlert.openAlert("main", $.i18n.prop("ES017"), null);
				} else if(mode=='mgmt') {
					//openMgmtAlertDialog("삭제에 실패하였습니다.");
					DSAlert.openAlert("mgmt", $.i18n.prop("ES017"), null);
				}
			}
		}else{	//실패
			ML4WebLog.log("[ERROR!!! - "+code+" ] errCode === " + obj.errCode + ", errMsg === " +  obj.errMsg);
		}
	});
}


//TODO 브라우저 인증서
//브라우저인증서저장 버튼 활성화/비활성화 flag

function openImportCertDialog(select_stg){
	var os_ver = ML4WebApi.getProperty("os");
	var keyName = "";
	var dialogHeight = "";
	
	var popOption = {
		mode:"main",
		title: $.i18n.prop("TS036"),
		showCert:false,
		certData:{},
		onclick:importCertToBrowser,
		contentKey:"add_browser",
		dialogHeight:"430px",
		selectStg:select_stg
	};
	
	//20200310 mj
	var btnObj = $('#in_browser');
	DSDialog.openDialog(popOption, btnObj, function(code,jsonObj){
		
		if(ML4WebApi.getProperty("storageList").indexOf("web_kftc") > -1 || ML4WebApi.getProperty("storageList").indexOf("kftc") > -1 || ML4WebApi.getProperty("storageList").indexOf("web") > -1){
			if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
				npPfsCtrl.RegistDynamicField("form1", "add_browser_password");
			}else if( ML4WebApi.webConfig.virtualKeyboardType === "DREAM"){
				ML4WebVKey.init(1);
			}
		}else{
			if( ML4WebApi.webConfig.useVirtualKeyboard && ML4WebApi.webConfig.virtualKeyboardType === "INCA"){
				npPfsCtrl.RegistDynamicField("form1", "add_browser_password");
			}else if( ML4WebApi.webConfig.useVirtualKeyboard && ML4WebApi.webConfig.virtualKeyboardType === "DREAM"){
				ML4WebVKey.init(1);
			}
		}
		
		if($("#filefile2").val() == ""){
			$("#add_browser_password").val('').prop("disabled",true);
			$("#add_browser_password").css({ 'pointer-events': 'none' });
		}
		
		//filePicker 선택시 처리
		$('#filefile2').change(function(evt){
			if( evt.target.files.length > 0 ){
				$("#add_browser_password").val('').prop("disabled",false);
				$("#add_browser_password").css({ 'pointer-events': 'auto' });
				if( ML4WebApi.webConfig.useVirtualKeyboard ){
					if( ML4WebApi.webConfig.virtualKeyboardType === "NSHC" ){
						initializedNFilter();
					}else if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
						//npPfsCtrl.RegistDynamicField("form1", "add_browser_password");
					}
				}
				certFileProcess( evt.target.files );
			}
		});
	});
}

// 만료된 인증서 안내 팝업 띄우기 함수
function certExpirePopup(obj, cert_datetime, callback){
	var isExpired = ML4WebUtil.isDateExpired(cert_datetime);
	if(isExpired){
		var popOption = {
				mode:"main",
				title: $.i18n.prop("TS042"),
				message: $.i18n.prop("TS095"),
				showCert:false,
				certData:{obj:obj, callback:callback},
				onclick:confirmExpiredPopup,
				contentKey:"check_cert_expire",
				dialogHeight:"200px"
			};
		//20200310 mj
		var btnObj = $("#btn_confirm_iframe");
		DSDialog.openDialog(popOption, btnObj, function(){});
	} else {
		callback(0, obj);
	}
}

function confirmExpiredPopup(data){
	data.callback(0, data.obj);
}


/*
 * 가상키패드 입력에 따른 비밀번호 처리 함수
 */
function vpad( tag, callback ){
	if(ML4WebApi.getProperty("storageList").indexOf("web_kftc") > -1 || ML4WebApi.getProperty("storageList").indexOf("kftc") > -1 || ML4WebApi.getProperty("storageList").indexOf("web") > -1){
		if( ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
			if(keyboardCheck){
				var eleName = tag;
				var pubkey = "-----BEGIN CERTIFICATE-----MIIBCgKCAQEAwWmijy+IsrsM34E6ixbRWZln7ZEnsIFg7ey9wjBlXwn816iKXHVhdr6LIkl4Ks/FY2T/DFJ+Kltw7AICsBTNDLnNZz7nWWVQlq86IJq5ejkrZR88BtQ4aPRshkL8l+DH1Qf4a1A7r/HbGfo/Ad80ns4Z9Z4T5oywX9YMsF2QBAMh+ORE04nwWzkFhrjPkYUiM2sJpWrvJO6FOoidLkCMq8jVu+axhsx6LAraO13F67Ui9NlAydCHtBybZavcfnbhJSUs4QwVfeks58o1+QHLA/hZG4/XbUKWyVmZBK7tt/ZeZuXOtvZVljU3rmjvEb4+FON2kwoplq8iM7hRxFLbFQIDAQAB-----END CERTIFICATE-----";
				var r = transkey_GetEncDream(pubkey,eleName);
				//alert("base64 인코딩 값 : " + r);
				//base64 Decode 적용
				var data = ML4WebApi.base64Decode(r);
				browserPasswd = data;
				callback( 0, browserPasswd );
			}else{
				browserPasswd = $('#'+tag).val();
				callback( 0, browserPasswd );
			}
		}else if( ML4WebApi.webConfig.virtualKeyboardType === "NSHC" ){
			var encData = nFilterEncrypted();
			nFilterJSProcess( encData, "", function(code, data){
				if( code == 0){
					browserPasswd = data;
					callback( 0, browserPasswd );
				}
			});
		}else if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
			var reEncryptValue = npPfsCtrl.GetEncryptResult(document.body, tag);
			reEncryptValue = reEncryptValue.trim();
			var ml4web_crypto_api = ML4WebApi.getCryptoApi();
			ml4web_crypto_api.incaDecrypt(reEncryptValue, function(code, obj){
				if(code == 0){
					callback( 0, obj.stringResult );
				}
			});
		}else if( ML4WebApi.webConfig.virtualKeyboardType === "DREAM"){
			var result = ML4WebVKey.getDecryptedPassword(tag, function(message){
				callback( 0, message );
			});
		}else{
			browserPasswd = $('#'+tag).val();
			callback( 0, browserPasswd );
		}
	}else{
		if( ML4WebApi.webConfig.useVirtualKeyboard ){
			if( ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
				if(keyboardCheck){
					var eleName = tag;
					var pubkey = "-----BEGIN CERTIFICATE-----MIIBCgKCAQEAwWmijy+IsrsM34E6ixbRWZln7ZEnsIFg7ey9wjBlXwn816iKXHVhdr6LIkl4Ks/FY2T/DFJ+Kltw7AICsBTNDLnNZz7nWWVQlq86IJq5ejkrZR88BtQ4aPRshkL8l+DH1Qf4a1A7r/HbGfo/Ad80ns4Z9Z4T5oywX9YMsF2QBAMh+ORE04nwWzkFhrjPkYUiM2sJpWrvJO6FOoidLkCMq8jVu+axhsx6LAraO13F67Ui9NlAydCHtBybZavcfnbhJSUs4QwVfeks58o1+QHLA/hZG4/XbUKWyVmZBK7tt/ZeZuXOtvZVljU3rmjvEb4+FON2kwoplq8iM7hRxFLbFQIDAQAB-----END CERTIFICATE-----";
					var r = transkey_GetEncDream(pubkey,eleName);
					//alert("base64 인코딩 값 : " + r);
					//base64 Decode 적용
					var data = ML4WebApi.base64Decode(r);
					browserPasswd = data;
					callback( 0, browserPasswd );
				}else{
					browserPasswd = $('#'+tag).val();
					callback( 0, browserPasswd );
				}
			}else if( ML4WebApi.webConfig.virtualKeyboardType === "NSHC" ){
				var encData = nFilterEncrypted();
				nFilterJSProcess( encData, "", function(code, data){
					if( code == 0){
						browserPasswd = data;
						callback( 0, browserPasswd );
					}
				});
			}else if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
				var reEncryptValue = npPfsCtrl.GetEncryptResult(document.body, tag);
				reEncryptValue = reEncryptValue.trim();
				var ml4web_crypto_api = ML4WebApi.getCryptoApi();
				ml4web_crypto_api.incaDecrypt(reEncryptValue, function(code, obj){
					if(code == 0){
						callback( 0, obj.stringResult );
					}
				});
			}else if( ML4WebApi.webConfig.virtualKeyboardType === "DREAM"){
				var result = ML4WebVKey.getDecryptedPassword(tag, function(message){
					callback( 0, message );
				});
			}
		}else{
			browserPasswd = $('#'+tag).val();
			callback( 0, browserPasswd );
		}
	}
}

// 인증서 가져오기 확인버튼 동작
function importCertToBrowser(){
	
	$("#form1").attr("style", "display:none;");
	
	// 열기버튼(파일탐색기)
	var CheckPfx = $('#pfx_file').text().indexOf(".pfx");
	var CheckP12 = $('#pfx_file').text().indexOf(".p12");
	
	var b64Cert = $('#file_route').attr("b64Cert");
	var b64CertKey = $('#file_route').attr("b64CertKey");
	var kmCert = $('#file_route').attr("kmCert");
	var kmPri = $('#file_route').attr("kmPri");
	
	var ml4web_crypto_api = ML4WebApi.getCryptoApi();
	
	var browserPasswd = "";
	// 가상키패드 추가
	vpad("add_browser_password", function( code, browserPasswd ){
		if( code == 0 ){
			var bPW = ml4web_crypto_api.HD_api( browserPasswd );
			
			var targetFields = [];
			var storageRawCertIdx = {};
			storageRawCertIdx.storageName = 'web';
			storageRawCertIdx.storageOpt = {};
			
			if(( CheckPfx > -1) || ( CheckP12 > -1) || ($('#file_route2').val().indexOf(".pfx") > -1) || ($('#file_route2').val().indexOf(".p12") > -1)){
				ml4web_crypto_api.pfxImport( b64Cert, bPW, function( code, data ){
					if( code === 0 ){
						if(data.result.kmcert != ""){
							storageRawCertIdx.kmcert = data.result.kmcert; 
						}
						if(data.result.kmpri != ""){
							storageRawCertIdx.kmpri = data.result.kmpri;
						}
						storageRawCertIdx.signcert = data.result.signcert;
						storageRawCertIdx.signpri = data.result.signpri;
						importCertDoAction( bPW, targetFields, storageRawCertIdx );
						return;
					}else if( code === 42101){
						$("#add_browser_password").val('').prop("disabled",true);
						convertFilesToBase64StringErrorDialog( 106 );
					}else{
						$("#add_browser_password").val('').prop("disabled",true);
						convertFilesToBase64StringErrorDialog( 108 );
					}
				});
			}else{
				storageRawCertIdx.signcert = b64Cert;
				storageRawCertIdx.signpri = b64CertKey;
				if(kmCert != ""){
					storageRawCertIdx.kmcert = kmCert; 
				}
				if(kmPri != ""){
					storageRawCertIdx.kmpri = kmPri;
				}
				
				importCertDoAction( bPW, targetFields, storageRawCertIdx );
				return;
			}
		}
	});
}

//filePicker or dragAndDrop 기능처리
function importCertDoAction( bPW, targetFields, storageRawCertIdx ){
	var resultobj = {};
	var ml4web_crypto_api = ML4WebApi.getCryptoApi();
	
	if(storageRawCertIdx.signcert==null || storageRawCertIdx.signcert==""){
		//20200310 mj
		ML4WebDraw.errorHandler("main", $.i18n.prop("ER111"), $("#in_browser"), null);
		return false;
	}else if(storageRawCertIdx.signpri==null || storageRawCertIdx.signpri==""){
		//20200310 mj
		ML4WebDraw.errorHandler("main", $.i18n.prop("ER112"), $("#in_browser"), null);
		return false;
	}else if(bPW==null || bPW==""){
		ML4WebDraw.errorHandler("main", $.i18n.prop("ES006"), $("#add_browser_password"), null);
		return false;
	}

	ml4web_crypto_api.getcertInfo( storageRawCertIdx.signcert, targetFields, function( code, obj ){
		if(code == 0){
			var certbag = {};
			var targetStorageOpt = '{}';
			var storageList = ML4WebApi.getProperty("storageList");
			var targetStorage = "";
			
			if(storageList.indexOf("web_kftc") >= 0){
				targetStorage = "web_kftc";
			} else{
				targetStorage = "web";
			}
			
			storageRawCertIdx.storageCertIdx = obj.result.subkeyid;
			obj.result.storageEncCertIdx = ML4WebApi.makeEncMssage(JSON.stringify(storageRawCertIdx));
			
			resultobj.rowData = obj.result;
			if(storageRawCertIdx.kmcert != ""){
				resultobj.kmcert = storageRawCertIdx.kmcert;
			}
			if(storageRawCertIdx.kmpri != ""){
				resultobj.kmpri = storageRawCertIdx.kmpri;
			}
			resultobj.signcert = storageRawCertIdx.signcert;
			resultobj.signpri = storageRawCertIdx.signpri;
			resultobj.pw = bPW;
			resultobj.selectedStg = targetStorage;
			
			var isExpired = ML4WebUtil.isDateExpired(resultobj.rowData.enddatetime);
			if(isExpired){
				$("#add_browser_password").val('').prop("disabled",true);
				convertFilesToBase64StringErrorDialog( 119 );
				return;
			}
			
			//certbag kmcert, signcert
			resultobj.certbag = {};
			if(storageRawCertIdx.kmcert != "" ){
				resultobj.certbag.kmcert = storageRawCertIdx.kmcert; 
			}
			if(storageRawCertIdx.kmpri != ""){
				resultobj.certbag.kmpri = storageRawCertIdx.kmpri;
			}
			resultobj.certbag.signcert = storageRawCertIdx.signcert;
			resultobj.certbag.signpri = storageRawCertIdx.signpri;
			
			var certList = [obj.result];
			Storage_API_filter.selectfilteredCertList(certList, function(code2, obj2){
				if(code2==0 && obj2.filtered_list.length == 0){
					//지원안함
					convertFilesToBase64StringErrorDialog( 31 );
					return;
				}else{
					if($('#browser_save_yn') && $('#browser_save_yn').is(':checked')){
						//certbag kmcert, signcert
						if(storageRawCertIdx.kmcert != ""){
							certbag.kmcert = storageRawCertIdx.kmcert; 
						}
						if(storageRawCertIdx.kmpri != ""){
							certbag.kmpri = storageRawCertIdx.kmpri;
						}
						certbag.signcert = storageRawCertIdx.signcert;
						certbag.signpri = storageRawCertIdx.signpri;
						
						ML4WebUI.selectStorageInfo(targetStorage, function(resultCode, jsonObj){
							if( resultCode == 0) {
								ML4WebUI.saveCertToStorage( certbag, bPW, targetStorage, targetStorageOpt, function( code, obj ){
									if(code == 0){
										ML4WebUtil.setBackupCertList(null);
										
										// oid 필터 구분하여 callback code 정의
										var policyID = resultobj.rowData.policyid + ",";
										var policyIDList = ML4WebApi.getProperty("cert_filter_oid");
										var policyIDList_new = "";
										
										var filter_arr = policyIDList.split(",");
										var filterArrCnt = filter_arr.length;
										for(var i=0; i<filterArrCnt; i++){
											if(filter_arr[i] != ""){
												policyIDList_new += filter_arr[i] + ",";
											}
										}
										
										//만료인증서 체크
										var isExpired = ML4WebUtil.isDateExpired(resultobj.rowData.enddatetime);
										
										if(policyIDList_new != null && policyIDList_new != ""){
											if(policyIDList_new.indexOf(policyID) > -1){
												if(isExpired){
													resultobj.errCode = 32;
													resultobj.errMsg = $.i18n.prop("ER119");
													ML4WebUI.callback(ML4WebLog.getErrCode("ML4Web_UI_saveCertToStorage"), resultobj);
												}else{
													ML4WebUI.callback(code, resultobj);
												}
											}else{
												resultobj.errCode = 31;
												resultobj.errMsg = $.i18n.prop("ES031");
												ML4WebUI.callback(ML4WebLog.getErrCode("ML4Web_UI_saveCertToStorage"), resultobj);
											}
										}else{
											ML4WebUI.callback(code, resultobj);
										}

									}else if(code == 2423){
										//20200310 mj
										ML4WebDraw.errorHandler("main", $.i18n.prop("ER300"), $("#in_browser"), null);
									}else if(code == 2424){
										//20200310 mj
										ML4WebDraw.errorHandler("main", obj, $("#in_browser"), null);
									}else{
										if(obj.errCode == 30009){
											//20200310 mj
											ML4WebDraw.errorHandler("main", $.i18n.prop("ER101"), $("#in_browser"), null);
										}else if(obj.errCode == 41401){
											ML4WebCert.certErrorHandler(40701, obj);
										}else{
											//20200310 mj
											ML4WebDraw.errorHandler("main", $.i18n.prop("ER113"), $("#in_browser"), null);
										}
									}
								});
							} else{
								ML4WebLog.log("[ERROR!!! - "+resultCode+" ] errCode === " + jsonObj.errCode + ", errMsg === " +  jsonObj.errMsg);
								EmptyCertDiv();
							}
						});
					}else{
						ML4WebUI.callback(0, resultobj);
					}
				}
			});
		}
	});
}

function handleDragOver(evt) {
	evt.stopPropagation();
	evt.preventDefault();
}



//인증서 찾기창 Open
function openSearchPfxDialog(){
	var os_ver = ML4WebApi.getProperty("os");
	var keyName = "";
	var dialogHeight = "";
	
	if(os_ver=="Android" || os_ver=="IPHONE" || os_ver=="BlackBerry"){
		keyName = "import_pfx_m";
		dialogHeight = "215px"
	}else if(os_ver=="IPAD"){
		dialogHeight = "215px"
	}else{
		keyName = "import_pfx";
		dialogHeight = "295px"
	}
	
	var popOption = {
		mode:"main",
		title:$.i18n.prop("TS090"),
		showCert:false,
		certData:{},
		onclick:importPfx,
		contentKey:keyName,
		dialogHeight:dialogHeight
	};
	//20200313 mj
	DSDialog.openDialog(popOption, null, $("#stg_pfx"));
}

//input file focus
function focusPfxFileObj(param){
	if(param=="open"){
		$("#span_filefile").css("border","2px solid black");
		$("#span_filefile").removeClass("ML_cert_file_btn");
		$("#span_filefile").addClass("ML_cert_file_btn_fcs");
	}else if(param=="close"){
		$("#span_filefile").css("border","");
		$("#span_filefile").removeClass("ML_cert_file_btn_fcs");
		$("#span_filefile").addClass("ML_cert_file_btn");
	}
}

//TODO PFX 변환 프로그램 윈도우에서만 다운받도록 수정
function pfxExportdownload(){
	var os_ver = ML4WebApi.getProperty("os");
	
	if(os_ver =="windows 7" || os_ver == "windows 8" || os_ver == "windows 8.1" || os_ver == "windows 10"){
		document.location.href = ML4WebApi.getProperty("PfxExportDownloadUrl");
	}else{
		DSAlert.openAlert("main", "PFX 인증서 변환 프로그램은 Windows 계열만 지원합니다.",null)
	}
}


function pad(n, width) {
	n = n + '';
	return n.length >= width ? n : new Array(width - n.length + 1).join('0') + n;
}

//certFile -> base64 에러처리 공통 
function convertFilesToBase64StringErrorDialog( code ){
	if(code == 31){
		code = pad(code, 3);
		//20200310 mj
		ML4WebDraw.errorHandler("main", $.i18n.prop("ES"+code), $("#in_browser"), null);
	}else{
		//20200310 mj
		ML4WebDraw.errorHandler("main", $.i18n.prop("ER"+code), $("#in_browser"), null);
	}
	
	$('.ML_cert_orgtext_area2').attr('style','display:none;');
	$('#drag_info_img').show();
	initTable();
	return;
}

// drag and drop 이벤트 처리
function DragAndDropCert(evt) {
	evt.stopPropagation(); // Do not allow the drop event to bubble.
	evt.preventDefault(); // Prevent default drop event behavior.
	initTable();
	$("#add_browser_password").val('').prop("disabled",false);
	certFileProcess( evt.dataTransfer.files );
}

//인증서 테이블 초기화
function initTable(){
	$('#file_route').text("");
	$('#file_route_issuer').text("");
	$('#file_route_enddate').text("");
	$('#pfx_type').text("");
	$('#pfx_file').text("");
	$('#filefile2').val("");
	$('#file_route2').val("");
}


//certfile처리 공통
function certFileProcess( files ){
	
	var today = new Date();
	var yyyy = today.getFullYear(); // 2017
	var mm = today.getMonth()+1; // 11
	var dd = today.getDate(); // 29
	
	var b64Cert='';
	var b64CertKey='';
	var kmCert='';
	var kmPri='';
	var targetFields = [];
	var ml4web_crypto_api = ML4WebApi.getCryptoApi();

	$('#file_route2').val('');
	$('#file_route2').removeAttr("b64Cert");
	$('#file_route2').removeAttr("b64CertKey");
	$('#file_route2').removeAttr("kmCert");
	$('#file_route2').removeAttr("kmPri");
	
	ML4WebApi.convertFilesToBase64String( files , function( code, data ){
		
		if( code == 0 ){
			$('#file_route2').val( data.fileName );
			if( data.type === "cert" ){
				
				$('.ML_cert_orgtext_area2').attr('style','display:"";');
				$('#drag_info_img').hide();
				$('#cert_der').attr('style','display:"";');
				$('#cert_pfx').attr('style','display:none;');
				
				$('#file_route').attr( "b64Cert", data.cert );
				$('#file_route').attr( "b64CertKey", data.key );
				$('#file_route').attr( "kmCert", data.kmCert );
				$('#file_route').attr( "kmPri", data.kmPri );
				
				ml4web_crypto_api.getcertInfo( data.cert, targetFields, function( code, obj ){
					
					if(code == 0){
						
						var issurname = (obj.result.issuername).split(',');
						var issuerOrganization=(issurname[1].split('='))[1];
						var issuerName = "";
						var EndDate = "";
						var enddate_split = (obj.result.enddate).split('-');
						var certName = "";
						var CN = (obj.result.subjectname).split(',');
						var CN_Name = (CN[0].split('='))[1];
						var use_Name = obj.result.policyid;
						
						// 인증서명 길이 체크  길이24 가 마지노선
						if(CN_Name.length > 24){
							certName = CN_Name.substring(0,18) + "...";
						}else{
							certName = CN_Name;
						}
						
						// 만료된 인증서
						var isExpired = ML4WebUtil.isDateExpired(obj.result.enddatetime);
						
						if(isExpired){
							$("#add_browser_password").val('').prop("disabled",true);
							convertFilesToBase64StringErrorDialog( 119 );
							return;
						}
						
						var certList = [obj.result];
						Storage_API_filter.selectfilteredCertList(certList, function(code2, obj2){
							if(code2==0 && obj2.filtered_list.length == 0){
								//지원안함
								convertFilesToBase64StringErrorDialog( 31 );
								return;
							}else{
								// 날짜 형식 맞춤
								if(enddate_split[1] < 10){
									enddate_split[1] = "0" + enddate_split[1];
								}
								if(enddate_split[2] < 10){
									enddate_split[2] = "0" + enddate_split[2];
								}
								EndDate = enddate_split[0] + "-" + enddate_split[1] + "-" + enddate_split[2];
								
								// 발급기관
								if(issuerOrganization.toUpperCase() == "GPKI"){
									issuerName = issuerOrganization;
								}else{
									issuerName = (issurname[2].split('='))[1];
								}
								
								$('#sub_title').text($.i18n.prop("ES045"));
								// 마우스 가져갔을때 가져온 여러 인증서 내용 풀네임 출력
								$('#file_route').attr("title",CN_Name);
								$('#file_route').text(certName);
								$('#file_route_issuer').text(issuerName);
								$('#file_route_enddate').text(EndDate);
							
								//pfx_file init
								$('#pfx_file').text("");
							}
						});
						
					}else{
						$("#add_browser_password").val('').prop("disabled",true);
						convertFilesToBase64StringErrorDialog( 120 );
						return;
					}
				});
			}else{
				// pfx
				$('.ML_cert_orgtext_area2').attr('style','display:"";');
				$('#drag_info_img').hide();
				$('#cert_der').attr('style','display:none;');
				$('#cert_pfx').attr('style','display:"";');
				
				$('#file_route').attr( "b64Cert", data.pfx );
				
				if( data.type == "pfx"){
					$('#sub_title').text($.i18n.prop("TS088"));
				}else{
					$('#sub_title').text($.i18n.prop("TS089"));
				}
					
				$('#pfx_type').text( data.type );
				var pfxPath = 'C:/fakepath/' + data.fileName;
				$('#pfx_file').text(pfxPath);
			}
			
			$("#add_browser_password").css({ 'pointer-events': 'auto' });
			
		}else{
			$("#add_browser_password").val('').prop("disabled",true);
			convertFilesToBase64StringErrorDialog( code );
			return;
		}
	});
}


//drag한 인증서 파일에 따라 테이블 형태를 변경
function tableState(files){
	if((files[0].name.indexOf(".cer") > -1) || (files[0].name.indexOf(".der") > -1) || (files[0].name.indexOf(".key") > -1)){
		$('.ML_cert_orgtext_area2').attr('style','display:"";');
		$('#drag_info_img').hide();
		$('#cert_der').attr('style','display:"";');
		$('#cert_pfx').attr('style','display:none;');
	}else{
		$('.ML_cert_orgtext_area2').attr('style','display:"";');
		$('#drag_info_img').hide();
		$('#cert_der').attr('style','display:none;');
		$('#cert_pfx').attr('style','display:"";');
	}
}

//************************ [END]    sub dialog event     **********************************/

/**
 * CS 인스톨, 업데이트 체크 함수
*/
var ML4WebCS = {
		
	installCheck : function(mode, callback){
		
		if(magiclineUtil.isMobile(magiclineUtil.getOS())){
			
			//errorHandler("main", "해당 사이트 스마트 인증은<br> 모바일 기기를 지원하지 않습니다.", null, null);
			callback(10001,"Mobile");
			return;
		}
		
		var callbackObj = {};
		
		ML4WebApi.getCsManager().checkinstall(function(code, obj){
			
			if(code == 0){
				
				ML4WebApi.setProperty("is_cs_install", true);
				ML4WebCS.updateCheck(mode, callback);
				return;
			}else{
						
				if(obj.csUpdateChk == "upgrade"){ 
					//ML4WebDraw.openCSUpdateDialog(mode);
					
					ML4WebApi.setProperty("is_cs_install", false);
					ML4WebApi.setProperty("is_cs_update", true);
					
					callbackObj.is_cs_install = false; 
					callbackObj.is_cs_update = true;
					
					
				}else{
					//ML4WebDraw.openCSInstallDialog(mode);
					
					ML4WebApi.setProperty("is_cs_install", false);
					ML4WebApi.setProperty("is_cs_update", false);
					
					callbackObj.is_cs_install = false; 
					callbackObj.is_cs_update = false;
										
				}
				callback(code, callbackObj);	
			} // end of else
		});
			
	},
	updateCheck : function(mode, callback){
		
		ML4WebApi.getCsManager().checkupdate(function(code, obj){
			//true : upgrade 필요, false : 최신버전
			ML4WebLog.log("[main]ML4WebApi.getCsManager().checkupdate callback code === "+code + " /  obj === " + JSON.stringify(obj));
			
			var callbackObj = {
				is_cs_install : true	
			};
			
			if(code == 0){
				if(obj.msg == "upgrade"){
						
					//ML4WebDraw.openCSUpdateDialog(mode);				
					ML4WebApi.setProperty("is_cs_install", true);
					ML4WebApi.setProperty("is_cs_update", true);
						
					callbackObj.is_cs_update = true;
					
				}else{
					
					ML4WebApi.setProperty("is_cs_install", true);
					ML4WebApi.setProperty("is_cs_update", false);
						
					callbackObj.is_cs_update = false;
					
				}
			}else{ // end if(code == 0)
					
				ML4WebApi.setProperty("is_cs_update", false);
				callbackObj.is_cs_update = false;
			}
			
			callback(code, callbackObj);
			
		});
	}
}

$('#input_cert_pw_save').click(function(){
	$('#input_cert_pw_save').removeAttr("style");
	keyboardCheck = false;
	if(!keyboardCheck){
		if(!magiclineUtil.isMobile(magiclineUtil.getOS())){
			if(selectMedia === "web_kftc" || selectMedia === "web"){
				if( ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
					$("#input_cert_pw_save").val('').prop("disabled",true);
				}else if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
					$('#input_cert_pw_save').val("");
					$("#form1").attr("style", "display:block;");
					$("#nppfs-keypad-input_cert_pw_save").css("z-index", 111111);
				}
			}else{
				if( ML4WebApi.webConfig.useVirtualKeyboard ){
					if( ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
						$("#input_cert_pw_save").val('').prop("disabled",true);
					}else if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
						$('#input_cert_pw_save').val("");
						$("#form1").attr("style", "display:block;");
						$("#nppfs-keypad-input_cert_pw_save").css("z-index", 111111);
					}
				}
			}
		}
	}
	$('#input_cert_pw_save').val("");
});

//인증서 저장 엔터키 이벤트 리스너
function saveCertEnterKeyEvent(e){
	if (e.keyCode == 13) {
		$('#btn_confirm_saveCert').click();
	} else {
		e.keyCode == 0;
		return;
	}
}

// 인증서 저장 버튼 클릭 이벤트
$('#btn_confirm_saveCert').click(function () {
	clickSaveConfirmBtn();
});

function clickSaveConfirmBtn(){
	if(magiclineUtil.isMobile(magiclineUtil.getOS())){
		var data = $('#input_cert_pw_save').val();
		saveCertconfirmProcess( data );
	}else{
		if( ML4WebApi.webConfig.useVirtualKeyboard ){
			if( ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
				if(keyboardCheck){
					var eleName = "input_cert_pw_save";
					var pubkey = "-----BEGIN CERTIFICATE-----MIIBCgKCAQEAwWmijy+IsrsM34E6ixbRWZln7ZEnsIFg7ey9wjBlXwn816iKXHVhdr6LIkl4Ks/FY2T/DFJ+Kltw7AICsBTNDLnNZz7nWWVQlq86IJq5ejkrZR88BtQ4aPRshkL8l+DH1Qf4a1A7r/HbGfo/Ad80ns4Z9Z4T5oywX9YMsF2QBAMh+ORE04nwWzkFhrjPkYUiM2sJpWrvJO6FOoidLkCMq8jVu+axhsx6LAraO13F67Ui9NlAydCHtBybZavcfnbhJSUs4QwVfeks58o1+QHLA/hZG4/XbUKWyVmZBK7tt/ZeZuXOtvZVljU3rmjvEb4+FON2kwoplq8iM7hRxFLbFQIDAQAB-----END CERTIFICATE-----";
					var r = transkey_GetEncDream(pubkey,eleName);
					//alert("base64 인코딩 값 : " + r);
					//base64 Decode 적용
					var data = ML4WebApi.base64Decode(r);
					saveCertconfirmProcess( data );
				}else{
					var data = $('#input_cert_pw_save').val();
					saveCertconfirmProcess( data );
				}
			}else if( ML4WebApi.webConfig.virtualKeyboardType === "NSHC" ){
				var encData = nFilterEncrypted();
				nFilterJSProcess( encData, "", function(code, data){
					if( code == 0){					
						saveCertconfirmProcess( data );	
					}
				});
			}else if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
				var reEncryptValue = npPfsCtrl.GetEncryptResult(document.body, "input_cert_pw_save");
				reEncryptValue = reEncryptValue.trim();
				var ml4web_crypto_api = ML4WebApi.getCryptoApi();
				ml4web_crypto_api.incaDecrypt(reEncryptValue, function(code, obj){
					if(code == 0){
						saveCertconfirmProcess( obj.stringResult );	
					}
				});
			}else if( ML4WebApi.webConfig.virtualKeyboardType === "DREAM" ){
				var elementId = "input_cert_pw_save";
				var result = ML4WebVKey.getDecryptedPassword(elementId, function(message){
					saveCertconfirmProcess( message );
				});
			}
		}else{
			var data = $('#input_cert_pw_save').val();
			saveCertconfirmProcess( data );
		}
	}
}

/**
 * 입력받은 비번과 스토리지 정보를 ML4WebUI 로 callback
 * @param data
 * @returns
 */
function saveCertconfirmProcess( data ){
	
	var cryptoApi = ML4WebApi.getCryptoApi();
	var certPw 	  = cryptoApi.HD_api(data);
	
	if(certPw == null || certPw == ""){
		ML4WebLog.log("비밀번호를 입력하세요.");		
		ML4WebDraw.errorHandler("main", $.i18n.prop("ES006"), $("#input_cert_pw_save"), null);		
		return;
	}
	
	// Return Object Mapping
	var resultObj = new Object();
	resultObj.certPw 	      	= certPw;
	resultObj.selectedStorage 	= ML4WebUI.selectedStorage.current_option.storageName;
	resultObj.storageOpt 	    = ML4WebUI.selectedStorage.current_option.storageOpt;
	
	ML4WebUI.callback(0, resultObj);
	
}

function clickConfirmBtn(){
	var selection = $("#dataTable").MLjquiDataTable('getSelection');
	var selectionCnt = selection.length;
	
	$('#btn_confirm_iframe').attr('disabled', true);
	
	if( selection && selectionCnt > 0 ){
		var pwdData = '';	
		if( !ML4WebApi.webConfig.useVirtualKeyboard && (ML4WebApi.webConfig.virtualKeyboardType === "INCA" || ML4WebApi.webConfig.virtualKeyboardType === "DREAM") ){
			if(selectMedia != 'web_kftc' && selectMedia != 'web'){
				pwdData = $('#input_cert_pw_new').val();
			}else{
				pwdData = $('#input_cert_pw').val();
			}
		}else{
			pwdData = $('#input_cert_pw').val();
		}
		
		if(pwdData == null || pwdData == ""){
			ML4WebLog.log("비밀번호를 입력하세요.");
			ML4WebDraw.errorHandler("main", $.i18n.prop("ES006"), $("#input_cert_pw"), null);
			return;
		}

		/*if(magiclineUtil.isMobile(magiclineUtil.getOS())){
			confirmProcess( pwdData );
		}else{*/
			if(selectMedia === "web_kftc" || selectMedia === "web"){
				if( ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
					if(keyboardCheck){
						var eleName = "input_cert_pw";
						var pubkey = "-----BEGIN CERTIFICATE-----MIIBCgKCAQEAwWmijy+IsrsM34E6ixbRWZln7ZEnsIFg7ey9wjBlXwn816iKXHVhdr6LIkl4Ks/FY2T/DFJ+Kltw7AICsBTNDLnNZz7nWWVQlq86IJq5ejkrZR88BtQ4aPRshkL8l+DH1Qf4a1A7r/HbGfo/Ad80ns4Z9Z4T5oywX9YMsF2QBAMh+ORE04nwWzkFhrjPkYUiM2sJpWrvJO6FOoidLkCMq8jVu+axhsx6LAraO13F67Ui9NlAydCHtBybZavcfnbhJSUs4QwVfeks58o1+QHLA/hZG4/XbUKWyVmZBK7tt/ZeZuXOtvZVljU3rmjvEb4+FON2kwoplq8iM7hRxFLbFQIDAQAB-----END CERTIFICATE-----";
						var r = transkey_GetEncDream(pubkey,eleName);
						//alert("base64 인코딩 값 : " + r);
						//base64 Decode 적용
						pwdData = ML4WebApi.base64Decode(r);
						confirmProcess( pwdData );
					}else{
						confirmProcess( pwdData );
					}
				}else if( ML4WebApi.webConfig.virtualKeyboardType === "NSHC" ){
					var encData = nFilterEncrypted();
					nFilterJSProcess( encData, "", function(code, data){
						if( code == 0){
							confirmProcess( data );	
						}
					});
				}else if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
					var reEncryptValue = npPfsCtrl.GetEncryptResult(document.body, "input_cert_pw");
					reEncryptValue = reEncryptValue.trim();
					var ml4web_crypto_api = ML4WebApi.getCryptoApi();
					ml4web_crypto_api.incaDecrypt(reEncryptValue, function(code, obj){
						if(code == 0){
							confirmProcess( obj.stringResult );	
						}
					});
				}else if( ML4WebApi.webConfig.virtualKeyboardType ==="DREAM" ){
					//pwdData = ML4WebVKey.getDecryptedPassword();
					var elementId = "input_cert_pw";
					var result = ML4WebVKey.getDecryptedPassword(elementId, function(message){
						confirmProcess( message );
					});
				}else{
					confirmProcess( pwdData );
				}	
			}else{
				if( ML4WebApi.webConfig.useVirtualKeyboard ){
					if( ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
						if(keyboardCheck){
							var eleName = "input_cert_pw";
							var pubkey = "-----BEGIN CERTIFICATE-----MIIBCgKCAQEAwWmijy+IsrsM34E6ixbRWZln7ZEnsIFg7ey9wjBlXwn816iKXHVhdr6LIkl4Ks/FY2T/DFJ+Kltw7AICsBTNDLnNZz7nWWVQlq86IJq5ejkrZR88BtQ4aPRshkL8l+DH1Qf4a1A7r/HbGfo/Ad80ns4Z9Z4T5oywX9YMsF2QBAMh+ORE04nwWzkFhrjPkYUiM2sJpWrvJO6FOoidLkCMq8jVu+axhsx6LAraO13F67Ui9NlAydCHtBybZavcfnbhJSUs4QwVfeks58o1+QHLA/hZG4/XbUKWyVmZBK7tt/ZeZuXOtvZVljU3rmjvEb4+FON2kwoplq8iM7hRxFLbFQIDAQAB-----END CERTIFICATE-----";
							var r = transkey_GetEncDream(pubkey,eleName);
							//alert("base64 인코딩 값 : " + r);
							//base64 Decode 적용
							pwdData = ML4WebApi.base64Decode(r);
							confirmProcess( pwdData );
						}else{
							confirmProcess( pwdData );
						}
					}else if( ML4WebApi.webConfig.virtualKeyboardType === "NSHC" ){
						var encData = nFilterEncrypted();
						nFilterJSProcess( encData, "", function(code, data){
							if( code == 0){
								confirmProcess( data );	
							}
						});
					}else if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
						var reEncryptValue = npPfsCtrl.GetEncryptResult(document.body, "input_cert_pw");
						reEncryptValue = reEncryptValue.trim();
						var ml4web_crypto_api = ML4WebApi.getCryptoApi();
						ml4web_crypto_api.incaDecrypt(reEncryptValue, function(code, obj){
							if(code == 0){
								confirmProcess( obj.stringResult );	
							}
						});
					}else if( ML4WebApi.webConfig.virtualKeyboardType ==="DREAM" ){
						var elementId = "input_cert_pw";
						var result = ML4WebVKey.getDecryptedPassword(elementId, function(message){
							confirmProcess( message );
						});
					}	
				}else{
					confirmProcess( pwdData );
				}
			}
		//}
	}else{
		ML4WebLog.log("선택된 인증서가 없습니다.");
		//openAlertDialog($.i18n.prop("ES021"));
		
		ML4WebDraw.errorHandler("main", $.i18n.prop("ES021"), null, null);
		//DSAlert.openAlert("main", $.i18n.prop("ES021"), null);
		return;
	}
}

jQuery(document).on("nppfs-npv-after-enter", function(event){
	if(event.name === "add_browser_password"){
		importCertToBrowser();
	}else if(event.name === "input_pw_confirm"){
		processCopyCert();
	}else if(event.name === "input_cert_pw_save"){
		clickSaveConfirmBtn();
	}else{
		clickConfirmBtn();
	}
});

//인증서 복사
$('#btn_CopyCert').click(function () {		
	var selection = $("#dataTable").MLjquiDataTable('getSelection');

	if(selection && selection.length > 0){
		if (typeof(selection[0]) =="object" && typeof(selection[0].source) != "undefined" && typeof(selection[0].cloud) != "undefined" && selection[0].cloud == "true") {
			if(ML4WebApi.webConfig.virtualKeyboardType === "INCA"){
				// 키패드 계속 생성되는 문제로 인한 초기화 패치(2019/06/14)
				var npLength = npVCtrl.keypadObject.length;
				for (var i=0; i<npLength; i++){
					if("nppfs-keypad-input_pw_confirm" == npVCtrl.keypadObject[i]._uuid){
						npVCtrl.keypadObject.splice(i,1);
						break;
					}
				}
			}
			
			openCopyCertDialog(selection[0]);
		}else{
			//20200310 mj
			ML4WebDraw.errorHandler("main", $.i18n.prop("ES083"), $('#btn_CopyCert'), null);
		}
	}else{
		ML4WebLog.log($.i18n.prop("ES021"));
		//20200310 mj
		ML4WebDraw.errorHandler("main", $.i18n.prop("ES021"), $('#btn_CopyCert'), null);
	}
});

//인증서 복사 다이얼로그 Open
function openCopyCertDialog(data) {
	var popOption = {
		mode:"main",
		title:"인증서 복사",
		showCert:true,
		certData:data,
		onclick:processCopyCert,
		contentKey:"select_browser",
		dialogHeight:"280px"
	};
	//20200310 mj
	var btnObj = $("#btn_CopyCert");
	DSDialog.openDialog(popOption, btnObj, function(code,jsonObj){
		$("#input_pw_prev").focus();
		if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
			npPfsCtrl.RegistDynamicField("form1", "input_pw_confirm");
		}
	});
}

function processCopyCert(data) {
	var selection = $("#dataTable").MLjquiDataTable('getSelection');
	data = selection[0];
	var ml4web_crypto_api = ML4WebApi.getCryptoApi();
	
	var pwdData = $('#input_pw_confirm').val();
	if(pwdData == null || pwdData == ""){
		ML4WebLog.log("비밀번호를 입력하세요.");
		ML4WebDraw.errorHandler("main", $.i18n.prop("ES006"), $("#input_pw_confirm"), null);
		return;
	}
	
	vpad("input_pw_confirm", function( code, browserPasswd ){
		if( code == 0 ){
			var bPW = ml4web_crypto_api.HD_api( browserPasswd );
			
			var certInfo = JSON.parse(ML4WebApi.makeDecMssage(data.storageEncCertIdx));
			certInfo.storageOpt.password = bPW;	
			certInfo.storageOpt.fingerprint = certInfo.storageCertIdx;
			
			Storage_API_kftc.GetCertString(certInfo, function(code, result) {
				if(code == 0){
					passwd = bPW;
					
					var certbag = {};
					certbag.signcert = result.cert.signcert;
					certbag.signpri = result.cert.signpri;
					
					var targetStorage = "web";
					var targetStorageOpt = '{}';
					
					var targetStorageRawIdx = {}
					targetStorageRawIdx.storageName = targetStorage;
					targetStorageRawIdx.storageOpt =  targetStorageOpt;
					targetStorageRawIdx.storageCertIdx = "";
					DSDialog.closeDialog(function(code, obj){
						Storage_API_web.SaveCert(certbag, passwd, targetStorageRawIdx, function(resultCode, resultValue){
							if(resultCode == 0){
								//20200310 mj
								ML4WebDraw.errorHandler("main", $.i18n.prop("ES084"), $('#btn_CopyCert'), null);
								$("#stg_web_kftc").click();
								//20200310 mj
								$('#btn_alert_confirm').focus();
							}
						});
					});
				}else if(code == ML4WebLog.getErrCode("Storage_kftc")){	//금결원 공동인증서 에러
					if (result.code == 2211) {
						if (result.failCount == result.maxFailCount){
							DSAlert.callbackAlert("main", "1", function(data){
								var jsonObj = new Object();
								jsonObj.errCode = -1;
								callback(-1, jsonObj);
							});
						} else {
							//20200310 mj
							ML4WebDraw.errorHandler("main", result.message, $('#btn_CopyCert'), null);
						}
					} else if (result.code == 2212) {
						//20200310 mj
						ML4WebDraw.errorHandler("main", result.message, $('#btn_CopyCert'), null);
					} else {
						//20200310 mj
						ML4WebDraw.errorHandler("main", result.message, $('#btn_CopyCert'), null);
					}
				}
			});
		}
	});
}

function clickpwd(){
	$('#input_pw_confirm').removeAttr("style");
	keyboardCheck = false;
	if(!keyboardCheck){
		if(ML4WebApi.getProperty("storageList").indexOf("web_kftc") > -1 || ML4WebApi.getProperty("storageList").indexOf("kftc") > -1){
			if( ML4WebApi.webConfig.virtualKeyboardType === "NSHC" ){
				$('#input_pw_confirm').val("");
				keyboardCheck = true;
			}
			if( ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
				keyboardCheck = true;
				if(typeof initTranskey=="function") {
					transkey["input_pw_confirm"] = null;
					initTranskey();
				}
				//raon keypad 위치 가운데로 수정
				var copyPwdTop = $("#input_pw_confirm").offset().top;
				var copyPwdLeft = $("#input_pw_confirm").offset().left;
				copyPwdTop = copyPwdTop + 30;
				copyPwdLeft = copyPwdLeft - 185;
				$('#input_pw_confirm').attr("data-tk-kbdxy", copyPwdLeft + " " + copyPwdTop);
				
				tk.onKeyboard(document.getElementById('input_pw_confirm'));
			}
			if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
				$('#input_pw_confirm').val("");
				$("#form1").attr("style", "display:block;");
				$("#nppfs-keypad-input_pw_confirm").css("z-index", 111111);
			}
		}else{
			if( ML4WebApi.webConfig.useVirtualKeyboard ){
				if( ML4WebApi.webConfig.virtualKeyboardType === "NSHC" ){
					$('#input_pw_confirm').val("");
					keyboardCheck = true;
				}
				if( ML4WebApi.webConfig.virtualKeyboardType === "RAON" ){
					keyboardCheck = true;
					if(typeof initTranskey=="function") {
						transkey["input_pw_confirm"] = null;
						initTranskey();
					}
					//raon keypad 위치 가운데로 수정
					var copyPwdTop = $("#input_pw_confirm").offset().top;
					var copyPwdLeft = $("#input_pw_confirm").offset().left;
					copyPwdTop = copyPwdTop + 30;
					copyPwdLeft = copyPwdLeft - 185;
					$('#input_pw_confirm').attr("data-tk-kbdxy", copyPwdLeft + " " + copyPwdTop);
					
					tk.onKeyboard(document.getElementById('input_pw_confirm'));
				}
				if( ML4WebApi.webConfig.virtualKeyboardType === "INCA" ){
					$('#input_pw_confirm').val("");
					$("#form1").attr("style", "display:block;");
					$("#nppfs-keypad-input_pw_confirm").css("z-index", 111111);
				}
			}
		}
	}
}

//인증서 삭제
$('#btn_deleteCert').click(function () {		
	ML4WebLog.log("deleteCertificate...");
	var selection = $("#dataTable").MLjquiDataTable('getSelection');
	var htmlTxt ='';
	if(selection && selection.length > 0){
		var stgKey =  ML4WebUI.selectedStorage.key;
		if(stgKey=='smart' || stgKey=='mobile' || stgKey=='pfx'){
			//20200310 mj
			ML4WebDraw.errorHandler("main", $.i18n.prop("ER101"), $('#btn_deleteCert'), null);
		}else if(stgKey=='web' || stgKey=='hdd' || stgKey=='token' || stgKey=='web_kftc'){
			deleteCertNonePwd('main', stgKey, selection[0]);
		}

	}else{
		ML4WebLog.log($.i18n.prop("ES021"));
		//20200310 mj
		ML4WebDraw.errorHandler("main", $.i18n.prop("ES021"), $('#btn_deleteCert'), null);
	}
});

//인증서 복사 안내
$("#btn_CopyCert").on("mouseover", function(event){
	var thisIsTop = $(this).offset().top + 30;
	var thisIsLeft = $(this).offset().left;
	var divArea = $("<div id='copyCert_box' class='copyCert_box'>클라우드에서 브라우저로 인증서 복사</div>");
	divArea.css("top",thisIsTop+"px");
	divArea.css("left",thisIsLeft+"px");
	$("body").append(divArea);
});

$("#btn_CopyCert").on("mouseout", function(event){
	$("body").find("div[id='copyCert_box']").remove();
});
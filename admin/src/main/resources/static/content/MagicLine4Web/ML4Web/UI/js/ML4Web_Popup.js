(function() {
    function popupModule() {
        (function(jQuery) {
//			console.log( "popupModule called...typeof window = " + typeof window);
  
			var mode = "";
			var dialog_title = "";
			var showCert = true;	//(true:false)
			var certData = {};
			var onclick = "";
			var contentKey = "";
			var content = "";
			var dialogHeight = "";
			var chkBrowser = ML4WebApi.getProperty("browser");
			var os = ML4WebApi.getProperty("os");
			var jqxwidget1 = $('#ML_window');
			var offset1 = jqxwidget1.offset();
			//브라우저인증서저장 버튼 활성화/비활성화 flag
			var selectStg = "";
			//20200310 mj
			var focusObj = {};
			
			var HandlePopup = {
				init: function(e) {
//					console.log( "The public can see me!" );
				},
				setProperty : function(key, value) {
					if(key=="certData"){
						certData = value;
					}
				},
				getProperty : function(key) {
					if(key=="certData"){
						return certData;
					}else{
						return mode;
					}
				},
				removeProperty : function(key) {

				},
				openConfirm : function(opt, obj, callback ){
					//20200310 mj
					focusObj = obj;
					this.setOptions(opt);
					$('#ML_content_area').empty().html(content);
					if ( $("#ML_Dialog_common").length > 0 ) {
						$('#ML_Dialog_common').MLjquiWindow({ title: dialog_title });
						$('#ML_Dialog_common').MLjquiWindow({height:dialogHeight});
						$('#ML_Dialog_common').MLjquiWindow({position:{x: offset1.left + ($(window).width()/2) - 200, y: offset1.top + 120}});
						$('#ML_Dialog_common').MLjquiWindow('open',callback);
						$('#ML_Dialog_common').find('#btn_common_confirm').find('span').text($.i18n.prop("TS014"));
						//20200310 mj
						$('#ML_Dialog_common').focus();
					}
				},
				openDialog : function(opt, obj, callback) {
					//20200310 mj
					focusObj = obj;
					this.setOptions(opt);
					if(opt.contentKey == "install_cs"){
						if(mode=='main'){
							//client 서비스로 변경하면서 '실행' 버튼을 없애기 위해 ie만 다르게 작동하던 것을 다 똑같은 alert이 나오도록 수정
							/*if(chkBrowser.indexOf("Firefox") > -1 || chkBrowser.indexOf("Chrome") > -1 || chkBrowser.indexOf("Opera") > -1 || os === "MAC"){*/
								$('#ML_content_area').empty().html(content);
								
								if ( $("#ML_Dialog_common").length > 0 ) {
									$('#ML_Dialog_common').MLjquiWindow({ title: dialog_title });
									$('#ML_Dialog_common').MLjquiWindow({height:dialogHeight});
									//$('#ML_Dialog_common').MLjquiWindow({position:'center'});
									$('#ML_Dialog_common').MLjquiWindow({position:{x: offset1.left + ($(window).width()/2) - 200, y: offset1.top + 120}});
									$('#ML_Dialog_common').MLjquiWindow('open',callback);
									$('#ML_Dialog_common').find('#btn_common_confirm').find('span').text($.i18n.prop("TS014"));
									//20200310 mj
									$('#ML_Dialog_common').focus();
								}
							/*}else{
								$('#ML_content_area_cs').empty().html(content);
								
								if ( $("#ML_dialog_cs_install").length > 0 ) {
									$('#ML_dialog_cs_install').MLjquiWindow({ title: dialog_title });
									$('#ML_dialog_cs_install').MLjquiWindow({height:dialogHeight});
									//$('#ML_dialog_cs_install').MLjquiWindow({position:'center'});
									$('#ML_dialog_cs_install').MLjquiWindow({position:{x: offset1.left + ($(window).width()/2) - 200, y: offset1.top + 120}});
									$('#ML_dialog_cs_install').MLjquiWindow('open',callback);
								}
							}*/
						}else if(mode=='mgmt'){
							$('#ML_mgmt_content_area').empty().html(content);
							
							$('#ML_Dialog_mgmt_common').MLjquiWindow({title: dialog_title});
							$('#ML_Dialog_mgmt_common').MLjquiWindow({height:dialogHeight});
							//$('#ML_Dialog_mgmt_common').MLjquiWindow({position:'center'});
							$('#ML_Dialog_mgmt_common').MLjquiWindow({position:{x: offset1.left + ($(window).width()/2) - 200, y: offset1.top + 120}});
							$('#ML_Dialog_mgmt_common').MLjquiWindow('open',callback);
							//20200310 mj
							$('#ML_Dialog_mgmt_common').focus();
							return;
						}else{
							
						}
					}else if(opt.contentKey == "install_update"){
						$('#ML_content_area').empty().html(content);
						if ( $("#ML_Dialog_common").length > 0 ) {
							$('#ML_Dialog_common').MLjquiWindow({ title: dialog_title });
							$('#ML_Dialog_common').MLjquiWindow({height:dialogHeight});
							//$('#ML_Dialog_common').MLjquiWindow({position:'center'});
							$('#ML_Dialog_common').MLjquiWindow({position:{x: offset1.left + ($(window).width()/2) - 200, y: offset1.top + 120}}); 
							$('#ML_Dialog_common').MLjquiWindow('open',callback);
							$('#ML_Dialog_common').find('#btn_common_confirm').find('span').text($.i18n.prop("TS070"));
							//20200310 mj
							$('#ML_Dialog_common').focus();
							return;
						}
					}else if(opt.contentKey == "check_cert_expire"){
						$('#ML_content_area').empty().html(content);

						$('#ML_Dialog_common').MLjquiWindow({ title: dialog_title });
						$('#ML_Dialog_common').MLjquiWindow({height:dialogHeight});
						$('#ML_Dialog_common').MLjquiWindow({position:{x: offset1.left + ($(window).width()/2) - 200, y: offset1.top + 170}});
						$('#ML_Dialog_common').MLjquiWindow('open',callback);
						//20200310 mj
						$('#ML_Dialog_common').focus();
						return;
					}else{
						if(mode=='main'){
							$('#ML_content_area').empty().html(content);

							$('#ML_Dialog_common').MLjquiWindow({ title: dialog_title });
							$('#ML_Dialog_common').MLjquiWindow({height:dialogHeight});
							//$('#ML_Dialog_common').MLjquiWindow({position:'center'});
							$('#ML_Dialog_common').MLjquiWindow({position:{x: offset1.left + ($(window).width()/2) - 200, y: offset1.top + 120}});
							$('#ML_Dialog_common').MLjquiWindow('open',callback);
							$('#ML_Dialog_common').find('#btn_common_confirm').find('span').text($.i18n.prop("TS014"));
							//20200310 mj
							$('#ML_Dialog_common').focus();
							return;
						}else if(mode=='mgmt'){
							$('#ML_mgmt_content_area').empty().html(content);
							
							$('#ML_Dialog_mgmt_common').MLjquiWindow({title: dialog_title});
							$('#ML_Dialog_mgmt_common').MLjquiWindow({height:dialogHeight});
							//$('#ML_Dialog_mgmt_common').MLjquiWindow({position:'center'});
							$('#ML_Dialog_mgmt_common').MLjquiWindow({position:{x: offset1.left + ($(window).width()/2) - 200, y: offset1.top + 120}});
							$('#ML_Dialog_mgmt_common').MLjquiWindow('open',callback);
							//20200310 mj
							$('#ML_Dialog_mgmt_common').focus();
							return;
						}else{

						}
					}
				},
				closeDialog : function(callback) {
					//document.getElementById('dialog_wrap').style.display='none';
					if(mode=='main'){
						$('#ML_Dialog_common').MLjquiWindow('close');
						$('#ML_dialog_cs_install').MLjquiWindow('close');
						//$('#browser_manual').MLjquiWindow('close');
					}else if(mode=='mgmt'){
						$('#ML_Dialog_mgmt_common').MLjquiWindow('close');
					}else {

					}
					
					//20200310 mj
					if(!$.isEmptyObject(focusObj)){
						focusObj.focus();
					}
					
					callback(0,{"mode" : mode});
				},
				runDialog : function(callback) {
					$('#ML_dialog_cs_install').MLjquiWindow('close');
					
					ML4WebApi.getCsManager().sendURLScheme(function(code, obj){
						var installType = obj.type;
						if(code == 0){
							if(obj.csUpdateChk == "upgrade"){ // update
								openCSUpdateDialog(mode);
							}else{
								var timer = setInterval(function(){
									if(code==0){
										//스토리지 목록 재로드.
										ML4WebApi.setProperty("is_cs_install", true);
										var default_stg = ML4WebApi.getProperty("defaultStorage");
										var dialogMode = mode;
										
										ML4WebDraw.MakeStorageListDiv(dialogMode);
										
										if(dialogMode == "main"){
											ML4WebDraw.initWebMainEvent();
											
											if(default_stg!=null && default_stg != ""){
												$("#stg_"+default_stg).click();
											}else{
												ML4WebDraw.MakeCertiListDiv(null);
											}
										}else if(dialogMode == "mgmt"){
											initAdminMainEvent();
											
											if(default_stg!=null && default_stg != ""){
												$("#stg_admin_"+default_stg).click();
											}else{
												makeAdminCertiListDiv(null);
											}
										}
										
										clearInterval(timer);
									}
								}, 1000);
							}
						}else{
							openCSInstallDialog(mode);
						}
					});
				},
				releaseDialog : function(callback) {
					//TODO
					try{
						$('#ML_Dialog_common').MLjquiWindow('close');
						
						if (typeof(onclick) == "function") {
							onclick(certData);
						} else {
							console.log("releaseDialog error... onclick is not function");
						}	
					}catch(e){
						console.log("releaseDialog error..."+e.message);
					}
					
				},
				setOptions: function(obj) {
					mode = obj.mode;
					dialog_title = (obj.title == undefined ? "" : obj.title);
					showCert = obj.showCert;
					certData = obj.certData;
					onclick = obj.onclick;
					contentKey = obj.contentKey;
					selectStg = obj.selectStg;
					message = (obj.message == undefined ? "" : obj.message);
					content = getContentHtml(obj.contentKey);
					dialogHeight = obj.dialogHeight;
				},
				enterKeyEvent: function(e) {
					if(e.keyCode == 13){ 
						this.releaseDialog();
					}else{ 
						e.keyCode == 0; 
						return; 
					} 
				}
			};

			function getContentHtml(key) {
				var htmlTxt = '';
				switch(key){
					case 'confirm' : 
						htmlTxt += '<div class="ML_pw_dialog_status_area">';
						htmlTxt += '	<div class="ML_pw_dialog_area">';
						htmlTxt += '		<div id="confirmMessage" class="ML_pw_dialog_txt">' + message + '</div>';
						htmlTxt += '	</div>';
						htmlTxt += '</div>';
						break;
					case 'confirm_pw' :
						htmlTxt += '<div class="ML_sub_tit">선택한 인증서</div>';
						htmlTxt += '<div id="ML_select_cert_info" class="ML_cert_list_sign_area">';
						htmlTxt += '	<div class="ML_cert_list">';

						var isExpired = ML4WebUtil.isDateExpired(certData.enddatetime);
					if(isExpired){
						htmlTxt += '		<div class="ML_cert_exp"></div>';
					}else{
						htmlTxt += '		<div class="ML_cert_nor"></div>';
					}
						
					if(showCert){
						htmlTxt += '		<div class="ML_cert_txt">';
						htmlTxt += '			<span class="certi_name">'+certData.realname+'</span>';
						htmlTxt += '			<span class="certi_issuOrg">'+certData.Policy+' | '+certData.Issuer+'</span>';
						htmlTxt += '			<span class="certi_expDate">만료일 : '+certData.enddate+'</span>';
						htmlTxt += '		</div>';
					}
						htmlTxt += '	</div>';
						htmlTxt += '</div>';
						htmlTxt += '<div class="ML_pw_popup_area">';
						htmlTxt += '	<div class="ML_cert_pw_area">';
						htmlTxt += '		<span class="ML_cert_popup_tit">비밀번호:</span>';
						htmlTxt += '		<span class="ML_cert_pw_field f_wh_255">';
						htmlTxt += '			<input type="password" id="input_pw_confirm" name="input_pw_confirm" class="passwd_input" style="ime-mode:disabled;" placeholder="" autocomplete="off" onKeypress="DSDialog.enterKeyEvent(event)" />';
						htmlTxt += '		</span>';
						htmlTxt += '	</div>';
						htmlTxt += '</div>';
						break;
					case 'change_pw' :
						htmlTxt += '<div class="ML_sub_tit">선택한 인증서</div>';
						htmlTxt += '<div id="ML_select_cert_info" class="ML_cert_list_sign_area">';
						htmlTxt += '	<div class="ML_cert_list">';
						var isExpired = ML4WebUtil.isDateExpired(certData.enddatetime);
					if(isExpired){
						htmlTxt += '		<div class="ML_cert_exp"></div>';
					}else{
						htmlTxt += '		<div class="ML_cert_nor"></div>';
					}
					if(showCert){
						htmlTxt += '		<div class="ML_cert_txt">';
						htmlTxt += '			<span class="certi_name">'+certData.realname+'</span>';
						htmlTxt += '			<span class="certi_issuOrg">'+certData.Policy+' | '+certData.Issuer+'</span>';
						htmlTxt += '			<span class="certi_expDate">만료일 : '+certData.enddate+'</span>';
						htmlTxt += '		</div>';
					}
						htmlTxt += '	</div>';
						htmlTxt += '</div>';
						htmlTxt += '<div class="ML_pw_popup_area" style="padding:11px 0 0 0;">';
						htmlTxt += '	<div class="ML_cert_pw_area">';
						htmlTxt += '		<span class="ML_cert_popup_tit">이전 비밀번호 :</span>';
						htmlTxt += '		<span class="ML_cert_pw_field f_wh_255">';
						htmlTxt += '			<input type="password" id="input_pw_prev" name="input_pw_prev" class="passwd_input" style="ime-mode:disabled;" placeholder="" autocomplete="off" />';
						htmlTxt += '		</span>';
						htmlTxt += '	</div>';
						htmlTxt += '	<div class="ML_cert_pw_area">';
						htmlTxt += '		<span class="ML_cert_popup_tit">새 비밀번호 :</span>';
						htmlTxt += '		<span class="ML_cert_pw_field f_wh_255">';
						htmlTxt += '			<input type="password" id="input_pw_new1" name="input_pw_new1" class="passwd_input" style="ime-mode:disabled;" placeholder="" autocomplete="off" />';
						htmlTxt += '		</span>';
						htmlTxt += '	</div>';
						htmlTxt += '	<div class="ML_cert_pw_area">';
						htmlTxt += '		<span class="ML_cert_popup_tit">새 비밀번호 확인 :</span>';
						htmlTxt += '		<span class="ML_cert_pw_field f_wh_255">';
						htmlTxt += '			<input type="password" id="input_pw_new2" name="input_pw_new2" class="passwd_input" style="ime-mode:disabled;" placeholder="" autocomplete="off" />';
						htmlTxt += '		</span>';
						htmlTxt += '	</div>';
						htmlTxt += '</div>';
						break;
					case 'select_browser' :
						htmlTxt += '<div class="ML_sub_tit">선택한 인증서</div>';
						//htmlTxt += '	<input type="hidden" id="copy_select_storage" name="copy_select_storage">';
						htmlTxt += '	<div class="ML_cert_list_sign_area" id="ML_certlist_area">';
						htmlTxt += '		<div class="ML_cert_list">';
						var isExpired = ML4WebUtil.isDateExpired(certData.enddatetime);
					if(isExpired){
						htmlTxt += '			<div class="ML_cert_exp"></div>';
					}else{
						htmlTxt += '			<div class="ML_cert_nor"></div>';
					}
					if(showCert){
						htmlTxt += '			<div class="ML_cert_txt">';
						htmlTxt += '				<span class="certi_name">'+certData.Cn+'</span>';
						htmlTxt += '				<span class="certi_issuOrg">'+certData.Policy+' | '+certData.Issuer+'</span>';
						htmlTxt += '				<span class="certi_expDate">만료일 : '+certData.enddate+'</span>';
						htmlTxt += '			</div>';
					}
						htmlTxt += '		</div>';
						htmlTxt += '	</div>';
						htmlTxt += '	<div class="ML_pw_popup_area" style="padding:11px 0 10px 0;">';
						htmlTxt += '	<div class="ML_cert_pw_area">';
						htmlTxt += '		<span class="ML_cert_popup_tit_copy">비밀번호</span>';
						htmlTxt += '		<span class="ML_cert_pw_field f_wh_255">';
						htmlTxt += '			<input type="password" id="input_pw_confirm" name="input_pw_confirm" class="passwd_input" style="ime-mode:disabled;" placeholder="" autocomplete="off" onKeypress="DSDialog.enterKeyEvent(event)" onClick="clickpwd();" data-tk-kbdType="qwerty" npkencrypt="on" data-keypad-type="alpha" data-keypad-theme="default" data-keypad-useyn-type="toggle"/>';
						htmlTxt += '		</span>';
						htmlTxt += '	</div>';
						htmlTxt += '	</div>';
						break;
					case 'select_storage' :
						htmlTxt += '<div class="ML_sub_tit">선택한 인증서</div>';
						//htmlTxt += '	<input type="hidden" id="copy_select_storage" name="copy_select_storage">';
						htmlTxt += '	<div class="ML_cert_list_sign_area" id="ML_certlist_area">';
						htmlTxt += '		<div class="ML_cert_list">';
						
						var isExpired = ML4WebUtil.isDateExpired(certData.enddatetime);
					if(isExpired){
						htmlTxt += '			<div class="ML_cert_exp"></div>';
					}else{
						htmlTxt += '			<div class="ML_cert_nor"></div>';
					}
					if(showCert){
						htmlTxt += '		<div class="ML_cert_txt">';
						htmlTxt += '			<span class="certi_name">'+certData.Cn+'</span>';
						htmlTxt += '			<span class="certi_issuOrg">'+certData.Policy+' | '+certData.Issuer+'</span>';
						htmlTxt += '			<span class="certi_expDate">만료일 : '+certData.enddate+'</span>';
						htmlTxt += '		</div>';
					}
						htmlTxt += '		  </div>';
						htmlTxt += '	</div>';
						htmlTxt += '	<div class="ML_sub_tit">인증서를 저장할 매체를 선택 해주세요</div>';
						htmlTxt += '	<div class="ML_pw_popup_area">';
						htmlTxt += '		<div class="ML_iestorage_box">';
						htmlTxt += '			<div class="ML_iestorage_area">';
						htmlTxt += '				<ul>';

					var saveStorageList = ML4WebApi.getProperty("saveStorageList");
					for(var i=0 ; i<saveStorageList.length  ; i++){
						switch(saveStorageList[i]){
							case 'web' :
								//htmlTxt += '					<li class="ML_iestorage_browser"><a href="javascript:$(\'#copy_select_storage\').val(\'web\');"><span>브라우저</span></a></li>';
								htmlTxt += '					<li class="ML_iestorage_web"><a href="javascript:selectToStorage(\'web\');"><span>브라우저</span></a></li>';
								break;
							case 'hdd' :
								//htmlTxt += '					<li class="ML_iestorage_pc"><a href="javascript:$(\'#copy_select_storage\').val(\'hdd\');"><span>HDD</span></a></li>';
								htmlTxt += '					<li class="ML_iestorage_hdd"><a href="javascript:selectToStorage(\'hdd\');"><span>HDD</span></a></li>';
								break;
							case 'shdd' :
								htmlTxt += '					<li class="ML_iestorage_shdd"><a href="javascript:selectToStorage(\'shdd\');"><span>안전디스크</span></a></li>';
								break;
							case 'token' :
								//htmlTxt += '					<li class="ML_iestorage_token"><a href="javascript:$(\'#copy_select_storage\').val(\'token\');"><span>보안토큰</span></a></li>';
								htmlTxt += '					<li class="ML_iestorage_token"><a href="javascript:selectToStorage(\'token\');"><span>보안토큰</span></a></li>';
								break;
							case 'mobile' :
								//htmlTxt += '					<li class="ML_iestorage_phone"><a href="javascript:$(\'#copy_select_storage\').val(\'mobile\');"><span>휴대폰</span></a></li>';
								htmlTxt += '					<li class="ML_iestorage_phone"><a href="javascript:selectToStorage(\'mobile\');"><span>휴대폰</span></a></li>';
								break;
							case 'smartcert' :
								//htmlTxt += '					<li class="ML_iestorage_smartcert"><a href="javascript:$(\'#copy_select_storage\').val(\'smartcert\');"><span>스마트인증</span></a></li>';
								htmlTxt += '					<li class="ML_iestorage_smartcert"><a href="javascript:selectToStorage(\'smartcert\');"><span>스마트인증</span></a></li>';
								break;
							case 'cloud' :
								//
								break;
						}
						//htmlTxt += '					<li class="ML_iestorage_hdd"><a href="#"><span>로컬디스크</span></a></li>';
						//htmlTxt += '					<li class="ML_iestorage_usb"><a href="#"><span>USB</span></a></li>';
						//htmlTxt += '					<li class="ML_iestorage_sd"><a href="#"><span>SD Card</span></a></li>';
					}
						htmlTxt += '				</ul>';
						htmlTxt += '			</div>';
						htmlTxt += '			<div id="subStorageDriver" class="drive_position iepos_01"  style="display:none;">';
//						htmlTxt += '				<ul class="drive_position_menu">';
//						htmlTxt += '					<li><a href="#">C:\</a></li>';
//						htmlTxt += '					<li><a href="#">D:\</a></li>';
//						htmlTxt += '					<li><a href="#">E:\</a></li>';
//						htmlTxt += '					<li><a href="#">F:\</a></li>';
//						htmlTxt += '				</ul>';
						htmlTxt += '			</div>';
					
						htmlTxt += '		</div>';
						htmlTxt += '	</div>';
						//htmlTxt += '	<div class="ML_sub_tit" id="selectedStg">선택한 매체 : </div>';
						/*htmlTxt += '	<div class="ML_pw_popup_area" style="padding:11px 0 10px 0;">';
						htmlTxt += '	<div class="ML_cert_pw_area">';
						htmlTxt += '		<span class="ML_cert_popup_tit_copy">비밀번호</span>';
						htmlTxt += '		<span class="ML_cert_pw_field f_wh_255">';
						htmlTxt += '			<input type="password" id="input_pw_confirm" name="input_pw_confirm" class="passwd_input" style="ime-mode:disabled;" placeholder="" autocomplete="off" onKeypress="DSDialog.enterKeyEvent(event)" onClick="clickpwd();" data-tk-kbdType="qwerty" npkencrypt="on" data-keypad-type="alpha" data-keypad-theme="default" data-keypad-useyn-type="toggle"/>';
						htmlTxt += '		</span>';
						htmlTxt += '	</div>';
						htmlTxt += '	</div>';*/
						break;
					case 'import_pfx' :
						if(mode=='main'){
						htmlTxt += '<div class="ML_pw_pfx_status_area" style="height:200px;" ondrop="DragAndDropCert(event)" ondragover="handleDragOver(event)">';
						}else{
						htmlTxt += '<div class="ML_pw_pfx_status_area" style="height:115px;" ondrop="DragAndDropCert(event)" ondragover="handleDragOver(event)">';
						}
						htmlTxt += '	<div class="ML_pw_pfx_area">';
						htmlTxt += '		<div class="pfx_info">';
						htmlTxt += '			<span>※ 인증서 파일(*.pfx, *.p12)을 선택해야 브라우저 인증서를</span>';
						htmlTxt += '			<span><br>사용할 수 있습니다.</span>';
						htmlTxt += '		</div>';
						htmlTxt += '		<div class="ML_cert_file_area2">';
						htmlTxt += '			<span class="ML_cert_pw_tit_pfx">인증서파일 경로</span>';
//						htmlTxt += '			<span class="ML_cert_pw_field f_wh_224">';
						htmlTxt += '			<span class="ML_cert_pw_field f_wh_259">';
						htmlTxt += '				<input type="text" id="file_route" class="passwd_input" style="ime-mode:active;" placeholder="" readonly autocomplete="off">';
						htmlTxt += '			</span>';
//						htmlTxt += '			<span id="span_filefile" class="ML_cert_file_btn_fcs">';
//						htmlTxt += '				<input type="file" id="filefile" accept=".pfx,.p12" class="ML_cert_file_add" onChange="javascript:document.getElementById(\'file_route\').value=this.value" onFocus="focusPfxFileObj(\'open\');" onBlur="focusPfxFileObj(\'close\');"/>';
//						htmlTxt += '			</span>';
						htmlTxt += '		</div>';
						htmlTxt += '		<div class="ML_cert_pw_area">';
						htmlTxt += '			<span class="ML_cert_pw_tit_pfx">인증서 비밀번호</span>';
						htmlTxt += '			<span class="ML_cert_pw_field f_wh_259">';
						htmlTxt += '				<input type="password" class="passwd_input" style="ime-mode:active;" placeholder="" id="import_pfx_password" name="import_pfx_password" autocomplete="off" onKeypress="DSDialog.enterKeyEvent(event)">';
						htmlTxt += '			</span>';
						htmlTxt += '		</div>';
						if(mode=='main'){
						/*htmlTxt += '		<div class="pfx_info">';
						htmlTxt += '			<span>※ PFX 파일이 없을 경우 인증서 변환 프로그램을 이용해 주십시오.</span>';
						htmlTxt += '			<span>PFX 인증서 변환 프로그램은 Windows 계열만 지원합니다.</span>';
						htmlTxt += '			<span class="pfx_btn_row_c">';
						htmlTxt += '				<p class="w_dkblue_btn">';
						htmlTxt += '					<button type="button" name="okBtn" id="okBtn" onClick="pfxExportdownload();">';
						htmlTxt += '						<span>PFX 인증서 변환 프로그램 다운로드</span>';
						htmlTxt += '					</button>';
						htmlTxt += '				</p>';
						htmlTxt += '			</span>';
						htmlTxt += '		</div>';*/
						htmlTxt += '		<div class="pfx_br_chk">';
						htmlTxt += '			<label id="chkLabel" for="pfx_save_yn">';
						htmlTxt += '				<input type="checkbox" id="pfx_save_yn" name="pfx_save_yn" checked><span class="w_space5">브라우저에 인증서 저장</span>';
						htmlTxt += '			</label>';
						htmlTxt += '		</div>';
						}
						htmlTxt += '	</div>';
						htmlTxt += '</div>';
						break;
					case 'import_pfx_m' :
						htmlTxt += '<div class="ML_pw_pfx_status_area" style="height:115px;">';
						htmlTxt += '	<div class="ML_pw_pfx_area">';
						htmlTxt += '		<div class="ML_cert_file_area">';
						htmlTxt += '			<span class="ML_cert_pw_tit_pfx">파일선택 :</span>';
						htmlTxt += '			<span class="ML_cert_pw_field f_wh_per_55">';
						htmlTxt += '				<input type="text" id="file_route" class="passwd_input" style="ime-mode:active;" placeholder="" readonly autocomplete="off">';
						htmlTxt += '			</span>';
						htmlTxt += '			<span class="ML_cert_file_btn">';
						//htmlTxt += '				<button type="button" id="btn_alert_confirm" class="ML_cert_file_add" onClick="importPfxM();"><span>확인</span></button></p>';
						htmlTxt += '				<input type="file" id="filefile" accept=".pfx,.p12" class="ML_cert_file_add" onChange="javascript:document.getElementById(\'file_route\').value=this.value">';
						htmlTxt += '			</span>';
						htmlTxt += '		</div>';
						htmlTxt += '		<div class="ML_cert_pw_area">';
						htmlTxt += '			<span class="ML_cert_pw_tit">인증서 비밀번호 :</span>';
						htmlTxt += '			<span class="ML_cert_pw_field f_wh_per_60">';
						htmlTxt += '				<input type="password" class="passwd_input" style="ime-mode:disabled;" placeholder="" id="import_pfx_password" name="import_pfx_password" autocomplete="off" onKeypress="DSDialog.enterKeyEvent(event)">';
						htmlTxt += '			</span>';
						htmlTxt += '		</div>';
						if(mode=='main'){
						htmlTxt += '		<div style="text-align:right;">';
						htmlTxt += '			<span><input type="checkbox" id="pfx_save_yn" checked />브라우저에 인증서 저장</span>';
						htmlTxt += '		</div>';
						}
						htmlTxt += '	</div>';
						htmlTxt += '</div>';
						break;
					case 'import_pfx_t' :
						htmlTxt += '<div class="ML_pw_pfx_status_area" style="height:200px;">';
						htmlTxt += '	<div class="ML_pw_pfx_area">';
						htmlTxt += '		<div class="ML_cert_file_area">';
						htmlTxt += '			<span class="ML_cert_pw_tit_pfx">파일선택 :</span>';
						htmlTxt += '			<span class="ML_cert_pw_field f_wh_per_310">';
						htmlTxt += '				<input type="text" id="file_route" class="passwd_input" style="ime-mode:active;" placeholder="" readonly autocomplete="off">';
						htmlTxt += '			</span>';
						htmlTxt += '			<span class="ML_cert_file_btn">';
						htmlTxt += '				<input type="file" id="filefile" accept=".pfx,.p12" class="ML_cert_file_add" onChange="javascript:document.getElementById(\'file_route\').value=this.value">';
						htmlTxt += '			</span>';
						htmlTxt += '		</div>';
						htmlTxt += '		<div class="ML_cert_pw_area">';
						htmlTxt += '			<span class="ML_cert_pw_tit">인증서 비밀번호 :</span>';
						htmlTxt += '			<span class="ML_cert_pw_field f_wh_per_60">';
						htmlTxt += '				<input type="password" class="passwd_input" style="ime-mode:disabled;" placeholder="" id="import_pfx_password" name="import_pfx_password" autocomplete="off" onKeypress="DSDialog.enterKeyEvent(event)">';
						htmlTxt += '			</span>';
						htmlTxt += '		</div>';
						if(mode=='main'){
							htmlTxt += '		<div style="text-align:right;">';
							htmlTxt += '			<span><input type="checkbox" id="pfx_save_yn" checked />브라우저에 인증서 저장</span>';
							htmlTxt += '		</div>';
						}
						htmlTxt += '	</div>';
						htmlTxt += '</div>';
						break;
					case 'import_pfx_c' :
						htmlTxt += '<div class="ML_pw_pfx_status_area">';
						htmlTxt += '	<div class="ML_pw_pfx_area">';
						htmlTxt += '		<div class="ML_cert_file_area">';
						htmlTxt += '			<span class="ML_cert_pw_tit_pfx">파일선택 :</span>';
						htmlTxt += '			<span class="ML_cert_pw_field f_wh_210">';
						htmlTxt += '				<input type="text" id="file_route" class="passwd_input" style="ime-mode:active;" placeholder="" readonly autocomplete="off">';
						htmlTxt += '			</span>';
						//htmlTxt += '			<span class="ML_cert_file_btn"><input type="file" id="filefile" accept=".pfx" class="ML_cert_file_add" onChange="javascript:document.getElementById(\'file_route\').value=this.value"></span>';
						htmlTxt += '			<span class="ML_cert_file_btn" onClick="openCSFilePicker();"></span>';
						htmlTxt += '		</div>';
						htmlTxt += '		<div class="ML_cert_pw_area">';
						htmlTxt += '			<span class="ML_cert_pw_tit">인증서 비밀번호 :</span>';
						htmlTxt += '			<span class="ML_cert_pw_field f_wh_255">';
						htmlTxt += '				<input type="password" class="passwd_input" style="ime-mode:disabled;" placeholder="" id="import_pfx_password" name="import_pfx_password" autocomplete="off" onKeypress="DSDialog.enterKeyEvent(event)">';
						htmlTxt += '			</span>';
						htmlTxt += '		</div>';
						htmlTxt += '	</div>';
						htmlTxt += '</div>';
						break;
					case 'copy_token' :
						htmlTxt += '<div class="ML_sub_tit">선택한 인증서</div>';
						htmlTxt += '<div id="ML_select_cert_info" class="ML_cert_list_sign_area">';
						htmlTxt += '	<div class="ML_cert_list">';
						var isExpired = ML4WebUtil.isDateExpired(certData.enddatetime);
					if(isExpired){
						htmlTxt += '		<div class="ML_cert_exp"></div>';
					}else{
						htmlTxt += '		<div class="ML_cert_nor"></div>';
					}
					if(showCert){
						htmlTxt += '		<div class="ML_cert_txt">';
						htmlTxt += '			<span class="certi_name">'+certData.realname+'</span>';
						htmlTxt += '			<span class="certi_issuOrg">'+certData.Policy+' | '+certData.Issuer+'</span>';
						htmlTxt += '			<span class="certi_expDate">만료일 : '+certData.enddate+'</span>';
						htmlTxt += '		</div>';
					}
						htmlTxt += '	</div>';
						htmlTxt += '</div>';
						htmlTxt += '<div class="ML_pw_popup_area" style="padding:11px 0 0 0;">';
						htmlTxt += '	<div class="ML_cert_pw_area">';
						htmlTxt += '		<span class="ML_cert_popup_tit">보안토큰 비밀번호:</span>';
						htmlTxt += '		<span class="ML_cert_pw_field f_wh_255">';
						htmlTxt += '			<input type="password" id="input_token_pw_confirm" name="input_token_pw_confirm" class="passwd_input" style="ime-mode:disabled;" placeholder="" autocomplete="off" />';
						htmlTxt += '		</span>';
						htmlTxt += '	</div>';
						htmlTxt += '	<div class="ML_cert_pw_area">';
						htmlTxt += '		<span class="ML_cert_popup_tit">인증서 비밀번호:</span>';
						htmlTxt += '		<span class="ML_cert_pw_field f_wh_255">';
						htmlTxt += '			<input type="password" id="input_pw_confirm" name="input_pw_confirm" class="passwd_input" style="ime-mode:disabled;" placeholder="" autocomplete="off" />';
						htmlTxt += '		</span>';
						htmlTxt += '	</div>';
						htmlTxt += '</div>';
						break;
					case 'install_cs' : 
						var installType = ML4WebApi.getProperty("cs_install_type")
						
						htmlTxt += '<div class="install_wrap">';
						htmlTxt += '	<img src="UI/images/setup_content.png" alt="아이콘"/>';
						//client 서비스로 변경하면서 '실행' 버튼을 없애기 위해 ie만 다르게 작동하던 것을 다 똑같은 alert이 나오도록 수정
						//if(chkBrowser.indexOf("Firefox") > -1 || chkBrowser.indexOf("Chrome") > -1 || chkBrowser.indexOf("Opera") > -1){
							if(installType == "download"){
								htmlTxt += '	<span class="install_txt">'+$.i18n.prop("ES046")+'</span>';
							}else{
								htmlTxt += '	<span class="install_txt">'+$.i18n.prop("ES047")+'</span>';
							}
						/*}else{
							htmlTxt += '	<span class="install_txt">MagicLine 프로그램이 설치되어 있지 않은 경우 \'설치\' 버튼을 눌러 주시고,<br>설치가 되어있을 경우 \'실행\' 버튼을 눌러주세요.</span>';
						}*/
						/*if(installType == "download"){
							htmlTxt += '	<span class="install_txt">MagicLine 프로그램을 설치합니다.</span>';
						}else{
							htmlTxt += '	<span class="install_txt">MagicLine 설치 페이지로 이동합니다.</span>';
						}*/
						htmlTxt += '	<div id="jqxLoader"></div>';
						htmlTxt += '</div>';

/*						htmlTxt += '<div class="ML_sub_tit">MagicLine NPIZ 프로그램을 설치하시겠습니까?</div>';
						htmlTxt += '<div class="ML_pw_popup_area" style="padding:11px 0 0 0;">';
						htmlTxt += '	<div class="ML_cert_pw_area">';
						htmlTxt += '		<div id="jqxLoader"></div>';
						//htmlTxt += '		<span class="ML_cert_popup_tit">비밀번호:</span>';
						//htmlTxt += '		<span class="ML_cert_pw_field f_wh_255">';
						//htmlTxt += '			<input type="password" id="input_pw_confirm" name="input_pw_confirm" class="passwd_input" style="ime-mode:active;" placeholder="" autocomplete="off" />';
						//htmlTxt += '		</span>';
						htmlTxt += '	</div>';
						htmlTxt += '</div>';*/
						break;
					case 'install_update' : 
						htmlTxt += '<div class="install_wrap">';
						htmlTxt += '	<img src="UI/images/setup_content2.png" alt="아이콘"/>';
						if(ML4WebApi.getProperty("libType") == "0"){
							htmlTxt += '	<span class="install_txt">' + $.i18n.prop("ES048") + '</span>';
						}else{
							htmlTxt += '	<span class="install_txt">' + $.i18n.prop("ES048") + $.i18n.prop("ES049") + '</span>';
						}
						htmlTxt += '	<div id="jqxLoader"></div>';
						htmlTxt += '</div>';
						break;
					case 'confirm_txt' : 
						var confirm_txt = '선택된 인증서를 삭제하시겠습니까?';
						htmlTxt += '<div class="ML_pw_dialog_status_area">';
						htmlTxt += '	<div class="ML_pw_dialog_area">';
						htmlTxt += '		<div id="alert_msg" class="ML_pw_dialog_txt">' +confirm_txt+ '</div>';
/*						htmlTxt += '		<span class="btn_row">';
						htmlTxt += '			<p class="b_blue_btn"><button type="button" name="btn_confirm" id="btn_alert_confirm" onClick="closeAlertDialog();"><span>확인</span></button></p>';
						htmlTxt += '				<span class="w_space40"></span>';
						htmlTxt += '			<p class="b_grey_btn"><button type="button" onClick="selfClose();"><span>취소</span></button></p>';
						htmlTxt += '		</span>';*/
						htmlTxt += '	</div>';
						htmlTxt += '</div>';
						break;
					case 'confirm_txt_token' : 
						var confirm_txt = '선택된 인증서를 삭제하시겠습니까?';
						htmlTxt += '<div class="ML_pw_dialog_status_area">';
						htmlTxt += '	<div class="ML_pw_dialog_area">';
						htmlTxt += '		<div id="alert_msg" class="ML_pw_dialog_txt">' +confirm_txt+ '</div>';
						htmlTxt += '		<div class="ML_cert_pw_area">';
						htmlTxt += '			<span class="ML_cert_popup_tit">보안토큰 비밀번호:</span>';
						htmlTxt += '			<span class="ML_cert_pw_field f_wh_210">';
						htmlTxt += '				<input type="password" id="input_token_pw_confirm" name="input_token_pw_confirm" class="passwd_input" style="ime-mode:disabled;" placeholder="" autocomplete="off" />';
						htmlTxt += '			</span>';
						htmlTxt += '		</div>';
						htmlTxt += '	</div>';
						htmlTxt += '</div>';
						break;
					case 'add_browser' : 
						htmlTxt += '<div class="ML_pw_pfx_status_area" style="height:340px;" ondrop="DragAndDropCert(event)" ondragover="handleDragOver(event)">';
						htmlTxt += '	<div class="ML_pw_pfx_area">';
						htmlTxt += '		<div class="pfx_info" style="text-align:left">';
						htmlTxt += '			<img style="margin-left: -10px;" src="UI/images/browser_user_method.png" alt="인증서파일, 키파일 선택 안내"/>';
//						htmlTxt += '			<span>※ 인증서 파일(*.pfx, *.p12, signCert.cer/signPri.key)을</span><br>';
//						htmlTxt += '			<span>선택해야 브라우저 인증서를 사용할 수 있습니다.</span><br>';
//						htmlTxt += '			<span>※ 아래 2가지 방법 중 한 가지를 이용하여 인증서를 선택</span><br>';
//						htmlTxt += '			<span>한 후 비밀번호를 입력하시기 바랍니다.</span><br>';
//						htmlTxt += '			<span>①인증서 파일을 끌어다 놓거나</span><br>';
//						htmlTxt += '			<span>②열기버튼으로 인증서 파일을 직접 선택</span><br>';
//						htmlTxt += '			<span>※ 인증서 파일을 끌어다 놓으면 브라우저에 인증서 저장</span><br>';
//						htmlTxt += '			<span>및 사용이 가능합니다.</span><br>';
						htmlTxt += '		</div>';
//						htmlTxt += '		<span id="sub_title" class="ML_cert_pw_tit_pfx" style="width:100%; margin:10px 0 0;">선택된 인증서</span>';
						htmlTxt += '		<div style="width:360px;height:81px;margin:2px 0 5px;" id="drag_info_img">';
						htmlTxt += '			<img src="UI/images/draganddrop_area.png" alt="인증서파일 끌어놓기 안내" />';
						htmlTxt += '		</div>';
						htmlTxt += '		<div class="ML_cert_orgtext_area2" style="display:none;">';
						//htmlTxt += '		<div class="ML_cert_orgtext_area2">';
						htmlTxt += '			<span id="sub_title" class="ML_cert_pw_tit_pfx" style="width:100%;">'+$.i18n.prop("TS071")+'</span>';
						htmlTxt += '			<div class="ML_cert_inner_area02">';
						htmlTxt += '			<div class="detail_text02">';
//						 .cer / .der 인증서 일 경우
						htmlTxt += '				<table id="cert_der" class="inner_tb">';
						htmlTxt += '					<tbody>';
						htmlTxt += '						<tr><th class="inner_head">'+$.i18n.prop("TS059")+'</th><th class="inner_head">'+$.i18n.prop("TS061")+'</th><th class="inner_head">'+$.i18n.prop("TS062")+'</th></tr>';
						htmlTxt += '						<tr><td id="file_route"></td><td id="file_route_issuer"></td><td id="file_route_enddate"></td></tr>';
//						htmlTxt += '						<tr><td></td><td></td><td></td></tr>';
//						htmlTxt += '						<tr><td></td><td></td><td></td></tr>';
//						htmlTxt += '						<tr><td></td><td ></td><td></td></tr>';
						htmlTxt += '					</tbody>';
						htmlTxt += '				</table>';
//						.pfx, .p12인증서 일 경우
						htmlTxt += '				<table id="cert_pfx" class="inner_tb_pfx" style="display:none;">';
						htmlTxt += '					<tbody>';
						htmlTxt += '						<tr><th class="inner_head">'+$.i18n.prop("TS072")+'</th><th class="inner_head">'+$.i18n.prop("TS067")+'</th></tr>';
						htmlTxt += '						<tr><td id="pfx_type"></td><td id="pfx_file"></td></tr>';
//						htmlTxt += '						<tr><td></td><td></td></tr>';
//						htmlTxt += '						<tr><td></td><td></td></tr>';
//						htmlTxt += '						<tr><td></td><td></td></tr>';
						htmlTxt += '					</tbody>';
						htmlTxt += '				</table>';
						htmlTxt += '			</div>';
						htmlTxt += '			</div>';
						htmlTxt += '		</div>';
						htmlTxt += '		<div class="ML_cert_file_area">';
						htmlTxt += '			<span class="ML_cert_pw_tit_pfx">'+$.i18n.prop("TS067")+'</span>';
						htmlTxt += '			<span class="ML_cert_pw_field f_wh_224">';
						htmlTxt += '				<input type="text" id="file_route2" class="passwd_input" style="ime-mode:active;" placeholder="" readonly autocomplete="off" title="'+$.i18n.prop("TS067")+'">';
						htmlTxt += '			</span>';
						htmlTxt += '			<span id="span_filefile" class="ML_cert_file_btn">';
						htmlTxt += '				<input type="file" id="filefile2" accept=".cer,.der,.key,.pfx,.p12" multiple="multiple" class="ML_cert_file_add" onFocus="focusPfxFileObj(\'open\');" onBlur="focusPfxFileObj(\'close\');" tabindex="0" title="파일찾기" />';
						htmlTxt += '			</span>';
						htmlTxt += '		</div>';
						htmlTxt += '		<div class="ML_cert_pw_area">';
						htmlTxt += '			<span class="ML_cert_pw_tit_pfx">'+$.i18n.prop("TS068")+'</span>';
						htmlTxt += '			<span class="ML_cert_pw_field f_wh_259">';
						/*htmlTxt += '				<input type="password" class="passwd_input"  placeholder="" id="add_browser_password" name="add_browser_password" autocomplete="off" onKeypress="DSDialog.enterKeyEvent(event)">';*/
						/*htmlTxt += '				<input type="password" class="passwd_input" title="'+$.i18n.prop("TS038")+'"  placeholder="'+$.i18n.prop("ES042")+'" id="add_browser_password" name="add_browser_password" onClick="initKeyPad();" data-tk-kbdType="qwerty" autocomplete="off" value="" onKeypress="DSDialog.enterKeyEvent(event)" npkencrypt="on" data-keypad-type="alpha" data-keypad-theme="default" data-keypad-useyn-type="toggle" data-keypad-useyn-input="addbrowserKeyboardOn">';*/
						htmlTxt += '				<input type="password" class="passwd_input" title="'+$.i18n.prop("TS038")+'"  placeholder="'+$.i18n.prop("ES042")+'" id="add_browser_password" name="add_browser_password" onClick="initKeyPad();" ds-kb-type="qwerty" ds-kb-focus="check" ds-kb-xy="[-100, 0]" data-tk-kbdType="qwerty" autocomplete="off" value="" onKeypress="DSDialog.enterKeyEvent(event)" npkencrypt="on" data-keypad-type="alpha" data-keypad-theme="default" data-keypad-useyn-type="toggle" data-keypad-useyn-input="addbrowserKeyboardOn">';
						htmlTxt += '			</span>';
						htmlTxt += '			<span class="ML_cert_pw_key_box"><a href="#" id="addbrowserKeyboardOn" title="보안키보드" tabindex="0"><div class="ML_cert_pw_keypad" onClick="keypadOn();"></div></a></span>';
						htmlTxt += '			<span id="add_capslock" class="add_capslock_box">＜Caps Lock＞이 켜져 있습니다.</span>';
						htmlTxt += '		</div>';
						htmlTxt += '		<div class="save_to_br_chk">';
						htmlTxt += '			<label class="blind" id="chkLabel" for="browser_save_yn">브라우저에 인증서 저장여부 체크</label>';
						//브라우저인증서저장 버튼 활성화/비활성화 flag
						if(selectStg == "web" || selectStg == "web_kftc"){
							htmlTxt += '				<input type="checkbox" id="browser_save_yn" name="browser_save_yn" checked/><span class="w_space5">'+$.i18n.prop("TS069")+'</span>';
						}else{
							htmlTxt += '				<input type="checkbox" id="browser_save_yn" name="browser_save_yn" /><span class="w_space5">'+$.i18n.prop("TS069")+'</span>';
						}
						/*htmlTxt += '				<select id="save_cert">';
						if(selectStg == "web_kftc"){
							if(!(chkBrowser.indexOf("IE") > -1 && chkBrowser.split(" ")[1] < 11)){
								htmlTxt += '			<option value="saveKFTC">공동인증서비스에 인증서 저장</option>';
							}
						}
						htmlTxt += '					<option value="saveBROWSER">브라우저에 인증서 저장</option>';
						htmlTxt += '					<option value="saveNO">저장 안함</option>';
						htmlTxt += '				</select>';
						htmlTxt += '			</label>';*/
						htmlTxt += '		</div>';
						htmlTxt += '	</div>';
						htmlTxt += '</div>';
						break;
					case 'check_cert_expire' : 
						htmlTxt += '<div class="ML_pw_dialog_status_area">';
						htmlTxt += '	<div class="ML_pw_dialog_area">';
						htmlTxt += '		<div id="confirmMessage" class="ML_pw_dialog_txt">' + message + '</div>';
						htmlTxt += '	</div>';
						htmlTxt += '</div>';
						break;
					
				}
				return htmlTxt;
			}

			function DS_setModule(e) {
				//console.log( "DS_setModule called...for ML4Web_Popup init." );
				return window.DSDialog = HandlePopup;
			}

			window.DS_setModule = DS_setModule();
		})(jQuery)
	}
	function alertModule() {
		(function(jQuery) {
			var alertMode		= "";
			var focusObj 		= {};
			var callbackEvent	= null;
			
			var HandleAlert = {
				init: function(e) {
//					console.log( "The public can see me!" );
				},
				openAlert : function(mode, msg, obj){
					alertMode = mode;
					focusObj = obj;
					
					$('#btn_confirm_iframe').attr('disabled', false);

					if(mode=='main'){
						$('#alert_msg').html(msg);
						if( msg.indexOf("&#60;Caps Lock&#62;") <= -1 ){
							if(msg.indexOf("클라우드") > -1){
								$('#popup_alert').MLjquiWindow({height:230});
							}else{
								$('#popup_alert').MLjquiWindow({height:200});
							}
						}else{
							$('#popup_alert').MLjquiWindow({height:240});
						}
						
						if(magiclineUtil.isMobile(magiclineUtil.getOS())){
							$('#popup_alert').MLjquiWindow({
								position: 'center'
							});
						}
						
						$('#popup_alert').MLjquiWindow('open');
						
						$('#popup_alert').focus();	//for WCA, 20200413
						//$('#btn_alert_confirm').focus();	//for WCA
					} else if(mode=='mgmt'){
						$('#mgmt_alert_msg').html(msg);
						$('#popup_mgmt_alert').MLjquiWindow('open');

						$('#popup_alert').focus();	//for WCA, 20200413
						//$('#btn_alert_confirm').focus();	//for WCA
					}
				},
				callbackAlert : function(mode, msg, obj){
					alertMode 	= mode;
					callbackEvent = obj;

					if(mode=='main'){
						$('#alert_msg').html(msg);
						if( msg.indexOf("&#60;Caps Lock&#62;") <= -1 ){
							if(msg.indexOf("클라우드") > -1){
								$('#popup_alert').MLjquiWindow({height:230});
							}else{
								$('#popup_alert').MLjquiWindow({height:200});
							}
						}else{
							$('#popup_alert').MLjquiWindow({height:240});
						}
						
						$('#popup_alert').MLjquiWindow('open');

						$('#popup_alert').focus();	//for WCA, 20200413
						//$('#btn_alert_confirm').focus();	//for WCA
					} else if(mode=='mgmt'){
						$('#mgmt_alert_msg').html(msg);
						$('#popup_mgmt_alert').MLjquiWindow('open');

						$('#popup_alert').focus();	//for WCA, 20200413
						//$('#btn_alert_confirm').focus();	//for WCA
					}
				},
				closeAlert : function (){
					if(alertMode=='main'){
						$('#popup_alert').MLjquiWindow('close');
					} else if(alertMode=='mgmt'){
						$('#popup_mgmt_alert').MLjquiWindow('close');
					}

					if(!$.isEmptyObject(focusObj)){
						focusObj.val('');
						focusObj.focus();
					}
					
					if (callbackEvent != null){
						callbackEvent("");
						callbackEvent = null;
					}
				}
			}

			function DS_setAlertModule(e) {
//				console.log( "DS_setAlertModule called...for ML4Web_Popup init." );
				return window.DSAlert = HandleAlert;
			}

			window.DS_setAlertModule = DS_setAlertModule();
		})(jQuery)
	}

	function initPopup() {
//		console.log( "initPopup called..." );

		if (typeof popupModule !== "function") return;

		if (typeof alertModule !== "function") return;

        var e = {};
        if (typeof window === "object") {
            if (typeof window.ml4popup === "object") {
                e = window.ml4popup
            } else {
                window.ml4popup = e
            }
        }
//        e.kryptos = e.kryptos || {};
        popupModule();
		alertModule();

        return e
    }

	var ml4popup = initPopup();
})();
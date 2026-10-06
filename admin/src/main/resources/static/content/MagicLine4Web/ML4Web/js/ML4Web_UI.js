(function(jQuery) {
	if (typeof jQuery === "undefined") alert("jQuery is not available!!!");

	if (typeof ML4WebUI === "undefined") {
		var _ML4WebUI = {
			api: {},
			initCallbackFunction: {},
			DSOption : {
				callback : function(){},
				option : {},
				message : "",
				certIdx : "",
				idn    : "",
				vidType : "",
				strVal : ""
			},
			mgmtDialogName : "",
			VestCertStorageOption : "HddMode",
			orignal : "서명메세지",
			funProcInit : function(){
				var params =  location.search.substring(1).split("&");
				var lgUrl = "";
				$.each(params, function(){
					var arr01 = this.split("=");
					if(arr01[0] == "lgUrl"){
						lgUrl = arr01[1];
					}
				});	
				return lgUrl;
			},
			defOption : {
				storage : "web",
				storageList : ["web","hdd","token","mobile","smartcert"] //[]
			},
			selectedStorage : {},
			Storage : {
				option : {},
				current_option : {},
				certificates: {
					certi_list : []
				},
				listeners : {},
				Add : function(obj) {
					this.certificates.certi_list.push(obj);
					this.dispatch("certi-add");	//Add event listener
				},
				AddList: function(list) {
					var listCnt = list.length;
					if(listCnt>0){
						for(var i=0 ; i<listCnt; i++){
							this.certificates.certi_list.push(list[i]);
						}
						this.dispatch("certi-add");	//Add event listener
					}
				},
				Empty : function(){
					this.certificates.certi_list = [];
				},
				Count : function(){
					return this.certificates.certi_list.length;
				},
				Get : function( idx ){
					if( idx > -1 && idx < this.certificates.certi_list.length ){
						return this.certificates.certi_list[ idx ];
					}
				},
				GetEncCertList: function( idx ){
					var certiListCnt = this.certificates.certi_list.length;
					if( this.certificates.certi_list!=null && certiListCnt>0){
						for(var i=0 ; i<certiListCnt; i++){
							var idx = this.certificates.certi_list[i].storagecertIdx;
							var rawCertIdx = {
								storageName:this.key,
								storageOpt: this.option,
								storagecertIdx : idx
							};
							this.certificates.certi_list[i].storageRawCertIdx = JSON.stringify(rawCertIdx);
						}
					}
				},
				Insert : function( obj, idx ){
					var pointer = -1;
					if( idx === 0 ){
						this.certificates.certi_list.unshift( obj );
						pointer = idx;
					}else if( idx === this.certificates.certi_list.length ){
						this.certificates.certi_list.push( obj );
						pointer = idx;
					}

					return pointer;
				},
				IndexOf : function( obj, startIndex ){
					var i = startIndex, pointer = -1;

					while( i < this.certificates.certi_list.length ){
						if( this.certificates.certi_list[i] === obj ){
							pointer = i;
						}
						i++;
					}

					return pointer;
				},
				RemoveAt: function(idx) {
					if( idx === 0 ){
						this.certificates.certi_list.shift();
					}else if( idx === this.certificates.certi_list.length -1 ){
						this.certificates.certi_list.pop();
					}
				},
				on: function(eventName, listener) {
					if(this.listeners == undefined) this.listeners = new Object();
					if(!this.listeners[eventName]) this.listeners[eventName] = [];
					this.listeners[eventName].push(listener);
				},
				dispatch: function(eventName) {
					var eventNmCnt = this.listeners[eventName].length;
					if(this.listeners[eventName]) {
						for(var i=0; i<eventNmCnt; i++) {
							this.listeners[eventName][i](this);
						}
					}
				},
				makeStorage : function(o){
					ML4WebLog.log( "ML4WebApi.makeStorage() called... ");
					var i = 0;
					for(i in Storage){
						if(Storage.hasOwnProperty(i) && typeof Storage[i] === "function"){
							o[i] = Storage[i];
						}
					}
					o.certificates = { certi_list: [] };
				}
			},
			blockUI : function (){
				$.blockUI({
					message:'<div><div><img src="' + ML4WebApi.getProperty("dirpath") + 'UI/images/loader.gif" alt="로딩중입니다."/></div><p style="display:inline-block; padding-top:4px; font-size:11px; color:#333; font-weight:bold;">'+$.i18n.prop("ES044")+'</p></div>',
					baseZ: 100005,
					css:{left:(($(window).width()/2)-75)+'px'}
					
				});
			},
			unblockUI : function(){
				$.unblockUI();
			},
			callback : "",
			showCertMana : function(viewoptObj, callback){
				ML4WebDraw.MakeStorageListDiv("mgmt");
			},
			showCertDiv : function(viewoptObj, callback){
				
				ML4WebLog.log("ML4WebUI.showCertDiv() called...");
				/*
				viewoptObj.defaultStorage;
				viewoptObj.storageList; //[]
				viewoptObj.installcheck;
				viewoptObj.updatecheck;
				viewoptObj.browserInfo;
				*/
				var defaultStg = viewoptObj.defaultStorage;
				var browserInfo = viewoptObj.browserInfo;
				browserVersion = browserInfo.version;
				
				if(viewoptObj.defaultStorage == "" || viewoptObj.defaultStorage == null){
					ML4WebLog.log("[value check] default storage value is null!! - set default option");
					viewoptObj.defaultStorage = this.defOption.storage;
					
					var checkarr = Array.isArray(viewoptObj.storageList);
					if(!checkarr){
						ML4WebLog.log("[value check] storage list is not array!! - set default option");
						viewoptObj.storageList = this.defOption.storageList;
					}
				}
				
				if((browserInfo.name.indexOf("IE") > -1 && browserVersion < 11) ||
					(browserInfo.name.indexOf("Edge") > -1 && browserVersion < 12))
				{
					if(viewoptObj.defaultStorage == "web_kftc" || viewoptObj.defaultStorage == "kftc"){
						ML4WebApi.setProperty("defaultStorage", this.defOption.storage);
						viewoptObj.defaultStorage = this.defOption.storage;
					}
					if(viewoptObj.storageList.indexOf("web_kftc") > -1 || viewoptObj.storageList.indexOf("kftc") > -1)
					{
						var idx = viewoptObj.storageList.indexOf("web_kftc");
						if(idx > -1){
							viewoptObj.storageList.splice(idx, 1, "web");
						}
					}
				}
				
				ML4WebApi.setProperty("cert_filter_expire", viewoptObj.certExpirefilter);
				ML4WebApi.setProperty("cert_filter_oid", viewoptObj.certOidfilter);
				
				if (typeof(arguments[2]) != "undefined") {
					ML4WebDraw.saveParam(arguments[2]);
				} else {
					ML4WebDraw.saveParam("");
				}
				
				Storage_API_filter.init(function(code, result){
					
					var isMobile = magiclineUtil.isMobile(magiclineUtil.getOS()); 
					
					if(isMobile){
						ML4WebDraw.MakeStorageListDiv_M(viewoptObj);
					}else{
						ML4WebDraw.MakeStorageListDiv(viewoptObj);
					}
					
					$("#ML_window").MLjquiWindow('open', function(e){
						
						if(isMobile){
							ML4WebDraw.initWebMainEvent_M();
						}else{
							ML4WebDraw.initWebMainEvent();
						}
						
						if(viewoptObj.storageList_lnb != null && viewoptObj.storageList_lnb[0] == "finance"){
							$("#ML_lnb_finance").click();
						}else if(((viewoptObj.storageList_lnb == null || viewoptObj.storageList_lnb == "") && viewoptObj.defaultStorage != null)
								|| viewoptObj.storageList_lnb[0] == "share"){
							$("#stg_" + viewoptObj.defaultStorage).click();
						}
						ML4WebUI.callback = callback;
						
					});
				})
			},
			/**
			 * 인증서 저장 기능을 수행할 UI를 호출
			 */
			showSaveCertDiv : function(viewoptObj, certbag, certInfo, callback){
				
				ML4WebLog.log("ML4WebUI.showSaveCertDiv() called...");
				/*
				viewoptObj.defaultStorage;
				viewoptObj.storageList; //[]
				viewoptObj.installcheck;
				viewoptObj.updatecheck;
				viewoptObj.browserInfo;
				*/
				var defaultStg = viewoptObj.defaultStorage;
				var browserInfo = viewoptObj.browserInfo;
				browserVersion = browserInfo.version;
				
				if(viewoptObj.defaultStorage == "" || viewoptObj.defaultStorage == null){
					ML4WebLog.log("[value check] default storage value is null!! - set default option");
					viewoptObj.defaultStorage = this.defOption.storage;
					
					var checkarr = Array.isArray(viewoptObj.storageList);
					if(!checkarr){
						ML4WebLog.log("[value check] storage list is not array!! - set default option");
						viewoptObj.storageList = this.defOption.storageList;
					}
				}
			
				if((browserInfo.name.indexOf("IE") > -1 && browserVersion < 11) ||
					(browserInfo.name.indexOf("Edge") > -1 && browserVersion < 12))
				{
					if(viewoptObj.defaultStorage == "web_kftc" || viewoptObj.defaultStorage == "kftc"){
						viewoptObj.defaultStorage = this.defOption.storage;
					}
					if(viewoptObj.storageList.indexOf("web_kftc") > -1 || viewoptObj.storageList.indexOf("kftc") > -1)
					{
						var idx = viewoptObj.storageList.indexOf("web_kftc");
						if(idx > -1){
							viewoptObj.storageList.splice(idx, 1, "web");
						}
					}
				}
				
				ML4WebApi.setProperty("cert_filter_expire", viewoptObj.certExpirefilter);
				ML4WebApi.setProperty("cert_filter_oid", viewoptObj.certOidfilter);
				
				Storage_API_filter.init(function(code, result){
					
					var isMobile = magiclineUtil.isMobile(magiclineUtil.getOS()); 
					
					if(isMobile){
						ML4WebDraw.MakeStorageListDiv_M(viewoptObj);
					}else{
						ML4WebDraw.MakeStorageListDiv(viewoptObj);
					}
					
					$("#ML_window").MLjquiWindow('open', function(e){
						
						/*
						if(isMobile){
							ML4WebDraw.initWebMainEvent_M();
						}else{
							ML4WebDraw.initWebMainEvent();
						}
						*/
						ML4WebSaveCertDraw.MakeSaveCertDiv(certInfo);
						
						if(viewoptObj.defaultStorage != null){
							$("#stg_" + viewoptObj.defaultStorage).click();
						}
						ML4WebUI.callback = callback;
						
					});
				})
			},
			selectStorageInfo : function(key, callback){
				if(key != 'smartcertnx'){
					ML4WebUI.blockUI();
				}
				ML4WebLog.log("ML4WebUI.selectStorageInfo() called...");
				setTimeout(function(){
					if(key == null || key == ""){
						callback( ML4WebLog.getErrCode("ML4Web_UI_selectStorageInfo"), {"errCode": 100, "errMsg": $.i18n.prop('ER100')});
						return;
					}else if( typeof callback != 'function' || callback == null || callback == ""){
						callback( ML4WebLog.getErrCode("ML4Web_UI_selectStorageInfo"), {"errCode": 103, "errMsg": $.i18n.prop('ER103')});
						return;
					}

					ML4WebUI.selectedStorage = {};

					switch(key){
						case 'web_kftc':
							ML4WebUI.selectedStorage = {
								key:"web_kftc",
								name : $.i18n.prop('TS701'),
								isPwdRequired : function() {
									return true;
								},
								AddCertificates : function( certi ){
									ML4WebUI.Storage.Add( certi );
								},
								RemoveCertificates : function( certi ){
									ML4WebUI.Storage.RemoveAt( this.IndexOf( certi, 0 ) );
								}
							};
							break;
						case 'kftc':
							ML4WebUI.selectedStorage = {
								key:"kftc",
								name : $.i18n.prop('TS701'),
								isPwdRequired : function() {
									return true;
								},
								AddCertificates : function( certi ){
									ML4WebUI.Storage.Add( certi );
								},
								RemoveCertificates : function( certi ){
									ML4WebUI.Storage.RemoveAt( this.IndexOf( certi, 0 ) );
								}
							};
							break;
						case 'web':
							ML4WebUI.selectedStorage = {
								key:"web",
								name : $.i18n.prop('TS701'),
								isPwdRequired : function() {
									return true;
								},
								AddCertificates : function( certi ){
									ML4WebUI.Storage.Add( certi );
								},
								RemoveCertificates : function( certi ){
									ML4WebUI.Storage.RemoveAt( this.IndexOf( certi, 0 ) );
								}
							};

							break;
						case 'pfx':
							ML4WebUI.selectedStorage = {
								key:"pfx",
								name : $.i18n.prop('TS702'),
								isPwdRequired : function() {
									return true;
								},
								AddCertificates : function( certi ){
									ML4WebUI.Storage.Add( certi );
								},
								RemoveCertificates : function( certi ){
									ML4WebUI.Storage.RemoveAt( this.IndexOf( certi, 0 ) );
								}
							};
							break;
						case 'hdd':
							ML4WebUI.selectedStorage = {
								key:"hdd",
								name : $.i18n.prop('TS703'),
								isPwdRequired : function() {
									return true;
								},
								AddCertificates : function( certi ){
									ML4WebUI.Storage.Add( certi );
								},
								RemoveCertificates : function( certi ){
									ML4WebUI.Storage.RemoveAt( this.IndexOf( certi, 0 ) );
								}
							};
							//
							break;
						case 'shdd':
							ML4WebUI.selectedStorage = {
								key:"shdd",
								name : $.i18n.prop('TS704'),
								isPwdRequired : function() {
									return true;
								},
								AddCertificates : function( certi ){
									ML4WebUI.Storage.Add( certi );
								},
								RemoveCertificates : function( certi ){
									ML4WebUI.Storage.RemoveAt( this.IndexOf( certi, 0 ) );
								}
							};
							break;
						case 'token':
							ML4WebUI.selectedStorage = {
								key:"token",
								name : $.i18n.prop('TS705'),
								isPwdRequired : function() {
									return true;
								},
								AddCertificates : function( certi ){
									ML4WebUI.Storage.Add( certi );
								},
								RemoveCertificates : function( certi ){
									ML4WebUI.Storage.RemoveAt( this.IndexOf( certi, 0 ) );
								}
							};
							break;
						case 'mobile':
							ML4WebUI.selectedStorage = {
								key:"mobile",
								name : $.i18n.prop('TS706'),
								isPwdRequired : function() {
									return true;
								},
								AddCertificates : function( certi ){
									ML4WebUI.Storage.Add( certi );
								},
								RemoveCertificates : function( certi ){
									ML4WebUI.Storage.RemoveAt( this.IndexOf( certi, 0 ) );
								}
							};
							break;
						case 'smartcert':
							ML4WebUI.selectedStorage = {
								key:"smartcert",
								name : $.i18n.prop('TS707'),
								isPwdRequired : function() {
									return false;
								},
								AddCertificates : function( certi ){
									ML4WebUI.Storage.Add( certi );
								},
								RemoveCertificates : function( certi ){
									ML4WebUI.Storage.RemoveAt( this.IndexOf( certi, 0 ) );
								}
							};
							break;
						case 'smartcertnx':
							ML4WebUI.selectedStorage = {
								key:"smartcertnx",
								name : $.i18n.prop('TS707'),
								isPwdRequired : function() {
									return false;
								},
								AddCertificates : function( certi ){
									ML4WebUI.Storage.Add( certi );
								},
								RemoveCertificates : function( certi ){
									ML4WebUI.Storage.RemoveAt( this.IndexOf( certi, 0 ) );
								}
							};
							break;
						case 'unisign':
							ML4WebUI.selectedStorage = {
								key:"unisign",
								name : $.i18n.prop('TS701'),
								isPwdRequired : function() {
									return false;
								},
								AddCertificates : function( certi ){
									ML4WebUI.Storage.Add( certi );
								},
								RemoveCertificates : function( certi ){
									ML4WebUI.Storage.RemoveAt( this.IndexOf( certi, 0 ) );
								}
						};
							break;
						case 'cloud':
							break;
						default:
							ML4WebLog.log("[Error] Not Support Storage.");
							break;
					}

					// 공동인증서 가져오기에서 '브라우저에 인증서 저장' 체크하고 비밀번호 틀린 후, 전자서명 시 비밀번호 틀림 알림 뜨지 않거나 전자서명 되지 않는 오류 수정 
					if(key != 'hdd'){
						var certOpt = {"storageName":key};
						
						ML4WebUI.selectedStorage.current_option = certOpt;
						certOpt.DS_PKI_CERT_PATH = encodeURIComponent(JSON.stringify(ML4WebApi.DS_PKI_CERT_PATH));
						certOpt.DS_PKI_POLICY_OID = encodeURIComponent(JSON.stringify(ML4WebApi.DS_PKI_POLICY_OID));
					}
					
					//create default selectedStorage
					ML4WebUI.Storage.makeStorage( ML4WebUI.selectedStorage );

					//create event listener(like callback) name of "certi-add"
					ML4WebUI.Storage.on("certi-add", function() {
						//ML4WebLog.log("selectedStorage.Count() === "+selectedStorage.Count());
					});

					ML4WebApi.ml4web_storage_api.SelectStorageInfo(key, function(resultCode, jsonObj){
						ML4WebLog.log("%%%%% select storageinfo : " + key + ", " + JSON.stringify(jsonObj));
						if(resultCode == 0){
							if(jsonObj != null){
								ML4WebUI.selectedStorage.option = jsonObj;
								callback(0, jsonObj);
								var temp = $('#temp').val();
								if(ML4WebUI.selectedStorage.key != "hdd" || (temp != '' && temp != 0 )){
									ML4WebUI.unblockUI();
								}
							}else{
								//no list
								callback(0, null);
								ML4WebUI.unblockUI();
							}
						}else{
							callback(ML4WebLog.getErrCode("ML4Web_UI_selectStorageInfo"), {"errCode": jsonObj.errCode, "errMsg": jsonObj.errMsg});
							ML4WebUI.unblockUI();
							return;
						}
					});
				},200);
			},
			
			getStorageCertList : function( certOpt, callback ){
				ML4WebLog.log("ML4WebUI.getStorageCertList() called..."); 

				if( certOpt == null || $.isEmptyObject(certOpt)){
					certOpt = ML4WebUI.selectedStorage.current_option;
				}else if( typeof callback != 'function' || callback == null || callback == "" ){
					callback( ML4WebLog.getErrCode("ML4Web_UI_getStorageCertList"), {"errCode": 103, "errMsg": $.i18n.prop('ER103')});
					return;
				}

				try{
					ML4WebUI.selectedStorage.current_option = certOpt;
					certOpt.DS_PKI_CERT_PATH = encodeURIComponent(JSON.stringify(ML4WebApi.DS_PKI_CERT_PATH));
					certOpt.DS_PKI_POLICY_OID = encodeURIComponent(JSON.stringify(ML4WebApi.DS_PKI_POLICY_OID));

					ML4WebApi.ml4web_storage_api.GetCertList(certOpt, function(resultCode, jsonObj){
						ML4WebLog.log("ml4web_storage_api.GetCertList resultCode :: " + resultCode);
						if(resultCode == 0){
							var cert_list = jsonObj.cert_list;
							if(cert_list!=null && cert_list.length>0){
								var tempArr = [];
								var certListCnt = cert_list.length;
								for(var i=0 ; i<certListCnt; i++){
									var tempCertObj = cert_list[i];
									var rawCertIdxObj = tempCertObj.storageRawCertIdx;
									
									if(typeof(rawCertIdxObj.storageOpt) == "string"){
										var obj = JSON.parse(rawCertIdxObj.storageOpt);
										rawCertIdxObj.storageOpt = obj;
									}
									
									var rawCertIdxStr = JSON.stringify(rawCertIdxObj);
									//ML4WebLog.log("cryto type : " + ML4WebApi.webConfig.libType)
									var encCertIdx = "";
									if(ML4WebApi.webConfig.libType == 0 || ML4WebApi.getProperty('selectedStorage').key=='smartcert'){
										encCertIdx = rawCertIdxStr;
									}else{
										//encCertIdx = ML4WebApi.dsencrypt(rawCertIdxStr);
										if(ML4WebUI.selectedStorage.current_option.storageName == "web"
											|| ML4WebUI.selectedStorage.current_option.storageName == "kftc"
											|| ML4WebUI.selectedStorage.current_option.storageName == "web_kftc"
											|| ML4WebUI.selectedStorage.current_option.storageName == "pfx"){
											encCertIdx = ML4WebApi.dsencrypt(rawCertIdxStr);
										}else{
											encCertIdx = rawCertIdxStr;
										}
									}
									
									if(rawCertIdxObj.storageName == "pfx"){ // pfx 파일 하위 브라우저(IE 8, 9) 브라우저 인증서 저장
										if(rawCertIdxObj.storageOpt.pfxOpt.libType == "c" && rawCertIdxObj.storageOpt.pfxOpt.SaveCert == "true"){
											HandleApi.getCertString(encCertIdx, function(code, obj){
												if(code == 0) {
													var certbag = [];
													
													obj.cert.storageCertIdx = tempCertObj.subkeyid;
													certbag.push(obj.cert);
													
													//localStorage.setItem('DreamWebCert', JSON.stringify(certbag));
													var targetStorage = "web";
													//var targetStorageOpt = '{"storageName":"web","storageOpt":{},"storageCertIdx":""}';
													var targetStorageOpt = '{}';

													ML4WebUI.saveCertToStorage(obj.cert, "aaa", targetStorage, targetStorageOpt, function(code2, obj2){
														if(code2 != 0){
															ML4WebLog.log("save cert failed in webStorage ");
														}
													});
												}else{
													callback(ML4WebLog.getErrCode("ML4Web_UI_getStorageCertList"), {"errCode": 888, "errMsg": e.message});
												}
											});
										}
									}
									
									delete tempCertObj["storageRawCertIdx"];
									tempCertObj["storageEncCertIdx"] = encCertIdx;
									
									//tempArr.push(tempCertObj);
									tempArr[i] = tempCertObj;
									
									ML4WebUI.selectedStorage.AddCertificates(tempCertObj);
								}

								jsonObj.cert_list = tempArr;
								callback(resultCode, jsonObj);
							}else{
								//목록없음.
								callback(ML4WebLog.getErrCode("ML4Web_UI_getStorageCertList"), {"errCode": 202, "errMsg": $.i18n.prop("ER202")});
								return;
							}
						}else{
							callback(resultCode, {"errCode": jsonObj.errCode, "errMsg": jsonObj.errMsg});
							return;
						}
					});
				}catch( e ){
					callback(ML4WebLog.getErrCode("ML4Web_UI_getStorageCertList"), {"errCode": 888, "errMsg": e.message});
					return;
				}
			},
			
			getCertString : function(certInfo, callback){
				ML4WebLog.log("ML4WebUI.getCertString() called... ");
				if(certInfo == null || certInfo == "" ){
					callback( ML4WebLog.getErrCode("ML4Web_UI_getCertString"), {"errCode": 100, "errMsg": $.i18n.prop("ER100")});
					return;
				}else if( typeof callback != 'function' || callback == null || callback == ""){
					callback( ML4WebLog.getErrCode("ML4Web_UI_getCertString"), {"errCode": 103, "errMsg": $.i18n.prop("ER103")});
					return;
				}

				var storageRawCertIdx = "";
				if(ML4WebApi.webConfig.libType == 0 || ML4WebApi.getProperty('selectedStorage').key=='smartcert'){
					storageRawCertIdx = JSON.parse(certInfo);
				}else{
					if(ML4WebUI.selectedStorage.current_option.storageName == "web"
						|| ML4WebUI.selectedStorage.current_option.storageName == "web_kftc"
						|| ML4WebUI.selectedStorage.current_option.storageName == "kftc"
						||	ML4WebUI.selectedStorage.current_option.storageName == "pfx")
					{
						storageRawCertIdx = JSON.parse(ML4WebApi.dsDecrypt(certInfo));
					}else{
						storageRawCertIdx = JSON.parse(certInfo);
					}
				}

				try{
					ML4WebApi.ml4web_storage_api.GetCertString(storageRawCertIdx, function(resultCode, resultObj){
						if( resultCode == 0) {
							callback(0, resultObj);
						}else{
							callback(resultCode, resultObj);
						}
					});
				}catch( e ){
					callback(ML4WebLog.getErrCode("ML4Web_UI_getCertString"), {"errCode": 888, "errMsg": e.message});
				}

			},
			
			saveCertToStorage : function(certbag, passwd, targetStorage, targetStorageOpt, callback){
				ML4WebLog.log("ML4WebUI.saveCertToStorage() called... ");
				//ML4WebLog.log("ML4WebApi.saveCertToStorage().certbag === "+JSON.stringify(certbag));
				//ML4WebLog.log("ML4WebApi.saveCertToStorage().passwd === "+passwd);
				//ML4WebLog.log("ML4WebApi.saveCertToStorage().targetStorage === "+targetStorage);
				//ML4WebLog.log("ML4WebApi.saveCertToStorage().targetStorageOpt === "+JSON.stringify(targetStorageOpt));

				if(certbag == null || $.isEmptyObject(certbag)){
					callback( ML4WebLog.getErrCode("ML4Web_UI_saveCertToStorage"), {"errCode": 100, "errMsg": $.i18n.prop("ER100")});
					return;
				}else if( typeof callback != 'function' || callback == null || callback == ""){
					callback( ML4WebLog.getErrCode("ML4Web_UI_saveCertToStorage"), {"errCode": 103, "errMsg": $.i18n.prop("ER103")});
					return;
				}

				var targetStorageRawIdx = {}
				targetStorageRawIdx.storageName = targetStorage;
				targetStorageRawIdx.storageOpt =  targetStorageOpt;
				targetStorageRawIdx.storageCertIdx = "";

				ML4WebApi.ml4web_storage_api.SaveCert (certbag, passwd, targetStorageRawIdx, callback);
			},
			
			copyCertToStorage : function(certInfo, passwd, targetStorage, targetStorageOpt, callback){
				ML4WebLog.log("ML4WebUI.copyCertToStorage() called... ");
				//ML4WebLog.log("ML4WebUI.copyCertToStorage().certInfo === "+JSON.stringify(certInfo));
				//ML4WebLog.log("ML4WebUI.copyCertToStorage().passwd === "+passwd);
				//ML4WebLog.log("ML4WebUI.copyCertToStorage().targetStorage === "+targetStorage);
				//ML4WebLog.log("ML4WebUI.copyCertToStorage().targetStorageOpt === "+JSON.stringify(targetStorageOpt));

				if(certInfo == null || certInfo==""){
					callback( ML4WebLog.getErrCode("ML4Web_UI_CopyCertToStorage"), {"errCode": 100, "errMsg": $.i18n.prop("ER100")});
					return;
				}else if(passwd==null || passwd==""){
					callback( ML4WebLog.getErrCode("ML4Web_UI_CopyCertToStorage"), {"errCode": 100, "errMsg": $.i18n.prop("ER100")});
					return;
				}else if(targetStorage==null || targetStorage==""){
					callback( ML4WebLog.getErrCode("ML4Web_UI_CopyCertToStorage"), {"errCode": 100, "errMsg": $.i18n.prop("ER100")});
					return;
				}else if(targetStorageOpt == null || $.isEmptyObject(targetStorageOpt)){
					callback( ML4WebLog.getErrCode("ML4Web_UI_CopyCertToStorage"), {"errCode": 100, "errMsg": $.i18n.prop("ER100")});
					return;
				}else if( typeof callback != 'function' || callback == null || callback == ""){
					callback( ML4WebLog.getErrCode("ML4Web_UI_CopyCertToStorage"), {"errCode": 103, "errMsg": $.i18n.prop("ER103")});
					return;
				}

				try{
					this.getCertString(certInfo, function(code, obj){
						if( code == 0) {
							var certbag = obj.cert;
							ML4WebApi.ml4web_storage_api.SaveCert (certbag, passwd, targetStorageOpt, callback);
						}else{
							callback(code, obj);
						}
					});
				}catch( e ){
					callback(ML4WebLog.getErrCode("ML4Web_UI_CopyCertToStorage"), {"errCode": 888, "errMsg": e.message});
				}
			},
			
			deleteStorageCert : function( certInfo, passwd, callback ) {
				ML4WebLog.log("ML4WebUI.deleteStorageCert() called...");

				if( certInfo == null || certInfo==""){
					callback( ML4WebLog.getErrCode("ML4Web_UI_DeleteStorageCert"), {"errCode": 100, "errMsg": $.i18n.prop("ER100")});
					return;
				} else if ( typeof callback != 'function' || callback == null || callback == ""){
					callback( ML4WebLog.getErrCode("ML4Web_UI_DeleteStorageCert"), {"errCode": 103, "errMsg": $.i18n.prop("ER103")});
					return;
				}

				//- certInfo = storageEncCertIdx
				var storageRawCertIdx;
				if(ML4WebApi.webConfig.libType == 0 || ML4WebApi.getProperty('selectedStorage').key=='smartcert'){
					storageRawCertIdx = JSON.parse(certInfo);
				}else{
					//storageRawCertIdx = JSON.parse(ML4WebApi.dsDecrypt(certInfo));
					if(ML4WebUI.selectedStorage.current_option.storageName == "web" || ML4WebUI.selectedStorage.current_option.storageName == "pfx" || ML4WebUI.selectedStorage.current_option.storageName == "web_kftc"){
						storageRawCertIdx = JSON.parse(ML4WebApi.dsDecrypt(certInfo));
					}else{
						storageRawCertIdx = JSON.parse(certInfo);
					}
				}
				var stgKey = ML4WebUI.selectedStorage.current_option.storageName;
				
				if(stgKey === "web_kftc"){
					storageRawCertIdx.storageName = "web_kftc";
				}
				
				if(stgKey == "token"){ // 토큰일 경우 pin 필요
					ML4WebApi.ml4web_storage_api.DeleteCert_Token( storageRawCertIdx, passwd, callback );
				}else{
					ML4WebApi.ml4web_storage_api.DeleteCert( storageRawCertIdx, callback );
				}
			},
			
			changeStorageCertPasswd : function( certInfo, passwdPre, passwdNext, callback ) {
				ML4WebLog.log("ML4WebUI.changeStorageCertPasswd() called... ");

				if( certInfo == null || certInfo==""){
					callback( ML4WebLog.getErrCode("ML4Web_UI_ChageStorageCertPasswd"), {"errCode": 100, "errMsg": $.i18n.prop("ER100")});
					return;
				}else if( passwdPre == null || passwdPre==""){
					callback( ML4WebLog.getErrCode("ML4Web_UI_ChageStorageCertPasswd"), {"errCode": 100, "errMsg": $.i18n.prop("ER100")});
					return;
				}else if( passwdNext == null || passwdNext==""){
					callback( ML4WebLog.getErrCode("ML4Web_UI_ChageStorageCertPasswd"), {"errCode": 100, "errMsg": $.i18n.prop("ER100")});
					return;
				}else if( typeof callback != 'function' || callback == null || callback == ""){
					callback( ML4WebLog.getErrCode("ML4Web_UI_ChageStorageCertPasswd"), {"errCode": 103, "errMsg": $.i18n.prop("ER103")});
					return;
				}

				//ML4WebLog.log("ML4WebApi.changeStorageCertPasswd()  certInfo === " + certInfo);
				//ML4WebLog.log("ML4WebApi.changeStorageCertPasswd()  passwdPre === " + passwdPre);
				//ML4WebLog.log("ML4WebApi.changeStorageCertPasswd()  passwdNext === " + passwdNext);

				var storageRawCertIdx;
				if(ML4WebApi.webConfig.libType == 0 || ML4WebApi.getProperty('selectedStorage').key=='smartcert'){
					storageRawCertIdx = JSON.parse(certInfo);
				}else{
					//storageRawCertIdx = JSON.parse(ML4WebApi.dsDecrypt(certInfo));
					if(ML4WebUI.selectedStorage.current_option.storageName == "web" || ML4WebUI.selectedStorage.current_option.storageName == "pfx"){
						storageRawCertIdx = JSON.parse(ML4WebApi.dsDecrypt(certInfo));
					}else{
						storageRawCertIdx = JSON.parse(certInfo);
					}
				}
				
				ML4WebApi.ml4web_storage_api.ChangePassword(storageRawCertIdx, passwdPre, passwdNext, callback);
			},
			EZinit : function(callback){
				
				//211018 간편인증
				$("#selectCertContainer").load("/MagicLine4Web/magicLineEZAuth/UI/ML_ezauth_WebUI.jsp?random=" + Math.random() * 99999, function(){
				//$("#selectCertContainer").load("UI/ML4Web_EZMain.html?random=" + Math.random() * 99999, function(){
					callback(200);
					$('#ML_Hub_window').MLjquiWindow('open');
				});
				
			},
			init : function(callback){
				$("#selectCertContainer").load("UI/ML4Web_Main.html?random=" + Math.random() * 99999, function(){
					/*
					$("#ML_window").MLjquiWindow('open', function(e){
						$('#certaction').val('MakeSignData');
						callback(200);
					});
					*/
					callback(200);
				});
			},
			// 인증서 저장 init : 다른 jsp 를 로드
			saveCertInit : function(callback){
				$("#selectCertContainer").load("UI/ML4Web_SaveCertMain.html?random=" + Math.random() * 99999, function(){
					/*
					$("#ML_window").MLjquiWindow('open', function(e){
						$('#certaction').val('MakeSignData');
						callback(200);
					});
					*/
					callback(200);
				});
			},
			MakeExtentionData : function(msg, option, callback){// 창 열기-확장기능

			},
			MgmtDialog : function(callback){// 창 열기-인증서관리기능 전체
				if( typeof callback != 'function' || callback == null || callback == ""){
					callback( ML4WebLog.getErrCode("ML4Web_UI_MgmtDialog"), {"errCode": 103, "errMsg": "callback 값이 null이거나  함수가 아닙니다."});
					return;
				}

				this.mgmtDialogName = "mgmtAll";

				try{
					if ( $("#ML_window_admin").length > 0 ) {
						$('#ML_window_admin').MLjquiWindow('title', '인증서관리');
						$('#ML_window_admin').MLjquiWindow('open', function(e){
//							$('#certaction').val('Mgmt');
						});
					}else{
						$("#selectCertContainer").load("UI/ML4Web_Mgmt.html", function(){
//							$('#ML_window_admin').MLjquiWindow('open', function(e){
//								$('#certaction').val('Mgmt');
								callback();
//							});
						});
					}
					//$("#manageCertContainer").load("UI/ML4Web_Mgmt_iframe.html", function(){
						//$('#certaction').val('MakeSignData');
					//});

					/*$("#selectCertContainer").empty();
					$("#selectCertContainer").load("UI/ML4Web_Mgmt_iframe.html", function(){
						//$('#certaction').val('MakeSignData');
					});*/
				}catch(ex){
					callback( ML4WebLog.getErrCode("ML4Web_UI_MgmtDialog"), {"errCode": 888, "errMsg": ex.message});
				}
			},
			getCertManager : function(dialogName, callback){
				if( typeof callback != 'function' || callback == null || callback == ""){
					callback( ML4WebLog.getErrCode("ML4Web_UI_getCertManager"), {"errCode": 103, "errMsg": $.i18n.prop("ER103")});
					return;
				}

				this.mgmtDialogName = dialogName;

				try{
					//dialogName = all, copy, delete, import, export...
					switch(dialogName){
						case 'all':
							this.MgmtDialog(callback);
							break;
						case 'copy':

							break;
						case 'delete':
							this.DeleteDialog(callback);
							break;
						case 'import':
							this.ImportDialog(callback);
							break;
						case 'export':

							break;
						case 'changePw':
							this.PWChangeDialog(callback);
							break;
						default:
							callback( ML4WebLog.getErrCode("ML4Web_UI_getCertManager"), {"errCode": 104, "errMsg": $.i18n.prop("ER104")});
							break;
					}
				}catch(ex){
					callback( ML4WebLog.getErrCode("ML4Web_UI_getCertManager"), {"errCode": 888, "errMsg": ex.message});
				}
			},
			PWChangeDialog : function(callback){// 창 열기-인증서 비밀번호 변경
				if( typeof callback != 'function' || callback == null || callback == ""){
					callback( ML4WebLog.getErrCode("ML4Web_UI_MgmtDialog"), {"errCode": 103, "errMsg": "callback 값이 null이거나  함수가 아닙니다."});
					return;
				}

				try{
					$("#selectCertContainer").empty();

					if ( $("#ML_window_admin").length > 0 ) {
						$('#ML_window_admin').MLjquiWindow('title', '인증서관리');
						$('#ML_window_admin').MLjquiWindow('open', function(e){
//							$('#certaction').val('Mgmt');
						});
					}else{
						$("#selectCertContainer").load("UI/ML4Web_Mgmt.html", function(){
							$('#ML_window_admin').MLjquiWindow('open', function(e){
//								$('#certaction').val('Mgmt');
							});
						});
					}

					/*
					
					$("#selectCertContainer").load("UI/ML4Web_Mgmt_iframe.html", function(){
						//$('#certaction').val('MakeSignData');
					});*/
				}catch(ex){
					callback( ML4WebLog.getErrCode("ML4Web_UI_MgmtDialog"), {"errCode": 888, "errMsg": ex.message});
				}
			},
			CopyDialog : function(callback){// 창 열기-인증서복사
				
			},
			DeleteDialog : function(callback){// 창 열기-인증서삭제
				if( typeof callback != 'function' || callback == null || callback == ""){
					callback( ML4WebLog.getErrCode("ML4Web_UI_DeleteDialog"), {"errCode": 103, "errMsg": "callback 값이 null이거나  함수가 아닙니다."});
					return;
				}

				try{
					$("#selectCertContainer").empty();

					if ( $("#ML_window_admin").length > 0 ) {
						$('#ML_window_admin').MLjquiWindow('title', '인증서관리');
						$('#ML_window_admin').MLjquiWindow('open', function(e){
//							$('#certaction').val('Mgmt');
						});
					}else{
						$("#selectCertContainer").load("UI/ML4Web_Mgmt.html", function(){
							$('#ML_window_admin').MLjquiWindow('open', function(e){
//								$('#certaction').val('Mgmt');
							});
						});
					}

					/*
					
					$("#selectCertContainer").load("UI/ML4Web_Mgmt_iframe.html", function(){
						//$('#certaction').val('MakeSignData');
					});*/
				}catch(ex){
					callback( ML4WebLog.getErrCode("ML4Web_UI_DeleteDialog"), {"errCode": 888, "errMsg": ex.message});
				}
			},
			ImportDialog : function(callback){// 창 열기-인증서가져오기
				if( typeof callback != 'function' || callback == null || callback == ""){
					callback( ML4WebLog.getErrCode("ML4Web_UI_ImportDialog"), {"errCode": 103, "errMsg": "callback 값이 null이거나  함수가 아닙니다."});
					return;
				}

				try{
					$("#selectCertContainer").empty();

					if ( $("#ML_window_admin").length > 0 ) {
						$('#ML_window_admin').MLjquiWindow('title', '인증서관리');
						$('#ML_window_admin').MLjquiWindow('open', function(e){
//							$('#certaction').val('Mgmt');
						});
					}else{
						$("#selectCertContainer").load("UI/ML4Web_Mgmt.html", function(){
							$('#ML_window_admin').MLjquiWindow('open', function(e){
//								$('#certaction').val('Mgmt');
							});
						});
					}

					/*
					$("#selectCertContainer").load("UI/ML4Web_Mgmt_iframe.html", function(){
						//$('#certaction').val('MakeSignData');
					});*/
				}catch(ex){
					callback( ML4WebLog.getErrCode("ML4Web_UI_ImportDialog"), {"errCode": 888, "errMsg": ex.message});
				}
			},
			ExportDialog : function(callback){// 창 열기-인증서내보내기

			},
			closeMainDialog : function (mode){
				//ML4WebLog.log("ML4WebUI.closeMainDialog() called... mode ===" + mode);
				ML4WebLog.log("ML4WebUI.closeMainDialog() called... mode ===");
				if(mode == "main") {
					
				}else if(mode == "mgmt"){

				}else if(mode == "cs"){

				}

				childInit.closeMainDialogToChild();

				
			},
			getSelectCert : function(callback){//인증서 정보 조회
				//?????????????????????
			},
			setDSOption: function(key, value){
				this.DSOption[key] = value;
			},
			getDSOption: function(){
				return this.DSOption;
			},
			getDialogName: function(){
				return this.mgmtDialogName;
			}
		};

		if(!window.ML4WebUI) window.ML4WebUI = _ML4WebUI;

		function popMainDialog(){
			ML4WebLog.log("ML4WebUI.popMainDialog called...");
		}

		function popMgmtDialog(key, callback){
			//ML4WebLog.log("ML4WebUI.popMgmtDialog called... key====" + key);
			ML4WebLog.log("ML4WebUI.popMgmtDialog called...");
		}
	}

	if (typeof ML4WebUtil === "undefined") {
		var _ML4WebUtil = {
			getBackupCertList : function(){
				ML4WebLog.log("ML4WebUtil.getBackupCertList called...");
				if(ML4WebApi.getProperty('isUseMLCert')){
					getBackupCertListFromMlcert();
				}
			},
			setBackupCertList : function(backupCertList){
				ML4WebLog.log("ML4WebUtil.setBackupCertList called...");
				if(ML4WebApi.getProperty('isUseMLCert')){
					if(backupCertList!=null){
						setBackupCertListToMlcert(backupCertList);
					}else{
						setBackupCertListToMlcert(localStorage.getItem('DreamWebCert'));
					}
				}
			},
			syncLocalStorage : function(backupCertList ){
				if(ML4WebApi.getProperty('isUseMLCert')){
					var CertBagList = localStorage.getItem('DreamWebCert');

					var arr1 = (backupCertList!=null && backupCertList!="null") ? JSON.parse(backupCertList) : [];
					var arr2 = (CertBagList!=null && CertBagList!="null") ? JSON.parse(CertBagList) : [];
					var arr = $.merge(arr1, arr2); 

					var result = [];
					$.each(arr, function(index, element) {
						var orgin_str = JSON.stringify(result);
						var elm_str = JSON.stringify(element);
						if (orgin_str.indexOf(elm_str) < 0){
							result.push(element);  
						}
					});

					//ML4WebLog.log("ML4WebUtil.syncLocalStorage result = " + JSON.stringify(result));

					// localStorage - ml4web
					localStorage.removeItem('DreamWebCert');
					localStorage.setItem('DreamWebCert', JSON.stringify(result));
					
					// localStorage - mlcert
					setBackupCertListToMlcert(JSON.stringify(result));
				}
			},
			saveCertValue : function( key,value ){
				if(window.localStorage){
					localStorage.setItem( key, value );
				} else{
					if(window.sessionStorage){
						sessionStorage.setItem( key, value );
					}else{
						var Cert = encodeURI( value );
						this.setCookie( key, "", -1 );
						this.setCookie( key, Cert, 1);
					}
				}
			},
			loadCertValue : function( key ){
				try{
					if(window.localStorage){
						return localStorage.getItem( key );
					} else{
						if(window.sessionStorage){
							return sessionStorage.getItem( key );
						}else{
							var cert = this.getCookie( key );
							return decodeURI( cert );
						}
					}
				}catch( e ){
					ML4WebLog.log("[ERROR]ML4WebUtil.loadValue  ===" + e.message);
					return "";
				}
			},
			/*setCookie : function(cname, cvalue, exdays){
				var d = new Date();
				
				//d.setTime(d.getTime() + (exdays*24*60*60*1000));
				d.setTime(d.getTime() + (exdays*300000));
				var expires = "expires=" + d.toUTCString();
				document.cookie = cname + "=" + cvalue + "; " + expires;
			},*/
			getCookie : function(cname){
				var name = cname + "=";
				var ca = document.cookie.split(';');
				for(var i=0; i<ca.length; i++) {
					var c = ca[i];
					while (c.charAt(0)==' ') c = c.substring(1);
					if (c.indexOf(name) == 0) return c.substring(name.length,c.length);
				}
				return "";
			},
			checkPasswordValidation : function(password, callback ) {
				if( password.length<8 || password.length > 20 ) {
					callback(1, {"msg":"비밀번호는 소문자, 대문자, 숫자, 특수문자의 조합으로 9~20자리로 입력해주세요."});
					return;
				}

				var passwd = password;
				var chk_num = passwd.search(/[0-9]{2,}/g);
				var chk_eng = passwd.search(/[a-z]{2,}/g);
				var chk_Beng = passwd.search(/[A-Z]{2,}/g);
				var chk_spc = passwd.search(/[`!@$%^&*()<>|\\\'\";:\/?]{2,}/i);
				//alert("chk_num :"+chk_num+"\n"+"chk_eng :"+chk_eng+"\n"+"chk_Beng :"+chk_Beng+"\n"+"chk_spc :"+chk_spc+"\n");

				if((chk_num < 0)) {
					 callback(1, {"msg":"숫자가 2자 이상 포함되어 있어야 합니다."});
					 return;
				}
				if((chk_eng < 0)) {
					callback(1, {"msg":"소문자가 2자 이상 포함되어 있어야 합니다."});
					return;
				}
				if((chk_Beng < 0)) {
					callback(1, {"msg":"대문자가 2자 이상 포함되어 있어야 합니다."});
					return;
				}
				if((chk_spc < 0)) {
					callback(1, {"msg":"기호가 2자 이상 포함되어야 합니다."});
					return;
				}

				var SamePass_0 = 0; //동일문자 카운트
				var SamePass_1 = 0; //연속성(+) 카운드
				var SamePass_2 = 0; //연속성(-) 카운드

				var chr_pass_0;
				var chr_pass_1;
				var chr_pass_2;

				for(var i=0; i < password.length; i++) {
					chr_pass_0 = password.charAt(i);
					chr_pass_1 = password.charAt(i+1);

					//동일문자 카운트
					if(chr_pass_0 == chr_pass_1) {
						SamePass_0 = SamePass_0 + 1;
					}

					chr_pass_2 = password.charAt(i+2);
					//연속성(+) 카운드

					if(chr_pass_0.charCodeAt(0) - chr_pass_1.charCodeAt(0) == 1 && chr_pass_1.charCodeAt(0) - chr_pass_2.charCodeAt(0) == 1) {
						SamePass_1 = SamePass_1 + 1;
					}

					//연속성(-) 카운드
					if(chr_pass_0.charCodeAt(0) - chr_pass_1.charCodeAt(0) == -1 && chr_pass_1.charCodeAt(0) - chr_pass_2.charCodeAt(0) == -1) {
						SamePass_2 = SamePass_2 + 1;
					}
				}

				if(SamePass_0 > 1) {
					callback(1, {"msg":"동일문자를 3번 이상 사용할 수 없습니다."});
					return;
				}

				if(SamePass_1 >= 1 || SamePass_2 >= 1 ) {
					callback(1, {"msg":"연속된 문자열(123 또는 321, abc, cba 등)을\n 3자 이상 사용 할 수 없습니다."});
					return;
				}

				callback(0, {"msg":"성공"});
			},
			isDateExpired : function(dateStr){
				var dateArr = dateStr.split("-");
				var today = new Date();
				//var endDate = new Date(dateArr[0],parseInt(dateArr[1])-1,dateArr[2]);
				var a = dateStr.split(" ");
				var d = a[0].split("-");
				var t = a[1].split(":");
				var endDate = new Date(d[0],(d[1]-1),d[2],t[0],t[1],t[2]);

				if (today < endDate) {
					return false;
				} else {
					return true;
				}
			},
			getTimeStamp : function() {
			  var d = new Date();

			  var s =
				leadingZeros(d.getFullYear(), 4) + '-' +
				leadingZeros(d.getMonth() + 1, 2) + '-' +
				leadingZeros(d.getDate(), 2) + ' ' +

				leadingZeros(d.getHours(), 2) + ':' +
				leadingZeros(d.getMinutes(), 2) + ':' +
				leadingZeros(d.getSeconds(), 2);

			  return s;
			},
			leadingZeros : function (n, digits) {
			  var zero = '';
			  n = n.toString();

			  if (n.length < digits) {
				for (i = 0; i < digits - n.length; i++)
				  zero += '0';
			  }
			  return zero + n;
			},
			isDateCompare : function(dtmfrom, dtmto){
				return new Date(dtmfrom).getTime() >=  new Date(dtmto).getTime() ;
			},
			yyyymmdd : function(dateIn) {
				var yyyy = dateIn.getFullYear();
				var mm = dateIn.getMonth()+1; // getMonth() is zero-based
				var dd  = dateIn.getDate();
				return String(10000*yyyy + 100*mm + dd); // Leading zeros for mm and dd
			}
		};

		window.ML4WebUtil = _ML4WebUtil;
	}
	jQuery(document).ready(function() {
	});
	
	$.cachedScript = function(url, options){
		options = $.extend(options || {}, {
			DataType : "script",
			cache : true,
			url : url
		});
		return jQuery.ajax(options);
	}
})(jQuery);

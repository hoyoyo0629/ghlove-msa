var fincert = {
	config : {
		/*TOKEN_URL : 'https://t-certapi.yeskey.or.kr/oauth/2.0/api/token',
		UCPID_URL : 'https://t-certapi.yeskey.or.kr/v1/ucpid/ucpid-info',
		SCRIPT_URL : 'https://t-4user.yeskey.or.kr/v1/fincert.js',
		ORG_CODE : 'DG00560000',
		API_KEY : 'dba3f9f8-0ada-4661-91f2-18e53681d255',
		CP_CODE : 'Y30000001301',
		CLIENT_SECRET : '87b70351-7d6a-42f4-96cf-b8c9d39c1770',*/
		TOKEN_URL : 'https://certapi.yeskey.or.kr/oauth/2.0/api/token',
		UCPID_URL : 'https://certapi.yeskey.or.kr/v1/ucpid/ucpid-info',
		SCRIPT_URL : 'https://4user.yeskey.or.kr/v1/fincert.js',
		ORG_CODE : 'RG00560000',
		API_KEY : 'ea1525f5-f619-4398-abc2-2c6d9581550d',
		CP_CODE : 'Y30000001301',
		CLIENT_SECRET : '8a6aa6d9-47d9-4999-9dd5-8f4a36d1b526',
        SCOPE : 'ucpid',
        GRANT_TYPE : 'client_credentials',
        SERVER_ID : 'server_server_ilovegohyang'
	},
	const : {
		LOADING : "LOADING",
		COMPLETE : "COMPLETE",
		INIT_ERROR : "INIT_ERROR",
		SCRIPT_ERROR : "SCRIPT_ERROR"
	},
	initState : '',
	tokenRefreshCnt : 0
};

window.onload = function () {
	fincert.loadScript();
}

fincert.loadScript = function () {
	this.initState = this.const.LOADING;
	var today = new Date();
	var year = today.getFullYear();
	var month = ('0' + (today.getMonth() + 1)).slice(-2);
	var day = ('0' + today.getDate()).slice(-2);

	var dateString = year + month + day;

	var head= document.getElementsByTagName('head')[0];
	var script = document.createElement("script");
	script.src = this.config.SCRIPT_URL + '?dt=' + dateString;
	head.appendChild(script);

	script.onload = function () {
		fincert.init();
	}

	script.onerror = function () {
		fincert.initState = fincert.const.SCRIPT_ERROR;
	}
}

fincert.init = function () {

	FinCert.Sdk.init({
		"orgCode": fincert.config.ORG_CODE,
		"apiKey": fincert.config.API_KEY,
		"lang": "kor",
		"success": function() {
			fincert.initState = fincert.const.COMPLETE;
		},
		"fail": function(error) {
			fincert.initState = fincert.const.INIT_ERROR;
		}
	});

}

fincert.initCheck = function () {
	if (this.initState === this.const.LOADING) {
		alert("금융인증서 로딩 중 입니다.\n잠시 후 다시 이용해 주세요.");
		return false;
	} else if (this.initState === this.const.SCRIPT_ERROR) {
		alert("금융인증서 호출 중 오류가 발생하였습니다.\n관리자에게 문의 부탁드립니다.");
		return false;
	} else if (this.initState === this.const.INIT_ERROR) {
		alert("금융인증서 초기화 중 오류가 발생하였습니다.\n관리자에게 문의 부탁드립니다.");
		return false;
	}

	return true;
}

// udpidNonce를 가져온다
fincert.getUdpidNonce = function () {

	return new Promise(function (resolve, reject) {
		$.get('/opmanager/user/fin/nonce', null, function (response) {
			if (response.isSuccess) {
				resolve(response.data);
			} else {
				reject({
					code : '00000',
					message : response.errorMessage,
					description : 'UpdId를 가저오는 중 오류가 발생하였습니다.'
				});
			}
		})
		.fail(function (err) {
			var error = err.responseJSON;
			if (error) error.description = 'UpdId 호출 중 오류가 발생하였습니다.';
			else error = {description : 'UpdId 호출 중 오류가 발생하였습니다.'};
			reject(err);
		});
	})

}

fincert.sign = function (nonce) {

	return new Promise(function (resolve, reject) {
		FinCert.Sdk.sign({
		    "content": {
				"ucpidInfo" : {
					"ispUrlInfo" : "https://www.ilovegohyang.go.kr",
					"ucpidNonce" : nonce,
					"userAgreement" : "금융분야 마이데이터 통합인증을 위한 인증서 본인확인서비스 이용약관, 개인정보 처리, 고유식별정보 수집·이용 및 위탁에 동의합니다.",
					"userAgreeInfo" : {
						"realName" : true,
						"gender" : true,
						"nationalInfo" : true,
						"birthDate" : true,
						"ci" : true
					}
				}
			},
			"info": {
				"signType": "12"
			},
			"success": function(result) {
				resolve(result)
			},
			"fail": function(error) {
				error.description = '전자서명 생성 중 오류가 발생하였습니다.';
				reject(error);
			}
		});
	});

}

fincert.getToken = function () {
	return new Promise(function (resolve, reject) {
		$.ajax({
			/*url : '/opmanager/user/fin/token',
			type : 'get',
			success : function (response) {
				if (response && response.data && response.data.err_code != null && response.data.err_code != '') {
					var error = {
						code : response.data.err_code,
						message : response.data.err_code,
						description : '토큰 생성 중 오류가 발생하였습니다.'
					};

					reject(error);
				}

				resolve(response.data);
			},*/
			url : fincert.config.TOKEN_URL,
			type : 'post',
			contentType: 'application/x-www-form-urlencoded;charset=utf-8',
			data : {
				"client_secret" : fincert.config.CLIENT_SECRET,
                "scope" : fincert.config.SCOPE,
                "grant_type" : fincert.config.GRANT_TYPE,
                "server_id" : fincert.config.SERVER_ID
			},
			beforeSend : function(xhr){
				xhr.setRequestHeader("client_id", fincert.config.API_KEY);
			},
			success : function (response) {
				if (response && response.data && response.data.err_code != null && response.data.err_code != '') {
					var error = {
						code : response.data.err_code,
						message : response.data.err_code,
						description : '토큰 생성 중 오류가 발생하였습니다.'
					};

					reject(error);
				}

				resolve(response);
			},
			error : function(err) {
				var error = err.responseJSON;

				if (error) error.description = '토큰 생성 호출 중 오류가 발생하였습니다.';
				else error = {description : '토큰 생성 호출 중 오류가 발생하였습니다.'};

				reject(error);
			}
		})
	})
}


fincert.getUcpidInfo = function (params) {

	return new Promise(function (resolve, reject) {
		$.ajax({
			url : fincert.config.UCPID_URL,
			type : 'post',
			contentType: 'application/json;charset=utf-8',
			data : JSON.stringify({
				"ucpid_request_info" : params.signedVal,
				"cp_code" : fincert.config.CP_CODE,
				"ucpid_nonce" : params.ucpidNonce
			}),
			beforeSend : function(xhr){
				xhr.setRequestHeader("Authorization", params.tokenType+ " " + params.accessToken);
			},
			error : function(request) {
				var error = request.responseJSON;
				error.description = '본인확인 요청 중 오류가 발생하였습니다.';
				// 900101 : Access Token 거부
				// 900102 : Access Token 만료
				// 토큰 거부 또는 만료인 경우 한 번만 재발행 시도
				if ((error.err_code == '900401' || error.err_code == '900101' || error.err_code == '900102') && fincert.tokenRefreshCnt == 0) {
					fincert.tokenRefreshCnt = 1;
					fincert.getToken()
					.then(function () {
						return fincert.getUcpidInfo(params);
					})
					.then(function (res) {
						fincert.tokenRefreshCnt = 0;
						resolve(res);
					})
					.catch(function () {
						fincert.tokenRefreshCnt = 0;
						reject(error);
					});
				} else {
					reject(error);
				}

			},
			success : function (response) {
				resolve(response);
			}
		})
	})
}
/**
 *  금융인증서 인증 후
 *  인증 한 사용자 정보 (이름, 생년월일, ci, di, dn등)값을 가져온다.
 */
fincert.getUserDn = function (succCallback, errorCallback) {

	if (!this.initCheck()) return false;


	var params = {
		signedVal : '',
		accessToken : '',
		tokenType : '',
		ucpidNonce : ''
	}

	// 1. udcpidNonce 획득
	this
	.getUdpidNonce()
	.then(function (nonce) {
		params.ucpidNonce = nonce;
		// 2. 전자 서명 UI 호출
		return fincert.sign(nonce);
	})
	.then(function (response) {
		params.signedVal = response.signedVals[0];
		// 3. 인증서비스 토큰 발급
		return fincert.getToken();
	})
	.then(function (response) {
		params.accessToken = response.access_token;
		params.tokenType = response.token_type;

		// 4. upcidInfo (ci, di, 이름, 생년월일등 사용자정보) 요청
		return fincert.getUcpidInfo(params);
	})
	.then(function (response) {
		if (succCallback) succCallback(response);
	})
	.catch(function (err) {
		if (err && err.code == 800000) return false;
		var error = {
			code : err.code ? err.code : err.err_code,
			message : err.message ? err.message : err.err_msg,
			description : err.description
		};

		if (errorCallback) errorCallback(error);
	});
}
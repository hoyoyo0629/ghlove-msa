var ML_Default_Set = {
	protocolType				: location.protocol.indexOf(":")<=-1?location.protocol+=":":location.protocol, // "http:" 또는 "https:"
	// 로컬
	cpUrl						: location.port != ""?location.hostname+":"+location.port:location.hostname,
	mlMainUrl					: location.port != ""?location.hostname+":"+location.port:location.hostname, //MagicLine4 Web 기본 URL
	mlDirPath 					: "/content/MagicLine4Web/ML4Web/",

	// SSL
	//cpUrl						: "mlweb.dreamsecurity.com:18443",
	//mlMainUrl					: "ml4web.dreamsecurity.com:8443", //MagicLine4 Web 기본 URL
	//mlDirPath 					: "/ML4-Web/ML4Web/",

	mlCertUrl					: "mlcert.dreamsecurity.com", //MLCert 기본 URL(로컬스토리지 인증서 백업목적)
	cryptoType					: 1, //암호라이브러리 선택 (0=CS, 1=JS, 2=ETC...)
	cs_install_type				: "download", //설치페이지 사용여부 true/false/download (바로다운로드)
	smartcert_type				: "C" //C or JS
}

var ConfigObject = {
	//1. MagicLine4 Web
	ProtocolType				: ML_Default_Set.protocolType,	// protocolType
	DirPath						: ML_Default_Set.mlDirPath,	// MagicLine4Web 디렉토리
	BaseURL						: ML_Default_Set.mlMainUrl,	//MagicLine4Web 기본 URL
	MLCertURL					: ML_Default_Set.mlCertUrl,	//MLCert 기본 URL(로컬스토리지 인증서 백업목적)
	isUseMLCert					: false,	//로컬스토리지 인증서 백업 기능 사용 여부
	STORAGELIST_LNB				: null,	//왼쪽날개창 여부 값. ["share","finance"] : 공동인증서, 금융인증서 ([]:off)
	STORAGELIST					: ["web","hdd","token","mobile","smartcertnx"],	//지원 스토리지 리스트 ( 순서대로 UI 그림, JSON)
	STORAGELIST_M				: ["web"], //지원 스토리지 리스트 (모바일)
	STORAGELISTMGMT				: ["web","hdd"],//지원 스토리지 리스트 (인증서 관리창)
	saveStorageList				: ["web","hdd"],//인증서 저장기능 지원 가능 스토리지 리스트
	STORAGESELECT				: "hdd",	//인증서 선택창 초기화 할 때, 기본 선택 스토리지
	CRYPTO						: ML_Default_Set.cryptoType,	//암호라이브러리 선택 (0=CS, 1=JS, 2=ETC...)
	banner						: true,	//인증서 선택창 배너 이미지 노출 여부
	adminBanner					: true,	//인증서 관리창 배너 이미지 노출 여부
	logType						: "console",	//"console", "alert" or no log
	ServiceID					: "MagicLineWeb",
	CsServiceID					: "NTSMagicLine",
	SessionID					: "",
	MessageID					: "",
	CsUrl 						: "https://127.0.0.1:42235",
	AuthKey						: location.hostname.indexOf("ilovegohyang.go.kr") <= -1 ? "fuOjEPHAEXTD2iv9whxAz0mWuQDD3n/1rG4J5raigw8h1VxeK5aI+IVM8+ea3IVUpur0NXgxAfDSx831eemAVyDaR3C5jDrNIVDKiGQFJkA=" :"H0QveD8xICt+XJpFVwI2FgMrNkiujOMnyNXuyCQPzHRfgOiT8kR8S+BTarwto6eH5PNmLjQp7dITFg+iQpDzW5fwFRTJKIW4qVIUIUMaGwBDL+VWeCgtRl3jVupgf5Nka3O9b3XI6w3VdOAZ5MjZJA==",
	//CsUrl 						: "https://10.10.30.156:42235",
	PfxExportDownloadUrl		: ML_Default_Set.protocolType + "://" + ML_Default_Set.cpUrl + ML_Default_Set.mlDirPath + "install_bin/pfxexport/MagicPFXExport.exe",

	//-- UI
	passwordCountLimit			: 10, 	//TODO-비밀번호 오류 카운트. 횟 수  초과시 정책에 따라 조치.
	SIGN_OPTION					: {ds_pki_sign:['OPT_USE_CONTNET_INFO'], ds_pki_rsa:'rsa15', ds_pki_hash:'sha256'}, // 서명 기본 옵션
	ENVELOP_OPTION				: {ds_pki_rsa:'rsa15'}, // Envelop 기본 옵션
	SIGNENVELOP_OPTION			: {ds_pki_sign:['OPT_USE_CONTNET_INFO'], ds_pki_rsa:'rsa15', ds_pki_algo:'SEED-CBC'}, // Sign&Envelop 기본 옵션
	ASYMENCRYPT_OPTION			: {ds_pki_rsa:'rsa15'},
	ASYMDECRYPT_OPTION			: {ds_pki_rsa:'rsa15'},
	LANGUAGE					: "ko", //언어팩 옵션
	USEVIRTUALKEYBOARD			: false, 	//가상키보드 사용 여부 true,false
	VIRTUALKEYBOARDTYPE			: "", 	//가상키보드 사용 여부 NSHC,RAON,INCA.DREAM

	BROWSER_NOTICE_SHOW			: true, // 브라우저 저장소 클릭시 안내 이미지 출력 여부 true, false
	BROWSER_NOTICE_IMG			: true, // 브라우저 인증서 사용 안내문 출력 시 이미지는 true, 텍스트는 false

	eOption						: false, //true or false
	kOption						: "",	// "", "dream"

	//Client version
	WinClientVersion 			: "1.0.0.32",
	MacClientVersion 			: "1.0.0.20",
	Lin64ClientVersion 			: "1.0.0.12",
	Lin32ClientVersion 			: "1.0.0.12",

	MobileOption				: ["ubikey"], // ["ubikey", "mobisign", "dreamCS"]

	//2. CS 연동 관련
	CS_PORT						: "42235", // CS PORT("42235", "55533")
	CS_PORT_SELECT 				: "42235", // CS PORT 선택
	CS_UR						: "https://127.0.0.1:",
	//CS_UR						: "https://10.10.30.156:",
	CS_DOWNLOAD_WIN				: ML_Default_Set.protocolType + "//" + ML_Default_Set.mlMainUrl + ML_Default_Set.mlDirPath + "install_bin/magicline4nx_setup.exe",		//윈도우용 CS 설치 파일 주소
	CS_DOWNLOAD_MAC				: ML_Default_Set.protocolType + "//" + ML_Default_Set.mlMainUrl + ML_Default_Set.mlDirPath + "install_bin/MagicLine4NX.pkg",		//MAC용 CS 설치 파일 주소
	CS_DOWNLOAD_LINUX_FEDORA64	: ML_Default_Set.protocolType + "//" + ML_Default_Set.mlMainUrl + ML_Default_Set.mlDirPath + "install_bin/magicline4nx_x86_64.rpm",	//LINUX(Fedora/openSUSE) 64bit CS 설치 파일 주소
	CS_DOWNLOAD_LINUX_FEDORA32	: ML_Default_Set.protocolType + "//" + ML_Default_Set.mlMainUrl + ML_Default_Set.mlDirPath + "install_bin/magicline4nx_i386.rpm",	//LINUX(Fedora/openSUSE) 32bit CS 설치 파일 주소
	CS_DOWNLOAD_LINUX_UBUNTU64	: ML_Default_Set.protocolType + "//" + ML_Default_Set.mlMainUrl + ML_Default_Set.mlDirPath + "install_bin/magicline4nx_x86_64.deb",	//LINUX(Debian/Ubuntu) 64bit CS 설치 파일 주소
	CS_DOWNLOAD_LINUX_UBUNTU32	: ML_Default_Set.protocolType + "//" + ML_Default_Set.mlMainUrl + ML_Default_Set.mlDirPath + "install_bin/magicline4nx_i386.deb",	//LINUX(Debian/Ubuntu) 32bit CS 설치 파일 주소
	//CS_AUTHSERVER_URL			: "https://ml4web.dreamsecurity.com:8443/ML4-Web/ServerPage/jsp/",	//인증서버 URL
	CS_AUTHSERVER_URL			: location.protocol + '//' + location.host + ML_Default_Set.mlDirPath + "ServerPage/jsp/",	//인증서버 URL
	CS_AUTHSERVER_CERT			: "MIIEgjCCA+ugAwIBAgICB6AwDQYJKoZIhvcNAQEFBQAwRDELMAkGA1UEBhMCS1IxFjAUBgNVBAoTDURyZWFtU2VjdXJpdHkxDjAMBgNVBAsTBVdpcmVkMQ0wCwYDVQQDEwRST09UMB4XDTA0MDUxNzA2MDMwMloXDTA1MDUxNzA2MDMwMlowTjELMAkGA1UEBhMCS1IxFjAUBgNVBAoTDURyZWFtU2VjdXJpdHkxDjAMBgNVBAsTBVdpcmVkMRcwFQYDVQQDDA5BTllfMDAwMDAwMTM3NDCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA8oiK9s24U15Zc27yPSXvruwlgsngL9+dGiALMSG0ug3U9yUdJ+NqgBfMTvu2LE2EgoVObbwEWfdMCE8xmjlWVJzQRQATtvlGHiKXvAIwSjZq/gBIKLdKYmHQxBJk9NNE1nhHE6u0dlvVulNpDqO8hPj0P0OplWxHFZtEBpkSsDECAwEAAaOCAncwggJzMGwGA1UdIwRlMGOAFIwdeyOqAicpnNHLlhqKYmCRRZpBoUikRjBEMQswCQYDVQQGEwJLUjEWMBQGA1UEChMNRHJlYW1TZWN1cml0eTEOMAwGA1UECxMFV2lyZWQxDTALBgNVBAMTBFJPT1SCAQMwHQYDVR0OBBYEFL+l1vv7eUOivC/pmfP4xbZtjJW3MA4GA1UdDwEB/wQEAwIAMDB7BgNVHSAEdDByMHAGCiqDGoyaRAUCPAEwYjBgBggrBgEFBQcCAjBUMBQaDURyZWFtU2VjdXJpdHkwAwIBARo8VGhpcyBDZXJ0aWZpY2F0ZSBpcyBnZW5lcmF0ZWQgYnkgRHJlYW1zZWN1cml0eSBmb3IgQ0Fvc19YNTA5MCEGA1UdEQQaMBigFgYJKoMajJpECgEBoAkwBwwDQU5ZMAAwgecGA1UdHwSB3zCB3DBRoE+gTYZLbGRhcDovL2Nhb3MuZHJlYW1zZWN1cml0eS5jb206Mzg5L2NuPWNybDExZHAyLG91PVdpcmVkLG89RHJlYW1TZWN1cml0eSxjPUtSMIGGoIGDoIGAhn5odHRwOi8vY2Fvcy5kcmVhbXNlY3VyaXR5LmNvbS9jcmw/aWg9b0N4RlRlMWFtZGdoV3NrbnlGdmt2ZG4yRG9nJTNkJmRwPWNuJTNkY3JsMTFkcDIlMmNvdSUzZFdpcmVkJTJjbyUzZERyZWFtU2VjdXJpdHklMmNjJTNkS1IwSgYIKwYBBQUHAQEEPjA8MDoGCCsGAQUFBzABhi5odHRwOi8vY2Fvcy5kcmVhbXNlY3VyaXR5LmNvbToxNDIwMy9PQ1NQU2VydmVyMA0GCSqGSIb3DQEBBQUAA4GBACYZfFj6/Ixe3VViMpURAyYX7zBnoUDbCputCTuETzWMEuAc7/ciMGrnGVXitbRmpFlRINWnvDbrwHGF88xCRM1MTzEbLaBcDIMMCvIerUSW2/ocwd/vY6RRN38RAvVuYyNogbphVPaHJv85ivmdT47F7WcvyTz2XCAOJY5QJnJ5",
	CS_TIMEOUT					: 60,	//CS Timeout 시간 / 분단위
	CS_URL_SCHEME				: "ntsmagiclinenp://",
	CS_INSTALL_TYPE				: ML_Default_Set.cs_install_type, //설치페이지 사용여부 true/false/download (바로다운로드)
	CS_INSTALL_PAGE_URL			: ML_Default_Set.protocolType + "://" + ML_Default_Set.cpUrl + ML_Default_Set.mlDirPath + "install_page/ML4Web_Install.html",

	//4. 인증서 필터
	CERT_BASE_URL				: "",          // JSON 인증서 저장 경로 (브라우저 인증서)
	CERT_BASE_DIR				: "",          // 인증서 억세스 기본 DIR - (JSON형태, CS HDD 일때만 허용)
	CERT_FILTER_TYPE			: 15,          // '1':GPKI, '2':NPKI, '4':MPKI, '8:'PPKI(사설인증서)
	CERT_FILTER_USE_TYPE		: "",          // '1':서명용,암호화용, '2':서명용+암호화용 모두 있는경우만
	//CERT_FILTER_EXPIRE			: false,        // false:만료 인증서 보여주기, true:보여주지 않기
	//CERT_FILTER_OID				: "",          // OID 필터 (array)//1.2.410.100001.2.2.1,1.2.410.200005.1.1.4
	CERT_OID_NAME				: "",          // OID , OID 이름 (JSON) ex)1.2.410.200005.1.1.4
	CERT_ISSUER_NAME			: "",          // 인증서 발급사 매칭 (JSON) ex)signGATE CA2

	//unused path
	CERT_FILTER_PATH			: [], // 기본 [], [ "Program Files" ] : Program Files 경로 제외, [ "GPKI" ] : GPKI 경로 제외, [ "Users" ] : Users 경로 제외

	//5. 스마트인증CS 옵션
	SMARTCERT_TYPE				: ML_Default_Set.smartcert_type,	//C or JS
	CS_SMARTCERT_ServerIP		: "center.smartcert.kr",      // 중계서버 IP
	CS_SMARTCERT_ServerPort		: "443",                      // 중계서버 Port
	CS_SMARTCERT_SiteDomain		: "www.dreamsecurity.com",    // 도메인정보
	CS_SMARTCERT_InstallURL		: "http://ids.smartcert.kr",
	CS_SMARTCERT_SIZE			: "width=500, height=380",
	CS_SMARTCERT_Filter_Expired	: "1",
	CS_SMARTCERT_Filter_OID		: "NONE",
	CS_SMARTCERT_Filter_CA		: "NONE",
	CS_SMARTCERT_Filter_User	: "NONE",
	CS_SMARTCERT_HideTokenList	: false,       // 보안토큰 목록에서 스마트인증 숨기기
	CS_SMARTCERT_SignOrgView	: false,       // false:서명원문보여주지않기, true:서명원문보여주기
	CS_SMARTCERT_RaonSiteCode	: 609100014,                       // 라온사이트코드

	// 스마트인증NX 옵션
	WEB_SMARTCERT_MultisignYn		: 'N',
	WEB_SMARTCERT_Subject			: '',
	WEB_SMARTCERT_Issuer			: '',
	WEB_SMARTCERT_Serial			: '',
	WEB_SMARTCERT_Validate			: true,
	WEB_SMARTCERT_URL				: "", // "https://cdn.smartcert.kr/SmartCertWeb/API/js/jSmartCertNP2.js"

	//20200324 token filter
	TOKEN_FILTER					: [ "SmartSign" ],

	//6. UBIKEY 옵션
	CS_PHONE_TYPE				: "", // 휴대폰 종류
	CS_UBIKEY_Version			: "1.2.5.8", // 휴대폰 인증서 저장서비스 버전
	CS_UBIKEY_PopupURL			: "http://www.ubikey.co.kr/infovine/download.html", // UBIKEY 다운로드 주소
	CS_UBIKEY_wParam			: "INFOVINE", // UBIKEY 설정 값
	CS_UBIKEY_lParam			: "DREAMSECURITY|KINGS_INFOVINE", // UBIKEY 설정 값
	CS_UBIKEY_URL				: "", // /UBikeyWeb/js/infovineHTML.js

	//7. MobileKey 옵션
	CS_MOBILEKEY_PopupURL		: "http://www.mkey.kr/popup/mkey.htm",

	//8. 스마트카드
	CS_SMARTCARD_TYPE			: "",   //'1':금융카드, '2':공무원카드T0, '3':공무원카드T1, '4':마거

	//9. MOBISIGN 옵션
	CS_MOBISIGN_Version			: "5.0.5.0",
	CS_MOBISIGN_PopupURL		: "https://mobi.yessign.or.kr/mobisignInstall.htm", // MOBISIGN 다운로드 주소
	CS_MOBISIGN_SiteCode		: "6070059",

	//10. 금결원 공동인증서비스 옵션
	KFTC_SCRIPT_URL_RELAY		: "", //https://fidoweb.yessign.or.kr:3100/cloud/v2/relay.js
	KFTC_SCRIPT_URL_OPENCERT	: "", //https://fidoweb.yessign.or.kr:3100/cloud/v2/opencert.js
	KFTC_CORP_CODE				: "", //ex) 099
	KFTC_APIKEY					: "",

	DS_PKI_CERT_PATH			: {"GPKI":[], "NPKI":['INIPASS'], "MPKI":[], "PPKI":[]},
	DS_PKI_POLICY_OID			: {	 "1.2.410.200004.5.5.1.1":"범용(개인)"
									,"1.2.410.200004.5.5.1.2":"범용(기업)"
									,"1.2.410.200004.5.5.1.3.1":"제휴기관용(개인)"
									,"1.2.410.200004.5.5.1.4.1":"제휴기관용(기업)"
									,"1.2.410.200004.5.5.1.4.2":"전자세금용(기업)" },

	//11. 금융인증 SDK 옵션
	FINCERT_ORGCODE				: "",
	FINCERT_URL					: "",//https://t-4user.yeskey.or.kr/v1/fincertInt.js
	FINCERT_APIKEY				: "",

	//magicjs license
	MAGICJS_LIC					: "MIH1BAE2BAdNYWdpY0pTBBvtlZzqta3sp4Dsl63soJXrs7TqsJzrsJzsm5AEFkRTVFMtMTExMTEtMjQwOTMwLTAxNTgEAAQABAAEgYA3QjFEMEY4RUU3NTA5QkEyRDhGRjUzNDMyODZEQ0M1N0JBNTE5REE2N0Q3QjhFRTc0QzQxNkE2MDAxREREQjc3MDUyNTkzMTEyMEI2NjlGQkU0MjBBRDdFNkRFRUM1OTREMDEyRkVFOTU3RTg0Njg4NDQwMjFFMEQyNUY0QThDN6MpBCdpbG92ZWdvaHlhbmcuZ28ua3IsKi5pbG92ZWdvaHlhbmcuZ28ua3I=",

	result:"load"
}
var DS_SmartCertUISize = {
		miniSize : '390*400',
		defaultSize: '600*400'
};

var DS_SmartCert_Filter = {
		allowCA		:'CROSSCERT,KICA,SIGNKOREA,TRADESIGN,YESSIGN', 
		allowOID	:'',
		allowExpire	:false
};

var DS_SmartCert_SIGN_OPTION = {		
		SIGN_TYPE : "SIGNEDDATA_PKCS7",
		/*
			SIGNATURE 	// 서명값
			SIGNEDDATA_PKCS7	// PKCS#7에서 정의한 전자서명 형식
			SIGNEDDATA_CMS 		// CMS에서 정의한 전자서명 형식
			SIGNEDDATA_KOSCOM 	// 코스콤에서 정의한 전자서명 형식
		*/
		CONTENTS_TYPE: "OPTION_NONE",
		/*
			OPTION_NONE 			// 옵션을 지정하지 않음
			OPTION_NO_CONTENT_INFO	// 원본 메시지를 포함하지 않음	
		*/		
}

var DS_SmartCertCustomParams ={
		
		// 제휴사코드(발급신청서를 작성해서 드림시큐리티에 요청)
		cpCode:"18010000",
		
		// 스마트 인증 창 표시 사이즈
	    // 390*400 : DS_SmartCertUISize.miniSize
	    // 600*400 : DS_SmartCertUISize.defaultSize
		winSize:DS_SmartCertUISize.defaultSize,
		//winSize:DS_SmartCertUISize.miniSize,
		
		// 스마트인증 라이브러리  디렉토리경로
		//rootDirecoty:'http://mlweb.dreamsecurity.com:18443/ML4-Web/ML4WebSample/SmartCert',
		rootDirecoty:'http://localhost:8080/ML4-Web/ML4WebSample/SmartCert',
		
		// 스마트인증앱(휴대폰)에서 전자서명을 위해 접속하는 페이지
		//signURL:'http://mlweb.dreamsecurity.com:18443/ML4-Web/ML4WebSample/SmartCert/jsp/mobile/sample_tobeSign.jsp'				
		signURL:'http://localhost:8080/ML4-Web/ML4WebSample/SmartCert/jsp/mobile/sample_tobeSign.jsp'				
};


var DS_SmartCertCommonParams ={
		// 스마트 인증 호출 버튼 ID
		smartCertButtonID:'DS_iF_Smartcert_link',
		
		// 전자서명 오쳥 Form ( 스크립트에서 자동 생성)
		requestFormID:'DS_SmartCertRequestForm',
		signRequestText:'DS_SignRequestText',
		
		// 스마트 인증 공통창 DIV
		requestDivID:'DS_iF_Smartcert',
		// 스마트 인증 공통창 IFRAME
		requestFrameID:'DS_iF_Smartcert_frame',
		
		// Cross Domain 회피를 위한 스마트인증 요청 페이지 (기본값 : 배포 파일 )
		cpProxyURL:DS_SmartCertCustomParams.rootDirecoty+"/jsp/smatcertRequest.jsp?d=main",		
		//cpProxyURL:DS_SmartCertCustomParams.rootDirecoty+"/jsp/cubrid/smatcertRequest.jsp?d=main",
		
		// 스마트인증  UI URL (기본값 : 배포 파일 ) d=main:요청 ,d=end:완료
		requestWindowURL:'https://www.smartcert.kr:10443/nonPlugin/requestSmartCert.sc',
		//requestWindowURL:'http://10.10.30.57:8080/nonPlugin/requestSmartCert.sc',
								
		// Cross Domain 회피를 위한 스마트인증 결과 페이지 (기본값 : 배포 파일 )
		returnURL:DS_SmartCertCustomParams.rootDirecoty+'/jsp/smatcertResponse.jsp'
		//returnURL:DS_SmartCertCustomParams.rootDirecoty+'/jsp/cubrid/smatcertResponse.jsp'
};


//대체 가능 선택 라이브러리
{
	// jquery library
	// 
	if (window.jQuery) {  
	    // jQuery is loaded  
	} else {
	    // jQuery is not loaded
		document.write('<script type="text/javascript" src="https://www.smartcert.kr:10443/SmartCertWeb/CP/js/jquery-1.10.2.js"></script>');
	}
	
	// dialog library
	document.write('<script type="text/javascript" src="https://www.smartcert.kr:10443/SmartCertWeb/CP/js/SmartcertUI-1.1.js"></script>');
	// dialog library css
	document.write('<link type="text/css" rel="stylesheet" href="https://www.smartcert.kr:10443/SmartCertWeb/CP/css/Smartcert_jq_1.1.css">');
}

var DS_SmartCert_Dialog ={		 
	   
    // Dialog Area(DIV/IFrame) 생성
    createDialogArea:function(){
    	    	
    	var smartwebDiv=document.createElement('div');
    	smartwebDiv.id =DS_SmartCertCommonParams.requestDivID;
    	smartwebDiv.name =DS_SmartCertCommonParams.requestDivID;
    	smartwebDiv.style.display='none';		
		
		var smartwebFrame=document.createElement('iframe');
		smartwebFrame.id=DS_SmartCertCommonParams.requestFrameID;
		smartwebFrame.name=DS_SmartCertCommonParams.requestFrameID;
		smartwebFrame.style.border='none';	
		smartwebFrame.style.overflow='hidden';
		smartwebFrame.marginHeight=0;
		smartwebFrame.marginWidth=0
		smartwebFrame.scrolling='no';
		
		if(DS_SmartCertCustomParams.winSize=='390*400'){
			smartwebFrame.width='390';
			smartwebFrame.height='400';
		}else{
			smartwebFrame.width='600';
			smartwebFrame.height='400';
		}
		smartwebDiv.appendChild(smartwebFrame);
		document.body.appendChild(smartwebDiv);		
    },
    // SmarCert Dialog 초기화 함수
    init:function() {  
    	// 0. Dialog DIV Area 삽입
    	// 스마트 인증 Dialog 팝업을 위해 필요한 영역을 생성하며 , 별도 Area 구성시 Disable 가능하다.
    	DS_SmartCert_Dialog.createDialogArea();
    	
    	// 1. 스마트 공인인증 Form Element 삽입
    	// 스마트 인증 결과 확인을 위해 서비스 페이지  Submit 시 사용되며, 별도의 Form 사용시 Disable 가능하다. 
    	DS_InitSmartCertWeb();
    	
    	// 2. 스마트 공인인증 Dialog Library 초기화
    	// 스마트 인증 Dialog 팝업을 위해 필요한 영역에 팝업창을 구성하는 라이브러리로, 별도 Dialog 라이브러리 운영시 Disable 가능하다.
    	if(DS_SmartCertCustomParams.winSize=='390*400'){
			$('#'+DS_SmartCertCommonParams.requestDivID).SCweb_dialog({autoOpen: false, width: 390, height: 400, ly_id: DS_SmartCertCommonParams.requestDivID});
		}else{
			$('#'+DS_SmartCertCommonParams.requestDivID).SCweb_dialog({autoOpen: false, width: 600, ly_id: DS_SmartCertCommonParams.requestDivID});
		}    	
    },
    // SmarCert Dialog Open 함수
    showWindow:function() {
    	 $('#'+DS_SmartCertCommonParams.requestDivID).SCweb_dialog('open');
    	 $('#'+DS_SmartCertCommonParams.requestDivID).parent().css('z-index',120000);
    },
    // SmarCert Dialog Close 함수
    closeWindow:function(){
    	$('#'+DS_SmartCertCommonParams.requestDivID).SCweb_dialog('close');
    }
}

// 필수 라이브러리  - 스마트 인증 초기화 함수 동적 호출

document.write('<script type="text/javascript" src="js/smartcert/Smartcert_jq_Common_1.0.1.js"></script>');

//스마트인증 초기화
window.onload = DS_SmartCert_Dialog.init;




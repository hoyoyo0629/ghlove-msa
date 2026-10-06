		
	
	function DS_InitSmartCertWeb(){		
		var smartwebForm=document.createElement('form');
		smartwebForm.name =DS_SmartCertCommonParams.requestFormID;
		smartwebForm.id =DS_SmartCertCommonParams.requestFormID;
		
		var smartwebElement=document.createElement('input');
		smartwebElement.type='hidden';
		smartwebElement.id=DS_SmartCertCommonParams.signRequestText;
		smartwebElement.name=DS_SmartCertCommonParams.signRequestText;
		smartwebForm.appendChild(smartwebElement);
		
		var smartwebElement2=document.createElement('input');
		smartwebElement2.type='hidden';
		smartwebElement2.id='telco';
		smartwebElement2.name='telco';
		smartwebForm.appendChild(smartwebElement2);
		
		var smartwebElement3=document.createElement('input');
		smartwebElement3.type='hidden';
		smartwebElement3.id='phoneNumber';
		smartwebElement3.name='phoneNumber';
		smartwebForm.appendChild(smartwebElement3);
		
		var smartwebElement4=document.createElement('input');
		smartwebElement4.type='hidden';
		smartwebElement4.id='filter';
		smartwebElement4.name='filter';
		smartwebForm.appendChild(smartwebElement4);
		
		var smartwebElement5=document.createElement('input');
		smartwebElement5.type='hidden';
		smartwebElement5.id='signOpt';
		smartwebElement5.name='signOpt';
		smartwebForm.appendChild(smartwebElement5);
		
		document.body.appendChild(smartwebForm);			
	}	
			
	function DS_SmartcertRequest(tobeSignData, filter, signOpt, telco, phonenumber){
		
		DS_SmartCert_Dialog.showWindow();			   
		
	   var requestForm = document.forms[DS_SmartCertCommonParams.requestFormID];
	   
	   var requestTextElement = requestForm.elements[DS_SmartCertCommonParams.signRequestText];
	   
	   requestTextElement.value = encodeURIComponent(tobeSignData);
		  
	   // 통신사, 전화번호 입력시 파라미터로 전달
	   if(telco!=null && phonenumber!=null){
		   var requestTextElement2 = requestForm.elements['telco'];
		   requestTextElement2.value=telco;
		   var requestTextElement3 = requestForm.elements['phonenumber'];
		   requestTextElement3.value=phonenumber;
	   }
	   
	   requestForm.method='post';
	   requestForm.action=DS_SmartCertCommonParams.cpProxyURL ;
	   
	   requestForm.target=DS_SmartCertCommonParams.requestFrameID;	   
	   if(filter!=null)
		   requestForm.filter.value = filter;
	   
	   if(signOpt!=null)
		   requestForm.signOpt.value = signOpt;  
		  
	   requestForm.submit(); 

		return false;		 
	}	
	
	function DS_SmartCertPopup(query, telco, phoneNumber)
	{
		var smartwebForm = document.createElement('form');
		smartwebForm.name = 'certReqForm';

		var smartwebElement = document.createElement('input');
		smartwebElement.type = 'hidden';
		smartwebElement.name = 'signURL';
		smartwebForm.appendChild(smartwebElement);
		
		smartwebElement = document.createElement('input');
		smartwebElement.type = 'hidden';
		smartwebElement.name = 'returnURL';
		smartwebForm.appendChild(smartwebElement);
		
		smartwebElement = document.createElement('input');
		smartwebElement.type = 'hidden';
		smartwebElement.name = 'telco';
		smartwebForm.appendChild(smartwebElement);
		
		smartwebElement = document.createElement('input');
		smartwebElement.type = 'hidden';
		smartwebElement.name = 'phoneNumber';
		smartwebForm.appendChild(smartwebElement);
		
		smartwebElement = document.createElement('input');
		smartwebElement.type = 'hidden'; 
		smartwebElement.name = 'query';
		smartwebForm.appendChild(smartwebElement);
		
		smartwebElement = document.createElement('input');
		smartwebElement.type = 'hidden';
		smartwebElement.name = 'winSize';
		smartwebForm.appendChild(smartwebElement);
		
		document.body.appendChild(smartwebForm);
		
		document.certReqForm.signURL.value = window.location.protocol+"//"+window.location.host+DS_SmartCertCustomParams.signURL;
		//document.certReqForm.signURL.value = window.location.protocol+"//10.10.30.57:8443"+DS_SmartCertCustomParams.signURL;
		document.certReqForm.returnURL.value = window.location.protocol+"//"+window.location.host+DS_SmartCertCommonParams.returnURL;
		document.certReqForm.winSize.value = DS_SmartCertCustomParams.winSize;
		document.certReqForm.action = DS_SmartCertCommonParams.requestWindowURL+"?d=main";
		
		if(telco!=null && phoneNumber!=null)
		{
			document.certReqForm.telco.value = telco;
			document.certReqForm.phoneNumber.value = phoneNumber;
		}
						
		document.certReqForm.query.value = query + "&cpCode=" + DS_SmartCertCustomParams.cpCode;
		document.certReqForm.method = "post";
		document.certReqForm.submit();
	}
	
	function DS_SignComplete(trID)
	{	
		var smartwebForm = document.createElement('form');
		smartwebForm.name = 'SmartCertForm';

		
		var smartwebElement = document.createElement('input');
		smartwebElement.type = 'hidden';
		smartwebElement.name = 'cpCode';
		smartwebForm.appendChild(smartwebElement);
		
		smartwebElement = document.createElement('input');
		smartwebElement.type = 'hidden';
		smartwebElement.name = 'trID';
		smartwebForm.appendChild(smartwebElement);
		
		document.body.appendChild(smartwebForm);		
		
		document.SmartCertForm.method='post';
		document.SmartCertForm.cpCode.value = DS_SmartCertCustomParams.cpCode;
		document.SmartCertForm.trID.value=trID;
		document.SmartCertForm.action = DS_SmartCertCommonParams.requestWindowURL+'?d=end';
		document.SmartCertForm.submit();
	}
	
	
	function DS_SmartCertComplete(trID, returnCode, telco, phonenumber)
	{		
			parent.OnSmartCertComplete(trID, returnCode, telco, phonenumber);
						
	}
	
//	DS_SmartCert_Dialog.init();	

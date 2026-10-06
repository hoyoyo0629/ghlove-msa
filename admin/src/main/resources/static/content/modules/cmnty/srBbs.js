
/**
	코멘트
 */
let srBbs = {
	add : function() {
		Common.getEditorContent("bbsCn");
		let message = '등록 하시겠습니까?';

		if (!confirm(message)) return;

		let noticeYn = 'N';
		if  ($("#noticeYn1").is(":checked")) noticeYn = 'Y';

		if($("textarea[name='bbsCn']").val().trim() == '') {
			alert('내용을 입력 하십시오.');
			return;
		}

		let isSecret = 'N';
		if  ($("#isSecret1").is(":checked")) isSecret = 'Y';

		let cmntyBbsInfo =
			{
				  bbsTtl : $('#bbsTtl').val()
				, bbsCn : encodeURIComponent($("textarea[name='bbsCn']").val())
				, noticeYn :noticeYn
				, isSecret : isSecret
			};

		$.post("/opmanager/community/bbs/add", cmntyBbsInfo, function(response) {
			Common.responseHandler(response, function() {
				if(response.isSuccess) {
					alert(response.data);
					location.href = "/opmanager/community/srBbs/list";
				}
			});
		});
	}
}

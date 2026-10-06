/* AS-IS /content/modules/cmnty/bbs.js 를 그대로 가져온 것이다.
 * 바꾼 것은 호출 경로의 접두어뿐이다(/opmanager/community/... → /community/...).
 */

/**
	코멘트
 */
let bbs = {

	create: {
		init : function() {
			this.bindEventListener();
		}

		,bindEventListener : function() {

			//목록 버튼 클릭 시
			$('button.cancel')[0].addEventListener('click', () => {
				window.history.back();
			}, false);

			$('button.add')[0].addEventListener('click', () => {
				bbs.create.add();
			}, false);
		}

		,add : function() {
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

			$.post("/community/bbs/add", cmntyBbsInfo, function(response) {
				Common.responseHandler(response, function() {
					if(response.isSuccess) {
						alert(response.data);
						location.href = "/community/bbs/list";
					}
				});
			});
		}
	}

	//수정 화면
	,edit : {
		init : function() {
			this.bindEventListener();
		}

		,bindEventListener : function() {
			//목록 버튼 클릭 시
			$('button.cancel')[0].addEventListener('click', () => {
				let message = '작성 중인 내용을 취소하시겠습니까';
				if(confirm(message)){
					window.history.back();
				}
			}, false);

			$('button.update')[0].addEventListener('click', () => {
				bbs.edit.updateCmnt($('#bbsId').val());
			}, false);
		}

		,updateCmnt : function(bbsId) {
			Common.getEditorContent("bbsCn");
			let message = '수정 하시겠습니까?';

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
					  bbsId : bbsId
					, bbsTtl : $('#bbsTtl').val()
					, bbsCn : encodeURIComponent($("textarea[name='bbsCn']").val())
					, noticeYn :noticeYn
					, isSecret : isSecret
				};

			$.post("/community/bbs/update", cmntyBbsInfo, function(response) {
				Common.responseHandler(response, function() {
					if(response.isSuccess) {
						alert(response.data);
						location.href = "/community/bbs/list";
					}
				});
			});
		}
	}
}

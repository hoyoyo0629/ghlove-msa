/* AS-IS /content/modules/cmnty/faqBbsCmnt.js 를 그대로 가져온 것이다.
 * 바꾼 것은 호출 경로뿐이다(/opmanager/community/faqBbs/... → /community/faq-bbs/...).
 * AS-IS 오타 1건 고침: 게시글 삭제 URL이 deletefaqBbs(소문자 f)라 컨트롤러의 deleteFaqBbs와
 * 맞지 않았다. 이 핸들러는 상세화면 버튼에 .delete 클래스가 없어 실제로 바인딩되지 않는
 * 죽은 코드지만(삭제는 화면의 inline deleteItem이 처리한다), 잘못된 경로를 남겨두지 않는다.
 */

/**
	코멘트
 */
let cmnt = {

	init : function() {
		this.bindEventListener();
	}

	,bindEventListener : function() {

		//목록 버튼 클릭 시
		$('.btn-box button.list')[0].addEventListener('click', () => {
			location.href = '/community/faq-bbs/list';
		}, false);

		if($('.btn-box button.update')[0] != undefined) {
			//수정 버튼 클릭 시
			$('.btn-box button.update')[0].addEventListener('click', () => {
				location.href = '/community/faq-bbs/edit/'+$('#bbsId').val();
			}, false);
		}

		if($('.btn-box button.delete')[0] != undefined) {
			//삭제 버튼 클릭 시
			$('.btn-box button.delete')[0].addEventListener('click', () => {
				cmnt.deleteBbs($('#bbsId').val());
			}, false);
		}

		// 댓글 삭제 버튼 클릭 시
		Array.prototype.forEach.call($('#cmntList li button.delete'), function(ele) {
			ele.addEventListener('click', (event) => {
				cmnt.deleteCmnt(event.target.attributes.cmntId.value, ele.parentNode);
			}, false);
		});

		// 댓글 수정 버튼 클릭 시
		Array.prototype.forEach.call($('#cmntList li button.openUpdate'), function(ele) {
			ele.addEventListener('click', () => {
				$(ele.parentNode).find('.updateArea').toggle();
			}, false);
		});

		// 댓글 수정 완료 클릭 시
		Array.prototype.forEach.call($('#cmntList li button.commentButton2.update'), function(ele) {
			ele.addEventListener('click', (event) => {
				cmnt.updateCmnt(event.target.attributes.cmntId.value, ele.previousElementSibling);
			}, false);
		});
	}

	/**
		게시글 삭제
	 */
	,deleteBbs : function(bbsId) {
		let message = '게시물을 삭제하시겠습니까?';
		if (!confirm(message)) return;

		$.post("/community/faq-bbs/deleteFaqBbs/" + bbsId, {}, function(response) {
			Common.responseHandler(response, function() {
				if(response.isSuccess) {
					alert(response.data);
					location.href = "/community/faq-bbs/list";
				}
			});
		});
	}

	/**
		댓글 삭제
	 */
	,deleteCmnt : function(cmntId) {
		let message = '댓글을 삭제하시겠습니까?';
		if (!confirm(message)) return;

		$.post("/community/faq-bbs/cmnt/delete/" + cmntId, {}, function(response) {
			Common.responseHandler(response, function() {
				if(response.isSuccess) {
					alert(response.data);
					location.href = "/community/faq-bbs/detail/"+$('#bbsId').val();
				}
			});
		});
	}

	,updateCmnt : function(cmntId, element) {
		let message = '댓글을 수정하시겠습니까?';
		if (!confirm(message)) return;

		let cmntyCmntInfo = {cmntId : cmntId, cmntCn : element.value};
		$.post("/community/faq-bbs/cmnt/update", cmntyCmntInfo, function(response) {
			Common.responseHandler(response, function() {
				if(response.isSuccess) {
					alert(response.data);
					location.href = "/community/faq-bbs/detail/"+$('#bbsId').val();
				}
			});
		});
	}

}

/* AS-IS /content/modules/cmnty/srBbsCmnt.js 를 그대로 가져온 것이다.
 * 바꾼 것은 호출 경로뿐이다(/opmanager/community/srBbs/... → /community/sr-bbs/...).
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
			location.href = '/community/sr-bbs/list';
		}, false);

		if($('.btn-box button.update')[0] != undefined) {
			//수정 버튼 클릭 시
			$('.btn-box button.update')[0].addEventListener('click', () => {
				location.href = '/community/sr-bbs/edit/'+$('#bbsId').val();
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

		$.post("/community/sr-bbs/deleteSrBbs/" + bbsId, {}, function(response) {
			Common.responseHandler(response, function() {
				if(response.isSuccess) {
					alert(response.data);
					location.href = "/community/sr-bbs/list";
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

		$.post("/community/sr-bbs/cmnt/delete/" + cmntId, {}, function(response) {
			Common.responseHandler(response, function() {
				if(response.isSuccess) {
					alert(response.data);
					location.href = "/community/sr-bbs/detail/"+$('#bbsId').val();
				}
			});
		});
	}

	,updateCmnt : function(cmntId, element) {
		let message = '댓글을 수정하시겠습니까?';
		if (!confirm(message)) return;

		let cmntyCmntInfo = {cmntId : cmntId, cmntCn : element.value};
		$.post("/community/sr-bbs/cmnt/update", cmntyCmntInfo, function(response) {
			Common.responseHandler(response, function() {
				if(response.isSuccess) {
					alert(response.data);
					location.href = "/community/sr-bbs/detail/"+$('#bbsId').val();
				}
			});
		});
	}

}

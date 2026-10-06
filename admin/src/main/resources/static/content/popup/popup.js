var pop = {};


pop.openNotice = function (type) {
	$.get('/common/manager/notice/list', {type: type}, function (response) {
		var px = 0;
		if (response.isSuccess) {
			for (var i in response.data) {

				var d = response.data[i];

				if (i > 0) px = px + response.data[i - 1].width;
				 if(getCookie("divpop"+d.managerNoticeId) !="Y"){
					pop.openPopup(d, px, i);
				};



			}
		}

	});
}

function getCookie(name)
{
    var obj = name + "=";
    var x = 0;
    while ( x <= document.cookie.length )
    {
        var y = (x+obj.length);
        if ( document.cookie.substring( x, y ) == obj )
        {
            if ((endOfCookie=document.cookie.indexOf( ";", y )) == -1 )
                endOfCookie = document.cookie.length;
            return unescape( document.cookie.substring( y, endOfCookie ) );
        }
        x = document.cookie.indexOf( " ", x ) + 1;

        if ( x == 0 ) break;
    }
    return "";
}

pop.openPopup = function (d, location, idx) {
	var popup = window.open("/content/popup/list.html","popup" + idx,'left=' + location + 'px,width=' + d.width + 'px,height=' + d.height + 'px,scrollbars=yes;');


	setTimeout(function () {
		$(popup.document).find("#popup_title").html(d.subject);
	    $(popup.document).find("#popup_content").html(d.content);
	    $(popup.document).find("#popupId").val(d.managerNoticeId);
	},100);

	popup.addEventListener('load', function() {


	   $(popup.document).find("#popup_title").html(d.subject);
	   $(popup.document).find("#popup_content").html(d.content);
	   $(popup.document).find("#popupId").val(d.managerNoticeId);
	});
}

// opmanager 설문조사 임시 팝업
pop.openQustnrPopup = function (d, location, idx) {
	var popup = window.open("/content/popup/list.html","popup" + idx,'left=' + location + 'px,width=' + d.width + 'px,height=' + d.height + 'px,scrollbars=yes;');

	setTimeout(function () {
		$(popup.document).find("#popup_title").html(d.subject);
	    $(popup.document).find("#popup_content").html(d.content);
	    $(popup.document).find("#popup_content").css('padding', '0');
	    $(popup.document).find("#popupId").val(d.managerNoticeId);
	},100);

	popup.addEventListener('load', function() {


	   $(popup.document).find("#popup_title").html(d.subject);
	   $(popup.document).find("#popup_content").html(d.content);
	   $(popup.document).find("#popupId").val(d.managerNoticeId);
	});
}

// 공지사항 안내 팝업 + 쿠키 확인
pop.openInfoPopup = function (d, location, idx) {
	if(getCookie("divpop"+d.managerNoticeId) !="Y"){
		//pop.openPopup(d, location, idx);
		var popup = window.open("/content/popup/list.html","popup" + idx,'left=' + location + 'px,width=' + d.width + 'px,height=' + d.height + 'px,scrollbars=yes;');

		setTimeout(function () {
			$(popup.document).find("#popup_title").html(d.subject);
			$(popup.document).find("#popup_title").css('background', '#092c67');
		    $(popup.document).find("#popup_content").html(d.content);
		    $(popup.document).find("#popup_content").css('padding-left', '0px !important');
		    $(popup.document).find("#popup_content").css('padding-right', '0px !important');
		    //$(popup.document).find("#popup_content").style.setProperty("padding", "0", "important");
		    $(popup.document).find("#popupId").val(d.managerNoticeId);
		},100);

		popup.addEventListener('load', function() {


		   $(popup.document).find("#popup_title").html(d.subject);
		   $(popup.document).find("#popup_content").html(d.content);
		   $(popup.document).find("#popupId").val(d.managerNoticeId);
		});
	};
}

// 공지사항 안내 팝업 + 쿠키 확인 + 오늘하루보기없앰
pop.openInfoPopup2 = function (d, location, idx) {
	if(getCookie("divpop"+d.managerNoticeId) !="Y"){
		//pop.openPopup(d, location, idx);
		var popup = window.open("/content/popup/list2.html","popup" + idx,'left=' + location + 'px,width=' + d.width + 'px,height=' + d.height + 'px,scrollbars=yes;');

		setTimeout(function () {
			$(popup.document).find("#popup_title2").html(d.subject);
			$(popup.document).find("#popup_title2").css('background', '#434755');
		    $(popup.document).find("#popup_content2").html(d.content);
		    $(popup.document).find("#popup_content2").css('padding-left', '0px !important');
		    $(popup.document).find("#popup_content2").css('padding-right', '0px !important');
		    //$(popup.document).find("#popup_content").style.setProperty("padding", "0", "important");
		    $(popup.document).find("#popupId2").val(d.managerNoticeId);
		},100);

		popup.addEventListener('load', function() {


		   $(popup.document).find("#popup_title2").html(d.subject);
		   $(popup.document).find("#popup_content2").html(d.content);
		   $(popup.document).find("#popupId2").val(d.managerNoticeId);
		});
	};
}


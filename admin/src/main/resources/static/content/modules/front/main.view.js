$(function () {
	// LNB 그룹 아이템 / 배너 조회
	initLnbGroupBanners();

	// 메인 비주얼
	initBxSliderMainVisual();

	// MD 추천상품 조회
	initBxSliderAsync('md-items');

	// 신상품 조회
	initBxSliderAsync('new-items');

	initBxSliderAsync('spot-items');

	// BEST 추천 상품 조회
	initBxSliderAsync('best-items', {
		maxSlides: 6,
		slideWidth: 159,
		slideMargin: 25,
		infiniteLoop: false,
		hideControlOnEnd: false,
		pager: false,
		touchEnabled: (navigator.maxTouchPoints > 0)
	});

	// 공지사항
	bindNotice();

	displayPopup();
});

// LNB 그룹 아이템 / 배너 조회
function initLnbGroupBanners() {
	$.post('/main/lnb-group-banners', function (html) {
		$('.lnb_group').each(function () {
			var groupBannerKey = $(this).data('lnb-group-banner-key');
			//alert(groupBannerKey);
			var groupBanner = $(html).find('.' + groupBannerKey).html();
			$(this).append(groupBanner);
		});
	}, 'html');
}


function initBxSliderMainVisual() {

	var slider = $('#op-main-visual .op-bxslider').bxSlider({
		pagerCustom: '#bx-pager2',
		auto: true,
		autoControls: false,
		controls: false
	});

	$('a.pager-prev').click(function () {
		var current = slider.getCurrentSlide();
		slider.goToPrevSlide(current) - 1;
	});
	$('a.pager-next').click(function () {
		var current = slider.getCurrentSlide();
		slider.goToNextSlide(current) + 1;
	});
}


// 비동기 상품 조회 및 bxSlider 실행
var BEST_ITEM_SLIDERS = [];

function initBxSliderAsync(contentCode, options) {
	var bxOptions = {
		pager: false,
		slideWidth: 223,
		maxSlides: 4,
		slideMargin: 50,
		auto: true,
		autoControls: false,
		touchEnabled: (navigator.maxTouchPoints > 0)
	};
	if (options !== undefined) {
		bxOptions = options;
	}

	var requestUri = '/main/' + contentCode;
	var $selector = $('#op-main-' + contentCode);

	$.post(requestUri, function (html) {
		$selector.empty().append(html);
		var $bxslider = $selector.find('.op-bxslider');

		Ga.frontImpressionBySelector($selector, getListNameByContentCode(contentCode), '');

		// best추천상품 탭이벤트 핸들러
		if (contentCode == 'best-items') {
			$bxslider.each(function (i) {
				BEST_ITEM_SLIDERS[i] = $(this).bxSlider(bxOptions);
			});
			bestItemEventHandler();
		} else {
			if ($bxslider.find('.slide').length >= bxOptions.maxSlides) {
				$bxslider.bxSlider(bxOptions);
			}

		}
	}, 'html');
}

function getListNameByContentCode(contentCode) {
	var listName = '';

	switch (contentCode) {
		case 'md-items' : listName = '메인 MD'; break;
		case 'new-items' : listName = '메인 신상품'; break;
		case 'spot-items' : listName = '메인 스팟'; break;
		case 'best-items' : listName = '메인 베스트'; break;
		default:
	}

	return listName;
}

function redirectItem(contentCode, itemUserCode, link) {

	var selectorId = '#'+contentCode+'-'+itemUserCode;
	var $selector = $(selectorId);

	if ($selector.size() > 0) {

		try {
			Ga.frontSelectBySelector($selector, getListNameByContentCode(contentCode), '');
		} catch (e) {
			let errBln = true;
		}
	}

	location.href=link;
}

function bestItemEventHandler() {
	var $ui_tabs_contents_wrap = $('.ui_tab_content');
	$(document).on('click', '.best_tab a', function (e) {
		e.preventDefault();
		var index = $('.best_tab a').index($(this));

		$('.best_tab a').removeClass('current');
		$(this).addClass('current');
		$ui_tabs_contents_wrap.hide().eq(index).show();

		BEST_ITEM_SLIDERS[index].reloadSlider();
	});
}

function bindNotice() {
	$.post('/main/notice', function (html) {
		$('#op-main-notice').empty().append(html);
	}, 'html');
}

//레이어 팝업관련
function popClose(popNo) {
	$('#popup_' + popNo).hide();
}

function closeWin(popNo) {
	setCookie(popNo);
	$('#popup_' + popNo).hide();
}

function setCookie(popNo) {
	var expire = new Date();

	expire.setDate(expire.getDate() + 1);
	document.cookie = "popup_check_" + popNo + "=1; expires=" + expire.toGMTString() + "; path=/";
}

function openPopup(popupId, width, height, topPosition, leftPosition, popupType) {
//임시로 쿠키제거
//$.cookie('popup_check_'+popupId, null);
	var ck_popup = $.cookie('popup_check_' + popupId);

	if (popupType == 2 && ck_popup != 1) {
		$('#popup_' + popupId).show();
		return;
	} else if (popupType == 1 && ck_popup != 1) {
		Common.popup(url("/popup/index/" + popupId), 'openPopup' + popupId, width, height, 0, leftPosition, topPosition);
	}
}

function displayPopup() {

	$.get('/main/popup', null, function (response) {
		Common.responseHandler(response, function (response) {

			makePopup(response.data);
		});
	});

	function makePopup(data) {
		try {

			var openPopupParams = []

			if (typeof data !== 'undefined' && data != null) {
				for (var i = 0; i < data.length; i++) {
					var popup = data[i];
					openPopupParams.push(getPopupParam(popup));
					if ('2' == popup.popupType) {
						makeLayerPopup(popup);
					}
				}
			}

			if (openPopupParams.length > 0) {
				for (var i = 0; i < openPopupParams.length; i++) {
					var param = openPopupParams[i];
					openPopup(param.popupId, param.width + 17, param.height + 117, param.topPosition, param.leftPosition, param.popupType);
				}
			}

		} catch (e) {
			let errBln = true;
		}
	}

	function makeLayerPopup(popup) {
		var template = $('#popupTemplate').html(),
			$popupArea = $('#popupArea'),
			rawHtml = '';

		rawHtml = template;

		var popupStyle = 'position:absolute;' +
			'left:' + popup.leftPosition + 'px;' +
			'top:' + popup.topPosition + 'px;' +
			'z-index:100;' +
			'width:' + (popup.width + 20) + 'px;' +
			'height:' + popup.height + 'px;' +
			'display:none;';

		var handleStyle = 'width:' + (popup.width + 20) + 'px;' +
			'height:15px;cursor:move;background:' + popup.backgroundColor + ';';

		var contentStyle = 'width:' + (popup.width + 20) + 'px;background:white';

		var popupContent = '';
		var imageLink = popup.imageLink,
			popupImage = popup.popupImage,
			popupImageSrc = popup.popupImageSrc,
			content = popup.content;

		if (typeof imageLink !== 'undefined' && imageLink != '') {
			popupContent += '<a href="' + imageLink + '">';
		}

		if (popupImage != null && typeof popupImage !== 'undefined' && popupImage != '') {

			popupContent += '<img src="' + popupImageSrc + '" border="0" />';
		} else {
			popupContent += content;
		}

		if (typeof imageLink !== 'undefined' && imageLink != '') {
			popupContent += '</a>';
		}

		rawHtml = rawHtml.replaceAll('{{popupId}}', popup.popupId)
			.replaceAll('{{popupStyle}}', popupStyle)
			.replaceAll('{{handleStyle}}', handleStyle)
			.replaceAll('{{contentStyle}}', contentStyle)
			.replaceAll('{{width}}', popup.width)
			.replaceAll('{{height}}', popup.height)
			.replaceAll('{{popupContent}}', popupContent);

		$popupArea.append(rawHtml);
	}

	function getPopupParam(popup) {

		return {
			popupId: popup.popupId,
			width: popup.width,
			height: popup.height,
			topPosition: popup.topPosition,
			leftPosition: popup.leftPosition,
			popupType: popup.popupType
		}
	}
}

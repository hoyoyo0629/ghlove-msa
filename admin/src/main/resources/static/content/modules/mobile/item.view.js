$(function () {

	// 상품 수량 체크 이벤트 (옵션 없는 경우)
	initItemQuantityEvent();

	// 상품 옵션 선택 추가 이벤트
	initItemOptionEvent();

	// 세트상품 선택 추가 이벤트
	initItemSetEvent();

	// 최초계산
	calculate();

	// 리뷰조회
	paginationReviewMore(1);

	// QNA조회
	paginationQnaMore(1);

	updateItemHits();

	viewItemRelations();
	setBenefitInfo();
	setCustomerInfo();

});

//상품 조회 수 업데이트.
function updateItemHits() {
	$.post('/common/update-item-hits', {'itemId': item.itemId}, function () {
	});
}

//상품 수량 체크 이벤트 (옵션이 없는 경우)
function initItemQuantityEvent() {
	var isSet = item.itemType === '3';
	var $itemQuantity = $('.op-item-quantity');

	if ($itemQuantity.size() > 0) {

		// 상품 수량 +
		$itemQuantity.find('a.op-item-order-count-plus').on('click', function (e) {
			e.preventDefault();
			var $quantity = $('.quantity');
			var quantity = Number($quantity.val());
			var addedQuantity = quantity + 1;

			if (isSet) {
				$quantity.val(addedQuantity);

				// 재고 체크
				if (isAvailableSetStock('plus')) {
					// update set quantity
					$('input[name="itemSets[0].quantity"]').val(addedQuantity);
				}
			} else {
				if (quantity === item.orderMaxQuantity) {
					alert('최대 구매 가능 수량은 ' + item.orderMaxQuantity + '개 입니다.');
					return;
				}

				if (addedQuantity > 999) {
					alert("해당 상품의 최대 구매 수량은 999개 입니다.");
					return;
				}

				// 재고체크
				if (item.stockFlag == 'Y' && addedQuantity > item.stockQuantity) {
					alert('해당 상품의 최대 구매 수량은 ' + item.stockQuantity + '개 입니다.');
					return;
				}

				$quantity.val(addedQuantity);

				// append ArrayRequiredItems
				setArrayRequiredItems(item.itemId, addedQuantity);
			}

			calculate();
		});

		// 상품 수량 -
		$itemQuantity.find('a.op-item-order-count-minus').on('click', function (e) {
			e.preventDefault();
			var $quantity = $('.quantity');
			var quantity = Number($quantity.val());
			var minusQuantity = quantity - 1;
			var defaultQuantity = 1;

			if (isSet) {
				// 최소구매 수량 체크
				if (quantity === defaultQuantity) {
					alert('해당 상품의 최소 구매 수량은 ' + defaultQuantity + "개 입니다.");
					return;
				}

				// update set quantity
				$('input[name="itemSets[0].quantity"]').val(minusQuantity);
			} else {
				if (item.orderMinQuantity > 0) {
					defaultQuantity = item.orderMinQuantity;
				}

				// 최소구매 수량 체크
				if (quantity == defaultQuantity) {
					alert('해당 상품의 최소 구매 수량은 ' + defaultQuantity + "개 입니다.");
					return;
				}

				$quantity.val(minusQuantity);

				// append ArrayRequiredItems
				setArrayRequiredItems(item.itemId, minusQuantity);
			}

			calculate();
		});

		// 상품 수량 입력.
		$('.quantity').on('blur', function (e) {
			var $quantity = $(this);
			var defaultQuantity = 1;
			var quantity = Number($quantity.val());

			if (isSet) {
				// 재고 체크
				if (isAvailableSetStock('manual')) {
					// update set quantity
					$('input[name="itemSets[0].quantity"]').val(quantity);
				}
			} else {
				if (item.orderMinQuantity > 0) {
					defaultQuantity = item.orderMinQuantity;
				}

				// 최소구매 수량 체크
				if (item.orderMaxQuantity > 0 && quantity < defaultQuantity) {
					alert('해당 상품의 최소 구매 수량은 ' + defaultQuantity + "개 입니다.");
					$quantity.val(item.orderMinQuantity);

					// append ArrayRequiredItems
					setArrayRequiredItems(item.itemId, defaultQuantity);

					calculate();
					return;
				}

				// 최대구매 수량 체크.
				if (quantity > item.orderMaxQuantity) {
					alert('해당 상품의 최대 구매 수량은 ' + item.orderMaxQuantity + '개 입니다.');
					$quantity.val(item.orderMaxQuantity);

					calculate();
					return;
				}

				// 재고 체크
				if (item.stockFlag == 'Y' && quantity > item.stockQuantity) {
					alert('해당 상품의 최대 구매 수량은 ' + item.stockQuantity + '개 입니다.');
					$quantity.val(item.stockQuantity);

					calculate();
					return;
				}

				// append ArrayRequiredItems
				setArrayRequiredItems(item.itemId, quantity);
			}

			calculate();
		}).on("keyup", function (e) {
			var pattern = /^[0-9]+$/;
			if (!pattern.test($(this).val())) {
				$(this).val('1');
				calculate();
				return;
			}

			if (!isSet) {
				var quantity = Number($(this).val());
				if (quantity > item.orderMaxQuantity) {
					alert('최대 구매 가능 수량은 ' + item.orderMaxQuantity + '개 입니다.');
					$(this).val(item.orderMaxQuantity);
					calculate();
				}
			}
		});
	}
}

// 상품 옵션 선택 추가 이벤트
function initItemOptionEvent() {
	var SELECTED_OPTION_IDS = [];

	// init
	if (item.itemType === '3' || item.itemOptionFlag === 'N') {
		return;
	}

	// 옵션 생성.
	initItemOption();

	// 전체 옵션 보기 버튼.
	displayAllOptionButton();

	// 상품필수옵션 셀렉트박스 클릭
	$('.op-option-select-box').on('click', function (e) {
		e.preventDefault();

		// 최대 구매 수량 체크
		var currentQuantity = getCurrentItemQuantity();

		if (currentQuantity == item.orderMaxQuantity) {
			alert('최대 구매 가능 수량은 ' + item.orderMaxQuantity + '개 입니다.');
			return;
		}

		// 옵션 선택 초기화.
		initItemOption();

		$('.op-option-box').hide();
		$(this).closest('div').find('.op-option-box').show();
		$(this).closest('div').find('.op-option-box ul').hide().eq(0).show();
		$(this).closest('div').find('.op-option-box ul.option_list02').hide().eq(0).show();

	});

	// 옵션 선택 박스 닫기
	$('.op-close-option-box').on('click', function (e) {
		e.preventDefault();

		$('.op-option-box').hide();

		// 옵션 선택 초기화.
		initItemOption();
	});


	// 전체옵션보기 버튼 이벤트
	$('.btn-option-box-type').on('click', function (e) {
		e.preventDefault();

		var $itemOptionInfo = $('.op-item-option-info');
		var $optionBoxType1 = $itemOptionInfo.find('.op-option-box-type1');
		var $optionBoxType2 = $itemOptionInfo.find('.op-option-box-type2');

		if ($optionBoxType1.css('display') == 'none') {
			$(this).text('전체옵션보기');
			$optionBoxType1.show();
			$optionBoxType2.hide();

		} else {
			$(this).text('이전방식보기');
			$optionBoxType1.hide();
			$optionBoxType2.show().find('ul').show();

		}
	});


	// 세부 옵션 선택 이벤트.
	$('.op-item-option-info .op-option-box').on('click', '.op-option-group li a', function (e) {
		e.preventDefault();

		var $optionBoxType = $(this).closest('.op-option-group').parent();
		var $optionGroup = $(this).closest('.op-option-group');
		var currentOptionGroupIndex = $optionBoxType.find('.op-option-group').index($optionGroup);
		var optionGroupCount = $optionBoxType.find('.op-option-group').size();
		var optionName = $(this).attr('data-option-name');

		// 옵션 선택 처리.
		$(this).addClass('on');
		$optionGroup.find('.option-selected-name').text(optionName);


		var optionSelectedCount = $optionBoxType.find('a.on').size();

		// 모든 옵션을 선택한 경우.
		if (optionGroupCount == optionSelectedCount) {
			completeSelectOption();

		} else {
			displayNextOptionGroup(currentOptionGroupIndex);

		}

		// 다음 옵션 보기.
		function displayNextOptionGroup(currentIndex) {
			var index = currentIndex + 1;

			// S2,S3는 다음 옵션을 조회해서 가져와야 한다.
			if (item.itemOptionType != 'S') {

				// 이전 까지 선택한 옵션명 가져오기.
				var optionNames = [];
				var i = 0;
				$optionBoxType.find('.op-option-group:lt(' + index + ')').each(function () {
					optionNames[i] = $(this).find('a.on').attr('data-option-name');
					i++;
				});

				// next 옵션 목록
				var optionHtml = '';
				var lastOptionValue = '';
				for (var i = 0; i < itemOptions.length; i++) {
					if (itemOptions[i].optionType == 'T') {
						continue;
					}

					var step = optionNames.length + 1;
					var optionValue = '';

					if (optionNames.length == 1 && itemOptions[i].optionName1 == optionNames[0]) {
						optionValue = itemOptions[i].optionName2;

					} else if (optionNames.length == 2 && itemOptions[i].optionName1 == optionNames[0] && itemOptions[i].optionName2 == optionNames[1]) {
						optionValue = itemOptions[i].optionName3;

					} else {
						continue;
					}


					if (optionValue != lastOptionValue) {

						// 마지막 옵션인 경우 옵션 정보까지 보여 줘야 함.
						if (step == optionGroupCount) {
							optionHtml += '		<li>' + getOptionInfo(itemOptions[i], step) + '</li>';
						} else {
							optionHtml += '		<li><a href="javascript:;" data-option-name="' + optionValue + '">' + getOptionImage(optionValue) + ' ' + optionValue + '</a><li>';
						}

						lastOptionValue = optionValue;
					}

				}


				// next 옵션 목록 추가.
				$optionBoxType.find('.op-option-group').eq(index).find('ul').append(optionHtml);
				$optionBoxType.find('.op-option-group').eq(index).find('.option-selected-name').text('');

			}
			$optionBoxType.find('.op-option-group').eq(currentIndex).find('ul').hide();
			$optionBoxType.find('.op-option-group').eq(index).find('ul').show();


		}

		// 옵션 선택 완료
		function completeSelectOption() {
			SELECTED_OPTION_IDS = [];
			var index = 0;
			var optionAdditionalAmount = 0;
			var optionNames = '';
			$optionBoxType.find('a.on').each(function (i) {
				var optionId = $(this).attr('data-option-id');

				if (optionId !== undefined) {
					SELECTED_OPTION_IDS[index] = optionId;
					index++;
				}

				var optionName = $(this).attr('data-option-name');
				if (optionName !== undefined) {
					if (optionNames != '') {
						optionNames += ' | ';
					}
					optionNames += optionName;
				}

				var optionPrice = $(this).attr('data-option-price');
				if (optionPrice !== undefined && optionPrice != '') {
					optionAdditionalAmount += Number(optionPrice);
				}
			});

			// 텍스트 옵션이 없는 경우 바로 추가.
			if ($('.text-option-id').size() == 0) {
				// 선택된 옵션으로 아이템옵션을 추가한다.
				addItemOption();
			} else {
				var additionAmountInfo = '';
				if (optionAdditionalAmount != 0) {
					additionAmountInfo = ' (' + (optionAdditionalAmount > 0 ? '+' : '') + Common.numberFormat(optionAdditionalAmount) + '원)';
				}

				var selectedOptionSummary = optionNames + additionAmountInfo;


				$('.op-option-box').hide();

				$('.op-option-select-box').text(selectedOptionSummary);
				$('.op-option-select-box').addClass('selected');

			}
		}
	}).on('mouseenter', '.option-group li a', function (e) {
		e.preventDefault();
		var optionName = $(this).attr('data-option-name');
		$(this).closest('.option-group').find('.option-selected-name').text(optionName);

	}).on('mouseleave', '.option-group li a', function (e) {
		e.preventDefault();
		if (!$(this).hasClass('on')) {
			$(this).closest('.option-group').find('.option-selected-name').text('');
		}

	});


	// 상품 옵션 그룹 링크
	$('.op-option-box-type1').on('click', '.op-option-group > a', function (e) {
		e.preventDefault();

		var $optionGroup = $(this).closest('.op-option-group');
		var $optionBoxType = $('.op-option-box-type1');
		var currentOptionGroupIndex = $optionBoxType.find('.op-option-group').index($optionGroup);

		if (item.itemOptionType == 'S') {
			var $previousOptionGroups = $optionBoxType.find('.op-option-group:lt(' + currentOptionGroupIndex + ')');
			var optionGroupCount = $previousOptionGroups.size();
			var selectedOptionGroupCount = $previousOptionGroups.find('a.on').size();

			if (selectedOptionGroupCount < optionGroupCount) {
				alert('위의 정보를 먼저 선택해 주세요.');
				return;
			}

			$optionBoxType.find('ul').hide();
			$optionGroup.find('a.on').removeClass('on');
			$optionGroup.find('.option-selected-name').text('');
			$optionGroup.find('ul').show();

			// 현재 이후 STEP 초기화.
			$optionBoxType.find('.op-option-group:gt(' + currentOptionGroupIndex + ')').each(function () {
				$(this).find('a.on').removeClass('on');
				$(this).find('.option-selected-name').text('위의 정보를 먼저 선택해 주세요.');
			});

		} else {
			if ($optionGroup.find('li').size() == 0) {
				alert('위의 정보를 먼저 선택해 주세요.');
				return;
			}

			$optionBoxType.find('ul').hide();
			$optionGroup.find('a.on').removeClass('on');
			$optionGroup.find('.option-selected-name').text('');
			$optionGroup.find('ul').show();

			// 현재 이후 STEP 초기화.
			$optionBoxType.find('.op-option-group:gt(' + currentOptionGroupIndex + ')').each(function () {
				$(this).find('a.on').removeClass('on');
				$(this).find('.option-selected-name').text('위의 정보를 먼저 선택해 주세요.');
				$(this).find('li').remove();
			});

		}
	});

	// 텍스트 옵션 추가
	$('.op-item-option-info').on('click', '.btn-add-item-option', function (e) {
		e.preventDefault();

		// 선택 옵션이 있는 경우
		if (item.itemOptionType != 'T' && SELECTED_OPTION_IDS.length == 0) {
			alert('상품필수옵션을 선택해 주세요.');
			$('.op-option-select-box').focus();
			return;
		}


		var errorIndex = -1;
		$('.text-option-value').each(function (index) {
			if ($.trim($(this).val()) == '') {
				errorIndex = index;
				return false;
			}

		});

		if (errorIndex > -1) {
			var $textOption = $('.text-option-value').eq(errorIndex);
			alert($textOption.attr('title') + ' 항목을 입력해 주세요.');
			$textOption.focus();
			return;
		}


		// 옵션 추가하기!!
		addItemOption();

	});


	// 옵션 구성 (초기화)
	initItemOptionEvent.initItemOption = initItemOption;
	function initItemOption() {
		if (item.itemOptionType == 'T') {
			return;
		}

		// 이전 선택 부분 초기화.
		SELECTED_OPTION_IDS = [];

		$('.op-option-select-box').removeClass('selected');

		var $itemOptionInfo = $('.op-item-option-info');
		$itemOptionInfo.find('.op-selected-option').text('선택하세요.');
		var $optionBoxType1 = $itemOptionInfo.find('.op-option-box-type1');

		$('.op-item-option-info .op-option-group li a').removeClass('on');

		$('.op-option-select-box').text('상품 필수 옵션을 선택하세요.');

		var optionGroupHtml = '';

		if (item.itemOptionType == 'S') {
			var groupTitle = '';
			for (var i = 0; i < itemOptions.length; i++) {
				if (itemOptions[i].optionType == 'T') {
					continue;
				}

				if (itemOptions[i].optionName1 != groupTitle) {
					if (i > 0) {
						optionGroupHtml += '	</ul>';
						optionGroupHtml += '</div>';
					}

					optionGroupHtml += '<div class="option_list01 op-option-group">';
					optionGroupHtml += '	<a href="javascript:;"><span>' + itemOptions[i].optionName1 + ' <label class="option-selected-name">선택하세요.</label></span></a>';
					optionGroupHtml += '	<ul class="option_list02">';
				}

				// 옵션항목.
				optionGroupHtml += '		<li>' + getOptionInfo(itemOptions[i]) + '</li>';

				groupTitle = itemOptions[i].optionName1;

			}

			optionGroupHtml += '	</ul>';
			optionGroupHtml += '</div>';

		} else {

			var optionGroupHtml = '';

			// option1
			optionGroupHtml += '<div class="option_list01 op-option-group">';
			optionGroupHtml += '	<a href="javascript:;"><span>' + item.itemOptionTitle1 + ' <label class="option-selected-name">선택하세요.</label></span></a>';
			optionGroupHtml += '	<ul class="option_list02">';

			var options = '';
			var optionName1 = '';
			for (var i = 0; i < itemOptions.length; i++) {
				if (itemOptions[i].optionType == 'T') {
					continue;
				}

				if (itemOptions[i].optionName1 != optionName1) {
					optionGroupHtml += '		<li><a href="javascript:;" data-option-name="' + itemOptions[i].optionName1 + '">' + getOptionImage(itemOptions[i].optionName1) + ' ' + itemOptions[i].optionName1 + '</a><li>';
				}

				optionName1 = itemOptions[i].optionName1;
			}

			optionGroupHtml += '	</ul>';
			optionGroupHtml += '</div>';


			// option2
			optionGroupHtml += '<div class="option_list01 op-option-group">';
			optionGroupHtml += '	<a href="javascript:;"><span>' + item.itemOptionTitle2 + ' <label class="option-selected-name">위의 정보를 먼저 선택해 주세요.</label></span></a>';
			optionGroupHtml += '	<ul class="option_list02">';
			optionGroupHtml += '	</ul>';
			optionGroupHtml += '</div>';

			if (item.itemOptionType == 'S3') {
				// option3
				optionGroupHtml += '<div class="option_list01 op-option-group">';
				optionGroupHtml += '	<a href="javascript:;"><span>' + item.itemOptionTitle3 + ' <label class="option-selected-name">위의 정보를 먼저 선택해 주세요.</label></span></a>';
				optionGroupHtml += '	<ul class="option_list02">';
				optionGroupHtml += '	</ul>';
				optionGroupHtml += '</div>';
			}
		}

		$optionBoxType1.find('.op-option-group').remove();
		$optionBoxType1.append(optionGroupHtml);
		$optionBoxType1.show();
	}


	// 상품 옵션 텍스트 구성
	function getOptionInfo(itemOption, step) {
		var info = '';
		var optionName = itemOption.optionName2;

		if (itemOption.optionType != 'S') {
			if (step == 1) optionName = itemOption.optionName1;
			if (step == 2) optionName = itemOption.optionName2;
			if (step == 3) optionName = itemOption.optionName3;
		}


		info += optionName;

		// 추가금
		if (itemOption.optionPrice != 0) {
			info += ' (' + (itemOption.optionPrice > 0 ? '+' : '') + Common.numberFormat(itemOption.optionPrice) + '원)';
		}

		// 품절, 재고연동
		if (itemOption.isSoldOut == 'true' || (itemOption.optionStockFlag == 'Y' && itemOption.optionStockQuantity > 0 && item.orderMinQuantity > itemOption.optionStockQuantity)) {
			info += ' - 품절';
		} else if (itemOption.optionStockFlag == 'Y' && itemOption.optionStockQuantity > 0) {
			info += ' | 재고: ' + Common.numberFormat(itemOption.optionStockQuantity) + '개';
		}

		if (itemOption.isSoldOut == 'true' || (itemOption.optionStockFlag == 'Y' && itemOption.optionStockQuantity > 0 && item.orderMinQuantity > itemOption.optionStockQuantity)) {
			return '<span>' + getOptionImage(optionName, itemOption.itemOptionId) + ' ' + info + '</span>';
		} else {
			return '<a href="javascript:;" data-option-name="' + optionName + '" data-option-id="' + itemOption.itemOptionId + '" data-option-price="' + itemOption.optionPrice + '">' + getOptionImage(optionName, itemOption.itemOptionId) + ' ' + info + '</a>';
		}
	}

	// 옵션 이미지 가져오기
	function getOptionImage(optionName, itemOptionId) {
		// optionId가 있는 경우.
		if (itemOptionId !== undefined && itemOptionId > 0) {
			for (var i = 0; i < itemOptionImages.length; i++) {
				if (itemOptionImages[i].itemOptionId == itemOptionId) {
					return '<img src="' + itemOptionImages[i].optionImage + '" alt="' + optionName + '" />';
				}
			}
		}

		for (var i = 0; i < itemOptionImages.length; i++) {
			if (itemOptionImages[i].optionName == optionName) {
				return '<img src="' + itemOptionImages[i].optionImage + '" alt="' + optionName + '" />';
			}
		}

		return "";
	}

	// 전체 옵션 보기
	function displayAllOptionButton() {
		if (item.itemOptionType == 'S2' || item.itemOptionType == 'S3') {
			$('.btn-option-box-type').show();
		}


		//
	}


	// 선택완료된 옵션으로 옵션 아이텝을 추가한다.
	function addItemOption() {

		var option = '';
		var optionText = '';
		var optionData = '';
		var optionCount = 0;
		var optionAdditionAmount = 0;
		var optionMaxQuantity = 9999;

		for (var i = 0; i < itemOptions.length; i++) {
			// 필수선택옵션.
			for (var j = 0; j < SELECTED_OPTION_IDS.length; j++) {

				if (itemOptions[i].itemOptionId != SELECTED_OPTION_IDS[j]) {
					continue;
				}

				if (optionCount > 0) {
					option += '<br />';
					optionText += ' | ';
					optionData += '^^^';
				}


				//option += '<input type="hidden" name="itemOptionId" value="' + itemOptions[i].itemOptionId + '" />';
				option += '<input type="hidden" name="optionPrice" value="' + itemOptions[i].optionPrice + '" />';

				if (item.itemOptionType == 'S') {
					option += '<b>' + itemOptions[i].optionName1 + '</b>';
					option += '<span>' + itemOptions[i].optionName2 + '</span>';

					optionText += itemOptions[i].optionName2;
				} else if (item.itemOptionType == 'S2') {
					option += '<b>' + item.itemOptionTitle1 + '/' + item.itemOptionTitle2 + '</b>';
					option += '<span>' + itemOptions[i].optionName1 + '/' + itemOptions[i].optionName2 + '</span>';

					optionText += itemOptions[i].optionName1 + '/' + itemOptions[i].optionName2;
				} else if (item.itemOptionType == 'S3') {
					option += '<b>' + item.itemOptionTitle1 + '/' + item.itemOptionTitle2 + '/' + item.itemOptionTitle3 + '</b>';
					option += '<span>' + itemOptions[i].optionName1 + '/' + itemOptions[i].optionName2 + '/' + itemOptions[i].optionName3 + '</span>';

					optionText += itemOptions[i].optionName1 + '/' + itemOptions[i].optionName2 + '/' + itemOptions[i].optionName3;
				}

				if (itemOptions[i].optionPrice != "0") {
					option += ' (';
					optionText += ' (';

					if (Number(itemOptions[i].optionPrice) > 0) {
						option += '+';
						optionText += '+';

					}

					option += Number(itemOptions[i].optionPrice) + '원)';
					optionText += Number(itemOptions[i].optionPrice) + '원)';
				}


				// 전송용 옵션정보
				optionData += itemOptions[i].itemOptionId + '```';

				// 옵션 추가금액
				optionAdditionAmount += itemOptions[i].optionPrice;

				// 옵션 최대 구매 수량
				var optionQty = 9999;
				if (itemOptions[i].optionStockFlag == 'Y') {
					optionQty = itemOptions[i].optionStockQuantity;
				}

				optionMaxQuantity = optionQty < optionMaxQuantity ? optionQty : optionMaxQuantity;

				optionCount++;
			}
		}


		// 텍스트 옵션
		var $itemOptionInfo = $('.op-item-option-info');
		$itemOptionInfo.find('.text-option-id').each(function () {
			if (optionCount > 0) {
				option += '<br />';
				optionData += '^^^';
			}


			var $textOption = $(this).closest('div.op-item-option-info');
			var optionId = $(this).val();
			var optionName = $textOption.find('.text-option-name').val();
			var optionValue = $textOption.find('.text-option-value').val();

			//option += '<input type="hidden" name="itemOptionId" value="' + $(this) + '" />';
			option += '<input type="hidden" name="optionPrice" value="0" />';
			option += '<b>' + optionName + '</b>' + '<span>' + optionValue + '</span>';

			optionData += optionId + '```' + optionValue;

			optionCount++;

		});

		var html = '';

		html += '<li data-option="' + optionData + '" data-option-max-quantity="' + optionMaxQuantity + '">';
		html += '	<input type="hidden" name="arrayRequiredItems" />';
		html += '	<p class="tit">옵션</p>';

		html += '	<p class="item_name op-added-option item-name">' + option + '</p>';
		html += '	<div class="cacul op-added-option-quantity">';
		html += '		<a href="javascript:;" class="minus op-item-order-count-minus"><img src="/content/mobile/images/common/btn_minus.png" alt="수량감소" /></a>';
		html += '		<input type="text" maxlength="3" class="num op-option-quantity _number" value="1" />';
		html += '		<a href="javascript:;" class="plus op-item-order-count-plus"><img src="/content/mobile/images/common/btn_plus.png" alt="수량증가" /></a>';
		html += '		<p class="sum op-added-option-price">' + Common.numberFormat(item.price + optionAdditionAmount) + '원</p>';
		html += '	</div>';

		html += '	<a href="javascript:;" class="close_btn op-added-option-delete"></a>';
		html += '</li>';

		var $addedOptions = $('.op-added-options');

		// 이미 추가된 옵션인지 체크!
		var isAlreadyAddition = false;
		$addedOptions.find('.op-added-option').each(function () {
			var currentText = $("<div/>").append($(this).html()).text().replace(/[\r|\n|\t]/g, '');
			var newText = $("<div/>").append(html).find('.op-added-option').text().replace(/[\r|\n|\t]/g, '');

			if (currentText == newText) {
				isAlreadyAddition = true;
				return false;
			}
		});

		if (isAlreadyAddition != true) {
			$addedOptions.append(html);
		}

		// 필수 선택 옵션 초기화
		initItemOption();

		$itemOptionInfo.find('.op-selected-option').text(optionText);

		// 텍스트 옵션 초기화
		$itemOptionInfo.find('.text-option-value').val('');

		// 옵션 선택 박스 숨기기.
		$('.op-option-box').hide();

		// 금액 계산.
		calculate();
	}


	/****************************
	 * 추가된 옵션에 대한 이벤트
	 ****************************/

	// 수량 입력
	$('.op-added-options').on('blur', '.op-option-quantity', function (e) {
		var $quantity = $(this).closest('li').find('.op-option-quantity');
		var quantity = 1;

		try {
			quantity = Number($(this).val());
		} catch (e) {quantity = 0;
		}

		// 최대구매 수량 체크.
		var currentQuantity = getCurrentItemQuantity();

		if (currentQuantity > item.orderMaxQuantity) {
			alert('해당 상품의 최대 구매 수량은 ' + item.orderMaxQuantity + '개 입니다.');
			$quantity.val(1);
			calculate();
			return;
		}

		// 옵션 구매 수량 체크 (옵션재고)
		var optionMaxQuantity = Number($(this).closest('li').data('option-max-quantity'));

		if (quantity > optionMaxQuantity) {
			alert('옵션 별 최대 구매 수량은 ' + optionMaxQuantity + '개 입니다.\n구매 수량을 다시 확인해주세요.')
			$quantity.val(1);
			calculate();
			return;
		}

		//$quantity.val(quantity);
		calculate();


	}).on("keyup", '.op-option-quantity', function (e) {
		var pattern = /^[0-9]+$/;
		if (!pattern.test($(this).val())) {
			$(this).val('1');
		}
		calculate();

		if (!canOrderByMaxQuantity()) {
			$(this).val('1');
			calculate();
		}


	});


	// 수량 +
	$('.op-added-options').on('click', '.op-item-order-count-plus', function (e) {
		e.preventDefault();

		// 최대구매 수량 체크.
		var currentQuantity = getCurrentItemQuantity();

		if (currentQuantity == item.orderMaxQuantity) {
			alert('해당 상품의 최대 구매 수량은 ' + item.orderMaxQuantity + '개 입니다.');
			return;
		}

		// 옵션 최대 구매 수량 체크.
		var $quantity = $(this).closest('li').find('.op-option-quantity');
		var quantity = Number($quantity.val()) + 1;
		var optionMaxQuantity = Number($(this).closest('li').data('option-max-quantity'));

		if (quantity > optionMaxQuantity) {
			alert('옵션 별 최대 구매 수량은 ' + optionMaxQuantity + '개 입니다.\n구매 수량을 다시 확인해주세요.')
			return;
		}


		$quantity.val(quantity);
		calculate();
	});

	// 수량 -
	$('.op-added-options').on('click', '.op-item-order-count-minus', function (e) {
		e.preventDefault();

		var $quantity = $(this).closest('li').find('.op-option-quantity');
		var quantity = Number($quantity.val()) - 1;

		if (quantity == 0) {
			return;
		}

		$quantity.val(quantity);
		calculate();
	});

	// 삭제
	$('.op-added-options').on('click', '.op-added-option-delete', function (e) {
		e.preventDefault();
		$(this).closest('li').remove();
		calculate();
	});

	// 최대구매 수량 체크
	function canOrderByMaxQuantity() {

		if (item.orderMaxQuantity == -1) {
			return true;
		}

		var currentQuantity = getCurrentItemQuantity();

		if (currentQuantity > item.orderMaxQuantity) {
			alert('해당 상품의 최대 구매 수량은 ' + item.orderMaxQuantity + '개 입니다.');
			return false;
		}

		return true;
	}

	// 현재 상품 추가 수량
	function getCurrentItemQuantity() {
		var $addedOptions = $('.op-added-options li');

		var currentQuantity = 0;

		$addedOptions.each(function () {
			var quantity = 0;
			try {
				quantity = Number($(this).find('.op-option-quantity').val());
			} catch (e) {quantity = 0;
			}

			currentQuantity += quantity;
		});

		return currentQuantity;
	}
}

/***************************
 * 추가 구성 상품.
 ***************************/
function initItemAdditionEvent() {
	// 상품필수옵션 셀렉트박스 클릭
	$('.addition-select-box').on('click', function (e) {
		e.preventDefault();

		/*
		// 본상품 필수 옵션 먼저.
		var $addedOptions = $('.added-options li');

		if ($addedOptions.size() == 0) {
			alert('상품필수옵션을 먼저 선택해 주세요.');
			$('.option-select-box').focus();
			return;
		}
		*/


		var $itemOptionInfo = $('.item-addition-info');
		var $optionBoxType2 = $itemOptionInfo.find('.option-box-type2');

		$optionBoxType2.show();

		$('.option-box').hide();
		$(this).closest('div').find('.option-box').show();
		$(this).closest('div').find('.option-box ul').hide().eq(0).show();

	});

	// 세부 옵션 선택 이벤트.
	$('.item-addition-info .option-box').on('click', '.option-group li a', function (e) {
		e.preventDefault();

		var additionItemName = $(this).text();
		var additionItemId = $(this).data('item-id');
		var additionItemPrice = $(this).data('item-price');
		var additionText = $(this).text();
		var itemMaxQuantity = 9999;
		var additionStockFlag = $(this).data('stock-flag');
		var additionStockQuantity = $(this).data('stock-quantity');

		// 옵션 최대 구매 수량
		var itemQty = 9999;
		if (additionStockFlag == 'Y') {
			itemQty = Number(additionStockQuantity);
		}

		itemMaxQuantity = itemQty < itemMaxQuantity ? itemQty : itemMaxQuantity;


		var html = '';
		html += '<li data-item-id="' + additionItemId + '" data-item-max-quantity="' + itemMaxQuantity + '" data-item-price="' + additionItemPrice + '">';
		html += '	<input type="hidden" name="arrayAdditionItems" />';
		html += '	<span class="added-option"><strong>추가구성상품</strong> ' + additionText + '</span>';
		html += '	<span class="added-option-quantity">';
		html += '		<a href="javascript:;" class="minus op-item-order-count-minus"><img src="/content/mobile/images/common/btn_minus.png" alt="수량감소" /></a>';
		html += '		<input type="text" maxlength="3" class="op-option-quantity _number" value="1" />';
		html += '		<a href="javascript:;" class="plus op-item-order-count-plus"><img src="/content/mobile/images/common/btn_plus.png" alt="수량증가" /></a>';
		html += '	</span>';
		html += '	<span class="added-option-price">' + Common.numberFormat(additionItemPrice) + '원</span>';
		html += '	<a href="javascript:;" class="added-option-delete"><span class="glyphicon glyphicon-remove"></span></a>';
		html += '</li>';


		var $addedItems = $('.added-items');

		// 이미 추가된 옵션인지 체크!
		var isAlreadyAddition = false;
		$addedItems.find('.added-option').each(function () {
			var currentText = $("<div/>").append($(this).html()).text().replace(/[\r|\n|\t]/g, '');
			var newText = $("<div/>").append(html).find('.added-option').text().replace(/[\r|\n|\t]/g, '');

			if (currentText == newText) {
				isAlreadyAddition = true;
				return false;
			}
		});

		if (isAlreadyAddition != true) {
			$addedItems.append(html);
		}

		// 필수 선택 옵션 초기화
		//initItemOption();

		$('.item-addition-info').find('.op-selected-option').text(additionText);

		// 옵션 선택 박스 숨기기.
		$('.option-box').hide();

		// 금액 계산.
		calculate();

	});

	/****************************
	 * 추가된 상품에 대한 이벤트
	 ****************************/

	// 수량 입력
	$('.added-items').on('blur', '.op-option-quantity', function (e) {
		var $quantity = $(this).closest('li').find('.option-quantity');
		var quantity = 1;

		try {
			quantity = Number($(this).val());
		} catch (e) {quantity = 0;
		}


		// 최대구매 수량 체크.
		var currentQuantity = getCurrentItemQuantity();

		if (currentQuantity > item.orderMaxQuantity) {
			alert('해당 상품의 최대 구매 수량은 ' + item.orderMaxQuantity + '개 입니다.');
			$quantity.val(1);
			calculate();
			return;
		}


		// 옵션 구매 수량 체크 (옵션재고)
		var optionMaxQuantity = Number($(this).closest('li').data('option-max-quantity'));

		if (quantity > optionMaxQuantity) {
			alert('옵션 별 최대 구매 수량은 ' + optionMaxQuantity + '개 입니다.\n구매 수량을 다시 확인해주세요.')
			$quantity.val(1);
			calculate();
			return;
		}


		//$quantity.val(quantity);
		calculate();


	}).on("keyup", '.option-quantity', function (e) {
		var pattern = /^[0-9]+$/;
		if (!pattern.test($(this).val())) {
			$(this).val('1');
		}
		calculate();

		if (!canOrderByMaxQuantity()) {
			$(this).val('1');
			calculate();
		}


	});


	// 수량 +
	$('.added-items').on('click', '.op-item-order-count-plus', function (e) {
		e.preventDefault();

		// 최대구매 수량 체크.
		var $quantity = $(this).parent().find('.option-quantity');
		var currentQuantity = Number($quantity.val());
		var itemPrice = Number($(this).parent().data('data-item-price'));
		var itemOrderMaxQuantity = Number($(this).parent().data('data-item-max-quantity'));

		if (currentQuantity == itemOrderMaxQuantity) {
			alert('추가 구성품의 최대 구매 수량은 ' + item.orderMaxQuantity + '개 입니다.');
			return;
		}


		$quantity.val(currentQuantity + 1);
		calculate();
	});

	// 수량 -
	$('.added-items').on('click', '.op-item-order-count-minus', function (e) {
		e.preventDefault();

		var $quantity = $(this).closest('li').find('.option-quantity');
		var quantity = Number($quantity.val()) - 1;

		if (quantity == 0) {
			return;
		}

		$quantity.val(quantity);
		calculate();

	});

	// 삭제
	$('.added-items').on('click', '.added-option-delete', function (e) {
		e.preventDefault();
		$(this).closest('li').remove();
		calculate();
	});

}

// 세트상품 선택 추가 이벤트
function initItemSetEvent() {
	if (item.itemType !== '3' || item.itemOptionFlag === 'N') {
		return;
	}

	// 세트상품 element
	var $SET_ELEMENTS = $('ul.set_det > li');

	// 선택 옵션 정보
	var SELECTED_OPTION_INFOS = [];

	// 선택 세트상품 정보
	var SELECTED_SET_INFO = {
		amount : 0,
		arrayItemSetInfos: [],
		arrayItemSets: []
	};

	// 세트상품 셀렉트 박스 클릭
	$('.op-option-select-box').on('click', function (e) {
		e.preventDefault();

		// 옵션 선택 초기화.
		initItemOption(getIndex($(this)));

		$('.op-option-box').hide();
		$(this).closest('div').find('.op-option-box').show();
		$(this).closest('div').find('.op-option-box ul.op-option-box-type1').hide().eq(0).show();
		$(this).closest('div').find('.op-option-box ul.option_list02').hide().eq(0).show();
	});

	// 선택 박스 닫기
	$('.op-close-option-box').on('click', function (e) {
		e.preventDefault();

		$('.op-option-box').hide();

		// 옵션 선택 초기화.
		initItemOption(getIndex($(this)));
	});

	// 세트 상품 선택 이벤트.
	$('.select_area .op-option-box').on('click', '.op-option-group li a', function (e) {
		e.preventDefault();

		var $selectArea = $(this).closest('.select_area');
		var $optionBoxType = $(this).closest('.op-option-group').parent();
		var $optionGroup = $(this).closest('.op-option-group');

		var parentsIndex = getIndex($selectArea);
		var currentOptionGroupIndex = $optionBoxType.find('.op-option-group').index($optionGroup);

		var optionGroupCount = $optionBoxType.find('.op-option-group').size();
		var optionName = $(this).attr('data-option-name');

		// 상품 선택 처리.
		$(this).addClass('on');
		$optionGroup.find('.option-selected-name').text(optionName);

		var optionSelectedCount = $optionBoxType.find('a.on').size();

		// 모든 상품을 선택한 경우.
		if (optionGroupCount === optionSelectedCount) {
			completeSelectOption(parentsIndex);
		} else {
			displayNextItem(parentsIndex, currentOptionGroupIndex);
		}

		// 다음 상품 보기.
		function displayNextItem(pIndex, currentIndex) {
			var $item = itemSets[pIndex].item;
			var index = currentIndex + 1;

			// S2, S3는 다음 옵션을 조회해서 가져와야 한다.
			if ($item.itemOptionType != 'S') {

				// 이전 까지 선택한 옵션명 가져오기.
				var optionNames = [];
				var i = 0;
				$optionBoxType.find('.op-option-group:lt(' + index + ')').each(function () {
					optionNames[i] = $(this).find('a.on').attr('data-option-name');
					i++;
				});

				// next 옵션 목록
				var optionHtml = '';
				var lastOptionValue = '';
				for (var i = 0; i < $item.itemOptions.length; i++) {
					if ($item.itemOptions[i].optionType == 'T') {
						continue;
					}

					var step = optionNames.length + 1;
					var optionValue = '';

					if (optionNames.length == 1 && $item.itemOptions[i].optionName1 == optionNames[0]) {
						optionValue = $item.itemOptions[i].optionName2;
					} else if (optionNames.length == 2 && $item.itemOptions[i].optionName1 == optionNames[0] && $item.itemOptions[i].optionName2 == optionNames[1]) {
						optionValue = $item.itemOptions[i].optionName3;
					} else {
						continue;
					}

					if (optionValue != lastOptionValue) {
						// 마지막 옵션인 경우 옵션 정보까지 보여 줘야 함.
						if (step == optionGroupCount) {
							optionHtml += '		<li>' + getOptionInfo($item, $item.itemOptions[i], step) + '</li>';
						} else {
							optionHtml += '		<li><a href="javascript:;" data-option-name="' + optionValue + '">' + getOptionImage($item, optionValue) + ' ' + optionValue + '</a><li>';
						}

						lastOptionValue = optionValue;
					}

				}

				// next 옵션 목록 추가.
				$optionBoxType.find('.op-option-group').eq(index).find('ul').append(optionHtml);
				$optionBoxType.find('.op-option-group').eq(index).find('.option-selected-name').text('');
			}

			$optionBoxType.find('.op-option-group').eq(currentIndex).find('ul').hide();
			$optionBoxType.find('.op-option-group').eq(index).find('ul').show();
		}

		// 상품 선택 완료
		function completeSelectOption(pIndex) {
			var tempItem = {'itemOptionType': ''};

			// 현재 선택한 옵션 단계별 loop
			$optionBoxType.find('a.on').each(function (i) {
				var itemId = $(this).data('item-id');
				var optionId = $(this).data('option-id');

				// 최종 선택 정보
				if (itemId != null && optionId != null) {
					// 선택 옵션 정보 loop
					for (var i=0; i<SELECTED_OPTION_INFOS.length; i++) {
						// 선택 정보 중복시, 기존 선택 정보 삭제
						if (SELECTED_OPTION_INFOS[i].itemId === itemId) {
							console.dir(SELECTED_OPTION_INFOS);
							SELECTED_OPTION_INFOS.splice(i, 1);
							break;
						}
					}

					tempItem.itemOptionType = $(this).data('option-type');

					SELECTED_OPTION_INFOS.push({
						'itemId': itemId,
						'itemOptionId': optionId,
						'optionName1': $(this).data('option-name1'),
						'optionName2': $(this).data('option-name2'),
						'optionName3': $(this).data('option-name3'),
						'optionPrice': $(this).data('option-price'),
						'optionStockFlag': $(this).data('option-stock-flag'),
						'optionStockQuantity': $(this).data('option-stock-quantity')
					});
				}
			});

			// 선택 옵션 숨김
			$SET_ELEMENTS.eq(pIndex).find('.op-option-box').hide();

			$SET_ELEMENTS.eq(pIndex).find('.op-option-select-box').text(makeOptions(tempItem, SELECTED_OPTION_INFOS[SELECTED_OPTION_INFOS.length-1], 'text'));
			$SET_ELEMENTS.eq(pIndex).find('.op-option-select-box').addClass('selected');

			if (SELECTED_OPTION_INFOS.length === $('.select_area').length) {
				// 선택된 구성으로 세트상품을 추가한다.
				addItemSets();

				$('.select_area').find('.op-option-select-box').each(function() {
					$(this).html('[필수] 옵션 선택');
				});

				// 선택 정보 초기화
				SELECTED_OPTION_INFOS = new Array();
			}
		}
	}).on('mouseenter', '.option-group li a', function (e) {
		e.preventDefault();
		var optionName = $(this).attr('data-option-name');
		$(this).closest('.option-group').find('.option-selected-name').text(optionName);
	}).on('mouseleave', '.option-group li a', function (e) {
		e.preventDefault();
		if (!$(this).hasClass('on')) {
			$(this).closest('.option-group').find('.option-selected-name').text('');
		}
	});

	// 상품 옵션 그룹 링크
	$('.op-option-box-type1').on('click', '.op-option-group > li > span', function (e) {
		e.preventDefault();

		var $selectArea = $(this).closest('.select_area');
		var $optionGroup = $(this).closest('.op-option-group');
		var $optionBoxType = $('.op-option-box-type1');
		var currentOptionGroupIndex = $optionBoxType.find('.op-option-group').index($optionGroup);

		var index = getIndex($selectArea);
		var $item = itemSets[index].item;

		if ($item.itemOptionType == 'S') {
			var $previousOptionGroups = $optionBoxType.find('.op-option-group:lt(' + currentOptionGroupIndex + ')');
			var optionGroupCount = $previousOptionGroups.size();
			var selectedOptionGroupCount = $previousOptionGroups.find('a.on').size();

			if (selectedOptionGroupCount < optionGroupCount) {
				alert('위의 정보를 먼저 선택해 주세요.');
				return;
			}

			$optionBoxType.find('.option_list02').hide();
			$optionGroup.find('a.on').removeClass('on');
			$optionGroup.find('.option-selected-name').text('');
			$optionGroup.find('.option_list02').show();

			// 현재 이후 STEP 초기화.
			$optionBoxType.find('.op-option-group:gt(' + currentOptionGroupIndex + ')').each(function () {
				$(this).find('a.on').removeClass('on');
				$(this).find('.option-selected-name').text('위의 정보를 먼저 선택해 주세요.');
			});

		} else {
			if ($optionGroup.find('.option_list02 > li').size() == 0) {
				alert('위의 정보를 먼저 선택해 주세요.');
				return;
			}

			$optionBoxType.find('.option_list02').hide();
			$optionGroup.find('a.on').removeClass('on');
			$optionGroup.find('.option-selected-name').text('');
			$optionGroup.find('.option_list02').show();

			// 현재 이후 STEP 초기화.
			$optionBoxType.find('.op-option-group:gt(' + currentOptionGroupIndex + ')').each(function () {
				$(this).find('a.on').removeClass('on');
				$(this).find('.option-selected-name').text('위의 정보를 먼저 선택해 주세요.');
				$(this).find('.option_list02 > li').remove();
			});

		}
	});

	function initItemOption(index) {
		index = index || 0;
		var $item = itemSets[index].item;

		if ($item.itemOptionFlag === 'N' || $item.itemOptionType === 'T') {
			return;
		}

		$('.op-option-select-box').removeClass('selected');

		var $selectArea = $SET_ELEMENTS.eq(index).find('.select_area');
		$selectArea.find('.op-option-select-box').text('[필수] 옵션 선택');

		var $optionBoxType1 = $selectArea.find('.op-option-box-type1');

		$('.select_area .op-option-group li a').removeClass('on');

		var optionGroupHtml = '';
		if ($item.itemOptionType === 'S') {
			var groupTitle = '';
			for (var i = 0; i < $item.itemOptions.length; i++) {
				if ($item.itemOptions[i].optionType === 'T') {
					continue;
				}

				if ($item.itemOptions[i].optionName1 !== groupTitle) {
					if (i > 0) {
						optionGroupHtml += '	</ul>';
						optionGroupHtml += '</div>';
					}

					optionGroupHtml += '<ul class="option_list01 op-option-group">';
					optionGroupHtml +=		'<li>';
					optionGroupHtml += 			'<span>' + $item.itemOptions[i].optionName1 + ' <label class="option-selected-name">선택하세요.</label></span>';
					optionGroupHtml += 			'<ul class="option_list02">';
				}

				// 옵션항목.
				optionGroupHtml += 					'<li>' + getOptionInfo($item, $item.itemOptions[i]) + '</li>';

				groupTitle = $item.itemOptions[i].optionName1;
			}

			optionGroupHtml += 			'</ul>';
			optionGroupHtml +=		'</li>';
			optionGroupHtml += '</ul>';
		} else {
			// option1
			optionGroupHtml += '<ul class="option_list01 op-option-group">';
			optionGroupHtml +=		'<li>';
			optionGroupHtml += 			'<span>' + $item.itemOptionTitle1 + ' <label class="option-selected-name">선택하세요.</label></span>';
			optionGroupHtml += 			'<ul class="option_list02">';

			var optionName1 = '';
			for (var i = 0; i < $item.itemOptions.length; i++) {
				if ($item.itemOptions[i].optionType === 'T') {
					continue;
				}

				if ($item.itemOptions[i].optionName1 !== optionName1) {
					optionGroupHtml += 			'<li><a href="javascript:;" data-option-name="' + $item.itemOptions[i].optionName1 + '">' + getOptionImage($item.itemOptions[i].optionName1) + ' ' + $item.itemOptions[i].optionName1 + '</a><li>';
				}

				optionName1 = $item.itemOptions[i].optionName1;
			}

			optionGroupHtml += 			'</ul>';
			optionGroupHtml +=		'</li>';
			optionGroupHtml += '</ul>';

			// option2
			optionGroupHtml += '<ul class="option_list01 op-option-group">';
			optionGroupHtml +=		'<li>';
			optionGroupHtml += 			'<span>' + $item.itemOptionTitle2 + ' <label class="option-selected-name">위의 정보를 먼저 선택해 주세요.</label></span>';
			optionGroupHtml += 			'<ul class="option_list02">';
			optionGroupHtml +=			'</ul>';
			optionGroupHtml +=		'</li>';
			optionGroupHtml += '</ul>';

			if ($item.itemOptionType === 'S3') {
				// option3
				optionGroupHtml += '<ul class="option_list01 op-option-group">';
				optionGroupHtml +=		'<li>';
				optionGroupHtml += 			'<span>' + $item.itemOptionTitle3 + ' <label class="option-selected-name">위의 정보를 먼저 선택해 주세요.</label></span>';
				optionGroupHtml += 			'<ul class="option_list02">';
				optionGroupHtml += 			'</ul>';
				optionGroupHtml +=		'</li>';
				optionGroupHtml += '</ul>';
			}
		}

		$optionBoxType1.find('.op-option-group').remove();
		$optionBoxType1.append(optionGroupHtml);
		$optionBoxType1.show();
	}

	// 상품 옵션 텍스트 구성.
	function getOptionInfo(item, itemOption, step) {
		var info = '';
		var optionName = itemOption.optionName2;

		if (itemOption.optionType != 'S') {
			if (step == 1) optionName = itemOption.optionName1;
			if (step == 2) optionName = itemOption.optionName2;
			if (step == 3) optionName = itemOption.optionName3;
		}

		info += optionName;

		// 추가금.
		if (itemOption.optionPrice != 0) {
			info += ' (' + (itemOption.optionPrice > 0 ? '+' : '') + Common.numberFormat(itemOption.optionPrice) + '원)';
		}

		// 품절, 재고연동
		if (itemOption.isSoldOut == 'true') {
			info += ' - 품절';
		} else if (itemOption.optionStockFlag == 'Y' && itemOption.optionStockQuantity > 0) {
			info += ' | 재고: ' + Common.numberFormat(itemOption.optionStockQuantity) + '개';
		}

		if (itemOption.isSoldOut == 'true') {
			return '<span>' + getOptionImage(item, optionName, itemOption.itemOptionId) + ' ' + info + '</span>';
		} else {
			return '<a href="javascript:;" data-item-id="' + item.itemId + '" data-option-type="' + itemOption.optionType + '" data-option-id="' + itemOption.itemOptionId + '"'
				+ ' data-option-name1="' + itemOption.optionName1 + '" data-option-name2="' + itemOption.optionName2 + '" data-option-name3="' + itemOption.optionName3 + '"'
				+ ' data-option-price="' + itemOption.optionPrice + '" data-option-stock-flag="' + itemOption.optionStockFlag + '" data-option-stock-quantity="' + itemOption.optionStockQuantity + '">'
				+ getOptionImage(item, optionName, itemOption.itemOptionId) + ' ' + info + '</a>';
		}
	}

	// 옵션 이미지 가져오기
	function getOptionImage(item, optionName, itemOptionId) {
		var $itemOptionImages = item.itemOptionImages;

		if ($itemOptionImages != null) {
			// optionId가 있는 경우.
			if (itemOptionId !== undefined && itemOptionId > 0) {
				for (var i = 0; i < $itemOptionImages.length; i++) {
					if ($itemOptionImages[i].itemOptionId == itemOptionId) {
						return '<img src="' + $itemOptionImages[i].optionImage + '" alt="' + optionName + '" />';
					}
				}
			}

			for (var i = 0; i < $itemOptionImages.length; i++) {
				if ($itemOptionImages[i].optionName == optionName) {
					return '<img src="' + $itemOptionImages[i].optionImage + '" alt="' + optionName + '" />';
				}
			}
		}

		return "";
	}

	// 선택완료된 구성으로 세트상품을 추가한다.
	function addItemSets() {
		if (item.isItemSoldOut) {
			return false;
		}

		for (var i=0; i<itemSets.length; i++) {
			var itemSetInfo = {
				'itemId': itemSets[i].item.itemId,
				'itemName': itemSets[i].item.itemName,
				'itemOptionType': itemSets[i].item.itemOptionType,
				'stockFlag': itemSets[i].item.stockFlag,
				'stockQuantity': itemSets[i].item.stockQuantity,
				'quantity': itemSets[i].quantity
			};

			// 옵션 상품
			if (itemSets[i].item.itemOptionFlag === 'Y') {
				for (var j=0; j<SELECTED_OPTION_INFOS.length; j++) {
					var itemOption = SELECTED_OPTION_INFOS[j];

					if (itemOption.itemId === itemSets[i].item.itemId) {
						SELECTED_SET_INFO.amount += itemOption.optionPrice;
						SELECTED_SET_INFO.arrayItemSets.push(makeItemSetData(itemSets[i].item, itemOption, itemSets[i].quantity));

						// 옵션 정보 세팅
						itemSetInfo.itemOption = itemOption;

						// 재고값에 옵션 재고 세팅
						itemSetInfo.stockFlag = itemOption.optionStockFlag;
						itemSetInfo.stockQuantity = itemOption.optionStockQuantity;
						break;
					}
				}
			}
			// 일반 상품
			else {
				SELECTED_SET_INFO.arrayItemSets.push(makeItemSetData(itemSets[i].item, null, itemSets[i].quantity));
			}

			// 선택된 상품 정보 세팅
			SELECTED_SET_INFO.arrayItemSetInfos.push(itemSetInfo);
		}

		// 옵션금액 총합 + 현재 상품가격
		SELECTED_SET_INFO.amount += item.presentPrice;

		// 세트상품 주문 정보 세팅
		var $addedSets = $('.op-added-sets');
		
		var html = '<li data-item-price="' + SELECTED_SET_INFO.amount + '">';
		html += '<div class="op-added-option">';

		// 선택 세트상품 상세정보
		var itemSetInfos = SELECTED_SET_INFO.arrayItemSetInfos;

		for (var i = 0; i < itemSetInfos.length; i++) {
			html += '<p class="item_name"' +
				' data-item-set="' + SELECTED_SET_INFO.arrayItemSets[i] + '"' +
				' data-quantity="' + itemSetInfos[i].quantity + '"' +
				' data-stock-flag="' + itemSetInfos[i].stockFlag + '"' +
				' data-stock-quantity="' + itemSetInfos[i].stockQuantity + '">';

			html += '구성상품' + (i + 1) + ' <span class="item-name">' + itemSetInfos[i].itemName + '</span>';

			if (itemSetInfos[i].itemOption != null) {
				html += ' <span class="option-name"><' + makeOptions(itemSetInfos[i], itemSetInfos[i].itemOption, 'text') + '></span>';
			}

			html += 	' (' + itemSetInfos[i].quantity + '개)';
			html += '</p>';
		}

		html += 	'</div>';
		html +=		'<div class="op-added-data" style="display:none;"></div>';
		html += 	'<div class="cacul op-added-option-quantity">';
		html +=			'<a href="javascript:;" class="minus op-item-order-count-minus"><img src="/content/mobile/images/common/btn_minus.png" alt="수량감소" /></a>';
		html += 		'<input type="text" maxlength="3" class="num op-option-quantity _number" value="1" />';
		html += 		'<a href="javascript:;" class="plus op-item-order-count-plus"><img src="/content/mobile/images/common/btn_plus.png" alt="수량증가" /></a>';
		html += 		'<p class="sum op-added-option-price">' + Common.numberFormat(SELECTED_SET_INFO.amount) + '원</p>';
		html += 	'</span>';
		html += 	'<a href="javascript:;" class="close_btn op-added-option-delete"></a>';
		html += '</li>';

		// 이미 추가된 세트인지 체크
		var isAlreadyAddition = false;
		$addedSets.find('.op-added-option').each(function () {
			var currentText = $("<div/>").append($(this).html()).text().replace(/[\r|\n|\t]/g, '');
			var newText = $("<div/>").append(html).find('.op-added-option').text().replace(/[\r|\n|\t]/g, '');

			if (currentText === newText) {
				isAlreadyAddition = true;
				return false;
			}
		});

		if (!isAlreadyAddition) {
			$addedSets.append(html);
		}

		// 재고 체크
		if (!isAvailableSetStock('create')) {
			$addedSets.find('li').last().remove();
		}

		// 금액 계산.
		calculate();

		// 세트상품 정보 초기화
		SELECTED_SET_INFO.amount = 0;
		SELECTED_SET_INFO.arrayItemSets = new Array();
		SELECTED_SET_INFO.arrayItemSetInfos = new Array();
	}

	/****************************
	 * 추가된 세트상품에 대한 이벤트
	 ****************************/
	// 수량 입력
	$('.op-added-sets').on('blur', '.op-option-quantity', function (e) {
		var index = $('.op-added-sets .op-option-quantity').index(this);

		isAvailableSetStock('manual', index);
		calculate();
	}).on("keyup", '.op-option-quantity', function (e) {
		var pattern = /^[0-9]+$/;
		if (!pattern.test($(this).val())) {
			$(this).val('1');
			calculate();
			return;
		}
	});

	// 수량 +
	$('.op-added-sets').on('click', '.op-item-order-count-plus', function (e) {
		e.preventDefault();

		// 구매 수량 체크.
		var index = $('.op-added-sets .op-item-order-count-plus').index(this);
		var $quantity = $(this).closest('li').find('.op-option-quantity');
		var quantity = Number($quantity.val()) + 1;

		$quantity.val(quantity);

		isAvailableSetStock('plus', index);
		calculate();
	});

	// 수량 -
	$('.op-added-sets').on('click', '.op-item-order-count-minus', function (e) {
		e.preventDefault();

		var $quantity = $(this).closest('li').find('.op-option-quantity');
		var quantity = Number($quantity.val()) - 1;

		if (quantity < 1) {
			return;
		}

		$quantity.val(quantity);
		calculate();

	});

	// 삭제
	$('.op-added-sets').on('click', '.op-added-option-delete', function (e) {
		e.preventDefault();
		$(this).closest('li').remove();
		calculate();
	});

	// 세트상품 데이터 생성
	function makeItemSetData(item, itemOption, quantity) {
		var itemSetData = '';

		if (itemOption != null) {
			itemSetData = item.itemId + "||" + quantity + "||" + itemOption.itemOptionId + "```";
		} else {
			itemSetData = item.itemId + "||" + quantity + "||";
		}

		return itemSetData;
	}

	// 세트상품 index 추출
	function getIndex($element) {
		return $element.closest($SET_ELEMENTS).data('index');
	}
}

// 구매가능 체크
function isAvailableSetStock(target, index) {
	index = index || 0;	// 현재 세트상품 주문 데이터 index
	var buyItems = [];	// 재고 체크용 temp array

	var isAvailable = true;	// 재고 가능 여부
	var alertText = '';		// alert 표시 text

	var $quantity = $('.op-item-quantity .quantity');	// 세트상품 수량 input

	if (item.itemOptionFlag === 'Y') {
		$quantity = $('.op-added-sets li').eq(index).find('.op-option-quantity');

		// 세트상품 주문 정보 loop
		$('.op-added-sets li').each(function() {
			// 세트상품별 상품정보 loop
			$(this).find('p').each(function () {
				var buyItemSetData = $(this).data('item-set');
				var buyQuantity = Number($(this).closest('li').find('.op-option-quantity').val()) * Number($(this).data('quantity'));

				// 재고 체크용 데이터 만들기
				var isAdd = false;
				for (var i=0; i<buyItems.length; i++) {
					// 이미 추가된 상품일 경우, buyQuantity 를 변경
					if (buyItemSetData == buyItems[i].itemSetData) {
						buyItems[i].buyQuantity += buyQuantity;
						isAdd = true;
						break;
					}
				}

				if (!isAdd) {
					// 새로 추가된 상품 저장
					buyItems.push({
						'itemSetData': buyItemSetData,
						'itemName': $(this).find('span.item-name').text(),
						'optionName': $(this).find('span.option-name').text(),
						'stockFlag': $(this).data('stock-flag'),
						'stockQuantity': Number($(this).data('stock-quantity')),
						'quantity': Number($(this).data('quantity')),
						'buyQuantity': buyQuantity
					});
				}
			});
		});
	} else {
		// 세트상품 loop
		for (var i=0; i<itemSets.length; i++) {
			// 세트상품별 상품정보 저장
			buyItems.push({
				'itemName': itemSets[i].item.itemName,
				'optionName': '',
				'stockFlag': itemSets[i].item.stockFlag,
				'stockQuantity': itemSets[i].item.stockQuantity,
				'quantity': itemSets[i].quantity,
				'buyQuantity': Number($quantity.val()) * itemSets[i].quantity
			});
		}
	}

	var inputQuantity = Number($quantity.val()); 	// 입력 수량

	// 재고 수량 내림차순 정렬
	if (buyItems.length > 1) {
		buyItems.sort(function(a, b) {
			return Math.floor(b.stockQuantity / b.quantity) - Math.floor(a.stockQuantity / a.quantity);
		});
	}

	// 만들어진 데이터로 재고 체크 및 수량 조절 (재고 수량이 많은 상품부터 내림차순 조절)
	for (var i=0; i<buyItems.length; i++) {
		var buyQuantity = buyItems[i].buyQuantity;
		var stockFlag = buyItems[i].stockFlag;
		var stockQuantity = buyItems[i].stockQuantity;

		if (stockFlag === 'Y' && buyQuantity > stockQuantity) {
			// 재고 내 가능한 최대값을 세팅
			// 수량 조절 : [재고수량 - {총 구매수량 - (입력수량 * 세트수량)}] / 세트수량
			if (target == 'manual' || target == 'order' || target === 'plus') {
				$quantity.val(Math.floor((stockQuantity - (buyQuantity - (inputQuantity * buyItems[i].quantity))) / buyItems[i].quantity));
			}

			// alert text 세팅
			alertText = buyItems[i].itemName;
			if (buyItems[i].optionName != '') { // 옵션 존재시, 옵션명 노출
				alertText += ' ' + buyItems[i].optionName;
			}
			alertText += '의 최대 구매 수량은 ' + buyItems[i].stockQuantity + '개 입니다.';

			isAvailable = false;
		}
	}

	// 재고 수량이 가장 적은 마지막 상품 정보만 노출됨
	if (!isAvailable) {
		alert(alertText);
	}

	return isAvailable;
}

// 옵션 텍스트 생성
function makeOptions(item, itemOption, type) {
	var options = '';

	if (type == 'text') {
		if (item.itemOptionType == 'S') {
			options += itemOption.optionName2;
		} else if (item.itemOptionType == 'S2') {
			options += itemOption.optionName1 + ' | ' + itemOption.optionName2;
		} else if (item.itemOptionType == 'S3') {
			options += itemOption.optionName1 + ' | ' + itemOption.optionName2 + ' | ' + itemOption.optionName3;
		}
	} else {
		if ($item.itemOptionType == 'S') {
			options += '<strong>' + itemOption.optionName1 + '</strong> ' + itemOption.optionName2;
		} else if (item.itemOptionType == 'S2') {
			options += '<strong>상품필수옵션</strong> ' + itemOption.optionName1 + ' | ' + itemOption.optionName2;
		} else if (item.itemOptionType == 'S3') {
			options += '<strong>상품필수옵션</strong> ' + itemOption.optionName1 + ' | ' + itemOption.optionName2 + ' | ' + itemOption.optionName3;
		}
	}

	if (itemOption.optionPrice != "0") {
		options += ' (';
		if (Number(itemOption.optionPrice) > 0) {
			options += '+';
		}
		options += Number(itemOption.optionPrice) + '원)';
	}

	return options;
}

// 가격계산
function calculate() {
	var itemPrice = item.presentPrice;
	if ($.trim(itemPrice) == '') {
		return;
	}

	var totalItemPrice = 0;
	var totalItemCount = 0;

	var totalOptionPrice = 0;
	var totalAdditionPrice = 0;
	var totalSetPrice = 0;

	var $addedOptions = $('.op-added-options li');
	var $addedItems = $('.op-added-items li');
	var $addedSets = $('.op-added-sets li');

	// 단품
	var $quantity = $('.quantity');
	if ($quantity.size() > 0) {
		totalItemPrice = itemPrice * Number($quantity.val());
	}

	// 상품 옵션
	$addedOptions.each(function () {
		var quantity = $(this).find('.op-option-quantity').val();
		var extraPrice = 0;

		$(this).find('input[name=optionPrice]').each(function() {
			extraPrice += $.trim($(this).val()) == '' ? 0 : Number($(this).val());
		});

		quantity = $.trim(quantity) == '' ? 0 : Number(quantity);

		var optionPrice = (Number(itemPrice) + Number(extraPrice)) * quantity;
		totalOptionPrice += optionPrice;
		totalItemCount += quantity;

		$(this).find('.op-added-option-price').text(Common.numberFormat(optionPrice) + '원');
	});

	// 추가구성 상품
	$addedItems.each(function () {
		var quantity = $(this).find('.op-option-quantity').val();
		var itemPrice = $(this).data('item-price');

		quantity = $.trim(quantity) == '' ? 0 : Number(quantity);
		itemPrice = $.trim(itemPrice) == '' ? 0 : Number(itemPrice);

		var additionItemPrice = Number(itemPrice) * quantity;
		totalAdditionPrice += additionItemPrice;
		totalItemCount += quantity;

		$(this).find('.op-added-option-price').text(Common.numberFormat(additionItemPrice) + '원');
	});

	// 세트 상품
	$addedSets.each(function () {
		var quantity = $(this).find('.op-option-quantity').val();
		var itemPrice = $(this).data('item-price');

		quantity = $.trim(quantity) == '' ? 0 : Number(quantity);
		itemPrice = $.trim(itemPrice) == '' ? 0 : Number(itemPrice);

		var setItemPrice = Number(itemPrice) * quantity;
		totalSetPrice += setItemPrice;
		totalItemCount += quantity;

		$(this).find('.op-added-option-price').text(Common.numberFormat(setItemPrice) + '원');
	});

	$('.total-amount').text(Common.numberFormat(totalItemPrice + totalOptionPrice + totalAdditionPrice));
	$('.total-price').show();
}

// 상품 옵션의 기준 셀렉터.
function getItemOptionInfo() {
	return $('#op-item-option-info');
}

// 모든 상품 옵션을 선택했는가?
function isSelectedAllOption() {
	var $itemOptionInfo = getItemOptionInfo();
	var optionGroupCount = $itemOptionInfo.find('tr').size();
	var optionCheckedCount = 0;

	// 라디오 옵션 체크 카운트
	optionCheckedCount += $itemOptionInfo.find('input[type=radio]:checked').size();

	// 셀렉트박스 옵션 체크 카운트
	$itemOptionInfo.find('select').each(function () {
		if ($(this).val() != '') {
			optionCheckedCount++;
		}
	});

	if (optionGroupCount == optionCheckedCount) {
		return true;
	}
	return false;
}

// 상품 옵션 정보
function getItemOption(val) {
	var itemOptionInfo = val.split('|');
	var optionInfos = itemOptionInfo[1].split('^');
	return {
		'itemOptionTitle': optionInfos[0],
		'itemOptionId': Number(optionInfos[1]),
		'optionName': optionInfos[2],
		'extraPrice': Number(optionInfos[3]),			// 추가금액
		'stockQuantity': Number(optionInfos[4]),
		'stockScheduleText': optionInfos[5],
		'stockScheduleDate': optionInfos[6]
	};
}

//상품 옵션 정보 (카드 전송용 )
function getOptionValueForCart(val) {
	return val.split('|')[0];
}

// 전체 주문 수량.
function getTotalItemQuantity() {
	var totalItemQuantity = 0;
	$('#added-options input[name=arrayQuantitys]').each(function () {
		totalItemQuantity += Number($(this).val());
	});
	return totalItemQuantity;
}

// 비회원 구매 가능 상품 체크.
// 비회원 구매 불가인 경우 로그인 페이지로 이동
function checkForNonmemberOrder() {
	
	var nonmemberOrderType = item.nonmemberOrderType;
	if (!(isLogin == 'true' || nonmemberOrderType == '1')) {
		var message = Message.get("M00601") + '\n' + Message.get("M00602");	// 회원만 구매가 가능합니다. 로그인 페이지로 이동하시겠습니까?
		if (confirm(message)) {
			location.href = "/m/users/login?target="+requestUri;
		}
		return false;
	}
}

// 장바구니 담기.
function addToCart() {

	// 장바구니에 담을 수 있는 지 확인한다.
	if (!checkForItem('cart')) {
		return;
	}

	var cartArrayRequiredItems = [];
	var $arrayRequiredItems = $('#op-item-cart-form').find('input[name=arrayRequiredItems]');

	if ($arrayRequiredItems.size() > 0) {
		$arrayRequiredItems.each(function(i) {
			cartArrayRequiredItems.push($arrayRequiredItems.eq(i).val());
		});
	}

	$.post('/m/cart/add-item-to-cart', $('#op-item-cart-form').serialize(), function (response) {
		clearSelectInformation();
		Common.responseHandler(response, function () {
			Shop.getCartInfo();

			Shop.openCartWishlistLayer('cart');

			/*
			var message = Message.get("M00595") + '\n' + Message.get("M00596");	// 장바구니에 상품을 담았습니다.\n바로 확인하시겠습니까?
			if (confirm(message)) {
				location.href = '/cart';
			}
			*/
			Ga.addToCart(cartArrayRequiredItems)
		});
	}, 'json');
}

//네이버 위시리스트
function naverWishInItemView() {

	// 장바구니에 담을 수 있는 지 확인한다.
	if (!checkForItem('naver_wishlist')) {
		return;
	}

	var returnUrl = "";
	$.ajaxSetup({'async': false});
	$.post('/open-market/naver-wishlist/mobile', $('#op-item-cart-form').serialize(), function (response) {
		Common.responseHandler(response, function (response) {
			returnUrl = response.data;
		}, function (response) {
			alert(response.errorMessage)
		});
	}, 'json');

	return returnUrl;
}

function naverPayInItem() {
	// 장바구니에 담을 수 있는 지 확인한다.
	if (!checkForItem('buy_now')) {
		return;
	}

	var returnUrl = "";

	$.ajaxSetup({'async': false});
	$.post('/m/cart/buy-now', $('#op-item-cart-form').serialize(), function (response) {
		Common.responseHandler(response, function () {
			$.post('/open-market/naver-buy-now/mobile', $('#op-item-cart-form').serialize(), function (response) {

				Common.responseHandler(response, function (response) {
					returnUrl = response.data;
				}, function (response) {
					alert(response.errorMessage)
				});
			}, 'json');
		});
	}, 'json');

	return returnUrl;
}

// 바로구매
function buyNow(loginCheck) {

	// 바로구매가 가능한 지 확인한다.
	if (!checkForItem('buy_now')) {
		return;
	}

	
	loginCheck = loginCheck == undefined ? true : loginCheck;

	$.post('/cart/buy-now', $('#op-item-cart-form').serialize(), function (response) {
		//clearSelectInformation();
		Common.responseHandler(response, function () {

			if (isLogin == 'false' && loginCheck == true) {
				location.href = "/m/users/login?target=/m/order/step1&popup=1&redirect=/m/order/step1&uri=/m/products/view/"+item.itemUserCode;
			} else {
				location.href = '/m/order/step1';
			}

		});
	}, 'json');
}

var PopupLogin = {};
PopupLogin.Callback = function () {
	buyNow(false);
}

// 모바일 로그인 후 바로구매 처리 [2018-04-19] yulsun.yoo
if (loginPopup == 'T') {
	buyNow(false);
}


// 장바구니/바로구매 가능한지 확인한다.
function checkForItem(target) {
	var isSet = item.itemType == '3';

	if (!(target === 'cart' || target === 'buy_now' || target == 'wishlist' || target === 'naver_wishlist')) {
		alert('처리할 수 없습니다.');
		return false;
	}

	var stockFlag = item.stockFlag;
	var stockQuantity = item.stockQuantity;
	var orderMinQuantity = item.orderMinQuantity;
	var orderMaxQuantity = item.orderMaxQuantity;

	// 품절 확인.
	if (item.isItemSoldOut) {
		alert('해당 상품은 판매 종료 되었습니다.');
		return false;
	}

	// 비회원 구매 불가인 경우 로그인 페이지로 이동
	if (checkForNonmemberOrder() == false) {
		return false;
	}

	if (target == 'wishlist' || target == 'naver_wishlist') {
		return true;
	}

	// 장바구니 체크.
	var $quantity = $('.quantity');

	if (isSet) {
		// 재고 체크
		if (!isAvailableSetStock('order')) {
			return false;
		}

		if (item.itemOptionFlag === 'Y') {
			var $addedSets = $('.op-added-sets li');
			if ($addedSets.size() == 0) {
				alert('세트상품을 선택해 주세요.');
				return false;
			}

			// 세트상품 필수 데이터 세팅
			$addedSets.each(function(i) {
				var setData = '<input type="hidden" name="itemSets[' + i + '].itemId" value="' + item.itemId + '">';
				setData += '<input type="hidden" name="itemSets[' + i + '].quantity" value="' + $(this).find('.op-option-quantity').val() + '">';
				$(this).find('.op-added-option p').each(function() {
					setData += '<input type="hidden" name="itemSets[' + i + '].arrayItemSets" value="' + $(this).data('item-set') + '">';
				});

				$(this).find('.op-added-data').html(setData);
			});
		} else {
			$('input[name="itemSets[0].quantity"]').val(Number($('.op-item-quantity .quantity').val()));
		}
	} else {
		if (item.itemOptionFlag == 'N') {

			// 상품 최소 수량 체크
			if ($quantity.val() < orderMinQuantity) {
				alert('해당 상품의 최소 구매 수량은 ' + orderMinQuantity + "개 입니다.");
				$quantity.val(orderMinQuantity);
				return false;
			}

			// 상품 최대 수량 체크
			if ($quantity.val() > orderMaxQuantity) {
				alert('해당 상품의 최대 구매 수량은 ' + orderMaxQuantity + "개 입니다.");
				$quantity.val(orderMaxQuantity);
				return false;
			}

			// 상품 재고 체크
			if (stockFlag == 'Y' && $quantity.val() > stockQuantity) {
				alert('해당 상품의 최대 구매 수량은 ' + stockQuantity + '개 입니다.');
				$quantity.val(stockQuantity);
				return false;
			}
		} else {
			// 옵션
			var $addedOptions = $('.op-added-options li');
			var $textOptions = $('.text-option-value');

			var rst = true;
			if ($addedOptions.size() == 0 && $('.text-option-value').size() > 0) {
				$textOptions.each(function () {
					if ($(this).val() == null || $(this).val() == '') {
						alert('텍스트 옵션값을 입력 후 옵션 추가 버튼을 눌러 옵션을 추가해주세요.');
						$(this).focus();
						rst = false;
						return false;
					}
				});

				if (rst == false) return false;
			}

			if ($('.text-option-value').size() > 0 && $addedOptions.size() == 0) {
				alert('옵션 추가 버튼을 눌러 옵션을 추가해주세요.');
				return false;

			} else if ($addedOptions.size() === 0) {
				alert('상품 필수옵션을 선택해 주세요.');
				$('.op-option-select-box').focus();
				return false;
			}

			var optionQuantity = 0;
			$addedOptions.each(function (index) {
				var optionData = item.itemId + '||' + $(this).find('.op-option-quantity').val() + '||' + $(this).data('option');
				optionQuantity += Number($(this).find('.op-option-quantity').val());
				$('input[name=arrayRequiredItems]').eq(index).val(optionData);

			});

			// 상품 최소 수량 체크
			if (optionQuantity < orderMinQuantity) {
				alert('해당 상품의 최소 구매 수량은 ' + orderMinQuantity + "개 입니다.");
				return false;
			}

			// 상품 최대 수량 체크
			if (optionQuantity > orderMaxQuantity) {
				alert('해당 상품의 최대 구매 수량은 ' + orderMaxQuantity + "개 입니다.");
				return false;
			}
		}
	}

	// 추가 구성 상품.
	var $addedItems = $('.added-items li');

	$addedItems.each(function (index) {
		var additionData = $(this).data('item-id') + '||' + $(this).find('.op-option-quantity').val() + '||';
		$('input[name=arrayAdditionItems]').eq(index).val(additionData);
	});

	return true;
}


// 관심상품 담기.
function addToWishList() {
	// 관심상품 담기 클래스 추가 2017-05-16 yulsun.yoo
	$(".favorite").toggleClass('on');

	
	if (isLogin == 'false') {
		alert('로그인 해주세요.');
		location.href = "/m/users/login?target="+requestUri;
		return;
	}

	var $wishlistGroupIds = $('input[name=wishlistGroupId]');
	$wishlistGroupIds.eq(0).prop('checked', true);

	var param = {
		'itemId': item.itemId
	}

	// 관심 상품으로 등록
// 	$.post('/m/wishlist/add', $('#op-item-cart-form').serialize(), function(response) {
	$.post('/m/wishlist/add', param, function (response) {
		Common.responseHandler(response, function () {
			resetWishlistItemCount(0);

			// 헤더에 위시리스트 카운트 표시
			Shop.getWishlistCount();

			// 확인 레이어
			Shop.openCartWishlistLayer('wishlist');

		});
		clearSelectInformation();
	}, 'json');

}

// 관심 상품 추가 가능 여부
function resetWishlistItemCount(addItemCount) {
	var $wishlistGroupIds = $('input[name=wishlistGroupId]');
	$wishlistGroupIds.each(function () {
		var $wishlistGroupId = $(this);
		var $tr = $wishlistGroupId.closest('tr');
		var $wishlistItemCount = $tr.find('.wishlist_item_count');
		var $wishlistMaxItemCount = $tr.find('.wishlist_max_item_count');

		var newItemCount = Number($wishlistItemCount.text()) + addItemCount;

		$wishlistGroupId.prop('checked', false);

		if (Number($wishlistItemCount.val()) >= Number($wishlistMaxItemCount.text())
			|| newItemCount > Number($wishlistMaxItemCount.text())) {
			//$wishlistGroupId.prop('disabled', true);   // 옵션상품 중 재고가 없는 경우 disabled 했으나 입하신청 때문에 주석으로 처리
		} else {
			//$wishlistGroupId.prop('disabled', false);	// 옵션상품 중 재고가 없는 경우 disabled 했으나 입하신청 때문에 주석으로 처리
		}
	});
}


// 선택정보 초기화
function clearSelectInformation() {
	var isSet = item.itemType === '3';

	// 관심상품.
	//closeLayer('wishlist_group_layer');

	$('#op-item-option-info select').val('');

	$('#cart_item').empty();

	if (item.itemOptionFlag === 'N') {
		var defaultQuantity = 1;
		$('.quantity').val(defaultQuantity);

		if (item.orderMinQuantity > 0) {
			$('.quantity').val(item.orderMinQuantity);
			defaultQuantity = item.orderMinQuantity;
		}

		if (isSet) {
			$('input[name="itemSets[0].quantity"]').val(defaultQuantity);
		} else {
			// append ArrayRequiredItems
			setArrayRequiredItems(item.itemId, defaultQuantity);
		}
	}

	$('.op-added-options').empty();
	$('.op-added-sets').empty();

	calculate();
}

// 이용후기, QnA 제목 클릭 이벤트 (내용 오픈)
function detailClickEvent() {
	$('.list-data-subject a').on('click',  function(e) {
		e.preventDefault();
		if($(this).parent().hasClass('on')) {
			$('.main_txt').removeClass('on');
			$('.list-data-subject li').removeClass('on');
			$('.sub_txt').hide();
		} else {
			$(this).parent().addClass('on');
			$('.sub_txt').hide();
			$(this).closest('li').find($('.sub_txt')).show();
		}
	});
}

var currentReviewPage = 0;
// 리뷰 페이징.
function paginationReviewMore(page) {
	currentReviewPage++;

	var param = {
		'page': currentReviewPage,
		'where': 'ITEM_ID',
		'query': item.itemId,
		'itemId': item.itemId
	};
	$.post('/m/item/review-list', param, function (response) {
		$('#op-review-list').append(response);
		showHideReviewMoreButton();
	}, 'html');
}

//리뷰 더보기 버튼 hide 처리
function showHideReviewMoreButton() {

	var totalItems = 0;
	var $info = $("#op-review-list").find('#page-' + currentReviewPage);

	if ($info.length > 0) {
		totalItems = Number($info.find('.total-items').text());
	}

	if ($("#op-review-list").find(' > li').size() == totalItems || totalItems == 0) {
		$('.op-review-more-load').hide();
	}
}

var currentQnaPage = 0;

// QNA 페이징.
function paginationQnaMore() {
	currentQnaPage++;

	var param = {
		'page': currentQnaPage,
		'itemId': item.itemId
	};

	$.post('/m/item/qna-list', param, function (response) {
		$('#op-qna-list').append(response);
		showHideQnaMoreButton();
	}, 'html');
}

//QNA 더보기 버튼 hide 처리
function showHideQnaMoreButton() {

	var totalItems = 0;
	var $info = $("#op-qna-list").find('#page-' + currentQnaPage);

	if ($info.length > 0) {
		totalItems = Number($info.find('.total-items').text());
	}

	if ($("#op-qna-list").find(' > li').size() == totalItems || totalItems == 0) {
		$('.op-qna-more-load').hide();
	}
}


// 쿠폰 다운로드
//(2014.10.28)
function downloadCoupon() {
	//Common.popup('/m/item/coupon/' + item.itemId, 'download-coupon', 800, 600);
	location.href = '/m/item/coupon/' + item.itemId;
}

// 카드 혜택
function cardBenefitsPopup() {
	location.href = '/m/item/cardBenefitsPopup';
}

function itemImagesDetail(itemId) {
	//location.href = '/m/item/details-image-view?itemId=' + itemId + '&itemImageId=' + itemImageId;
	location.href = '/m/item/details-image-view?itemId=' + itemId;

}

function setArrayRequiredItems(itemId, quantity) {
	var template = '<input type="hidden" name="arrayRequiredItems" value="' + itemId + '||' + quantity + '||" />';
	$('#op-cart-item').empty().append(template);
}

function restockNotice() {

	if (isRestockNotice == 'true' || $('.restock_btn_wrap .restock').hasClass('on')) {
		return false;
	}

	if (isLogin == 'false') {
		alert('로그인해주세요.');
		location.href = "/m/users/login?target="+requestUri;
		return;
	}

	var param = {
		'itemId': item.itemId
	}

	// 관심 상품으로 등록
	$.post('/restock-notice/add', param, function (response) {
		Common.responseHandler(response, function () {
			$('.restock_btn_wrap .restock').addClass('on');
			alert('재입고 알림을 신청했습니다.');
		});

	}, 'json');
}

function viewItemRelations() {
	$.get('/m/item/item-relations/' + item.itemId, null, function (response) {
		$('#op-item-relation').empty().html(response);
		relation();
	}, 'html');
}

function setBenefitInfo() {

	$('.item-point').hide();
	$('.item-card-benefit').hide();

	$.get('/item/benefit-info/' + item.itemId, null, function (response) {
		Common.responseHandler(response, function (response) {

			var data = response.data;

			if (data != null) {

				var pointPolicy = data.pointPolicy,
					cardBenefits = data.cardBenefits,
					itemEarnPoint = data.itemEarnPoint;

				if (itemEarnPoint != null) {

					$('.item-point span').text(Common.numberFormat(itemEarnPoint.totalPoint) + ' P');
					$('.item-point').show();
				}

				if (cardBenefits != null) {
					var content = cardBenefits.content;
					if (content != null && content != '') {
						$('.item-card-benefit span').text(cardBenefits.subject);
						$('.item-card-benefit').show();
					}
				}
			}

		});
	});
}

function setCustomerInfo() {
	$.get('/item/customer-info/' + item.itemId, null, function (response) {
		Common.responseHandler(response, function (response) {
			var data = response.data;

			if (data != null) {
				$('.item-review-count').text('(' + Common.numberFormat(data.reviewCount) + ')');
				$('.item-qna-count').text('(' + Common.numberFormat(data.qnaCount) + ')');
			}
		});
	});
}
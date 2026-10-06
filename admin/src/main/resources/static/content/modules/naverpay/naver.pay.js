/**
 * Naver Pay 공통 스크립트 

 * @package    
 * @author     skc
 * @copyright  (c) 2015 Onlinepowers Development Team
 * 
 * ----------	------		------------------------------------------------------------
 *  수정일		수정자		수정내용
 * ----------	------		------------------------------------------------------------
 * 2017-07-05	윤민애		
 */

// 주문 정보 등록 URL - item-page-button.tag, cart-page-button.tag의 스크립트 중 NaverPay 객제 참조 2017-09-01 seungil.lee

/*var NaverPay = {
	'BASE_DOMAIN': 'test-pay.naver.com',
	'BASE_MOBILE_DOMAIN' : 'm.test-pay.naver.com'
};*/

// 네이버 페이 요청 파라미터를 가져옴.
function getNaverPayParamAfterVerification(requestType) {
	var naverOrder = new Array();
	var orderType = "item";
	
	// 상품이 배열이면 장바구니 아니면 상품상세로 구분
	if (item.constructor === Array) {
		orderType = "cart";

		var $itemCart = $('input[name="id"]:checked');
		if ($itemCart.length == 0) {
			alert("상품을 선택해주세요.");
			return false;
		}

		item = new Array();
		$itemCart.each(function(){
			item.push(itemCart["item_"+$(this).val()]);
		});

		naverOrder = item;
	}
	else if (item.constructor === Object) {
		naverOrder.push(item);
	}
	
	var data = new Array();
	if (orderType == "item") {
		var $options = $('.added-options li');
		var $optionQuantity = ".option-quantity";

		if (NaverPay.deviceType == 'mobile') {
			$options = $('.op-added-options li');
			$optionQuantity = ".op-option-quantity";
		}

		// 품절 확인.
		if (item.isItemSoldOut) {
			alert('해당 상품은 판매 종료 되었습니다.');
			return false;
		}

		if (requestType == 'order') {
			// 주문가능 상태 체크
			if (item.itemOptionFlag == "N") {
				if ($(".quantity").val() < item.orderMinQuantity) {
					alert('해당 상품의 최소 구매 수량은 ' + item.orderMinQuantity + "개 입니다.");
					$('.quantity').val(item.orderMinQuantity);
					return false;
				}

				// 상품 최대 수량 체크
				if ($(".quantity").val() > item.orderMaxQuantity) {
					alert('해당 상품의 최대 구매 수량은 ' + item.orderMaxQuantity + "개 입니다.");
					$('.quantity').val(item.orderMaxQuantity);
					return false;
				}

				if (item.stockFlag == 'Y' && $(".quantity").val() > item.stockQuantity) {
					alert('해당 상품의 최대 구매 수량은 ' + item.stockQuantity + '개 입니다.');
					$('.quantity').val(item.stockQuantity);
					return false;
				}
			} else {
				if ($options.size() == 0) {
					alert("상품필수옵션을 선택해주세요.");
					return false;
				}

				var optionQuantity = 0;
				$options.each(function() {
					optionQuantity += Number($(this).find($optionQuantity).val());
				});

				// 상품 최소 수량 체크
				if (optionQuantity < item.orderMinQuantity) {
					alert('해당 상품의 최소 구매 수량은 ' + item.orderMinQuantity + "개 입니다.");
					return false;
				}

				// 상품 최대 수량 체크
				if (optionQuantity > item.orderMaxQuantity) {
					alert('해당 상품의 최대 구매 수량은 ' + item.orderMaxQuantity + "개 입니다.");
					return false;
				}
			}

		}

		for (var i=0; i<naverOrder.length; i++) {
			// 상품
			var itemPrice = naverOrder[i].price;
			var quantity = $('.quantity').val();
			quantity = Number(quantity == undefined ? 0 : quantity);
			
			// 옵션 
			var optionPrice = 0;
			var optionItemPrice = 0;
			var optionArray = new Array();
			if ($options.size() > 0 && requestType == 'order') {
				
				$options.each(function(i) {
					var optionValue = $(this).data("option");
					var optionValues = "";

					$options.eq(i).find(".item-name span").each(function(j) {
						if (j > 0) {
							optionValues += " | ";
						}
						optionValues += $(this).text();
					});

					optionItemPrice = 0;
					$options.eq(i).find(".item-name input[name='optionPrice']").each(function () {
						optionItemPrice += $.trim($(this).val()) == '' ? 0 : Number($(this).val());
					});

					if (optionItemPrice > 0) {
						optionValues += ' (+' + optionItemPrice + '원)';
					}

					var optionQuantity = $(this).find($optionQuantity).val();

					if (optionValue == '') {
						//alert(optionTitle + ' 항목을 선택해 주십시오.');
						$(this).focus();
						return false;
					}

					optionPrice += optionItemPrice * optionQuantity;
					quantity += Number(optionQuantity);
					
					var optionList = {
						"optionText" : optionValues,
						"optionPrice" : Number(optionItemPrice),
						"optionQuantity" : Number(optionQuantity)
					};
					
					// option을 optionArray에 담아 vo로 전달
					optionArray.push(optionList);
				});
			}
			
			var orderPayAmount = itemPrice * quantity + optionPrice;
			
			var shippingAmount = 0;
			if (shipping.shippingType == '1') {
				shippingAmount = 0;
			}
			else if (shipping.shippingType == '2' || shipping.shippingType == '3' || shipping.shippingType == '4') {
				
				if (orderPayAmount >= shipping.shippingFreeAmount) {
					shippingAmount = 0;
				}
				else {
					shippingAmount = shipping.shipping;
				}
			}
			else if (shipping.shippingType == '5') {
				var count = Math.round(quantity/shipping.shippingItemCount); 
				shippingAmount = shipping.shipping * count;
			}
			else if (shipping.shippingType == '6') {
				shippingAmount = shipping.shipping;
			}
			var orderItem = {
				'itemId': naverOrder[i].itemId,
				'itemUserCode': naverOrder[i].itemUserCode,
				'itemName': $("#itemTitle").text(),
				'imageSrc': naverOrder[i].imageSrc,
				'quantity': quantity,
				'optionPrice': optionItemPrice,
				'price': Number(itemPrice),
				'itemPrice': Number(itemPrice),
				'shipping': Number(shippingAmount)
			};
			
			if (requestType == 'order' && naverOrder[i].itemOptionFlag == "Y") {
				orderItem.optionArray = optionArray;
			}

			data.push(orderItem);
		}
	}
	else {
		data = naverOrder;
	}
	return {
		'isValid': true,
		'orderType': orderType,
		'deviceType': NaverPay.deviceType,
		'data': data
	}
};

// 구매하기 
function NaverPay_buyButtonHandler() {
	
	var naverPayParam = getNaverPayParamAfterVerification('order');
	
	if (!naverPayParam.isValid) {
		return false;
	}
	
	$.post("/open-market/checkOutReturn", JSON.stringify(naverPayParam), function(response){
		if (response.isSuccess) {
			var resp = response.data;
			if (resp.flag1) {
				if (resp.isMobile == true) {
					location.href = NaverPay.payPopupUrl + '?ORDER_ID=' + resp.orderKey+'&SHOP_ID=' + resp.shopId + '&TOTAL_PRICE=' + resp.price;
				} else {
					Common.popup(NaverPay.payPopupUrl + '?ORDER_ID='+resp.orderKey+'&SHOP_ID=' + resp.shopId + '&TOTAL_PRICE='+resp.price, 'checkout', 1100, 800, 'yes');
				}
			} else {
				alert('네트워크 문제로 실패되었습니다.');
			}
		}
		else {
			alert(response.errorMessage);
		}
	});
}


// 찜하기 
function NaverPay_wishlistButtonHandler() {
	var naverPayParam = getNaverPayParamAfterVerification('wish');
	
	if (!naverPayParam.isValid) {
		return false;
	}
	
	$.post('/open-market/checkOutWishReturn', JSON.stringify(naverPayParam), function(response) {
		var resp = response.data;
		if (resp.flag1) {
			var itemId = resp.itemId;
			
			if (itemId == '') {
				alert('상품정보가 Naver Pay에 정확히 전송되지 않았습니다.');
				return;
			}
			
			if (resp.isMobile == true) {
				location.href = NaverPay.wishlistPopupUrl + '?SHOP_ID=' + resp.shopId + '&ITEM_ID=' + itemId;
			} else {
				Common.popup(NaverPay.wishlistPopupUrl + '?SHOP_ID=' + resp.shopId+ '&ITEM_ID=' + itemId, 'checkout', 380, 291);
			}
			
		} else {
			//alert(resp.message);
			alert('네트워크 문제로 실패되었습니다.');
		}
	}, 'json');
}

// 주문하기 (장바구니용)
function NaverPay_buyButtonHandlerForCart() {
	
	var naverPayParam = getNaverPayParamAfterVerification('order');
	//"https://test-pay.naver.com/customer/api/order.nhn?SHOP_ID=wjb1588&CERTI_KEY=7AE081F3-87DE-4F01-978A-2F9A5880EE28
	//&ITEM_ID=a1&ITEM_NAME=item1&ITEM_TPRICE=2000&ITEM_OPTION=&ITEM_UPRICE=&ITEM_COUNT=1
	//&BACK_URL=http://www.naver.com
	//&TOTAL_PRICE=4500&SHIPPING_TYPE=PAYED&SHIPPING_PRICE=2500";
	
	if (!naverPayParam.isValid) {
		return false;
	}
	
	//jQuery.ajaxSetup({ cache: false });
	jQuery.ajax({
		url: "/open-market/checkOutReturn",
		data : naverPayParam.data,
		dataType: "json",
		type: "POST",
		//async: true,
		success:function(response){
			var resp = response.data;
			
			if (resp.flag1) {
				//alert(resp.message);
				//resp.flag2는 팝업이냐 아니냐
				if (resp.flag2 == true) {
					Common.popup(NaverPay.payPopupUrl + '?ORDER_ID='+resp.orderKey+'&SHOP_ID=onlinepowers&TOTAL_PRICE='+resp.price, 'checkout', 1100, 800, 'yes');
//					location.href = 'https://' + NaverPay.BASE_DOMAIN  + '/customer/order.nhn?ORDER_ID='+resp.oderKey+'&SHOP_ID=onlinepowers&TOTAL_PRICE='+resp.price;
				} else {
					Common.popup(NaverPay.payPopupUrl + '?ORDER_ID='+resp.orderKey+'&SHOP_ID=onlinepowers&TOTAL_PRICE='+resp.price, 'checkout', 1100, 800, 'yes');
				}
			} else {
				//alert(resp.message);
				alert('네트워크 문제로 실패되었습니다.');
			}
			
			
		},
		error:function(xhr, sataus, e){
			alert(e);
		}
	});
	
}

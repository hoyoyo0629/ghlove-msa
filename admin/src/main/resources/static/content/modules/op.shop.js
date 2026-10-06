var Shop = {
    'mobilePrefix' : '/m'
};

var reasonType = {
	'1' : '기부금 모금 실적 확인',
	'2' : '답례품 주문내역 확인',
	'3' : '택배 송장 출력 및 배송확인',
	'4' : '답례품 정산 및 지급 확인',
	'5' : '기타'
}

/**
 * 모바일 페이지 여부 (디바이스 체크 아님)
 */

Shop.isMobilePage = (function () {
    var mobileCheck = location.href.split("/");

    for (var index in mobileCheck) {
        if (mobileCheck[index] == "m") {
            return true;
        }
    }
    return false;
}());


Shop.url = function(url) {
    if (Shop.isMobilePage) {
        return Shop.mobilePrefix + url;
    }
    return url;
};


/**
 * 상품검색
 */
Shop.findItem = function(targetId) {
    Common.popup("/opmanager/item/find-item?targetId=" + targetId, 'find-item', 1000, 600, 1);
};



// 관련상품 추가
Shop.addRelationItem = function(id, item) {
    var key = id + '_item_' + item.itemId;
    var itemCount = $('#' + key).size();
    if (itemCount == 0) {

        if (typeof findItemCallback == 'function') {
            findItemCallback(id, item);
        } else {
            var ordering = $('input[name=' + id + 'ItemIds]').size() + 1;
            var html = '';

            if (id === 'set') {
                var displayFlagText = '공개';
                if (item.displayFlag === 'N') {
                    displayFlagText = '<span style="color:#e84700">비공개</span>';
                }

                var itemSaleStatusText = '판매중';
                if (item.dataStatusCode === '20' || item.dataStatusCode === '31' || item.dataStatusCode === '40' || item.dataStatusCode === '41') {
                    itemSaleStatusText = '등록대기';
                } else if (item.dataStatusCode === '21') {
                    itemSaleStatusText = '<a href="#" class="show-reject-message">등록보류</a>';
                } else if (item.dataStatusCode === '30') {
                    itemSaleStatusText = '재등록신청';
                } else if (item.dataStatusCode === '90') {
                    itemSaleStatusText = '판매종료';
                } else if (item.itemSoldOutFlag == 'Y') {
                    itemSaleStatusText = '<span style="color:#e84700">품절</span>';
                }

                html += '<tr id="' + key + '" class="set_items" data-name="' + item.itemName + '" data-price="' + item.itemSalePrice + '" ' +
                                'data-min-quantity="' + item.orderMinQuantity + '" data-max-quantity="' + item.orderMaxQuantity + '">';
                html +=     '<td class="text-center">';
                html +=         '<input type="hidden" name="setItemIds" value="' + item.itemId + '" />';
                html +=         '<span class="ordering">' + ordering + '</span>';
                html +=     '</td>';
                html +=     '<td>';
                html +=         '<div>';
                html +=             '<img src="' + item.itemImage + '" class="item_image" alt="' + item.itemName + '" />';
                html +=         '</div>';
                html +=     '</td>';
                html +=     '<td class="left break-word">';
                html +=         '[' + item.itemUserCode + ']<br/>' + item.itemName;
                html +=     '</td>';
                html +=     '<td class="text-center">';
                html +=         item.itemOptionFlag;
                html +=     '</td>';
                html +=     '<td class="text-right">';
                html +=         Common.numberFormat(item.itemSalePrice) + '원';
                html +=     '</td>';
                html +=     '<td class="text-center">';
                html +=         itemSaleStatusText;
                html +=     '</td>';
                html +=     '<td class="text-center">';
                html +=         displayFlagText;
                html +=     '</td>';
                html +=     '<td class="text-center">';
                html +=         '<div>';
                html +=             '<input type="text" name="setQuantities" class="form-half _number" maxlength="3" value="1" /> 개';
                html +=         '</div>';
                html +=     '</td>';
                html +=     '<td class="text-center">';
                html +=         '<a href="javascript:deleteItemSet(\'select\', \'' + key + '\');" class="delete_item_image btn btn-gradient btn-xs">삭제</a>';
                html +=     '</td>';

                $('#' + id + ' .no_content').remove();
                $('#' + id).append(html);

                if (typeof initItemSetEvent == 'function') {
                    initItemSetEvent();
                }
            } else {
                html += '<li id="' + key + '">';
                if (id == "group-banner") {
                    html += '<input type="hidden" name="itemList" value="' + item.itemId + '" />';
                } else {
                    html += '	<input type="hidden" name="' + id + 'ItemIds" value="' + item.itemId + '" />';
                }
                html += '	<p class="image"><img src="' + item.itemImage + '" class="item_image size-100 none" alt="' + item.itemName + '" /></p>';

                html += '	<p class="title">';
                if (item.dataStatusCode == '1') {
					if (item.itemSoldOutFlag == 'Y') {
                		html += '<strong style="color:red">[품절]</strong>';
					}
					if (item.displayFlag == 'N') {
						html += '<strong style="color:red">[비공개]</strong>';
					}
				} else {
					html += '<strong style="color:red">[비공개]</strong>';
				}

                //html += '	<p class="title">[' + item.itemUserCode + ']<br />' + item.itemName + '</p>';
                html += '[' + item.itemUserCode + ']';
                html += '<label style="color:red">[' + Common.numberFormat(item.itemSalePrice) + ']</label><br />'
                html += item.itemName + '</p>';
                if (item.targerId != "favProd" && item.targerId != "rprsProd") {
                	html += '	<span class="ordering">' + ordering + '</span>';
                }
                html += '	<a href="javascript:Shop.deleteRelationItem(\'' + key + '\');" class="delete_item_image"><img src="/content/opmanager/images/icon/icon_x.gif" alt="" /></a>';
                html += '</li>';

				if (item.targerId == "favProd") {
					$('#' + id).empty();
					$('#itemId').val(item.itemId);
				}
				$('#' + id).append(html);
            }
        }
    }
};

// 오프라인 답례품 추가
Shop.addRelationItemOff = function(id, item) {
    var key = id + '_item_' + item.itemId;
    var itemCount = $('#' + key).size();
    if (itemCount == 0) {

        if (typeof findItemCallback == 'function') {
            findItemCallback(id, item);
        } else {
            var ordering = $('input[name=' + id + 'ItemIds]').size() + 1;
            var html = '';

            if (id === 'set') {
                var displayFlagText = '공개';
                if (item.displayFlag === 'N') {
                    displayFlagText = '<span style="color:#e84700">비공개</span>';
                }

                var itemSaleStatusText = '판매중';
                if (item.dataStatusCode === '20' || item.dataStatusCode === '31' || item.dataStatusCode === '40' || item.dataStatusCode === '41') {
                    itemSaleStatusText = '등록대기';
                } else if (item.dataStatusCode === '21') {
                    itemSaleStatusText = '<a href="#" class="show-reject-message">등록보류</a>';
                } else if (item.dataStatusCode === '30') {
                    itemSaleStatusText = '재등록신청';
                } else if (item.dataStatusCode === '90') {
                    itemSaleStatusText = '판매종료';
                } else if (item.itemSoldOutFlag == 'Y') {
                    itemSaleStatusText = '<span style="color:#e84700">품절</span>';
                }

                html += '<tr id="' + key + '" class="set_items" data-name="' + item.itemName + '" data-price="' + item.itemSalePrice + '" ' +
                                'data-min-quantity="' + item.orderMinQuantity + '" data-max-quantity="' + item.orderMaxQuantity + '">';
                html +=     '<td class="text-center">';
                html +=         '<input type="hidden" name="setItemIds" value="' + item.itemId + '" />';
                html +=         '<span class="ordering">' + ordering + '</span>';
                html +=     '</td>';
                html +=     '<td>';
                html +=         '<div>';
                html +=             '<img src="' + item.itemImage + '" class="item_image" alt="' + item.itemName + '" />';
                html +=         '</div>';
                html +=     '</td>';
                html +=     '<td class="left break-word">';
                html +=         '[' + item.itemUserCode + ']<br/>' + item.itemName;
                html +=     '</td>';
                html +=     '<td class="text-center">';
                html +=         item.itemOptionFlag;
                html +=     '</td>';
                html +=     '<td class="text-right">';
                html +=         Common.numberFormat(item.itemSalePrice) + '원';
                html +=     '</td>';
                html +=     '<td class="text-center">';
                html +=         itemSaleStatusText;
                html +=     '</td>';
                html +=     '<td class="text-center">';
                html +=         displayFlagText;
                html +=     '</td>';
                html +=     '<td class="text-center">';
                html +=         '<div>';
                html +=             '<input type="text" name="setQuantities" class="form-half _number" maxlength="3" value="1" /> 개';
                html +=         '</div>';
                html +=     '</td>';
                html +=     '<td class="text-center">';
                html +=         '<a href="javascript:deleteItemSet(\'select\', \'' + key + '\');" class="delete_item_image btn btn-gradient btn-xs">삭제</a>';
                html +=     '</td>';

                $('#' + id + ' .no_content').remove();
                $('#' + id).append(html);

                if (typeof initItemSetEvent == 'function') {
                    initItemSetEvent();
                }
            } else {
                html += '<li id="' + key + '">';
                if (id == "group-banner") {
                    html += '<input type="hidden" name="itemList" value="' + item.itemId + '" />';
                } else {
                    html += '	<input type="hidden" name="' + id + 'ItemIds" value="' + item.itemId + '" />';
                }

				// 상품의 원가 및 옵션 가격을 hidden으로 저장 - 추후 금액 변동 시 사용하기 위함
                html += '<input type="hidden" id="default_price_' + item.itemId +'" value="' + item.itemSalePrice + '">'
                html += '<input type="hidden" id="option_price_' + item.itemId +'" value="">'

				// span태그의 id=price_area_itemId로 동적으로 변경된 금액을 수정
                html += '<label style="color:red"><span id="price_area_' + item.itemId +  '">[' + Common.numberFormat(item.itemSalePrice) + 'P 답례품]</span></label>'
                html += '	<p class="image"><img src="' + item.itemImage + '" class="item_image size-100 none" alt="' + item.itemName + '" /></p>';
                html += '	<p class="off_prod">';
                if (item.dataStatusCode == '1') {
					if (item.itemSoldOutFlag == 'Y') {
                		html += '<strong style="color:red">[품절]</strong>';
					}
					if (item.displayFlag == 'N') {
						html += '<strong style="color:red">[비공개]</strong>';
					}
				} else {
					html += '<strong style="color:red">[비공개]</strong>';
				}
                html += '<span id="itemName_' + item.itemId + '">' + item.itemName + '</span>'
				html += '</p>'
				html += '	<a href="javascript:Shop.deleteRelationItemOff(\'' + key + '\');" class="delete_item_image"><img src="/content/opmanager/images/icon/icon_x.gif" alt="" /></a>';
                html += '</li>';

				$('#' + id).append(html);
            }
        }
    }
};

// 관련상품 추가여부 확인
Shop.isAddedRelationItem = function(id, itemId) {
    var key = id + '_item_' + itemId;
    var itemCount = $('#' + key).size();
    if (itemCount == 0) {
        return false;
    }
    return true;
};

// 관련상품 삭제
Shop.deleteRelationItem = function(key) {
	var id = key.substring(0,13);
	if(id == "favProd_item_"){
		$('#itemId').val(0);
	};
    $('#' + key).remove();
};

// 오프라인 대표 답례품 삭제
Shop.deleteRelationItemOff = function(key) {
	var id = key.substring(0,16);
	if(id == "favProd_item_"){
		$('#itemId').val(0);
	};
    $('#' + key).remove();
};

// 관련상품 삭제
Shop.deleteRelationItemAll = function(targetId) {
    $('#' + targetId).find('li, tr').remove();
};

Shop.isAvilableRelationItemCount = function(targetId, maxCount, addCount) {
    var count = $('#' + targetId).find('li, tr').size();
    if (addCount) {
		maxCount -= addCount;
	}
    if (count > maxCount) {
        return false;
    }

    return true;
};

// 회원만 상세페이지 접속 가능.
Shop.loginToItemDetailPage = function(itemUserCode) {
    var message = Message.get("M00775") + '\n' + Message.get("M00602");	// 로그인 후 확인이 가능합니다.\n 로그인 페이지로 이동하시겠습니까?
    if (confirm(message)) {
        location.href="/users/login?target=/products/view/" + itemUserCode;
    }
};



var ShopEventHandler = {};


/**
 * 팀/그룹 > 1차 > 2차 > 3차 > 4차 셀렉트 박스 선택 이벤트
 */
var CURRENT_SELECT_CATEGORY_LEVEL;
ShopEventHandler.categorySelectboxChagneEvent = function() {

    if ($('#categoryGroupId').size() > 0) {
        // 카테고리 그룹 선택
        $('#categoryGroupId').on('change', function(){
            var categoryGroupId = $(this).val();
            $('#categoryId').val("");
            Shop.getCategoriesByCategoryGroupId(categoryGroupId, "");
        });


        // 1차 카테고리 목록 조회 (by categoryGroupId)
        Shop.getCategoriesByCategoryGroupId = function(categoryGroupId, categoryClass1) {
            Common.loading.display = false;
            $.post('/categories/options-by-groupid', {'categoryGroupId' : categoryGroupId, 'categoryClass1': categoryClass1}, function(response) {
                var optionTitles = [];

                for (var i = 1; i <= 4; i++) {
                    optionTitles[i] = '<option value="">= ' + (i+1) + Message.get("M00075") + ' =</option>';	// {}차 카테고리
                }


                $('select.category:not(#categoryGroupId)').empty().each(function(i) {
                    $(this).append(optionTitles[i + 1]);
                });
                $('#categoryClass1').append(response);
            }, 'html');
        };

        // 1~3차 카테고리 선택
        $(document).on("change", '#categoryClass1, #categoryClass2, #categoryClass3', function() {
            var categoryClass = $(this).val();
            var categoryId = $(this).find('option:selected').attr('rel');

            $('#categoryId').val(categoryId);

            CURRENT_SELECT_CATEGORY_LEVEL = $(this).attr('id').replace('categoryClass', '');
            Shop.getCategorisByCategoryClass(CURRENT_SELECT_CATEGORY_LEVEL, categoryClass, "");
        });


        Shop.getCategorisByCategoryClass = function(categoryLevel, categoryClass, categoryClass1) {
            CURRENT_SELECT_CATEGORY_LEVEL = categoryLevel;
            $.post('/categories/options', {'categoryClass' : categoryClass, 'categoryClass1' : categoryClass1}, function(response) {
                var nextCategoryLevel = Number(CURRENT_SELECT_CATEGORY_LEVEL) + 1;

                for (var i = nextCategoryLevel; i <= 4; i++) {
                    var optionTitle = '<option value="">= ' + (i+1) + '' + Message.get("M00075") + ' =</option>';	// {}차 카테고리
                    $('#categoryClass' + i).empty().append(optionTitle);
                }
                $('#categoryClass' + nextCategoryLevel).append(response);
            }, 'html');
        };

        // 카테고리 검색 활성화
        Shop.activeCategoryClass = function(categoryGroupId, categoryClass1, categoryClass2, categoryClass3, categoryClass4) {
            $.ajaxSetup({'async': false});
            if (categoryGroupId != '') {
                Shop.getCategoriesByCategoryGroupId(categoryGroupId, categoryClass1);
            }

            if (categoryClass1 != '') {
                Shop.getCategorisByCategoryClass("1", categoryClass1, categoryClass2);
            }

            if (categoryClass2 != '') {
                Shop.getCategorisByCategoryClass("2", categoryClass2, categoryClass3);
            }

            if (categoryClass3 != '') {
                Shop.getCategorisByCategoryClass("3", categoryClass3, categoryClass4);
            }

            $.ajaxSetup({'async': true});
        };
    }
};



/**
 * 팀 > 그룹 > 1차
 */
var CURRENT_SELECT_CATEGORY_LEVEL;
ShopEventHandler.categorySelectboxChagneEvent2 = function() {

    if ($('#code').size() > 0) {

        // 카테고리 그룹 선택

        $('#code').on('change', function(){
            var teamId = $(this).val();
            Shop.getCategoriesByCategoryTeamId(teamId, "");
        });

        $('#categoryGroupId').on('change', function(){
            var categoryGroupId = $(this).val();
            $('#categoryId').val("");
            Shop.getCategoriesByCategoryGroupId(categoryGroupId, "");
        });


        // 1차 카테고리 목록 조회 (by categoryGroupId)

        Shop.getCategoriesByCategoryTeamId = function(categoryTeamId, categoryGroupId) {
            Common.loading.display = false;
            $.post('/categories/options-by-groupid2', {'categoryTeamId' : categoryTeamId, 'categoryGroupId': categoryGroupId}, function(response) {
                var optionTitles = [];

                for (var i = 1; i <= 4; i++) {
                    optionTitles[i] = '<option value="">= ' + i + Message.get("M00075") + ' =</option>';	// {}차 카테고리
                }


                $('select.category:not(#categoryGroupId)').empty().each(function(i) {
                    $(this).append(optionTitles[i + 1]);
                });
                $('#categoryGroupId').html(response);
            }, 'html');
        };

        Shop.getCategoriesByCategoryGroupId = function(categoryGroupId, categoryClass1) {
            Common.loading.display = false;
            $.post('/categories/options-by-groupid', {'categoryGroupId' : categoryGroupId, 'categoryClass1': categoryClass1}, function(response) {
                var optionTitles = [];

                for (var i = 1; i <= 4; i++) {
                    optionTitles[i] = '<option value="">= ' + i + Message.get("M00075") + ' =</option>';	// {}차 카테고리
                }


                $('select.category:not(#categoryGroupId)').empty().each(function(i) {
                    $(this).append(optionTitles[i + 1]);
                });
                $('#categoryClass1').append(response);
            }, 'html');
        };


        // 1~3차 카테고리 선택
        $(document).on("change", '#categoryClass1, #categoryClass2, #categoryClass3', function() {
            var categoryClass = $(this).val();
            var categoryId = $(this).find('option:selected').attr('rel');

            $('#categoryId').val(categoryId);

            CURRENT_SELECT_CATEGORY_LEVEL = $(this).attr('id').replace('categoryClass', '');
            Shop.getCategorisByCategoryClass(CURRENT_SELECT_CATEGORY_LEVEL, categoryClass, "");
        });

        Shop.getCategorisByCategoryClass = function(categoryLevel, categoryClass, categoryClass1) {
            CURRENT_SELECT_CATEGORY_LEVEL = categoryLevel;
            $.post('/categories/options', {'categoryClass' : categoryClass, 'categoryClass1' : categoryClass1}, function(response) {
                var nextCategoryLevel = Number(CURRENT_SELECT_CATEGORY_LEVEL) + 1;

                for (var i = nextCategoryLevel; i <= 4; i++) {
                    var optionTitle = '<option value="">= ' + i + '' + Message.get("M00075") + ' =</option>';	// {}차 카테고리
                    $('#categoryClass' + i).empty().append(optionTitle);
                }
                $('#categoryClass' + nextCategoryLevel).append(response);
            }, 'html');
        };

        // 카테고리 검색 활성화
        Shop.activeCategoryClass2 = function(code, categoryGroupId, categoryClass1, categoryClass2, categoryClass3, categoryClass4) {
            $.ajaxSetup({'async': false});

            if (code != '') {
                Shop.getCategoriesByCategoryTeamId(code, categoryGroupId);
            }

            if (categoryGroupId != '') {
                Shop.getCategoriesByCategoryGroupId(categoryGroupId, categoryClass1);
            }

            if (categoryClass1 != '') {
                Shop.getCategorisByCategoryClass("1", categoryClass1, categoryClass2);
            }

            if (categoryClass2 != '') {
                Shop.getCategorisByCategoryClass("2", categoryClass2, categoryClass3);
            }

            if (categoryClass3 != '') {
                Shop.getCategorisByCategoryClass("3", categoryClass3, categoryClass4);
            }

            $.ajaxSetup({'async': true});
        };
    }
};

function seoCopy($event){
    var flag = $event.attr("id");
    var inputName = $event.prev().attr("name");
    var inputName2 = $event.prev().attr("name").replace("rankSeo","categoriesSeo").replace("reviewSeo","categoriesSeo");
    switch (flag)
    {
        case "rank_title":
        case "rank_headercontents":
        case "rank_themawordtitle":
            $("input[name='"+inputName+"']").val($("input[name='"+inputName2+"']").val().replace("通販", "売れ筋人気ランキング"));
            break;
        case "rank_keyword":
            $("input[name='"+inputName+"']").val($("input[name='"+inputName2+"']").val().replace("通販", "売れ筋,人気,ランキング"));
            break;

        case "rev_title":
        case "rev_headercontents":
        case "rev_themawordtitle":
            $("input[name='"+inputName+"']").val($("input[name='"+inputName2+"']").val().replace("通販", "口コミ・レビュー"));
            break;
        case "rev_keyword":
            $("input[name='"+inputName+"']").val($("input[name='"+inputName2+"']").val().replace("通販","口コミ,レビュー"));
            break;
    }

}

/**
 * 해당 차수에 child category의 옵션을 가져온다.
 * 파라미터가 없으면 : TEAM 목록
 * categoryTeamCode : GROUP 목록
 * categoryGroupCode : 1차 카테고리 목록
 * categoryUrl : 하위 카테고리 목록.
 *
 * @param param object = {'categoryTeamCode' : '', categoryGroupCode : '', categoryUrl : '', categoryCode: ''} // categoryCode ==> 활성화 코드.(selected="selected")
 * @param string options Html
 * @return
 */
Shop.getChildCategoryOptionsByCode = function(param) {
    var result = '';
    $.ajaxSetup({'async': false});

    $.post('/categories/options-by-category-code', param, function(response) {
        result = response;
    }, 'html');

    $.ajaxSetup({'async': true});

    return result;
};



// 상품 목록에서 장바구니 담기.
Shop.addToCart = function(itemId, orderMinQuantity, nonmemberOrderType, isLogin, target, itemType) {
    var quantity = Number(orderMinQuantity) > 1 ? Number(orderMinQuantity) : 1;

    /*if(!Shop.checkedBusinessItem(itemType, businessFlag)){
        return;
    }*/

    if (isLogin == 'false' && nonmemberOrderType == '2') {
        var message = Message.get("M00601") + '\n' + Message.get("M00602");	// 회원만 구매가 가능합니다. 로그인 페이지로 이동하시겠습니까?
        if (confirm(message)) {
            location.href = Shop.url('/users/login?target=' + target);
        }
        return false;
    }
    var param = {
        'arrayRequiredItems' : itemId + '||' + quantity + '||'
    };

    $.post(Shop.url('/cart/add-item-to-cart'), param, function(response) {
        Common.responseHandler(response, function() {

            Shop.getCartInfo();

            Shop.openCartWishlistLayer('cart');

        });
    }, 'json');
};

//오늘 본 상품 조회
Shop.getTodayItems = function() {
    Common.loading.display = false;
    var $todayItems = $('.op-today-items');

    if ($todayItems.size() == 0) {
        return;
    }

    $.post('/common/today-items', {}, function(response) {
        Common.responseHandler(response, function(response) {
            var items = response.data;

            if (items != null) {

                if (items.length > 0) {

                    var html = '';

                    html += '	<div class="quick_latest"><p>오늘본상품</p></div>';
                    html += '	<div class="recency-item">'
                    html += '		<div class="item_area">';
                    html += '			<ul>';
                    for (var i = 0; i < items.length; i++) {
                        var item = items[i];
                        html += '			<li style="height:58px"><a href="' + item.link + '"><img src="' + item.itemImage + '" alt="item1" /></a></li>';
                    }
                    html += '			</ul>';
                    html += '		</div>';

                    html += '		<div class="page">';
                    html += '			<a href="#" class="prev"><img src="/content/images/btn/btn_quick_prev.gif" alt="이전상품"></a>';
                    html += '			<p><strong>1</strong>/<span>0</span></p>';
                    html += '			<a href="#" class="next"><img src="/content/images/btn/btn_quick_next.gif" alt="다음상품"></a>';
                    html += '		</div>';
                    html += '	</div>';

                    $todayItems.html(html);

                    try {
                        Shop.quickMenuPagination();
                    } catch(e) {
						let errBln = true;
                    }
                }
            }
        });
    });

};

//오늘 본 상품 조회
Shop.getMobileTodayItems = function() {
    Common.loading.display = false;
    var $todayItems = $('.op-today-items');

    if ($todayItems.size() == 0) {
        return;
    }

    $.post('/common/today-items', {}, function(response) {
        Common.responseHandler(response, function(response) {
            var items = response.data;

            if (items != null) {

                if (items.length > 0) {

                    var html = '';

                    html += '	<div class="title"><h2>오늘 본 상품</h2></div>';
                    html += '		<div class="viewed_list">';
                    html += '			<ul>';
                    for (var i = 0; i < items.length; i++) {

                        if (i > 2) {
                            break;
                        }

                        var item = items[i];
                        var itemName = item.itemName;
                        if (itemName.length > 25) {
                            itemName = itemName.substring(0, 25) + '..';
                        }

                        html += '			<li>';
                        html += '				<div class="img"><a href="' + item.link + '"><img src="' + item.imageSrc + '" alt="item1" /></a></div>'
                        html += '				<div class="txt">';
                        html += '					<a href="' + item.link + '">';
                        html += '						<p class="name">'+ itemName +'</p>';
                        html += '						<p class="price">'+ Common.numberFormat(item.exceptUserDiscountPresentPrice) +'원</p>';
                        html += '					</a>';
                        html += '				</div>';
                        html += '			</li>';
                    }
                    html += '			</ul>';
                    html += '		</div>';

                    $todayItems.html(html);
                }
            }
        });
    });

};

// 상품목록에서 관심상품 담기
Shop.addToWishList = function(itemId, orderMinQuantity,  isLogin, target, wishlistGroupId, itemType) {
    var quantity = Number(orderMinQuantity) > 1 ? Number(orderMinQuantity) : 1;

    /*if(!Shop.checkedBusinessItem(itemType, businessFlag)){
        return;
    }*/

    if (isLogin == 'false') {
        var message = "관심상품 등록은 회원만 가능합니다." + '\n' + Message.get("M00602");	// 관심상품 등록은 회원만 가능합니다. 로그인 페이지로 이동하시겠습니까?
        if (confirm(message)) {
            location.href = Shop.url('/users/login?target=' + target);
        }
        return false;
    }
    var param = {
        'arrayItemId' : itemId,
        'arrayQuantitys' : quantity,
        'arrayRequiredOptions' : '',
        'arraySelectionOptions' : '',
        'wishlistGroupId' : wishlistGroupId
    };

    $.post(Shop.url('/wishlist/add'), param, function(response) {
        Common.responseHandler(response, function() {

            Shop.getWishlistCount();

            Shop.openCartWishlistLayer('wishlist');

        });
    }, 'json');
};

Shop.checkedBusinessItem = function(itemType, businessFlag){

    if (itemType == "2" && businessFlag != "Y") {
        alert("사업자회원만 구매 가능합니다.");
        return false;
    } else {
        return true;
    }
}

//상품 목록에서 입하소식신청.
Shop.applyForArrival = function(itemId, isLogin, target) {

    // 1. 로그인 체크
    if (isLogin == 'false') {
        alert('ログインしてください。');
        location.href = Shop.url('/users/login?target=' + target);
        return;
    }

    // 2. 신청 처리
    var param = {
        'itemId': itemId,
        'itemOptionIds': ''
    };

    $.post(Shop.url('/resale/apply-for-arrival'), param, function(response) {
        Common.responseHandler(response, function() {
            var message = Message.get("M00598") + '\n' + Message.get("M00596");	// 입하 소식을 신청했습니다. 바로 확인하시겠습니까?
            if (confirm(message)) {
                location.href = Shop.url("/mypage/resale");
            }
        });
    });
};

//관심상품 수 조회
Shop.getWishlistCount = function() {
    if ($('#aside_wishlist_count').size() == 0) {
        return;
    }


    $.post('/common/wishlist', {}, function(response) {
        Common.responseHandler(response, function(response) {
            var count = response.data;

            if (count > 0) {
                $('#aside_wishlist_count').text(Common.numberFormat(count));
            }
        });
    }, 'json');
};


//장바구니 정보 조회
Shop.getCartInfo = function() {
    Common.loading.display = false;
    $.post('/common/cart', {}, function(response) {
        Common.responseHandler(response, function(response) {
            var obj = response.data;

            if ($('#quick-menu').size() > 0) {
                $('#aside_cart_quantity').text(Common.numberFormat(obj.cartQuantity));
                $('#header_cart_quantity').text(Common.numberFormat(obj.cartQuantity));
            } else {
                if (obj.cartQuantity > 0) {
                    $('#header_cart_quantity').text(Common.numberFormat(obj.cartQuantity)).show();
                    $('#footer_cart_quantity').text(Common.numberFormat(obj.cartQuantity)).show();
                }
            }
        });
    }, 'json');

};

// 상품 목록에서 상세페이지로 이동.
Shop.goItemDetails = function(itemUserCode, nonmemberOrderType, isLogin) {
    if (nonmemberOrderType == '3' && isLogin == 'false') {
        Shop.loginToItemDetailPage(itemUserCode);
    } else {
        location.href = Shop.url('/products/view/' + itemUserCode);
    }
};

Shop.closeOrderLayer = function(type) {
    $('#layer_'+type).hide();
    Common.dimmed.hide();
};

// 장바구니 / 위시리스트 확인창 열기
Shop.openCartWishlistLayer = function(type) {
    Common.dimmed.show();

    var layerCartHtml = $('#op-layer-' + type).htmlWithParent();
    $('#op-layer-' + type).remove();
    $('body').append(layerCartHtml);
    $('#op-layer-' + type).show();
};

//장바구니 / 위시리스트 확인창 닫기
Shop.closeCartWishlistLayer = function() {
    $('#op-layer-wishlist, #op-layer-cart').hide();
    Common.dimmed.hide();
    //모바일에서 위시리스트 백그라운드 닫기
    $('.layer_pop_bg').hide();
};

// [모바일] 팀, 그룹, 카테고리 페이지의 '카테고리에서 찾기'
Shop.findCategory = function() {
    // 카테고리 검색 (하위 카테고리 목록)
    var $category = $('.category_depth01 > li > a');
    if ($category.size() > 0) {
        $category.on('click', function(e) {
            var hasChildCategories = $(this).next().size() > 0 ? true : false;

            if (hasChildCategories) {
                e.preventDefault();

                var $parent = $(this).parent();
                var isActive = $parent.hasClass('on');

                $(this).closest('ul').find(' > li').removeClass('on');
                if (!isActive) {
                    $parent.addClass('on');
                }
            }

        });
    }
};



Shop.setHeightOfItemList = function(selector) {
    var $selector = $(selector);
    var h = $selector.height() - 2;
    $selector.css({'overflow': 'hidden', 'height': h + 'px'});
};

// breadcrumbs select.team
Shop.showAllCategoriesLayer = function() {

    if ($('#op-all-menus').css('display') == 'none') {
        $('#op-all-menu-button').addClass('on');
        $("#op-all-menus").addClass('show');
    } else {
        Shop.hideAllCategoriesLayer();
    }

    $("#op-all-menus").focus();

};

Shop.hideAllCategoriesLayer = function() {
    $('#op-all-menu-button').removeClass('on');
    $("#op-all-menus").removeClass('show');
    $("#op-all-menus").focus();
};

/**
 * 방문자 접속 통계를 저장.
 */
Shop.writeVisitorLog = function() {
    if ($.cookie('IS_VISITED') != "1") {
        $.post('/common/visitor-log', {}, function() {

        });
    }
};

Shop.quickMenuPagination = function() {
    // QUICK MENU
    var $quickMenu = $('#quick-menu');
    var $todayItems = $('.item_area ul');
    var todayItemCount = $todayItems.find('> li').size();
    var todayItemsIndex = 0;
    var itemHeight = 58;
    var itemsPerPage = 3;
    var totalPages = Math.ceil(todayItemCount / itemsPerPage);
    var currentPage = 1;

    $quickMenu.find('.item_area').css('height', itemHeight * (itemsPerPage < todayItemCount ? itemsPerPage : todayItemCount));

    $quickMenu.find('.page p span').text(totalPages);

    $('.page a.prev').on('click', function(e) {
        e.preventDefault();

        if (totalPages > 1) {
            if (currentPage == 1) {
                currentPage = totalPages;
            } else {
                currentPage--;
            }

            var topSize = (currentPage - 1) * itemHeight * itemsPerPage;
            $todayItems.css('margin-top', '-' + topSize + 'px');
            $quickMenu.find('.page p strong').text(currentPage);
        }



    });

    $('.page a.next').on('click', function(e) {
        e.preventDefault();

        if (totalPages > 1) {
            if (currentPage == totalPages) {
                currentPage = 1;
            } else {
                currentPage++;
            }

            var topSize = (currentPage - 1) * itemHeight * itemsPerPage;
            $todayItems.css('margin-top', '-' + topSize + 'px');
            $quickMenu.find('.page p strong').text(currentPage);
        }

    });
};

//장바구니 정보 조회
Shop.getCouponInfo = function() {
    Common.loading.display = false;
    $.post('/common/coupon', {}, function(response) {
        Common.responseHandler(response, function(response) {
            var obj = response.data;

            if ($('#aside_coupon_count').size() > 0) {
                $('#aside_coupon_count').text(Common.numberFormat(obj.userCouponCount));
            }

            if ($('#aside_shipping_coupon_count').size() > 0) {
                $('#aside_shipping_coupon_count').text(Common.numberFormat(obj.userShippingCount));
            }

        });
    }, 'json');

};



/**
 * 페이지 링크롤 POST로 전송 (일괄처리)
 *
 * 페이징 class에 .op-pagination 가 추가되어야 함.
 * submit되는 form의 selector 값을 파라미터로 설정함.
 * <form id="abd" ==> Shop.handlePagination('#abd');
 *
 * @author skc@onlinepowers.com
 * @date 2018-07-11
 */
Shop.handlePagination = function(formSelector) {
    var $pagination = $('.op-pagination');
    var $form = $(formSelector);
    if ($pagination.size() == 0 || $form.size() == 0) {
        return;
    }

    // 페이지 링크 이벤트 처리.
    $pagination.find('a').on('click', function(e) {
        var href = $(this).attr('href');

        var pattern = /page=[0-9]+/;
        var m = href.match(pattern);
        if (m != null) {
            e.preventDefault();

            var page = m[0].replace('page=', '');

            if ($form.find('input[name=page]').size() == 0) {
                $form.append('<input type="hidden" name="page" value="' + page + '" />')
            } else {
                $form.find('input[name=page]').val(page);
            }

            $form.submit();
        }
    });
};

Shop.procSessionTimeout = function() {

	if (OP_USER_TIMEOUT == '' || OP_USER_TIMEOUT == '0') {
		return;
	}

	var timeout = Number(OP_USER_TIMEOUT) * 60 * 1000;

	setTimeout(function () {

		if (Shop.isMobilePage) {
			location.href = '/op_security_logout?target=/m';
		} else {
			location.href = '/op_security_logout?target=/';
		}

	}, timeout);
}

/**
 * 사은픔 검색
 * @param targetId
 */
Shop.findGiftItem = function(targetId) {
    Common.popup("/opmanager/gift-item/find-item?processType=progress&targetId=" + targetId, 'find-gift-item', 1000, 600, 1);
};

Shop.makeGnb = function() {
    const selector = $('.gnb').find('.gnb_wrap');
    const path = location.pathname;

    if (selector.length > 0) {

        selector.hide();

        $.get('/common/gnb', {}, function(response) {
            Common.responseHandler(response, function(response) {
                const obj = response.data;
                if (selector.length > 0) {
                    let html = '';
                    let matchIndex = 0;

                    if (obj.length > 0) {

                        for (let i=0; i<obj.length; i++) {
                            html += '<a href="'+obj[i].target+'">'+obj[i].title+'</a>';
                            if (matchIndex == 0 && obj[i].target == path) {
                                matchIndex = (i+1);
                            }
                        }

                        selector.append(html);

                        selector.find('a').removeClass('on');
                        selector.find('a').eq(matchIndex).addClass('on');
                        selector.show();
                    }

                }
            });
        }, 'json');
    }

}

//엑셀다운로드 사유 - 팝업
Shop.downloadExcel = function(url, queryString, isPrivacyAccess) {
    var uri = url;
    if (queryString != "") {
        uri += "?" + queryString;
    }

	Common.popup("/opmanager/log/exceldownload-reason", "excelReason", 500, 450, 1);

    if (isPrivacyAccess) {
        //$("#privacyModal").modal({backdrop: 'static'}, 'show');
        //$("#formPrivacy").find("input[name='uri']").val(uri);
        //$("#formPrivacy").find("input[name='url']").val(url);
        $("#excelLogForm").find("input[name='uri']").val(uri);
        $("#excelLogForm").find("input[name='url']").val(url);
    } else {
        location.href = uri;
    }
}

//엑셀다운로드 사유 - 레이어팝업
Shop.downloadExcelOrder = function(url, queryString, isPrivacyAccess) {
    var uri = url;
    if (queryString != "") {
        uri += "?" + queryString;
    }

	var selectType = $("#reasonType")[0];

	if(selectType === undefined){
		var typeLength = Object.keys(reasonType).length;
		var typeText = '';

		typeText += '<select id="reasonType" title="엑셀다운로드타입" name="reasonType" class="wd-400 required" style="margin-left:15px;margin-top:20px">';
		typeText += '<option value="">-선택-</option>';
		if(typeLength > 0){
			Object.keys(reasonType).forEach(function(key) {
				typeText += '<option value="' + key + '">'+ reasonType[key] +'</option>';
			});
		}
		typeText += '</select>';
		$("#formPrivacy .modal-body").before(typeText);
	}else{
		$("#reasonType").val('');
	}

	// 체크박스 선택 후 해제했을 때, param에는 선택했던 값이 들어가서 전체 다운로드가 정상적으로 되지 않음
	// Get 방식은 전체 다운로드이기 때문에 param의 값이 비어있어야 함
	$("#formPrivacy").find("input[name='param']").val('');

    if (isPrivacyAccess) {
        $("#privacyModal").modal({backdrop: 'static'}, 'show');
        $("#formPrivacy").find("input[name='uri']").val(uri);
        $("#formPrivacy").find("input[name='url']").val(url);
    } else {
        location.href = uri;
    }
}

//엑셀다운로드 사유 - POST 방식
Shop.downloadExcelOrderPost = function(url, param, isPrivacyAccess) {
    var uri = url;

	var selectType = $("#reasonType")[0];

	if(selectType === undefined){
		var typeLength = Object.keys(reasonType).length;
		var typeText = '';

		typeText += '<select id="reasonType" title="엑셀다운로드타입" name="reasonType" class="wd-400 required" style="margin-left:15px;margin-top:20px">';
		typeText += '<option value="">-선택-</option>';
		if(typeLength > 0){
			Object.keys(reasonType).forEach(function(key) {
				typeText += '<option value="' + key + '">'+ reasonType[key] +'</option>';
			});
		}
		typeText += '</select>';
		$("#formPrivacy .modal-body").before(typeText);
	}else{
		$("#reasonType").val('');
	}

    if (isPrivacyAccess) {
        $("#privacyModal").modal({backdrop: 'static'}, 'show');
        $("#formPrivacy").find("input[name='uri']").val(uri);
        $("#formPrivacy").find("input[name='url']").val(url);
        // 체크박스에 선택된 id값(주문번호)이 POST 방식으로 전달하기 위함
        $("#formPrivacy").find("input[name='param']").val(param);
    } else {
        location.href = uri;
    }
}
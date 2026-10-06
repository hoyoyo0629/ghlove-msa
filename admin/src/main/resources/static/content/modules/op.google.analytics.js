var Ga = {}

Ga.const = {
	CHECKOUT_INIT_STEP:1,
	CHECKOUT_INPUT_USER_STEP:2,
	CHECKOUT_INPUT_PAYMENT_STEP:3,
	CHECKOUT_CONFIRM_ORDER_STEP:4,
	CHECKOUT_PURCHASE_STEP:5,
}

Ga.send = function (type, params) {

	var url = '/google-analytics/'+type;

	try {

		$.ajax({
			type:"post",
			url: url,
			data: JSON.stringify(params),
			contentType : 'application/json;charset=utf-8',
			success : function (response) {}
		});
	} catch (e) {let errBln = true;}

}
Ga.purchase = function(orderCode, orderSequence) {
	try {

		var params = Ga.initParams();
		params['orderCode'] = orderCode;
		params['orderSequence'] = orderSequence;

		Ga.send('purchase',params);
	} catch (e) {
		let errBln = true;
	}

}
Ga.addToCart = function(cartArrayRequiredItems) {

	try {
		var params = Ga.initParams();
		params['cartArrayRequiredItems'] = cartArrayRequiredItems;

		Ga.send('add-to-cart',params);

	} catch (e) {
		let errBln = true;
	}
}
Ga.removeFromCart = function(items) {

	try {
		if (typeof items == 'undefined' || items == null) {
			return false;
		}

		var params = Ga.initParams(items);

		Ga.send('remove-from-cart',params);

	} catch (e) {
		let errBln = true;
	}
}
Ga.changeFromCartQuantity = function(items, addQuantityFlag) {

	try {
		if (typeof items == 'undefined' || items == null) {
			return false;
		}

		if (typeof addQuantityFlag == 'undefined' || addQuantityFlag == null) {
			return false;
		}

		var params = Ga.initParams(items);
		params['addQuantityFlag'] = addQuantityFlag;

		Ga.send('change-from-cart-quantity',params);

	} catch (e) {
		let errBln = true;
	}
}
Ga.detail =  function(itemUserCode) {

	try {
		if (typeof itemUserCode == 'undefined' || itemUserCode == null) {
			return false;
		}

		var params = Ga.initParams();
		params['itemUserCode'] = itemUserCode;

		Ga.send('detail',params);

	} catch (e) {
		let errBln = true;
	}
}
Ga.checkout =  function(step) {

	try {

		if (step == Ga.const.CHECKOUT_INIT_STEP) {
			var params = Ga.initParams();

			Ga.send('checkout',params);

		} else {

			if (typeof gtag == 'function') {
				gtag('event', 'checkout_progress', {
					"checkout_step": step,
				});
			}

		}


	} catch (e) {
		let errBln = true;
	}
}
Ga.initParams =  function(items) {

	var href = location.href,
		array = href.split('?');

	var params = {
		cid : Ga.getCid(),
		page : array[0],
	}

	if (typeof items != 'undefined' && items != null) {
		params['products'] = items;
	}

	return params

}
Ga.getCid = function() {

	/*
	GA1.1.1019322984.1593591448
	[0] 쿠키포멧
	[1] 도메인 구성요소
	[2] 임의의 고유 ID
	[3] 타임스탬프
	*/

	try {
		var _ga = Ga.getCookie('_ga');

		if (typeof _ga != 'undefined' && _ga != '') {
			var array = _ga.split('.'),
				length = 4;

			if (typeof array != 'undefined' && array != null && array.length == length) {
				return array[2]+'.'+array[3];
			}
		}

	} catch (e) {		let errBln = true;
	}

	return '';
}
Ga.select =  function(items) {

	try {

		if (typeof gtag == 'function' && typeof items != 'undefined' && items != null) {
			gtag("event",  "select_content", {
				"content_type": "product", "items" : items
			});
		}
	} catch (e) {		let errBln = true;
	}

}
Ga.impression = function(items) {

	try {

		if (typeof gtag == 'function' && typeof items != 'undefined' && items != null) {
			gtag("event",  "view_item_list", {"items" : items});
		}
	} catch (e) {		let errBln = true;
	}
}
Ga.getImpressionsItem = function(id, name, listName, brand, category, listPosition, price) {

	var item = null;

	if (isValid(id) ||isValid(name) ) {

		item = {};

		if (isValid(id)) {
			item['id'] = id;
		}

		if (isValid(name)) {
			item['name'] = name;
		}

		if (isValid(listName)) {
			item['list_name'] = listName;
		}

		if (isValid(brand)) {
			item['brand'] = brand;
		}

		if (isValid(category)) {
			item['category'] = category;
		}

		if (listPosition == 0 || listPosition > 0) {
			item['list_position'] = listPosition;
		}

		price = price+'';

		if (isValid(price)) {
			item['price'] = price;
		}
	}

	return item;

	function isValid(value) {
		return typeof value != 'undefined' && value != null && value != ''
	}
}
Ga.frontImpression = function(items, listName, category) {

	try {
		var gaItems = [];
		if (typeof items != 'undefined' && items != null) {
			var index = 0;
			for (var i=0; i < items.length; i++) {
				var item = items[i],
					gaItem = Ga.getImpressionsItem(
						item.itemUserCode,
						item.itemName,
						listName,
						item.brand,
						category,
						index,
						item.presentPrice
					);

				if (gaItem != null) {
					gaItems.push(gaItem);
					index++;
				}
			}

			Ga.impression(gaItems);
		}
	} catch (e) {		let errBln = true;
	}
}
Ga.frontSelect = function(item, listName, category) {
	try {

		var gaItem = Ga.getImpressionsItem(
			item.itemUserCode,
			item.itemName,
			listName,
			item.brand,
			category,
			-1,
			item.presentPrice
		);

		Ga.select([gaItem]);
	} catch (e) {		let errBln = true;
	}
}

Ga.frontImpressionBySelector = function ($selector, listName, category) {

	try {
		var items = [];
		if ($selector.size() > 0) {
			var $datas = $selector.find('.item_data');

			if ($datas.size() > 0) {

				$datas.each(function(i) {
					var $data = $datas.eq(i);
					items.push(Ga.getSelectorItem($data));
				});

			}
		}

		if (items.length > 0) {
			Ga.frontImpression(items, listName, category);
		}
	} catch (e) {
		console.error(e);
	}
}

Ga.frontSelectBySelector = function ($selector, listName, category) {
	try {
		if ($selector.size() > 0) {
			var item = Ga.getSelectorItem($selector);
			Ga.frontSelect(item, listName, category);
		}
	} catch (e) {
		console.error(e);
	}
}

Ga.getSelectorItem = function ($selector) {
	var itemUserCode = '',
		itemName = '',
		brand = '',
		presentPrice = 0;

	try {
		itemUserCode = $selector.data('item-user-code');
		itemName = $selector.data('item-name');
		brand = $selector.data('brand');
		presentPrice = Number($selector.data('present-price'));
	} catch (e) {let errBln = true;}

	return {
		itemUserCode : itemUserCode,
		itemName : itemName,
		brand : brand,
		presentPrice : presentPrice,
	}
}

Ga.getCookie = function(name) {
	name = name + "=";
	var decodedCookie = decodeURIComponent(document.cookie);
	var ca = decodedCookie.split(';');
	for(var i = 0; i <ca.length; i++) {
		var c = ca[i];
		while (c.charAt(0) == ' ') {
			c = c.substring(1);
		}
		if (c.indexOf(name) == 0) {
			return c.substring(name.length, c.length);
		}
	}
	return "";
}


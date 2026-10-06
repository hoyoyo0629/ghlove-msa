
var Social = {
	facebook : function(title, currentPageLink, refCode, refId) {
		this.share("facebook", title, currentPageLink);
		this.shareCount(refCode, refId, "1");
	},
	twitter : function(title, currentPageLink, refCode, refId) {
		this.share("twitter", title, currentPageLink);
		this.shareCount(refCode, refId, "2");
	},
	kakao : function(id, title, desc, image, eventUrl){
		Kakao.init(id);

		Kakao.Link.sendDefault({
			objectType: 'feed',
			content: {
				title: title,
				description: desc,
				imageUrl: image,
				link: {
					webUrl: eventUrl
				},
			},
			success: function(response) {
				let errBln = true;
			},
			fail: function(error) {
				let errBln = true;
			}
		});
	},

	share : function(target, title, currentPageLink) {

		currentPageLink = currentPageLink.replace('http://withkb.kbstar.com:443', 'https://withkb.kbstar.com');
		currentPageLink = currentPageLink.replace('http://withkb.kbstar.com:30443', 'https://withkb.kbstar.com');
		currentPageLink = currentPageLink.replace('http://withkb.kbstar.com:20443', 'https://withkb.kbstar.com');

		var snsShareUrl;
		if (target == 'twitter') {
			//var ranNum = Math.floor(Math.random()*10);
            const array = new Uint32Array(1);
            var ranNum = window.crypto.getRandomValues(array)[0];
			snsShareUrl = 'http://twitter.com/share?url='+encodeURIComponent(currentPageLink)+'&text='+encodeURIComponent(title)+"&nocache="+ranNum;
		} else if (target == 'facebook') {
			snsShareUrl ="http://www.facebook.com/share.php?u="+encodeURIComponent(currentPageLink);
		} else if (target == 'kakaotalk') {
			snsShareUrl ="https://accounts.kakao.com/login?continue=https://sharer.kakao.com/talk/friends/picker/shortlink/";
		}

		var win = window.open(snsShareUrl ,'sharer', 'toolbar=0, status=0, width=626, height=436');
		if(win){
			win.focus();
		}
	},

	shareCount : function(refCode,refId,snsType){
		if(refCode == undefined || refId == undefined) {
			return false;
		}

		var params = {
				'refCode' : refCode,
				'refId'   : refId,
				'snsType' : snsType
		};

		$.post(url('/sns/share'),params,function(){});
	},

	//상품상세 공유하기 url복사
	urlClip : function clip(){
		var url = '';
		var textarea = document.createElement("textarea");
		document.body.appendChild(textarea);
		url = window.document.location.href;
		textarea.value = url;
		textarea.select();
		document.execCommand("copy");
		document.body.removeChild(textarea);
		alert("URL이 복사되었습니다.")
	}

};

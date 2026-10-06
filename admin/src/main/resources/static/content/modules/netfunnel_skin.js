if (typeof NetFunnel == "object") {
	
	NetFunnel.SkinUtil.add('skin2', {
		prepareCallback: function () {
			var progress_print = document.getElementById("Progress_Print");
			progress_print.innerHTML = "0 % (0/0) - 0 sec";
		},
		updateCallback: function (percent, nwait, totwait, timeleft) {
			var progress_print = document.getElementById("Progress_Print");
			var prog = totwait - nwait;
			progress_print.innerHTML = percent + " % (" + prog + "/" + totwait + ") - " + timeleft + " sec";
		},
		htmlStr: `
		<div id="NetFunnel_Skin_Top">
			<h2 class="logo"><img src="/static/images/cli-logo.png" alt="logo" /></h2>
		<div class="NetFunnel_Skin_Body">
			<div class="info">
				서비스 이용고객이 많아<br>
				<strong>접속 대기중</strong>
				입니다.
				<p>
					대기 순서에 따라 
					<strong class="pointRed">자동 접속</strong>됩니다.
				</p>
			</div>
			<p>
			예상 대기 시간 : 
			<strong id="NetFunnel_Loading_Popup_TimeLeft" class="%H시간 %M분 %02S초^ ^false^font-size:15px !important;color:#525252;"></strong>
			&nbsp;/&nbsp;현재 대기순번 
			<strong id="NetFunnel_Loading_Popup_Count" class="pointRed"></strong>번째
			</p>
			<div class="warn">
			- 새로고침, 뒤로가기 또는 재접속하시면 대기시간이 더 길어집니다.
			</div>
			<div id="Progress_Print" style="font-size:17px;color:gray">
			</div> 
			<div id="NetFunnel_Loading_Popup_Progressbar"> </div>
		
			<button id="NetFunnel_Countdown_Stop">중지</button>
		</div>
	</div>`
	}, 'normal');
}
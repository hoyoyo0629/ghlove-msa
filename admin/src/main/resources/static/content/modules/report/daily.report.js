class CommonReport {
  attachZero = (date) => (date < 10 ? "0" + date : date);

  today = new Date();

  numberFormatToString = (type, value) => {
    let result = "0";
    switch (type) {
      case "round":
        result =
          parseInt(value) < 1000000
            ? Common.numberFormat(value)
            : Math.round(parseInt(value) / 1000000)
                .toLocaleString()
                .toString() + "백만";
        break;
      case "basic":
        result = Common.numberFormat(value);
        break;
    }

    return result;
  };

  nvl = (value) => {
    if (value === null || value === undefined) return 0;
  };
}

// 일일보고용
class Daily extends CommonReport {
  constructor(element, searchDate) {
    super();
    element.addEventListener("click", this, false);
    this.searchDate = searchDate;
    this.endDate = endDate;
    $('body').append('<div id="loading-dimmed" style="display: none"></div>');
    $('body').append('<div id="loading" style="display: none"></div>');
    new Spinner(Common.loading.options).spin(document.getElementById('loading'));
  }

  handleEvent(event) {
    switch (event.type) {
      case "click":
        this.searchDate = document.querySelector("#report-searchdate").value;

        if (!this.searchDate) {
	      alert("날짜를 입력하십시오");
	      return false;
	    }

        alert("조회 시간이 많이 소요 됩니다. 데이터 조회를 완료 할때 까지 기다립시오.");
        this.getDailyReport().then((html) => {
          document.querySelector("#daily-report-area").value = html;
          loding('hide');
	   	});
        break;
    }
  }

  getDailyReport = async () => {
    loding('show');

	let response = await fetch("/opmanager/shop-statistics/dashboard/day/report/daily/" + this.searchDate, {
      method: "GET",
    });

    return await this.reportParsing(this.searchDate, await response.json());

  };

  reportParsing = async (searchDate, data) => {
    const searchDateFmt = searchDate.substring(2, 4) + "." + parseInt(searchDate.substring(4, 6)) + "." + parseInt(searchDate.substring(6, 8));

    const htmlDonationString = `
[ 고향사랑 기부 현황 ] ${searchDateFmt} 기준

📦 답례품 등록: ${this.numberFormatToString("basic", data.item.itemRegCount)}개
👥 회원가입: ${this.numberFormatToString("basic", data.user.userCount)}명

📈 기부건수: ${this.numberFormatToString("basic", data.donation.totalCount)}건
  · 시스템 일반기부: ${this.numberFormatToString("basic", data.donation.onlineCount)}건
  · 시스템 지정기부: ${this.numberFormatToString("basic", data.donation.onlineDsgnDntnBizCount)}건

  · 오프라인 일반기부: ${this.numberFormatToString("basic", data.donation.offlineCount)}건
  · 오프라인 지정기부: ${this.numberFormatToString("basic", data.donation.offlineDsgnDntnBizCount)}건

💰 기부금: ${this.numberFormatToString("round", data.donation.totalCntrAmt)}원
  · 시스템 일반기부금: ${this.numberFormatToString("round", data.donation.onlineCntrAmt)}원
  · 시스템 지정기부금: ${this.numberFormatToString("round", data.donation.onlineDsgnDntnBizAmt)}원

  · 오프라인 일반기부금: ${this.numberFormatToString("round", data.donation.offlineCntrAmt)}원
  · 오프라인 지정기부금: ${this.numberFormatToString("round", data.donation.offlineDsgnDntnBizAmt)}원

🎁 답례품 신청: ${this.numberFormatToString("basic", data.item.orderCount)}건
💸 답례품 신청 금액: ${this.numberFormatToString("round", data.item.usePointAmt)}원

🔼 기부 증감액: ${this.numberFormatToString("round", data.donation.increasedCount)}건 ${this.numberFormatToString(
      "round",
      data.donation.increasedAmt
    )}원
`;

	let htmlPlatformlinkCurrentString = `
고향사랑e음 일일 기부 현황 ( 민간 제외 )
일일 기부건수: ${this.numberFormatToString("basic", data.donation.increasedTotalCountNotLinkInstt)}건 / 일일 기부금액: ${this.numberFormatToString("round", data.donation.increasedTotalCntrAmtNotLinkInstt)}원

민간플랫폼 일별 기부 현황`;
	data.donation.linkInsttCurrent.forEach((element) => {
	  htmlPlatformlinkCurrentString += "\n" + element.linkInsttNm +  "     총 기부건수: "+ this.numberFormatToString("basic", element.linkInsttCnt) + "건 / 총 기부금액 " + this.numberFormatToString("basic", element.linkInsttAmt) + "원";
	});
	htmlPlatformlinkCurrentString += "\n\n";

	let htmlPlatformlinkCumulativeString = `
고향사랑e음 누적 기부 현황 ( 민간 제외 )
총 기부건수: ${this.numberFormatToString("basic", data.donation.totalCountNotLinkInstt)}건 / 총 기부금액: ${this.numberFormatToString("round", data.donation.totalCntrAmtNotLinkInstt)}원

민간플랫폼 누적 기부 현황`;
	data.donation.linkInsttCumulative.forEach((element) => {
	  htmlPlatformlinkCumulativeString += "\n" + element.linkInsttNm +  "     총 기부건수: "+ this.numberFormatToString("basic", element.linkInsttCnt) + "건 / 총 기부금액 " + this.numberFormatToString("basic", element.linkInsttAmt) + "원";
	});
	htmlPlatformlinkCumulativeString += "\n\n";

    const htmlCustomerCenterString = `
고향사랑 콜 현황
☎ 일일 총 콜수: ${this.numberFormatToString(
      "basic",
      parseInt(data.customerCenter?.callKookmin || 0) +
        parseInt(data.customerCenter?.callLov || 0) +
        parseInt(data.customerCenter?.callGiver || 0) +
        parseInt(data.customerCenter?.callNhbank || 0) +
        parseInt(data.customerCenter?.callPlatform || 0)
    )}건
· 대국민 콜수: ${this.numberFormatToString("basic", data.customerCenter?.callKookmin || 0)}건
· 지자체 콜수: ${this.numberFormatToString("basic", data.customerCenter?.callLov || 0)}건
· 답례품 제공자 콜수: ${this.numberFormatToString("basic", data.customerCenter?.callGiver || 0)}건
· 농협 관리자 콜수: ${this.numberFormatToString("basic", data.customerCenter?.callNhbank || 0)}건
· 민간 개방 콜수: ${this.numberFormatToString("basic", data.customerCenter?.callPlatform || 0)}건
`;

    return htmlDonationString + htmlPlatformlinkCurrentString + htmlPlatformlinkCumulativeString + htmlCustomerCenterString;

  };
}
// 주간보고용
class Week extends CommonReport {
  constructor(element, startDate, endDate) {
    super();
    element.addEventListener("click", this, false);
    this.startDate = startDate;
    this.endDate = endDate;
    $('body').append('<div id="loading-dimmed" style="display: none"></div>');
    $('body').append('<div id="loading" style="display: none"></div>');
    new Spinner(Common.loading.options).spin(document.getElementById('loading'));
  }

  formatYYYYMMDD = (date) => {
	return (
		date.getFullYear() + String(date.getMonth() + 1).padStart(2, '0') + String(date.getDate().padStart(2, '0'))
	);
  };
  handleEvent(event) {
    switch (event.type) {
      case "click":
        this.endDate = document.querySelector("#report-searchdate").value;
        const end = new Date(
		Number(this.endDate.slice(0,4)),
		Number(this.endDate.slice(4,6)) -1,
		Number(this.endDate.slice(6,8)));
        this.startDate = (end.getFullYear() + String(end.getMonth()+ 1).padStart(2, '0') + String(end.getDate()).padStart(2, '0') - 6).toString();

        if (!this.endDate) {
          alert("날짜를 입력하십시오");
          return false;
	      }

        alert("조회 시간이 많이 소요 됩니다. 데이터 조회를 완료 할때 까지 기다립시오.");
        this.getWeekReport().then((html) => {
          document.querySelector("#daily-report-area").value = html;
          loding('hide');
   	  	});
        break;
    }
  }

  getWeekReport = async () => {
    loding('show');
      let res = await fetch("/opmanager/shop-statistics/dashboard/day/report/daily/" + this.startDate +'/' +this.endDate, {
        method: "GET",
      });
      return await this.reportParsingWeek(this.endDate, await res.json());
  };

  reportParsingWeek = async (endDate, data) => {
  	const endDateFmt = endDate.substring(2, 4) + "." + parseInt(endDate.substring(4, 6)) + "." + parseInt(endDate.substring(6, 8));

	let aPer = 0;
	let bPer = 0;
	const tCount = Number(data.donation.totalCount);
	const aCount = Number(data.donation.totalCountNotLinkInstt);
	if(tCount === 0) {
		aPer = 0;
		bPer = 0;
	} else {
		// 제로 디바이드 방지
		let bCount = tCount - aCount;
		aPer = (aCount / tCount) * 100;
		bPer = (bCount / tCount) * 100;
	}

	let AResult = Math.round(aPer * 10) / 10;
	let BResult = Math.round(bPer * 10) / 10;


const htmlDonationString = `
[ 고향사랑 기부 현황 ] (${endDateFmt} 기준, ※ 대외 주의)

💰 총 기부금(백만원) : ${this.numberFormatToString("round", data.donation.totalCntrAmt)}원 : 일반 ${this.numberFormatToString("round", data.donation.onlineCntrAmt + data.donation.offlineCntrAmt)}원 / 지정 ${this.numberFormatToString("round", data.donation.onlineDsgnDntnBizAmt + data.donation.offlineDsgnDntnBizAmt)}원
   · e음(+오프라인) : ${this.numberFormatToString("round", data.donation.totalCntrAmtNotLinkInstt)}원(${this.numberFormatToString("basic", AResult)}%) / 민간(${data.donation.linkInsttCumulative.length}개) : ${this.numberFormatToString("round", data.donation.totalCntrAmt - data.donation.totalCntrAmtNotLinkInstt)}원(${this.numberFormatToString("basic", BResult)}%)
   · 기부 증가액(1주) : ${this.numberFormatToString("round", data.donation.increasedCount)}건 / ${this.numberFormatToString(
          "round",
          data.donation.increasedAmt
	 )}원

📈 총 기부건수(건) ${this.numberFormatToString("basic", data.donation.totalCount)} : 일반 ${this.numberFormatToString("basic", data.donation.onlineCount + data.donation.offlineCount)} / 지정 : ${this.numberFormatToString("basic", data.donation.onlineDsgnDntnBizCount + data.donation.offlineDsgnDntnBizCount)}

📦 답례품 : ${this.numberFormatToString("basic", data.item.itemRegCount)}개 / 판매(민간제외) : ${this.numberFormatToString("round", data.item.usePointAmt)} 포인트

👥 e음 회원수 : ${this.numberFormatToString("basic", data.user.userCount)}명(${this.numberFormatToString("basic", data.user.increaseUserCount)}명 증가)
`;

    // TBD: API 데이터로 변경예정
    const banks = ['KB국민은행','신한은행','IBK기업은행','하나은행','NH농협은행'];

    const result = data.donation.linkInsttCumulative.reduce((acc, item) => {

	  const isBank = banks.some(t => item.linkInsttNm.includes(t));

    if(isBank) {
      acc.bank.push(item);
    } else {
      acc.company.push(item);
    }

    return acc;
    },{bank: [], company: []}
  );

  let htmlPlatformlinkCumulativeString = `
♥️ 민간플랫폼 누적 기부 현황`;
    htmlPlatformlinkCumulativeString += "\n" + "<기업>";

    result.company.forEach((el) => {
      htmlPlatformlinkCumulativeString += "\n" + "	· " + el.linkInsttNm + "		금액 : " + this.numberFormatToString("basic", el.linkInsttAmt) + "원  /  건수 : " +  this.numberFormatToString("basic", el.linkInsttCnt) + "건"
    });

    htmlPlatformlinkCumulativeString += "\n" + "<은행>";

    result.bank.forEach((el) => {
      htmlPlatformlinkCumulativeString += "\n" + "	· " + el.linkInsttNm + "		금액 : " + this.numberFormatToString("basic", el.linkInsttAmt) + "원  /  건수 : " +  this.numberFormatToString("basic", el.linkInsttCnt) + "건"
    });

    htmlPlatformlinkCumulativeString += "\n\n";

    return htmlDonationString + htmlPlatformlinkCumulativeString

  };
}


const writeClipboardText = async () => {
  try {
    await navigator.clipboard.writeText(document.querySelector("#daily-report-area").value).then(() => alert("복사 되었습니다."));
  } catch (err) {
    console.log(err);
    alert("복사 실패. 다시 조회 하십시오");
  }
};

document.addEventListener("DOMContentLoaded", () => {
  new Daily(document.querySelector("#daily-report"));
  new Week(document.querySelector("#week-report"));
  const el = document.querySelector("#daily-report-copy");
  el.addEventListener(
    "click",
    (e) => {
      writeClipboardText();
    },
    false
  );
});


const loding = (method) => {
	if(method === 'show') {
		$('#loading-dimmed').show();
		$('#loading').show();
	} else {
		$('#loading-dimmed').hide();
		$('#loading').hide();
	}
}
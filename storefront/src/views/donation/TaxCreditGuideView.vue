<script setup>
import { useRouter } from 'vue-router'

// AS-IS donation/guide3.html(및 Thymeleaf 버전 honor.html) 재현 - 순수 정적 안내 페이지라
// 로그인이 필요 없다. 개인화된 세액공제 예상액 계산은 /mypage/tax-credit-estimate(로그인 필요)
// 로 분리되어 있다.
const router = useRouter()
</script>

<template>
  <nav class="breadcrumb-wrap" aria-label="현재 경로" id="breadcrumb">
    <ol class="breadcrumb inner">
      <li class="home"><router-link to="/" class="txt">HOME</router-link></li>
      <li><span class="txt">안내사항</span></li>
      <li><span class="txt">연말정산 세액공제 안내</span></li>
    </ol>
  </nav>

  <div id="contents" class="contents-page">
    <div class="give">
      <section class="give-info inner">
        <h2 class="give-info__txt">연말정산 세액공제 안내</h2>

        <div class="give-info__intro ty2">
          <div class="give-info__intro-text">
            <ul class="give-info__lists">
              <li class="give-info__list">고향사랑 기부금은<br class="give-mob" /> 연말정산시 세액공제 받을 수 있습니다.</li>
              <li class="give-info__list">
                또한 세액공제는 산출세액 기준에 따라 진행되므로<br class="give-mob" />
                공제받을 세액이 없을 경우 기부를 하셔도<br class="give-mob" />
                공제액이 없을 수 있으므로 유의 부탁드립니다.
              </li>
              <li class="give-info__list">
                고향사랑 기부영수증은<br class="give-mob" /> 국세청 홈택스에서 발급됩니다.<br class="give-mob" /> (기부 후 약 1~2개월)
              </li>
            </ul>
          </div>
          <div class="give-info__intro-image" aria-hidden="true"></div>
        </div>

        <div class="give-dl-wrap">
          <dl class="give-dl">
            <dt class="give-dl__dt"><span class="give-dl__num">1</span><strong class="give-dl__title">기부</strong></dt>
            <dd class="give-dl__dd">자치단체에 기부하기, 특정사업에 기부하기 모두 세액공제 혜택을 동일하게 제공합니다.</dd>
          </dl>

          <dl class="give-dl">
            <dt class="give-dl__dt"><span class="give-dl__num">2</span><strong class="give-dl__title">기부정보 전달, 세액공제 처리</strong></dt>
            <dd class="give-dl__dd">
              기부 정보는 국세청(홈택스)에 신고되어 세액공제가 처리됩니다.<br />
              <strong class="give-dl__title">※ 연말정산 신고 항목(특별재난지역여부, 기부금액 10만원 초과 등)을 정확히 확인하여 신고하시기 바랍니다.</strong>
            </dd>
          </dl>

          <dl class="give-dl">
            <dt class="give-dl__dt"><span class="give-dl__num">3</span><strong class="give-dl__title">연말정산 시 세액공제 확인</strong></dt>
            <dd class="give-dl__dd">국세청 연말정산 간소화 서비스를 통해 세액공제 처리내역 확인이 가능합니다.</dd>
            <dd class="give-dl__dd">
              국세청 연말정산 간소화 서비스에서는 국세(소득세) 공제 금액만 표출됩니다.<br />
              ※ 예: 10만원 기부 시 소득세 90,909원,지방소득세(소득세의 10%) 9,090원 공제
            </dd>
          </dl>
        </div>

        <table class="give-table give-pc">
          <caption>연말정산 세액공제 안내의 기부금액 (연간 누적), 세액공제율, 혜택 예시로 구성된 표</caption>
          <thead>
            <tr>
              <th colspan="2">기부금액 (연간 누적)</th>
              <th>세액공제율</th>
              <th>혜택 예시</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td colspan="2"><strong>10만원 이하</strong></td>
              <td><strong>100%</strong></td>
              <td>
                <p><strong>10만원 기부 시, 13만원 상당 혜택</strong></p>
                <p><strong>10</strong>만원 전액 세액공제 + <strong>3</strong>만 포인트</p>
              </td>
            </tr>
            <tr>
              <td colspan="2">
                <p><strong>10만원 초과 20만원 이하</strong></p>
                <p class="give-table__sub">(특별재난지역 포함)</p>
              </td>
              <td><strong>44%</strong></td>
              <td>
                <p><strong>20만원 기부 시, 20만 4천원 상당 혜택</strong></p>
                <p><strong>14.4</strong>만원(10+4.4) 세액공제 + <strong>6</strong>만 포인트</p>
              </td>
            </tr>
            <tr>
              <td rowspan="2">
                <p><strong>20만원 초과분</strong></p>
                <p class="give-table__sub">(2,000만원 이하)</p>
              </td>
              <td>
                <p><strong>특별재난지역</strong></p>
                <p class="give-table__sub">(선포일로부터 3개월 이내)</p>
              </td>
              <td><strong>33%</strong></td>
              <td>
                <p><strong>100만원 기부 시,<br />70만 8천원 상당 혜택</strong></p>
                <p><strong>40.8</strong>만원(10+4.4+26.4) 세액공제 + <strong>30</strong>만 포인트</p>
              </td>
            </tr>
            <tr>
              <td><strong>특별재난지역 외</strong></td>
              <td><strong>16.5%</strong></td>
              <td>
                <p><strong>100만원 기부 시,<br />57만 6천원 상당 혜택</strong></p>
                <p><strong>27.6</strong>만원(10+4.4+13.2) 세액공제 + <strong>30</strong>만 포인트</p>
              </td>
            </tr>
          </tbody>
        </table>

        <table class="give-table give-mob">
          <caption>연말정산 세액공제 안내의 기부금액 (연간 누적), 세액공제율, 혜택 예시로 구성된 표</caption>
          <thead>
            <tr>
              <th>기부금액<br />(연간 누적)</th>
              <th>세액공제율</th>
              <th>혜택 예시</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><strong>10만원 이하</strong></td>
              <td><strong>100%</strong></td>
              <td>
                <p><strong>10만원 기부 시,<br />13만원 상당 혜택</strong></p>
                <p><strong>10</strong>만원 전액 세액공제<br />+ <strong>3</strong>만 포인트</p>
              </td>
            </tr>
            <tr>
              <td><p><strong>10만원 초과<br />20만원 이하</strong></p></td>
              <td><strong>44%</strong><p class="give-table__sub">(특별재난지역 포함)</p></td>
              <td>
                <p><strong>20만원 기부 시,<br />20만 4천원 상당 혜택</strong></p>
                <p><strong>14.4</strong>만원(10+4.4)<br />세액공제 + <strong>6</strong>만 포인트</p>
              </td>
            </tr>
            <tr>
              <td rowspan="2"><p><strong>20만원 초과<br />2,000만원 이하</strong></p></td>
              <td>
                <p><strong>특별재난지역</strong> 기부</p>
                <p><strong>33%</strong></p>
                <p>(선포일로부터<br /> 3개월 이내)</p>
              </td>
              <td>
                <p><strong>100만원 기부 시,<br />70만 8천원 상당 혜택</strong></p>
                <p><strong>40.8</strong>만원(10+4.4+26.4)<br /> 세액공제 + <strong>30</strong>만 포인트</p>
              </td>
            </tr>
            <tr>
              <td>
                <p><strong>특별재난지역 외</strong> 기부</p>
                <p><strong>16.5%</strong></p>
              </td>
              <td>
                <p><strong>100만원 기부 시,<br />57만 6천원 상당 혜택</strong></p>
                <p><strong>27.6</strong>만원(10+4.4+13.2)<br /> 세액공제 + <strong>30</strong>만 포인트</p>
              </td>
            </tr>
          </tbody>
        </table>
        <br />
        <strong class="give-dl__title">※ 연말정산 신고 항목(특별재난지역여부, 기부금액 10만원 초과 등)을 정확히 확인하여 신고하시기 바랍니다.</strong>

        <div class="btn-box many mb_b60" style="margin-top: 32px">
          <button type="button" class="blueBtn u-confirm" @click="router.push('/mypage/tax-credit-estimate')">내 세액공제 예상액 확인하기</button>
        </div>
      </section>
    </div>
  </div>
</template>

<style>
@import '/css/donation_guge.css';
@import '/css/research-box.css';
</style>

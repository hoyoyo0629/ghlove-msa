<script setup>
// AS-IS donation/guide1.html("고향사랑기부제안내") 재현. 아이콘/일러스트가 전부
// donation_guge.css의 background-image로 그려지는 순수 정적 페이지라 이미지 자산이 필요 없다.
</script>

<template>
  <nav class="breadcrumb-wrap" aria-label="현재 경로" id="breadcrumb">
    <ol class="breadcrumb inner">
      <li class="home"><router-link to="/" class="txt">HOME</router-link></li>
      <li><span class="txt">안내사항</span></li>
      <li><span class="txt">고향사랑기부제안내</span></li>
    </ol>
  </nav>

  <div id="contents" class="contents-page">
    <div class="give">
      <section class="give-info inner">
        <h2 class="give-info__title">고향사랑기부제안내</h2>
        <p class="give-info__txt">
          <span class="give-info__txt-item">고향</span>
          과 함께
          <span class="give-info__txt-ico"></span>
          <span class="give-info__txt-item">사랑</span>
          을 같이
          <span class="give-info__txt-ico"></span>
          <span class="give-info__txt-item">기부</span>
          로 나눔
        </p>

        <div class="give-info__intro">
          <div class="give-info__intro-text">
            <h3 class="give-info__intro-title">고향사랑기부제란?</h3>
            <p class="give-info__intro-description">개인이 고향(지자체)에 기부하고 지자체는 이를 모아서 주민 복리에 사용하는 제도</p>
            <ul class="give-info__lists">
              <li class="give-info__list">지방재정을 확충하고, 지역특산품 등 답례품으로 지역경제 활성화</li>
              <li class="give-info__list">기부자는 고향을 돕는 자부심과 세액공제, 답례품 혜택을 동시에!</li>
            </ul>
          </div>
          <div class="give-info__intro-image" aria-hidden="true"></div>
        </div>

        <div class="give-info__grid">
          <article class="give-card is-org">
            <div class="give-card__title-area">
              <h3 class="give-card__title">기부자 및 기부처</h3>
              <div class="give-card__icon is-blue" aria-hidden="true"></div>
            </div>
            <div class="give-card__content">
              <ul class="give-card__list">
                <li>
                  <strong>기부자</strong>
                  <p>개인(법인 불가능)</p>
                </li>
                <li>
                  <strong>기부처</strong>
                  <p>거주지를 제외한 모든 지방자치단체</p>
                  <p class="give-card__note">
                    ※ 거주지는 주민등록을 기준으로 하며,<br />
                    거주하는 광역 및 기초 자치단체에는 기부 불가
                  </p>
                </li>
              </ul>
            </div>
          </article>

          <article class="give-card is-amount">
            <div class="give-card__title-area">
              <h3 class="give-card__title">기부금액</h3>
              <div class="give-card__icon is-yellow" aria-hidden="true"></div>
            </div>
            <div class="give-card__content">
              <ul class="give-card__list">
                <li>
                  <strong>기부액 한도</strong>
                  <p>개인당 연간 2,000만원</p>
                </li>
              </ul>
            </div>
          </article>

          <article class="give-card is-benefit">
            <div class="give-card__title-area">
              <h3 class="give-card__title">주요혜택</h3>
              <div class="give-card__icon is-pink" aria-hidden="true"></div>
            </div>
            <div class="give-card__content">
              <ul class="give-card__list">
                <li>
                  <strong>1. 기부금에 대해 세액공제 혜택 제공</strong>
                  <p class="give-card__dot">10만원까지 100% 공제</p>
                  <p class="give-card__dot">10만원 초과 20만원 이하는 44% 공제</p>
                  <p class="give-card__note">※ (예) 20만원 기부 시 14.4만원(10만원 + 10만원의 44%) 공제</p>
                  <p class="give-card__note">※ '26년 기부금부터 공제율 상향 적용(신설)</p>
                  <p class="give-card__dot">20만원 초과분(2,000만원 이하)은 16.5% 공제</p>
                  <p class="give-card__note">
                    ※ (예) 100만원 기부 시 27.6만원 공제<br />
                    27.6만원 = 10만원 + 10만원의 44% + 80만원의 16.5%
                  </p>
                  <p class="give-card__dot">
                    단, 특별재난지역 기부시(선포일로부터 3개월 이내)<br />
                    20만원 초과분(2,000만원 이하) 33% 공제
                  </p>
                  <p class="give-card__note">
                    ※ (예) 100만원 기부 시 40.8만원 공제<br />
                    40.8만원 = 10만원 + 10만원의 44% + 80만원의 33%
                  </p>
                  <p class="give-card__dot">
                    세액공제는 기부자 본인에 한하며 이월공제 미적용.<br />
                    개인의 결정세액에 따라 공제금액은 달라질 수 있음
                  </p>
                </li>
                <li>
                  <strong>2. 기부한 지역의 답례품 제공</strong>
                  <p class="give-card__dot">기부금액의 30% 한도로 기부 포인트 제공</p>
                  <p class="give-card__note">※ 기부 포인트의 유효기간은 5년이며, 누적하여 사용 가능</p>
                  <p class="give-card__dot">기부한 지역의 답례품만 구매 가능</p>
                </li>
              </ul>
            </div>
          </article>
        </div>

        <div class="give-info__type">
          <h3 class="give-info__type-title">지정기부란?</h3>
          <p class="give-info__type-txt1">콕콕!<br class="give-mob" />집어서 기부하면<br />고향이 쑥쑥!</p>
          <p class="give-info__type-txt2">
            기부자가 미리 정해진 자치단체의 사업 중에서 본인의<br class="give-mob" /> 기부금이<br class="give-pc" />
            사용되기를 원하는 사업을 지정하여 하는 기부<br class="give-mob" /> (「고향사랑기부금법」 제8조의2)
          </p>
          <div class="give-info__type-img" aria-hidden="true"></div>
        </div>
      </section>

      <section class="give-flow">
        <div class="inner">
          <h3 class="give-flow__title">온 · 오프라인에서<br />자유롭게 고향사랑기부</h3>
          <div class="give-flow__grid">
            <div class="give-flow__row">
              <div class="give-flow__head">
                <div class="give-flow__tit">온라인 기부</div>
                <p class="give-flow__lead">고향사랑e음(http://ilovegohyang.go.kr)</p>
              </div>
              <div class="give-steps">
                <div class="give-step">
                  <span class="give-step__num">STEP 01</span>
                  <strong>회원가입</strong>
                  <span class="give-step__ico ico-step01" aria-hidden="true"></span>
                </div>
                <div class="give-step">
                  <span class="give-step__num">STEP 02</span>
                  <strong>기부지자체 /<br />사업선택</strong>
                  <span class="give-step__ico ico-step02" aria-hidden="true"></span>
                </div>
                <div class="give-step">
                  <span class="give-step__num">STEP 03</span>
                  <strong>기부하기</strong>
                  <span class="give-step__ico ico-step03" aria-hidden="true"></span>
                </div>
                <div class="give-step">
                  <span class="give-step__num">STEP 04</span>
                  <strong>답례품 선택 및<br />배송조회</strong>
                  <span class="give-step__ico ico-step04" aria-hidden="true"></span>
                </div>
              </div>
            </div>
            <div class="give-flow__row">
              <div class="give-flow__head">
                <div class="give-flow__tit">오프라인 기부</div>
                <p class="give-flow__lead">전국 농협은행, 농축협(약 5,900여개) 대면 접수 창구</p>
              </div>
              <div class="give-steps">
                <div class="give-step">
                  <span class="give-step__num">STEP 01</span>
                  <strong>회원가입</strong>
                  <span class="give-step__ico ico-step05" aria-hidden="true"></span>
                </div>
                <div class="give-step">
                  <span class="give-step__num">STEP 02</span>
                  <strong>고향사랑 기부금<br />기탁서 작성</strong>
                  <span class="give-step__ico ico-step06" aria-hidden="true"></span>
                </div>
                <div class="give-step">
                  <span class="give-step__num">STEP 03</span>
                  <strong>기부하기</strong>
                  <span class="give-step__ico ico-step07" aria-hidden="true"></span>
                </div>
                <div class="give-step">
                  <span class="give-step__num">STEP 04</span>
                  <strong>납입완료 명세서 출력</strong>
                  <span class="give-step__ico ico-step08" aria-hidden="true"></span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="give-benefit inner">
        <div class="give-benefit__col">
          <h3 class="give-benefit__title">답례품 구매 방법은?</h3>
          <ul class="give-path">
            <li class="give-path__item">
              <div class="give-path__left">
                <div class="give-path__img img-01" aria-hidden="true"></div>
              </div>
              <div class="give-path__right"><strong>○○도 기부완료</strong></div>
            </li>
            <li class="give-path__item">
              <div class="give-path__left">
                <div class="give-path__img img-02" aria-hidden="true"></div>
              </div>
              <div class="give-path__right"><strong>○○도<br class="give-mob" /> 기부포인트 생성</strong></div>
            </li>
            <li class="give-path__item">
              <div class="give-path__left">
                <div class="give-path__inner">
                  <div class="give-path__img img-03" aria-hidden="true"></div>
                  <strong>○○도 답례품</strong>
                  <strong class="give-path__blue">구매 가능</strong>
                </div>
              </div>
              <div class="give-path__right">
                <div class="give-path__inner">
                  <div class="give-path__img img-04" aria-hidden="true"></div>
                  <strong>△△도 답례품</strong>
                  <strong class="give-path__red">구매 불가능</strong>
                </div>
              </div>
            </li>
          </ul>
        </div>
        <div class="give-benefit__col">
          <h3 class="give-benefit__title">기부 포인트란?</h3>
          <div class="give-benefit-table">
            <div class="give-benefit-row">
              <div class="give-benefit-row__icon benefit01" aria-hidden="true"></div>
              <div class="give-benefit-row__desc">지자체에서 설정한 비율에 따라<br />최대 30%까지 제공</div>
            </div>
            <div class="give-benefit-row">
              <div class="give-benefit-row__icon benefit02" aria-hidden="true"></div>
              <div class="give-benefit-row__desc">기부 포인트는 기부한 지자체별로 누적</div>
            </div>
            <div class="give-benefit-row">
              <div class="give-benefit-row__icon benefit03" aria-hidden="true"></div>
              <div class="give-benefit-row__desc">고향사랑 기부 완료시, 해당 지자체<br />기부 포인트 생성</div>
            </div>
            <div class="give-benefit-row">
              <div class="give-benefit-row__icon benefit04" aria-hidden="true"></div>
              <div class="give-benefit-row__desc">본인의 기부 포인트는 본인만 사용<br />(양도 불가)</div>
            </div>
            <div class="give-benefit-row">
              <div class="give-benefit-row__icon benefit05" aria-hidden="true"></div>
              <div class="give-benefit-row__desc">고향사랑 답례품 구매는<br />고향사랑 기부 포인트로만</div>
            </div>
            <div class="give-benefit-row">
              <div class="give-benefit-row__icon benefit06" aria-hidden="true"></div>
              <div class="give-benefit-row__desc">기부 포인트는 회원 탈퇴시 소멸<br />(미사용포인트는 유효기간 5년 경과 시<br />자동 삭제)</div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style>
@import '/css/donation_guge.css';
@import '/css/research-box.css';
</style>

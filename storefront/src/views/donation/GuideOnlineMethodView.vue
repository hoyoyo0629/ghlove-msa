<script setup>
// AS-IS donation/guide2.html("온라인 기부방법", 9탭: 기부따라하기/회원가입/금융인증서/로그인/
// 자치단체기부/특정사업기부/전자결제x2/답례품선택, 실이미지 120장 이상) 재현. 대용량(2500+줄)
// 페이지라 gnb_expansion_round에서 확립한 관례대로 raw Thymeleaf HTML을 Node 스크립트로
// 통째 변환했다(th:src -> src만 치환, 나머지 마크업/alt 텍스트는 원본 그대로). 원본은 탭 전환시
// .btn_h3_wrap(버튼 감싸는 부모)에 on 클래스를 토글하므로(guide5와 다른 구조) 그 vanilla JS
// 로직을 그대로 옮기고 정적 onclick="showGuideTab(this)" 속성이 계속 동작하도록 전역에 노출한다.
import { onMounted } from 'vue'

function showGuideTab(btn) {
  const idx = btn.dataset.tab
  document.querySelectorAll('.btn_h3_wrap').forEach((el) => el.classList.remove('on'))
  btn.parentElement.classList.add('on')
  document.querySelectorAll('[data-tab-con]').forEach((el) => {
    const show = el.dataset.tabCon === idx
    el.style.display = show ? '' : 'none'
    el.classList.toggle('view', show)
  })
}

onMounted(() => {
  window.showGuideTab = showGuideTab
})
</script>

<template>
  <section class="center">
    <div class="page-title-box">
      <span class="ali_breadcrumb">
        <router-link to="/"><img class="icon-img" src="/images/icon/cli-icon_home.png" alt="홈으로" /></router-link>
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        안내사항
        <span class="txt-arrow"><span class="sr-only">&gt;</span></span>
        온라인 기부방법
      </span>
      <h2 class="page-title-txt">온라인 기부방법</h2>
    </div>
  </section>
  <section id="contents">
    <div class="center">
<div class="s-contents d-guide first">
                <div class="tab_menu_wrap" id="tabMenu">
                  <div class="tab_group">
                    <div class="btn_h3_wrap on">
                      <button type="button" class="tab_items" data-tab="0" onclick="showGuideTab(this)">
                        기부따라하기
                        
                      </button>
                    </div>

                    <div class="btn_h3_wrap">
                      <button type="button" class="tab_items" data-tab="1" onclick="showGuideTab(this)">
                        회원가입 
                        
                      </button>
                    </div>

                    <div class="btn_h3_wrap">
                      <button type="button" class="tab_items" data-tab="2" onclick="showGuideTab(this)">
                        금융인증서
                        
                      </button>
                    </div>

                    <div class="btn_h3_wrap">
                      <button type="button" class="tab_items" data-tab="3" onclick="showGuideTab(this)">
                        <span class="liLabel">고향사랑e음</span><span class="liLabel">
                          로그인
                          
                        </span>
                      </button>
                    </div>

                    <div class="btn_h3_wrap">
                      <button type="button" class="tab_items" data-tab="4" onclick="showGuideTab(this)">자치단체에<br />
                        기부하기
                        
                      </button>
                    </div>

                    <div class="btn_h3_wrap">
                      <button type="button" class="tab_items" data-tab="5" onclick="showGuideTab(this)">특정사업에<br />
                        기부하기
                        
                      </button>
                    </div>

                    <div class="btn_h3_wrap">
                      <button type="button" class="tab_items" data-tab="6" onclick="showGuideTab(this)" style="display: block">
                        <span class="liLabel">전자결제</span><span class="liLabel xst"><br />(16개 시·도-서울시 제외)</span>
                        
                      </button>
                    </div>

                    <div class="btn_h3_wrap">
                      <button type="button" class="tab_items" data-tab="7" onclick="showGuideTab(this)" style="display: block">
                        <span class="liLabel">전자결제</span><span class="liLabel xst"><br />(서울시)</span>
                        
                      </button>
                    </div>

                    <div class="btn_h3_wrap">
                      <button type="button" class="tab_items" data-tab="8" onclick="showGuideTab(this)">
                        답례품 선택
                        
                      </button>
                    </div>
                  </div>
                </div>
                <!-- 기부따라하기 -->
                <div class="tab_con view" data-tab-con="0">
                  <h3 class="guide-tab-tit">기부따라하기</h3>
                  <div class="btn-box do_video">
                    <button type="button" class="blueBtn do_video" onclick="window.open('https://youtu.be/Xgb2btcKyr4','_blank')" title="새 창 열림">
                      영상으로 보기
                      <span>
                        <img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" />
                      </span>
                    </button>
                  </div>
                  <!-- <div class="video_frame">
                                <iframe class="youtube_frame" src="https://www.youtube.com/embed/Xgb2btcKyr4"
                                    title="YouTube video player" frameborder="0"
                                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                                    allowfullscreen></iframe>
                            </div> -->

                  <div class="card_news">
                    <img
                      src="/images/card-news/card_news01.jpg"
                      alt="
                      		고향의 봄을 부르는 고향사랑 기부제 기부하는 방법
                          저희가 쉽게 알려드립니다!
                          함께해요~
                      	"
                    />
                    <img
                      src="/images/card-news/card_news02.jpg"
                      alt="
                          기부도 하고 혜택도 챙기는 고양사랑 기부제
                      		개인이 고향에 기부하면 답례품과 세액공제를 받는 제도입니다!
                      		기부자는 지자체에 기부를,
                      		국가는 기부자 개인에게 세액공제 혜택을
                      		지자체는 기부자에게 답례품 제공을,
                      		지역주민과 공동체는 주민 복리 증진 혜택을 받습니다.
                          ※ 기부자 주소지에는 기부하실 수 없습니다.
                          ※ 기부는 100원부터 가능합니다.
                      	"
                    />
                    <img
                      src="/images/card-news/card_news03.jpg"
                      alt="
                          PC&middot;모바일에서 회원가입하는 방법
                      		STEP.1 우측 상단 전체메뉴 아이콘 클릭 후 회원가입
                          STEP.2 약관 동의 후 개인정보 입력
                          STEP.3 회원가입 완료료
                          ※ 반드시 본인명의의 휴대폰 또는 금융인증서를 이용하여 가입하여 주시기 바랍니다.
                      	"
                    />
                    <img
                      src="/images/card-news/card_news04.jpg"
                      alt="
                          PC&middot;모바일에서 기부하는 방법
                          STEP.1 로그인 후 기부하기 클릭
                          STEP.2 기부 정보 입력
                          STEP.3 잠시 기다리기
                      	"
                    />
                    <img
                      src="/images/card-news/card_news05.jpg"
                      alt="
                          PC&middot;모바일에서 기부하는 방법
                          STEP.4 납부시스템 약관동의
                          STEP.5 계좌이체 신용카드 중 납부방법 선택
                          STEP.6 결제완료
                          STEP.7 답례품 선택하기-답례품몰 바로가기 클릭릭
                          ※ 신용카드 결제 시 BC전북카드, KB전북카드는 전북은행카드를 선택가기 바랍니다.
                      	"
                    />
                    <img
                      src="/images/card-news/card_news06.jpg"
                      alt="
                          답례품 둘러보기기
                      		STEP.1 '답례품둘러보기' 클릭
                          STEP.2 기부한 지역 선택 후 조회
                          STEP.3 답례품 선택
                          ※ 기부한 지역의 답례품만 선택 가능합니다.
                      	"
                    />
                    <img
                      src="/images/card-news/card_news07.jpg"
                      alt="
                          은행에서 기부하는 방법
                          은행에서 기부하기기
                      		PC 또는 모바일을 활용하여 기부하기 어려우신 분들이나 답례품 선택 방법 등
                      		고향사랑e음에 대한 안내가 필요하신 분들이 이용
                          ※ 5,900여개 전국 농협지점 | 업무시간 09:00 ~ 16:00
                      	"
                    />
                    <img
                      src="/images/card-news/card_news08.jpg"
                      alt="
                      		내 고향에 기부하고 답례품도 받고
                      		고향사랑 기부제
                      		많은 관심 부탁드립니다.
                          고양을 사랑하는 마음!
                          지금 바로 표현하세요!
                      	"
                    />
                  </div>
                </div>

                <div class="tab_con" data-tab-con="1" style="display:none">
                  <h3 class="guide-tab-tit">회원가입</h3>
                  <div class="pc_img">
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/JOIN/1.png"
                        alt="
                        	회원가입은 아래의 순서대로 진행하세요.
                        	1. 회원가입
                        	2. 실명인증
                        	3. 회원정보입력
                        	"
                        class="pc_img"
                        style="margin: auto"
                      />
                      <div class="detailsTxt">
                        <p>온라인 기부를 위해서는 회원에 가입되어 있어야 합니다. 절차에 따라 가입을 진행해 주세요.</p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/JOIN/2.png"
                        alt="
                        	화면 상단에 있는 회원가입 버튼을 선택하여 약관동의 페이지가 열리면
                        	이용약관과 개인정보 수집 이용 동의, 제3자 이용 동의를 체크한 후
                        	다음 버튼 또는 카카오톡 인증 버튼을 클릭해 주세요.
                        	"
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          상단 회원가입 버튼을 선택하여 약관동의 페이지가 열리면
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          이용약관과 개인정보 수집ㆍ이용 동의, 제3자 이용 동의를 체크한 후
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 03.</strong>
                          다음 또는 카카오톡 인증 버튼을 클릭해 주세요.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/JOIN/3.png"
                        alt="
                        	금융인증서의 인증하기 버튼을 클릭하시면
                        	금융인증서로 인증할 수 있는 팝업이 제공되며
                        	휴대폰의 인증하기 버튼을 클릭하시면
                        	휴대폰 문자로 인증을 진행할 수 있는 팝업이 제공됩니다.
                        	"
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p>회원가입을 위해서는 실명인증을 진행하시기 바랍니다.</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 04.</strong>
                          금융인증서 또는 휴대폰 인증을 통해 본인 인증을 진행합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/JOIN/4.png"
                        alt="
                        	정보입력 화면에서는 아이디 입력란에 아이디를 입력 후
                        	중복확인 버튼을 통해 중복체크를 진행합니다.
                        	아이디 입력시 영문은 소문자만 가능하며 글자수는 6~20자
                        	언더바를 제외한 특수문자는 사용 불가능합니다.
                        	중복되지 않은 아이디를 입력하였다면
                        	비밀번호, 비밀번호 확인을 입력합니다.
                        	비밀번호는 숫자, 기호, 영문자가 모두 포함되어야하며
                        	3개 이상 연속된 문자, 숫자는 설정할 수 없고
                        	아이디를 포함할 수 없습니다.
                        	길이는 최소 9자에서 최대 20자까지 설정가능합니다.
                        	이후 회원등록 버튼을 클릭하면 회원 가입이 완료됩니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p>회원 정보를 입력 하시고 회원등록을 진행하세요.</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 05.</strong>
                          아이디 입력 후 중복확인을 해 주세요.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 06.</strong>
                          비밀번호 규칙에 따라 X표시된 항목 전체가 체크 표시로 변경되어야 회원가입이 정상적으로 진행됩니다. 이메일주소, 주소 등
                          필수정보를 입력하고
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 07.</strong>
                          회원등록 버튼을 클릭하면 회원가입이 완료됩니다.
                        </p>
                      </div>
                    </div>
                  </div>
                  <div class="mobile_img">
                    <div class="details_steps">
                      <div class="detailsTxt" style="padding-top: 0px; padding-bottom: 20px">
                        <p style="text-align: center">
                          <strong class="pointblue">회원가입은 아래의 순서대로 진행하세요</strong>
                        </p>
                      </div>
                      <img
                        src="/images/guide/M_GUIDE2/JOIN/1.png"
                        alt="
                        	회원가입은 아래의 순서대로 진행하세요.
                        	1. 회원가입
                        	2. 실명인증
                        	3. 회원정보입력
                        "
                        class="mobile_img"
                      />
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/JOIN/2.png"
                        alt="
                        	오른쪽 상단의 전체메뉴 아이콘을 선택하여 전체메뉴가 열리면
                        	회원가입 버튼을 클릭하여 회원가입 페이지로 이동합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>온라인 기부를 위해서는 회원에 가입되어 있어야 합니다. 절차에 따라 가입을 진행해 주세요.</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          상단 회원가입 버튼을 선택하여 약관동의 페이지가 열리면
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/JOIN/3.png"
                        alt="
                        	회원가입 페이지에서 이용약관 확인 후 모두 동의를 체크하고
                        	다음 또는 카카오톡 인증 버튼을 눌러서 본인인증 또는 카카오톡 인증을 진행합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          이용약관과 개인정보수집 제3자 이용에 동의를 체크한 후
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 03.</strong>
                          다음 또는 카카오톡 인증 버튼을 클릭해 주세요.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/JOIN/4.png"
                        alt="
                        	다음버튼을 눌러서 화면이동 했을 경우
                        	본인인증화면에서 금융인증서 인증하기 또는 휴대폰 인증하기 버튼을 클릭하여
                        	인증을 진행하면 완료 후 정보입력 화면으로 이동합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>회원가입을 위해서는 실명인증을 진행하시기 바랍니다.</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 04.</strong>
                          금융인증서 또는 휴대폰 인증을 통해 본인 인증을 진행합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/JOIN/5.png"
                        alt="
                        	정보입력 화면에서 아이디는 영문, 숫자 및 특수문자 언더바, 하이픈 포함 6~20자로 입력 가능합니다.
                        	비밀번호는 숫자, 기호, 영문자 포함하여야 하며 3개 이상 연속 문자나 숫자는 불가합니다.
                        	비밀번호에 아이디를 포함할 수 없으며 9~20자로 설정 가능합니다.
                        	그외  휴대폰 번호, 이메일, 주소, 알림서비스 수신동의, 관심지자체 정보 등을 입력 후 회원등록
                        	버튼을 선택하여 회원 가입을 완료합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>회원 정보를 입력 하시고 회원등록을 진행하세요.</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 05.</strong>
                          아이디 입력 후 중복확인을 해 주세요.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 06.</strong>
                          비밀번호 규칙에 따라 X표시된 항목 전체가 체크 표시로 변경되어야 회원가입이 정상적으로 진행됩니다. 이메일주소, 주소 등
                          필수정보를 입력하고
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 07.</strong>
                          회원등록 버튼을 클릭하면 회원가입이 완료됩니다.
                        </p>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 금융인증서 -->
                <div class="tab_con" data-tab-con="2" style="display:none">
                  <h3 class="guide-tab-tit">금융인증서</h3>
                  <div class="fince_explan">
                    <div class="fince_header">
                      <div>
                        <img src="/images/icon/cli-icon-bullet.png" alt="총알 아이콘" />
                        <div class="fince_tit">
                          <span>고향사랑e음 금융인증서로 회원가입 및 본인인증 방법(내국인)</span>
                        </div>
                      </div>
                      <div class="fince_sub_header">
                        <div class="fince_line flex_1"></div>
                        <div class="fince_block"></div>
                        <span class="fince_sub_tit">기본 절차 이용화면</span>
                        <div class="fince_block"></div>
                        <div class="fince_line right flex_1"></div>
                      </div>
                      <div class="fince_sub_explan">
                        <img src="/images/icon/flag_wait.png" alt="잠깐" />
                        <span class="fince_wait_txt line_270"
                          >기부자의 금융계좌가 개설되어 있는 금융기관을 통해 먼저 금융인증서를 발급 받으셔야 합니다.</span
                        >
                      </div>
                    </div>

                    <div class="pc_img">
                      <div class="details_steps">
                        <div class="flex_1">
                          <img
                            src="/images/do_info/pc_fince_give_01.png"
                            alt="
                            	회원가입 화면에서 약관 동의 후 본인 인증 화면이 열린 후
                            	금융인증서 부분의 인증하기 버튼을 선택하시면
                            	금융인증서 인증을 위한 화면이 제공됩니다.
                            "
                            class="pc_img margin_center"
                          />
                          <div class="fince_description">
                            <p>회원가입 > 약관동의 > 본인인증에서</p>
                            <p><strong>금융인증서를 선택</strong>해 주세요.</p>
                          </div>
                        </div>
                        <div class="flex_1">
                          <img
                            src="/images/do_info/pc_fince_give_02.png"
                            alt="
                            	금융인증서 인증을 위한 화면에서 금융인증서에 가입된
                            	이름, 휴대폰 번호, 생년월일을 입력 후 본인 인증을 진행해주세요.
                            "
                            class="pc_img margin_center"
                          />
                          <div class="fince_description">
                            <p>가입정보 확인을 위해</p>
                            <p><strong>이름/전화번호/생년월일</strong>을</p>
                            <p>입력해 주세요</p>
                          </div>
                        </div>
                        <div class="flex_1">
                          <img
                            src="/images/do_info/pc_fince_give_03.png"
                            alt="
                            	화면에 확인코드가 표시되며 선택한 ARS 응답 또는 문자로 확인코드를 발송해주세요.
                            	ARS 또는 문자발송 전화번호는 1688-4932 입니다.
                            "
                            class="pc_img margin_center"
                          />
                          <div class="fince_description">
                            <p>본인확인을 위해 휴대폰으로</p>
                            <p>받은 <strong>SMS/ARS 확인코드 숫자를</strong></p>
                            <p><strong>입력</strong>해 주세요</p>
                          </div>
                        </div>
                        <div class="flex_1">
                          <img
                            src="/images/do_info/pc_fince_give_04.png"
                            alt="
                            	휴대폰 인증이 완료되면 본인인증에 사용할 인증서를 선택해주세요.
                            "
                            class="pc_img margin_center"
                          />
                          <div class="fince_description">
                            <p><strong>사용할 인증서</strong>를 선택해 주세요</p>
                          </div>
                        </div>
                      </div>

                      <div class="details_steps" style="display: flex">
                        <div class="flex_1">
                          <img
                            src="/images/do_info/pc_fince_give_05.png"
                            alt="
                            	선택할 인증서의 비밀번호 6자리를 입력하여 금융인증서 본인인증을 완료합니다.
                            "
                            class="pc_img margin_center"
                          />
                          <div class="fince_description">
                            <p>금융인증서 <strong>비밀번호 6자리</strong>를</p>
                            <p>입력해 주세요</p>
                          </div>
                        </div>
                        <div class="flex_1">
                          <img
                            src="/images/do_info/pc_fince_give_06.png"
                            alt="
                            	금융인증서 본인인증이 완료되면 고향사랑e음에 가입할 회원정보를 입혁하는
                            	화면이 표시되며 아이디, 비밀번호 등 가입정보를 입력해주세요.
                            "
                            class="pc_img margin_center"
                          />
                          <div class="fince_description">
                            <p>아이디, 비밀번호 등 <strong>회원</strong></p>
                            <p><strong>가입정보</strong>를 입력해 주세요</p>
                          </div>
                        </div>
                        <div class="flex_1">
                          <img
                            src="/images/guide/PC_GUIDE2/FIN_CERT/1.png"
                            alt="
                            	회원가입 완료 화면이 표시됩니다.
                            	확인 버튼 선택 후 로그인 페이지로 이동하여 로그인 하세요.
                            "
                            class="pc_img margin_center"
                          />
                          <div class="fince_description">
                            <p>회원가입 완료 버튼 선택하여</p>
                            <p>로그인해 주세요</p>
                          </div>
                        </div>
                        <div class="flex_1"></div>
                      </div>
                    </div>

                    <div class="mobile_img">
                      <div class="details_steps">
                        <div class="flex_1">
                          <img
                            src="/images/do_info/m_fince_give_01.png"
                            alt="
                            	회원가입 화면에서 약관 동의 후 본인 인증 화면이 열린 후
                            	금융인증서 부분의 인증하기 버튼을 선택하시면
                            	금융인증서 인증을 위한 화면이 제공됩니다.
                            "
                            class="mobile_img margin_center"
                          />
                          <div class="fince_description">
                            <p>
                              회원가입 > 약관동의 > 본인인증에서
                              <strong>금융인증서를 선택</strong>해 주세요.
                            </p>
                          </div>
                        </div>

                        <div class="flex_1">
                          <img
                            src="/images/do_info/m_fince_give_02.png"
                            alt="
                            	금융인증서 인증을 위한 화면에서 금융인증서에 가입된
                            	이름, 휴대폰 번호, 생년월일을 입력 후 본인 인증을 진행해주세요.
                            "
                            class="mobile_img margin_center"
                          />
                          <div class="fince_description">
                            <p>가입정보 확인을 위해</p>
                            <p><strong>이름/전화번호/생년월일</strong>을 입력해 주세요</p>
                          </div>
                        </div>
                      </div>
                      <div class="details_steps">
                        <div class="flex_1">
                          <img
                            src="/images/do_info/m_fince_give_03.png"
                            alt="
                            	화면에 확인코드가 표시되며 선택한 ARS 응답 또는 문자로 확인코드를 발송해주세요.
                            	ARS 또는 문자발송 전화번호는 1688-4932 입니다.
                            "
                            class="mobile_img margin_center"
                          />
                          <div class="fince_description">
                            <p>
                              본인확인을 위해 휴대폰으로 받은
                              <strong>SMS/ARS 확인코드 숫자를 입력</strong>해 주세요
                            </p>
                          </div>
                        </div>

                        <div class="flex_1">
                          <img
                            src="/images/do_info/m_fince_give_04.png"
                            alt="
                            	휴대폰 인증이 완료되면 본인인증에 사용할 인증서를 선택해주세요.
                            "
                            class="mobile_img margin_center"
                          />
                          <div class="fince_description">
                            <p><strong>사용할 인증서</strong>를 선택해 주세요</p>
                          </div>
                        </div>
                      </div>
                      <div class="details_steps">
                        <div class="flex_1">
                          <img
                            src="/images/do_info/m_fince_give_05.png"
                            alt="
                            	선택할 인증서의 비밀번호 6자리를 입력하여 금융인증서 본인인증을 완료합니다.
                            "
                            class="mobile_img margin_center"
                          />
                          <div class="fince_description">
                            <p>금융인증서 <strong>비밀번호 6자리</strong>를 입력해 주세요</p>
                          </div>
                        </div>

                        <div class="flex_1">
                          <img
                            src="/images/do_info/m_fince_give_06.png"
                            alt="
                            	금융인증서 본인인증이 완료되면 고향사랑e음에 가입할 회원정보를 입혁하는
                            	화면이 표시되며 아이디, 비밀번호 등 가입정보를 입력해주세요.
                            "
                            class="mobile_img margin_center"
                          />
                          <div class="fince_description">
                            <p>
                              아이디, 비밀번호 등
                              <strong>회원 가입정보</strong>를 입력해 주세요
                            </p>
                          </div>
                        </div>
                      </div>
                      <div class="details_steps">
                        <div class="flex_1">
                          <!-- <img
                            src="/images/do_info/m_fince_give_07.png"
                            alt="
                            	회원가입 완료 화면이 표시됩니다.
                            	확인 버튼으로 로그인 페이지 이동 후 로그인 하세요.
                            "
                            class="mobile_img margin_center"
                          /> -->
                          <img
                            src="/images/guide/M_GUIDE2/FIN_CERT/1.png"
                            alt="
                            	회원가입 완료 화면이 표시됩니다.
                            	확인 버튼 선택 후 로그인 페이지로 이동하여 로그인 하세요.
                            "
                            class="mobile_img margin_center"
                          />
                          <div class="fince_description">
                            <p>회원가입 완료 버튼 선택하여 로그인해 주세요</p>
                          </div>
                        </div>

                        <div class="flex_1"></div>
                      </div>
                    </div>
                  </div>

                  <div class="fince_explan">
                    <div class="fince_header">
                      <div>
                        <img src="/images/icon/cli-icon-bullet.png" alt="총알 아이콘" />
                        <div class="fince_tit">
                          <span>고향사랑e음 금융인증서로 회원가입 및 본인인증 방법 (해외에 체류중인 국민)</span>
                        </div>
                      </div>

                      <div class="fince_sub_header" style="height: auto">
                        <div class="fince_line flex_1 m_hide"></div>
                        <div class="fince_block"></div>
                        <span class="fince_sub_tit">해외전화번호, 전화거는 ARS인증</span>
                        <div class="fince_block"></div>
                        <div class="fince_line right flex_1 m_hide"></div>
                      </div>
                      <div class="fince_sub_explan">
                        <img src="/images/icon/flag_wait.png" alt="잠깐" />
                        <div class="line_139">
                          <p class="fince_wait_txt">기부자의 금융계좌가 개설되어 있는 금융기관을 통해 먼저 금융인증서를 발급 받으셔야 합니다.</p>
                          <p class="fince_wait_sub_txt">
                            ※ 국내 계좌보유 등 전자금융거래서비스에 가입한 해외 체류중인 국민(재외국민)도 금융인증서를 활용하여 국내 휴대전화 없이
                            해외전화번호 ARS인증을 통해 회원가입 가능
                          </p>
                        </div>
                      </div>
                    </div>
                    <div class="pc_img">
                      <div class="details_steps" style="display: flex">
                        <div class="flex_1">
                          <img
                            src="/images/do_info/pc_fince_give_overseas_01.png"
                            alt="
                            	회원가입 화면에서 약관 동의 후 본인 인증 화면이 열린 후
                            	금융인증서 부분의 인증하기 버튼을 선택하시면
                            	금융인증서 인증을 위한 화면이 제공됩니다.
                            "
                            class="pc_img margin_center"
                          />
                          <div class="fince_description">
                            <p>회원가입 > 약관동의 > 본인인증에서</p>
                            <p><strong>금융인증서를 선택</strong>해 주세요.</p>
                          </div>
                        </div>
                        <div class="flex_1">
                          <img
                            src="/images/do_info/pc_fince_give_overseas_02.png"
                            alt="
                            	금융인증서비스 정보입력 화면 하단 오른쪽에 해외전화 이용 버튼을 선택해주세요.
                            "
                            class="pc_img margin_center"
                          />
                          <div class="fince_description">
                            <p>금융인증서 이용을 위해</p>
                            <p><strong>해외전화 이용</strong>을 선택해 주세요</p>
                          </div>
                        </div>
                        <div class="flex_1">
                          <img
                            src="/images/do_info/pc_fince_give_overseas_03.png"
                            alt="
                            	금융인증서 인증을 위한 화면에서 금융인증서에 가입된
                            	이름, 휴대폰 번호, 생년월일을 입력 후 본인 인증을 진행해주세요.
                            "
                            class="pc_img margin_center"
                          />
                          <div class="fince_description">
                            <p>가입정보 확인을 위해</p>
                            <p><strong>이름/전화번호/생년월일을</strong> 입력해 주세요</p>
                          </div>
                        </div>
                        <div class="flex_1">
                          <img
                            src="/images/do_info/pc_fince_give_overseas_04.png"
                            alt="
                            	화면에 확인코드가 표시되며 선택한 ARS 응답 또는 문자로 확인코드를 발송해주세요.
                            	ARS 또는 문자발송 전화번호는 1688-4932 입니다.
                            "
                            class="pc_img margin_center"
                          />
                          <div class="fince_description">
                            <p>본인확인을 위해 휴대폰으로</p>
                            <p>받은 <strong>SMS/ARS 확인코드 숫자를</strong> <strong>입력</strong>해 주세요.</p>
                          </div>
                        </div>
                      </div>

                      <div class="details_steps" style="display: flex">
                        <div class="flex_1">
                          <img
                            src="/images/do_info/pc_fince_give_overseas_05.png"
                            alt="
                            	휴대폰 인증이 완료되면 본인인증에 사용할 인증서를 선택해주세요.
                            "
                            class="pc_img margin_center"
                          />
                          <div class="fince_description">
                            <p><strong>사용할 인증서</strong>를 선택해 주세요</p>
                          </div>
                        </div>
                        <div class="flex_1">
                          <img
                            src="/images/do_info/pc_fince_give_overseas_06.png"
                            alt="
                            	선택할 인증서의 비밀번호 6자리를 입력하여 금융인증서 본인인증을 완료합니다.
                            "
                            class="pc_img margin_center"
                          />
                          <div class="fince_description">
                            <p>
                              금융인증서
                              <strong>비밀번호 6자리를</strong> 입력해 주세요
                            </p>
                          </div>
                        </div>
                        <div class="flex_1">
                          <img
                            src="/images/do_info/pc_fince_give_overseas_07.png"
                            alt="
                            	금융인증서 본인인증이 완료되면 고향사랑e음에 가입할 회원정보를 입혁하는
                            	화면이 표시되며 아이디, 비밀번호 등 가입정보를 입력해주세요.
                            "
                            class="pc_img margin_center"
                          />
                          <div class="fince_description">
                            <p>아이디, 비밀번호 등 <strong>회원</strong> <strong>가입정보</strong>를 입력해 주세요</p>
                          </div>
                        </div>
                        <div class="flex_1">
                          <img
                            src="/images/guide/PC_GUIDE2/FIN_CERT/2.png"
                            alt="
                            	회원가입 완료 화면이 표시됩니다.
                            	확인 버튼 선택 후 로그인 페이지로 이동하여 로그인 하세요.
                            "
                            class="pc_img margin_center"
                          />
                          <div class="fince_description">
                            <p>회원가입 완료 버튼 선택하여</p>
                            <p>로그인해 주세요</p>
                          </div>
                        </div>
                      </div>
                    </div>

                    <div class="mobile_img">
                      <div class="details_steps">
                        <div class="flex_1">
                          <img
                            src="/images/do_info/m_fince_give_overseas_01.png"
                            alt="
                            	회원가입 화면에서 약관 동의 후 본인 인증 화면이 열린 후
                            	금융인증서 부분의 인증하기 버튼을 선택하시면
                            	금융인증서 인증을 위한 화면이 제공됩니다.
                            "
                            class="mobile_img margin_center"
                          />
                          <div class="fince_description">
                            <p>
                              회원가입 > 약관동의 > 본인인증에서
                              <strong>금융인증서를 선택</strong>해 주세요.
                            </p>
                          </div>
                        </div>

                        <div class="flex_1">
                          <img
                            src="/images/do_info/m_fince_give_overseas_02.png"
                            alt="
                            	금융인증서비스 정보입력 화면 하단 오른쪽에 해외전화 이용 버튼을 선택해주세요.
                            "
                            class="mobile_img margin_center"
                          />
                          <div class="fince_description">
                            <p>
                              금융인증서 이용을 위해
                              <strong>해외전화 이용</strong>을 선택해 주세요
                            </p>
                          </div>
                        </div>
                      </div>
                      <div class="details_steps">
                        <div class="flex_1">
                          <img
                            src="/images/do_info/m_fince_give_overseas_03.png"
                            alt="
                            	금융인증서 인증을 위한 화면에서 금융인증서에 가입된
                            	이름, 휴대폰 번호, 생년월일을 입력 후 본인 인증을 진행해주세요.
                            "
                            class="mobile_img margin_center"
                          />
                          <div class="fince_description">
                            <p>가입정보 확인을 위해</p>
                            <p><strong>이름/전화번호/생년월일을</strong>입력해 주세요</p>
                          </div>
                        </div>

                        <div class="flex_1">
                          <img
                            src="/images/do_info/m_fince_give_overseas_04.png"
                            alt="
                            	화면에 확인코드가 표시되며 선택한 ARS 응답 또는 문자로 확인코드를 발송해주세요.
                            	ARS 또는 문자발송 전화번호는 1688-4932 입니다.
                            "
                            class="mobile_img margin_center"
                          />
                          <div class="fince_description">
                            <p>
                              본인확인을 위해 휴대폰으로 받은
                              <strong>SMS/ARS 확인코드 숫자를 입력</strong>해 주세요
                            </p>
                          </div>
                        </div>
                      </div>
                      <div class="details_steps">
                        <div class="flex_1">
                          <img
                            src="/images/do_info/m_fince_give_overseas_05.png"
                            alt="
                            	휴대폰 인증이 완료되면 본인인증에 사용할 인증서를 선택해주세요.
                            "
                            class="mobile_img margin_center"
                          />
                          <div class="fince_description">
                            <p><strong>사용할 인증서</strong>를 선택해 주세요</p>
                          </div>
                        </div>

                        <div class="flex_1">
                          <img
                            src="/images/do_info/m_fince_give_overseas_06.png"
                            alt="
                            	선택할 인증서의 비밀번호 6자리를 입력하여 금융인증서 본인인증을 완료합니다.
                            "
                            class="mobile_img margin_center"
                          />
                          <div class="fince_description">
                            <p>금융인증서 <strong>비밀번호 6자리</strong>를 입력해 주세요</p>
                          </div>
                        </div>
                      </div>
                      <div class="details_steps">
                        <div class="flex_1">
                          <img
                            src="/images/do_info/m_fince_give_overseas_07.png"
                            alt="
                            	금융인증서 본인인증이 완료되면 고향사랑e음에 가입할 회원정보를 입혁하는
                            	화면이 표시되며 아이디, 비밀번호 등 가입정보를 입력해주세요.
                            "
                            class="mobile_img margin_center"
                          />
                          <div class="fince_description">
                            <p>
                              아이디, 비밀번호 등
                              <strong>회원 가입정보</strong>를 입력해 주세요
                            </p>
                          </div>
                        </div>

                        <div class="flex_1">
                          <!-- <img
                            src="/images/do_info/m_fince_give_overseas_08.png"
                            alt=""
                            class="mobile_img margin_center"
                          /> -->
                          <img
                            src="/images/guide/M_GUIDE2/FIN_CERT/2.png"
                            alt="
                            	회원가입 완료 화면이 표시됩니다.
                            	확인 버튼 선택 후 로그인 페이지로 이동하여 로그인 하세요.
                            "
                            class="mobile_img margin_center"
                          />
                          <div class="fince_description">
                            <p>회원가입 완료 버튼 선택하여 로그인해 주세요</p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 로그인 안내 -->
                <div class="tab_con" data-tab-con="3" style="display:none">
                  <h3 class="guide-tab-tit">고향사랑e음 로그인</h3>
                  <div class="details_steps">
                    <div class="mobile_img">
                      <div class="detailsTxt" style="padding-top: 0px; padding-bottom: 20px">
                        <p style="text-align: center">
                          <strong class="pointblue">로그인은 4가지 방식</strong>
                        </p>
                      </div>
                    </div>
                    <img
                      src="/images/guide/PC_GUIDE2/LOGIN/1.png"
                      alt="
                      	로그인은 4가지 방식
                      	1. 아이디 로그인
                      	2. 카카오 로그인
                      	3. 금융/간편 인증
                      	4. 디지털 원패스

                      	메인화면에서 화면 상단 로그인 버튼을 누르면 로그인 페이지로 이동합니다.
                      	로그인 페이지에서 아이디와 비밀번호를 입력 후 로그인 버튼을 누르면
                      	로그인이 진행됩니다.
                      "
                      class="pc_img"
                    />
                    <img
                      src="/images/guide/M_GUIDE2/LOGIN/1.png"
                      alt="
                      	로그인은 4가지 방식
                      	1. 아이디 로그인
                      	2. 카카오 로그인
                      	3. 금융/간편 인증
                      	4. 디지털 원패스

                      	화면 상단 로그인 버튼을 누르면 로그인 페이지로 이동합니다.
                      	또는 전체메뉴 클릭 후 표시되는 메뉴 화면에서 로그인 버튼을 눌러 로그인 페이지로 이동합니다.
                      	로그인 페이지에서 아이디와 비밀번호를 입력 후 로그인 버튼을 누르면
                      	로그인이 진행됩니다.
                      "
                      class="mobile_img"
                    />

                    <div class="detailsTxt">
                      <p>회원가입 시 등록한 아이디 / 패스워드로 로그인을 진행하세요.</p>
                      <p style="margin-left: 25px">
                        <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                        상단 로그인 버튼을 선택하여 로그인 창이 열리면
                      </p>
                      <p style="margin-left: 25px">
                        <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                        아이디 / 패스워드를 입력 후 로그인을 진행해 주세요
                      </p>
                    </div>
                  </div>
                  <div class="details_steps">
                    <img
                      src="/images/guide/PC_GUIDE2/LOGIN/2.png"
                      alt="
                      	카카오톡 인증 로그인 방법은
                      	로그인 화면에서 카카오톡 인증 로그인 버튼을 누르면
                      	카카오톡 인증 로그인 화면으로 이동합니다.
                      	카카오톡 인증 로그인 화면에서 카카오톡 아이디와 비밀번호를 입력 후 카카오톡 인증을 진행합니다.
                      "
                      class="pc_img"
                    />
                    <img
                      src="/images/guide/M_GUIDE2/LOGIN/2.png"
                      alt="
                      	카카오톡 인증 로그인 방법은
                      	로그인 화면에서 카카오톡 인증 로그인 버튼을 누르면
                      	카카오톡 인증 로그인 화면으로 이동합니다.
                      	카카오톡 인증 로그인 화면에서 카카오톡 아이디와 비밀번호를 입력 후 카카오톡 인증을 진행합니다.
                      "
                      class="mobile_img"
                    />

                    <div class="detailsTxt">
                      <p>카카오톡 인증으로 간편하게 로그인 하세요</p>
                      <p style="margin-left: 25px">
                        <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                        화면 상단의 로그인 > 아이디 로그인 선택 후
                      </p>
                      <p style="margin-left: 25px">
                        <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                        카카오톡 인증 로그인 버튼 클릭 후
                      </p>
                      <p style="margin-left: 25px">
                        <strong class="pointblue" style="margin-left: -25px"> 03.</strong>
                        카카오톡 인증 로그인 창이 열리면 카카오톡 아이디와 비밀번호를 입력하여 진행하면 카카오톡 인증 후 로그인이 됩니다.
                      </p>
                    </div>
                  </div>
                  <div class="details_steps">
                    <img
                      src="/images/guide/PC_GUIDE2/LOGIN/3.png"
                      alt="
                      	로그인 화면에서 금융/간편 인증 탭을 선택하면
                      	금융인증서와 간편인증을 통한 로그인이 가능합니다.
                      	금융인증서 로그인 버튼을 선택하면 금융인증서 로그인 진행을 위한 팝업이 제공되며
                      	간편인증 로그인 버튼을 선택하면 간편인증 로그인 진행을 위한 팝업이 제공됩니다.
                      "
                      class="pc_img"
                    />
                    <img
                      src="/images/guide/M_GUIDE2/LOGIN/3.png"
                      alt="
                      	로그인 화면에서 금융/간편 인증 탭을 선택하면
                      	금융인증서와 간편인증을 통한 로그인이 가능합니다.
                      	금융인증서 로그인 버튼을 선택하면 금융인증서 로그인 진행을 위한 팝업이 제공되며
                      	간편인증 로그인 버튼을 선택하면 간편인증 로그인 진행을 위한 팝업이 제공됩니다.
                      "
                      class="mobile_img"
                    />

                    <div class="detailsTxt">
                      <p>금융인증서 및 간편인증서로 로그인 하세요</p>
                      <p style="margin-left: 25px">
                        <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                        금융/간편 인증 탭을 선택합니다.
                      </p>
                      <p></p>
                      <p style="margin-left: 25px">
                        <strong class="pointblue" style="margin-left: -25px"> 03.</strong>
                        금융인증서 로그인 버튼을 클릭합니다.
                      </p>
                      <p style="margin-left: 25px">
                        <strong class="pointblue" style="margin-left: -25px"> 04.</strong>
                        금융인증 창이 열리면 인증에 필요한 정보를 입력하고 인증을 진행하면 로그인이 됩니다.
                      </p>
                    </div>
                  </div>
                  <div class="details_steps">
                    <img
                      src="/images/guide/PC_GUIDE2/LOGIN/4.png"
                      alt="
                      	로그인 화면에서 디지털원패스 탭을 선택하면
                      	디지털원패스를 통한 로그인이 가능합니다.
                      	디지털원패스 아이디를 입력 후 디지털 원패스 로그인 버튼을 선택하면
                      	디지털원패스 로그인 진행을 위한 팝업이 제공됩니다.
                      "
                      class="pc_img"
                    />
                    <img
                      src="/images/guide/M_GUIDE2/LOGIN/4.png"
                      alt="
                      	로그인 화면에서 디지털원패스 탭을 선택하면
                      	디지털원패스를 통한 로그인이 가능합니다.
                      	디지털원패스 아이디를 입력 후 디지털 원패스 로그인 버튼을 선택하면
                      	디지털원패스 로그인 진행을 위한 팝업이 제공됩니다.
                      "
                      class="mobile_img"
                    />

                    <div class="detailsTxt">
                      <p>디지털원패스로 간편하게 로그인하세요</p>
                      <p style="margin-left: 25px">
                        <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                        <span>디지털 원패스로 로그인을 진행 하시려면 사전에 디지털 원패스 등록을 하셔야 됩니다.</span>
                      </p>
                      <p style="margin-left: 25px">기존에 갖고 계신 아이디를 입력 후 "디지털 원패스 로그인"버튼을 클릭하세요</p>
                      <p style="margin-left: 25px">
                        <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                        다음으로 인증창이 노출 됩니다.
                      </p>
                      <p style="margin-left: 25px">
                        <strong class="pointblue" style="margin-left: -25px"> 03.</strong>
                        인증 창에서 핸드폰 인증을 거치면 자동 로그인이 진행 되며 고향사랑e음 메인으로 이동합니다.
                      </p>
                    </div>
                  </div>
                </div>

                <!-- 자치단체 기부하기 안내 -->
                <div class="tab_con" data-tab-con="4" style="display:none">
                  <h3 class="guide-tab-tit">자치단체 기부하기 안내</h3>
                  <div class="pc_img">
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/DONATION/1.png"
                        alt="
                        	자치단체에 기부하기 순서는 다음과 같습니다.
                        	1. 고향사랑 e음 접속
                        	2. 기부하기
                        	3. 결제하기
                        	4. 답례품 신청 하기

                        	기부페이지 이동 방법은
                        	메인화면 상단 기부하기 메뉴에 마우스 이동 후 아래에 펼쳐지는 하위 메뉴 중 자치단체에 기부하기 버튼을 클릭
                        	또는
                        	배너 하단에 자치단체에 기부하기 버튼을 클릭하세요.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p>고향사랑e음에 접속 하신 후 자치단체에 기부하기 버튼을 클릭 하세요.</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          고향사랑e음 메인에서 자치단체에 기부하기를 선택 하세요.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/DONATION/2.png"
                        alt="
                        	기부하기 페이지에서 기부할 지역을 선택 후 특정사업 선택 칸에서 자치단체에 기부하기를 선택해주세요.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          <strong class="pointblue">기부하기</strong>
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          기부 지역을 선택 하시고 자치단체에 기부하기를 선택 하세요.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/DONATION/3.png"
                        alt="
                        	기부가능 여부 확인 칸에서 주민등록번호 또는 외국인등록번호(거소신고번호) 뒷자리를 입력 후 주소확인하기 버튼을 클릭해주세요.
                        	이름과 생년월일은 회원가입 정보로 자동 설정합니다.
                        	카카오톡 인증으로 회원가입을 진행했을 경우 최초 기부시에만 생년월일 변경이 가능합니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          기부 가능여부를 확인해 주세요.
                        </p>
                        <p style="margin-left: 25px">
                          자신의 주민등록 번호를 입력 후 주소확인하기 버튼을 클릭 하시면 기부 가능여부를 확인 하실 수 있습니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/DONATION/4.png"
                        alt="
                        	기부가 가능하면 주민등록번호 또는 외국인등록번호(거소신고번호) 입력칸 하단에 기부가능 메시지가 표시되며
                        	기부 불가능시 기부불가능 사유가 담긴 알림창이 표시됩니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 03.</strong>
                          기부가능 여부를 확인합니다.
                        </p>
                        <p style="margin-left: 25px">자신의 주민등록상의 주소지에는 기부를 하실 수 없습니다.</p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/DONATION/5.png"
                        alt="
                        	기부액 칸에 기부금액을 입력하시고 답례품 지급 칸에 답례품 제공여부를 선택해주세요.
                        	답례품을 제공 받음을 선택하시면 기부지역의 답례품 포인트가 지급됩니다.

                        	확인 항목 전체 체크 후
                        	기부금 납부하기 버튼을 선택하면 결제 팝업 화면이 표시됩니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 04.</strong>
                          답례품 제공여부를 확인해주세요
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblack"> "답례품을 제공 받음"</strong>을 선택 하시면 답례품 포인트를 지급받게 됩니다.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 05.</strong>
                          기부금 납부하기 버튼을 클릭 하시면 결제하기 페이지로 이동합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/DONATION/6.png"
                        alt="
                        	기부지역이 서울일 경우 결제 화면에서 은행 납부, 카드 납부, 간편 결제로 결제가 가능합니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 결제하기(서울)</strong>
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          고향사랑e음 시스템에서는 계좌이체, 신용카드, 간편결제를 제공합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/DONATION/7.png"
                        alt="
                        	기부지역이 서울 외 다른 지역일 경우 결제 팝업창에서 납부시스템 약관동의화면이 표시되며 전체 동의 후 확인버튼 클릭시
                        	결제 화면이 제공됩니다.
                        	결제화면에서 계좌이체, 신용카드, 간편결제로 결제가 가능합니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 결제하기(지방)</strong>
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          납부시스템 약관 동의, 확인 클릭
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          고향사랑e음 시스템에서는 계좌이체, 신용카드, 간편결제를 제공합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img src="/images/guide/PC_GUIDE2/DONATION/8.png" alt="답례품 목록 사진입니다." class="pc_img" />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 답례품 선택 하기</strong>
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          기부 하고 받으신 포인트로 기부하신 지역의 답례품을 선택 하세요.
                        </p>
                      </div>
                    </div>
                  </div>
                  <div class="mobile_img">
                    <div class="details_steps">
                      <div class="detailsTxt" style="padding-top: 0px; padding-bottom: 20px">
                        <p style="text-align: center">
                          <strong class="pointblue">자치단체에 기부하기 순서는 다음과 같습니다.</strong>
                        </p>
                      </div>
                      <img
                        src="/images/guide/M_GUIDE2/DONATION/1.png"
                        alt="
                        	자치단체에 기부하기 순서는 다음과 같습니다.
                        	1. 고향사랑e음 접속
                        	2. 기부하기
                        	3. 결제하기
                        	4. 답례품 신청하기

                        	메인화면에서 오른쪽 상단 전체메뉴 아이콘을 선택하면 메뉴화면이 표시됩니다.
                        	이중 기부하기 메뉴를 클릭하여 나오는 하위메뉴 중 자치단체에 기부하기 버튼을 누르면
                        	기부하기 화면으로 이동합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>고향사랑e음 메인에서 자치단체에 기부하기를 선택 하세요</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          고향사랑e음 메인에서 자치단체에 기부하기를 선택 하세요
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/DONATION/2.png"
                        alt="
                        	기부지자체 선택 항목에서
                        	기부할 지역을 선택 후
                        	특정사업 선택 항목에서 자치단체에 기부하기를 선택해주세요.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          <strong class="pointblue">기부하기</strong>
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          기부지역을 선택 하시고 자치단체에 기부하기를 선택 하세요.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/DONATION/3.png"
                        alt="
                        	기부가능 여부 확인 방법은 이름과 생년월일을 가입정보에서 가져오므로
                        	주민등록번호 또는 외국인등록번호(거소신고번호) 뒷자리만 입력 후 주소확인하기 버튼을 선택해서 진행합니다.
                        	카카오로 가입했을 경우 최초 기부시에만 생년월일 변경이 가능합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          기부 가능여부를 확인해 주세요.
                        </p>
                        <p>자신의 주민등록 번호를 입력 후 주소확인하기 버튼을 클릭 하시면 기부 가능여부를 확인 하실 수 있습니다.</p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/DONATION/4.png"
                        alt="
                        	기부가능시 주소확인하기 아래에 기부가 가능하다고 표시되며
                        	기부 불가능시 기부불가능한 사유 내용이 담긴 팝업창이 표시됩니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 03.</strong>
                          기부 가능 여부를 확인 합니다.
                        </p>
                        <p>자신의 주민등록상의 주소지에는 기부를 하실 수 없습니다.</p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/DONATION/5.png"
                        alt="
                        	기부액 칸에 기부금액을 입력하시고 답례품 지급 칸에 답례품 제공여부를 선택해주세요.
                        	답례품을 제공 받음을 선택하시면 기부지역의 답례품 포인트가 지급됩니다.

                        	동의 항목 확인 및 전체 동의 후 기부금 납부하기 버튼을 선택하면 결제 팝업 화면이 표시됩니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 04.</strong>
                          답례품 제공여부를 확인해 주세요.
                        </p>
                        <p></p>
                        <p style="margin-left: 25px">
                          <strong class="pointblack"> "답례품을 제공 받음"</strong>을 선택 하시면 답례품 포인트를 지급받게 됩니다.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 05.</strong>
                          기부금 납부하기 버튼을 클릭 하시면 결제하기 페이지로 이동 합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/DONATION/6.png"
                        alt="
                        	기부지역이 서울일 경우 결제 화면에서 은행납부, 신용카드, 간편결제로 결제가 가능합니다.
                        	납부 방법을 선택하시고 동의 및 기타 입력사항을 입력하여 결제를 진행합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          <strong class="pointblue"> 결제하기(서울)</strong>
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          고향사랑e음 시스템에서는 계좌이체, 신용카드, 간편결제를 제공합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/DONATION/7.png"
                        alt="
                        	기부지역이 서울 외 다른 지역일 경우 결제 팝업창에서 납부시스템 약관동의화면이 표시되며 전체 동의 후 확인버튼 클릭시
                        	결제 화면이 제공됩니다.
                        	결제화면에서 계좌이체, 신용카드, 간편결제로 결제가 가능합니다.
                        	납부방법 선택 후 기타 입력사항을 입력하여 결제를 진행합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          <strong class="pointblue"> 결제하기(지방)</strong>
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          납부시스템 약관 동의, 확인 클릭
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          고향사랑e음 시스템에서는 계좌이체, 신용카드, 간편결제를 제공합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img src="/images/guide/M_GUIDE2/DONATION/8.png" alt="답례품 목록 사진입니다." class="mobile_img" />
                      <div class="detailsTxt">
                        <p>
                          <strong class="pointblue"> 답례품 선택 하기</strong>
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          기부 하고 받으신 포인트로 기부 하신 지역의 답례품을 선택 하세요
                        </p>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 특정사업 기부하기 안내 -->
                <div class="tab_con" data-tab-con="5" style="display:none">
                  <h3 class="guide-tab-tit">특정사업 기부하기 안내</h3>
                  <div class="pc_img">
                    <div class="details_steps">
                      <div>
                        <img src="/images/icon/cli-icon-bullet.png" alt="총알 아이콘" />
                        <div class="fince_tit">
                          <span>특정사업에 기부(지정기부)란?</span>
                        </div>
                      </div>
                      <img
                        src="/images/guide/PC_GUIDE2/DESIGNATION_DONATION/1.png"
                        alt="
                        	특정사업에 기부(지정기부)는 기부자가 기부시에 특정한 목적의 사업에 기부하는 것입니다.
                        	특정사업은 지방자치단체에서 운영하는 특수한 목적의 사업을 뜻합니다.
                        	특정사업의 분류는 다음과 같이 4가지에 포함되어 있습니다.
                        	1. 사회적 취약계층의 지원 및 청소년의 육성, 보호
                        	2. 지역 주민의 문화, 예술, 보건 등의 증진
                        	3. 시민참여, 자원봉사 등 지역공동체 활성화 지원
                        	4. 그 밖에 주민의 복리 증진에 필요한 사업의 추진

                        	특정사업에 기부하기 순서는 다음과 같습니다.
                        	1. 기부지자체 선택
                        	2. 특정사업 선택하기
                        	3. 기부하기
                        	4. 결제하기
                        	5. 답례품 신청하기

                        	특정사업에 기부 페이지 이동 방법은
                        	메인화면 상단 기부하기 메뉴에 마우스 이동 후 아래에 펼쳐지는 하위 메뉴 중 특정사업에 기부하기 버튼을 클릭
                        	또는
                        	배너 하단에 특정사업에 기부하기 버튼을 클릭하세요.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          <!-- 기부하실 지방자치단체(지역)를 선택 하세요. -->
                          고향사랑e음에 접속 하신 후 특정사업에 기부하기 버튼을 클릭 하세요.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          고향사랑e음 메인에서 특정사업에 기부하기를 선택 하세요.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/DESIGNATION_DONATION/2.png"
                        alt="
                        	특정사업에 기부하기 화면에서 상단 지자체 선택하기를 통해 해당 지자체의 특정사업 목록을 조회할 수 있습니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          특정사업에 기부하기는 지역을 선택 하시면 해당 지자체의 특정사업 목록을 확인 하실 수 있습니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/DESIGNATION_DONATION/3.png"
                        alt="
                        	기부하실 특정사업을 정하여 해당 내용을 클릭하시면 상세 페이지로 이동합니다.

                        	상세페이지에서 기부하기 버튼을 클릭하시면 기부하기 페이지로 이동합니다.
                        "
                        class="pc_img"
                      />
                      <img
                        src="/images/guide/PC_GUIDE2/DESIGNATION_DONATION/sub.png"
                        alt="
                        	상세페이지에 목표 금액, 달성율, 남은 일자, 기부된 금액, 기부 참여수, 상세 내용,
                        	공지사항 등이 표시되니 내용을 확인하세요.
                        "
                        class="pc_img"
                        style="
                          position: absolute;
                          right: 9vw;
                          margin-top: -2vw;
                          filter: drop-shadow(6px 6px 6px #ebebeb);
                          border-radius: 10px;
                          max-width: 333px;
                          width: 23vw;
                        "
                      />
                      <div class="detailsTxt">
                        <p>
                          <strong class="pointblue"> 특정사업 선택 하기</strong>
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          선택 하신 지방자치단체(지역)의 특정사업을 선택 하세요.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          선택 하신 특정사업의 상세 내용을 확인하실 수 있습니다.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 03.</strong>
                          기부하기 버튼을 클릭, 기부하기 페이지로 이동합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/DESIGNATION_DONATION/4.png"
                        alt="
                        	특정사업에 기부하기 상세 화면에서 기부하기 버튼을 통해 기부페이지로 이동했을 경우
                        	기부 지역과 특정사업 선택 항목은 해당 내용으로 자동 설정됩니다.
                        	자치단체에 기부하기 버튼으로 기부페이지로 진입했을 경우 직접 지역과 특정사업을 선택하여 기부하실 수 있습니다.
                        	특정사업을 선택했을 경우 해당 특정사업의 상세페이지로 이동하는 버튼이 특정사업 선택칸 우측에 표시됩니다.
                       	"
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          <strong class="pointblue"> 기부하기</strong>
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          특정사업을 선택 하실 수 있으며 다른 사업으로 변경이 가능합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/DESIGNATION_DONATION/5.png"
                        alt="
                        	기부가능 여부 확인 칸에서 주민등록번호 또는 외국인등록번호(거소신고번호) 뒷자리를 입력 후 주소확인하기 버튼을 클릭해주세요.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          기부 가능여부를 확인해 주세요.
                        </p>
                        <p style="margin-left: 25px">
                          자신의 주민등록 번호를 입력 후 주소확인하기 버튼을 클릭 하시면 기부 가능여부를 확인 하실 수 있습니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/DESIGNATION_DONATION/6.png"
                        alt="
                        	기부액 칸에 기부금액을 입력하시고 답례품 지급 칸에 답례품 제공여부를 선택해주세요.
                        	답례품을 제공 받음을 선택하시면 기부지역의 답례품 포인트가 지급됩니다.

                        	동의항목을 읽고 동의하기 체크를 진행하신 후
                        	기부금 납부하기 버튼을 선택하면 결제 팝업 화면이 표시됩니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 03.</strong>
                          답례품 제공여부를 확인해 주세요.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblack"> "답례품을 제공 받음"</strong>을 선택 하시면 답례품 포인트를 지급받게 됩니다.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 04.</strong>
                          기부금 납부하기 버튼을 클릭 하시면 결제하기 페이지로 이동 합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/DESIGNATION_DONATION/7.png"
                        alt="
                        	기부지역이 서울일 경우 결제 화면에서 계좌이체, 신용카드, 간편결제로 결제가 가능합니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          <strong class="pointblue"> 결제하기(서울)</strong>
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          고향사랑e음 시스템에서는 계좌이체, 신용카드, 간편결제를 제공합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/DESIGNATION_DONATION/8.png"
                        alt="
                        	기부지역이 서울 외 다른 지역일 경우 결제 팝업창에서 납부시스템 약관동의화면이 표시되며 전체 동의 후 확인버튼 클릭시
                        	결제 화면이 제공됩니다.
                        	결제화면에서 계좌이체, 신용카드, 간편결제로 결제가 가능합니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          <strong class="pointblue"> 결제하기(지방)</strong>
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          납부시스템 약관 동의, 확인 클릭
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          고향사랑e음 시스템에서는 계좌이체, 신용카드, 간편결제를 제공합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img src="/images/guide/PC_GUIDE2/DESIGNATION_DONATION/9.png" alt="답례품 목록 샘플 이미지 입니다." class="pc_img" />
                      <div class="detailsTxt">
                        <p>
                          <strong class="pointblue"> 답례품 선택 하기</strong>
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          기부 하고 받으신 포인트로 기부하신 지역의 답례품을 선택 하세요.
                        </p>
                      </div>
                    </div>
                  </div>
                  <div class="mobile_img">
                    <div class="fince_header">
                      <div>
                        <img src="/images/icon/cli-icon-bullet.png" alt="총알 아이콘" />
                        <div class="fince_tit">
                          <span>특정사업에 기부(지정기부)란?</span>
                        </div>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/DESIGNATION_DONATION/1.png"
                        alt="
                        	특정사업에 기부(지정기부)는 기부자가 기부시에 특정한 목적의 사업에 기부하는 것입니다.
                        	특정사업은 지방자치단체에서 운영하는 특수한 목적의 사업을 뜻합니다.
                        	특정사업의 분류는 다음과 같이 4가지에 포함되어 있습니다.
                        	1. 사회적 취약계층의 지원 및 청소년의 육성, 보호
                        	2. 지역 주민의 문화, 예술, 보건 등의 증진
                        	3. 시민참여, 자원봉사 등 지역공동체 활성화 지원
                        	4. 그 밖에 주민의 복리 증진에 필요한 사업의 추진

                        	특정사업에 기부하기 순서는 다음과 같습니다.
                        	1. 기부지자체 선택
                        	2. 특정사업 선택하기
                        	3. 기부하기
                        	4. 결제하기
                        	5. 답례품 신청하기

                        	우측 상단 전체메뉴 클릭 후 기부하기를 클릭하면 표시되는 하단 메뉴에서
                        	특정사업에 기부하기 항목을 클릭하시면 특정사업에 기부하기 목록페이지로 이동합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>기부하실 지방자치단체(지역)를 선택 하세요.</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          고향사랑e음 메인에서 특정사업에 기부하기를 선택 하세요.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/DESIGNATION_DONATION/2.png"
                        alt="
                        	모바일 화면 하단에 지자체 검색 버튼을 클릭하여 지자체 선택 화면을 연 후
                        	시도, 시군구를 선택하고 선택완료를 클릭하시면 선택한 지자체의 특정 사업 목록을 확인할 수 있습니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          특정사업에 기부하기는 지역을 선택 하시면 해당 지자체의 특정사업 목록을 확인 하실 수 있습니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/DESIGNATION_DONATION/3.png"
                        alt="
                        	기부하실 특정사업을 정하여 해당 내용을 클릭하시면 상세 페이지로 이동합니다.

                        	상세페이지에서 기부하기 버튼을 클릭하시면 기부하기 페이지로 이동합니다.

                        	상세페이지에 목표 금액, 달성율, 남은 일자, 기부된 금액, 기부 참여수, 상세 내용,
                        	공지사항 등이 표시되니 내용을 확인하세요.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          <strong class="pointblue"> 특정사업 선택 하기</strong>
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          선택 하신 지방자치단체(지역)의 특정사업을 선택 하세요.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          선택 하신 특정사업의 상세 내용을 확인하실 수 있습니다.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 03.</strong>
                          기부하기 버튼을 클릭, 기부하기 페이지로 이동합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/DESIGNATION_DONATION/4.png"
                        alt="
                        	특정사업에 기부하기 상세 화면에서 기부하기 버튼을 통해 기부페이지로 이동했을 경우
                        	기부 지역과 특정사업 선택 항목은 해당 내용으로 자동 설정됩니다.
                        	자치단체에 기부하기 버튼으로 기부페이지로 진입했을 경우 직접 지역과 특정사업을 선택하여 기부하실 수 있습니다.
                        	특정사업 항목이 선택되어 있을 경우 해당 특정사업의 상세페이지로 이동하는 버튼이 특정사업 선택칸 우측에 표시됩니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          <strong class="pointblue"> 기부하기</strong>
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          특정사업을 선택 하실 수 있으며 다른 사업으로 변경이 가능합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/DESIGNATION_DONATION/5.png"
                        alt="
                        	기부가능 여부 확인 칸에서 주민등록번호 또는 외국인등록번호(거소신고번호) 뒷자리만 입력 후 주소확인하기 버튼을 클릭해주세요.
                        	이름 및 생년월일은 가입정보로 설정됩니다. 카카오톡 인증으로 회원가입을 했을 경우에는 최초 기부시에만
                        	생년월일 변경이 가능합니다.

                        	기부가 가능하면 주민등록번호 또는 외국인등록번호(거소신고번호) 입력칸 하단에 기부가능 메시지가 표시되며
                        	기부 불가능시 기부불가능한 사유가 팝업에 표시됩니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          기부 가능여부를 확인해 주세요
                        </p>
                        <p>자신의 주민등록 번호를 입력 후 주소확인하기 버튼을 클릭 하시면 기부 가능여부를 확인 하실 수 있습니다.</p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/DESIGNATION_DONATION/6.png"
                        alt="
                        	기부액 칸에 기부금액을 입력하시고 답례품 지급 칸에 답례품 제공여부를 선택해주세요.
                        	답례품을 제공 받음을 선택하시면 기부지역의 답례품 포인트가 지급됩니다.

                        	동의항목을 읽고 전체 동의하신 후

                        	기부금 납부하기 버튼을 선택하면 결제 팝업 화면이 표시됩니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 03.</strong>
                          답례품 제공여부를 확인해 주세요
                        </p>
                        <p><strong class="pointblack"> "답례품을 제공 받음"</strong>을 선택 하시면 답례품 포인트를 지급받게 됩니다.</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 04.</strong>
                          기부금 납부하기 버튼을 클릭 하시면 결제하기 페이지로 이동 합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/DESIGNATION_DONATION/7.png"
                        alt="
                        	기부지역이 서울일 경우 결제화면에서 은행납부, 신용카드, 간편결제로 결제가능합니다.
                        	납부 방법을 선택하고 동의 및 기타 입력항목을 입력하여 결제를 진행해주세요.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          <strong class="pointblue"> 결제하기(서울)</strong>
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          고향사랑e음 시스템에서는 계좌이체, 신용카드, 간편결제를 제공합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/DESIGNATION_DONATION/8.png"
                        alt="
                        	기부지역이 서울 외 다른 지역일 경우 결제 팝업에서 납부시스템 약관동의 화면이 먼저 표시되며 전체 동의 및 확인 버튼 클릭 후
                        	결제화면이 표시됩니다.
                        	결제화면에서 계좌이체, 신용카드, 간편결제(카카오페이,네이버페이)로 결제가 가능합니다.
                        	납부방법을 선택하고 기타 입력항목을 입력하여 결제를 진행해주세요.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          <strong class="pointblue"> 결제하기(지방)</strong>
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          납부시스템 약관 동의, 확인 클릭
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          고향사랑e음 시스템에서는 계좌이체, 신용카드, 간편결제를 지공합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/DESIGNATION_DONATION/9.png"
                        alt="
                        	답례품 목록 사진입니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          <strong class="pointblue"> 답례품 선택하기</strong>
                        </p>
                        <p>기부 하고 받으신 포인트로 기부하신 지역의 답례품을 선택 하세요.</p>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 위택스 전자결제 안내 -->
                <div class="tab_con" data-tab-con="6" style="display:none">
                  <h3 class="guide-tab-tit">전자결제 (16개 시·도-서울시 제외)</h3>
                  <div class="pc_img">
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/PAYMENT_LOCAL/1.png"
                        alt="
                        	기부 후 바로 결제하는 경우
                        	결제 팝업 페이지에서 납부시스템 약관동의 내용 확인 후
                        	약관 동의를 진행합니다. 전체 동의 후 확인 버튼을 클릭합니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          기부정보 처리를 위해 잠시
                          <strong class="pointblue">기다려주세요. </strong>
                        </p>
                        <p>납부시스텝 팝업창이 뜨면 약관 내용을 확인합니다.</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          약관동의 체크 박스를 클릭해주신 후
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          확인버튼을 클릭해주세요.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/PAYMENT_LOCAL/2.png"
                        alt="
                        	납부 방법에서 계좌이체 또는 신용카드를 선택할 수 있으며
                        	결제 방법을 선택 후 결제 방법 버튼 아래에 나오는 항목을 입력하고 다음 버튼을 눌러주세요.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 03.</strong>
                          <strong class="pointblue"> 납부방법</strong>에서 계좌이체와 신용카드 중 하나를 선택 후,
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 04.</strong>
                          결제 정보를 입력합니다.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 05.</strong>
                          다음 버튼을 클릭합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/PAYMENT_LOCAL/3.png"
                        alt="
                        	납부완료 화면이 나오면 확인 버튼을 눌러주세요.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p>납부가 완료되었습니다. 내용확인 후</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 06.</strong>
                          확인 버튼을 눌러주세요.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/PAYMENT_LOCAL/4.png"
                        alt="
                        	기부 감사 팝업 페이지가 열리며 팝업에서 마이페이지 바로가기 버튼으로
                        	기부내역 확인 및 답례품 주문 진행이 가능합니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p>납부창 팝업창이 닫히면 고향사랑 e음 사이트에서 완료 안내창에 열립니다.</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 07.</strong>
                          마이 페이지 바로가기 버튼을 클릭하여 기부내역을 확인합니다.
                        </p>
                        <p class="pointRed martop10">
                          ※ 전자납부번호 및 납부결과 확인은 "마이페이지 > 기부내역 현황"에서 다시 확인 하실 수 있습니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/PAYMENT_LOCAL/5.png"
                        alt="
                        	결제 실패 또는 오류 등으로 결제 중단 후 다시 결제하는 경우
                        	로그인 후 상단에 마이페이지 버튼을 눌러서 마이페이지 화면으로 이동합니다.
                        	마이페이지 화면에서 기부내역 현황 선택 후 결제가 중단된 내역에서
                        	다시 기부하기 버튼을 클릭하면 결제를 위한 팝업 화면이 제공됩니다.
                        	그 이후는 팝업에서 제공되는 결제 절차를 진행합니다.
                        	기부 시 바로 결제하는 경우와 동일합니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p style="font-weight: bold">
                          기부금 결제가 실패했거나 강제 중단한 경우는 "다시 기부하기" 버튼을 클릭하여 다시 기부금 결제를 진행할 수 있습니다.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          마이 페이지 > 기부내역 현황 페이지로 이동합니다.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          기부금액 및 기부지역을 다시 확인하고 "다시 기부하기" 버튼을 클릭합니다.
                        </p>
                        <p>이후 진행 절차는 "기부 후 바로 결제한 경우"와 동일하게 진행됩니다.</p>
                      </div>
                    </div>
                    <!-- <div class="details_steps">
                                    <img src="/images/do_info/tab_wetax5.png" alt="" class="pc_img">
                                    <div class="detailsTxt">
                                        <p>기부는 하셨지만 결제는 안한 경우 </p>
                                        <p><strong class="pointblue"> 01.</strong> '고향사랑 e음' 사이트에서 '기부마이페이지 > 기부내역 현황'
                                            에서
                                        </p>
                                        <p><strong class="pointblue"> 02.</strong> 전자납부번호 를 마우스로 긁어서 복사해주세요.</p>
                                        <p><strong class="pointblue"> 03.</strong> '위택스'사이트의 메인 화면에 빠른납부 칸에 복사한 전자납부번호를
                                            붙여
                                            넣기를 합니다. ( 전자납부번호를 직접입력하셔도 됩니다 )</p>
                                        <p>이후 결제방법을 선택후 결제를 진행해주시면 됩니다.</p>
                                    </div>
                                </div> -->
                  </div>
                  <div class="mobile_img">
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/PAYMENT_LOCAL/1.png"
                        alt="
                        	기부 시 바로 결제하는 경우 결제 팝업화면에서 약관 동의 내용 확인 및 전체동의하고
                        	확인 버튼을 누르면 결제화면이 제공됩니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          기부정보 처리를 위해 잠시
                          <strong class="pointblue">기다려주세요. </strong>
                        </p>
                        <p>납부시스템 팝업창이 뜨면 약관을 확인 합니다.</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          약관동의 체크 박스를 선택해주신 후
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          확인버튼을 선택해주세요.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/PAYMENT_LOCAL/2.png"
                        alt="
                        	납부 방법은 계좌이체, 신용카드, 간편결제가 있으며
                        	납부 내용 확인 후 납부방법을 선택하시고 기타 입력사항을 입력 후 다음 버튼을 눌러 진행합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 03.</strong>
                          <strong class="pointblue">납부방법</strong>에서 계좌이체와 신용카드 중 하나를 선택 후,
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 04.</strong>
                          결제 정보를 입력합니다.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 05.</strong>
                          다음 버튼을 클릭합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/PAYMENT_LOCAL/3.png"
                        alt="
                        	납부 진행 팝업에서 확인버튼을 누르면 납부가 완료되며
                        	납부 완료화면에서 납부 내역 확인 후 확인버튼을 누르면 기부감사 화면으로 이동합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>금액을 다시 한번 확인 후</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 06.</strong>
                          확인 버튼을 선택합니다.
                        </p>
                        <p>납부 완료 팝업이 나오면, 내용 확인 후</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 07.</strong>
                          확인 버튼을 선택해주세요.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/PAYMENT_LOCAL/4.png"
                        alt="
                        	기부감사 화면에서 마이페이지 바로가기 버튼으로
                        	기부내역 확인 및 답례품 주문 진행이 가능합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>납부창 팝업창이 닫히면 고향사랑 e음 사이트에서 완료 안내창이 열립니다.</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 08.</strong>
                          마이 페이지 바로가기 버튼을 클릭하여 기부내역을 확인합니다.
                        </p>
                        <p class="pointRed martop10">
                          ※ 전자납부번호 및 납부결과 확인은 "마이페이지 > 기부내역 현황"에서 다시 확인 하실 수 있습니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/PAYMENT_LOCAL/5.png"
                        alt="
                        	인터넷 접속불가 등으로 납부 완료되기 전 중단되었을 경우 다시 납부 진행을 원하시면
                        	로그인 후 우측 상단 전체메뉴 선택 후 마이페이지 하위 메뉴의 기부포인트 현황 버튼을 클릭합니다.

                        	기부 포인트 현황 화면이 열리면 기부 내역 중에서 중단된 기부 목록을 확인하여 다시 기부하기 버튼을
                        	선택하면 결제화면 팝업이 제공됩니다. 이후 단계는 기부화면에서 결제하는 방법과 동일합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p style="font-weight: bold">
                          기부금 결제가 실패했거나 강제 중단한 경우는 "다시 기부하기" 버튼을 클릭하여 다시 기부금 결제를 진행할 수 있습니다.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          마이 페이지 > 기부내역 현황 페이지로 이동합니다.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          기부금액 및 기부지역을 다시 확인하고 "다시 기부하기" 버튼을 클릭합니다.
                        </p>
                        <p>이후 진행 절차는 "기부 후 바로 결제한 경우"와 동일하게 진행됩니다.</p>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 전자결제(서울시) 안내 -->
                <div class="tab_con" data-tab-con="7" style="display:none">
                  <h3 class="guide-tab-tit">전자결제(서울시) 안내</h3>
                  <div class="pc_img">
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/PAYMENT_SEOUL/1.png"
                        alt="
                        	기부 후 바로 결제 진행하는 경우
                        	납부방법을 선택 후 기타 입력사항을 입력하고 결제하기 버튼을 눌러 결제를 진행합니다.
                        	납부방법은 은행 납부, 카드 납부, 간편 결제를 제공합니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          서울지역에 기부는
                          <strong class="pointblue">이택스</strong> 납부화면 창이 열립니다.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          3가지 결제수단 중 하나를 선택하여
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          결제수단별로 결제를 진행합니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/PAYMENT_SEOUL/2.png"
                        alt="
                        	납부가 완료되면 납부 내역이 표시됩니다.
                        	닫기 버튼을 누르면 기부감사 화면으로 이동합니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p>납부가 완료되었습니다. 납부확인증을 확인해주세요.</p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/PAYMENT_SEOUL/3.png"
                        alt="
                        	기부감사 화면에서 마이페이지 바로가기를 누르면 기부 내역을 진입할 수 있는 마이페이지 화면이 표시됩니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p>고향사랑 e음 사이트에서 완료 안내창에 열리면,</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 03.</strong>
                          마이 페이지 바로가기 버튼을 클릭하여 기부내역을 확인합니다.
                        </p>
                        <p class="pointRed martop10">
                          ※ 전자납부번호 및 납부결과 확인은 "마이페이지 > 기부내역 현황"에서 다시 확인 하실 수 있습니다.
                        </p>
                      </div>
                    </div>
                  </div>
                  <div class="mobile_img">
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/PAYMENT_SEOUL/1.png"
                        alt="
                        	기부 후 바로 결제하는 경우 납부할 내역이 표시되며 확인 후 납부 버튼을 선택합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          서울지역에 기부는
                          <strong class="pointblue">이택스</strong> 납부화면 창이 열립니다.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          해당 기부내역을 선택하고
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          납부내용을 확인 후 맨 아래 '납부'버튼을 선택합니다.
                        </p>
                      </div>
                    </div>

                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/PAYMENT_SEOUL/2.png"
                        alt="
                        	납부 방법을 선택 후 동의 항목 및 기타 입력사항을 입력 후 납부 버튼을 선택합니다.
                        	납부 방법으로는 은행 납부, 신용카드, 간편 결제 방법이 있습니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 03.</strong>
                          3가지 결제수단 중 하나를 선택하여
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 04.</strong>
                          결제 정보를 입력한 후
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 05.</strong>
                          '납부'버튼을 선택하여 결제를 진행합니다.
                        </p>
                      </div>
                    </div>

                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/PAYMENT_SEOUL/3.png"
                        alt="
                        	납부 주의사항 화면이 뜨면 내용 확인 후 납부동의 체크 및 세입금 납부 버튼을 선택합니다.

                        	이후 납부 완료 화면이 표시되며 납부 금액 확인 후 확인 버튼을 선택합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>결제수단에 따라 결제를 하신 후, 세입금 납부 필독사항에서 내용을 확인해주세요.</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 06.</strong>
                          납부동의를 선택 후,
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 07.</strong>
                          세입금 납부(취소불가) 버튼을 선택해주세요.
                        </p>
                        <p>'납부가 정상적으로 완료되었습니다.' 메시지 확인 후 맨 아래의</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 08.</strong>
                          확인을 선택하시거나 해당 창을 닫아 "고향사랑 e음" 사이트로 돌아갑니다.
                        </p>
                        <p class="pointRed martop10">※ 결제수단에 따라 결제 과정 및 완료 화면은 예시 이미지와 다를 수 있습니다.</p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/PAYMENT_SEOUL/4.png"
                        alt="
                        	기부감사 화면이 표시되며 마이페이지 바로가기 버튼을 누르면 기부내역화면에 진입할 수 있는
                        	마이페이지 화면이 열립니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>고향사랑 e음 사이트에서 완료 안내창이 열리면,</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 06.</strong>
                          마이 페이지 바로가기 버튼을 클릭하여 기부내역을 확인합니다.
                        </p>
                        <p class="pointRed martop10">
                          ※ 전자납부번호 및 납부결과 확인은 "마이페이지 > 기부내역 현황"에서 다시 확인 하실 수 있습니다.
                        </p>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 답례품 구매 -->
                <div class="tab_con" data-tab-con="8" style="display:none">
                  <h3 class="guide-tab-tit">답례품 선택</h3>
                  <div class="pc_img">
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/GIFT/1.png"
                        alt="
                        	답례품 신청하기 순서는 다음과 같습니다.
                        	1. 마이페이지 클릭
                        	2. 기부포인트 현황
                        	3. 답례품 신청하기

                        	로그인 후 상단 마이페이지 버튼을 클릭하면 마이페이지 화면으로 이동합니다.
                        	마이페이지 화면에서 기부포인트 현황 버튼을 클릭하면 기부포인트 현황 화면으로 이동합니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p>
                          고향사랑e음에 접속 하신 후
                          <strong class="pointblack">상단 마이페이지</strong>
                          버튼을 클릭 하세요
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          마이페이지에서 기부하신 지자체 목록을 확인 하실 수 있습니다.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          기부포인트 현황을 클릭 하시면 지자체의 포인트를 확인 하실 수 있습니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/GIFT/2.png"
                        alt="
                        	기부포인트 현황 화면에서 기부한 내역을 확인하시고
                        	주문할 지방자치단체 기부 내역의 오른쪽 끝에 있는 답례품 몰 가기 버튼을 클릭하시면
                        	선택한 지방자치단체 답례품 상점 화면으로 이동합니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p>정상 기부내역을 확인 하신 후에 포인트를 확인 하세요</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 03.</strong>
                          기부하신 지자체의 포인트를 클릭 하시거나 답례품 바로가기 버튼을 클릭 하시면
                        </p>
                        <p style="margin-left: 25px">기부하신 지자체의 답례품을 확인 하실 수 있습니다.</p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/PC_GUIDE2/GIFT/3.png"
                        alt="
                        	지방자치단체 답례품 상점 화면에서 주문할 답례품을 선택하시면
                        	해당 답례품 상세화면으로 이동합니다.
                        	기부시 받은 포인트와 주문할 답례품의 포인트를 확인하시고 옵션 및 수량을
                        	선택하신 후 바로선택 버튼을 누르면 주문 페이지로 이동합니다.
                        "
                        class="pc_img"
                      />
                      <div class="detailsTxt">
                        <p>기부하신 지역의 답례품을 확인 하시고 마음에 드시는 상품을 선택 하세요</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 04.</strong>
                          기부하신 지역의 포인트를 확인 하시고 구매 하세요
                        </p>
                        <p style="margin-left: 25px">고향사랑e음에서는 답례품 선택 시 갖고 계신 한도 내에서 답례품 선택이 가능 합니다.</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 05.</strong>
                          바로선택 버튼을 클릭 하시면 답례품 신청이 진행 됩니다.
                        </p>
                      </div>
                    </div>
                  </div>
                  <div class="mobile_img">
                    <div class="detailsTxt" style="padding-top: 0px; padding-bottom: 20px">
                      <p style="text-align: center">
                        <strong class="pointblue">답례품신청하기 순서는 다음과 같습니다.</strong>
                      </p>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/GIFT/1.png"
                        alt="
                        	답례품 신청하기 순서는 다음과 같습니다.
                        	1. 마이페이지 클릭
                        	2. 기부포인트 현황
                        	3. 답례품 신청하기
                        "
                        class="mobile_img"
                        style="width: auto"
                      />
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/GIFT/2.png"
                        alt="
                        	오른쪽 상단 전체메뉴 버튼을 선택하면 메뉴 화면이 열립니다.
                        	마이페이지 메뉴를 눌러서 하위메뉴를 열고 기부 포인트 현황 버튼을 누르면
                        	기부 포인트 현황 화면으로 이동합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>고향사랑e음에 접속 하신 후 상단 마이페이지 버튼을 클릭 하세요</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 01.</strong>
                          마이페이지에서 기부하신 지자체 목록을 확인 하실 수 있습니다.
                        </p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 02.</strong>
                          기부포인트 현황을 클릭 하시면 지자체의 포인트를 확인 하실 수 있습니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/GIFT/3.png"
                        alt="
                        	기부 포인트 현황 화면에서 답례품을 주문하려는 지자체의 잔여포인트를 확인 하신 후
                        	답례품 몰 가기 버튼을 클릭하면 해당 지자체의 답례품 몰로 이동합니다.

                        	답례품 몰에서 주문하고자 하시는 답례품을 선택하시면 답례품 상세페이지로 이동합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>정상 기부내역을 확인 하신 후에 포인트를 확인 하세요</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 03.</strong>
                          기부하신 지자체의 포인트를 클릭 하시거나 답례품 바로가기 버튼을 클릭 하시면 기부하신 지자체의 답례품을 확인 하실 수
                          있습니다.
                        </p>
                      </div>
                    </div>
                    <div class="details_steps">
                      <img
                        src="/images/guide/M_GUIDE2/GIFT/4.png"
                        alt="
                        	답례품 상세화면에서 지자체와 포인트를 확인하고 옵션 및 수량 등을 입력하신 후
                        	바로선택 버튼을 누르면 주문정보 입력 화면으로 이동합니다.
                        	장바구니에 넣은 후 화면 상단의 장바구니 버튼을 클릭하여 장바구니 화면 이동 후
                        	주문 진행도 가능합니다.
                        "
                        class="mobile_img"
                      />
                      <div class="detailsTxt">
                        <p>기부하신 지역의 답례품을 확인 하시고 마음에 드시는 상품을 선택하세요</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 04.</strong>
                          기부하신 지역의 포인트를 확인하시고 구매하세요
                        </p>
                        <p>고향사랑e음에서는 답례품 선택 시 갖고 계신 한도 내에서 답례품 선택이 가능합니다.</p>
                        <p style="margin-left: 25px">
                          <strong class="pointblue" style="margin-left: -25px"> 05.</strong>
                          바로선택 버튼을 클릭 하시면 답례품 신청이 진행 됩니다.
                        </p>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 버튼그룹 -->
                <div class="btn-box">
                  <button type="button" class="blueBtn cancellation" onclick="location.href='#tabMenu'">
                    맨위로
                    <span>
                      <img src="/images/icon/cli-icon_btn-hover-arrow.png" alt="" />
                    </span>
                  </button>
                </div>
        </div>
    </div>
</section>
</template>

<style>
@import '/css/donation_guge.css';
@import '/css/research-box.css';
</style>

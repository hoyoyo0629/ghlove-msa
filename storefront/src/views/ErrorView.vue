<script setup>
// AS-IS error/404.html을 그대로 이식한 페이지 오류 화면. 사이트 헤더/푸터 없이 단독 렌더된다
// (라우터 meta.bare). 존재하지 않는 경로(잘못된 배너 링크·삭제된 게시물 등)로 진입하면 표시된다.
</script>

<template>
  <div class="error404">
    <div class="center">
      <div class="page-title-box">
        <h2 class="page-title-txt">페이지가 존재하지 않습니다.</h2>
      </div>
      <div id="sub_contents" class="center">
        <div class="page-title-box error_inner">
          <p>죄송합니다만 지정하신페이지는 찾을수 없습니다.<br />이미 삭제되었던지,주소가 잘못되었을 가능성이 있습니다. </p>
          <img src="/images/error404.png" alt="페이지에러" />
        </div>
      </div>
      <!--// sub_contents  E-->
    </div>
  </div>
</template>

<style scoped>
/* AS-IS error/404.html의 인라인 스타일 원문 그대로 */
.error404 * {
  margin: 0;
  padding: 0;
  word-break: keep-all;
}
.error404 {
  background-color: #f5f5f5;
  min-height: 100vh;
  text-align: center;
  line-height: 1em;
  font-family: 'Noto Sans KR', sans-serif;
}
.error404 .center {
  max-width: 1280px;
  margin: 0 auto;
  min-width: 360px;
}
.error404 .page-title-box {
  padding-top: 80px;
  text-align: center;
  position: relative;
  margin-bottom: 60px;
}
.error404 .page-title-box .page-title-txt {
  font-size: 50px;
  color: #00215a;
  margin-top: 16px;
  margin-bottom: 32px;
  line-height: 1.1em;
}
.error404 img {
  max-width: 100%;
}
.error404 #sub_contents {
  max-width: 700px;
}
.error404 #sub_contents .error_inner {
  background-color: #fff;
  box-shadow: 0px 3px 6px rgba(0, 0, 0, 0.16);
  padding: 60px;
}
.error404 #sub_contents .error_inner p {
  margin-bottom: 60px;
}
.error404 .page-title-box p {
  color: #878787;
  font-size: 16px;
  margin-bottom: 32px;
  line-height: 1.5em;
}
@media screen and (max-width: 768px) {
  .error404 {
    background-color: #fff;
  }
  .error404 .page-title-box {
    margin-bottom: 16px;
    padding-top: 130px;
  }
  .error404 .page-title-box .page-title-txt {
    margin: 0 auto;
    width: 80%;
    font-size: 40px;
  }
  .error404 #sub_contents .error_inner {
    box-shadow: none;
    padding: 0 20px;
  }
}
</style>

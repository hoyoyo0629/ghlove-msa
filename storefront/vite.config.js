import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

// 각 백엔드 서비스는 로컬에서 Kong 없이 자기 포트로 직접 뜬다(SETUP.md 기준). Kong의
// strip_path 라우팅(/member, /donation, ...)과 동일한 접두사 규칙을 dev 서버 프록시로도
// 재현해서, 운영에서 Kong 뒤에 이 빌드가 놓이더라도 프론트 코드가 호출하는 상대경로가
// 그대로 통하게 한다.
const services = {
  member: 8081,
  donation: 8082,
  point: 8083,
  gift: 8084,
  order: 8085,
  admin: 8086,
}

export default defineConfig({
  // 이 프로젝트의 이미지는 전부 public/images에 있고 절대경로(/images/...)로만 참조한다
  // (빌드시 모듈로 임포트할 상대경로 자산이 없음) - Vue SFC의 기본 asset-url 변환을 꺼서
  // <img src="/images/..."> 같은 정적 속성이 엉뚱하게 모듈 임포트로 바뀌지 않게 한다.
  plugins: [vue({ template: { transformAssetUrls: false } })],
  server: {
    // 문자열 키는 접두사(startsWith) 매칭이라 "/order"가 SPA 라우트 "/orders"까지
    // 삼켜버린다(실제로 재현됨: /orders가 8085로 잘못 프록시되어 백엔드 404를 반환했음) -
    // 반드시 "/order" 뒤가 "/"거나 끝나는 경우만 매치하도록 정규식 키를 쓴다.
    proxy: Object.fromEntries(
      Object.entries(services).map(([name, port]) => [
        `^/${name}(/|$)`,
        {
          target: `http://localhost:${port}`,
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp(`^/${name}`), ''),
        },
      ]),
    ),
  },
})

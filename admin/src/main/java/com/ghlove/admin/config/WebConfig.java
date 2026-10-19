package com.ghlove.admin.config;

import com.ghlove.admin.web.AdminAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Value("${admin.upload.dir}")
    private String uploadDir;

    private final AdminAuthInterceptor adminAuthInterceptor;

    /**
     * 업로드 파일을 <b>두 경로로 같이</b> 서비스한다.
     * <ul>
     *   <li>{@code /uploads/**} - admin 콘솔이 자기 화면에서 쓰는 경로(기존 데이터도 이 형태)</li>
     *   <li>{@code /admin/uploads/**} - <b>storefront가 쓰는 경로.</b> 스토어프론트는 서비스
     *       접두사 규칙(Kong strip_path = Vite 프록시)으로 호출하므로
     *       {@code /admin/uploads/...}가 8086의 {@code /uploads/...}로 들어온다
     *       (답례품이 {@code /gift/uploads/...}를 쓰는 것과 같은 규칙)</li>
     * </ul>
     * 에디터로 넣은 이미지는 <b>본문 HTML에 그대로 박혀 스토어프론트에서 렌더</b>되므로,
     * 업로드 시점에 접두사까지 포함한 경로를 심어야 양쪽에서 다 보인다(AS-IS는 이 자리에
     * 절대 CDN URL을 심어 같은 문제를 피했다). 2026-10-06: 팝업 본문 이미지가 5173에서
     * 404나던 원인이다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + uploadDir.replace("\\", "/") + "/";
        registry.addResourceHandler("/uploads/**", "/admin/uploads/**")
                .addResourceLocations(location);
    }

    /**
     * 로그인 게이트 - <b>기본 전부 보호(/**), 공개 경로만 제외</b>한다.
     *
     * <p>2026-10-13 이전에는 반대였다(보호할 경로를 하나씩 적는 화이트리스트). 그러면
     * <b>새 운영자 화면을 추가하면서 목록에 안 넣으면 그 화면이 로그인 없이 열린다</b> -
     * 실제로 13개 화면이 그렇게 열려 있었고({@code /point-history} 는 표 69행이 그대로 떴다),
     * 세션 없이 들어온 요청이 컨트롤러까지 가서 {@code manager == null} NPE 500을 내기도 했다.
     * 뒤집으면 실패 방향이 "빠뜨리면 열린다"에서 "빠뜨리면 막힌다"로 바뀐다 - 조용한
     * 정보노출보다 눈에 띄는 차단이 낫다.
     *
     * <p>아래 제외 목록은 2026-10-13에 <b>347개 경로를 세션 없이 전부 호출해 실측</b>한
     * 결과다(공개로 응답하던 것만 추렸다). 공개 화면을 새로 만들면 여기에 추가해야 한다.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // --- 정적 자원 (인터셉터는 리소스 핸들러 요청에도 걸린다) ---
                        "/content/**", "/css/**", "/images/**", "/fonts/**", "/favicon.ico",
                        // 업로드 파일 - /uploads 는 admin 자기 화면용, /admin/uploads 는 storefront 용
                        "/uploads/**", "/admin/uploads/**",
                        // 스프링 기본 오류 페이지. 막으면 오류가 로그인 리다이렉트로 둔갑한다
                        "/error",

                        // --- 로그인·진입 경로 ---
                        "/admin/login", "/admin/login/**", "/admin/logout", "/admin/access-denied",
                        // 지자체 담당자 인증 로그인
                        "/auth/cert-login", "/auth/cert-login/**", "/auth-swap",
                        // 로그인 화면에서 관리자가 아닌 회원이 넘어오는 권한 요청(AS-IS isManagerPage 예외
                        // manager-request/form·pkiForm·pkiFormFin·create·code-child/list, designated-donation/partList,
                        // 행정복지센터 목록)과 계정 잠김 안내(AS-IS /opmanager/login-lock).
                        // 같은 /admin/manager-requests 라도 승인 목록·승인/거절(confirm)은 보호된다(AS-IS도 로그인 필요).
                        "/admin/manager-requests/form", "/admin/manager-requests/pkiForm",
                        "/admin/manager-requests/pkiFormFin", "/admin/manager-requests/create",
                        "/admin/manager-requests/code-child/list", "/admin/manager-requests/designated-part-list",
                        "/admin/manager-requests/wlfr-cntr-mng-list", "/admin/login-lock",
                        // 로그인 전 인증서 등록/삭제(AS-IS /opmanager/user/pki/form·form/valid·update·delete, isManagerPage 예외).
                        "/admin/user/pki/**",
                        // 관리자 공지 팝업 목록 - 로그인 화면에서도 부른다(AS-IS /common/manager/notice/list).
                        "/common/manager/notice/list",
                        // 보안 로그아웃 콜백
                        "/op_security_logout",

                        // --- 고객센터 공개 콘텐츠(스토어프론트가 읽는다) ---
                        "/notices", "/notices/**", "/data-board", "/data-board/**",
                        "/events", "/events/**", "/faqs", "/faqs/**", "/qna", "/qna/**",
                        "/policies/**", "/surveys/**",

                        // --- AS-IS OpmanagerHandlerInterceptor.isManagerPage 예외 ---
                        // 실명인증 결과수신(사이렌24 → ok_url) - 세션 없이 들어오는 벤더 콜백.
                        "/offgive/sci-result",
                        // 시도→시군구 셀렉트 조회(AS-IS /give/give-state/options-by-locgovCode). 기탁서 등록(15102) 등
                        // 기부금관리(14101) 메뉴권한이 없는 롤도 부르는 공통 조회라, 권한검사를 태우면 오프라인 담당자에게
                        // 권한없음 화면(HTML)이 돌아가 시군구가 전부 undefined가 된다.
                        "/give-state/options-by-locgovCode",
                        // 카테고리 select 옵션 조각(AS-IS 프론트 /categories/options·options-by-groupid) - 답례품관리 목록 등의 그룹→1~4차 연동 select가
                        // 부른다. 프론트 경로라 관리자 메뉴 권한과 무관하게 열려 있다(16101 카테고리관리 권한이 없는 지자체 담당자도 쓴다).
                        "/categories/options", "/categories/options-by-groupid",
                        // 개인정보 암호화 솔루션 설치 확인 페이지(AS-IS /offgive/pdbtest).
                        "/offgive/pdbtest",

                        // --- 공개 API ---
                        "/api/**", "/common/message", "/common/messages");
    }
}

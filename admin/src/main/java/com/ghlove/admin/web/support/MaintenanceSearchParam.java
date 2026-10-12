package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 운영관리 SR게시판 검색조건 (메뉴 5113) - AS-IS {@code MaintenanceDto} 중 검색에 쓰는 부분.
 *
 * <p>AS-IS 컨트롤러는 GET·POST 양쪽에서 등록일자를 <b>오늘로 채운다</b>. 완료구분은
 * 체크박스 다중선택({@code shArrProcessState})이고 화면 진입 시 '전체'가 눌린 상태로 시작한다.
 */
@Getter
@Setter
public class MaintenanceSearchParam {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 검색구분 - ALL(기본)/CSRNO/SUBJECT/CN. AS-IS의 '작성자(USERNAME)'는 주석처리다. */
    private String where = "ALL";
    private String query;
    /** 완료구분 다중선택. 'ALL'이 섞여 오면 조건을 걸지 않는다(= 전체). */
    private List<String> shArrProcessState = new ArrayList<>();
    private String startDt;
    private String endDt;
    private int itemsPerPage = 10;
    private int page = 1;

    public void applyDefaults() {
        String today = LocalDate.now().format(YMD);
        if (startDt == null || startDt.isBlank()) {
            startDt = today;
        }
        if (endDt == null || endDt.isBlank()) {
            endDt = today;
        }
    }

    /** 화면이 보낸 완료구분 중 'ALL'을 빼고 실제 코드만 추린다 - 비면 조건 없음(전체). */
    public List<String> selectedProcessStates() {
        if (shArrProcessState == null) {
            return List.of();
        }
        if (shArrProcessState.contains("ALL")) {
            return List.of();
        }
        return shArrProcessState.stream().filter(s -> s != null && !s.isBlank()).toList();
    }

    public boolean isChecked(String code) {
        return shArrProcessState != null && shArrProcessState.contains(code);
    }

    /** AS-IS는 진입 시 아무것도 안 눌려 있으면 '전체'를 눌러 준다. */
    public boolean isAllChecked() {
        return shArrProcessState == null || shArrProcessState.isEmpty() || shArrProcessState.contains("ALL");
    }
}

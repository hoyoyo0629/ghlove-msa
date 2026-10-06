package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 이메일 발송 등록 폼 (AS-IS는 POST /opmanager/email/form에서 {@code Email} 빈에 직접 바인딩한다).
 *
 * AS-IS 화면 JS가 FormData로 보내는 파라미터 이름을 그대로 받는다 -
 * {@code subject, content, sendType, sendDate, authTarget, authList[i].authority, files}.
 * 엔티티에 요청 파라미터를 직접 바인딩하지 않으려고 폼 클래스를 따로 뒀다(AS-IS와 값은 동일).
 */
@Getter
@Setter
@NoArgsConstructor
public class EmailForm {

    private String subject;

    private String content;

    /** D:즉시 R:지정. */
    private String sendType;

    /** 지정발송 시각 yyyyMMddHHmmss (화면이 날짜+시+분+'00'으로 만들어 보낸다). */
    private String sendDate;

    /** A:권한별 S:답례품 E:개별. */
    private String authTarget;

    private List<AuthorityItem> authList = new ArrayList<>();

    /** 발송대상이 권한별일 때 고른 권한코드들. */
    public List<String> authorities() {
        return authList.stream()
                .filter(item -> item != null && item.getAuthority() != null && !item.getAuthority().isBlank())
                .map(AuthorityItem::getAuthority)
                .toList();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AuthorityItem {
        private String authority;
    }
}

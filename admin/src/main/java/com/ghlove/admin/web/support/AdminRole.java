package com.ghlove.admin.web.support;

import com.ghlove.admin.domain.Manager;

/**
 * AS-IS {@code LocgovServiceImpl.getLoginUserAdminRoleCheck} - 로그인한 관리자 권한을 화면 분기용 문자열로 줄인다:
 * ROLE_ADMIN_5/6 = LOC(지자체), 7/8 = OFF(오프라인), 11 = WCM(복지센터), 그 밖은 SYS(시스템/행안부).
 */
public final class AdminRole {

    private AdminRole() {
    }

    public static String of(Manager manager) {
        String auth = manager != null ? manager.getAuthority() : null;
        if (auth == null) {
            return "SYS";
        }
        return switch (auth) {
            case "ROLE_ADMIN_5", "ROLE_ADMIN_6" -> "LOC";
            case "ROLE_ADMIN_7", "ROLE_ADMIN_8" -> "OFF";
            case "ROLE_ADMIN_11" -> "WCM";
            default -> "SYS";
        };
    }
}

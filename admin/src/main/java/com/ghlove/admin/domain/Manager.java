package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자(운영자) 로그인 계정 - AS-IS OP_MANAGER 재현. LOGIN_DATE 등 날짜 컬럼이 OP_USER와
 * 달리 TIMESTAMP가 아니라 VARCHAR(14)(CommonMapper.datetime 컨벤션)인 것도 실제 DDL 그대로다.
 * AUTHORITY 컬럼은 라이브 테이블 배치 스캔에는 없었지만 OP_MANAGER_HIST(이력 테이블)에는
 * 있어(같은 행을 스냅샷 저장하는 구조상 라이브 테이블에도 있었을 것) 그대로 추가했다.
 */
@Entity
@Table(name = "OP_MANAGER")
@Getter
@Setter
@NoArgsConstructor
public class Manager {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opManagerUserIdSeq")
    @SequenceGenerator(name = "opManagerUserIdSeq", sequenceName = "op_manager_user_id_seq", allocationSize = 1)
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "LOGIN_ID")
    private String loginId;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "STATUS_CODE")
    private String statusCode;

    @Column(name = "LOGIN_COUNT")
    private Integer loginCount;

    /** yyyyMMddHHmmss. */
    @Column(name = "LOGIN_DATE")
    private String loginDate;

    @Column(name = "LOGIN_FAIL_COUNT")
    private Integer loginFailCount;

    @Column(name = "LOGIN_TRY_DATE")
    private String loginTryDate;

    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "UPDATED_DATE")
    private String updatedDate;

    /** OP_ROLE.AUTHORITY 참조. AS-IS는 8단계다: ROLE_ADMIN_1(시스템주담당자)/2(시스템부담당자)/
     *  3(행안부주담당자)/4(행안부부담당자)/5(지자체주담당자)/6(지자체부담당자)/
     *  7(오프라인주담당자)/8(오프라인부담당자) - AS-IS 근거는 ghlove-common의
     *  `UserAdminRole`(SYS/MOIS/LOC/OFF 4그룹 × 정·부). 이 프로젝트는 1~6만 구현했고
     *  7·8은 미구현이다. 자세한 내용은 {@link com.ghlove.admin.service.MenuService}. */
    @Column(name = "AUTHORITY")
    private String authority;

    /** ROLE_ADMIN_5/6(지자체 담당자)만 값을 갖는다 - 그 지자체 데이터로 조회범위가 제한된다.
     *  AS-IS 라이브 스캔에도 이미 있던 실제 컬럼(그동안 매핑만 안 돼 있었음). */
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    /** 인증서(금융인증서/공동인증서) 로그인용 Subject DN - 사전 등록 필요, 미등록 시 null
     *  (AS-IS opmanager/user/login_main.jsp의 인증서 로그인 카드와 동일한 방식이지만,
     *  MagicLine4Web 브라우저 SDK 대신 이 프로젝트는 CertLoginService와 동일하게 인증서
     *  파일 업로드+로컬 X.509 파싱으로 DN을 얻는다 - SDK 없이도 실제로 동작하는 부분). */
    @Column(name = "CERT_SUBJECT_DN")
    private String certSubjectDn;

    /** D8 오프라인담당자(ROLE_ADMIN_7/8) 전용 확장 컬럼 - DB에는 이미 있었으나(라이브 스캔)
     *  그동안 매핑이 안 돼 있었다. 오프라인 기부접수 정산계좌 코드. */
    @Column(name = "BANK_CODE")
    private String bankCode;

    /** 사번(직원식별번호). */
    @Column(name = "EMP_ID")
    private String empId;

    /** 직위명(예: 팀장/주무관). */
    @Column(name = "PSITN_NM")
    private String psitnNm;

    /** 소속부서명. */
    @Column(name = "PSITN_DEPT_NM")
    private String psitnDeptNm;

    /** 직책명. */
    @Column(name = "OFCPS_NM")
    private String ofcpsNm;

    /** 휴대폰 - 운영자관리(메뉴 4501)·지자체담당자관리(4402) 목록 컬럼. 컬럼은 원래 있었고 매핑만 없었다. */
    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    /** 중지일자 - 사용여부가 '중지'일 때만 목록에 보여준다(AS-IS 동일). */
    @Column(name = "DENY_DATE")
    private String denyDate;

    /**
     * 정보수정일(yyyyMMdd) - 오프라인담당자(메뉴 4601)가 지점정보를 처음 채웠는지 판정하는 플래그다.
     * AS-IS는 등록 시 오늘로 넣고, 수정 시 <b>비어 있을 때만</b> 오늘로 채운다(한 번 채워지면
     * 그대로 둔다 - AS-IS personincharge-mapper.updateOffPersonInCharge의 CASE WHEN).
     * 컬럼은 원래 있었고 매핑만 없었다.
     */
    @Column(name = "INFO_UPDT_DE")
    private String infoUpdtDe;

    /**
     * AS-IS 담당자 화면들이 쓰는 사용여부 값('9' 사용 / '2' 중지)으로 바꿔 준다.
     *
     * <p>AS-IS {@code OP_MANAGER.STATUS_CODE}는 숫자 코드지만 이 프로젝트는 같은 컬럼에
     * {@code ACTIVE}/{@code LOCKED}를 쓴다({@link com.ghlove.admin.service.ManagerAdminService}).
     * 화면 마크업은 AS-IS 그대로 9/2 라디오를 두고, 값 변환은 이 getter와
     * {@code PersonInChargeAdminService.toStatusCode}가 경계에서 담당한다 - 그러지 않으면
     * 수정화면에서 어떤 라디오도 선택되지 않고, 저장 시 '9'/'2'가 들어가 두 체계가 섞인다.
     */
    @Transient
    public String getAsIsStatusCode() {
        return "LOCKED".equals(statusCode) ? "2" : "9";
    }
}

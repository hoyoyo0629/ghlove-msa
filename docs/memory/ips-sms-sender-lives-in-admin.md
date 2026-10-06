---
name: ips-sms-sender-lives-in-admin
description: "국민비서(IPS) 문자 \"발송\"은 TIF_IPS_SNDNG_M 적재이고, TO-BE 발송부는 admin SmsIpsService 하나뿐이다(나머지 문자종류 미이식)"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-04T23:48:16.510Z
---

고향사랑e음의 문자 발송은 외부 API 호출이 아니라 **`TIF_IPS_SNDNG_M`에 한 행 적재**하는 것이고,
행정망 ESB가 그 행을 집어가 국민비서로 보낸다. AS-IS `SmsIpsServiceImpl.giveSendSms`가 그 적재부다.

**TO-BE 현황(2026-10-05)**: 적재부는 `admin/service/SmsIpsService.java` **하나뿐**이고,
Q&A 답변(`SmsType.QNA`, SVC_ID `812-A022`) 한 종류만 호출된다. 표(admin.tif_ips_sndng_m)와
`SmsType` 24종 enum, 문자전송이력 조회화면(7208)은 이미 있었지만 **적재하는 코드가 없었다**.
AS-IS에서 발송하는 나머지(기부시 `812-A002`, 답례품 주문/배송, 과오납, 명예기부, 가입/탈퇴/비번변경,
관리자 로그인 등)는 **아직 미이식**이다 - 각 도메인 라운드에서 `SmsIpsService.send()`를 호출하면 된다.

**규칙(AS-IS 그대로, 바꾸지 말 것)**
- 수신동의 `RECEIVE_SMS='0'`인 회원만 보낸다(`0`=수신, `1`=비수신. [[receive-sms-encoding-split]] 주의)
- 수신자 식별값은 **회원 CI**(`PRVC_IDNTFC_INFO = op_user.mber_ci`). CI 없으면 발송하지 않는다
- 발송내용은 파이프 구분 문자열. QNA·가입·탈퇴·비번변경·관리자로그인은 `이름|전화번호` 2칸,
  기부·과오납은 `이름|지자체|금액|전화번호`, 명예기부는 `이름|지자체|전화번호`
- `LIST_SN`과 `INSTT_CRT_SN`(PK)에 **같은 채번값** - 시퀀스 `admin.tif_ips_sndng_m_list_sn`
  (`database/ddl/migration-admin-tif-ips-sequence.sql`, AS-IS CUBRID serial 대체)
- 공통값 `ESB_STATUS_CD='N'`, `ESB_WORK_GBN='I'`, `SVC_GRP_ID`/`PRVC_IDNTFC_SE_CD`/`ESB_IF_ID`는 설정값

**개발환경에서는 꺼 둔다**: `ghlove.integrations.sms-ips.enabled=false`.
적재하면 실제 문자가 나갈 수 있어서다(AS-IS 소스에도 같은 이유의 로컬 가드가 들어가 있다).
운영 전환 때 켜는 것을 잊지 말 것.

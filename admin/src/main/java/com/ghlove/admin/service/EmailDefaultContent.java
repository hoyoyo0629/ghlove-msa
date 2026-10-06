package com.ghlove.admin.service;

/**
 * 이메일 발송 폼의 기본 본문 (AS-IS opmanager/i18n/mail/form.jsp의 textarea#content 초기값).
 *
 * AS-IS는 이 HTML(고향사랑e음 로고 + "제목"/"내용" 자리 + 수신거부 안내 + 주소/고객센터 푸터)을
 * 스마트에디터 초기 내용으로 미리 채워 두고, 운영자가 그 틀 안에서 메일을 작성한다. 한 글자도
 * 바꾸지 않고 그대로 옮긴 것이다 - 템플릿에 두면 Thymeleaf가 마크업으로 파싱해 재직렬화하므로
 * 상수로 두고 th:utext로 textarea 본문에 그대로 쓴다.
 */
public final class EmailDefaultContent {

    public static final String HTML = """
<p><font face="Malgun Gothic, 돋움, dotum, sans-serif"><meta charset="UTF-8"><meta http-equiv="X-UA-Compatible" content="IE=edge"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title>고향사랑관리자</title></font></p><table width="100%" cellspacing="0" cellpadding="0" border="0" style="font-family: 'Noto Sans KR', -apple-system, BlinkMacSystemFont, system-ui, Roboto, 'Helvetica Neue', 'Segoe UI', 'Apple SD Gothic Neo',, 'Malgun Gothic', sans-serif; font-size: 14px; line-height: 1; font-weight: 400; vertical-align: top; word-wrap: break-word; word-break: break-all; box-sizing: border-box;"><tbody><tr><td><table align="center" style="width: 600px; border: 5px solid #d6d6d6; border-collapse: collapse; margin: 0 auto;"><thead><tr><td style="padding: 25px 25px 55px"><h1 style="margin: 0; text-align: left;"><a style="display: block;" href="https://www.ilovegohyang.go.kr" title="고향사랑이음"><img style="border: 0; width: 190px; height: 38px;" src="https://www.ilovegohyang.go.kr/content/opmanager/images/img_logo_02.png" alt="고향사랑이음 로고"></a></h1></td></tr></thead><tbody><tr><td style="padding: 0 36px; text-align: center;"><h2 style="margin: 0 0 40px; font-size: 38px; line-height: 45px; font-weight: bold; color: #1c2957">제목</h2></td></tr><tr><td style="padding: 0 36px"><ul style="margin: 0; padding: 30px 0; background: #f7f7f7; font-size: 14px; color: #4c4c4c; text-align: center; list-style: none">내용</ul></td></tr><tr><td style="padding: 40px 36px 0; text-align: center;"><br></td></tr></tbody><tfoot><tr><td style="padding: 50px 36px 30px; line-height: 18px; text-align: center"><div style="padding: 20px; background: #f5f5f5;"><p style="margin: 4px 0 0 0">메일수신을 원하지 않을 경우, 홈페이지 로그인 후 회원정보변경에서&nbsp;</p><p style="margin: 4px 0 0 0">e-mail수신여부를 변경해 주세요.</p></div></td></tr><tr><td style="padding: 20px 36px 0; line-height: 18px; color: #959595; text-align: center; letter-spacing: -1px"><p style="margin: 0">30128 세종특별자치시 정부2청사로13(나성동)<span style="padding: 0px 6px; color: #bbb; font-size: 10px">|</span>30116 세종특별자치시 한누리대로411(어진동)<span style="padding: 0px 6px; color: #bbb; font-size: 10px">|</span>고객센터 1522-2431</p></td></tr><tr><td style="padding: 0 36px 35px; line-height: 18px; color: #959595; text-align: center; letter-spacing: -1px"><p style="margin: 7px 0 0 0; color: #b0b0b0">©Ministry of the interior and safety.All rights reserved.</p></td></tr></tfoot></table></td></tr></tbody></table><br><br>&nbsp;&nbsp;<p>&nbsp;</p><p>&nbsp;</p><p>&nbsp;</p>""";

    private EmailDefaultContent() {
    }
}

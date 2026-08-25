package com.ghlove.admin.web;

/** donation 서비스의 기금사업소개("지자체공지사항" 탭)가 크로스서비스로 조회하는 응답 DTO. */
public record NoticeDto(Integer noticeId, String subject, String createdDate) {
}

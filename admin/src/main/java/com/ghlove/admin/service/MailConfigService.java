package com.ghlove.admin.service;

import com.ghlove.admin.domain.MailConfig;
import com.ghlove.admin.repository.MailConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/** 메일설정 관리 (AS-IS opmanager/mail-config) - 주문상태(ORDER_STATUS)별 발송 이메일
 *  템플릿 CRUD. 실제 발송 트리거(주문상태 변경 시 자동 발송)는 이 프로젝트에 아직 없다 -
 *  order 서비스는 PENDING/CONFIRMED/CANCELLED 3단계 SAGA choreography만 쓰고 AS-IS의
 *  15단계 배송/교환/반품 흐름은 구현돼 있지 않기 때문(템플릿 코드값 재설계는 별도 작업) -
 *  이 화면은 템플릿 내용 관리까지만 담당한다. */
@Service
@RequiredArgsConstructor
public class MailConfigService {

    private final MailConfigRepository mailConfigRepository;
    private final MailTemplateCodes mailTemplateCodes;

    public record TemplateRow(String templateId, String label, boolean configured) {
    }

    /**
     * AS-IS MailTemplate.getTemplateCodes()의 고정 10종. 예전에는 공통코드 ORDER_STATUS로
     * 목록을 만들었는데 그건 주문상태라 회원가입·임시비밀번호·문의답변·휴면안내·관리자 권한
     * 승인/거절 템플릿이 아예 없었다 - AS-IS 목록으로 바로잡았다.
     */
    public List<TemplateRow> templateList() {
        List<TemplateRow> rows = new java.util.ArrayList<>();
        mailTemplateCodes.templateCodes().forEach((id, label) ->
                rows.add(new TemplateRow(id, label, mailConfigRepository.findByTemplateId(id).isPresent())));
        return rows;
    }

    public String labelOf(String templateId) {
        return mailTemplateCodes.templateCodeTitle(templateId);
    }

    /** AS-IS getMailConfigByTemplateId - 없으면 null(등록/수정 분기에 쓰인다). */
    public MailConfig findByTemplateId(String templateId) {
        return mailConfigRepository.findByTemplateId(templateId).orElse(null);
    }

    /** AS-IS deleteMailConfig - 목록의 삭제 링크. */
    @Transactional
    public void delete(Integer mailConfigId) {
        mailConfigRepository.deleteById(mailConfigId);
    }

    public MailConfig getOrNew(String templateId) {
        return mailConfigRepository.findByTemplateId(templateId).orElseGet(() -> {
            MailConfig config = new MailConfig();
            config.setTemplateId(templateId);
            config.setSmsConfig("N");
            return config;
        });
    }

    @Transactional
    public MailConfig save(MailConfig form) {
        MailConfig target = mailConfigRepository.findByTemplateId(form.getTemplateId()).orElseGet(MailConfig::new);
        target.setTemplateId(form.getTemplateId());
        target.setSmsConfig(form.getSmsConfig());
        target.setTitle(form.getTitle());
        target.setBuyerSubject(form.getBuyerSubject());
        target.setBuyerContent(form.getBuyerContent());
        target.setAdminSubject(form.getAdminSubject());
        target.setAdminContent(form.getAdminContent());
        target.setSellerSubject(form.getSellerSubject());
        target.setSellerContent(form.getSellerContent());
        // AS-IS 폼이 보내는 발송여부 - buyerSendFlag는 라디오, adminSendFlag는 hidden('N')이다
        target.setBuyerSendFlag(form.getBuyerSendFlag() != null ? form.getBuyerSendFlag() : "N");
        target.setAdminSendFlag(form.getAdminSendFlag() != null ? form.getAdminSendFlag() : "N");
        if (target.getCreatedDate() == null) {
            target.setCreatedDate(java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        }
        return mailConfigRepository.save(target);
    }
}

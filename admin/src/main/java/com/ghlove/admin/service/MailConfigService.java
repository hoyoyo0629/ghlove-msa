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
    private final CommonCodeService commonCodeService;

    public record TemplateRow(String templateId, String label, boolean configured) {
    }

    public List<TemplateRow> templateList() {
        Map<String, String> labels = commonCodeService.labelsOf("ORDER_STATUS");
        List<TemplateRow> rows = new java.util.ArrayList<>();
        labels.forEach((id, label) -> rows.add(new TemplateRow(id, label, mailConfigRepository.findByTemplateId(id).isPresent())));
        return rows;
    }

    public String labelOf(String templateId) {
        return commonCodeService.labelsOf("ORDER_STATUS").getOrDefault(templateId, templateId);
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
        return mailConfigRepository.save(target);
    }
}

package com.ghlove.order.service;

import com.ghlove.order.domain.DeliveryCompany;
import com.ghlove.order.repository.DeliveryCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 택배사 마스터 (AS-IS DeliveryCompanyManagerController). 편집 주체는 admin, 여기는 CRUD 로직만. */
@Service
@RequiredArgsConstructor
public class DeliveryCompanyService {

    private static final String USE_Y = "Y";

    private final DeliveryCompanyRepository repository;

    public List<DeliveryCompany> all() {
        return repository.findAllByOrderByDeliveryCompanyIdDesc();
    }

    public List<DeliveryCompany> active() {
        return repository.findByUseFlagOrderByDeliveryCompanyNameAsc(USE_Y);
    }

    public DeliveryCompany get(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new OrderException("택배사를 찾을 수 없습니다."));
    }

    @Transactional
    public DeliveryCompany create(String name, String telNumber, String url, String sendFlag,
                                  String numberParameter, String useFlag) {
        requireName(name);
        DeliveryCompany c = new DeliveryCompany();
        apply(c, name, telNumber, url, sendFlag, numberParameter, useFlag);
        return repository.save(c);
    }

    @Transactional
    public DeliveryCompany update(Integer id, String name, String telNumber, String url, String sendFlag,
                                  String numberParameter, String useFlag) {
        requireName(name);
        DeliveryCompany c = get(id);
        apply(c, name, telNumber, url, sendFlag, numberParameter, useFlag);
        return repository.save(c);
    }

    @Transactional
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    private void apply(DeliveryCompany c, String name, String telNumber, String url, String sendFlag,
                       String numberParameter, String useFlag) {
        c.setDeliveryCompanyName(name.trim());
        c.setTelNumber(telNumber);
        c.setDeliveryCompanyUrl(url);
        c.setSendFlag(sendFlag == null || sendFlag.isBlank() ? "1" : sendFlag);
        c.setDeliveryNumberParameter(numberParameter);
        c.setUseFlag(USE_Y.equals(useFlag) ? USE_Y : "N");
    }

    private void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new OrderException("택배사명을 입력해 주세요.");
        }
    }
}

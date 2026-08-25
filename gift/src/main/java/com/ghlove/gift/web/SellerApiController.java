package com.ghlove.gift.web;

import com.ghlove.gift.domain.Seller;
import com.ghlove.gift.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 입점업체관리 (AS-IS opmanager/seller) cross-service API - admin 콘솔이 이 엔드포인트로
 *  판매자(답례품 제공업체) CRUD를 수행한다. 판매자 자기서비스 로그인은 범위 밖(Seller.java
 *  주석 참고) - 이 API는 admin 전용 관리 채널이다. */
@RestController
@RequestMapping("/api/admin/sellers")
@RequiredArgsConstructor
public class SellerApiController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final SellerRepository sellerRepository;

    @GetMapping
    public List<Seller> list() {
        return sellerRepository.findAllByOrderBySellerIdDesc();
    }

    @GetMapping("/{id}")
    public Seller get(@PathVariable Long id) {
        return sellerRepository.findById(id).orElseThrow();
    }

    @PostMapping
    public Seller create(@RequestBody Seller form) {
        form.setSellerId(null);
        form.setCreatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        form.setUpdatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        if (form.getStatusCode() == null) {
            form.setStatusCode("0");
        }
        return sellerRepository.save(form);
    }

    @PutMapping("/{id}")
    public Seller update(@PathVariable Long id, @RequestBody Seller form) {
        Seller seller = sellerRepository.findById(id).orElseThrow();
        seller.setSellerName(form.getSellerName());
        seller.setCompanyName(form.getCompanyName());
        seller.setRepresentativeName(form.getRepresentativeName());
        seller.setBusinessNumber(form.getBusinessNumber());
        seller.setBusinessLocation(form.getBusinessLocation());
        seller.setUserName(form.getUserName());
        seller.setTelephoneNumber(form.getTelephoneNumber());
        seller.setPhoneNumber(form.getPhoneNumber());
        seller.setEmail(form.getEmail());
        seller.setAddress(form.getAddress());
        seller.setAddressDetail(form.getAddressDetail());
        seller.setBankName(form.getBankName());
        seller.setBankInName(form.getBankInName());
        seller.setBankAccountNumber(form.getBankAccountNumber());
        seller.setCommissionRate(form.getCommissionRate());
        seller.setRemittanceType(form.getRemittanceType());
        seller.setCommunityBusinessYn(form.getCommunityBusinessYn());
        seller.setUpdatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        return sellerRepository.save(seller);
    }

    @PostMapping("/{id}/status")
    public Seller updateStatus(@PathVariable Long id, @RequestParam String statusCode) {
        Seller seller = sellerRepository.findById(id).orElseThrow();
        seller.setStatusCode(statusCode);
        seller.setUpdatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        return sellerRepository.save(seller);
    }
}

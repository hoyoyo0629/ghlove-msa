package com.ghlove.donation.web;

import com.ghlove.donation.domain.WelfareCenter;
import com.ghlove.donation.repository.WelfareCenterRepository;
import com.ghlove.donation.service.DonationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/** admin 콘솔 "행정복지센터 관리" 화면(AS-IS opmanager/welfareCenter -
 * WelfareCenterManagerController)이 부르는 cross-service API - 다른 admin 전용 API와
 * 동일하게 별도 인증 없이 열려있다(admin 콘솔에서만 호출되는 내부용). */
@RestController
@RequestMapping("/api/welfare-centers-admin")
@RequiredArgsConstructor
public class WelfareCenterAdminApiController {

    private final WelfareCenterRepository repository;

    public record Row(Long id, String lclgvCd, String name, String code, String useYn, String frstRegDt) {
    }

    public record Form(String lclgvCd, String name, String code, String useYn) {
    }

    @GetMapping
    public List<Row> list(@RequestParam(required = false) String lclgvCd) {
        List<WelfareCenter> all = (lclgvCd == null || lclgvCd.isBlank())
                ? repository.findAllByOrderByPbadmsWlfrCntrIdDesc()
                : repository.findByLclgvCdOrderByPbadmsWlfrCntrIdDesc(lclgvCd);
        return all.stream().map(this::toRow).toList();
    }

    @PostMapping
    public Row create(@RequestBody Form form) {
        if (form.name() == null || form.name().isBlank()) {
            throw new DonationException("센터명을 입력해 주세요.");
        }
        WelfareCenter w = new WelfareCenter();
        w.setLclgvCd(form.lclgvCd());
        w.setPbadmsWlfrCntrNm(form.name());
        w.setPbadmsWlfrCntrCd(form.code());
        w.setUseYn("Y");
        w.setFrstRegDt(LocalDateTime.now());
        return toRow(repository.save(w));
    }

    @PutMapping("/{id}")
    public Row update(@PathVariable Long id, @RequestBody Form form) {
        WelfareCenter w = repository.findById(id).orElseThrow(() -> new DonationException("센터를 찾을 수 없습니다."));
        w.setLclgvCd(form.lclgvCd());
        w.setPbadmsWlfrCntrNm(form.name());
        w.setPbadmsWlfrCntrCd(form.code());
        w.setUseYn(form.useYn() != null ? form.useYn() : w.getUseYn());
        return toRow(repository.save(w));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Row toRow(WelfareCenter w) {
        return new Row(w.getPbadmsWlfrCntrId(), w.getLclgvCd(), w.getPbadmsWlfrCntrNm(), w.getPbadmsWlfrCntrCd(),
                w.getUseYn(), w.getFrstRegDt() != null ? w.getFrstRegDt().toString() : null);
    }
}

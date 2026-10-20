package com.ghlove.gift.web;

import com.ghlove.gift.service.SellerManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * 관리자 > 답례품관리 > 답례품제공자 관리(AS-IS opmanager/seller) cross-service API - 판매자 + 판매자 계정 목록/등록/수정.
 * admin 콘솔이 부른다(X-Internal-Secret 보호 경로 /api/admin/**). 등록·수정은 AS-IS 폼 입력칸 이름 그대로 multipart로 받는다.
 * (기존 {@code /api/admin/sellers}는 이메일 발송·문자 수신정보·답례품 판매자 선택 등 다른 화면이 쓰는 채널이라 그대로 둔다.)
 */
@RestController
@RequestMapping("/api/admin/seller-manage")
@RequiredArgsConstructor
public class SellerManageApiController {

    private final SellerManageService service;

    @GetMapping("/count")
    public Map<String, Integer> count(@RequestParam(required = false) String statusCode,
                                      @RequestParam(required = false) String where,
                                      @RequestParam(required = false) String query,
                                      @RequestParam(required = false) String startDate,
                                      @RequestParam(required = false) String endDate,
                                      @RequestParam(required = false) String itemApprovalType,
                                      @RequestParam(required = false) String locgov) {
        return Map.of("count", service.count(new SellerManageService.Search(statusCode, where, query, startDate, endDate, itemApprovalType, locgov)));
    }

    @GetMapping("/list")
    public List<SellerManageService.ListRow> list(@RequestParam(required = false) String statusCode,
                                                  @RequestParam(required = false) String where,
                                                  @RequestParam(required = false) String query,
                                                  @RequestParam(required = false) String startDate,
                                                  @RequestParam(required = false) String endDate,
                                                  @RequestParam(required = false) String itemApprovalType,
                                                  @RequestParam(required = false) String locgov,
                                                  @RequestParam(defaultValue = "0") int offset,
                                                  @RequestParam(defaultValue = "10") int limit) {
        return service.list(new SellerManageService.Search(statusCode, where, query, startDate, endDate, itemApprovalType, locgov), offset, limit);
    }

    /** 판매자 계정(OP_SELLER_USER) 번호로 아이디·지자체 - 관리자 문의(AS-IS qna-admin) 목록이 작성자 칸과 지자체 칸에 쓴다. */
    @PostMapping("/users")
    public List<SellerManageService.UserBrief> users(@org.springframework.web.bind.annotation.RequestBody List<Long> userIds) {
        return service.usersByIds(userIds);
    }

    /** 지자체 판매자 목록 - 상품검색 팝업(AS-IS item/find-item)의 판매자 select. */
    @GetMapping("/by-locgov")
    public List<SellerManageService.SellerOption> sellersByLocgov(@RequestParam(required = false) String locgov) {
        return service.sellersByLocgov(locgov);
    }

    @GetMapping("/{sellerId}")
    public Map<String, Object> detail(@PathVariable long sellerId) {
        Map<String, Object> d = service.detail(sellerId);
        if (d == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "판매자를 찾을 수 없습니다.");
        }
        return d;
    }

    @GetMapping("/make-id")
    public Map<String, String> makeId(@RequestParam(required = false) String locgovCode) {
        return Map.of("loginId", service.makeSellerId(locgovCode));
    }

    @PostMapping
    public Map<String, Long> create(@RequestParam Map<String, String> params,
                                    @RequestParam(required = false) MultipartFile uploadFile1,
                                    @RequestParam(required = false) MultipartFile uploadFile2,
                                    @RequestParam(required = false) MultipartFile uploadFile3,
                                    @RequestParam Long createdUserId,
                                    @RequestParam(required = false) String locgovCode,
                                    @RequestParam(required = false) String passwordExpiredDate) {
        long id = service.create(params, new MultipartFile[]{uploadFile1, uploadFile2, uploadFile3}, createdUserId, locgovCode, passwordExpiredDate);
        return Map.of("sellerId", id);
    }

    @PostMapping("/{sellerId}")
    public void update(@PathVariable long sellerId,
                       @RequestParam Map<String, String> params,
                       @RequestParam(required = false) MultipartFile uploadFile1,
                       @RequestParam(required = false) MultipartFile uploadFile2,
                       @RequestParam(required = false) MultipartFile uploadFile3,
                       @RequestParam Long updatedUserId) {
        service.update(sellerId, params, new MultipartFile[]{uploadFile1, uploadFile2, uploadFile3}, updatedUserId);
    }

    @PostMapping("/{sellerId}/init-password")
    public void initPassword(@PathVariable long sellerId, @RequestParam String password, @RequestParam Long updatedUserId) {
        service.initPassword(sellerId, password, updatedUserId);
    }

    @PostMapping("/{sellerId}/delete-file")
    public void deleteFile(@PathVariable long sellerId, @RequestParam String fileType, @RequestParam Long updatedUserId) {
        service.deleteFile(sellerId, fileType, updatedUserId);
    }
}

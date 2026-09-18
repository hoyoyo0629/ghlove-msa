package com.ghlove.admin.web;

import com.ghlove.admin.domain.Popup;
import com.ghlove.admin.service.OperationContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * storefront(Vue3 SPA)용 공개 팝업 JSON API - AS-IS `/api/popup/list`(PopupController)
 * 재현. 운영 콘솔의 팝업관리({@link OperationContentController} /popups)는 등록·수정·삭제·
 * 토글까지 있었으나, 이용자 화면에 실제로 띄우는 경로가 없어 등록해도 뜨지 않았다.
 * displayPopups()가 노출기간·사용여부를 판정하고, 렌더링(레이어/오늘 하루 보지않음)은
 * storefront SitePopups.vue가 한다.
 */
@RestController
@RequiredArgsConstructor
public class PopupApiController {

    private final OperationContentService operationContentService;

    /** popupStyle "3"=이미지, 그 외=텍스트 (AS-IS 컨벤션). imageLink는 이미지 클릭 시 이동 URL. */
    public record PopupDto(Integer popupId, String popupType, String popupStyle, String popupClose,
                           String subject, String content, String popupImage, String imageLink,
                           Integer width, Integer height, Integer topPosition, Integer leftPosition,
                           String backgroundColor) {
    }

    @GetMapping("/api/popups")
    public List<PopupDto> list() {
        return operationContentService.displayPopups().stream()
                .map(p -> new PopupDto(p.getPopupId(), p.getPopupType(), p.getPopupStyle(), p.getPopupClose(),
                        p.getSubject(), p.getContent(), p.getPopupImage(), p.getImageLink(),
                        p.getWidth(), p.getHeight(), p.getTopPosition(), p.getLeftPosition(),
                        p.getBackgroundColor()))
                .toList();
    }
}

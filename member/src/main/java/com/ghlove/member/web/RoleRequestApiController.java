package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.domain.UserRoleRequest;
import com.ghlove.member.service.MemberException;
import com.ghlove.member.service.MemberService;
import com.ghlove.member.service.RoleRequestService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** storefront(Vue3 SPA)용 "지자체담당자·제공자 역할 신청"/"역할신청 승인함" JSON API -
 * {@link RoleRequestController}(Thymeleaf)와 완전히 같은 {@link RoleRequestService} 로직을
 * JSON 요청/응답으로 감싼다. roles/queue는 AS-IS Thymeleaf 버전과 동일하게 승인권자 인증
 * 게이트웨이가 아직 없어 로그인만 요구하고 별도 권한 검사는 하지 않는다({@link RoleRequestController#queue}
 * 참고 - "임시로 이 화면에서 바로 처리"). */
@RestController
@RequiredArgsConstructor
public class RoleRequestApiController {

    private final RoleRequestService roleRequestService;
    private final MemberService memberService;

    private User requireLogin(HttpSession session) {
        return (User) session.getAttribute(AuthController.SESSION_USER_KEY);
    }

    public record RoleRequestRowDto(Long requestId, String requestedRole, String requestedRoleLabel, String reason,
                                     String status, String statusLabel, LocalDateTime createdDate) {
    }

    public record MyRoleRequestsResponse(List<RoleRequestRowDto> myRequests, Map<String, String> roleLabels) {
    }

    @GetMapping("/api/roles/request")
    public ResponseEntity<MyRoleRequestsResponse> myRequests(HttpSession session) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        Map<String, String> roleLabels = memberService.codesOf("ROLE");
        Map<String, String> statusLabels = memberService.codesOf("ROLE_REQUEST_STATUS");
        List<RoleRequestRowDto> rows = roleRequestService.requestsOf(user.getUserId()).stream()
                .map(r -> toDto(r, roleLabels, statusLabels))
                .toList();
        return ResponseEntity.ok(new MyRoleRequestsResponse(rows, roleLabels));
    }

    public record RequestRoleRequest(String requestedRole, String reason) {
    }

    @PostMapping("/api/roles/request")
    public ResponseEntity<Map<String, Object>> request(HttpSession session, @RequestBody RequestRoleRequest req) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            roleRequestService.request(user.getUserId(), req.requestedRole(), req.reason());
            return ResponseEntity.ok(Map.of("status", "OK"));
        } catch (MemberException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    public record QueueRowDto(Long requestId, Long userId, String requestedRole, String requestedRoleLabel,
                               String reason, LocalDateTime createdDate) {
    }

    @GetMapping("/api/roles/queue")
    public ResponseEntity<List<QueueRowDto>> queue(HttpSession session) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        Map<String, String> roleLabels = memberService.codesOf("ROLE");
        List<QueueRowDto> rows = roleRequestService.pendingRequests().stream()
                .map(r -> new QueueRowDto(r.getRequestId(), r.getUserId(), r.getRequestedRole(),
                        roleLabels.getOrDefault(r.getRequestedRole(), r.getRequestedRole()),
                        r.getReason(), r.getCreatedDate()))
                .toList();
        return ResponseEntity.ok(rows);
    }

    @PostMapping("/api/roles/{requestId}/approve")
    public ResponseEntity<Void> approve(HttpSession session, @PathVariable Long requestId) {
        if (requireLogin(session) == null) {
            return ResponseEntity.status(401).build();
        }
        roleRequestService.approve(requestId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/roles/{requestId}/reject")
    public ResponseEntity<Void> reject(HttpSession session, @PathVariable Long requestId) {
        if (requireLogin(session) == null) {
            return ResponseEntity.status(401).build();
        }
        roleRequestService.reject(requestId);
        return ResponseEntity.noContent().build();
    }

    private RoleRequestRowDto toDto(UserRoleRequest r, Map<String, String> roleLabels, Map<String, String> statusLabels) {
        return new RoleRequestRowDto(r.getRequestId(), r.getRequestedRole(),
                roleLabels.getOrDefault(r.getRequestedRole(), r.getRequestedRole()), r.getReason(),
                r.getStatus(), statusLabels.getOrDefault(r.getStatus(), r.getStatus()), r.getCreatedDate());
    }
}

package com.ghlove.member.service;

import com.ghlove.member.domain.UserDelivery;
import com.ghlove.member.repository.UserDeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 마이페이지 "배송지 관리" - AS-IS mypage/deliveryInfo.html 재현. 배송지는 여러 개
 *  등록 가능하고 그중 하나만 DEFAULT_FLAG='Y'(기본배송지)다. */
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private static final String FLAG_YES = "Y";
    private static final String FLAG_NO = "N";

    private final UserDeliveryRepository userDeliveryRepository;

    public List<UserDelivery> listOf(Long userId) {
        return userDeliveryRepository.findByUserIdOrderByDefaultFlagDescCreatedDateDesc(userId);
    }

    public UserDelivery get(Long userId, Long userDeliveryId) {
        return userDeliveryRepository.findByUserDeliveryIdAndUserId(userDeliveryId, userId)
                .orElseThrow(() -> new MemberException("배송지 정보를 찾을 수 없습니다."));
    }

    @Transactional
    public UserDelivery create(Long userId, String title, String userName, String phone, String mobile,
                                String zipcode, String address, String addressDetail, boolean makeDefault) {
        validate(title, userName, mobile, address);
        boolean isFirst = userDeliveryRepository.findByUserIdOrderByDefaultFlagDescCreatedDateDesc(userId).isEmpty();
        boolean setDefault = makeDefault || isFirst;
        if (setDefault) {
            clearExistingDefault(userId);
        }

        UserDelivery delivery = new UserDelivery();
        delivery.setUserId(userId);
        delivery.setDefaultFlag(setDefault ? FLAG_YES : FLAG_NO);
        delivery.setTitle(title);
        delivery.setUserName(userName);
        delivery.setPhone(phone);
        delivery.setMobile(mobile);
        delivery.setZipcode(zipcode);
        delivery.setNewZipcode(zipcode);
        delivery.setAddress(address);
        delivery.setAddressDetail(addressDetail);
        delivery.setCreatedDate(LocalDateTime.now());
        return userDeliveryRepository.save(delivery);
    }

    @Transactional
    public UserDelivery update(Long userId, Long userDeliveryId, String title, String userName, String phone,
                                String mobile, String zipcode, String address, String addressDetail,
                                boolean makeDefault) {
        validate(title, userName, mobile, address);
        UserDelivery delivery = get(userId, userDeliveryId);
        if (makeDefault && !FLAG_YES.equals(delivery.getDefaultFlag())) {
            clearExistingDefault(userId);
            delivery.setDefaultFlag(FLAG_YES);
        }
        delivery.setTitle(title);
        delivery.setUserName(userName);
        delivery.setPhone(phone);
        delivery.setMobile(mobile);
        delivery.setZipcode(zipcode);
        delivery.setNewZipcode(zipcode);
        delivery.setAddress(address);
        delivery.setAddressDetail(addressDetail);
        return userDeliveryRepository.save(delivery);
    }

    @Transactional
    public void delete(Long userId, Long userDeliveryId) {
        UserDelivery delivery = get(userId, userDeliveryId);
        boolean wasDefault = FLAG_YES.equals(delivery.getDefaultFlag());
        userDeliveryRepository.delete(delivery);
        if (wasDefault) {
            userDeliveryRepository.findByUserIdOrderByDefaultFlagDescCreatedDateDesc(userId).stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.setDefaultFlag(FLAG_YES);
                        userDeliveryRepository.save(next);
                    });
        }
    }

    @Transactional
    public void setDefault(Long userId, Long userDeliveryId) {
        UserDelivery delivery = get(userId, userDeliveryId);
        clearExistingDefault(userId);
        delivery.setDefaultFlag(FLAG_YES);
        userDeliveryRepository.save(delivery);
    }

    private void clearExistingDefault(Long userId) {
        userDeliveryRepository.findByUserIdOrderByDefaultFlagDescCreatedDateDesc(userId).stream()
                .filter(d -> FLAG_YES.equals(d.getDefaultFlag()))
                .forEach(d -> {
                    d.setDefaultFlag(FLAG_NO);
                    userDeliveryRepository.save(d);
                });
    }

    private void validate(String title, String userName, String mobile, String address) {
        if (title == null || title.isBlank()) {
            throw new MemberException("배송지명을 입력해 주세요.");
        }
        if (userName == null || userName.isBlank()) {
            throw new MemberException("받는사람을 입력해 주세요.");
        }
        if (mobile == null || mobile.isBlank()) {
            throw new MemberException("휴대전화번호를 입력해 주세요.");
        }
        if (address == null || address.isBlank()) {
            throw new MemberException("주소를 입력해 주세요.");
        }
    }
}

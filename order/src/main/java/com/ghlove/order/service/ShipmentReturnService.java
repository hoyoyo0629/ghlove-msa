package com.ghlove.order.service;

import com.ghlove.order.domain.ShipmentReturn;
import com.ghlove.order.repository.ShipmentReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 반송지(반품 회수 주소) 관리 - AS-IS ShipmentReturnManagerController. 판매자별 주소록이고
 * 기본 반품지(defaultAddressFlag='Y')는 판매자당 하나만 유지한다(AS-IS updateDefaultAddressFlag).
 */
@Service
@RequiredArgsConstructor
public class ShipmentReturnService {

    private static final String FLAG_Y = "Y";
    private static final String FLAG_N = "N";
    private static final DateTimeFormatter CREATED = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ShipmentReturnRepository repository;

    public List<ShipmentReturn> bySeller(Long sellerId) {
        return repository.findBySellerIdOrderByDefaultAddressFlagDescShipmentReturnIdDesc(sellerId);
    }

    public List<ShipmentReturn> all() {
        return repository.findAllByOrderByShipmentReturnIdDesc();
    }

    public ShipmentReturn get(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new OrderException("반품지를 찾을 수 없습니다."));
    }

    @Transactional
    public ShipmentReturn create(Long sellerId, String addressName, String name, String telephoneNumber,
                                 String zipcode, String address, String addressDetail, boolean isDefault) {
        require(sellerId, addressName, zipcode);
        ShipmentReturn s = new ShipmentReturn();
        s.setSellerId(sellerId);
        s.setCreatedDate(LocalDateTime.now().format(CREATED));
        apply(s, addressName, name, telephoneNumber, zipcode, address, addressDetail);
        // 첫 주소는 자동으로 기본, 아니면 요청대로
        boolean makeDefault = isDefault || bySeller(sellerId).isEmpty();
        s.setDefaultAddressFlag(makeDefault ? FLAG_Y : FLAG_N);
        ShipmentReturn saved = repository.save(s);
        if (makeDefault) {
            clearOtherDefaults(sellerId, saved.getShipmentReturnId());
        }
        return saved;
    }

    @Transactional
    public ShipmentReturn update(Integer id, String addressName, String name, String telephoneNumber,
                                 String zipcode, String address, String addressDetail, boolean isDefault) {
        ShipmentReturn s = get(id);
        require(s.getSellerId(), addressName, zipcode);
        apply(s, addressName, name, telephoneNumber, zipcode, address, addressDetail);
        if (isDefault) {
            s.setDefaultAddressFlag(FLAG_Y);
        }
        ShipmentReturn saved = repository.save(s);
        if (isDefault) {
            clearOtherDefaults(s.getSellerId(), id);
        }
        return saved;
    }

    @Transactional
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    /** 판매자당 기본 반품지는 하나 - 지정된 것 외 나머지는 N으로 내린다. */
    private void clearOtherDefaults(Long sellerId, Integer keepId) {
        for (ShipmentReturn other : bySeller(sellerId)) {
            if (!other.getShipmentReturnId().equals(keepId) && FLAG_Y.equals(other.getDefaultAddressFlag())) {
                other.setDefaultAddressFlag(FLAG_N);
                repository.save(other);
            }
        }
    }

    private void apply(ShipmentReturn s, String addressName, String name, String telephoneNumber,
                       String zipcode, String address, String addressDetail) {
        s.setAddressName(addressName.trim());
        s.setName(name);
        s.setTelephoneNumber(telephoneNumber);
        s.setZipcode(zipcode);
        s.setAddress(address);
        s.setAddressDetail(addressDetail);
    }

    private void require(Long sellerId, String addressName, String zipcode) {
        if (sellerId == null) {
            throw new OrderException("판매자를 지정해 주세요.");
        }
        if (addressName == null || addressName.isBlank()) {
            throw new OrderException("주소명을 입력해 주세요.");
        }
        if (zipcode == null || zipcode.isBlank()) {
            throw new OrderException("우편번호를 입력해 주세요.");
        }
    }
}

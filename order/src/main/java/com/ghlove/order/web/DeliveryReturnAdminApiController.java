package com.ghlove.order.web;

import com.ghlove.order.domain.DeliveryCompany;
import com.ghlove.order.domain.ShipmentReturn;
import com.ghlove.order.service.DeliveryCompanyService;
import com.ghlove.order.service.OrderException;
import com.ghlove.order.service.ShipmentReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 택배사 마스터 + 반송지 관리 API - admin 운영관리(택배사관리·반품지관리)가 호출한다.
 * 두 리소스 모두 ord 스키마 소유라 order 서비스가 데이터 주인이고, 편집 UI는 admin이 갖는다.
 * AdminApiAuthInterceptor(X-Internal-Secret)로 보호된다 - WebConfig에 경로 등록.
 */
@RestController
@RequiredArgsConstructor
public class DeliveryReturnAdminApiController {

    private final DeliveryCompanyService deliveryCompanyService;
    private final ShipmentReturnService shipmentReturnService;

    // ---- 택배사 마스터 ----

    public record DeliveryCompanyDto(Integer deliveryCompanyId, String deliveryCompanyName, String telNumber,
                                     String deliveryCompanyUrl, String sendFlag, String deliveryNumberParameter,
                                     String useFlag) {
        static DeliveryCompanyDto of(DeliveryCompany c) {
            return new DeliveryCompanyDto(c.getDeliveryCompanyId(), c.getDeliveryCompanyName(), c.getTelNumber(),
                    c.getDeliveryCompanyUrl(), c.getSendFlag(), c.getDeliveryNumberParameter(), c.getUseFlag());
        }
    }

    public record DeliveryCompanyForm(String deliveryCompanyName, String telNumber, String deliveryCompanyUrl,
                                      String sendFlag, String deliveryNumberParameter, String useFlag) {
    }

    @GetMapping("/api/admin/delivery-companies")
    public List<DeliveryCompanyDto> deliveryCompanies(@RequestParam(required = false) String useYn) {
        List<DeliveryCompany> list = "Y".equals(useYn) ? deliveryCompanyService.active() : deliveryCompanyService.all();
        return list.stream().map(DeliveryCompanyDto::of).toList();
    }

    @GetMapping("/api/admin/delivery-companies/{id}")
    public DeliveryCompanyDto deliveryCompany(@PathVariable Integer id) {
        return DeliveryCompanyDto.of(deliveryCompanyService.get(id));
    }

    @PostMapping("/api/admin/delivery-companies")
    public DeliveryCompanyDto createDeliveryCompany(@RequestBody DeliveryCompanyForm f) {
        try {
            return DeliveryCompanyDto.of(deliveryCompanyService.create(f.deliveryCompanyName(), f.telNumber(),
                    f.deliveryCompanyUrl(), f.sendFlag(), f.deliveryNumberParameter(), f.useFlag()));
        } catch (OrderException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/api/admin/delivery-companies/{id}")
    public DeliveryCompanyDto updateDeliveryCompany(@PathVariable Integer id, @RequestBody DeliveryCompanyForm f) {
        try {
            return DeliveryCompanyDto.of(deliveryCompanyService.update(id, f.deliveryCompanyName(), f.telNumber(),
                    f.deliveryCompanyUrl(), f.sendFlag(), f.deliveryNumberParameter(), f.useFlag()));
        } catch (OrderException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/api/admin/delivery-companies/{id}")
    public void deleteDeliveryCompany(@PathVariable Integer id) {
        deliveryCompanyService.delete(id);
    }

    // ---- 반송지(반품지) ----

    public record ShipmentReturnDto(Integer shipmentReturnId, Long sellerId, String addressName, String name,
                                    String telephoneNumber, String zipcode, String address, String addressDetail,
                                    String defaultAddressFlag, String createdDate) {
        static ShipmentReturnDto of(ShipmentReturn s) {
            return new ShipmentReturnDto(s.getShipmentReturnId(), s.getSellerId(), s.getAddressName(), s.getName(),
                    s.getTelephoneNumber(), s.getZipcode(), s.getAddress(), s.getAddressDetail(),
                    s.getDefaultAddressFlag(), s.getCreatedDate());
        }
    }

    public record ShipmentReturnForm(Long sellerId, String addressName, String name, String telephoneNumber,
                                     String zipcode, String address, String addressDetail, Boolean defaultAddress) {
    }

    @GetMapping("/api/admin/shipment-returns")
    public List<ShipmentReturnDto> shipmentReturns(@RequestParam(required = false) Long sellerId) {
        List<ShipmentReturn> list = sellerId != null ? shipmentReturnService.bySeller(sellerId) : shipmentReturnService.all();
        return list.stream().map(ShipmentReturnDto::of).toList();
    }

    @GetMapping("/api/admin/shipment-returns/{id}")
    public ShipmentReturnDto shipmentReturn(@PathVariable Integer id) {
        return ShipmentReturnDto.of(shipmentReturnService.get(id));
    }

    @PostMapping("/api/admin/shipment-returns")
    public ShipmentReturnDto createShipmentReturn(@RequestBody ShipmentReturnForm f) {
        try {
            return ShipmentReturnDto.of(shipmentReturnService.create(f.sellerId(), f.addressName(), f.name(),
                    f.telephoneNumber(), f.zipcode(), f.address(), f.addressDetail(), Boolean.TRUE.equals(f.defaultAddress())));
        } catch (OrderException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/api/admin/shipment-returns/{id}")
    public ShipmentReturnDto updateShipmentReturn(@PathVariable Integer id, @RequestBody ShipmentReturnForm f) {
        try {
            return ShipmentReturnDto.of(shipmentReturnService.update(id, f.addressName(), f.name(),
                    f.telephoneNumber(), f.zipcode(), f.address(), f.addressDetail(), Boolean.TRUE.equals(f.defaultAddress())));
        } catch (OrderException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/api/admin/shipment-returns/{id}")
    public void deleteShipmentReturn(@PathVariable Integer id) {
        shipmentReturnService.delete(id);
    }
}

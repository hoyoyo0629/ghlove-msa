package com.ghlove.gift.web;

import com.ghlove.gift.domain.Brand;
import com.ghlove.gift.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 브랜드관리 (AS-IS opmanager/brand) cross-service API - admin 콘솔이 사용. */
@RestController
@RequestMapping("/api/admin/brands")
@RequiredArgsConstructor
public class BrandApiController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final BrandRepository brandRepository;

    @GetMapping
    public List<Brand> list() {
        return brandRepository.findAllByOrderByBrandIdDesc();
    }

    @GetMapping("/{id}")
    public Brand get(@PathVariable Integer id) {
        return brandRepository.findById(id).orElseThrow();
    }

    @PostMapping
    public Brand create(@RequestBody Brand form) {
        form.setBrandId(null);
        form.setCreatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        form.setUpdatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        return brandRepository.save(form);
    }

    @PutMapping("/{id}")
    public Brand update(@PathVariable Integer id, @RequestBody Brand form) {
        Brand brand = brandRepository.findById(id).orElseThrow();
        brand.setBrandName(form.getBrandName());
        brand.setBrandContent(form.getBrandContent());
        brand.setDisplayFlag(form.getDisplayFlag());
        brand.setLocgovCode(form.getLocgovCode());
        brand.setUpdatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        return brandRepository.save(brand);
    }

    @PostMapping("/{id}/delete")
    public void delete(@PathVariable Integer id) {
        brandRepository.deleteById(id);
    }
}

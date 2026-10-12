package com.dacs.fashion.controller;

import com.dacs.fashion.dto.ProductVariantDTO;
import com.dacs.fashion.entity.ProductVariant;
import com.dacs.fashion.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.dacs.fashion.entity.User;

import java.util.List;

@RestController
@RequestMapping("/api/variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService variantService;

    @GetMapping
    public List<ProductVariant> getAll() {
        return variantService.getAll();
    }

    @GetMapping("/{id}")
    public ProductVariant getById(@PathVariable Long id) {
        return variantService.getById(id);
    }

    @GetMapping("/product/{productId}")
    public List<ProductVariant> getByProduct(@PathVariable Long productId) {
        return variantService.getByProduct(productId);
    }

    @PostMapping
    public ProductVariant create(@RequestBody ProductVariantDTO dto) {
        return variantService.create(dto);
    }

    @PutMapping("/{id}")
    public ProductVariant update(@PathVariable Long id,
                                @RequestBody ProductVariantDTO dto,
                                Authentication authentication) {
        User staff = (User) authentication.getPrincipal();

        return variantService.update(id, dto, staff.getFullname());
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        variantService.delete(id);
        return "Xóa biến thể thành công";
    }
}
package com.dacs.fashion.controller;

import com.dacs.fashion.dto.ProductImageDTO;
import com.dacs.fashion.entity.Product;
import com.dacs.fashion.entity.ProductImage;
import com.dacs.fashion.repository.ProductImageRepository;
import com.dacs.fashion.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    @GetMapping("/product/{productId}")
    public List<ProductImage> getByProduct(
            @PathVariable Long productId
    ) {
        return productImageRepository
                .findByProduct_ProductId(productId);
    }

    @PostMapping
    public ProductImage create(
            @RequestBody ProductImageDTO dto
    ) {
        Product product = productRepository
                .findById(dto.getProductId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy sản phẩm"
                        )
                );

        ProductImage image = new ProductImage();

        image.setProduct(product);
        image.setImageUrl(dto.getImageUrl());

        image.setColor(
                dto.getColor() == null ||
                dto.getColor().isBlank()
                        ? null
                        : dto.getColor().trim()
        );

        image.setIsMain(
                dto.getIsMain() != null
                        ? dto.getIsMain()
                        : true
        );

        return productImageRepository.save(image);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        productImageRepository.deleteById(id);
        return "Xóa ảnh thành công";
    }
}
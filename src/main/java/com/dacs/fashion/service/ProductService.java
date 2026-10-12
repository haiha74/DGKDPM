package com.dacs.fashion.service;

import com.dacs.fashion.dto.ProductDTO;
import com.dacs.fashion.entity.Brand;
import com.dacs.fashion.entity.Category;
import com.dacs.fashion.entity.Product;
import com.dacs.fashion.repository.BrandRepository;
import com.dacs.fashion.repository.CartItemRepository;
import com.dacs.fashion.repository.CategoryRepository;
import com.dacs.fashion.repository.OrderItemRepository;
import com.dacs.fashion.repository.ProductImageRepository;
import com.dacs.fashion.repository.ProductRepository;
import com.dacs.fashion.repository.ProductVariantRepository;
import com.dacs.fashion.repository.ProductViewRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductViewRepository productViewRepository;

    public List<Product> getAll() {
        return productRepository.findAll()
                .stream()
                .filter(p -> "ACTIVE".equals(p.getStatus()))
                .toList();
    }

    public List<Product> getAllForAdmin() {
        return productRepository.findAll();
    }

    public Product getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (!"ACTIVE".equals(product.getStatus())) {
            throw new RuntimeException("Sản phẩm không khả dụng");
        }

        return product;
    }

    public List<Product> search(String keyword) {
        return productRepository.findByProductNameContaining(keyword)
                .stream()
                .filter(p -> "ACTIVE".equals(p.getStatus()))
                .toList();
    }

    public List<Product> getByCategory(Long categoryId) {
        return productRepository.findByCategory_CategoryId(categoryId)
                .stream()
                .filter(p -> "ACTIVE".equals(p.getStatus()))
                .toList();
    }

    public Product create(ProductDTO dto) {
        Product product = new Product();

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        product.setCategory(category);

        if (dto.getBrandId() != null) {
            Brand brand = brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));
            product.setBrand(brand);
        }

        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setBasePrice(dto.getBasePrice());
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");

        return productRepository.save(product);
    }

    public Product update(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        product.setCategory(category);

        if (dto.getBrandId() != null) {
            Brand brand = brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));
            product.setBrand(brand);
        } else {
            product.setBrand(null);
        }

        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setBasePrice(dto.getBasePrice());
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : product.getStatus());

        return productRepository.save(product);
    }

    @Transactional

    public Long getSoldCount(Long productId) {
        Long count = productRepository.getSoldCountByProductId(productId);
        return count != null ? count : 0L;
    }

    public void delete(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (orderItemRepository.existsByVariant_Product_ProductId(id)) {
            throw new RuntimeException("Sản phẩm đã phát sinh đơn hàng, chỉ có thể ẩn");
        }

        cartItemRepository.deleteByProductId(id);
        productImageRepository.deleteByProductId(id);
        productViewRepository.deleteByProductId(id);
        productVariantRepository.deleteByProductId(id);
        productRepository.deleteProductByIdNative(id);
    }
}
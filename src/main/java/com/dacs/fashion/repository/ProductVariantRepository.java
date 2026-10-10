package com.dacs.fashion.repository;

import com.dacs.fashion.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProduct_ProductId(Long productId);

    @Modifying
    @Query(value = "DELETE FROM product_variants WHERE product_id = ?1", nativeQuery = true)
    void deleteByProductId(Long productId);
}
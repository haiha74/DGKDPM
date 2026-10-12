package com.dacs.fashion.repository;

import com.dacs.fashion.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByOrderByCreatedAtDesc();

    List<Review> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByOrderItem_OrderItemId(Long orderItemId);
}
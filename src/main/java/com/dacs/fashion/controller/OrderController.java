package com.dacs.fashion.controller;

import com.dacs.fashion.dto.CheckoutDTO;
import com.dacs.fashion.dto.CreateOrderDTO;
import com.dacs.fashion.entity.Order;
import com.dacs.fashion.entity.User;
import com.dacs.fashion.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(orderService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getByUser(@PathVariable Long userId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        if (!currentUser.getUserId().equals(userId)
                && !"ADMIN".equals(currentUser.getRole())
                && !"STAFF".equals(currentUser.getRole())) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Không có quyền xem đơn hàng này"));
        }

        return ResponseEntity.ok(orderService.getByUser(userId));
    }

    @PostMapping("/from-cart")
    public ResponseEntity<?> createFromCart(@RequestBody CreateOrderDTO dto,
                                            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        dto.setUserId(currentUser.getUserId());

        try {
            return ResponseEntity.ok(orderService.createFromCart(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestParam String status,
                                          Authentication authentication) {
        User staff = (User) authentication.getPrincipal();

        try {
            return ResponseEntity.ok(
                    orderService.updateStatus(id, status, staff.getFullname())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutDTO dto,
                                      Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        dto.setUserId(currentUser.getUserId());

        try {
            return ResponseEntity.ok(orderService.checkout(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/paid")
    public ResponseEntity<?> markPaid(@PathVariable Long id,
                                      Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Order order = orderService.getById(id);

        if (!order.getUser().getUserId().equals(currentUser.getUserId())
                && !"ADMIN".equals(currentUser.getRole())
                && !"STAFF".equals(currentUser.getRole())) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Không có quyền cập nhật đơn này"));
        }

        return ResponseEntity.ok(orderService.markPaid(id));
    }
}
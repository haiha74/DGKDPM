package com.dacs.fashion.controller;

import com.dacs.fashion.entity.Voucher;
import com.dacs.fashion.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherRepository voucherRepository;

    @GetMapping
    public ResponseEntity<?> getAll(){
        return ResponseEntity.ok(voucherRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Voucher voucher){
        return ResponseEntity.ok(voucherRepository.save(voucher));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody Voucher data){

        Voucher v = voucherRepository.findById(id)
                .orElseThrow();

        v.setCode(data.getCode());
        v.setDiscountType(data.getDiscountType());
        v.setDiscountValue(data.getDiscountValue());
        v.setEndDate(data.getEndDate());
        v.setMinOrderValue(data.getMinOrderValue());
        v.setStatus(data.getStatus());

        return ResponseEntity.ok(voucherRepository.save(v));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        voucherRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
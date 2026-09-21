package com.ridealongug.backend.services.coupon;

import com.alibaba.fastjson2.JSONObject;
import com.ridealongug.backend.models.database.DiscountCouponModel;
import com.ridealongug.backend.repositories.DiscountCouponRepository;
import com.ridealongug.backend.services.base.BaseWebActionsService;
import com.ridealongug.backend.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiscountCouponService extends BaseWebActionsService {

    private final DiscountCouponRepository discountCouponRepository;

    private OperationReturnObject createCoupon(JSONObject request) {
        requiresAuth();
        can("CAN_MANAGE_COUPONS", null);

        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("code");
        requiredFields.add("discount_percentage");
        requiredFields.add("valid_from");
        requiredFields.add("valid_to");
        requires(requiredFields, request);

        DiscountCouponModel coupon = DiscountCouponModel.builder()
                .code(request.getString("code").toUpperCase())
                .discountPercentage(request.getDouble("discount_percentage"))
                .validFrom(request.getObject("valid_from", LocalDate.class))
                .validTo(request.getObject("valid_to", LocalDate.class))
                .maxUses(request.getInteger("max_uses"))
                .timesUsed(0)
                .isActive(true)
                .build();

        DiscountCouponModel saved = discountCouponRepository.save(coupon);
        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Coupon created", saved);
        return res;
    }



    public double validateAndConsume(String code) {
        DiscountCouponModel coupon = discountCouponRepository.findFirstByCodeAndIsActiveTrue(code.toUpperCase())
                .orElseThrow(() -> new IllegalStateException("Invalid or inactive coupon code"));

        LocalDate today = LocalDate.now();
        if (today.isBefore(coupon.getValidFrom()) || today.isAfter(coupon.getValidTo())) {
            throw new IllegalStateException("Coupon is not valid at this time");
        }
        if (coupon.getMaxUses() != null && coupon.getTimesUsed() >= coupon.getMaxUses()) {
            throw new IllegalStateException("Coupon has reached its usage limit");
        }

        coupon.setTimesUsed(coupon.getTimesUsed() + 1);
        discountCouponRepository.save(coupon);
        return coupon.getDiscountPercentage();
    }



    public double peekDiscount(String code) {
        DiscountCouponModel coupon = discountCouponRepository.findFirstByCodeAndIsActiveTrue(code.toUpperCase())
                .orElseThrow(() -> new IllegalStateException("Invalid or inactive coupon code"));

        LocalDate today = LocalDate.now();
        if (today.isBefore(coupon.getValidFrom()) || today.isAfter(coupon.getValidTo())) {
            throw new IllegalStateException("Coupon is not valid at this time");
        }
        if (coupon.getMaxUses() != null && coupon.getTimesUsed() >= coupon.getMaxUses()) {
            throw new IllegalStateException("Coupon has reached its usage limit");
        }
        return coupon.getDiscountPercentage();
    }

    @Override
    public OperationReturnObject switchActions(String action, JSONObject request) {
        return switch (action) {
            case "createCoupon" -> createCoupon(request);
            default -> throw new IllegalArgumentException("Action " + action + " not known in this context");
        };
    }
}

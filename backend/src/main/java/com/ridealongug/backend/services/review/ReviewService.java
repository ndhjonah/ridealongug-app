package com.ridealongug.backend.services.review;

import com.alibaba.fastjson2.JSONObject;
import com.ridealongug.backend.models.database.BookingModel;
import com.ridealongug.backend.models.database.ReviewModel;
import com.ridealongug.backend.models.enums.BookingStatus;
import com.ridealongug.backend.repositories.BookingRepository;
import com.ridealongug.backend.repositories.ReviewRepository;
import com.ridealongug.backend.services.base.BaseWebActionsService;
import com.ridealongug.backend.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService extends BaseWebActionsService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    private OperationReturnObject leaveReview(JSONObject request) {
        requiresAuth();
        can("CAN_LEAVE_REVIEW", null);

        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("booking_id");
        requiredFields.add("vehicle_rating");
        requires(requiredFields, request);

        Long bookingId = request.getLong("booking_id");
        Long customerId = authenticatedUser().getId();

        BookingModel booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        if (!booking.getCustomerId().equals(customerId)) {
            throw new IllegalStateException("You can only review your own bookings");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalStateException("You can only review a completed booking");
        }
        if (reviewRepository.findFirstByBookingId(bookingId).isPresent()) {
            throw new IllegalStateException("This booking has already been reviewed");
        }

        ReviewModel review = ReviewModel.builder()
                .bookingId(bookingId)
                .customerId(customerId)
                .vehicleRating(request.getInteger("vehicle_rating"))
                .driverRating(request.getInteger("driver_rating"))
                .comment(request.getString("comment"))
                .build();

        ReviewModel saved = reviewRepository.save(review);
        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Thank you for your review", saved);
        return res;
    }

    private OperationReturnObject myReviews() {
        requiresAuth();
        Long customerId = authenticatedUser().getId();
        List<ReviewModel> reviews = reviewRepository.findAllByCustomerId(customerId);
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, reviews);
        return res;
    }

    private OperationReturnObject allReviews() {
        requiresAuth();
        can("CAN_VIEW_ALL_BOOKINGS", null);
        List<ReviewModel> reviews = reviewRepository.findAll();
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, reviews);
        return res;
    }

    @Override
    public OperationReturnObject switchActions(String action, JSONObject request) {
        return switch (action) {
            case "leaveReview" -> leaveReview(request);
            case "myReviews" -> myReviews();
            case "allReviews" -> allReviews();
            default -> throw new IllegalArgumentException("Action " + action + " not known in this context");
        };
    }
}

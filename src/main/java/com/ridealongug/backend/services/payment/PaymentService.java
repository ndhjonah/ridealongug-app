package com.ridealongug.backend.services.payment;

import com.alibaba.fastjson2.JSONObject;
import com.ridealongug.backend.models.database.BookingModel;
import com.ridealongug.backend.models.database.PaymentModel;
import com.ridealongug.backend.models.database.VehicleModel;
import com.ridealongug.backend.models.enums.BookingStatus;
import com.ridealongug.backend.models.enums.NotificationType;
import com.ridealongug.backend.models.enums.PaymentStatus;
import com.ridealongug.backend.repositories.BookingRepository;
import com.ridealongug.backend.repositories.PaymentRepository;
import com.ridealongug.backend.repositories.VehicleRepository;
import com.ridealongug.backend.services.notification.NotificationService;
import com.ridealongug.backend.services.base.BaseWebActionsService;
import com.ridealongug.backend.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService extends BaseWebActionsService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final NotificationService notificationService;



    private OperationReturnObject makePayment(JSONObject request) {
        requiresAuth();

        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("booking_id");
        requiredFields.add("amount");
        requiredFields.add("payment_method");
        requires(requiredFields, request);

        Long bookingId = request.getLong("booking_id");
        BookingModel booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        if (!booking.getCustomerId().equals(authenticatedUser().getId())) {
            throw new IllegalStateException("You can only pay for your own bookings");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("This booking is not awaiting payment (current status: " + booking.getStatus() + ")");
        }

        PaymentModel payment = PaymentModel.builder()
                .bookingId(bookingId)
                .amount(request.getObject("amount", BigDecimal.class))
                .paymentMethod(request.getObject("payment_method", com.ridealongug.backend.models.enums.PaymentMethod.class))
                .paymentStatus(PaymentStatus.COMPLETED)
                .transactionRef(request.getString("transaction_ref"))
                .paidAt(new Timestamp(System.currentTimeMillis()))
                .build();

        PaymentModel savedPayment = paymentRepository.save(payment);

        booking.setStatus(BookingStatus.PICKUP_PENDING);
        BookingModel savedBooking = bookingRepository.save(booking);

        notificationService.notify(booking.getCustomerId(), "Payment Received",
                "We received your payment of " + payment.getAmount() + " for booking #" + bookingId
                        + ". Please go pick up the vehicle - the owner will confirm handover before you can start your trip.",
                NotificationType.PAYMENT_RECEIVED);




        vehicleRepository.findById(booking.getVehicleId()).ifPresent((VehicleModel vehicle) -> {
            if (vehicle.getOwnerId() != null) {
                notificationService.notify(vehicle.getOwnerId(), "Customer Ready for Pickup",
                        "A customer has paid for booking #" + bookingId + " on your vehicle \"" + vehicle.getMake()
                                + " " + vehicle.getModel() + "\" and is coming to collect it. Please confirm pickup once handed over.",
                        NotificationType.BOOKING_CONFIRMED);
            }
        });

        JSONObject result = new JSONObject();
        result.put("payment", savedPayment);
        result.put("booking", savedBooking);

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Payment recorded - awaiting owner pickup confirmation", result);
        return res;
    }

    private OperationReturnObject paymentsForBooking(JSONObject request) {
        requires("booking_id", request);
        List<PaymentModel> payments = paymentRepository.findAllByBookingId(request.getLong("booking_id"));
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, payments);
        return res;
    }

    @Override
    public OperationReturnObject switchActions(String action, JSONObject request) {
        return switch (action) {
            case "makePayment" -> makePayment(request);
            case "paymentsForBooking" -> paymentsForBooking(request);
            default -> throw new IllegalArgumentException("Action " + action + " not known in this context");
        };
    }
}

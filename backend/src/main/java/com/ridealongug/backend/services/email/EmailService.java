package com.ridealongug.backend.services.email;

import com.ridealongug.backend.models.database.BookingModel;
import com.ridealongug.backend.models.database.SystemUserModel;
import com.ridealongug.backend.models.database.VehicleModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@ridealongug.com}")
    private String fromAddress;

    @Value("${app.name:RideAlongUG}")
    private String appName;

    private void send(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(fromAddress, appName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    public void sendBookingConfirmedEmail(SystemUserModel customer, VehicleModel vehicle, BookingModel booking) {
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            return;
        }
        SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy");
        String subject = appName + " - Booking Confirmed (#" + booking.getId() + ")";
        String body = "Hi " + customer.getFirstName() + ",\n\n"
                + "Your booking has been confirmed. Here are the details:\n\n"
                + "Vehicle: " + vehicle.getMake() + " " + vehicle.getModel() + " (" + vehicle.getPlateNumber() + ")\n"
                + "Pickup: " + booking.getPickupLocation() + "\n"
                + "Dropoff: " + booking.getDropoffLocation() + "\n"
                + "From: " + fmt.format(booking.getStartDate()) + "\n"
                + "To: " + fmt.format(booking.getEndDate()) + "\n"
                + "Total cost: " + booking.getTotalCost() + " UGX\n\n"
                + "Thank you for choosing " + appName + ".\n";
        send(customer.getEmail(), subject, body);
    }

    public void sendPasswordResetEmail(SystemUserModel user, String resetToken, int expiryMinutes) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }
        String subject = appName + " - Password Reset Code";
        String body = "Hi " + user.getFirstName() + ",\n\n"
                + "You (or someone else) requested a password reset for your " + appName + " account.\n\n"
                + "Your reset code is: " + resetToken + "\n\n"
                + "This code expires in " + expiryMinutes + " minutes. If you didn't request this, "
                + "you can safely ignore this email - your password will not be changed.\n";
        send(user.getEmail(), subject, body);
    }
}

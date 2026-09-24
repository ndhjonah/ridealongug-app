package com.ridealongug.backend.services.auth;

import com.alibaba.fastjson2.JSONObject;
import com.ridealongug.backend.config.ApplicationConf;
import com.ridealongug.backend.config.JwtUtility;
import com.ridealongug.backend.models.database.PasswordResetTokenModel;
import com.ridealongug.backend.models.database.SystemUserModel;
import com.ridealongug.backend.repositories.PasswordResetTokenRepository;
import com.ridealongug.backend.repositories.SystemUserRepository;
import com.ridealongug.backend.services.base.BaseWebActionsService;
import com.ridealongug.backend.services.email.EmailService;
import com.ridealongug.backend.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthService extends BaseWebActionsService {

    private static final int RESET_TOKEN_VALIDITY_MINUTES = 30;

    private final AuthenticationManager authenticationManager;
    private final ApplicationConf userDetailService;
    private final JwtUtility jwtUtility;
    private final SystemUserRepository systemUserRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private OperationReturnObject login(JSONObject request) {
        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("username");
        requiredFields.add("password");
        requires(requiredFields, request);

        String username = request.getString("username");
        String password = request.getString("password");

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        final SystemUserModel userDetails = userDetailService.loadUserByUsername(username);
        final String token = jwtUtility.generateToken(userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userDetails);

        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnMessage(0, "Welcome back " + userDetails.getUsername());
        res.setReturnObject(response);
        return res;
    }

    private OperationReturnObject forgotPassword(JSONObject request) {
        requires("email", request);
        String email = request.getString("email");

        Optional<SystemUserModel> userOpt = systemUserRepository.findFirstByUsernameOrEmail(email, email);
        if (userOpt.isPresent()) {
            SystemUserModel user = userOpt.get();
            String token = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            long expiresAtMillis = System.currentTimeMillis() + (RESET_TOKEN_VALIDITY_MINUTES * 60_000L);

            PasswordResetTokenModel resetToken = PasswordResetTokenModel.builder()
                    .userId(user.getId())
                    .token(token)
                    .expiresAt(new Timestamp(expiresAtMillis))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(user, token, RESET_TOKEN_VALIDITY_MINUTES);
        }

        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnMessage(0, "If an account with that email exists, a reset code has been sent");
        return res;
    }

    private OperationReturnObject resetPassword(JSONObject request) {
        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("token");
        requiredFields.add("new_password");
        requires(requiredFields, request);

        String token = request.getString("token");
        String newPassword = request.getString("new_password");

        PasswordResetTokenModel resetToken = passwordResetTokenRepository.findFirstByToken(token)
                .orElseThrow(() -> new IllegalStateException("Invalid or expired reset code"));

        if (Boolean.TRUE.equals(resetToken.getUsed())) {
            throw new IllegalStateException("This reset code has already been used");
        }
        if (resetToken.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            throw new IllegalStateException("This reset code has expired");
        }

        SystemUserModel user = systemUserRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        systemUserRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnMessage(0, "Password reset successfully - you can now log in with your new password");
        return res;
    }

    @Override
    public OperationReturnObject switchActions(String action, JSONObject request) {
        return switch (action) {
            case "login" -> login(request);
            case "forgotPassword" -> forgotPassword(request);
            case "resetPassword" -> resetPassword(request);
            default -> throw new IllegalArgumentException("Action " + action + " not known in this context");
        };
    }
}
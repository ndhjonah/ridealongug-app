package com.ridealongug.backend.services.user;

import com.alibaba.fastjson2.JSONObject;
import com.ridealongug.backend.models.database.SystemUserModel;
import com.ridealongug.backend.repositories.SystemUserRepository;
import com.ridealongug.backend.services.base.BaseWebActionsService;
import com.ridealongug.backend.utils.OperationReturnObject;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class SystemUserModelService extends BaseWebActionsService {

    private SystemUserRepository systemUserRepository;
    private PasswordEncoder passwordEncoder;

    private static final java.util.regex.Pattern EMAIL_PATTERN =
            java.util.regex.Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final java.util.regex.Pattern PHONE_PATTERN = java.util.regex.Pattern.compile("^\\d{9,10}$");
    private static final java.util.regex.Pattern USERNAME_PATTERN = java.util.regex.Pattern.compile("^[A-Za-z0-9_.]{3,30}$");

    public OperationReturnObject registerUser(String user) {
        SystemUserModel systemUserModel = JSONObject.parseObject(user, SystemUserModel.class);


        String requestedRole = systemUserModel.getRoleCode();
        if (requestedRole == null
                || (!requestedRole.equals("CUSTOMER")
                && !requestedRole.equals("DRIVER")
                && !requestedRole.equals("VEHICLE_OWNER"))) {
            throw new IllegalArgumentException("Invalid role for self-registration");
        }

        validateContactFields(systemUserModel.getUsername(), systemUserModel.getEmail(), systemUserModel.getPhoneNumber());

        if (systemUserRepository.findFirstByUsernameOrEmail(
                systemUserModel.getUsername(), systemUserModel.getEmail()).isPresent()) {
            throw new IllegalStateException("A user with that username or email already exists");
        }

        systemUserModel.setPassword(passwordEncoder.encode(systemUserModel.getPassword()));
        systemUserModel.setIsActive(true);

        OperationReturnObject operationReturnObject = new OperationReturnObject();
        operationReturnObject.setReturnCodeAndReturnObject(0, scrub(systemUserRepository.save(systemUserModel)));
        return operationReturnObject;
    }



    private OperationReturnObject createAdminUser(JSONObject request) {
        requiresAuth();
        hasRole("ADMINISTRATOR");

        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("first_name");
        requiredFields.add("last_name");
        requiredFields.add("username");
        requiredFields.add("email");
        requiredFields.add("password");
        requires(requiredFields, request);

        validateContactFields(request.getString("username"), request.getString("email"), request.getString("phone_number"));

        if (systemUserRepository.findFirstByUsernameOrEmail(
                request.getString("username"), request.getString("email")).isPresent()) {
            throw new IllegalStateException("A user with that username or email already exists");
        }

        SystemUserModel admin = SystemUserModel.builder()
                .firstName(request.getString("first_name"))
                .lastName(request.getString("last_name"))
                .username(request.getString("username"))
                .email(request.getString("email"))
                .phoneNumber(request.getString("phone_number"))
                .password(passwordEncoder.encode(request.getString("password")))
                .roleCode("ADMINISTRATOR")
                .isActive(true)
                .build();

        SystemUserModel saved = systemUserRepository.save(admin);
        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Admin user created successfully", scrub(saved));
        return res;
    }


    private OperationReturnObject myProfile() {
        requiresAuth();
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, scrub(authenticatedUser()));
        return res;
    }



    private OperationReturnObject updateMyProfile(JSONObject request) {
        requiresAuth();
        SystemUserModel user = authenticatedUser();

        if (request.containsKey("first_name")) user.setFirstName(request.getString("first_name"));
        if (request.containsKey("last_name")) user.setLastName(request.getString("last_name"));

        if (request.containsKey("email")) {
            String newEmail = request.getString("email");
            validateContactFields(null, newEmail, null);
            systemUserRepository.findFirstByUsernameOrEmail(null, newEmail)
                    .filter(existing -> !existing.getId().equals(user.getId()))
                    .ifPresent(existing -> { throw new IllegalStateException("That email is already in use by another account"); });
            user.setEmail(newEmail);
        }
        if (request.containsKey("phone_number")) {
            String newPhone = request.getString("phone_number");
            validateContactFields(null, null, newPhone);
            user.setPhoneNumber(newPhone);
        }

        SystemUserModel saved = systemUserRepository.save(user);
        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Profile updated successfully", scrub(saved));
        return res;
    }



    private OperationReturnObject deactivateMyAccount() {
        requiresAuth();
        SystemUserModel user = authenticatedUser();
        user.setIsActive(false);
        systemUserRepository.save(user);

        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnMessage(0, "Your account has been deactivated");
        return res;
    }


    private OperationReturnObject listAllUsers(JSONObject request) {
        requiresAuth();
        can("CAN_MANAGE_USERS", null);

        String roleFilter = request.getString("role_code");
        List<SystemUserModel> users = systemUserRepository.findAll();
        if (roleFilter != null && !roleFilter.isBlank()) {
            users.removeIf(u -> !roleFilter.equals(u.getRoleCode()));
        }
        users.forEach(this::scrub);

        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, users);
        return res;
    }


    private OperationReturnObject deleteUser(JSONObject request) {
        requiresAuth();
        can("CAN_MANAGE_USERS", null);
        requires("id", request);

        Long targetId = request.getLong("id");
        SystemUserModel target = systemUserRepository.findById(targetId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (targetId.equals(authenticatedUser().getId())) {
            throw new IllegalStateException("You cannot delete your own account this way - use deactivateMyAccount instead");
        }

        systemUserRepository.delete(target);
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnMessage(0, "User deleted permanently");
        return res;
    }


    private void validateContactFields(String username, String email, String phoneNumber) {
        if (username != null && !USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Username must be 3-30 characters: letters, numbers, dots or underscores only");
        }
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Please enter a valid email address");
        }
        if (phoneNumber != null && !phoneNumber.isBlank() && !PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("Phone number must be 9 to 10 digits, numbers only");
        }
    }


    private SystemUserModel scrub(SystemUserModel user) {
        user.setPassword(null);
        return user;
    }

    @Override
    public OperationReturnObject switchActions(String action, JSONObject request) {
        return switch (action) {
            case "registerUser" -> registerUser(request.getString("User"));
            case "createAdminUser" -> createAdminUser(request);
            case "myProfile" -> myProfile();
            case "updateMyProfile" -> updateMyProfile(request);
            case "deactivateMyAccount" -> deactivateMyAccount();
            case "listAllUsers" -> listAllUsers(request);
            case "deleteUser" -> deleteUser(request);
            default -> throw new IllegalStateException("Unexpected value:" + action);
        };
    }
}

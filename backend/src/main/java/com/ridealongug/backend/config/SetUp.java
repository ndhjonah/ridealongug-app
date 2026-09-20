package com.ridealongug.backend.config;

import com.ridealongug.backend.models.database.*;
import com.ridealongug.backend.models.jpahelpers.enums.AppDomains;
import com.ridealongug.backend.permissions.Permission;
import com.ridealongug.backend.permissions.Permisions;
import com.ridealongug.backend.repositories.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetUp {

    @Value("${USE_DOMAINS:true}")
    Boolean useDomains;

    @Value("${ADMIN_ROLE_NAME}")
    String adminRoleName;

    @Value("${ADMIN_ROLE_DOMAIN}")
    AppDomains adminDomain;

    @Value("${BOOTSTRAP_ADMIN_USERNAME:admin}")
    String bootstrapAdminUsername;

    @Value("${BOOTSTRAP_ADMIN_PASSWORD:ChangeMe123!}")
    String bootstrapAdminPassword;

    @Value("${BOOTSTRAP_ADMIN_EMAIL:admin@ridealongug.com}")
    String bootstrapAdminEmail;

    private final SystemDomainRepository domainRepository;
    private final SystemPermissionRepository permissionRepository;
    private final SystemRolePermissionRepository permissionAssignmentRepository;
    private final SystemRoleRepository roleRepository;
    private final SystemUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    protected void setupDomains() {
        if (Boolean.TRUE.equals(useDomains)) {
            log.info("Domains supported, setting them up.");
            domainRepository.deleteAll();
            for (AppDomains domain : AppDomains.values()) {
                log.info("Adding {} domain", domain.name());
                var md = SystemDomainModel.builder();
                md.domainName(String.valueOf(domain));
                domainRepository.save(md.build());
            }
        } else {
            log.info("Domains are currently inactive.");
        }
    }

    @PostConstruct
    public void setupPermissions() {
        permissionRepository.deleteAll();
        Permisions obj = new Permisions();
        ReflectionUtils.doWithFields(obj.getClass(), field -> {
            field.setAccessible(true);
            Permission perm = (Permission) field.get(obj);
            SystemPermissionModel permissionsModel = new SystemPermissionModel();
            permissionsModel.setPermissionCode(perm.getCode());
            permissionsModel.setPermissionName(perm.getName());
            if (Boolean.TRUE.equals(useDomains)) {
                permissionsModel.setPermissionDomain(perm.getDomain());
            }
            permissionRepository.save(permissionsModel);
        });
        log.info("Permissions setup successfully");

        setupAdministratorRole();
        setupCustomerRole();
        setupDriverRole();
        setupVehicleOwnerRole();

        setUpAdminPerms();
        bootstrapSuperAdminUser();
    }

    private void setupAdministratorRole() {
        Optional<SystemRoleModel> checkIfAdminRoleExists = roleRepository.findFirstByRoleCode("ADMINISTRATOR");
        if (checkIfAdminRoleExists.isEmpty()) {
            var adminRole = SystemRoleModel.builder();
            adminRole.roleName("Administrator");
            if (Boolean.TRUE.equals(useDomains)) {
                if (StringUtils.isBlank(adminRoleName)) {
                    adminRoleName = "ADMINISTRATOR";
                }
                adminRole.roleCode(adminRoleName);
                if (adminDomain == null) {
                    throw new IllegalStateException("Please define the domain enum String to be used for administrators");
                }
                adminRole.roleDomain(adminDomain);
            }
            roleRepository.save(adminRole.build());
        }
        Optional<SystemRolePermissionAssignmentModel> assignmentModel =
                permissionAssignmentRepository.findFirstByRoleCodeAndPermissionCode("ADMINISTRATOR", "ADMINISTRATOR");
        if (assignmentModel.isEmpty()) {
            var assignment = SystemRolePermissionAssignmentModel.builder();
            assignment.permissionCode("ADMINISTRATOR");
            assignment.roleCode(adminRoleName);
            permissionAssignmentRepository.save(assignment.build());
        }
    }



    private void bootstrapSuperAdminUser() {
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(u -> "ADMINISTRATOR".equals(u.getRoleCode()));
        if (adminExists) {
            return;
        }
        SystemUserModel admin = SystemUserModel.builder()
                .firstName("Super")
                .lastName("Admin")
                .username(bootstrapAdminUsername)
                .email(bootstrapAdminEmail)
                .password(passwordEncoder.encode(bootstrapAdminPassword))
                .roleCode("ADMINISTRATOR")
                .isActive(true)
                .build();
        userRepository.save(admin);
        log.warn("Bootstrap Super Admin created - username: '{}'. Change the default password after first login.",
                bootstrapAdminUsername);
    }

    private void setupCustomerRole() {
        createRoleIfMissing("CUSTOMER", "Customer", AppDomains.CLIENT_SIDE);
        assignPermission("CUSTOMER", "CAN_BOOK_VEHICLE");
        assignPermission("CUSTOMER", "CAN_MANAGE_OWN_BOOKINGS");
        assignPermission("CUSTOMER", "CAN_SUBMIT_LICENCE");
        assignPermission("CUSTOMER", "CAN_LEAVE_REVIEW");
    }

    private void setupDriverRole() {
        createRoleIfMissing("DRIVER", "Driver", AppDomains.CLIENT_SIDE);
        assignPermission("DRIVER", "CAN_ACCEPT_TRIPS");
        assignPermission("DRIVER", "CAN_UPDATE_AVAILABILITY");
    }



    private void setupVehicleOwnerRole() {
        createRoleIfMissing("VEHICLE_OWNER", "Vehicle Owner", AppDomains.CLIENT_SIDE);
        assignPermission("VEHICLE_OWNER", "CAN_SUBMIT_VEHICLE");
        assignPermission("VEHICLE_OWNER", "CAN_MANAGE_OWN_VEHICLES");
        assignPermission("VEHICLE_OWNER", "CAN_VIEW_OWN_EARNINGS");
    }

    private void createRoleIfMissing(String roleCode, String roleName, AppDomains domain) {
        Optional<SystemRoleModel> existing = roleRepository.findFirstByRoleCode(roleCode);
        if (existing.isEmpty()) {
            var role = SystemRoleModel.builder();
            role.roleName(roleName);
            role.roleCode(roleCode);
            if (Boolean.TRUE.equals(useDomains)) {
                role.roleDomain(domain);
            }
            roleRepository.save(role.build());
            log.info("Created {} role", roleCode);
        }
    }

    private void assignPermission(String roleCode, String permissionCode) {
        Optional<SystemRolePermissionAssignmentModel> assignmentModel =
                permissionAssignmentRepository.findFirstByRoleCodeAndPermissionCode(roleCode, permissionCode);
        if (assignmentModel.isEmpty()) {
            var assignment = SystemRolePermissionAssignmentModel.builder();
            assignment.permissionCode(permissionCode);
            assignment.roleCode(roleCode);
            permissionAssignmentRepository.save(assignment.build());
        }
    }



    private void setUpAdminPerms() {
        Permisions obj = new Permisions();
        ReflectionUtils.doWithFields(obj.getClass(), field -> {
            field.setAccessible(true);
            Permission perm = (Permission) field.get(obj);
            if (Boolean.TRUE.equals(perm.getShipWithAdmin())) {
                Optional<SystemRolePermissionAssignmentModel> assignmentModel =
                        permissionAssignmentRepository.findFirstByRoleCodeAndPermissionCode("ADMINISTRATOR", perm.getCode());
                if (assignmentModel.isEmpty()) {
                    var assignment = SystemRolePermissionAssignmentModel.builder();
                    assignment.permissionCode(perm.getCode());
                    assignment.roleCode(adminRoleName);
                    permissionAssignmentRepository.save(assignment.build());
                }
            }
        });
    }
}

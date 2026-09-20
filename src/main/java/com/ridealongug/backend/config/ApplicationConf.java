package com.ridealongug.backend.config;

import com.ridealongug.backend.models.database.SystemPermissionModel;
import com.ridealongug.backend.models.database.SystemRolePermissionAssignmentModel;
import com.ridealongug.backend.models.database.SystemUserModel;
import com.ridealongug.backend.repositories.SystemPermissionRepository;
import com.ridealongug.backend.repositories.SystemRolePermissionRepository;
import com.ridealongug.backend.repositories.SystemUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ApplicationConf implements UserDetailsService {

    private final SystemUserRepository userRepository;
    private final SystemRolePermissionRepository permissionAssignmentRepository;
    private final SystemPermissionRepository permissionRepository;

    @Override
    public SystemUserModel loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<SystemUserModel> usersModel = userRepository.findFirstByUsernameOrEmail(username, username);
        if (usersModel.isPresent()) {
            SystemUserModel user = usersModel.get();
            if (Boolean.FALSE.equals(user.getIsActive())) {
                throw new IllegalStateException("User account is not active");
            }
            Collection<SimpleGrantedAuthority> authorities = user.getAuthorities();
            Collection<SystemRolePermissionAssignmentModel> permissions =
                    permissionAssignmentRepository.findAllByRoleCode(user.getRoleCode());
            for (SystemRolePermissionAssignmentModel permissionAssignmentModel : permissions) {
                Optional<SystemPermissionModel> permissionsModel =
                        permissionRepository.findFirstByPermissionCode(permissionAssignmentModel.getPermissionCode());
                permissionsModel.ifPresent(permission ->
                        authorities.add(new SimpleGrantedAuthority(permission.getPermissionCode())));
            }
            user.setAuthorities(authorities);
            return user;
        } else {
            throw new IllegalStateException("User not found");
        }
    }
}

package com.ridealongug.backend.services.base;

import com.alibaba.fastjson2.JSONObject;
import com.ridealongug.backend.config.ApplicationConf;
import com.ridealongug.backend.models.database.SystemRoleModel;
import com.ridealongug.backend.models.database.SystemUserModel;
import com.ridealongug.backend.models.jpahelpers.enums.AppDomains;
import com.ridealongug.backend.repositories.SystemRoleRepository;
import com.ridealongug.backend.repositories.SystemUserRepository;
import com.ridealongug.backend.utils.OperationReturnObject;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public abstract class BaseWebActionsService implements BaseWebActionsImpl {

    @Autowired
    private SystemUserRepository userRepository;
    @Autowired
    private SystemRoleRepository roleRepository;
    @Autowired
    private ApplicationConf userDetailService;

    public OperationReturnObject process(String action, JSONObject payload) {
        return switchActions(action, payload);
    }

    public void requires(List<String> fields, JSONObject request) {
        for (String field : fields) {
            if (!request.containsKey(field) || request.get(field) == null) {
                throw new IllegalArgumentException(field.replace("_", " ") + " cannot be empty");
            }
        }
    }

    public Boolean requires(String field, JSONObject request) {
        if (!request.containsKey(field) || request.get(field) == null) {
            throw new IllegalArgumentException(field.replace("_", " ") + " cannot be empty");
        }
        return true;
    }

    public UserDetails getContextUserDetails() {
        return authenticatedUser();
    }

    public Boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return null != authentication
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public void requiresAuth() {
        if (Boolean.FALSE.equals(isAuthenticated())) {
            throw new IllegalArgumentException("AUTHENTICATION REQUIRED");
        }
    }

    public SystemUserModel authenticatedUser() {
        if (Boolean.TRUE.equals(isAuthenticated())) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();




            return userDetailService.loadUserByUsername(userDetails.getUsername());
        }
        throw new IllegalArgumentException("AUTHENTICATION REQUIRED");
    }

    public Boolean hasRole(String roleCode) {
        SystemUserModel usersModel = authenticatedUser();
        if (usersModel != null) {
            Optional<SystemRoleModel> rolesModel = roleRepository.findFirstByRoleCode(usersModel.getRoleCode());
            if (rolesModel.isPresent()) {
                SystemRoleModel role = rolesModel.get();
                return Objects.equals(role.getRoleCode(), roleCode);
            }
        }
        throw new IllegalStateException("USER HAS LESS PRIVILEGES");
    }

    public Boolean can(String permission, @Nullable String username) {
        UserDetails userDetails;
        if (username != null) {
            userDetails = userDetailService.loadUserByUsername(username);
        } else {
            userDetails = getContextUserDetails();
        }
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().matches(permission))) {
            return true;
        }
        throw new IllegalStateException("NOT AUTHORISED");
    }

    public Boolean canWithCustomError(String permission, @Nullable String username, @NonNull String error) {
        UserDetails userDetails;
        if (username != null) {
            userDetails = userDetailService.loadUserByUsername(username);
        } else {
            userDetails = getContextUserDetails();
        }
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().matches(permission))) {
            return true;
        }
        throw new IllegalStateException(error);
    }

    public List<String> userPerms(@Nullable String username) {
        List<String> perms = new ArrayList<>();
        UserDetails userDetails = getContextUserDetails();
        if (username != null) {
            userDetails = userDetailService.loadUserByUsername(username);
        }
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            perms.add(authority.getAuthority());
        }
        return perms;
    }

    public Boolean can(List<String> permissions, @Nullable String username) {
        UserDetails userDetails = getContextUserDetails();
        if (username != null) {
            userDetails = userDetailService.loadUserByUsername(username);
        }
        for (String permission : permissions) {
            if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().matches(permission))) {
                return true;
            }
        }
        throw new IllegalStateException("NOT AUTHORISED");
    }

    public SystemRoleModel getRole() {
        String roleCode = authenticatedUser().getRoleCode();
        Optional<SystemRoleModel> rolesModel = roleRepository.findFirstByRoleCode(roleCode);
        if (rolesModel.isEmpty()) {
            throw new IllegalStateException("UNKNOWN USER ROLE");
        }
        return rolesModel.get();
    }

    public void belongsTo(AppDomains domain) {
        if (getUserDomain() != domain) {
            throw new IllegalStateException("You have no access to the " + domain + " services");
        }
    }

    public AppDomains getUserDomain() {
        return getRole().getRoleDomain();
    }
}

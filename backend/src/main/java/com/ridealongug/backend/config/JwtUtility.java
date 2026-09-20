package com.ridealongug.backend.config;

import com.ridealongug.backend.models.database.SystemRoleModel;
import com.ridealongug.backend.models.database.SystemUserModel;
import com.ridealongug.backend.repositories.SystemRoleRepository;
import com.ridealongug.backend.repositories.SystemUserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;

@Service
@Data
public class JwtUtility {

    private final SystemUserRepository userRepository;
    private final SystemRoleRepository roleRepository;

    @Value("${secret}")
    private String secret;

    public static final long JWT_TOKEN_VALIDITY = 12 * 60 * 60;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) throws Exception {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token, username);
    }

    private boolean isTokenExpired(String token, String username) throws Exception {
        SystemUserModel usersModel = userRepository.findFirstByUsername(username);
        if (usersModel == null) {
            throw new IllegalStateException("User not found");
        } else if (extractExpiration(token).before(new Date())) {
            throw new Exception("SESSION EXPIRED");
        } else if (usersModel.getLastLoggedInAt() == null) {
            throw new IllegalStateException("INVALID TOKEN");
        } else if (extractIssuedAt(token).after(usersModel.getLastLoggedInAt())) {
            throw new Exception("EXPIRED TOKEN USED");
        } else if (!usersModel.getIsActive()) {
            throw new Exception("ACCOUNT INACTIVE");
        }
        return false;
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        long now = System.currentTimeMillis();
        Timestamp stamp = new Timestamp(now);

        SystemUserModel user = userRepository.findFirstByUsername(userDetails.getUsername());
        user.setLastLoggedInAt(stamp);
        userRepository.save(user);

        Optional<SystemRoleModel> rolesModel = roleRepository.findFirstByRoleCode(user.getRoleCode());
        rolesModel.ifPresentOrElse(
                model -> {
                    claims.put("role", model.getRoleName());
                    claims.put("role_code", model.getRoleCode());
                    claims.put("domain", model.getRoleDomain());
                },
                () -> {
                    throw new IllegalArgumentException("Invalid User Role");
                }
        );

        List<String> permissions = new ArrayList<>();
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            if (!permissions.contains(authority.getAuthority())) {
                permissions.add(authority.getAuthority());
            }
        }
        claims.put("permissions", permissions);

        return Jwts
                .builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + JWT_TOKEN_VALIDITY * 1000))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

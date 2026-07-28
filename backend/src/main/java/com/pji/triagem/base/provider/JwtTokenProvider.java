package com.pji.triagem.base.provider;

import com.pji.triagem.base.utils.AuthUtils;
import com.pji.triagem.dto.response.UserAuth;
import com.pji.triagem.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    private final Key signingKey;
    private final JwtParser parser;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret) {
        this.signingKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        this.parser = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build();
    }

    /**
     * Faz o parse e retorna os Claims do token.
     * Lança InvalidTokenException se for inválido ou expirado.
     */
    private Claims parseClaims(String token) {
        try {
            return parser.parseClaimsJws(token).getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Expired or invalid JWT token");
        }
    }

    public String createAccessToken(UserAuth userDetails, String name) {
        Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
        claims.put("id", userDetails.getId());
        claims.put("role", userDetails.getAuthorities().toString());
        claims.put("type", userDetails.getTypeUser());
        claims.put("login", userDetails.getLogin());
        claims.put("blocked_temporary", userDetails.getIsBlockedTemporary());
        claims.put("token_type", "access");
        claims.put("name", name);

        Date now = new Date();
        Date validity = new Date(now.getTime() + 3600000);
//        Date validity = new Date(now.getTime() + 20 * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(UserAuth userDetails, String name) {
        Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
        claims.put("id", userDetails.getId());
        claims.put("login", userDetails.getLogin());
        claims.put("type", userDetails.getTypeUser());
        claims.put("name", name);
        claims.put("token_type", "refresh");

        Date now = new Date();
        Date validity = new Date(now.getTime() + 604800000L); // 7 dias

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrai qualquer campo do token usando uma função.
     */
    private <T> T getClaim(String token, Function<Claims, T> extractor) {
        Claims claims = parseClaims(token);
        return extractor.apply(claims);
    }

    public Claims getClaims(String token) {
        return parser.parseClaimsJws(token).getBody();
    }

    public boolean validateToken(String token) {
        Date expiration = getClaim(token, Claims::getExpiration);
        return expiration != null && expiration.after(new Date());
    }

    public String extractUsername(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public String getLogin(String token) {
        return extractUsername(token); // alias
    }

    public List<String> extractRoles(String token) {
        return getClaim(token, claims -> {
            Object rolesObj = claims.get("role");
            if (rolesObj instanceof String s) {
                // caso venha como string única
                return List.of(s);
            }
            if (rolesObj instanceof Collection<?> c) {
                return c.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .toList();
            }
            return List.of();
        });
    }

    public String extractTypeToken(String token) {
        return getClaim(token, c -> c.get("token_type", String.class));
    }

    public String extractId(String token) {
        return getClaim(token, c -> String.valueOf(c.get("id")));
    }

    public Boolean isBlockedTemporary(String token) {
        return getClaim(token, c -> c.get("blocked_temporary", Boolean.class));
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AuthUtils.AUTH_HEADER);
        return (authHeader != null && authHeader.startsWith(AuthUtils.BEARER_PREFIX))
                ? authHeader.substring(7)
                : null;
    }
}

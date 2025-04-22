package com.scrapspringboot.security;

import com.scrapspringboot.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JWTService {


    private static final Logger log = LoggerFactory.getLogger(JWTService.class);

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;


    public String generateToken(Usuario usuario) {
        log.debug("Attempting to generate token for user: {}", usuario.getUsername());
        if (usuario == null || usuario.getUsername() == null || usuario.getId() == null || usuario.getRol() == null) {
            log.error("Cannot generate token: Usuario object or its critical fields (ID, Username, Rol) are null.");
            throw new IllegalArgumentException("Cannot generate token from incomplete user data.");
        }

        try {
            long now = System.currentTimeMillis();
            long expirationMillis = now + 1000 * 60 * 60 * 3;

            TokenDataDTO tokenDataDTO = TokenDataDTO
                    .builder()
                    .id(usuario.getId())
                    .username(usuario.getUsername())
                    .rol(usuario.getRol().name())
                    .fecha_creacion(now)
                    .fecha_expiracion(expirationMillis)
                    .build();
            log.debug("TokenDataDTO created for user {}: {}", usuario.getUsername(), tokenDataDTO);

            Key signingKey = getSignInKey();

            log.debug("Building JWT for user: {}", usuario.getUsername());
            String token = Jwts
                    .builder()
                    .claim("tokenDataDTO", tokenDataDTO)
                    .signWith(signingKey, SignatureAlgorithm.HS256)
                    .compact();

            log.info("Successfully generated token for user: {}", usuario.getUsername());
            return token;

        } catch (NullPointerException npe) {
            log.error("NullPointerException during token generation for user {}: Check if user.getRol() is null. Message: {}", usuario.getUsername(), npe.getMessage(), npe);
            throw new RuntimeException("Failed to generate JWT token due to null data.", npe);
        } catch (Exception e) {
            log.error("Failed to generate token for user {}: {}", usuario.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }


    private Claims extractDatosToken(String token) {
        log.debug("Attempting to extract claims from token.");
        try {
            Claims claims = Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            log.debug("Successfully extracted claims.");
            return claims;
        } catch (Exception e) {
            log.error("Failed to extract claims from token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse JWT token claims", e);
        }
    }

    public TokenDataDTO extractTokenData(String token) {
        log.debug("Attempting to extract TokenDataDTO from token.");
        Claims claims = extractDatosToken(token);

        if (!claims.containsKey("tokenDataDTO")) {
            log.error("Token is missing the 'tokenDataDTO' claim.");
            throw new IllegalArgumentException("Token data claim ('tokenDataDTO') is missing");
        }

        Object tokenDataObject = claims.get("tokenDataDTO");
        if (!(tokenDataObject instanceof Map)) {
            log.error("Claim 'tokenDataDTO' is not a Map. Actual type: {}", tokenDataObject.getClass().getName());
            throw new IllegalArgumentException("Token data claim ('tokenDataDTO') is not structured correctly.");
        }

        Map<String, Object> mapa = (Map<String, Object>) tokenDataObject;

        String username = (String) mapa.get("username");
        Number fechaCreacionNum = (Number) mapa.get("fecha_creacion");
        Number fechaExpiracionNum = (Number) mapa.get("fecha_expiracion");
        String rol = (String) mapa.get("rol");
        Number idNum = (Number) mapa.get("id");

        if (username == null || fechaCreacionNum == null || fechaExpiracionNum == null || rol == null || idNum == null) {
            log.error("Token data contains null values. Username: {}, Created: {}, Expires: {}, Rol: {}, ID: {}",
                    username, fechaCreacionNum, fechaExpiracionNum, rol, idNum);
            throw new IllegalArgumentException("Token data contains null or missing values within the 'tokenDataDTO' claim.");
        }

        try {
            TokenDataDTO extractedData = TokenDataDTO.builder()
                    .username(username)
                    .fecha_creacion(fechaCreacionNum.longValue())
                    .fecha_expiracion(fechaExpiracionNum.longValue())
                    .rol(rol)
                    .id(idNum.longValue())
                    .build();
            log.debug("Successfully extracted TokenDataDTO: {}", extractedData);
            return extractedData;
        } catch (Exception e) {
            log.error("Error building TokenDataDTO from extracted map data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to construct TokenDataDTO from claims", e);
        }
    }

    public boolean isExpired(String token) {
        log.debug("Checking if token is expired.");
        try {
            Date expirationDate = new Date(extractTokenData(token).getFecha_expiracion());
            boolean expired = expirationDate.before(new Date());
            log.debug("Token expiration date: {}. Is expired: {}", expirationDate, expired);
            return expired;
        } catch (Exception e) {
            log.error("Could not determine token expiration: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to determine token expiration", e);
        }
    }

    private Key getSignInKey() {
        log.debug("Attempting to get sign-in key.");
        if (secretKey == null || secretKey.trim().isEmpty()) {
            log.error("JWT Secret Key ('application.security.jwt.secret-key') is not configured!");
            throw new RuntimeException("JWT Secret Key is missing or empty.");
        }
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            log.debug("Secret key successfully decoded from Base64.");

            Key key = Keys.hmacShaKeyFor(keyBytes);
            log.debug("HMAC SHA Key created successfully.");
            return key;
        } catch (IllegalArgumentException e) {
            log.error("Failed to decode Base64 secret key: {}. Ensure the key is valid Base64.", e.getMessage(), e);
            throw new RuntimeException("Invalid Base64 format for JWT Secret Key", e);
        } catch (WeakKeyException wke) {
            log.error("The configured JWT secret key is too weak for HS256 algorithm: {}", wke.getMessage(), wke);
            throw new RuntimeException("JWT secret key is too weak for the selected algorithm.", wke);
        } catch (Exception e) {
            log.error("Failed to create signing key: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create signing key", e);
        }
    }
}
package com.scrapspringboot.security;

import com.scrapspringboot.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JWTService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    /**
     * Método para generar token de acceso a través de los datos
     * de un usuario
     *
     * @param usuario
     * @return
     */
    public String generateToken(Usuario usuario){
        TokenDataDTO tokenDataDTO = TokenDataDTO
                .builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .rol(usuario.getRol().name())
                .fecha_creacion(System.currentTimeMillis())
                .fecha_expiracion(System.currentTimeMillis() + 1000 * 60 * 60 * 3)
                .build();

        return Jwts
                .builder()
                .claim("tokenDataDTO", tokenDataDTO)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims extractDatosToken(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public TokenDataDTO extractTokenData(String token){
        Claims claims = extractDatosToken(token);
        Map<String, Object> mapa = (LinkedHashMap<String, Object>) claims.get("tokenDataDTO");

        if (mapa == null) {
            throw new IllegalArgumentException("Token data is missing");
        }

        String username = (String) mapa.get("username");
        Number fechaCreacion = (Number) mapa.get("fecha_creacion");
        Number fechaExpiracion = (Number) mapa.get("fecha_expiracion");
        String rol = (String) mapa.get("rol");
        Number id = (Number) mapa.get("id");

        if (fechaCreacion == null || fechaExpiracion == null || id == null) {
            throw new IllegalArgumentException("Token data contains null values");
        }

        return TokenDataDTO.builder()
                .username(username)
                .fecha_creacion(fechaCreacion.longValue())
                .fecha_expiracion(fechaExpiracion.longValue())
                .rol(rol)
                .id(id.longValue())
                .build();
    }

    /**
     * Método que me dice si el token a expirado
     * @param token
     * @return
     */
    public boolean isExpired(String token){
        return new Date(extractTokenData(token).getFecha_expiracion()).before(new Date()) ;
    }

    private Key getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
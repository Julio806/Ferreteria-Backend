package ferreteria_backend.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {

        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException(
                    "La clave JWT debe tener al menos 32 caracteres"
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        this.expirationMs = expirationMs;
    }

    // ==========================================
    // GENERAR TOKEN
    // ==========================================

    public String generarToken(
            String username,
            String rol
    ) {

        Date ahora = new Date();

        Date expiracion = new Date(
                ahora.getTime() + expirationMs
        );

        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(secretKey)
                .compact();
    }

    // ==========================================
    // OBTENER USERNAME
    // ==========================================

    public String obtenerUsername(String token) {

        return obtenerClaims(token)
                .getSubject();
    }

    // ==========================================
    // OBTENER ROL
    // ==========================================

    public String obtenerRol(String token) {

        return obtenerClaims(token)
                .get("rol", String.class);
    }

    // ==========================================
    // VALIDAR TOKEN
    // ==========================================

    public boolean esTokenValido(String token) {

        try {

            obtenerClaims(token);

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    // ==========================================
    // OBTENER CLAIMS
    // ==========================================

    private Claims obtenerClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
package com.barberia.shared.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String JWT_SECRET = "mySecretKeyForJWTTokenGenerationAndValidationWithMinimumLength123456789";
    private static final long JWT_EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();

        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", JWT_EXPIRATION);
    }

    @Test
    void generateToken_DeberiaGenerarTokenValido() {
        String token = jwtTokenProvider.generateToken("usuario@test.com", "123456789", 3);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void getEmailFromToken_DeberiaRetornarEmailDelToken() {
        String token = jwtTokenProvider.generateToken("usuario@test.com", "123456789", 3);

        String email = jwtTokenProvider.getEmailFromToken(token);

        assertEquals("usuario@test.com", email);
    }

    @Test
    void getNumeroDocumentoFromToken_DeberiaRetornarNumeroDocumentoDelToken() {
        String token = jwtTokenProvider.generateToken("usuario@test.com", "123456789", 3);

        String numeroDocumento = jwtTokenProvider.getNumeroDocumentoFromToken(token);

        assertEquals("123456789", numeroDocumento);
    }

    @Test
    void getRolFromToken_DeberiaRetornarRolDelToken() {
        String token = jwtTokenProvider.generateToken("usuario@test.com", "123456789", 3);

        Integer rol = jwtTokenProvider.getRolFromToken(token);

        assertEquals(3, rol);
    }

    @Test
    void validateToken_DeberiaRetornarFalseCuandoTokenEsInvalido() {
        boolean resultado = jwtTokenProvider.validateToken("token-invalido");

        assertFalse(resultado);
    }

    @Test
    void validateToken_DeberiaRetornarFalseCuandoTokenEstaExpirado() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", -1000L);

        String token = jwtTokenProvider.generateToken("usuario@test.com", "123456789", 3);

        boolean resultado = jwtTokenProvider.validateToken(token);

        assertFalse(resultado);
    }
}
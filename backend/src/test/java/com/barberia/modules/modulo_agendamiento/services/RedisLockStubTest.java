package com.barberia.modules.modulo_agendamiento.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link RedisLockStub} siguiendo el patrón AAA.
 *
 * El stub es una implementación temporal hasta integrar Redis real.
 * Estos tests documentan el comportamiento actual.
 */
class RedisLockStubTest {

    private RedisLockStub redisLockStub;

    @BeforeEach
    void setUp() {
        redisLockStub = new RedisLockStub();
    }

    @Test
    @DisplayName("acquire siempre retorna false en la implementación stub")
    void acquire_siempreRetornaFalse() {
        // Arrange
        String key = "lock:peluquero-200:2026-06-01";
        long ttl = 5000;

        // Act
        boolean resultado = redisLockStub.acquire(key, ttl);

        // Assert
        assertFalse(resultado);
    }

    @Test
    @DisplayName("acquire retorna false incluso con key null")
    void acquire_conKeyNull_retornaFalse() {
        // Arrange + Act
        boolean resultado = redisLockStub.acquire(null, 1000);

        // Assert
        assertFalse(resultado);
    }

    @Test
    @DisplayName("release no lanza excepción aunque no haya lock previo")
    void release_noLanzaExcepcionSinLockPrevio() {
        // Arrange + Act + Assert
        assertDoesNotThrow(() -> redisLockStub.release("cualquier-key"));
    }

    @Test
    @DisplayName("release no lanza excepción con key null")
    void release_conKeyNull_noLanzaExcepcion() {
        assertDoesNotThrow(() -> redisLockStub.release(null));
    }
}

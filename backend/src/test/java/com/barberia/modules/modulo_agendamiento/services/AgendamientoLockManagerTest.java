package com.barberia.modules.modulo_agendamiento.services;

import com.barberia.modules.modulo_agendamiento.services.AgendamientoLockManager.LockHandle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link AgendamientoLockManager} siguiendo el patrón AAA.
 */
class AgendamientoLockManagerTest {

    private AgendamientoLockManager lockManager;

    @BeforeEach
    void setUp() {
        lockManager = new AgendamientoLockManager();
    }

    @Test
    @DisplayName("acquire retorna LockHandle con el resourceKey correcto")
    void acquire_conKeyValida_retornaLockHandleConKey() {
        // Arrange
        String key = "peluquero-200-2026-06-01";

        // Act
        LockHandle handle = lockManager.acquire(key);

        // Assert
        assertNotNull(handle);
        assertEquals(key, handle.getKey());
        handle.close(); // limpieza
    }

    @Test
    @DisplayName("acquire lanza NullPointerException cuando la key es null")
    void acquire_conKeyNull_lanzaNullPointerException() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> lockManager.acquire(null));
    }

    @Test
    @DisplayName("close libera el lock y permite que otro hilo lo adquiera con tryAcquire")
    void close_liberaLockYPermiteAdquirirloDeNuevo() throws InterruptedException {
        // Arrange
        String key = "peluquero-100";
        LockHandle primero = lockManager.acquire(key);

        // Act
        primero.close();
        LockHandle segundo = lockManager.tryAcquire(key, 100, TimeUnit.MILLISECONDS);

        // Assert
        assertNotNull(segundo);
        segundo.close();
    }

    @Test
    @DisplayName("tryAcquire retorna null cuando otro hilo ya tiene el lock")
    void tryAcquire_conLockOcupado_retornaNull() throws InterruptedException {
        // Arrange
        String key = "peluquero-300";
        CountDownLatch lockAdquirido = new CountDownLatch(1);
        CountDownLatch testTermina = new CountDownLatch(1);
        AtomicBoolean otroHiloTieneLock = new AtomicBoolean(false);

        Thread holder = new Thread(() -> {
            LockHandle handle = lockManager.acquire(key);
            otroHiloTieneLock.set(true);
            lockAdquirido.countDown();
            try {
                testTermina.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                handle.close();
            }
        });
        holder.start();
        lockAdquirido.await(1, TimeUnit.SECONDS);

        // Act
        LockHandle resultado = lockManager.tryAcquire(key, 50, TimeUnit.MILLISECONDS);

        // Assert
        assertNull(resultado);
        assertTrue(otroHiloTieneLock.get());

        // Limpieza
        testTermina.countDown();
        holder.join(1000);
    }

    @Test
    @DisplayName("tryAcquire retorna LockHandle cuando el lock está libre")
    void tryAcquire_conLockLibre_retornaLockHandle() throws InterruptedException {
        // Arrange
        String key = "peluquero-400";

        // Act
        LockHandle handle = lockManager.tryAcquire(key, 100, TimeUnit.MILLISECONDS);

        // Assert
        assertNotNull(handle);
        assertEquals(key, handle.getKey());
        handle.close();
    }

    @Test
    @DisplayName("tryAcquire lanza NullPointerException cuando la key es null")
    void tryAcquire_conKeyNull_lanzaNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> lockManager.tryAcquire(null, 100, TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("acquire es reentrante para el mismo hilo")
    void acquire_esReentranteEnElMismoHilo() {
        // Arrange
        String key = "peluquero-500";

        // Act
        LockHandle primero = lockManager.acquire(key);
        LockHandle segundo = lockManager.acquire(key); // mismo hilo, no debe bloquear

        // Assert
        assertNotNull(primero);
        assertNotNull(segundo);
        segundo.close();
        primero.close();
    }
}

package com.barberia.modules.modulo_horarios.services;

import com.barberia.modules.modulo_servicios.repositories.ServicioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link EspecialidadesPeluqueroService} siguiendo el patrón AAA.
 *
 * Como el service usa inyección por campo (@PersistenceContext + @Resource),
 * inyectamos los mocks manualmente vía reflexión.
 */
@ExtendWith(MockitoExtension.class)
class EspecialidadesPeluqueroServiceTest {

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private EspecialidadesPeluqueroService especialidadesPeluqueroService;

    @BeforeEach
    void setUp() throws Exception {
        // Inyectamos los mocks por reflexión porque los campos usan
        // @PersistenceContext y @Resource (no constructor)
        setField(especialidadesPeluqueroService, "entityManager", entityManager);
        setField(especialidadesPeluqueroService, "servicioRepository", servicioRepository);
    }

    @Test
    @DisplayName("asociarServicio ejecuta el insert cuando los parámetros son válidos")
    void asociarServicio_conDatosValidos_ejecutaInsert() {
        // Arrange
        when(servicioRepository.existsById(10L)).thenReturn(true);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        // Act
        especialidadesPeluqueroService.asociarServicio("123", 10L);

        // Assert
        verify(entityManager).createNativeQuery(anyString());
        verify(query).executeUpdate();
    }

    @Test
    @DisplayName("asociarServicio lanza IllegalArgumentException con documento null")
    void asociarServicio_conDocumentoNull_lanzaIllegalArgumentException() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> especialidadesPeluqueroService.asociarServicio(null, 10L));
        verifyNoInteractions(entityManager);
    }

    @Test
    @DisplayName("asociarServicio lanza IllegalArgumentException con documento blanco")
    void asociarServicio_conDocumentoBlanco_lanzaIllegalArgumentException() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> especialidadesPeluqueroService.asociarServicio("   ", 10L));
        verifyNoInteractions(entityManager);
    }

    @Test
    @DisplayName("asociarServicio lanza NullPointerException con idServicio null")
    void asociarServicio_conIdServicioNull_lanzaNullPointerException() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class,
                () -> especialidadesPeluqueroService.asociarServicio("123", null));
        verifyNoInteractions(entityManager);
    }

    @Test
    @DisplayName("asociarServicio lanza IllegalArgumentException cuando el servicio no existe")
    void asociarServicio_conServicioInexistente_lanzaIllegalArgumentException() {
        // Arrange
        when(servicioRepository.existsById(99L)).thenReturn(false);

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> especialidadesPeluqueroService.asociarServicio("123", 99L));
        assertTrue(ex.getMessage().contains("99"));
        verifyNoInteractions(entityManager);
    }

    /**
     * Setea un campo privado vía reflexión.
     */
    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}

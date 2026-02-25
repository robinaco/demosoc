package com.example.sonardemo.service;

import com.example.sonardemo.entity.Persona;
import com.example.sonardemo.repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonaServiceTest {

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private PersonaService personaService;

    private Persona persona;

    @BeforeEach
    void setUp() {
        persona = new Persona(1L, "Juan Pérez", "juan@email.com", 30);
    }

    @Test
    void guardar() {
        when(personaRepository.save(any(Persona.class))).thenReturn(persona);

        Persona resultado = personaService.guardar(persona);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(personaRepository, times(1)).save(persona);
    }

    @Test
    void guardarConEdadNegativaLanzaExcepcion() {
        Persona personaInvalida = new Persona(null, "Test", "test@email.com", -5);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            personaService.guardar(personaInvalida);
        });

        assertEquals("La edad no puede ser negativa", exception.getMessage());
        verify(personaRepository, never()).save(any());
    }

    @Test
    void obtenerPorId_CuandoExiste() {
        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));

        Optional<Persona> resultado = personaService.obtenerPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Juan Pérez", resultado.get().getNombre());
    }

    @Test
    void obtenerPorId_CuandoNoExiste() {
        when(personaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Persona> resultado = personaService.obtenerPorId(99L);

        assertFalse(resultado.isPresent());
    }

    @Test
    void listar() {
        List<Persona> personas = Arrays.asList(
                persona,
                new Persona(2L, "María García", "maria@email.com", 25)
        );
        when(personaRepository.findAll()).thenReturn(personas);

        List<Persona> resultado = personaService.listar();

        assertEquals(2, resultado.size());
        verify(personaRepository, times(1)).findAll();
    }

    @Test
    void eliminar_DeberiaLlamarAlRepositorio() {
        // given
        Long id = 1L;
        doNothing().when(personaRepository).deleteById(id);

        // when
        personaService.eliminar(id);

        // then
        verify(personaRepository, times(1)).deleteById(id);
    }

    @Test
    void actualizar_CuandoExiste_DeberiaActualizarYRetornar() {
        // given
        Long id = 1L;
        Persona personaExistente = new Persona(id, "Juan", "juan@email.com", 30);
        Persona personaActualizada = new Persona(null, "Juan Actualizado", "juan.nuevo@email.com", 31);

        when(personaRepository.findById(id)).thenReturn(Optional.of(personaExistente));
        when(personaRepository.save(any(Persona.class))).thenReturn(personaExistente);

        // when
        Persona resultado = personaService.actualizar(id, personaActualizada);

        // then
        assertNotNull(resultado);
        assertEquals("Juan Actualizado", resultado.getNombre());
        assertEquals("juan.nuevo@email.com", resultado.getEmail());
        assertEquals(31, resultado.getEdad());
        verify(personaRepository, times(1)).findById(id);
        verify(personaRepository, times(1)).save(personaExistente);
    }

    @Test
    void actualizar_CuandoNoExiste_DeberiaLanzarExcepcion() {
        // given
        Long id = 999L;
        Persona personaActualizada = new Persona(null, "Test", "test@email.com", 25);

        when(personaRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            personaService.actualizar(id, personaActualizada);
        });

        assertEquals("Persona no encontrada con id: " + id, exception.getMessage());
        verify(personaRepository, times(1)).findById(id);
        verify(personaRepository, never()).save(any());
    }



}
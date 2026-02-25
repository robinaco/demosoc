package com.example.sonardemo.repository;

import com.example.sonardemo.entity.Persona;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PersonaRepositoryTest {

    @Autowired
    private PersonaRepository personaRepository;

    @Test
    void saveAndFindById() {
        Persona persona = new Persona(null, "Test", "test@email.com", 30);
        Persona guardada = personaRepository.save(persona);

        Optional<Persona> encontrada = personaRepository.findById(guardada.getId());

        assertTrue(encontrada.isPresent());
        assertEquals("Test", encontrada.get().getNombre());
    }

    @Test
    void findAll() {
        personaRepository.save(new Persona(null, "Test1", "test1@email.com", 25));
        personaRepository.save(new Persona(null, "Test2", "test2@email.com", 30));

        var personas = personaRepository.findAll();

        assertTrue(personas.size() >= 2);
    }

    @Test
    void deleteById() {
        Persona persona = personaRepository.save(new Persona(null, "Test", "test@email.com", 30));

        personaRepository.deleteById(persona.getId());

        Optional<Persona> encontrada = personaRepository.findById(persona.getId());
        assertFalse(encontrada.isPresent());
    }
}

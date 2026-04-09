package com.example.dto;

import com.example.dto.PersonaDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonaDTOTest {

    @Test
    void shouldCoverLombokGeneratedMethods() {
        PersonaDTO dto1 = new PersonaDTO();
        dto1.setId(1L);
        dto1.setNombre("Robinson");
        dto1.setEmail("robinson@test.com");
        dto1.setEdad(30);

        PersonaDTO dto2 = new PersonaDTO();
        dto2.setId(1L);
        dto2.setNombre("Robinson");
        dto2.setEmail("robinson@test.com");
        dto2.setEdad(30);

        // getters
        assertEquals(1L, dto1.getId());
        assertEquals("Robinson", dto1.getNombre());
        assertEquals("robinson@test.com", dto1.getEmail());
        assertEquals(30, dto1.getEdad());

        // equals / hashCode
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        // toString
        assertNotNull(dto1.toString());
        assertTrue(dto1.toString().contains("Robinson"));
    }
}